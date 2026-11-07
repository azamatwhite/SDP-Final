package com.rental.repo;

import com.rental.creation.VehicleFactoryRegistry;
import com.rental.domain.VehicleType;

import java.math.BigDecimal;

public final class SeedData {
    private SeedData() {
    }

    public static void load(VehicleRepository repo, VehicleFactoryRegistry r) {
        if (!repo.findAll().isEmpty()) {
            return;
        }
        repo.save(r.create(VehicleType.CAR, "Toyota", "Camry", 5, "Petrol", new BigDecimal("50.00")));
        repo.save(r.create(VehicleType.CAR, "Tesla", "Model 3", 5, "Electric", new BigDecimal("85.00")));
        repo.save(r.create(VehicleType.CAR, "Hyundai", "Tucson", 5, "Hybrid", new BigDecimal("65.00")));
        repo.save(r.create(VehicleType.CAR, "BMW", "3 Series", 5, "Petrol", new BigDecimal("95.00")));

        repo.save(r.create(VehicleType.MOTORCYCLE, "Yamaha", "MT-07", 2, "Petrol", new BigDecimal("40.00")));
        repo.save(r.create(VehicleType.MOTORCYCLE, "Honda", "CB500F", 2, "Petrol", new BigDecimal("35.00")));

        repo.save(r.create(VehicleType.SCOOTER, "Vespa", "Primavera", 2, "Petrol", new BigDecimal("25.00")));
        repo.save(r.create(VehicleType.SCOOTER, "Xiaomi", "Electric Scooter 4 Pro", 1, "Electric", new BigDecimal("15.00")));
    }
}
