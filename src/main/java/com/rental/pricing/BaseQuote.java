package com.rental.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BaseQuote implements Quote {
    private final BigDecimal baseAmount;
    private final String description;

    public BaseQuote(BigDecimal baseAmount, String description) {
        this.baseAmount = baseAmount;
        this.description = description;
    }

    @Override
    public BigDecimal total() {
        return baseAmount;
    }

    @Override
    public List<String> lines() {
        List<String> list = new ArrayList<>();
        list.add(description + ": " + baseAmount);
        return list;
    }
}