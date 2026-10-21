package com.rental.domain;

import java.math.BigDecimal;

/**
 * Pattern Role: Concrete Product.
 * Represents a passenger car in the rental service.
 */
public class Car extends Vehicle {

    public Car(String id,
               String brand,
               String model,
               int seats,
               String fuel,
               BigDecimal dailyRate,
               boolean available) {
        super(id, VehicleType.CAR, brand, model, seats, fuel, dailyRate, available);
    }
}
