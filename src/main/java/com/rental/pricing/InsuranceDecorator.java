package com.rental.pricing;

import java.math.BigDecimal;
import java.util.List;

public class InsuranceDecorator extends QuoteDecorator {
    public InsuranceDecorator(Quote wrapped) {
        super(wrapped);
    }

    @Override
    public BigDecimal total() {
        BigDecimal currentTotal = super.total();
        BigDecimal insuranceCost = currentTotal.multiply(BigDecimal.valueOf(0.10));
        return currentTotal.add(insuranceCost);
    }

    @Override
    public List<String> lines() {
        List<String> l = super.lines();
        l.add("Full Insurance (10%): added");
        return l;
    }
}