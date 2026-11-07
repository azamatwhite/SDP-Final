package com.rental.service;

import com.rental.domain.Car;
import com.rental.domain.Vehicle;
import com.rental.notification.NotificationService;
import com.rental.payment.PaymentGateway;
import com.rental.pricing.DailyPricing;
import com.rental.pricing.QuoteService;
import com.rental.pricing.WeeklyDiscountPricing;
import com.rental.repo.InMemoryVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RentalFacadeTest {
    private InMemoryVehicleRepository vehicleRepository;
    private InMemoryReservationRepository reservationRepository;
    private RecordingPaymentGateway paymentGateway;
    private RecordingNotificationService notificationService;
    private RentalFacade rentalFacade;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository = new InMemoryVehicleRepository();
        reservationRepository = new InMemoryReservationRepository();
        paymentGateway = new RecordingPaymentGateway(true);
        notificationService = new RecordingNotificationService();
        rentalFacade = new RentalFacade(vehicleRepository,
                new QuoteService(new DailyPricing(), new WeeklyDiscountPricing()),
                paymentGateway, reservationRepository, notificationService);
        vehicle = new Car("car-1", "Toyota", "Camry", 5, "Petrol",
                new BigDecimal("50.00"), true);
        vehicleRepository.save(vehicle);
    }

    @Test
    void quoteUsesVehicleDailyRateAndRentalDuration() {
        assertEquals(new BigDecimal("150.00"), rentalFacade.quote("car-1", 3));
    }

    @Test
    void weeklyDiscountAndExtrasAreApplied() {
        // 50 * 7 = 350 -> 15% off = 297.50 -> + GPS 50.00 = 347.50 -> + insurance 10% = 382.25
        BigDecimal total = RentalFacade.price(rentalFacade.buildQuote(
                "car-1", 7, "WEEKLY_DISCOUNT", List.of("GPS", "INSURANCE")));
        assertEquals(new BigDecimal("382.25"), total);
    }

    @Test
    void successfulCheckoutChargesAndSavesReservation() {
        Reservation reservation = rentalFacade.checkout("car-1", 2, "customer-1", "renter@example.com");

        assertEquals("customer-1", paymentGateway.customerId);
        assertEquals(new BigDecimal("100.00"), paymentGateway.amount);
        assertEquals(1, reservationRepository.findAll().size());
        assertEquals(reservation.getId(), reservationRepository.findAll().get(0).getId());
        assertFalse(vehicle.isAvailable());
        assertEquals("renter@example.com", notificationService.recipient);
        assertTrue(notificationService.message.contains(reservation.getId()));
    }

    @Test
    void failedPaymentDoesNotSaveReservationOrMakeVehicleUnavailable() {
        paymentGateway.approved = false;

        assertThrows(PaymentFailedException.class,
                () -> rentalFacade.checkout("car-1", 1, "customer-1", "renter@example.com"));

        assertTrue(reservationRepository.findAll().isEmpty());
        assertTrue(vehicle.isAvailable());
        assertNull(notificationService.recipient);
    }

    @Test
    void unavailableVehicleCannotBeReserved() {
        vehicle.setAvailable(false);

        assertThrows(VehicleUnavailableException.class,
                () -> rentalFacade.checkout("car-1", 1, "customer-1", "renter@example.com"));

        assertTrue(reservationRepository.findAll().isEmpty());
        assertNull(paymentGateway.customerId);
    }

    @Test
    void invalidRentalDurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> rentalFacade.quote("car-1", 0));
    }

    @Test
    void cancelMakesVehicleAvailableAgain() {
        Reservation r = rentalFacade.checkout("car-1", 2, "customer-1", "renter@example.com");
        rentalFacade.cancel(r.getId());

        assertTrue(vehicle.isAvailable());
        assertEquals(ReservationStatus.CANCELLED, reservationRepository.findAll().get(0).getStatus());
        assertThrows(IllegalStateException.class, () -> rentalFacade.cancel(r.getId()));
    }

    private static class RecordingPaymentGateway implements PaymentGateway {
        private boolean approved;
        private String customerId;
        private BigDecimal amount;

        private RecordingPaymentGateway(boolean approved) {
            this.approved = approved;
        }

        @Override
        public boolean charge(String customerId, BigDecimal amount) {
            this.customerId = customerId;
            this.amount = amount;
            return approved;
        }
    }

    private static class RecordingNotificationService implements NotificationService {
        private String recipient;
        private String message;

        @Override
        public void send(String to, String message) {
            recipient = to;
            this.message = message;
        }
    }
}
