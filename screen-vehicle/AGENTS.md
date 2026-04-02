# AGENTS.md — screen-vehicle

## Purpose

Full-stack **Vehicle Management** feature module. Handles vehicle CRUD, detail with tabbed content (overview, trips, documents, costs, routes), add vehicle form, maintenance cost entry, and state transitions. This is one of the most content-rich modules in the app.

**Package:** `com.ijs.vehicle`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/vehicle/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   └── VehicleRemoteDataSource.kt     # API calls: list, detail, create, update, state change, documents
│   ├── mapper/
│   │   └── VehicleMapper.kt               # VehicleDto ↔ Vehicle/VehicleDetail
│   ├── model/
│   │   └── VehicleDto.kt                  # @Serializable DTOs + API response wrappers
│   └── repository/
│       └── VehicleRepositoryImpl.kt       # Repository implementation with auth token pattern
├── domain/
│   ├── entity/
│   │   ├── Vehicle.kt                     # Vehicle list entity
│   │   └── VehicleDetail.kt               # Full vehicle detail entity
│   ├── repository/
│   │   └── VehicleRepository.kt           # Repository interface
│   └── usecase/
│       └── VehicleUseCases.kt             # GetVehicles, GetVehicleDetail, CreateVehicle, UpdateVehicle, etc.
└── presentation/
    ├── VehicleFeatureFacade.kt            # DI entry point: creates repositories + ViewModels
    ├── VehicleStateOptions.kt             # State transition options per current state
    ├── VehiclesContract.kt                # List screen MVI contract (State/Intent/Effect)
    ├── VehiclesScreen.kt                  # Vehicle list with search + status filter chips
    ├── VehiclesViewModel.kt               # List ViewModel
    ├── AddVehicleContract.kt              # Add vehicle MVI contract
    ├── AddVehicleScreen.kt                # Multi-step add vehicle form
    ├── AddVehicleViewModel.kt             # Add vehicle ViewModel
    ├── costs/
    │   ├── MaintenanceCostEntryContract.kt
    │   ├── MaintenanceCostEntryScreen.kt  # Record maintenance expense (tyre, battery, service, etc.)
    │   └── MaintenanceCostEntryViewModel.kt
    └── detail/
        ├── VehicleDetailContract.kt       # Detail MVI contract
        ├── VehicleDetailScreen.kt         # Tabbed detail: Overview, Trips, Documents, Costs, Routes
        ├── VehicleDetailViewModel.kt      # Detail ViewModel (loads vehicle + related data)
        ├── VehicleCostComponents.kt       # Cost display cards and rows
        ├── VehicleDetailCostsContent.kt   # Costs tab content
        ├── VehicleDetailDocumentsContent.kt # Documents tab (RC, Insurance, Permit, etc.)
        ├── VehicleDetailEditContent.kt    # Inline edit form
        ├── VehicleDetailOverviewContent.kt # Overview tab
        ├── VehicleDetailRouteContent.kt   # Route/location tab
        └── VehicleDetailTripsContent.kt   # Trips tab
```

---

## Vehicle State Machine

```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned
```

States are defined in `StatusConstants` (ijs-core-lib). Transitions are filtered by `VehicleStateOptions`.

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:screen-team`, `:screen-driver` |
| implementation | `:ijs-pdf-report`, `:ijs-datetime-picker` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client, kotlinx-datetime |

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| VehiclesScreen | `Vehicles` | List with search + status filter chips |
| VehicleDetailScreen | `VehicleDetail(id)` | Tabbed detail: Overview, Trips, Documents, Costs, Routes |
| AddVehicleScreen | `AddVehicle` | Multi-step registration with document upload |
| MaintenanceCostEntryScreen | `MaintenanceCostEntry(vehicleId?)` | Record maintenance expense |

---

## Key Patterns

- **FeatureFacade** — `VehicleFeatureFacade` is the DI entry point. `sharedUI/DefaultViewModelProvider` calls it to create repositories and ViewModels.
- **Tabbed detail** — `VehicleDetailScreen` uses `TabRow` with 5 content composables, each in its own file for maintainability.
- **State transitions** — `VehicleStateOptions.getAvailableTransitions(currentState)` returns valid next states, used in `StateChangeDialog`.
- **Cost entry** — `MaintenanceCostEntryScreen` uses cost type chips from `ijs-ui-components-lib` and date/time from `ijs-datetime-picker`.
