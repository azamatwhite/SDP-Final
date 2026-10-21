package com.rental.creation;

import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;

public class ScooterFactory extends VehicleFactory {

    @Override
    public Vehicle createVehicle(String id,
                                 String brand,
                                 String model,
                                 int seats,
                                 String fuel,
                                 BigDecimal dailyRate,
                                 boolean available) {
        return Vehicle.builder()
                .id(id)
                .type(VehicleType.SCOOTER)
                .brand(brand)
                .model(model)
                .seats(seats)
                .fuel(fuel)
                .dailyRate(dailyRate)
                .available(available)
                .build();
    }
}
