package com.rental.domain;

import java.math.BigDecimal;

/**
 * Pattern Role: Concrete Product.
 * Represents an electric/kick scooter in the rental service.
 */
public class Scooter extends Vehicle {

    public Scooter(String id,
                   String brand,
                   String model,
                   int seats,
                   String fuel,
                   BigDecimal dailyRate,
                   boolean available) {
        super(id, VehicleType.SCOOTER, brand, model, seats, fuel, dailyRate, available);
    }
}
