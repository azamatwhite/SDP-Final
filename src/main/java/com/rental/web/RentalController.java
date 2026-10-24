package com.rental.web;

import com.rental.creation.VehicleFactoryRegistry;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;
import com.rental.repo.VehicleRepository;
import com.rental.service.PaymentFailedException;
import com.rental.service.RentalFacade;
import com.rental.service.Reservation;
import com.rental.service.VehicleNotFoundException;
import com.rental.service.VehicleUnavailableException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RentalController implements HttpHandler {
    private final RentalFacade rentalFacade;
    private final VehicleFactoryRegistry vehicleFactoryRegistry;
    private final VehicleRepository vehicleRepository;

    public RentalController(RentalFacade rentalFacade, VehicleFactoryRegistry vehicleFactoryRegistry,
                            VehicleRepository vehicleRepository) {
        this.rentalFacade = rentalFacade;
        this.vehicleFactoryRegistry = vehicleFactoryRegistry;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("GET".equals(method) && "/api/vehicles".equals(path)) {
                send(exchange, 200, "application/json", vehiclesJson(rentalFacade.getAvailableVehicles()));
            } else if ("POST".equals(method) && "/api/quote".equals(path)) {
                Map<String, String> form = readForm(exchange);
                BigDecimal total = rentalFacade.quote(required(form, "vehicleId"),
                        Integer.parseInt(required(form, "rentalDays")));
                send(exchange, 200, "application/json", "{\"totalPrice\":\"" + total.toPlainString() + "\"}");
            } else if ("POST".equals(method) && "/api/checkout".equals(path)) {
                Map<String, String> form = readForm(exchange);
                Reservation reservation = rentalFacade.checkout(required(form, "vehicleId"),
                        Integer.parseInt(required(form, "rentalDays")),
                        required(form, "customerId"), required(form, "email"));
                send(exchange, 201, "application/json", reservationJson(reservation));
            } else if ("POST".equals(method) && "/api/vehicles".equals(path)) {
                Vehicle vehicle = vehicleRepository.save(createVehicle(readForm(exchange)));
                send(exchange, 201, "application/json", vehicleJson(vehicle));
            } else {
                send(exchange, 404, "application/json", "{\"error\":\"Route not found\"}");
            }
        } catch (PaymentFailedException exception) {
            send(exchange, 402, "application/json", errorJson(exception.getMessage()));
        } catch (VehicleUnavailableException exception) {
            send(exchange, 409, "application/json", errorJson(exception.getMessage()));
        } catch (VehicleNotFoundException exception) {
            send(exchange, 404, "application/json", errorJson(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            send(exchange, 400, "application/json", errorJson(exception.getMessage()));
        }
    }

    private Vehicle createVehicle(Map<String, String> form) {
        VehicleType type = VehicleType.valueOf(required(form, "type"));
        String brand = required(form, "brand");
        String model = required(form, "model");
        int seats = Integer.parseInt(required(form, "seats"));
        String fuel = required(form, "fuel");
        BigDecimal dailyRate = new BigDecimal(required(form, "dailyRate"));
        return vehicleFactoryRegistry.create(type, brand, model, seats, fuel, dailyRate);
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new HashMap<>();
        if (body.isEmpty()) {
            return values;
        }
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private String required(Map<String, String> form, String name) {
        String value = form.get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private String vehiclesJson(Iterable<Vehicle> vehicles) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (Vehicle vehicle : vehicles) {
            if (!first) {
                json.append(',');
            }
            json.append(vehicleJson(vehicle));
            first = false;
        }
        return json.append(']').toString();
    }

    private String vehicleJson(Vehicle vehicle) {
        return "{\"id\":\"" + escape(vehicle.getId()) + "\",\"type\":\"" + vehicle.getType()
                + "\",\"brand\":\"" + escape(vehicle.getBrand()) + "\",\"model\":\""
                + escape(vehicle.getModel()) + "\",\"seats\":" + vehicle.getSeats()
                + ",\"fuel\":\"" + escape(vehicle.getFuel()) + "\",\"dailyRate\":\""
                + vehicle.getDailyRate().toPlainString() + "\",\"available\":" + vehicle.isAvailable() + "}";
    }

    private String reservationJson(Reservation reservation) {
        return "{\"id\":\"" + escape(reservation.getId()) + "\",\"vehicleId\":\""
                + escape(reservation.getVehicleId()) + "\",\"rentalDays\":" + reservation.getRentalDays()
                + ",\"totalPrice\":\"" + reservation.getTotalPrice().toPlainString() + "\"}";
    }

    private String errorJson(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
