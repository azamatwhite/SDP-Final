package com.rental.repo;

import com.rental.domain.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * Data access interface for storing and retrieving Vehicle entities.
 */
public interface VehicleRepository {

    /**
     * Retrieves all vehicles currently in the repository.
     *
     * @return a list of all vehicles
     */
    List<Vehicle> findAll();

    /**
     * Finds a vehicle by its unique ID.
     *
     * @param id vehicle identifier
     * @return an Optional containing the vehicle if found, or empty Optional
     */
    Optional<Vehicle> findById(String id);

    /**
     * Persists or updates a vehicle in the repository.
     *
     * @param vehicle the vehicle to save
     * @return the saved vehicle
     */
    Vehicle save(Vehicle vehicle);
}
