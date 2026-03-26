# 02 — Cross-Feature Dependency Matrix

## Purpose

Maps every ViewModel's cross-feature dependency and documents the resolution strategy. This ensures no feature module ever imports another feature module directly.

---

## Dependency Classification

| Source | Classification | Action |
|--------|---------------|--------|
| Own module's repository/use cases | ✅ Internal — no action | Direct usage |
| `ijs-network-lib` (`CostsRepository`, `CostTypesRepository`, `UserLocalDataSource`, `UserRepository`, `GooglePlacesService`) | ✅ Allowed infrastructure | Direct usage via `ijs-network-lib` dependency |
| `ijs-core-lib` (`MviViewModel`, `StatusConstants`, `ValidationUtils`, DTOs) | ✅ Allowed foundation | Direct usage via transitive dependency |
| Another feature module's repository/entity/use case | ❌ FORBIDDEN | Must use `ExternalDeps` callback + shared contracts from `ijs-core-lib` |

---

## Per-ViewModel Cross-Feature Dependencies

### screen-vehicle

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehicleDetailVM` | `GetDriversUseCase` | screen-driver | ❌ Cross-feature | `VehicleExternalDeps.getDrivers() → Result<List<SelectableDriver>>` |
| `VehicleDetailVM` | `TeamRepository` | screen-team | ❌ Cross-feature | `VehicleExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `VehicleDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `VehicleDetailVM` | `CostTypesRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `AddVehicleVM` | `TeamRepository` | screen-team | ❌ Cross-feature | `VehicleExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `AddVehicleVM` | `CreateVehicleWithDocumentsUseCase` | screen-vehicle | ✅ Internal | Direct access |
| `MaintenanceCostEntryVM` | `VehicleRepository` | screen-vehicle | ✅ Internal | Direct access |
| `MaintenanceCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `MaintenanceCostEntryVM` | `CostTypesRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `VehiclesVM` | `GetVehiclesUseCase`, `DeleteVehicleUseCase` | screen-vehicle | ✅ Internal | Direct access |

### screen-driver

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `DriverDetailVM` | `TeamRepository` | screen-team | ❌ Cross-feature | `DriverExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `DriverDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `DriverDetailVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `CreateDriverVM` | `TeamRepository` | screen-team | ❌ Cross-feature | `DriverExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `DriverCostEntryVM` | `DriverRepository` | screen-driver | ✅ Internal | Direct access |
| `DriverCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `DriversVM` | all own | screen-driver | ✅ Internal | Direct access |

### screen-trip

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `CreateTripVM` | `GetAvailableVehiclesUseCase` | screen-vehicle | ❌ Cross-feature | `TripExternalDeps.getAvailableVehicles() → Result<List<SelectableVehicle>>` |
| `CreateTripVM` | `GetAvailableDriversUseCase` | screen-driver | ❌ Cross-feature | `TripExternalDeps.getAvailableDrivers() → Result<List<SelectableDriver>>` |
| `CreateTripVM` | `CustomerRepository` | screen-customer | ❌ Cross-feature | `TripExternalDeps.getCustomers() → Result<List<SelectableCustomer>>` |
| `CreateTripVM` | `GooglePlacesService` | ijs-network-lib | ✅ Allowed | `TripExternalDeps.searchPlaces()` (wrapped for decoupling) |
| `CreateTripVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripDetailVM` | `GetVehiclesUseCase` | screen-vehicle | ❌ Cross-feature | `TripExternalDeps.getAvailableVehicles()` |
| `TripDetailVM` | `GetDriversUseCase` | screen-driver | ❌ Cross-feature | `TripExternalDeps.getAvailableDrivers()` |
| `TripDetailVM` | `CustomerRepository` | screen-customer | ❌ Cross-feature | `TripExternalDeps.getCustomers()` |
| `TripDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripDetailVM` | `GooglePlacesService` | ijs-network-lib | ✅ Allowed | Wrapped via `TripExternalDeps.searchPlaces()` |
| `TripCostEntryVM` | `TripRepository` | screen-trip | ✅ Internal | Direct access |
| `TripCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripsVM` | all own | screen-trip | ✅ Internal | Direct access |

### screen-customer

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `CustomersListVM` | `CustomerRepository` | screen-customer | ✅ Internal | Direct access |
| `CustomerDetailVM` | `CustomerRepository` | screen-customer | ✅ Internal | Direct access |
| `CreateCustomerVM` | `CreateCustomerUseCase` | screen-customer | ✅ Internal | Direct access |

**Zero cross-feature dependencies — safest pilot.**

### screen-payment

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `AddPaymentVM` | `TripRepository` | screen-trip | ❌ Cross-feature | `PaymentExternalDeps.getTrips() → Result<List<SelectableTrip>>` |
| `PaymentDetailVM` | `UserRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `PaymentsVM` | `TripPaymentRepository` | screen-payment | ✅ Internal | Direct access |

### screen-finance

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehicleFinanceVM` | `VehicleRepository` | screen-vehicle | ❌ Cross-feature | `FinanceExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `VehicleFinanceVM` | `VehicleFinanceRepository` | screen-finance | ✅ Internal | Direct access |

### screen-report

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehiclePLVM` | `VehicleRepository` | screen-vehicle | ❌ Cross-feature | `ReportsExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `TripPLVM` | `TripRepository` | screen-trip | ❌ Cross-feature | `ReportsExternalDeps.getTrips() → Result<List<SelectableTrip>>` |
| `ConsolidatedPLVM` | `VehicleRepository` | screen-vehicle | ❌ Cross-feature | `ReportsExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `ReportsVM` | `ReportsRepository` | screen-report | ✅ Internal | Direct access |
| `CostAnalysisVM` | `ReportsRepository` | screen-report | ✅ Internal | Direct access |

### screen-team

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `TeamListVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `CreateTeamMemberVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `TeamMemberDetailVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |

**Zero cross-feature dependencies.**

---

## ExternalDeps Interface Summary

| Feature Module | Interface | Methods |
|---------------|-----------|---------|
| `screen-vehicle` | `VehicleExternalDeps` | `getDrivers(): Result<List<SelectableDriver>>`, `getCaretakers(): Result<List<CaretakerInfo>>` |
| `screen-driver` | `DriverExternalDeps` | `getCaretakers(): Result<List<CaretakerInfo>>` |
| `screen-trip` | `TripExternalDeps` | `getAvailableVehicles()`, `getAvailableDrivers()`, `getCustomers()`, `searchPlaces()`, `getUserRole()` |
| `screen-payment` | `PaymentExternalDeps` | `getTrips(): Result<List<SelectableTrip>>`, `getUserRole(): String` |
| `screen-finance` | `FinanceExternalDeps` | `getVehicles(): Result<List<SelectableVehicle>>` |
| `screen-report` | `ReportsExternalDeps` | `getVehicles(): Result<List<SelectableVehicle>>`, `getTrips(): Result<List<SelectableTrip>>` |
| `screen-customer` | None needed | Self-contained |
| `screen-team` | None needed | `UserLocalDataSource` from network-lib is sufficient |

---

## Shared Data Contracts (in ijs-core-lib)

All `ExternalDeps` interfaces return these lightweight types — not feature-specific domain entities:

```kotlin
// ijs-core-lib/core/model/shared/
data class SelectableVehicle(val id: String, val registrationNumber: String, val displayName: String, val status: String)
data class SelectableDriver(val id: String, val name: String, val mobile: String, val status: String)
data class SelectableCustomer(val id: String, val companyName: String, val personName: String, val primaryContact: String)
data class SelectableTrip(val id: String, val routeLabel: String, val vehicleInfo: String, val status: String)
data class CaretakerInfo(val id: String, val name: String, val role: String, val mobile: String)
```

