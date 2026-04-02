# AGENTS.md — screen-trip

## Purpose

Full-stack **Trip Management** feature module — the most complex feature in IndusJS Fleet. Handles trip CRUD, multi-step creation (vehicle + driver + route + schedule + cargo + customer), trip detail with costs/payments tabs, trip cost entry, and state transitions. Depends on vehicle, driver, customer, and payment modules for cross-feature data.

**Package:** `com.ijs.trip`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/trip/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   └── TripRemoteDataSource.kt        # API: list, detail, create, update, state change, costs
│   ├── mapper/
│   │   └── TripMapper.kt                  # TripDto ↔ Trip entity
│   ├── model/
│   │   └── TripDto.kt                     # @Serializable DTOs + request/response wrappers
│   └── repository/
│       └── TripRepositoryImpl.kt          # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   └── Trip.kt                        # Trip domain entity + nested types (route, cargo, schedule)
│   ├── repository/
│   │   └── TripRepository.kt             # Repository interface
│   └── usecase/
│       └── TripUseCases.kt               # GetTrips, GetTripDetail, CreateTrip, UpdateTrip, etc.
└── presentation/
    ├── TripFeatureFacade.kt               # DI entry point
    ├── TripStateOptions.kt                # State transition options per current trip state
    ├── TripsContract.kt                   # List screen MVI contract
    ├── TripsScreen.kt                     # Trip list with status filter chips
    ├── TripsViewModel.kt                  # List ViewModel
    ├── cost/
    │   ├── TripCostEntryContract.kt
    │   ├── TripCostEntryScreen.kt         # Record trip expense (fuel, toll, loading, etc.)
    │   ├── TripCostEntryViewModel.kt
    │   └── util/
    │       └── TripCostToDriverCostMapper.kt  # Maps trip cost → driver cost when driver bears cost
    ├── create/
    │   ├── CreateTripContract.kt          # Multi-step wizard MVI contract
    │   ├── CreateTripScreen.kt            # 4-step wizard: Schedule → Route → Cargo → Review
    │   └── CreateTripViewModel.kt         # Handles Google Places, vehicle/driver selection, validation
    └── detail/
        ├── TripDetailContract.kt          # Detail MVI contract
        ├── TripDetailScreen.kt            # Tabbed detail view
        ├── TripDetailViewModel.kt         # Loads trip + costs + payments
        ├── TripDetailActionHandler.kt     # State change, edit, delete action processing
        ├── TripDetailDataLoader.kt        # Async data loading orchestration
        ├── TripDetailStateManager.kt      # UI state transitions for detail screen
        ├── TripDetailComponents.kt        # Shared detail UI elements
        ├── TripDetailInfoSections.kt      # Route, schedule, cargo, customer info sections
        ├── TripDetailViewContent.kt       # Read-only detail content
        ├── TripDetailEditContent.kt       # Inline edit form
        ├── TripDetailEditForms.kt         # Edit form field groups
        ├── TripDetailLocationHandler.kt   # Google Places integration for route editing
        ├── TripCostsContent.kt            # Costs tab content
        └── TripPaymentsContent.kt         # Payments tab content
```

---

## Trip State Machine

```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

States defined in `StatusConstants` (ijs-core-lib). Transitions filtered by `TripStateOptions`.

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:screen-vehicle`, `:screen-driver`, `:screen-customer`, `:screen-payment` |
| implementation | `:ijs-pdf-report`, `:ijs-datetime-picker` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client |

This is the **most connected** module — it depends on 4 other screen modules for cross-entity selection (vehicle picker, driver picker, customer picker, payment data).

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| TripsScreen | `Trips` | List with status filter chips (planned, on_route, completed, etc.) |
| TripDetailScreen | `TripDetail(id)` | Tabbed detail: Info, Costs, Payments |
| CreateTripScreen | `CreateTrip` | 4-step wizard with `FleetStepIndicator` |
| TripCostEntryScreen | `TripCostEntry(tripId?, vehicleId?)` | Record trip expense |

---

## Key Patterns

- **Multi-step wizard** — `CreateTripScreen` uses `FleetStepIndicator` and step-based state. Each step validates before allowing next.
- **Google Places integration** — `CreateTripViewModel` and `TripDetailLocationHandler` use `GooglePlacesService` from `ijs-network-lib` for route autocomplete and distance calculation.
- **Cross-module data** — Trip creation needs vehicles, drivers, and customers from their respective modules. `TripFeatureFacade` accepts these repositories.
- **Cost-to-driver mapping** — When a trip cost is marked as "driver bears cost," `TripCostToDriverCostMapper` creates a corresponding driver cost entry.
- **Detail decomposition** — `TripDetailScreen` is split across 14 files for maintainability due to the complexity of trip detail (info, costs, payments, edit, location, state management).
