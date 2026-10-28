package com.rental.pricing;

import com.rental.domain.Vehicle;
import java.math.BigDecimal;

public class WeeklyDiscountPricing implements PricingStrategy {
    @Override
    public String code() {
        return "WEEKLY_DISCOUNT";
    }

    @Override
    public BigDecimal basePrice(Vehicle vehicle, int days) {
        BigDecimal total = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
        if (days >= 7) {
            // Скидка 15% при аренде от 7 дней
            total = total.multiply(BigDecimal.valueOf(0.85));
        }
        return total;
    }
}