package com.rental.service;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;

public class DailyRatePricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculatePrice(Vehicle vehicle, int rentalDays) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        if (rentalDays <= 0) {
            throw new IllegalArgumentException("Rental duration must be greater than 0 days");
        }
        return vehicle.getDailyRate().multiply(BigDecimal.valueOf(rentalDays));
    }
}
