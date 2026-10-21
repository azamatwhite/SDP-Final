package com.rental.domain;

import com.rental.creation.VehicleBuilder;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Pattern Role: Product (Abstract Product in Factory Method & Target Product in Builder).
 * Base abstract class representing any rental vehicle in the system.
 */
public abstract class Vehicle {

    private final String id;
    private final VehicleType type;
    private final String brand;
    private final String model;
    private final int seats;
    private final String fuel;
    private final BigDecimal dailyRate;
    private boolean available;

    protected Vehicle(String id,
                      VehicleType type,
                      String brand,
                      String model,
                      int seats,
                      String fuel,
                      BigDecimal dailyRate,
                      boolean available) {
        this.id = id;
        this.type = type;
        this.brand = brand;
        this.model = model;
        this.seats = seats;
        this.fuel = fuel;
        this.dailyRate = dailyRate;
        this.available = available;
    }

    /**
     * Entry point to the fluent Builder pattern for creating vehicles.
     *
     * @return a new VehicleBuilder instance
     */
    public static VehicleBuilder builder() {
        return new VehicleBuilder();
    }

    public String getId() {
        return id;
    }

    public VehicleType getType() {
        return type;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getSeats() {
        return seats;
    }

    public String getFuel() {
        return fuel;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle vehicle)) return false;
        return Objects.equals(id, vehicle.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", seats=" + seats +
                ", fuel='" + fuel + '\'' +
                ", dailyRate=" + dailyRate +
                ", available=" + available +
                '}';
    }
}
