package com.rental.service;

import com.rental.domain.Vehicle;
import com.rental.notification.NotificationService;
import com.rental.payment.PaymentGateway;
import com.rental.repo.VehicleRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RentalFacade {
    private final VehicleRepository vehicleRepository;
    private final PricingStrategy pricingStrategy;
    private final PaymentGateway paymentGateway;
    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    public RentalFacade(VehicleRepository vehicleRepository,
                        PricingStrategy pricingStrategy,
                        PaymentGateway paymentGateway,
                        ReservationRepository reservationRepository,
                        NotificationService notificationService) {
        if (vehicleRepository == null || pricingStrategy == null || paymentGateway == null
                || reservationRepository == null || notificationService == null) {
            throw new IllegalArgumentException("RentalFacade dependencies must not be null");
        }
        this.vehicleRepository = vehicleRepository;
        this.pricingStrategy = pricingStrategy;
        this.paymentGateway = paymentGateway;
        this.reservationRepository = reservationRepository;
        this.notificationService = notificationService;
    }

    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> availableVehicles = new ArrayList<>();
        for (Vehicle vehicle : vehicleRepository.findAll()) {
            if (vehicle.isAvailable()) {
                availableVehicles.add(vehicle);
            }
        }
        return availableVehicles;
    }

    public BigDecimal quote(String vehicleId, int rentalDays) {
        validateRentalDays(rentalDays);
        Vehicle vehicle = findVehicle(vehicleId);
        return pricingStrategy.calculatePrice(vehicle, rentalDays);
    }

    // The Facade coordinates availability, pricing, payment, reservation, and confirmation.
    public Reservation checkout(String vehicleId, int rentalDays, String customerId, String email) {
        validateRentalDays(rentalDays);
        requireText(customerId, "Customer ID");
        requireText(email, "Email");

        Vehicle vehicle = findVehicle(vehicleId);
        synchronized (vehicle) {
            if (!vehicle.isAvailable()) {
                throw new VehicleUnavailableException(vehicleId);
            }

            BigDecimal totalPrice = pricingStrategy.calculatePrice(vehicle, rentalDays);
            if (!paymentGateway.charge(customerId, totalPrice)) {
                throw new PaymentFailedException();
            }

            Reservation reservation = new Reservation(
                    UUID.randomUUID().toString(), vehicleId, customerId, email, rentalDays, totalPrice);
            reservationRepository.save(reservation);
            vehicle.setAvailable(false);
            notificationService.send(email, "Booking confirmed. Reservation " + reservation.getId()
                    + " total: $" + totalPrice.toPlainString());
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
