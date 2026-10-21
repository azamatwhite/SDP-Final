package com.rental.creation;

import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class VehicleFactoryRegistry {

    private final Map<VehicleType, VehicleFactory> factories = new EnumMap<>(VehicleType.class);

    public VehicleFactoryRegistry() {
        registerFactory(VehicleType.CAR, new CarFactory());
        registerFactory(VehicleType.MOTORCYCLE, new MotorcycleFactory());
        registerFactory(VehicleType.SCOOTER, new ScooterFactory());
    }

    public void registerFactory(VehicleType type, VehicleFactory factory) {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("Vehicle factory must not be null");
        }
        factories.put(type, factory);
    }

    public VehicleFactory getFactory(VehicleType type) {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        VehicleFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown or unsupported vehicle type: " + type);
        }
        return factory;
    }

    public Vehicle create(VehicleType type,
                          String brand,
                          String model,
                          int seats,
                          String fuel,
                          BigDecimal dailyRate) {
        return getFactory(type).createVehicle(brand, model, seats, fuel, dailyRate);
    }

    public Vehicle create(VehicleType type,
                          String id,
                          String brand,
                          String model,
                          int seats,
                          String fuel,
                          BigDecimal dailyRate,
                          boolean available) {
        return getFactory(type).createVehicle(id, brand, model, seats, fuel, dailyRate, available);
    }

    public Map<VehicleType, VehicleFactory> getRegisteredFactories() {
        return Collections.unmodifiableMap(factories);
    }
}
