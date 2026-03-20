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

### ijs-vehicle-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehicleDetailVM` | `GetDriversUseCase` | ijs-driver-lib | ❌ Cross-feature | `VehicleExternalDeps.getDrivers() → Result<List<SelectableDriver>>` |
| `VehicleDetailVM` | `TeamRepository` | ijs-team-lib | ❌ Cross-feature | `VehicleExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `VehicleDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `VehicleDetailVM` | `CostTypesRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `AddVehicleVM` | `TeamRepository` | ijs-team-lib | ❌ Cross-feature | `VehicleExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `AddVehicleVM` | `CreateVehicleWithDocumentsUseCase` | ijs-vehicle-lib | ✅ Internal | Direct access |
| `MaintenanceCostEntryVM` | `VehicleRepository` | ijs-vehicle-lib | ✅ Internal | Direct access |
| `MaintenanceCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `MaintenanceCostEntryVM` | `CostTypesRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `VehiclesVM` | `GetVehiclesUseCase`, `DeleteVehicleUseCase` | ijs-vehicle-lib | ✅ Internal | Direct access |

### ijs-driver-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `DriverDetailVM` | `TeamRepository` | ijs-team-lib | ❌ Cross-feature | `DriverExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `DriverDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `DriverDetailVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `CreateDriverVM` | `TeamRepository` | ijs-team-lib | ❌ Cross-feature | `DriverExternalDeps.getCaretakers() → Result<List<CaretakerInfo>>` |
| `DriverCostEntryVM` | `DriverRepository` | ijs-driver-lib | ✅ Internal | Direct access |
| `DriverCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `DriversVM` | all own | ijs-driver-lib | ✅ Internal | Direct access |

### ijs-trip-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `CreateTripVM` | `GetAvailableVehiclesUseCase` | ijs-vehicle-lib | ❌ Cross-feature | `TripExternalDeps.getAvailableVehicles() → Result<List<SelectableVehicle>>` |
| `CreateTripVM` | `GetAvailableDriversUseCase` | ijs-driver-lib | ❌ Cross-feature | `TripExternalDeps.getAvailableDrivers() → Result<List<SelectableDriver>>` |
| `CreateTripVM` | `CustomerRepository` | ijs-customer-lib | ❌ Cross-feature | `TripExternalDeps.getCustomers() → Result<List<SelectableCustomer>>` |
| `CreateTripVM` | `GooglePlacesService` | ijs-network-lib | ✅ Allowed | `TripExternalDeps.searchPlaces()` (wrapped for decoupling) |
| `CreateTripVM` | `UserLocalDataSource` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripDetailVM` | `GetVehiclesUseCase` | ijs-vehicle-lib | ❌ Cross-feature | `TripExternalDeps.getAvailableVehicles()` |
| `TripDetailVM` | `GetDriversUseCase` | ijs-driver-lib | ❌ Cross-feature | `TripExternalDeps.getAvailableDrivers()` |
| `TripDetailVM` | `CustomerRepository` | ijs-customer-lib | ❌ Cross-feature | `TripExternalDeps.getCustomers()` |
| `TripDetailVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripDetailVM` | `GooglePlacesService` | ijs-network-lib | ✅ Allowed | Wrapped via `TripExternalDeps.searchPlaces()` |
| `TripCostEntryVM` | `TripRepository` | ijs-trip-lib | ✅ Internal | Direct access |
| `TripCostEntryVM` | `CostsRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `TripsVM` | all own | ijs-trip-lib | ✅ Internal | Direct access |

### ijs-customer-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `CustomersListVM` | `CustomerRepository` | ijs-customer-lib | ✅ Internal | Direct access |
| `CustomerDetailVM` | `CustomerRepository` | ijs-customer-lib | ✅ Internal | Direct access |
| `CreateCustomerVM` | `CreateCustomerUseCase` | ijs-customer-lib | ✅ Internal | Direct access |

**Zero cross-feature dependencies — safest pilot.**

### ijs-payment-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `AddPaymentVM` | `TripRepository` | ijs-trip-lib | ❌ Cross-feature | `PaymentExternalDeps.getTrips() → Result<List<SelectableTrip>>` |
| `PaymentDetailVM` | `UserRepository` | ijs-network-lib | ✅ Allowed | Direct access |
| `PaymentsVM` | `TripPaymentRepository` | ijs-payment-lib | ✅ Internal | Direct access |

### ijs-finance-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehicleFinanceVM` | `VehicleRepository` | ijs-vehicle-lib | ❌ Cross-feature | `FinanceExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `VehicleFinanceVM` | `VehicleFinanceRepository` | ijs-finance-lib | ✅ Internal | Direct access |

### ijs-reports-lib

| ViewModel | Dependency | Source Module | Type | Resolution |
|-----------|-----------|---------------|------|------------|
| `VehiclePLVM` | `VehicleRepository` | ijs-vehicle-lib | ❌ Cross-feature | `ReportsExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `TripPLVM` | `TripRepository` | ijs-trip-lib | ❌ Cross-feature | `ReportsExternalDeps.getTrips() → Result<List<SelectableTrip>>` |
| `ConsolidatedPLVM` | `VehicleRepository` | ijs-vehicle-lib | ❌ Cross-feature | `ReportsExternalDeps.getVehicles() → Result<List<SelectableVehicle>>` |
| `ReportsVM` | `ReportsRepository` | ijs-reports-lib | ✅ Internal | Direct access |
| `CostAnalysisVM` | `ReportsRepository` | ijs-reports-lib | ✅ Internal | Direct access |

### ijs-team-lib

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
| `ijs-vehicle-lib` | `VehicleExternalDeps` | `getDrivers(): Result<List<SelectableDriver>>`, `getCaretakers(): Result<List<CaretakerInfo>>` |
| `ijs-driver-lib` | `DriverExternalDeps` | `getCaretakers(): Result<List<CaretakerInfo>>` |
| `ijs-trip-lib` | `TripExternalDeps` | `getAvailableVehicles()`, `getAvailableDrivers()`, `getCustomers()`, `searchPlaces()`, `getUserRole()` |
| `ijs-payment-lib` | `PaymentExternalDeps` | `getTrips(): Result<List<SelectableTrip>>`, `getUserRole(): String` |
| `ijs-finance-lib` | `FinanceExternalDeps` | `getVehicles(): Result<List<SelectableVehicle>>` |
| `ijs-reports-lib` | `ReportsExternalDeps` | `getVehicles(): Result<List<SelectableVehicle>>`, `getTrips(): Result<List<SelectableTrip>>` |
| `ijs-customer-lib` | None needed | Self-contained |
| `ijs-team-lib` | None needed | `UserLocalDataSource` from network-lib is sufficient |

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

