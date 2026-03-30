# screen-vehicle — IndusJS Fleet

## Purpose

Vehicle feature: CRUD, document management, maintenance costs, status transitions.

## Package: `com.ijs.vehicle`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| VehiclesScreen | `Vehicles` | List with search, filter by status |
| AddVehicleScreen | `AddVehicle` | Register with documents (RC, Insurance) |
| VehicleDetailScreen | `VehicleDetail(id)` | Tabs: Overview, Trips, Documents, Costs |
| MaintenanceCostEntryScreen | `MaintenanceCostEntry(vehicleId?)` | Record maintenance expense |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/VehicleFeatureFacade.kt` | Facade — 4 entry points |
| `presentation/VehiclesContract.kt` | List State/Intent/Effect |
| `presentation/VehiclesViewModel.kt` | List ViewModel |
| `presentation/VehiclesScreen.kt` | List UI |
| `presentation/detail/VehicleDetailContract.kt` | Detail State/Intent/Effect |
| `presentation/detail/VehicleDetailViewModel.kt` | Detail ViewModel |
| `presentation/detail/VehicleDetailScreen.kt` | Detail UI with tabs |
| `presentation/costs/MaintenanceCostEntry*.kt` | Cost entry Contract/VM/Screen |
| `data/datasource/vehicle/VehicleRemoteDataSourceImpl.kt` | API calls |
| `data/mapper/vehicle/VehicleMapper.kt` | DTO → Entity |
| `data/model/vehicle/VehicleDto.kt` | API DTOs |
| `data/repository/vehicle/VehicleRepositoryImpl.kt` | Repository impl |
| `domain/entity/vehicle/Vehicle.kt` | Domain entity |
| `domain/repository/vehicle/VehicleRepository.kt` | Repository interface |
| `domain/usecase/vehicle/VehicleUseCases.kt` | Use cases |

## Vehicle States

```
inactive → active → on_route → active
                 → maintenance ←→ damaged → decommissioned
```

## APIs

- `GET /vehicles` — List all
- `GET /vehicles/{id}` — Detail
- `POST /vehicles` — Create
- `PUT /vehicles/{id}` — Update
- `POST /vehicles/{id}/documents` — Upload document
- `GET /vehicles/{id}/maintenance-costs` — Costs list
- `POST /vehicles/{id}/maintenance-costs` — Add cost

## Maintenance Cost Types

`tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other`

## Module Path

`screen-vehicle/src/commonMain/kotlin/com/ijs/vehicle/`

## Depends On: `ijs-network-lib`

