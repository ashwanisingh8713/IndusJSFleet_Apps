# ijs-core-lib — IndusJS Fleet

## Purpose

Shared foundation for ALL feature modules. Aggregates `ijs-error-lib`, `ijs-dispatcher-lib`,
`ijs-datetime-utils` via `api()`. Every feature module gets these transitively.

## Package: `com.indusjs.fleet.core`

## Key Components

| Component | Path | Purpose |
|-----------|------|---------|
| `MviViewModel` | `core/mvi/MviViewModel.kt` | Base ViewModel (93 lines) |
| `MviContract` | `core/mvi/MviContract.kt` | UiState, UiIntent, UiEffect (21 lines) |
| `MviExtensions` | `core/mvi/MviExtensions.kt` | HandleEffects composable |
| `FleetErrorContext` | `core/error/FleetErrorContext.kt` | Screen-specific error contexts |
| `StatusConstants` | `core/constants/StatusConstants.kt` | Vehicle/Driver/Trip states |
| `ValidationUtils` | `core/util/ValidationUtils.kt` | Date, time, email, mobile validation |
| `FormatUtils` | `core/util/FormatUtils.kt` | Currency, number formatting |
| `CostTypeUtils` | `core/util/CostTypeUtils.kt` | Cost type labels and mappings |
| `FleetLogger` | `core/logger/FleetLogger.kt` | Logger interface |

## Shared DTOs (used across features)

| DTO | Purpose |
|-----|---------|
| `CostModels` | Shared cost request/response DTOs |
| `DataModels` | Common data structures |
| `DriverCostModels` | Driver cost DTOs |
| `HistoryDto` | Trip/cost history |
| `StateHistoryDto` | Entity state transitions |

## Base Interfaces

| Interface | Purpose |
|-----------|---------|
| `DataSource` | Base for remote/local data sources |
| `Mapper` | DTO ↔ Entity conversion |
| `Repository` | Base repository interface |
| `UseCase` | Single-action business logic |
| `Entity` | Domain entity marker |

## Module Path

`ijs-core-lib/src/commonMain/kotlin/com/indusjs/fleet/core/`

## Depends On: `ijs-error-lib`, `ijs-dispatcher-lib`, `ijs-datetime-utils` (all via `api()`)
## Depended On By: `ijs-network-lib`, all `screen-*` modules (transitively)

## Validation Examples

```kotlin
ValidationUtils.validateDate("04-01-2026")  // → ValidationResult.Success/Error
ValidationUtils.validateTime("14:30", required = true)
ValidationUtils.isValidEmail("user@email.com")
ValidationUtils.isValidMobile("9876543210")
```

