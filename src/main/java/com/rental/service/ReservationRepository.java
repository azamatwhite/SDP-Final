package com.rental.service;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(String id);
}
