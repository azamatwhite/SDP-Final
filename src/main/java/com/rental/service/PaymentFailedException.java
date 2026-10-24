package com.rental.service;

public class PaymentFailedException extends IllegalStateException {
    public PaymentFailedException() {
        super("Payment was declined");
    }
}
