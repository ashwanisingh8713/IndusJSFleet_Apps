# AGENTS.md — screen-driver

## Purpose

Full-stack **Driver Management** feature module. Handles driver CRUD, list with search and status filters, detail with tabs (overview, edit, costs), driver cost entry, and state transitions.

**Package:** `com.ijs.driver`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/driver/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   └── DriverRemoteDataSource.kt      # API: list, detail, create, update, state change, costs
│   ├── mapper/
│   │   └── DriverMapper.kt                # DriverDto ↔ Driver entity
│   ├── model/
│   │   ├── DriverDto.kt                   # @Serializable DTOs + API response wrappers
│   │   └── DriverCostModels.kt            # Driver cost DTOs (salary, advance, bonus, penalty)
│   └── repository/
│       └── DriverRepositoryImpl.kt        # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   └── Driver.kt                      # Driver domain entity (profile, license, status)
│   ├── repository/
│   │   └── DriverRepository.kt            # Repository interface
│   └── usecase/
│       └── DriverUseCases.kt              # GetDrivers, GetDriverDetail, CreateDriver, etc.
└── presentation/
    ├── DriverFeatureFacade.kt             # DI entry point: creates repositories + ViewModels
    ├── DriverStateOptions.kt              # State transition options per current driver state
    ├── DriversContract.kt                 # List screen MVI contract (State/Intent/Effect)
    ├── DriversScreen.kt                   # Driver list with search + status filter chips
    ├── DriversViewModel.kt                # List ViewModel
    ├── create/
    │   ├── CreateDriverContract.kt
    │   ├── CreateDriverScreen.kt          # Driver registration form (profile + license info)
    │   └── CreateDriverViewModel.kt
    ├── cost/
    │   ├── DriverCostEntryContract.kt
    │   ├── DriverCostEntryScreen.kt       # Record driver cost (salary, advance, bonus, penalty)
    │   └── DriverCostEntryViewModel.kt
    └── detail/
        ├── DriverDetailContract.kt        # Detail MVI contract
        ├── DriverDetailScreen.kt          # Tabbed detail: Overview, Edit, Costs
        ├── DriverDetailViewModel.kt       # Loads driver + costs + trip history
        ├── DriverDetailOverviewContent.kt # Overview tab (profile, license, status)
        ├── DriverDetailEditContent.kt     # Inline edit form
        ├── DriverDetailCostsContent.kt    # Costs tab with cost history
        └── DriverCostComponents.kt        # Cost display cards and rows
```

---

## Driver State Machine

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```

States defined in `StatusConstants` (ijs-core-lib). Transitions filtered by `DriverStateOptions`.

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` → `:ijs-core-lib` → `:ijs-error-lib`, `:ijs-dispatcher-lib`, `:ijs-datetime-utils` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client |

> **Note:** `ijs-network-lib` does NOT depend on `screen-driver` (no circular dependency).
> `sharedUI/DefaultViewModelProvider` calls `DriverFeatureFacade` to create repositories and ViewModels.

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| DriversScreen | `Drivers` | List with search + status filter chips |
| DriverDetailScreen | `DriverDetail(id)` | Tabbed: Overview, Edit, Costs |
| CreateDriverScreen | `CreateDriver` | Registration with license info |
| DriverCostEntryScreen | `DriverCostEntry(driverId?)` | Record salary/advance/bonus/penalty |

---

## Driver Cost Types

| Type | Description |
|------|-------------|
| `salary` | Monthly salary payment |
| `advance` | Advance payment to driver |
| `bonus` | Performance/trip bonus |
| `penalty` | Deduction for damage/violation |

---

## Key Patterns

- **FeatureFacade** — `DriverFeatureFacade` is the DI entry point. `sharedUI/DefaultViewModelProvider` calls it to wire repositories and ViewModels.
- **State transitions** — `DriverStateOptions.getAvailableTransitions(currentState)` returns valid next states, used in `StateChangeDialog` from `ijs-ui-components-lib`.
- **Detail decomposition** — `DriverDetailScreen` splits tabs into separate composable files (`DriverDetailOverviewContent`, `DriverDetailEditContent`, `DriverDetailCostsContent`).
- **Cost entry** — `DriverCostEntryScreen` uses cost type chips and date/time picker from shared UI libs.
