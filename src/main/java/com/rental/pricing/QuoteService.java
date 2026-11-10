package com.rental.pricing;

import com.rental.domain.Vehicle;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class QuoteService {
    private final Map<String, PricingStrategy> strategies = new LinkedHashMap<>();

    public QuoteService(PricingStrategy... list) {
        for (PricingStrategy s : list) {
            strategies.put(s.code(), s);
        }
    }

    public Quote build(Vehicle vehicle, int days, String pricingCode, Collection<String> extras) {
        String effectiveCode = resolveStrategyCode(days, pricingCode);
        PricingStrategy strategy = strategies.get(effectiveCode);
        if (strategy == null) {
            strategy = strategies.get("DAILY");
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown pricing strategy: " + effectiveCode);
        }

        Quote quote = new BaseQuote(strategy.basePrice(vehicle, days),
                strategy.code() + " x " + days + " day(s)");
        for (String extra : extras) {
            String ex = extra.trim().toUpperCase();
            if (ex.equals("CHILD_SEAT") && vehicle.getType() != com.rental.domain.VehicleType.CAR) {
                throw new IllegalArgumentException("Child seat is only available for cars");
            }
            if (ex.equals("HELMET") && vehicle.getType() == com.rental.domain.VehicleType.CAR) {
                throw new IllegalArgumentException("Helmet is only available for motorcycles and scooters");
            }
            quote = switch (ex) {
                case "" -> quote;
                case "HELMET" -> new HelmetDecorator(quote);
                case "CHILD_SEAT" -> new ChildSeatDecorator(quote);
                case "INSURANCE" -> new InsuranceDecorator(quote);
                default -> throw new IllegalArgumentException("Unknown extra: " + extra);
            };
        }
        return quote;
    }

    public static String resolveStrategyCode(int days, String promoOrCode) {
        boolean isMonthly = (days >= 30);
        boolean isWeekly = (days >= 7);

        if (promoOrCode != null && !promoOrCode.isBlank()) {
            String p = promoOrCode.trim().toUpperCase();
            if (p.equals("ASTANA-IT") || p.equals("AITU") || p.equals("STUDENT_AITU")) {
                return isMonthly ? "MONTHLY" : "STUDENT_AITU";
            }
            if (p.equals("CORP10") || p.equals("CORPORATE")) {
                if (isMonthly) return "MONTHLY";
                if (isWeekly) return "WEEKLY_DISCOUNT";
                return "CORPORATE";
            }
            if (p.equals("MONTHLY")) {
                return "MONTHLY";
            }
            if (p.equals("WEEKLY_DISCOUNT") || p.equals("WEEKLY")) {
                return "WEEKLY_DISCOUNT";
            }
            if (p.equals("DAILY")) {
                return "DAILY";
            }
        }
        if (isMonthly) {
            return "MONTHLY";
        }
        if (isWeekly) {
            return "WEEKLY_DISCOUNT";
        }
        return "DAILY";
    }
}
