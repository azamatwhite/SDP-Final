package com.rental.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Object Adapter: converts the application's payment request to the legacy processor API.
public class LegacyPaymentAdapter implements PaymentGateway {
    private final LegacyPaymentProcessor legacyProcessor;

    public LegacyPaymentAdapter(LegacyPaymentProcessor legacyProcessor) {
        if (legacyProcessor == null) {
            throw new IllegalArgumentException("LegacyPaymentProcessor must not be null");
        }
        this.legacyProcessor = legacyProcessor;
    }

    @Override
    public boolean charge(String customerId, BigDecimal amount) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID must not be blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }
        long cents = amount.setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
        return legacyProcessor.makePayment(customerId, cents, "USD");
    }
}
