package com.rental.creation;

import com.rental.domain.Car;
import com.rental.domain.Motorcycle;
import com.rental.domain.Scooter;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class VehicleFactoryTest {

    private VehicleFactoryRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new VehicleFactoryRegistry();
    }

    @Test
    void carFactoryShouldCreateCar() {
        VehicleFactory factory = new CarFactory();
        Vehicle vehicle = factory.createVehicle("Toyota", "Corolla", 5, "Petrol", new BigDecimal("45.00"));

        assertNotNull(vehicle);
        assertInstanceOf(Car.class, vehicle);
        assertEquals(VehicleType.CAR, vehicle.getType());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Corolla", vehicle.getModel());
    }

    @Test
    void motorcycleFactoryShouldCreateMotorcycle() {
        VehicleFactory factory = new MotorcycleFactory();
        Vehicle vehicle = factory.createVehicle("Kawasaki", "Ninja", 2, "Petrol", new BigDecimal("55.00"));

        assertNotNull(vehicle);
        assertInstanceOf(Motorcycle.class, vehicle);
        assertEquals(VehicleType.MOTORCYCLE, vehicle.getType());
        assertEquals("Kawasaki", vehicle.getBrand());
    }

    @Test
    void scooterFactoryShouldCreateScooter() {
        VehicleFactory factory = new ScooterFactory();
        Vehicle vehicle = factory.createVehicle("Segway", "Ninebot", 1, "Electric", new BigDecimal("20.00"));

        assertNotNull(vehicle);
        assertInstanceOf(Scooter.class, vehicle);
        assertEquals(VehicleType.SCOOTER, vehicle.getType());
        assertEquals("Segway", vehicle.getBrand());
    }

    @Test
    void registryShouldCreateVehiclesByType() {
        Vehicle car = registry.create(VehicleType.CAR, "Ford", "Focus", 5, "Diesel", new BigDecimal("40.00"));
        assertInstanceOf(Car.class, car);

        Vehicle moto = registry.create(VehicleType.MOTORCYCLE, "Ducati", "Monster", 2, "Petrol", new BigDecimal("80.00"));
        assertInstanceOf(Motorcycle.class, moto);

        Vehicle scooter = registry.create(VehicleType.SCOOTER, "Xiaomi", "Pro", 1, "Electric", new BigDecimal("15.00"));
        assertInstanceOf(Scooter.class, scooter);
    }

    @Test
    void registryShouldThrowOnNullType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                registry.create(null, "Ford", "Focus", 5, "Petrol", new BigDecimal("40.00"))
        );
        assertTrue(ex.getMessage().contains("Vehicle type must not be null"));
    }

    @Test
    void registryShouldThrowOnRegisterNullFactory() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                registry.registerFactory(VehicleType.CAR, null)
        );
        assertTrue(ex.getMessage().contains("Vehicle factory must not be null"));
    }

    @Test
    void registryShouldThrowOnUnknownType() {
        VehicleFactoryRegistry customRegistry = new VehicleFactoryRegistry();
        assertThrows(IllegalArgumentException.class, () ->
                customRegistry.getFactory(null)
        );
    }
}
