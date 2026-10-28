package com.rental.pricing;

import com.rental.domain.Vehicle;
import java.math.BigDecimal;

public interface PricingStrategy {
    String code();
    BigDecimal basePrice(Vehicle vehicle, int days);
}