package com.rental;

import com.rental.creation.VehicleFactoryRegistry;
import com.rental.notification.EmailClientAdapter;
import com.rental.notification.LegacyEmailClient;
import com.rental.payment.LegacyPaymentAdapter;
import com.rental.payment.LegacyPaymentProcessor;
import com.rental.pricing.CorporatePricing;
import com.rental.pricing.DailyPricing;
import com.rental.pricing.MonthlySubscriptionPricing;
import com.rental.pricing.QuoteService;
import com.rental.pricing.StudentPricing;
import com.rental.pricing.WeeklyDiscountPricing;
import com.rental.repo.Database;
import com.rental.repo.JdbcReservationRepository;
import com.rental.repo.JdbcVehicleRepository;
import com.rental.repo.SeedData;
import com.rental.repo.VehicleRepository;
import com.rental.service.RentalFacade;
import com.rental.web.RentalController;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RentalApplication {
    public static void main(String[] args) throws IOException {
        Database db = new Database();
        VehicleRepository vehicleRepository = new JdbcVehicleRepository(db);
        VehicleFactoryRegistry registry = new VehicleFactoryRegistry();
        SeedData.load(vehicleRepository, registry);

        RentalFacade rentalFacade = new RentalFacade(
                vehicleRepository,
                new QuoteService(new DailyPricing(), new WeeklyDiscountPricing(),
                        new MonthlySubscriptionPricing(), new CorporatePricing(), new StudentPricing()),
                new LegacyPaymentAdapter(new LegacyPaymentProcessor()),
                new JdbcReservationRepository(db),
                new EmailClientAdapter(new LegacyEmailClient()));

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", new RentalController(rentalFacade, registry, vehicleRepository));
        server.createContext("/", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())
                    || !("/".equals(exchange.getRequestURI().getPath())
                    || "/index.html".equals(exchange.getRequestURI().getPath()))) {
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }
            try (var page = RentalApplication.class.getResourceAsStream("/public/index.html")) {
                if (page == null) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }
                byte[] content = page.readAllBytes();
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(200, content.length);
                exchange.getResponseBody().write(content);
                exchange.close();
            }
        });
        server.start();
        System.out.println("Vehicle rental service is available at http://localhost:8080");
    }
}
