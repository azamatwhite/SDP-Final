package com.rental.creation;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;

public abstract class VehicleFactory {

    public Vehicle createVehicle(String brand, String model, int seats, String fuel, BigDecimal dailyRate) {
        return createVehicle(null, brand, model, seats, fuel, dailyRate, true);
    }

    public abstract Vehicle createVehicle(String id,
                                         String brand,
                                         String model,
                                         int seats,
                                         String fuel,
                                         BigDecimal dailyRate,
                                         boolean available);
}
