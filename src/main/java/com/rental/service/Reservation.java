package com.rental.service;

import java.math.BigDecimal;

public class Reservation {
    private final String id;
    private final String vehicleId;
    private final String customerId;
    private final String email;
    private final int rentalDays;
    private final BigDecimal totalPrice;

    public Reservation(String id, String vehicleId, String customerId, String email,
                       int rentalDays, BigDecimal totalPrice) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.email = email;
        this.rentalDays = rentalDays;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getEmail() {
        return email;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
}
