package com.rental.repo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final String url;
    private final String user;
    private final String password;

    public Database() {
        this(env("DB_URL", "jdbc:postgresql://localhost:5432/rental"),
                env("DB_USER", "rental"),
                env("DB_PASSWORD", "rental"));
    }

    public Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        init();
    }

    public Connection connection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return v == null || v.isBlank() ? def : v;
    }

    private void init() {
        try (Connection c = connection(); Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS vehicles (
                    id VARCHAR(36) PRIMARY KEY,
                    type VARCHAR(20) NOT NULL,
                    brand VARCHAR(100) NOT NULL,
                    model VARCHAR(100) NOT NULL,
                    seats INT NOT NULL,
                    fuel VARCHAR(50) NOT NULL,
                    daily_rate DECIMAL(10,2) NOT NULL,
                    available BOOLEAN NOT NULL
                )""");
            s.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id VARCHAR(36) PRIMARY KEY,
                    vehicle_id VARCHAR(36) NOT NULL REFERENCES vehicles(id),
                    customer_id VARCHAR(100) NOT NULL,
                    email VARCHAR(200) NOT NULL,
                    rental_days INT NOT NULL,
                    total_price DECIMAL(10,2) NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    start_date DATE,
                    end_date DATE,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )""");
            s.execute("ALTER TABLE reservations ADD COLUMN IF NOT EXISTS start_date DATE");
            s.execute("ALTER TABLE reservations ADD COLUMN IF NOT EXISTS end_date DATE");
        } catch (SQLException e) {
            throw new IllegalStateException("DB init failed (is Postgres running?)", e);
        }
    }
}
