package com.rental.domain;

import java.math.BigDecimal;

/**
 * Pattern Role: Concrete Product.
 * Represents a motorcycle in the rental service.
 */
public class Motorcycle extends Vehicle {

    public Motorcycle(String id,
                      String brand,
                      String model,
                      int seats,
                      String fuel,
                      BigDecimal dailyRate,
                      boolean available) {
        super(id, VehicleType.MOTORCYCLE, brand, model, seats, fuel, dailyRate, available);
    }
}
