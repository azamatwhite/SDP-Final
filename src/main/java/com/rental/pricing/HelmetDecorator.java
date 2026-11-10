package com.rental.pricing;

import java.math.BigDecimal;
import java.util.List;

public class HelmetDecorator extends QuoteDecorator {
    private static final BigDecimal HELMET_PRICE = new BigDecimal("1000.00");

    public HelmetDecorator(Quote wrapped) {
        super(wrapped);
    }

    @Override
    public BigDecimal total() {
        return super.total().add(HELMET_PRICE);
    }

    @Override
    public List<String> lines() {
        List<String> l = super.lines();
        l.add("Protective Helmet: " + HELMET_PRICE);
        return l;
    }
}
