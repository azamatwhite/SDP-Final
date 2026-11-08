package com.rental.pricing;

import com.rental.domain.Vehicle;
import java.math.BigDecimal;

/**
 * Strategy Pattern Interface: Defines a family of pricing algorithms,
 * encapsulating each one and making them interchangeable at runtime.
 */
public interface PricingStrategy {

    /**
     * Unique code identifier for the pricing strategy.
     */
    String code();

    /**
     * Calculates the base rental price for a given vehicle and duration.
     */
    BigDecimal basePrice(Vehicle vehicle, int days);
}