package com.rental.pricing;

import java.math.BigDecimal;
import java.util.List;

public class ChildSeatDecorator extends QuoteDecorator {
    private static final BigDecimal SEAT_PRICE = BigDecimal.valueOf(30.00);

    public ChildSeatDecorator(Quote wrapped) {
        super(wrapped);
    }

    @Override
    public BigDecimal total() {
        return super.total().add(SEAT_PRICE);
    }

    @Override
    public List<String> lines() {
        List<String> l = super.lines();
        l.add("Child Seat: " + SEAT_PRICE);
        return l;
    }
}