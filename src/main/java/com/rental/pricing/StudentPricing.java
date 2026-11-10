package com.rental.pricing;

import com.rental.domain.Vehicle;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StudentPricing implements PricingStrategy {
    @Override
    public String code() {
        return "STUDENT_AITU";
    }

    @Override
    public BigDecimal basePrice(Vehicle vehicle, int days) {
        BigDecimal total = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));
        // 20% discount for Astana IT University (AITU) students
        return total.multiply(new BigDecimal("0.80")).setScale(2, RoundingMode.HALF_UP);
    }
}
