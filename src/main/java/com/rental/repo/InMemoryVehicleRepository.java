package com.rental.repo;

import com.rental.creation.VehicleFactoryRegistry;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;
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

        save(registry.create(VehicleType.CAR, "Toyota", "Camry", 5, "Petrol", new BigDecimal("50.00")));
        save(registry.create(VehicleType.CAR, "Tesla", "Model 3", 5, "Electric", new BigDecimal("85.00")));
        save(registry.create(VehicleType.CAR, "Hyundai", "Tucson", 5, "Hybrid", new BigDecimal("65.00")));
        save(registry.create(VehicleType.CAR, "BMW", "3 Series", 5, "Petrol", new BigDecimal("95.00")));

        save(registry.create(VehicleType.MOTORCYCLE, "Yamaha", "MT-07", 2, "Petrol", new BigDecimal("40.00")));
        save(registry.create(VehicleType.MOTORCYCLE, "Honda", "CB500F", 2, "Petrol", new BigDecimal("35.00")));

        save(registry.create(VehicleType.SCOOTER, "Vespa", "Primavera", 2, "Petrol", new BigDecimal("25.00")));
        save(registry.create(VehicleType.SCOOTER, "Xiaomi", "Electric Scooter 4 Pro", 1, "Electric", new BigDecimal("15.00")));
    }
}
