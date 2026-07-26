# Smart Parking System

## Project Brief

A backend system for managing an urban multi-floor smart parking lot. The system handles vehicle entry and exit, automatically assigns parking spots based on vehicle size, tracks time spent, and calculates fees on exit.

**Key capabilities:**
- Vehicle check-in / check-out via entry and exit gate controllers
- Automatic spot allocation based on vehicle type (Motorcycle, Car, Bus)
- Fee calculation using pluggable strategies (flat-rate or slab-based)
- Real-time availability updates via an observer notification mechanism
- REST API surface for gate operations and admin management
- In-memory H2 database for persistence (JPA/Hibernate)

---

## Design Patterns Used

| Pattern | Where / Why |
|---|---|
| **Strategy** | `IAllocationStrategy` — `NearestSpotAllocationStrategy` and `BestFitAllocationStrategy` are swappable spot-picking algorithms. `IFeeStrategy` — `FlatRateFeeStrategy` and `SlabBasedFeeStrategy` are swappable fee calculators. |
| **Factory** | `VehicleFactory` creates the correct `Vehicle` subtype (Motorcycle, Car, Bus) from a `VehicleType` enum, keeping instantiation logic out of services. |
| **Observer** | `IAvailabilityObserver` / `DisplayBoardObserver` — subscribers are notified when spot availability changes, so displays update in real time without polling. |
| **Repository** | `ParkingSpotRepository`, `TicketRepository`, `VehicleRepository`, `FloorRepository` abstract all persistence operations, decoupling the service layer from storage details. |
| **Singleton** | Spring manages `ParkingLotConfig` and services as singletons through its IoC container, ensuring one shared parking-lot state per application instance. |

SOLID principles are applied throughout: single-responsibility services (`SpotAllocationService`, `FeeCalculationService`, `TicketService`), open/closed extension via strategy interfaces, and dependency inversion via injected abstractions.

---

## Architecture Overview

```
CLI Layer (Controllers — Scanner-based, no REST)
  ├── EntryGateController   → check-in flow  (reads license plate + vehicle type from stdin)
  ├── ExitGateController    → check-out flow (reads ticket UUID from stdin, prints fee)
  └── AdminController       → availability view, add floor, add spot
          │
          ▼
Service Layer
  ├── ParkingLotService     → orchestrates check-in / check-out flow
  ├── SpotAllocationService → delegates to IAllocationStrategy
  ├── FeeCalculationService → delegates to IFeeStrategy
  └── TicketService         → manages Ticket lifecycle (create, close, find)
          │
          ▼
Strategy Layer
  ├── IAllocationStrategy
  │     ├── NearestSpotAllocationStrategy
  │     └── BestFitAllocationStrategy
  └── IFeeStrategy
        ├── FlatRateFeeStrategy
        └── SlabBasedFeeStrategy
          │
          ▼
Repository Layer  (Spring Data JPA → H2 in-memory DB)
  ├── ParkingSpotRepository
  ├── TicketRepository
  ├── VehicleRepository
  └── FloorRepository
          │
          ▼
Domain Model (Entities)
  ├── Vehicle (abstract) → Motorcycle, Car, Bus
  ├── ParkingSpot        → belongs to Floor, has SpotSize & SpotStatus
  ├── Floor              → contains ParkingSpots
  ├── Ticket             → entry/exit record, fee, status
  └── RateCard           → fee parameters per vehicle type

Observer (cross-cutting)
  └── DisplayBoardObserver → listens to IAvailabilityObserver events
```

---

## How to Run

**Prerequisites:** Java 17+, Gradle (wrapper included)

```bash
# Clone / open the project, then from the project root:
./gradlew bootRun
```

The application starts and presents an interactive CLI menu. Follow the on-screen prompts to:

1. **Check in a vehicle** — enter license plate and select vehicle type (Motorcycle / Car / Bus)
2. **Check out a vehicle** — enter the ticket UUID printed at check-in
3. **View availability** — see free/occupied spot counts per floor broken down by size (S/M/L)
4. **Add a floor / Add a spot** — admin operations to configure the lot

The H2 console is available at **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:testdb`) while the app is running.

---

## How to Run Tests

```bash
# Run all tests
./gradlew test

# Run tests with a detailed report
./gradlew test --info

# View HTML test report after the run
open build/reports/tests/test/index.html
```

**Test coverage:**

| Test Class | What it covers |
|---|---|
| `ParkingLotServiceTest` | End-to-end check-in / check-out flow |
| `SpotAllocationServiceTest` | Spot allocation strategies and edge cases |
| `FeeCalculationServiceTest` | Flat-rate and slab-based fee computation |
| `SlabBasedFeeStrategyTest` | Slab boundary conditions |
| `ParkingSpotRepositoryTest` | Repository query correctness |
