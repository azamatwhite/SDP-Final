package com.rental.pricing;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MonthlySubscriptionPricing implements PricingStrategy {
    @Override
    public String code() {
        return "MONTHLY";
    }

    @Override
    public BigDecimal basePrice(Vehicle vehicle, int days) {
        BigDecimal total = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
        if (days >= 30) {
            // 30% discount for long-term monthly subscription
            total = total.multiply(new BigDecimal("0.70"));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
