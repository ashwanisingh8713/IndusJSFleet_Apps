# ijs-core-lib — AGENTS.md

## Purpose

Shared foundation module for IndusJS Fleet. Contains **MVI base classes, status constants, utilities, shared DTOs, marker interfaces, and user domain entities** used across all feature modules. Every `screen-*` and `ijs-*` module depends on this (transitively through `ijs-network-lib`).

**Package:** `com.indusjs.fleet`
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS (browser), WasmJS (browser)

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/fleet/
├── core/
│   ├── constants/
│   │   └── StatusConstants.kt             # Vehicle/Driver/Trip state machines, valid transitions, color schemes
│   ├── error/
│   │   └── FleetErrorContext.kt           # Screen-specific error messages (implements ErrorContext from ijs-error-lib)
│   ├── logger/
│   │   └── FleetLogger.kt                # FleetLogger interface (impl in ijs-logger-lib)
│   ├── mvi/
│   │   ├── MviContract.kt                # UiState, UiIntent, UiEffect marker interfaces
│   │   ├── MviViewModel.kt               # Base ViewModel: updateState, sendEffect, handleIntent
│   │   └── MviExtensions.kt              # collectState, HandleEffects composable extensions
│   ├── model/
│   │   └── shared/
│   │       ├── CaretakerInfo.kt           # Shared caretaker DTO (used across trip/vehicle)
│   │       ├── SelectableCustomer.kt      # Lightweight customer for dropdowns/pickers
│   │       ├── SelectableDriver.kt        # Lightweight driver for dropdowns/pickers
│   │       ├── SelectableTrip.kt          # Lightweight trip for dropdowns/pickers
│   │       └── SelectableVehicle.kt       # Lightweight vehicle for dropdowns/pickers
│   └── util/
│       ├── CostTypeUtils.kt              # Cost type constants + display helpers
│       ├── FormatUtils.kt                # Currency formatting, number formatting
│       ├── PermissionUtils.kt            # Role-based feature visibility checks
│       ├── PhoneCallUtil.kt              # expect/actual: platform phone call intent
│       ├── TimeUtils.kt                  # expect/actual: platform time formatting
│       └── ValidationUtils.kt            # Email, phone, GST, password validation
├── data/
│   ├── datasource/
│   │   └── DataSource.kt                 # LocalDataSource, RemoteDataSource marker interfaces
│   ├── mapper/
│   │   └── Mapper.kt                     # Mapper<D,E> interface + list mapping extension
│   ├── model/
│   │   ├── DataModels.kt                 # Dto, DbEntity marker interfaces
│   │   ├── costs/
│   │   │   ├── CostApiResponses.kt       # Shared cost API response wrappers
│   │   │   ├── CostBreakdownItemDto.kt   # Cost breakdown DTO (used in reports + detail screens)
│   │   │   ├── CostEntityModels.kt       # Shared cost domain entities
│   │   │   ├── CostModels.kt             # Shared cost DTOs
│   │   │   └── CostTypeSelection.kt      # Cost type selection model for UI chips
│   │   ├── driver/
│   │   │   └── DriverCostModels.kt       # Shared driver cost DTOs
│   │   ├── history/
│   │   │   └── HistoryDto.kt             # Entity history/audit log DTO
│   │   └── state/
│   │       └── StateHistoryDto.kt        # State transition history DTO
├── domain/
│   ├── entity/
│   │   ├── Entity.kt                     # Entity marker interface
│   │   └── user/
│   │       └── User.kt                   # UserRole enum, User, UserProfile, AuthResult
│   ├── repository/
│   │   └── Repository.kt                 # Repository marker interface
│   └── usecase/
│       └── UseCase.kt                    # UseCase, UseCaseWithParams, SuspendUseCase, SuspendUseCaseWithParams
```

---

## Key Types

### MVI Base (`core.mvi`)

```kotlin
interface UiState
interface UiIntent
interface UiEffect

abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(initialState: S) : ViewModel() {
    val state: StateFlow<S>
    val effect: SharedFlow<E>
    fun sendIntent(intent: I)
    protected abstract fun handleIntent(intent: I)
    protected fun updateState(reducer: S.() -> S)
    protected fun sendEffect(effect: E)
}
```

Every ViewModel in the app extends `MviViewModel` with a screen-specific `Contract` (State + Intent + Effect).

### StatusConstants (`core.constants`)

Centralized state machines for Vehicle, Driver, and Trip. Provides:
- Valid state transitions per entity type
- State display names
- State color schemes (for status badges)

### Shared Selectables (`core.model.shared`)

Lightweight entity representations for dropdowns/pickers in forms (e.g., vehicle picker in CreateTrip). These are `@Serializable` so they can be passed through Navigation 3 routes.

### PermissionUtils (`core.util`)

Role-based feature checks:
```kotlin
PermissionUtils.canViewFinancials(role: UserRole): Boolean
PermissionUtils.canManageTeam(role: UserRole): Boolean
PermissionUtils.canCreateTrips(role: UserRole): Boolean
```

---

## Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-error-lib` — `Result<T>`, exception hierarchy |
| api | `:ijs-dispatcher-lib` — `DispatcherProvider` |
| api | `:ijs-datetime-utils` — `FleetDateTime` |
| implementation | Compose Runtime, UI, Material3, Lifecycle ViewModel/Runtime |

All three `api` dependencies are transitively exposed to all consumers.

---

## Platform Source Sets

| Platform | Files |
|----------|-------|
| androidMain | `TimeUtils.android.kt`, `PhoneCallUtil.android.kt` |
| iosMain | `TimeUtils.ios.kt`, `PhoneCallUtil.ios.kt` |
| jsMain | `TimeUtils.js.kt`, `PhoneCallUtil.js.kt` |
| wasmJsMain | `TimeUtils.wasmJs.kt`, `PhoneCallUtil.wasmJs.kt` |

---

## Consumers

Every module in the project depends on `ijs-core-lib` (directly or transitively via `ijs-network-lib`). It is the **innermost layer** of the dependency graph.
