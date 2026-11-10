
package com.rental.repo;




import com.rental.service.Reservation;
import com.rental.service.ReservationRepository;
import com.rental.service.ReservationStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcReservationRepository implements ReservationRepository {
    private final Database db;

    public JdbcReservationRepository(Database db) {
        this.db = db;
    }

    @Override
    public Reservation save(Reservation r) {
        if (r == null) {
            throw new IllegalArgumentException("Reservation must not be null");
        }
        String sql = "INSERT INTO reservations (id,vehicle_id,customer_id,email,rental_days,total_price,status,start_date,end_date) "
                + "VALUES (?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO UPDATE SET status=EXCLUDED.status";
        try (Connection c = db.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getId());
            ps.setString(2, r.getVehicleId());
            ps.setString(3, r.getCustomerId());
            ps.setString(4, r.getEmail());
            ps.setInt(5, r.getRentalDays());
            ps.setBigDecimal(6, r.getTotalPrice());
            ps.setString(7, r.getStatus().name());
            ps.setDate(8, r.getStartDate() != null ? Date.valueOf(r.getStartDate()) : Date.valueOf(LocalDate.now()));
            ps.setDate(9, r.getEndDate() != null ? Date.valueOf(r.getEndDate()) : Date.valueOf(LocalDate.now().plusDays(r.getRentalDays())));
            ps.executeUpdate();
            return r;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        try (Connection c = db.connection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM reservations ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            List<Reservation> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<Reservation> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection c = db.connection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM reservations WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Date start = null;
        Date end = null;
        try {
            start = rs.getDate("start_date");
            end = rs.getDate("end_date");
        } catch (SQLException ignored) {
        }
        LocalDate startDate = start != null ? start.toLocalDate() : null;
        LocalDate endDate = end != null ? end.toLocalDate() : null;
        return new Reservation(
                rs.getString("id"),
                rs.getString("vehicle_id"),
                rs.getString("customer_id"),
                rs.getString("email"),
                rs.getInt("rental_days"),
                rs.getBigDecimal("total_price"),
                ReservationStatus.valueOf(rs.getString("status")),
                startDate,
                endDate);
    }
}
