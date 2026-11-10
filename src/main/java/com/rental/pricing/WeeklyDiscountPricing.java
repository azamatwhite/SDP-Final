package com.rental.pricing;

import com.rental.domain.Vehicle;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class WeeklyDiscountPricing implements PricingStrategy {
    @Override
    public String code() {
        return "WEEKLY_DISCOUNT";
    }

    @Override
    public BigDecimal basePrice(Vehicle vehicle, int days) {
        BigDecimal total = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
        if (days >= 7) {
            total = total.multiply(new BigDecimal("0.85"));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
