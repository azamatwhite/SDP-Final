package com.rental.repo;

import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcVehicleRepository implements VehicleRepository {
    private final Database db;

    public JdbcVehicleRepository(Database db) {
        this.db = db;
    }

    @Override
    public List<Vehicle> findAll() {
        try (Connection c = db.connection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM vehicles ORDER BY type, brand, model");
             ResultSet rs = ps.executeQuery()) {
            List<Vehicle> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection c = db.connection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM vehicles WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Vehicle save(Vehicle v) {
        if (v == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }
        String sql = "INSERT INTO vehicles (id,type,brand,model,seats,fuel,daily_rate,available) "
                + "VALUES (?,?,?,?,?,?,?,?) ON CONFLICT (id) DO UPDATE SET type=EXCLUDED.type, "
                + "brand=EXCLUDED.brand, model=EXCLUDED.model, seats=EXCLUDED.seats, fuel=EXCLUDED.fuel, "
                + "daily_rate=EXCLUDED.daily_rate, available=EXCLUDED.available";
        try (Connection c = db.connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, v.getId());
            ps.setString(2, v.getType().name());
            ps.setString(3, v.getBrand());
            ps.setString(4, v.getModel());
            ps.setInt(5, v.getSeats());
            ps.setString(6, v.getFuel());
            ps.setBigDecimal(7, v.getDailyRate());
            ps.setBoolean(8, v.isAvailable());
            ps.executeUpdate();
            return v;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private Vehicle map(ResultSet rs) throws SQLException {
        return Vehicle.builder()
                .id(rs.getString("id"))
                .type(VehicleType.valueOf(rs.getString("type")))
                .brand(rs.getString("brand"))
                .model(rs.getString("model"))
                .seats(rs.getInt("seats"))
                .fuel(rs.getString("fuel"))
                .dailyRate(rs.getBigDecimal("daily_rate"))
                .available(rs.getBoolean("available"))
                .build();
    }
}
