# Drivers Feature

> Use this prompt when working on driver management, driver costs, or driver status.

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `DriversScreen` | `Drivers` | List with search + status filter |
| `DriverDetailScreen` | `DriverDetail(driverId)` | Info, license, trip history, costs |
| `CreateDriverScreen` | `CreateDriver` | Registration form with license info |
| `DriverCostEntryScreen` | `DriverCostEntry(driverId?)` | Record salary, advance, bonus, penalty |

## API Endpoints

```
GET    /drivers                     → List all drivers
GET    /drivers/{id}                → Driver details
POST   /drivers                     → Create driver
PUT    /drivers/{id}                → Update driver
DELETE /drivers/{id}                → Delete driver
PATCH  /drivers/{id}/toggle-active  → Toggle active/inactive
PATCH  /drivers/{id}/status         → Update status to any valid state
POST   /drivers/{id}/costs          → Add driver cost
GET    /drivers/{id}/costs          → Get driver costs
POST   /drivers/{id}/assign-caretaker → Assign caretaker
```

## Domain Entity: `Driver`

```kotlin
data class Driver(
    val id: String,
    val name: String,
    val mobile: String,
    val email: String?,
    val status: String,               // DriverState constants
    val licenseNumber: String?,
    val licenseExpiry: String?,       // DD-MM-YYYY
    val licenseType: String?,
    val address: String?,
    val emergencyContact: String?,
    val assignedVehicle: String?,
    val isActive: Boolean
)
```

## Driver States

See `entity-states.prompt.md` — Driver section.

States: `inactive`, `active`, `on_route`, `on_leave`, `suspended`, `terminated`

## Driver Cost Types

See `cost-types.prompt.md` — Driver Costs (DC) section.

Groups: Salary & Wages, Incentives & Bonuses, Deductions, Other.

Deduction costs have `is_deduction = true`.

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/driver/Driver.kt` |
| Repository | `domain/repository/driver/DriverRepository.kt` |
| Use Cases | `domain/usecase/driver/` (8 use cases) |
| DTO | `data/model/driver/` |
| Mapper | `data/mapper/driver/DriverMapper.kt` |
| DataSource | `data/datasource/driver/DriverRemoteDataSourceImpl.kt` |
| Contract | `presentation/drivers/DriversContract.kt` |
| ViewModel | `presentation/drivers/DriversViewModel.kt` |
| Screen | `presentation/drivers/DriversScreen.kt` |
| Detail | `presentation/drivers/detail/` |
| Create | `presentation/drivers/create/` |
| Cost Entry | `presentation/drivers/cost/` |

