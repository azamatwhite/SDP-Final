package com.rental.creation;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;

/**
 * Pattern Role: Creator.
 * Abstract creator declaring the factory method for producing Vehicle products.
 * Concrete subclasses override the factory method to produce specific Vehicle subtypes.
 */
public abstract class VehicleFactory {

    /**
     * Factory method creating a Vehicle with auto-generated ID and default availability (true).
     *
     * @param brand     the vehicle brand
     * @param model     the vehicle model
     * @param seats     the seat count
     * @param fuel      the fuel or power source
     * @param dailyRate the price per rental day
     * @return a created and validated Vehicle instance
     */
    public Vehicle createVehicle(String brand, String model, int seats, String fuel, BigDecimal dailyRate) {
        return createVehicle(null, brand, model, seats, fuel, dailyRate, true);
    }

    /**
     * Factory method creating a Vehicle with custom ID and availability.
     * Concrete creators implement this method using VehicleBuilder.
     *
     * @param id        the unique identifier (null for auto-generation)
     * @param brand     the vehicle brand
     * @param model     the vehicle model
     * @param seats     the seat count
     * @param fuel      the fuel or power source
     * @param dailyRate the price per rental day
     * @param available initial availability status
     * @return a created and validated Vehicle instance
     */
    public abstract Vehicle createVehicle(String id,
                                         String brand,
                                         String model,
                                         int seats,
                                         String fuel,
                                         BigDecimal dailyRate,
                                         boolean available);
}
