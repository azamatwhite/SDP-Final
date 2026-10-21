package com.rental.creation;

import com.rental.domain.Car;
import com.rental.domain.Motorcycle;
import com.rental.domain.Scooter;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VehicleBuilder Tests")
class VehicleBuilderTest {

    @Test
    @DisplayName("Should successfully build a Car with valid fields")
    void shouldBuildCarSuccessfully() {
        Vehicle vehicle = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand("Toyota")
                .model("Camry")
                .seats(5)
                .fuel("Petrol")
                .dailyRate(new BigDecimal("50.00"))
                .build();

        assertNotNull(vehicle);
        assertInstanceOf(Car.class, vehicle);
        assertEquals(VehicleType.CAR, vehicle.getType());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Camry", vehicle.getModel());
        assertEquals(5, vehicle.getSeats());
        assertEquals("Petrol", vehicle.getFuel());
        assertEquals(new BigDecimal("50.00"), vehicle.getDailyRate());
        assertTrue(vehicle.isAvailable());
        assertNotNull(vehicle.getId());
        assertFalse(vehicle.getId().trim().isEmpty());
    }

    @Test
    @DisplayName("Should successfully build a Motorcycle")
    void shouldBuildMotorcycleSuccessfully() {
        Vehicle vehicle = Vehicle.builder()
                .type(VehicleType.MOTORCYCLE)
                .brand("Yamaha")
                .model("MT-07")
                .seats(2)
                .fuel("Petrol")
                .dailyRate(new BigDecimal("40.00"))
                .build();

        assertInstanceOf(Motorcycle.class, vehicle);
        assertEquals(VehicleType.MOTORCYCLE, vehicle.getType());
    }

    @Test
    @DisplayName("Should successfully build a Scooter")
    void shouldBuildScooterSuccessfully() {
        Vehicle vehicle = Vehicle.builder()
                .type(VehicleType.SCOOTER)
                .brand("Xiaomi")
                .model("Pro 4")
                .seats(1)
                .fuel("Electric")
                .dailyRate(new BigDecimal("15.00"))
                .build();

        assertInstanceOf(Scooter.class, vehicle);
        assertEquals(VehicleType.SCOOTER, vehicle.getType());
    }

    @Test
    @DisplayName("Should retain custom id and availability if specified")
    void shouldRetainCustomIdAndAvailability() {
        Vehicle vehicle = Vehicle.builder()
                .id("custom-uuid-123")
                .type(VehicleType.CAR)
                .brand("Audi")
                .model("A6")
                .seats(5)
                .fuel("Diesel")
                .dailyRate(new BigDecimal("90.00"))
                .available(false)
                .build();

        assertEquals("custom-uuid-123", vehicle.getId());
        assertFalse(vehicle.isAvailable());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when vehicle type is null")
    void shouldThrowWhenTypeIsNull() {
        VehicleBuilder builder = Vehicle.builder()
                .brand("Tesla")
                .model("Model S")
                .seats(5)
                .dailyRate(new BigDecimal("100.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Vehicle type must not be null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should throw IllegalArgumentException when brand is blank or empty")
    void shouldThrowWhenBrandIsBlank(String invalidBrand) {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand(invalidBrand)
                .model("Camry")
                .seats(5)
                .dailyRate(new BigDecimal("50.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Brand must not be blank"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when brand is null")
    void shouldThrowWhenBrandIsNull() {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand(null)
                .model("Camry")
                .seats(5)
                .dailyRate(new BigDecimal("50.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Brand must not be blank"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should throw IllegalArgumentException when model is blank or empty")
    void shouldThrowWhenModelIsBlank(String invalidModel) {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand("Toyota")
                .model(invalidModel)
                .seats(5)
                .dailyRate(new BigDecimal("50.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Model must not be blank"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    @DisplayName("Should throw IllegalArgumentException when seats count is zero or negative")
    void shouldThrowWhenSeatsIsZeroOrNegative(int invalidSeats) {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand("Toyota")
                .model("Camry")
                .seats(invalidSeats)
                .dailyRate(new BigDecimal("50.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Seats must be greater than 0"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when dailyRate is null")
    void shouldThrowWhenDailyRateIsNull() {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand("Toyota")
                .model("Camry")
                .seats(5)
                .dailyRate(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Daily rate must be greater than 0"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when dailyRate is zero")
    void shouldThrowWhenDailyRateIsZero() {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand("Toyota")
                .model("Camry")
                .seats(5)
                .dailyRate(BigDecimal.ZERO);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Daily rate must be greater than 0"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when dailyRate is negative")
    void shouldThrowWhenDailyRateIsNegative() {
        VehicleBuilder builder = Vehicle.builder()
                .type(VehicleType.CAR)
                .brand("Toyota")
                .model("Camry")
                .seats(5)
                .dailyRate(new BigDecimal("-10.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, builder::build);
        assertTrue(ex.getMessage().contains("Daily rate must be greater than 0"));
    }
}
