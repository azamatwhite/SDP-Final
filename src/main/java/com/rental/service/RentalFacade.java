package com.rental.service;

import com.rental.domain.Vehicle;
import com.rental.notification.NotificationService;
import com.rental.payment.PaymentGateway;
import com.rental.pricing.Quote;
import com.rental.pricing.QuoteService;
import com.rental.repo.VehicleRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class RentalFacade {
    private final VehicleRepository vehicleRepository;
    private final QuoteService quoteService;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;
    private final Object lock = new Object();

    public RentalFacade(VehicleRepository vehicleRepository,
                        QuoteService quoteService,
                        PaymentGateway paymentGateway,
                        ReservationRepository reservationRepository,
                        NotificationService notificationService) {
        if (vehicleRepository == null || quoteService == null || paymentGateway == null
                || reservationRepository == null || notificationService == null) {
            throw new IllegalArgumentException("RentalFacade dependencies must not be null");
        }
        this.vehicleRepository = vehicleRepository;
        this.quoteService = quoteService;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    public List<Vehicle> findVehicles(String search, String type, String sort) {
        Stream<Vehicle> stream = vehicleRepository.findAll().stream();

        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type.trim())) {
            stream = stream.filter(v -> v.getType().name().equalsIgnoreCase(type.trim()));
        }

        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            stream = stream.filter(v ->
                    (v.getBrand() != null && v.getBrand().toLowerCase().contains(q)) ||
                    (v.getModel() != null && v.getModel().toLowerCase().contains(q)));
        }

        if ("priceAsc".equalsIgnoreCase(sort)) {
            stream = stream.sorted(Comparator.comparing(Vehicle::getDailyRate));
        } else if ("priceDesc".equalsIgnoreCase(sort)) {
            stream = stream.sorted(Comparator.comparing(Vehicle::getDailyRate).reversed());
        } else if ("seats".equalsIgnoreCase(sort)) {
            stream = stream.sorted(Comparator.comparingInt(Vehicle::getSeats).reversed());
        }

        return stream.toList();
    }

    public boolean isVehicleAvailableForDates(String vehicleId, LocalDate start, LocalDate end) {
        if (start == null) start = LocalDate.now();
        if (end == null) end = start.plusDays(1);
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Return date must be after pickup date");
        }
        Vehicle vehicle = findVehicle(vehicleId);

        for (Reservation r : reservationRepository.findAll()) {
            if (r.getVehicleId().equals(vehicleId) && r.getStatus() == ReservationStatus.ACTIVE) {
                LocalDate rStart = r.getStartDate() != null ? r.getStartDate() : LocalDate.now();
                LocalDate rEnd = r.getEndDate() != null ? r.getEndDate() : rStart.plusDays(Math.max(1, r.getRentalDays()));
                if (start.isBefore(rEnd) && end.isAfter(rStart)) {
                    return false;
                }
            }
        }

        if (!vehicle.isAvailable()) {
            boolean activeReservationToday = reservationRepository.findAll().stream()
                    .filter(r -> r.getVehicleId().equals(vehicleId) && r.getStatus() == ReservationStatus.ACTIVE)
                    .anyMatch(r -> {
                        LocalDate rs = r.getStartDate() != null ? r.getStartDate() : LocalDate.now();
                        LocalDate re = r.getEndDate() != null ? r.getEndDate() : rs.plusDays(Math.max(1, r.getRentalDays()));
                        return !LocalDate.now().isBefore(rs) && LocalDate.now().isBefore(re);
                    });
            if (!activeReservationToday) {
                return false;
            }
        }

        return true;
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public static BigDecimal price(Quote quote) {
        return quote.total().setScale(2, RoundingMode.HALF_UP);
    }

    public Quote buildQuote(String vehicleId, int rentalDays, String pricing, Collection<String> extras) {
        validateRentalDays(rentalDays);
        return quoteService.build(findVehicle(vehicleId), rentalDays, pricing, extras);
    }

    public BigDecimal quote(String vehicleId, int rentalDays) {
        return price(buildQuote(vehicleId, rentalDays, "DAILY", List.of()));
    }

    public Reservation checkout(String vehicleId, int rentalDays, String customerId, String email) {
        return checkout(vehicleId, rentalDays, customerId, email, "DAILY", List.of());
    }

    public Reservation checkout(String vehicleId, int rentalDays, String customerId, String email,
                                String pricing, Collection<String> extras) {
        return checkout(vehicleId, rentalDays, customerId, email, pricing, extras, null, null);
    }

    public Reservation checkout(String vehicleId, int rentalDays, String customerId, String email,
                                String pricing, Collection<String> extras,
                                LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (!endDate.isAfter(startDate)) {
                throw new IllegalArgumentException("Return date must be after pickup date");
            }
            rentalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        }
        validateRentalDays(rentalDays);
        requireText(customerId, "Customer ID");
        requireText(email, "Email");

        synchronized (lock) {
            Vehicle vehicle = findVehicle(vehicleId);
            LocalDate start = startDate != null ? startDate : LocalDate.now();
            LocalDate end = endDate != null ? endDate : start.plusDays(rentalDays);

            if (!isVehicleAvailableForDates(vehicleId, start, end)) {
                throw new VehicleUnavailableException(vehicleId);
            }

            BigDecimal total = price(quoteService.build(vehicle, rentalDays, pricing, extras));
            if (!paymentGateway.charge(customerId, total)) {
                throw new PaymentFailedException();
            }

            Reservation reservation = new Reservation(
                    UUID.randomUUID().toString(), vehicleId, customerId, email, rentalDays, total,
                    ReservationStatus.ACTIVE, start, end);
            reservationRepository.save(reservation);

            LocalDate today = LocalDate.now();
            if (!today.isBefore(start) && today.isBefore(end)) {
                vehicle.setAvailable(false);
                vehicleRepository.save(vehicle);
            } else if (start.isEqual(today)) {
                vehicle.setAvailable(false);
                vehicleRepository.save(vehicle);
            }

            notificationService.send(email, "Booking confirmed. Reservation " + reservation.getId()
                    + " total: " + total.toPlainString() + " KZT");
            return reservation;
        }
    }

    public Reservation cancel(String reservationId) {
        return finish(reservationId, ReservationStatus.CANCELLED);
    }

    public Reservation complete(String reservationId) {
        return finish(reservationId, ReservationStatus.COMPLETED);
    }

    private Reservation finish(String reservationId, ReservationStatus status) {
        synchronized (lock) {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new ReservationNotFoundException(reservationId));
            if (reservation.getStatus() != ReservationStatus.ACTIVE) {
                throw new IllegalStateException("Reservation is already " + reservation.getStatus());
            }
            reservation.setStatus(status);
            reservationRepository.save(reservation);
            vehicleRepository.findById(reservation.getVehicleId()).ifPresent(v -> {
                LocalDate today = LocalDate.now();
                boolean busyToday = reservationRepository.findAll().stream()
                        .filter(r -> r.getVehicleId().equals(v.getId()) && r.getStatus() == ReservationStatus.ACTIVE)
                        .anyMatch(r -> {
                            LocalDate rs = r.getStartDate() != null ? r.getStartDate() : today;
                            LocalDate re = r.getEndDate() != null ? r.getEndDate() : rs.plusDays(Math.max(1, r.getRentalDays()));
                            return !today.isBefore(rs) && today.isBefore(re);
                        });
                if (!busyToday) {
                    v.setAvailable(true);
                    vehicleRepository.save(v);
                }
            });
            return reservation;
        }
    }

    private Vehicle findVehicle(String vehicleId) {
        requireText(vehicleId, "Vehicle ID");
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
    }

    private void validateRentalDays(int rentalDays) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental duration must be greater than 0 days");
        }
    }

    private void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
