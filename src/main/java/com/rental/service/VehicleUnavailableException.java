package com.rental.service;

public class VehicleUnavailableException extends IllegalStateException {
    public VehicleUnavailableException(String vehicleId) {
        super("Vehicle is not available: " + vehicleId);
    }
}
