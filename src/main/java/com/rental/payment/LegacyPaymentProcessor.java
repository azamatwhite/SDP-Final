package com.rental.payment;

public class LegacyPaymentProcessor {
    public boolean makePayment(String cardToken, long cents, String currency) {
        if ("declined".equals(cardToken)) {
            System.out.println("Simulated payment declined for " + cents + " " + currency);
            return false;
        }
        System.out.println("Simulated payment accepted for " + cents + " " + currency
                + " using token " + cardToken);
        return true;
    }
}
