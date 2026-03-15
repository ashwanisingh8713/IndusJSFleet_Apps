# Vehicles Feature

> Use this prompt when working on vehicle CRUD, vehicle detail tabs, or vehicle documents.

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `VehiclesScreen` | `Vehicles` | List with search + status filter chips |
| `VehicleDetailScreen` | `VehicleDetail(vehicleId)` | Tabs: Overview, Trips, Documents, Costs |
| `AddVehicleScreen` | `AddVehicle` | Form with document upload |

## API Endpoints

```
GET    /vehicles              → List all vehicles
GET    /vehicles/{id}         → Vehicle details (includes trips, documents, costs)
POST   /vehicles              → Create vehicle
PUT    /vehicles/{id}         → Update vehicle
DELETE /vehicles/{id}         → Delete vehicle
POST   /vehicles/{id}/documents         → Upload document
DELETE /vehicles/{id}/documents/{docId}  → Delete document
POST   /vehicles/{id}/assign-caretaker   → Assign caretaker
```

## Domain Entity: `Vehicle`

```kotlin
data class Vehicle(
    val id: String,
    val registrationNumber: String,
    val vehicleType: String,
    val make: String?,
    val model: String?,
    val year: Int?,
    val status: String,           // VehicleState constants
    val fuelType: String?,
    val chassisNumber: String?,
    val engineNumber: String?,
    val insuranceExpiry: String?,
    val fitnessExpiry: String?,
    val permitExpiry: String?,
    val documents: List<VehicleDocument>,
    val assignedDriver: AssignedDriver?,
    val trips: List<VehicleTrip>?,
    val maintenanceCosts: List<MaintenanceCost>?
)
```

## Vehicle States

See `entity-states.prompt.md` — Vehicle section.

Available for trip assignment: only `"active"` status.

## Detail Screen Tabs

1. **Overview** — registration, make/model, status, assigned driver, insurance/fitness/permit expiry
2. **Trips** — list of trips assigned to this vehicle
3. **Documents** — uploaded documents (RC, insurance, permit, fitness, PUC) with upload/delete
4. **Costs** — maintenance costs with date range filter

## Document Types

```kotlin
enum class DocumentType {
    REGISTRATION_CERTIFICATE,
    INSURANCE,
    FITNESS_CERTIFICATE,
    NATIONAL_PERMIT,
    STATE_PERMIT,
    PUC_CERTIFICATE,
    OTHER
}
```

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/vehicle/Vehicle.kt` |
| Repository | `domain/repository/vehicle/VehicleRepository.kt` |
| Use Cases | `domain/usecase/vehicle/` (6 use cases) |
| DTO | `data/model/vehicle/` |
| Mapper | `data/mapper/vehicle/VehicleMapper.kt` |
| DataSource | `data/datasource/vehicle/VehicleRemoteDataSourceImpl.kt` |
| Contract | `presentation/vehicles/VehiclesContract.kt` |
| ViewModel | `presentation/vehicles/VehiclesViewModel.kt` |
| Screen | `presentation/vehicles/VehiclesScreen.kt` |
| Detail | `presentation/vehicles/detail/` |
| Add | `presentation/vehicles/AddVehicle*.kt` |
| Costs | `presentation/vehicles/costs/` |

