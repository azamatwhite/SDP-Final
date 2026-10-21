package com.rental.repo;

import com.rental.domain.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository {

    List<Vehicle> findAll();

    Optional<Vehicle> findById(String id);

    Vehicle save(Vehicle vehicle);
}
