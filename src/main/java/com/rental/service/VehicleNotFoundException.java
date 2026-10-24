package com.rental.service;

public class VehicleNotFoundException extends IllegalArgumentException {
    public VehicleNotFoundException(String vehicleId) {
        super("Unknown vehicle: " + vehicleId);
    }
}
