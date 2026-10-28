package com.rental.pricing;

import java.math.BigDecimal;
import java.util.List;

public interface Quote {
    BigDecimal total();
    List<String> lines();
}