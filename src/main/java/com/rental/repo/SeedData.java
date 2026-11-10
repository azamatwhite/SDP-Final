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
        repo.save(r.create(VehicleType.CAR, "Toyota", "Camry 70", 5, "Petrol", new BigDecimal("25000.00")));
        repo.save(r.create(VehicleType.CAR, "Hyundai", "Tucson", 5, "Hybrid", new BigDecimal("28000.00")));
        repo.save(r.create(VehicleType.CAR, "Kia", "K5", 5, "Petrol", new BigDecimal("24000.00")));
        repo.save(r.create(VehicleType.CAR, "Zeekr", "001 AWD", 5, "Electric", new BigDecimal("45000.00")));

        repo.save(r.create(VehicleType.MOTORCYCLE, "Yamaha", "MT-07", 2, "Petrol", new BigDecimal("18000.00")));
        repo.save(r.create(VehicleType.MOTORCYCLE, "Honda", "CB500F", 2, "Petrol", new BigDecimal("15000.00")));

        repo.save(r.create(VehicleType.SCOOTER, "Ninebot", "KickScooter Max", 1, "Electric", new BigDecimal("5000.00")));
        repo.save(r.create(VehicleType.SCOOTER, "Xiaomi", "Electric Scooter 4 Pro", 1, "Electric", new BigDecimal("4000.00")));
    }
}
