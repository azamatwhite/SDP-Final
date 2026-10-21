package com.rental.creation;

import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;

/**
 * Pattern Role: Concrete Creator.
 * Creates Car products using the VehicleBuilder.
 */
public class CarFactory extends VehicleFactory {

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
                .type(VehicleType.CAR)
                .brand(brand)
                .model(model)
                .seats(seats)
                .fuel(fuel)
                .dailyRate(dailyRate)
                .available(available)
                .build();
    }
}
