# ijs-core-lib — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.fleet.core`, `com.indusjs.fleet.domain`, `com.indusjs.fleet.data`

---

## 1. Purpose

Shared foundation module providing **base architectural contracts, MVI pattern, status constants, utilities, and user domain entities** used across all feature modules in IndusJS Fleet.

## 2. Package Structure

```
src/commonMain/kotlin/com/indusjs/fleet/
├── core/
│   ├── constants/
│   │   └── StatusConstants.kt     # Vehicle/Driver/Trip state machines, transitions, colors
│   ├── error/
│   │   └── FleetErrorContext.kt   # Screen-specific error context enum
│   ├── mvi/
│   │   ├── MviContract.kt        # UiState, UiIntent, UiEffect marker interfaces
│   │   ├── MviViewModel.kt       # Base ViewModel with state/intent/effect channels
│   │   └── MviExtensions.kt      # HandleEffects, collectState, intentHandler composables
│   └── util/
│       ├── CostTypeUtils.kt      # Cost type display names, icons, colors
│       ├── FormatUtils.kt        # Currency, percentage formatting
│       ├── PermissionUtils.kt    # Role-based access control checks
│       ├── PhoneCallUtil.kt      # expect/actual phone call launcher
│       ├── TimeUtils.kt          # expect/actual time utilities
│       └── ValidationUtils.kt    # Date/time/email/mobile/amount validation
├── data/
│   ├── datasource/
│   │   └── DataSource.kt         # LocalDataSource, RemoteDataSource marker interfaces
│   ├── mapper/
│   │   └── Mapper.kt             # Mapper<D,E> interface + list extensions
│   └── model/
│       └── DataModels.kt         # Dto, DbEntity marker interfaces
└── domain/
    ├── entity/
    │   ├── Entity.kt             # Entity marker interface
    │   └── user/
    │       └── User.kt           # UserRole enum, User, UserProfile, AuthResult, etc.
    ├── repository/
    │   └── Repository.kt         # Repository marker interface
    └── usecase/
        └── UseCase.kt            # UseCase, UseCaseWithParams, SuspendUseCase variants
```

## 3. Dependencies

| Dependency | Type | Purpose |
|-----------|------|---------|
| `ijs-error-lib` | `api` | `Result<T>`, exception hierarchy |
| `ijs-dispatcher-lib` | `api` | `DispatcherProvider` |
| `ijs-datetime-utils` | `api` | `FleetDateTime` operations |
| Compose Runtime/UI/Material3 | `implementation` | For MVI extensions and CostTypeUtils |
| Lifecycle ViewModel/Runtime | `implementation` | MviViewModel base class |

## 4. Key Types

### MVI Pattern
- `UiState` — marker interface for screen state
- `UiIntent` — marker interface for user actions
- `UiEffect` — marker interface for one-time side effects
- `MviViewModel<S, I, E>` — base ViewModel managing state/intent/effect flows

### Status Constants
- `VehicleStatus` — `active`, `inactive`, `maintenance`, `on_route`, `damaged`, `decommissioned`
- `DriverStatus` — `active`, `inactive`, `on_route`, `on_leave`, `suspended`, `terminated`
- `TripStatus` — `planned`, `in_progress`, `completed`, `cancelled`, `failed`, `delayed`

### User Domain
- `UserRole` — `OWNER`, `GENERAL_MANAGER`, `MANAGER`, `SUPERVISOR`
- `User` — Core user entity
- `UserProfile` — User with organization stats
- `AuthResult` — User + auth token pair

## 5. Platform Source Sets

| Platform | Files |
|----------|-------|
| `androidMain` | `TimeUtils.android.kt`, `PhoneCallUtil.android.kt` |
| `iosMain` | `TimeUtils.ios.kt`, `PhoneCallUtil.ios.kt` |
| `jsMain` | `TimeUtils.js.kt`, `PhoneCallUtil.js.kt` |
| `wasmJsMain` | `TimeUtils.wasmJs.kt`, `PhoneCallUtil.wasmJs.kt` |

