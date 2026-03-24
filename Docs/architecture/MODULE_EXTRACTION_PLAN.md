# IndusJS Fleet — Module Extraction Plan

## Status: Phase 1 COMPLETE ✅

**Date**: March 15, 2026  
**Objective**: Extract shared code from the monolithic `sharedUI` module into focused, reusable library modules following Clean Architecture.

---

## Architecture Before vs After

### Before (Monolithic)
```
androidApp → sharedUI (everything)
webApp    → sharedUI
```

### After (Modular)
```
                            ┌───────────────────────────────────────────────┐
                            │              sharedUI (UI Shell)              │
                            │    Compose UI, Navigation, DI Graphs,        │
                            │    Theme, Presentation (Screens/ViewModels)  │
                            └────────┬──────────┬──────────┬───────────────┘
                                     │          │          │
            ┌────────────────────────┤          │          ├────────────────────┐
            │                        │          │          │                    │
    ┌───────▼───────┐   ┌───────────▼──┐  ┌───▼──────┐  ┌▼──────────────┐    │
    │ ijs-reports-  │   │ ijs-finance- │  │ sharedUI │  │ ijs-pdf-      │    │
    │ lib           │   │ lib          │  │ (remain) │  │ report        │    │
    │ P&L Reports   │   │ Vehicle      │  │ vehicle, │  │               │    │
    │ Cost Analysis │   │ Purchase     │  │ driver,  │  └───────────────┘    │
    │ Consolidated  │   │ Loan/EMI     │  │ trip,    │                       │
    └───────┬───────┘   └──────┬───────┘  │ customer │                       │
            │                  │          │ payment, │                       │
            └─────────┬────────┘          │ team,    │                       │
                      │                   │ dashboard│                       │
              ┌───────▼───────┐           └────┬─────┘                       │
              │ ijs-network-  │                │                             │
              │ lib           ├────────────────┘                             │
              │ HTTP Client   │                                              │
              │ Auth Manager  │                                              │
              │ User Data     │                                              │
              └───────┬───────┘                                              │
                      │                                                      │
              ┌───────▼───────┐                                              │
              │ ijs-core-lib  │◄─────────────────────────────────────────────┘
              │ MVI Pattern   │
              │ Constants     │
              │ Utilities     │
              │ Base Contracts│
              └───┬───┬───┬───┘
                  │   │   │
        ┌─────────┘   │   └──────────┐
        │             │              │
  ┌─────▼─────┐ ┌────▼────┐  ┌──────▼──────┐
  │ ijs-error- │ │ijs-disp-│  │ijs-datetime-│
  │ lib        │ │atcher   │  │ utils       │
  └────────────┘ └─────────┘  └─────────────┘
```

---

## Module Dependency Graph

```
ijs-error-lib         (standalone)
ijs-dispatcher-lib    (standalone)
ijs-datetime-utils    (standalone, depends on kotlinx-datetime)
ijs-datetime-picker   → ijs-datetime-utils
ijs-core-lib          → ijs-error-lib, ijs-dispatcher-lib, ijs-datetime-utils
ijs-network-lib       → ijs-core-lib
feat-report       → ijs-network-lib
feat-finance       → ijs-network-lib
ijs-pdf-report        → ijs-datetime-utils
sharedUI              → ijs-core-lib, ijs-network-lib, feat-report, feat-finance, ijs-datetime-picker, ijs-pdf-report
androidApp            → sharedUI
webApp                → sharedUI
```

---

## Phase 1 — COMPLETED ✅

### Step 1: `ijs-core-lib` — Foundation Module
**Status**: ✅ Created and compiling

Files extracted from `sharedUI`:
- `core/mvi/` — `MviContract`, `MviViewModel`, `MviExtensions`
- `core/constants/StatusConstants.kt` — All entity state machines
- `core/error/FleetErrorContext.kt` — Screen-specific error contexts
- `core/util/` — `FormatUtils`, `ValidationUtils`, `CostTypeUtils`, `TimeUtils` (expect/actual), `PhoneCallUtil` (expect/actual), `PermissionUtils`
- `domain/entity/Entity.kt`, `domain/entity/user/User.kt`
- `domain/repository/Repository.kt`
- `domain/usecase/UseCase.kt` (all 4 variants)
- `data/datasource/DataSource.kt`
- `data/model/DataModels.kt`
- `data/mapper/Mapper.kt`

**Breaking change**: `PermissionUtils.canEditTrip` now accepts `String` instead of `TripStatus` enum to avoid circular dependency.

### Step 2: `ijs-network-lib` — Networking Module
**Status**: ✅ Created and compiling

Files extracted from `sharedUI`:
- `core/network/` — `ApiConfig`, `HttpClientProvider`, `ApiErrorHandler`, `NetworkError`
- `core/auth/` — `AuthenticationManager`, `AuthTokenHelper`
- Full user data layer: `UserLocalDataSource`, `UserRemoteDataSource`, `UserDto`, `UserMapper`, `UserRepositoryImpl`, `UserRepository`

### Step 10: `feat-report` — Reports Module (SEPARATE per user request)
**Status**: ✅ Created and compiling

Files extracted from `sharedUI`:
- `domain/entity/reports/ProfitLossEntities.kt`
- `domain/repository/reports/ReportsRepository.kt`
- `data/model/reports/ProfitLossDto.kt`, `ProfitLossRequest.kt`
- `data/mapper/reports/ProfitLossMapper.kt`
- `data/datasource/reports/ReportsRemoteDataSource.kt`
- `data/repository/reports/ReportsRepositoryImpl.kt`

### Step 11: `feat-finance` — Finance Module (SEPARATE per user request)
**Status**: ✅ Created and compiling

Files extracted from `sharedUI`:
- `domain/entity/finance/VehiclePurchase.kt`, `LoanPayment.kt`
- `domain/repository/finance/VehicleFinanceRepository.kt`
- `data/model/finance/VehicleFinanceDto.kt`
- `data/mapper/finance/VehicleFinanceMapper.kt`
- `data/datasource/finance/VehicleFinanceRemoteDataSource.kt`
- `data/repository/finance/VehicleFinanceRepositoryImpl.kt`

### Verification
- ✅ `ijs-core-lib:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- ✅ `ijs-network-lib:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- ✅ `feat-report:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- ✅ `feat-finance:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- ✅ `sharedUI:compileCommonMainKotlinMetadata` — BUILD SUCCESSFUL
- ✅ `androidApp:assembleDebug` — BUILD SUCCESSFUL
- ✅ `webApp:jsBrowserDevelopmentWebpack` — BUILD SUCCESSFUL

---

## Phase 2 — Future Work (Remaining in sharedUI)

The following feature domains remain in `sharedUI` and can be extracted into separate modules when needed:

| Feature | Potential Module | Status |
|---------|-----------------|--------|
| Vehicle domain (entity, data, datasource, repository) | — | Stays in sharedUI |
| Driver domain | — | Stays in sharedUI |
| Trip domain (includes Google Places integration) | — | Stays in sharedUI |
| Customer domain | — | Stays in sharedUI |
| Payment domain | — | Stays in sharedUI |
| Team management domain | — | Stays in sharedUI |
| Dashboard (presentation + data) | — | Stays in sharedUI |
| All Compose screens + ViewModels | — | Stays in sharedUI |
| Navigation (FleetRoute, FleetNavigation) | — | Stays in sharedUI |
| Theme (Color, Typography, FleetTheme) | — | Stays in sharedUI |
| DI Graphs (RootGraph, feature graphs) | — | Stays in sharedUI |
| Core UI components (FleetTextField, etc.) | — | Stays in sharedUI |

---

## Files NOT Moved

These files remain in `sharedUI` because they depend on feature-specific types:
- `core/util/TripCostToDriverCostMapper.kt` — Depends on `CostEntryRow` (presentation layer)
- `core/init/AppInitializer.kt` — App startup logic

---

## settings.gradle.kts — Final State

```kotlin
include(":sharedUI")
include(":androidApp")
include(":webApp")
include(":locationTracker")
include(":ijs-core-lib")
include(":ijs-network-lib")
include(":feat-report")
include(":feat-finance")
include(":ijs-error-lib")
include(":ijs-dispatcher-lib")
include(":ijs-datetime-picker")
include(":ijs-datetime-utils")
include(":ijs-pdf-report")
```

