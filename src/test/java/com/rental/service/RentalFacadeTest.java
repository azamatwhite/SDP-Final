package com.rental.service;

import com.rental.domain.Car;
import com.rental.domain.Motorcycle;
import com.rental.domain.Scooter;
import com.rental.domain.Vehicle;
import com.rental.notification.NotificationService;
import com.rental.payment.PaymentGateway;
import com.rental.pricing.CorporatePricing;
import com.rental.pricing.DailyPricing;
import com.rental.pricing.MonthlySubscriptionPricing;
import com.rental.pricing.Quote;
import com.rental.pricing.QuoteService;
import com.rental.pricing.StudentPricing;
import com.rental.pricing.WeeklyDiscountPricing;
import com.rental.repo.InMemoryVehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
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
                new QuoteService(new DailyPricing(), new WeeklyDiscountPricing(),
                        new MonthlySubscriptionPricing(), new CorporatePricing(), new StudentPricing()),
                paymentGateway, reservationRepository, notificationService);
        vehicle = new Car("car-1", "Toyota", "Camry", 5, "Petrol",
                new BigDecimal("25000.00"), true);
        vehicleRepository.save(vehicle);
    }

    @Test
    void quoteUsesVehicleDailyRateAndRentalDuration() {
        assertEquals(new BigDecimal("75000.00"), rentalFacade.quote("car-1", 3));
    }

    @Test
    void weeklyDiscountAndExtrasAreApplied() {
        // 25000 * 7 = 175000 -> 15% off = 148750.00 -> + GPS 3000.00 = 151750.00 -> + insurance 10% = 166925.00
        BigDecimal total = RentalFacade.price(rentalFacade.buildQuote(
                "car-1", 7, "WEEKLY_DISCOUNT", List.of("GPS", "INSURANCE")));
        assertEquals(new BigDecimal("166925.00"), total);
    }

    @Test
    void studentPromoCodeAppliesAituTwentyPercentDiscount() {
        // 25000 * 2 = 50000 -> 20% student discount = 40000.00
        BigDecimal total = RentalFacade.price(rentalFacade.buildQuote(
                "car-1", 2, "ASTANA-IT", List.of()));
        assertEquals(new BigDecimal("40000.00"), total);
    }

    @Test
    void monthlySubscriptionAppliesThirtyPercentDiscount() {
        // 25000 * 30 = 750000 -> 30% monthly discount = 525000.00
        BigDecimal total = RentalFacade.price(rentalFacade.buildQuote(
                "car-1", 30, "MONTHLY", List.of()));
        assertEquals(new BigDecimal("525000.00"), total);
    }

    @Test
    void corporatePromoAppliesTenPercentDiscount() {
        // 25000 * 3 = 75000 -> 10% corporate discount = 67500.00
        BigDecimal total = RentalFacade.price(rentalFacade.buildQuote(
                "car-1", 3, "CORP10", List.of()));
        assertEquals(new BigDecimal("67500.00"), total);
    }

    @Test
    void findVehiclesFiltersBySearchCategoryAndSorts() {
        Vehicle moto = new Motorcycle("moto-1", "Yamaha", "MT-07", 2, "Petrol",
                new BigDecimal("18000.00"), true);
        vehicleRepository.save(moto);

        List<Vehicle> onlyCars = rentalFacade.findVehicles(null, "CAR", "default");
        assertEquals(1, onlyCars.size());
        assertEquals("Toyota", onlyCars.get(0).getBrand());

        List<Vehicle> searched = rentalFacade.findVehicles("Yamaha", "ALL", "default");
        assertEquals(1, searched.size());
        assertEquals("moto-1", searched.get(0).getId());

        List<Vehicle> sorted = rentalFacade.findVehicles(null, "ALL", "priceAsc");
        assertEquals(2, sorted.size());
        assertEquals("moto-1", sorted.get(0).getId()); // 18000 < 25000
    }

    @Test
    void successfulCheckoutChargesAndSavesReservation() {
        Reservation reservation = rentalFacade.checkout("car-1", 2, "customer-1", "renter@example.com");

        assertEquals("customer-1", paymentGateway.customerId);
        assertEquals(new BigDecimal("50000.00"), paymentGateway.amount);
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

    @Test
    void childSeatAndGpsValidationOnScootersAndMotorcycles() {
        Vehicle scooter = new Scooter("scooter-1", "Ninebot", "KickScooter Max", 1, "Electric",
                new BigDecimal("5000.00"), true);
        vehicleRepository.save(scooter);

        Vehicle moto = new Motorcycle("moto-2", "Honda", "CB500F", 2, "Petrol",
                new BigDecimal("15000.00"), true);
        vehicleRepository.save(moto);

        // Child seat is only allowed for cars
        assertThrows(IllegalArgumentException.class,
                () -> rentalFacade.buildQuote("scooter-1", 1, "DAILY", List.of("CHILD_SEAT")));
        assertThrows(IllegalArgumentException.class,
                () -> rentalFacade.buildQuote("moto-2", 1, "DAILY", List.of("CHILD_SEAT")));

        // GPS is not allowed for scooters
        assertThrows(IllegalArgumentException.class,
                () -> rentalFacade.buildQuote("scooter-1", 1, "DAILY", List.of("GPS")));
    }

    @Test
    void bestDiscountSelectedWhenPromoProvidedForLongRental() {
        // If 30+ days, monthly subscription discount (30%) is better than 20% student discount
        Quote quote = rentalFacade.buildQuote("car-1", 30, "ASTANA-IT", List.of());
        assertEquals(new BigDecimal("525000.00"), RentalFacade.price(quote));
        assertTrue(quote.lines().get(0).startsWith("MONTHLY"));
    }

    @Test
    void helmetOptionAvailableForScootersAndRejectedForCars() {
        Vehicle scooter = new Scooter("scooter-2", "Ninebot", "KickScooter", 1, "Electric",
                new BigDecimal("5000.00"), true);
        vehicleRepository.save(scooter);

        // 5000 + 1000 helmet = 6000
        Quote quote = rentalFacade.buildQuote("scooter-2", 1, "DAILY", List.of("HELMET"));
        assertEquals(new BigDecimal("6000.00"), RentalFacade.price(quote));
        assertTrue(quote.lines().stream().anyMatch(l -> l.contains("Protective Helmet")));

        // Helmet cannot be added to a car
        assertThrows(IllegalArgumentException.class,
                () -> rentalFacade.buildQuote("car-1", 1, "DAILY", List.of("HELMET")));
    }

    @Test
    void vehicleAvailableForFutureDatesWhenCurrentlyRented() {
        LocalDate currentStart = LocalDate.of(2026, 11, 10);
        LocalDate currentEnd = LocalDate.of(2026, 11, 13);
        rentalFacade.checkout("car-1", 3, "cust-1", "user@example.com", "DAILY", List.of(), currentStart, currentEnd);

        // Conflicting dates are rejected
        assertFalse(rentalFacade.isVehicleAvailableForDates("car-1",
                LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 14)));
        assertThrows(VehicleUnavailableException.class, () ->
                rentalFacade.checkout("car-1", 3, "cust-2", "user2@example.com", "DAILY", List.of(),
                        LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 14)));

        // Non-conflicting future dates are available!
        LocalDate futureStart = LocalDate.of(2026, 11, 14);
        LocalDate futureEnd = LocalDate.of(2026, 11, 28);
        assertTrue(rentalFacade.isVehicleAvailableForDates("car-1", futureStart, futureEnd));

        // Booking non-conflicting future dates succeeds
        Reservation futureReservation = rentalFacade.checkout("car-1", 14, "cust-2", "user2@example.com",
                "DAILY", List.of(), futureStart, futureEnd);
        assertNotNull(futureReservation);
        assertEquals(futureStart, futureReservation.getStartDate());
        assertEquals(futureEnd, futureReservation.getEndDate());
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
