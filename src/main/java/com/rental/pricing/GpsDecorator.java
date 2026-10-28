package com.rental.pricing;

import java.math.BigDecimal;
import java.util.List;

public class GpsDecorator extends QuoteDecorator {
    private static final BigDecimal GPS_PRICE = BigDecimal.valueOf(50.00);

    public GpsDecorator(Quote wrapped) {
        super(wrapped);
    }

    @Override
    public BigDecimal total() {
        return super.total().add(GPS_PRICE);
    }

    @Override
    public List<String> lines() {
        List<String> l = super.lines();
        l.add("GPS Navigation: " + GPS_PRICE);
        return l;
    }
}