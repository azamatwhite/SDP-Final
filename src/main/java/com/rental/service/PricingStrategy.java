package com.rental.service;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Vehicle vehicle, int rentalDays);
}
