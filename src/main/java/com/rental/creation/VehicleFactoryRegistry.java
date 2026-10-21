package com.rental.creation;

import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Registry mapping each VehicleType to its corresponding concrete VehicleFactory.
 * Coordinates factory lookup and vehicle creation without global static state.
 */
public class VehicleFactoryRegistry {

    private final Map<VehicleType, VehicleFactory> factories = new EnumMap<>(VehicleType.class);

    /**
     * Initializes the registry with default concrete factories for all supported VehicleTypes.
     */
    public VehicleFactoryRegistry() {
        registerFactory(VehicleType.CAR, new CarFactory());
        registerFactory(VehicleType.MOTORCYCLE, new MotorcycleFactory());
        registerFactory(VehicleType.SCOOTER, new ScooterFactory());
    }

    /**
     * Registers a factory for a given vehicle type.
     *
     * @param type    the vehicle type
     * @param factory the factory instance
     */
    public void registerFactory(VehicleType type, VehicleFactory factory) {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("Vehicle factory must not be null");
        }
        factories.put(type, factory);
    }

    /**
     * Retrieves the factory registered for the given type.
     *
     * @param type the vehicle type
     * @return the registered VehicleFactory
     * @throws IllegalArgumentException if type is null or has no registered factory
     */
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

    /**
     * Creates a Vehicle using the factory registered for the given type.
     */
    public Vehicle create(VehicleType type,
                          String brand,
                          String model,
                          int seats,
                          String fuel,
                          BigDecimal dailyRate) {
        return getFactory(type).createVehicle(brand, model, seats, fuel, dailyRate);
    }

    /**
     * Creates a Vehicle with specific ID and availability using the registered factory.
     */
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

    /**
     * Returns an unmodifiable view of currently registered factories.
     */
    public Map<VehicleType, VehicleFactory> getRegisteredFactories() {
        return Collections.unmodifiableMap(factories);
    }
}
