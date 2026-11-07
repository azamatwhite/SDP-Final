package com.rental.web;

import com.google.gson.Gson;
import com.rental.creation.VehicleFactoryRegistry;
import com.rental.domain.Vehicle;
import com.rental.domain.VehicleType;
import com.rental.pricing.Quote;
import com.rental.repo.VehicleRepository;
import com.rental.service.PaymentFailedException;
import com.rental.service.RentalFacade;
import com.rental.service.Reservation;
import com.rental.service.ReservationNotFoundException;
import com.rental.service.VehicleNotFoundException;
import com.rental.service.VehicleUnavailableException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RentalController implements HttpHandler {
    private final Gson gson = new Gson();
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
            String[] seg = path.split("/"); // ["", "api", "reservations", "{id}", "cancel"]

            if ("GET".equals(method) && "/api/vehicles".equals(path)) {
                send(exchange, 200, gson.toJson(rentalFacade.getAllVehicles()));
            } else if ("POST".equals(method) && "/api/vehicles".equals(path)) {
                Vehicle vehicle = vehicleRepository.save(createVehicle(readForm(exchange)));
                send(exchange, 201, gson.toJson(vehicle));
            } else if ("POST".equals(method) && "/api/quote".equals(path)) {
                Map<String, String> form = readForm(exchange);
                Quote quote = rentalFacade.buildQuote(required(form, "vehicleId"),
                        Integer.parseInt(required(form, "rentalDays")),
                        form.get("pricing"), extras(form));
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("totalPrice", RentalFacade.price(quote));
                body.put("lines", quote.lines());
                send(exchange, 200, gson.toJson(body));
            } else if ("POST".equals(method) && "/api/checkout".equals(path)) {
                Map<String, String> form = readForm(exchange);
                Reservation r = rentalFacade.checkout(required(form, "vehicleId"),
                        Integer.parseInt(required(form, "rentalDays")),
                        required(form, "customerId"), required(form, "email"),
                        form.get("pricing"), extras(form));
                send(exchange, 201, gson.toJson(view(r)));
            } else if ("GET".equals(method) && "/api/reservations".equals(path)) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (Reservation r : rentalFacade.getReservations()) {
                    list.add(view(r));
                }
                send(exchange, 200, gson.toJson(list));
            } else if ("POST".equals(method) && seg.length == 5 && "reservations".equals(seg[2])
                    && "cancel".equals(seg[4])) {
                send(exchange, 200, gson.toJson(view(rentalFacade.cancel(seg[3]))));
            } else if ("POST".equals(method) && seg.length == 5 && "reservations".equals(seg[2])
                    && "complete".equals(seg[4])) {
                send(exchange, 200, gson.toJson(view(rentalFacade.complete(seg[3]))));
            } else {
                send(exchange, 404, errorJson("Route not found"));
            }
        } catch (PaymentFailedException e) {
            send(exchange, 402, errorJson(e.getMessage()));
        } catch (VehicleUnavailableException e) {
            send(exchange, 409, errorJson(e.getMessage()));
        } catch (VehicleNotFoundException | ReservationNotFoundException e) {
            send(exchange, 404, errorJson(e.getMessage()));
        } catch (IllegalArgumentException e) {
            send(exchange, 400, errorJson(e.getMessage()));
        } catch (IllegalStateException e) {
            send(exchange, 409, errorJson(e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            send(exchange, 500, errorJson("Internal server error"));
        }
    }

    // The payment token (customerId) is intentionally not exposed.
    private Map<String, Object> view(Reservation r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("vehicleId", r.getVehicleId());
        m.put("email", r.getEmail());
        m.put("rentalDays", r.getRentalDays());
        m.put("totalPrice", r.getTotalPrice());
        m.put("status", r.getStatus().name());
        return m;
    }

    private List<String> extras(Map<String, String> form) {
        String raw = form.getOrDefault("extras", "");
        return raw.isBlank() ? List.of() : Arrays.asList(raw.split(","));
    }

    private Vehicle createVehicle(Map<String, String> form) {
        VehicleType type = VehicleType.valueOf(required(form, "type"));
        return vehicleFactoryRegistry.create(type,
                required(form, "brand"),
                required(form, "model"),
                Integer.parseInt(required(form, "seats")),
                required(form, "fuel"),
                new BigDecimal(required(form, "dailyRate")));
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new HashMap<>();
        if (body.isEmpty()) {
            return values;
        }
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            values.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                    parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
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

    private String errorJson(String message) {
        return gson.toJson(Map.of("error", message == null ? "Error" : message));
    }

    private void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
