# screen-driver — IndusJS Fleet

## Purpose

Driver feature: CRUD, license tracking, driver cost management, status transitions.

## Package: `com.ijs.driver`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| DriversScreen | `Drivers` | List with search, filter by status |
| CreateDriverScreen | `CreateDriver` | Register with license info |
| DriverDetailScreen | `DriverDetail(id)` | Info, license, trip history, costs |
| DriverCostEntryScreen | `DriverCostEntry(driverId?)` | Record driver cost |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/DriverFeatureFacade.kt` | Facade — 4 entry points |
| `presentation/DriversContract.kt` | List State/Intent/Effect |
| `presentation/DriversViewModel.kt` | List ViewModel |
| `presentation/DriversScreen.kt` | List UI |
| `presentation/detail/DriverDetail*.kt` | Detail Contract/VM/Screen |
| `presentation/create/CreateDriver*.kt` | Create Contract/VM/Screen |
| `presentation/cost/DriverCostEntry*.kt` | Cost entry Contract/VM/Screen |
| `data/datasource/DriverRemoteDataSourceImpl.kt` | API calls |
| `data/mapper/DriverMapper.kt` | DTO → Entity |
| `data/repository/DriverRepositoryImpl.kt` | Repository impl |
| `domain/entity/Driver.kt` | Domain entity |
| `domain/repository/DriverRepository.kt` | Repository interface |
| `domain/usecase/DriverUseCases.kt` | Use cases |

## Driver States

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```

## APIs

- `GET /drivers` — List all
- `GET /drivers/{id}` — Detail
- `POST /drivers` — Create
- `PUT /drivers/{id}` — Update
- `PATCH /drivers/{id}/toggle-active` — Toggle status

## Driver Cost Types

`salary, advance, bonus, penalty`

## Module Path

`screen-driver/src/commonMain/kotlin/com/ijs/driver/`

## Depends On: `ijs-network-lib`

