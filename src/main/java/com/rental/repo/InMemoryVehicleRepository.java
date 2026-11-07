package com.rental.repo;

import com.rental.creation.VehicleFactoryRegistry;
import com.rental.domain.Vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryVehicleRepository implements VehicleRepository {

    private final ConcurrentMap<String, Vehicle> storage = new ConcurrentHashMap<>();

    @Override
    public List<Vehicle> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        if (vehicle.getId() == null || vehicle.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle must have a valid non-empty id");
        }
        storage.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    public void initSeedData() {
        initSeedData(new VehicleFactoryRegistry());
    }

    public void initSeedData(VehicleFactoryRegistry registry) {
        if (registry == null) {
            throw new IllegalArgumentException("VehicleFactoryRegistry must not be null");
        }
        SeedData.load(this, registry);
    }
}
