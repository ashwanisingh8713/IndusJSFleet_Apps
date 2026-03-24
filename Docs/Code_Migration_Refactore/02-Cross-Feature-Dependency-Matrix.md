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

### feat-vehicle

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehicleDetailVM` | `GetDriversUseCase` | feat-driver | ❌ Cross-feature | `VehicleExternalDeps.getDrivers() → Result<List<SelectableDriver>>` |
| `VehicleDetailVM` | `TeamRepository` | feat-team | ❌ Cross-feature | `VehicleExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `VehicleDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `VehicleDetailVM` | `CostTypesRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `AddVehicleVM` | `TeamRepository` | feat-team | ❌ Cross-feature | `VehicleExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `AddVehicleVM` | `CreateVehicleWithDocumentsUseCase` | feat-vehicle | ✅ Internal | Direct access |
| `MaintenanceCostEntryVM` | `VehicleRepository` | feat-vehicle | ✅ Internal | Direct access |
| `MaintenanceCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `MaintenanceCostEntryVM` | `CostTypesRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `VehiclesVM` | `GetVehiclesUseCase`, `DeleteVehicleUseCase` | feat-vehicle | ✅ Internal | Direct access |

### feat-driver

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `DriverDetailVM` | `TeamRepository` | feat-team | ❌ Cross-feature | `DriverExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `DriverDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `DriverDetailVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `CreateDriverVM` | `TeamRepository` | feat-team | ❌ Cross-feature | `DriverExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `DriverCostEntryVM` | `DriverRepository` | feat-driver | ✅ Internal | Direct access |
| `DriverCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `DriversVM` | all own | feat-driver | ✅ Internal | Direct access |

### feat-trip

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `CreateTripVM` | `GetAvailableVehiclesUseCase` | feat-vehicle | ❌ Cross-feature | `TripExternalDeps.getAvailableVehicles() → Result<List<SelectableVehicle>>` |
| `CreateTripVM` | `GetAvailableDriversUseCase` | feat-driver | ❌ Cross-feature | `TripExternalDeps.getAvailableDrivers() → Result<List<SelectableDriver>>` |
| `CreateTripVM` | `CustomerRepository` | feat-customer | ❌ Cross-feature | `TripExternalDeps.getCustomers() → Result<List<SelectableCustomer>>` |
| `CreateTripVM` | `GooglePlacesService` | ijs-network-lib | ✅ Allowed | `TripExternalDeps.searchPlaces()` (wrapped for decoupling) |
| `CreateTripVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripDetailVM` | `GetVehiclesUseCase` | feat-vehicle | ❌ Cross-feature | `TripExternalDeps.getAvailableVehicles()` |
| `TripDetailVM` | `GetDriversUseCase` | feat-driver | ❌ Cross-feature | `TripExternalDeps.getAvailableDrivers()` |
| `TripDetailVM` | `CustomerRepository` | feat-customer | ❌ Cross-feature | `TripExternalDeps.getCustomers()` |
| `TripDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripDetailVM` | `GooglePlacesService` | ijs-network-lib | ✅ Allowed | Wrapped via `TripExternalDeps.searchPlaces()` |
| `TripCostEntryVM` | `TripRepository` | feat-trip | ✅ Internal | Direct access |
| `TripCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripsVM` | all own | feat-trip | ✅ Internal | Direct access |

### feat-customer

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `CustomersListVM` | `CustomerRepository` | feat-customer | ✅ Internal | Direct access |
| `CustomerDetailVM` | `CustomerRepository` | feat-customer | ✅ Internal | Direct access |
| `CreateCustomerVM` | `CreateCustomerUseCase` | feat-customer | ✅ Internal | Direct access |

**Zero cross-feature dependencies — safest pilot.**

### feat-payment

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `AddPaymentVM` | `TripRepository` | feat-trip | ❌ Cross-feature | `PaymentExternalDeps.getTrips() → Result<List<SelectableTrip>>` |
| `PaymentDetailVM` | `UserRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `PaymentsVM` | `TripPaymentRepository` | feat-payment | ✅ Internal | Direct access |

### feat-finance

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehicleFinanceVM` | `VehicleRepository` | feat-vehicle | ❌ Cross-feature | `FinanceExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `VehicleFinanceVM` | `VehicleFinanceRepository` | feat-finance | ✅ Internal | Direct access |

### feat-report

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehiclePLVM` | `VehicleRepository` | feat-vehicle | ❌ Cross-feature | `ReportsExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `TripPLVM` | `TripRepository` | feat-trip | ❌ Cross-feature | `ReportsExternalDeps.getTrips() → Result<List<SelectableTrip>>` |
| `ConsolidatedPLVM` | `VehicleRepository` | feat-vehicle | ❌ Cross-feature | `ReportsExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `ReportsVM` | `ReportsRepository` | feat-report | ✅ Internal | Direct access |
| `CostAnalysisVM` | `ReportsRepository` | feat-report | ✅ Internal | Direct access |

### feat-team

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
| `feat-vehicle` | `VehicleExternalDeps` | `getDrivers(): Result<List<SelectableDriver>>`, `getCaretakers(): Result<List<CaretakerInfo>>` |
| `feat-driver` | `DriverExternalDeps` | `getCaretakers(): Result<List<CaretakerInfo>>` |
| `feat-trip` | `TripExternalDeps` | `getAvailableVehicles()`, `getAvailableDrivers()`, `getCustomers()`, `searchPlaces()`, `getUserRole()` |
| `feat-payment` | `PaymentExternalDeps` | `getTrips(): Result<List<SelectableTrip>>`, `getUserRole(): String` |
| `feat-finance` | `FinanceExternalDeps` | `getVehicles(): Result<List<SelectableVehicle>>` |
| `feat-report` | `ReportsExternalDeps` | `getVehicles(): Result<List<SelectableVehicle>>`, `getTrips(): Result<List<SelectableTrip>>` |
| `feat-customer` | None needed | Self-contained |
| `feat-team` | None needed | `UserLocalDataSource` from network-lib is sufficient |

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

