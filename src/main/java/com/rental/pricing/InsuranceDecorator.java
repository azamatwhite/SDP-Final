package com.rental.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class InsuranceDecorator extends QuoteDecorator {
    public InsuranceDecorator(Quote wrapped) {
        super(wrapped);
    }

    @Override
    public BigDecimal total() {
        BigDecimal current = super.total();
        return current.add(current.multiply(new BigDecimal("0.10")))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<String> lines() {
        List<String> l = super.lines();
        l.add("Full Insurance (10%): added");
        return l;
    }
}
