package com.rental.pricing;

import com.rental.domain.Vehicle;
import java.math.BigDecimal;

public class DailyPricing implements PricingStrategy {
    @Override
    public String code() {
        return "DAILY";
    }

    @Override
    public BigDecimal basePrice(Vehicle vehicle, int days) {
        return vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
    }
}