package com.rental.pricing;

import com.rental.domain.Vehicle;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// Connects Strategy (base price) with Decorator (extras).
public class QuoteService {
    private final Map<String, PricingStrategy> strategies = new LinkedHashMap<>();

    public QuoteService(PricingStrategy... list) {
        for (PricingStrategy s : list) {
            strategies.put(s.code(), s);
        }
    }

    public Quote build(Vehicle vehicle, int days, String pricingCode, Collection<String> extras) {
        String code = (pricingCode == null || pricingCode.isBlank()) ? "DAILY" : pricingCode;
        PricingStrategy strategy = strategies.get(code);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown pricing: " + code);
        }

        Quote quote = new BaseQuote(strategy.basePrice(vehicle, days),
                strategy.code() + " x " + days + " day(s)");
        for (String extra : extras) {
            quote = switch (extra.trim().toUpperCase()) {
                case "" -> quote;
                case "GPS" -> new GpsDecorator(quote);
                case "CHILD_SEAT" -> new ChildSeatDecorator(quote);
                case "INSURANCE" -> new InsuranceDecorator(quote);
                default -> throw new IllegalArgumentException("Unknown extra: " + extra);
            };
        }
        return quote;
    }
}
