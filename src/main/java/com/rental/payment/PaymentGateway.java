package com.rental.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    boolean charge(String customerId, BigDecimal amount);
}
