# screen-trip — IndusJS Fleet

## Purpose

Trip feature: planning, route management, cargo, scheduling, trip costs, state machine.

## Package: `com.ijs.trip`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| TripsScreen | `Trips` | Filter by status (planned, on_route, completed, etc.) |
| CreateTripScreen | `CreateTrip` | Vehicle + driver + route (Places API) + schedule + cargo |
| TripDetailScreen | `TripDetail(id)` | Route, cargo, schedule, costs, payments |
| TripCostEntryScreen | `TripCostEntry(tripId?, vehicleId?)` | Record trip expense |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/TripFeatureFacade.kt` | Facade — 4 entry points |
| `presentation/TripsContract.kt` | List State/Intent/Effect |
| `presentation/TripsViewModel.kt` | List ViewModel |
| `presentation/TripsScreen.kt` | List UI |
| `presentation/detail/TripDetail*.kt` | Detail Contract/VM/Screen |
| `presentation/create/CreateTrip*.kt` | Create Contract/VM/Screen |
| `presentation/cost/TripCostEntry*.kt` | Cost entry Contract/VM/Screen |
| `data/datasource/TripRemoteDataSourceImpl.kt` | API calls |
| `data/mapper/TripMapper.kt` | DTO → Entity |
| `data/mapper/TripStopMapper.kt` | Stop DTOs → Entities |
| `data/repository/TripRepositoryImpl.kt` | Repository impl |
| `domain/entity/Trip.kt` | Domain entity |
| `domain/repository/TripRepository.kt` | Repository interface |

## Trip States

```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

## APIs

- `GET /trips` — List all
- `GET /trips/{id}` — Detail
- `POST /trips` — Create (ISO 8601 dates!)
- `PUT /trips/{id}` — Update
- `PATCH /trips/{id}/cancel` — Cancel
- `POST /trips/{id}/costs` — Add cost

## Trip Cost Types

`fuel, toll, driver_allowance, parking, loading_charges, unloading_charges, chalan, permit, insurance, other`

## Date Format (CRITICAL)

Trip create/update uses **ISO 8601**: `FleetDateTime.toIso8601(date, time)`
Trip costs use **DD-MM-YYYY** + **HH:MM**: send as-is

## Module Path

`screen-trip/src/commonMain/kotlin/com/ijs/trip/`

## Depends On: `ijs-network-lib`

