package com.rental.service;

public class ReservationNotFoundException extends IllegalArgumentException {
    public ReservationNotFoundException(String id) {
        super("Unknown reservation: " + id);
    }
}
