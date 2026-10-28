package com.rental.pricing;

import java.math.BigDecimal;
import java.util.List;

public abstract class QuoteDecorator implements Quote {
    protected final Quote wrapped;

    protected QuoteDecorator(Quote wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal total() {
        return wrapped.total();
    }

    @Override
    public List<String> lines() {
        return wrapped.lines();
    }
}