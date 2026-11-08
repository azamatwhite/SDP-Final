package com.rental.service;

import com.rental.domain.Vehicle;
import com.rental.notification.NotificationService;
import com.rental.payment.PaymentGateway;
import com.rental.pricing.Quote;
import com.rental.pricing.QuoteService;
import com.rental.repo.VehicleRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findAll().stream().filter(Vehicle::isAvailable).toList();
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

    // The Facade coordinates availability, pricing, payment, reservation, and confirmation.
    public Reservation checkout(String vehicleId, int rentalDays, String customerId, String email,
                                String pricing, Collection<String> extras) {
        validateRentalDays(rentalDays);
        requireText(customerId, "Customer ID");
        requireText(email, "Email");

        synchronized (lock) {
            Vehicle vehicle = findVehicle(vehicleId);
            if (!vehicle.isAvailable()) {
                throw new VehicleUnavailableException(vehicleId);
            }

            BigDecimal total = price(quoteService.build(vehicle, rentalDays, pricing, extras));
            if (!paymentGateway.charge(customerId, total)) {
                throw new PaymentFailedException();
            }

            Reservation reservation = new Reservation(
                    UUID.randomUUID().toString(), vehicleId, customerId, email, rentalDays, total);
            reservationRepository.save(reservation);
            vehicle.setAvailable(false);
            vehicleRepository.save(vehicle);
            notificationService.send(email, "Booking confirmed. Reservation " + reservation.getId()
                    + " total: $" + total.toPlainString());
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
                v.setAvailable(true);
                vehicleRepository.save(v);
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
