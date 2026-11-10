# Vehicle Rental System

### Course: Software Design Patterns (Final Project)
- **Group:** 2534
- **Team members:**
  - Absamat Ruslan
  - Yernazar Meiirzhan
  - Zhiyembetov Azamat

---

## Project Overview
A vehicle rental service supporting multiple vehicle categories (cars, motorcycles, scooters), dynamic pricing models, optional rental add-ons, and date-range reservations.

The primary objective of the project is demonstrating the practical application of Gang of Four (GoF) design patterns across creational, structural, and behavioral categories.

---

## Implemented Design Patterns

### 1. Creational Patterns

- **Factory Method** (`com.rental.creation`)
  - `VehicleFactory`: abstract creator defining the factory method for vehicle instantiation.
  - `CarFactory`, `MotorcycleFactory`, `ScooterFactory`: concrete creators producing specific vehicle types.
  - `VehicleFactoryRegistry`: encapsulates factory registration and routes creation based on `VehicleType`.

- **Builder** (`com.rental.creation.VehicleBuilder`)
  - Provides step-by-step construction of `Vehicle` objects with validation for required attributes (brand, model, positive daily rate, seat capacity).
  - Used internally by factories and repository mappers.

---

### 2. Structural Patterns

- **Facade** (`com.rental.service.RentalFacade`)
  - Serves as the single unified entry point for all rental operations.
  - Coordinates date availability checks, quote generation, payment processing, persistence, and confirmation notifications, decoupling the web controller from underlying subsystems.

- **Adapter** (`com.rental.payment`, `com.rental.notification`)
  - `LegacyPaymentAdapter`: adapts `LegacyPaymentProcessor` to the application's `PaymentGateway` interface.
  - `EmailClientAdapter`: adapts `LegacyEmailClient` to the `NotificationService` interface.

- **Decorator** (`com.rental.pricing`)
  - Dynamically attaches additional fees and optional services to a rental quote:
    - `HelmetDecorator`: adds protective helmet fee (+1,000 KZT, restricted to motorcycles and scooters).
    - `ChildSeatDecorator`: adds child safety seat fee (+2,000 KZT, restricted to cars).
    - `InsuranceDecorator`: applies comprehensive coverage (+10% of total).

---

### 3. Behavioral Patterns

- **Strategy** (`com.rental.pricing`)
  - Defines a family of interchangeable pricing algorithms via `PricingStrategy`:
    - `DailyPricing`: standard daily base rate.
    - `WeeklyDiscountPricing`: 15% discount for rentals of 7 days or more.
    - `MonthlySubscriptionPricing`: 30% discount for long-term rentals (30+ days).
    - `StudentPricing`: 20% discount for students (`ASTANA-IT` / `AITU` promo codes).
    - `CorporatePricing`: 10% partner discount (`CORP10` promo code).
  - `QuoteService`: resolves and selects the best applicable pricing strategy.

---

## UML Diagrams
All architecture diagrams are located in `docs/uml/`:
- `rental-facade-adapter-class.puml` — Class diagram for Facade, Repositories, and Adapters.
- `rental-checkout-sequence.puml` — Sequence diagram detailing the checkout workflow (`/api/checkout`).
- `pricing-strategy-decorator-class.puml` — Class diagram for Strategy and Decorator pricing modules.
- `vehicle-creation-class.puml` — Class diagram for Factory Method, Builder, and Vehicle domain hierarchy.
