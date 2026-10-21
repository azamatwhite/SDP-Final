package com.rental.creation;

import com.rental.domain.Car;
import com.rental.domain.Motorcycle;
import com.rental.domain.Scooter;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pattern Role: Builder.
 * Provides a fluent interface to construct Vehicle instances with validation.
 */
public class VehicleBuilder {

    private String id;
    private VehicleType type;
    private String brand;
    private String model;
    private int seats;
    private String fuel;
    private BigDecimal dailyRate;
    private boolean available = true;

    public VehicleBuilder id(String id) {
        this.id = id;
        return this;
    }

    public VehicleBuilder type(VehicleType type) {
        this.type = type;
        return this;
    }

    public VehicleBuilder brand(String brand) {
        this.brand = brand;
        return this;
    }

    public VehicleBuilder model(String model) {
        this.model = model;
        return this;
    }

    public VehicleBuilder seats(int seats) {
        this.seats = seats;
        return this;
    }

    public VehicleBuilder fuel(String fuel) {
        this.fuel = fuel;
        return this;
    }

    public VehicleBuilder dailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
        return this;
    }

    public VehicleBuilder available(boolean available) {
        this.available = available;
        return this;
    }

    /**
     * Validates input parameters and builds the corresponding Vehicle concrete product.
     *
     * @return a validated Vehicle subclass instance
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public Vehicle build() {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand must not be blank");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model must not be blank");
        }
        if (seats <= 0) {
            throw new IllegalArgumentException("Seats must be greater than 0");
        }
        if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Daily rate must be greater than 0");
        }

        String effectiveId = (id != null && !id.trim().isEmpty()) ? id : UUID.randomUUID().toString();
        String effectiveFuel = (fuel != null) ? fuel : "N/A";

        return switch (type) {
            case CAR -> new Car(effectiveId, brand.trim(), model.trim(), seats, effectiveFuel, dailyRate, available);
            case MOTORCYCLE -> new Motorcycle(effectiveId, brand.trim(), model.trim(), seats, effectiveFuel, dailyRate, available);
            case SCOOTER -> new Scooter(effectiveId, brand.trim(), model.trim(), seats, effectiveFuel, dailyRate, available);
        };
    }
}
