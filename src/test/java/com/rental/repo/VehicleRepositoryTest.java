package com.rental.repo;

import com.rental.creation.VehicleFactoryRegistry;
import com.rental.domain.Car;
import com.rental.domain.Motorcycle;
import com.rental.domain.Scooter;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VehicleRepositoryTest {

    private InMemoryVehicleRepository repository;
    private VehicleFactoryRegistry registry;

    @BeforeEach
    void setUp() {
        repository = new InMemoryVehicleRepository();
        registry = new VehicleFactoryRegistry();
    }

    @Test
    void shouldSaveAndFindById() {
        Vehicle car = registry.create(VehicleType.CAR, "Honda", "Civic", 5, "Petrol", new BigDecimal("45.00"));

        Vehicle saved = repository.save(car);
        assertEquals(car.getId(), saved.getId());

        Optional<Vehicle> found = repository.findById(car.getId());
        assertTrue(found.isPresent());
        assertEquals(car.getId(), found.get().getId());
        assertEquals("Honda", found.get().getBrand());
        assertEquals("Civic", found.get().getModel());
    }

    @Test
    void shouldReturnEmptyForNonExistentId() {
        Optional<Vehicle> found = repository.findById("non-existent-id");
        assertFalse(found.isPresent());

        Optional<Vehicle> foundNull = repository.findById(null);
        assertFalse(foundNull.isPresent());
    }

    @Test
    void shouldInitializeSeedDataWithEightVehicles() {
        repository.initSeedData();

        List<Vehicle> allVehicles = repository.findAll();
        assertEquals(8, allVehicles.size());

        long carsCount = allVehicles.stream().filter(v -> v instanceof Car).count();
        long motosCount = allVehicles.stream().filter(v -> v instanceof Motorcycle).count();
        long scootersCount = allVehicles.stream().filter(v -> v instanceof Scooter).count();

        assertEquals(4, carsCount);
        assertEquals(2, motosCount);
        assertEquals(2, scootersCount);

        allVehicles.forEach(vehicle -> {
            assertNotNull(vehicle.getId());
            assertNotNull(vehicle.getBrand());
            assertNotNull(vehicle.getModel());
            assertTrue(vehicle.getSeats() > 0);
            assertTrue(vehicle.getDailyRate().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(vehicle.isAvailable());
        });
    }

    @Test
    void shouldThrowWhenSavingNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                repository.save(null)
        );
        assertTrue(ex.getMessage().contains("Vehicle must not be null"));
    }
}
