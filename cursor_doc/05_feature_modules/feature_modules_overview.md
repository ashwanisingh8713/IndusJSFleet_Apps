# Feature Modules Overview

## Summary Table

| Module | Files | Data Layer | Use Cases | Facade Entries | Special Patterns |
|--------|-------|------------|-----------|----------------|-----------------|
| `screen-vehicle` | 23 | Full (remote) | 7 | 4 | Standard |
| `screen-driver` | 23 | Full (remote) | 8 | 4 | Standard |
| `screen-trip` | 34 | Full (remote) | Multiple | 4 | Decomposed VM (handlers) |
| `screen-customer` | 29 | Full (remote + local) | Yes | 3 | Handlers + PDF exporter |
| `screen-payment` | 18 | Full (remote) | No | 3 | Repo direct, add/edit shared |
| `screen-team` | 19 | Full (remote + local) | No | 3 | Repo direct |
| `screen-report` | 32 | Full (remote) | 9 | 5 | Report generator + date calc |
| `screen-finance` | 15 | Full (remote) | No | 4 | Single shared VM |
| `screen-user` | 17 | Presentation only | N/A | 5 | Auth data in ijs-network-lib |
| `screen-onboarding` | 5 | Presentation only | N/A | 1 | Minimal |
| `screen-dashboard` | 12 | Presentation only | N/A | 1 | Widest nav surface |
| `screen-alerts` | 5 | Presentation only | N/A | 1 | Minimal |
| `screen-map` | 5 | Presentation only | N/A | 1 | Minimal |

## Facade Pattern

Every feature exposes an `object *FeatureFacade` with `@Composable` entry functions:

```kotlin
object VehicleFeatureFacade {
    @Composable
    fun VehiclesListEntry(
        viewModel: VehiclesViewModel,
        onNavigateToVehicleDetail: (String) -> Unit,
        onNavigateToAddVehicle: () -> Unit,
        onNavigateBack: () -> Unit = {}
    ) { VehiclesScreen(viewModel, ...) }
}
```

**Key design:**
- Takes a pre-built ViewModel (from sharedUI DI)
- Takes navigation lambdas (NO FleetRoute references inside feature modules)
- Delegates to the real Screen composable
- Acts as the navigation-agnostic public API for `FleetNavigation.kt`

## Full-Stack Feature Modules

### screen-vehicle
**Screens:** VehiclesList, AddVehicle, VehicleDetail, MaintenanceCostEntry

**Domain entities:** `Vehicle`, `VehicleDetail`
**Vehicle states:** inactive → active → on_route → maintenance → damaged → decommissioned

**Use cases:** GetVehicles, GetAvailableVehicles, GetVehicleById, CreateVehicle, CreateVehicleWithDocuments, UpdateVehicle, DeleteVehicle

---

### screen-driver
**Screens:** DriversList, CreateDriver, DriverDetail, DriverCostEntry

**Domain entities:** `Driver`
**Driver states:** inactive → active → on_route → on_leave → suspended → terminated

**Use cases:** GetDrivers, GetAvailableDrivers, GetDriverById, CreateDriver, UpdateDriver, UpdateDriverStatus, ToggleDriverActive, DeleteDriver

**Note:** Driver cost DTOs are centralized in `ijs-core-lib` to avoid circular dependencies.

---

### screen-trip
**Screens:** TripsList, CreateTrip, TripDetail, TripCostEntry

**Domain entities:** `Trip`
**Trip states:** planned → on_route → completed / cancelled / failed / delayed

**Use cases:** Trip CRUD, status transitions, cancel, stops CRUD

**Special:** TripDetail is decomposed into internal helper classes:
- `TripDetailDataLoader` — loads vehicle, trips, documents
- `TripDetailActionHandler` — handles user actions
- `TripDetailLocationHandler` — Google Places integration
- `TripDetailStateManager` — state transition interface
- `TripCostToDriverCostMapper` — cross-concern mapping

---

### screen-customer
**Screens:** CustomersList, CustomerDetail, CreateCustomer

**Domain entities:** `Customer` (rich aggregate: trips, payments, financials)
**Data sources:** Remote + Local (Room cache)

**Special:** Detail screen decomposed into:
- `CustomerDetailTripsHandler`, `CustomerDetailPaymentsHandler`
- `CustomerDetailFinancialsHandler`, `CustomerDetailPdfExporter`

---

### screen-payment
**Screens:** PaymentsList, PaymentDetail, AddPayment (also used for EditPayment)

**Domain entities:** `TripPayment`, `PaymentEnums`
**Payment modes:** cash, upi, bank_transfer, cheque, card
**Payment status:** received, pending, cancelled

**Special:** No use case layer. VMs use repository + `TripProviderForPayment` abstraction directly. Multiple contracts in one file. Add and Edit share the same screen (differentiated by paymentId).

---

### screen-team
**Screens:** TeamList, CreateTeamMember, TeamMemberDetail

**Domain entities:** `TeamMember`
**Data sources:** Remote + Local (Room cache)

**Special:** No use case layer.

---

### screen-report
**Screens:** ReportsHub, VehicleProfitLoss, TripProfitLoss, CostAnalysis, ConsolidatedPL

**Domain entities:** `ProfitLossEntities` (many types)
**Report periods:** today, weekly, 15 days, monthly, quarterly, half-yearly, yearly, custom

**Use cases:** 9 (GetPLSummary, vehicle/trip/multi cost/fleet/consolidated)

**Special:** `VehiclePLReportGenerator` for PDF export, `VehiclePLDateCalculator` object, `ReportEnums.kt` for periods/charts/export/sorting.

---

### screen-finance
**Screens:** VehicleFinanceList, VehicleFinanceDetail, AddPurchaseInfo, EmiPaymentHistory

**Domain entities:** `VehiclePurchase`, `LoanPayment`

**Special:** Single shared `VehicleFinanceViewModel` drives all 4 screens (uses `rememberSharedViewModel` pattern). No use case layer.

## Presentation-Only Feature Modules

### screen-user
**Screens:** Login, SignUp, ForgotPassword, Profile, ChangePassword
**Data layer lives in:** `ijs-network-lib` (UserRepository, UserRemoteDataSource, etc.)

### screen-onboarding
**Screens:** Onboarding flow (single screen)

### screen-dashboard
**Screens:** Dashboard (fleet overview, cost overview, financial summary, alerts, quick actions)
**Data layer lives in:** `ijs-network-lib` (DashboardRepository)
**Special:** Widest navigation surface — facade takes 18+ navigation lambdas.
**Components:** AlertsSection, CostOverviewSection, FleetOverviewSection, NavigationDrawerContent, TripsSection, VehicleDriverSections

### screen-alerts
**Screens:** AlertsList (document/license expiry, maintenance due)

### screen-map
**Screens:** MapsScreen (real-time vehicle tracking via MQTT — currently mock data)
