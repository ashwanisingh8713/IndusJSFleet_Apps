# AGENTS.md - screen-driver

## Purpose

**screen-driver** is a Kotlin Multiplatform library module that encapsulates the **Driver feature's data layer** for the IndusJS Fleet application.

It provides the **implementation** artifacts for driver operations:
- **Data sources:** `DriverRemoteDataSource` (interface + Ktor impl)
- **DTOs:** `DriverDto`, `DriverApiResponse`, `CreateDriverRequest`, `UpdateDriverRequest`, driver cost models
- **Mapper:** `DriverMapper` (DTO ↔ Entity conversion)
- **Repository impl:** `DriverRepositoryImpl`

Domain contracts (entities, repository interface, use cases) remain in `ijs-network-lib` as shared types used across multiple modules.

## Module Dependencies

```
screen-driver → ijs-network-lib → ijs-core-lib → ijs-error-lib
                                                 → ijs-dispatcher-lib
                                                 → ijs-datetime-utils
```

> **Note:** `ijs-network-lib` does NOT depend on `screen-driver` (no circular dependency).
> `DefaultViewModelProvider` in `sharedUI` creates `DriverRepositoryImpl` from this module
> and passes it to `NetworkDataGraph.create()`.

## Package Structure

```
com.indusjs.fleet/
├── data/
│   ├── datasource/driver/    # DriverRemoteDataSource interface + impl
│   ├── mapper/driver/        # DriverMapper (DTO ↔ Entity)
│   ├── model/driver/         # DriverDto, DriverCostModels, request DTOs
│   └── repository/driver/    # DriverRepositoryImpl
```

## Domain Contracts (in ijs-network-lib)

| Type | Location | Description |
|------|----------|-------------|
| `Driver` entity | `ijs-network-lib/.../domain/entity/driver/` | Domain entity + enums |
| `DriverRepository` interface | `ijs-network-lib/.../domain/repository/driver/` | Repository contract |
| `DriverUseCases` | `ijs-network-lib/.../domain/usecase/driver/` | Use case classes |

## Consumers

- **sharedUI** — `DefaultViewModelProvider` constructs `DriverRepositoryImpl` and passes it to `NetworkDataGraph`
- **sharedUI** — presentation layer (ViewModels, Screens) for driver management

## Driver State Machine

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```
