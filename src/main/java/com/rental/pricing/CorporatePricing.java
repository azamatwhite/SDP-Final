package com.rental.pricing;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CorporatePricing implements PricingStrategy {
    @Override
    public String code() {
        return "CORPORATE";
    }

    @Override
    public BigDecimal basePrice(Vehicle vehicle, int days) {
        BigDecimal total = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
        return total.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
    }
}
