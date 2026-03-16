# Network Library Consolidation Design Document

## Date: March 16, 2026
## Status: Implementation In Progress

---

## 1. Overview

### Problem Statement
The original modularization plan proposed 11 separate feature libraries (`ijs-vehicle-lib`, `ijs-driver-lib`, etc.), leading to excessive module overhead for a single-team project. A simpler approach consolidates **all network-related APIs, data layer, domain layer, and use cases** into `ijs-network-lib`, making it the single **Data + Domain** module.

### Decision
Consolidate all data layer (DTOs, remote data sources, mappers, repository interfaces + implementations) and domain layer (entities, use cases) for **all 10 features** into `ijs-network-lib`. Fold `ijs-reports-lib` and `ijs-finance-lib` into it. Keep Room-based local data source **implementations** in `sharedUI`; move only their **interfaces** to `ijs-network-lib`.

---

## 2. Module Architecture (After Consolidation)

### Module Dependency Graph

```
androidApp ──→ sharedUI
webApp ─────→ sharedUI
iosApp ─────→ sharedUI (via framework)

sharedUI ──→ ijs-network-lib ──→ ijs-core-lib ──→ ijs-error-lib
                                                ──→ ijs-dispatcher-lib
                                                ──→ ijs-datetime-utils
sharedUI ──→ ijs-datetime-picker ──→ ijs-datetime-utils
sharedUI ──→ ijs-pdf-report ──→ ijs-datetime-utils

locationTracker (standalone Android app)
```

### Removed Modules
- `ijs-reports-lib` → folded into `ijs-network-lib`
- `ijs-finance-lib` → folded into `ijs-network-lib`

### Module Count: 11 → 9

---

## 3. `ijs-network-lib` Internal Structure

```
ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/
├── core/
│   ├── auth/
│   │   ├── AuthenticationManager.kt       # Auth event bus (singleton)
│   │   └── AuthTokenHelper.kt            # Token retrieval helper
│   └── network/
│       ├── ApiConfig.kt                   # Base URL + all endpoints
│       ├── ApiErrorHandler.kt             # Error message extraction
│       ├── HttpClientProvider.kt          # Ktor HttpClient factory
│       └── NetworkError.kt               # Network error types
├── data/
│   ├── datasource/
│   │   ├── costs/
│   │   │   ├── CostsLocalDataSource.kt    # ← INTERFACE ONLY (impl in sharedUI)
│   │   │   └── CostsRemoteDataSource.kt   # Full impl
│   │   ├── customer/
│   │   │   ├── CustomerLocalDataSource.kt  # ← INTERFACE ONLY (impl in sharedUI)
│   │   │   └── CustomerRemoteDataSource.kt
│   │   ├── dashboard/
│   │   │   ├── DashboardLocalDataSource.kt # ← INTERFACE ONLY (impl in sharedUI)
│   │   │   └── DashboardRemoteDataSource.kt
│   │   ├── driver/
│   │   │   └── DriverRemoteDataSource.kt
│   │   ├── finance/
│   │   │   └── VehicleFinanceRemoteDataSource.kt
│   │   ├── location/
│   │   │   └── GooglePlacesService.kt
│   │   ├── payment/
│   │   │   └── TripPaymentRemoteDataSource.kt
│   │   ├── reports/
│   │   │   └── ReportsRemoteDataSource.kt
│   │   ├── team/
│   │   │   ├── TeamLocalDataSource.kt      # ← INTERFACE ONLY (impl in sharedUI)
│   │   │   └── TeamRemoteDataSource.kt
│   │   ├── trip/
│   │   │   └── TripRemoteDataSource.kt
│   │   ├── user/
│   │   │   ├── UserLocalDataSource.kt      # Interface + impl (uses Settings, no Room)
│   │   │   └── UserRemoteDataSource.kt
│   │   └── vehicle/
│   │       └── VehicleRemoteDataSource.kt
│   ├── mapper/
│   │   ├── customer/CustomerMapper.kt
│   │   ├── dashboard/
│   │   │   ├── DashboardCacheMapper.kt
│   │   │   └── DashboardMapper.kt
│   │   ├── driver/DriverMapper.kt
│   │   ├── finance/VehicleFinanceMapper.kt
│   │   ├── payment/TripPaymentMapper.kt
│   │   ├── reports/ProfitLossMapper.kt
│   │   ├── team/TeamMapper.kt
│   │   ├── trip/TripMapper.kt
│   │   ├── user/UserMapper.kt
│   │   └── vehicle/VehicleMapper.kt
│   ├── model/
│   │   ├── caretaker/CaretakerDto.kt
│   │   ├── costs/CostModels.kt
│   │   ├── customer/CustomerDto.kt
│   │   ├── dashboard/DashboardModels.kt
│   │   ├── driver/
│   │   │   ├── DriverCostModels.kt
│   │   │   └── DriverDto.kt
│   │   ├── finance/VehicleFinanceDto.kt
│   │   ├── history/HistoryDto.kt
│   │   ├── payment/
│   │   │   ├── TripPaymentDto.kt
│   │   │   └── TripPaymentRequest.kt
│   │   ├── reports/
│   │   │   ├── ProfitLossDto.kt
│   │   │   └── ProfitLossRequest.kt
│   │   ├── state/StateHistoryDto.kt
│   │   ├── team/TeamDto.kt
│   │   ├── trip/TripDto.kt
│   │   ├── user/UserDto.kt
│   │   └── vehicle/VehicleDto.kt
│   └── repository/
│       ├── costs/
│       │   ├── CostsRepositoryImpl.kt
│       │   └── CostTypesRepositoryImpl.kt
│       ├── customer/CustomerRepositoryImpl.kt
│       ├── dashboard/DashboardRepositoryImpl.kt
│       ├── driver/DriverRepositoryImpl.kt
│       ├── finance/VehicleFinanceRepositoryImpl.kt
│       ├── payment/TripPaymentRepositoryImpl.kt
│       ├── reports/ReportsRepositoryImpl.kt
│       ├── team/TeamRepositoryImpl.kt
│       ├── trip/TripRepositoryImpl.kt
│       ├── user/UserRepositoryImpl.kt
│       └── vehicle/VehicleRepositoryImpl.kt
└── domain/
    ├── entity/
    │   ├── customer/Customer.kt
    │   ├── dashboard/DashboardStats.kt
    │   ├── driver/Driver.kt
    │   ├── finance/
    │   │   ├── LoanPayment.kt
    │   │   └── VehiclePurchase.kt
    │   ├── maps/MapEntities.kt
    │   ├── payment/
    │   │   ├── PaymentEnums.kt
    │   │   └── TripPayment.kt
    │   ├── reports/ProfitLossEntities.kt
    │   ├── team/TeamMember.kt
    │   ├── trip/Trip.kt
    │   └── vehicle/
    │       ├── Vehicle.kt
    │       └── VehicleDetail.kt
    ├── repository/
    │   ├── costs/
    │   │   ├── CostsRepository.kt
    │   │   └── CostTypesRepository.kt
    │   ├── customer/CustomerRepository.kt
    │   ├── dashboard/DashboardRepository.kt
    │   ├── driver/DriverRepository.kt
    │   ├── finance/VehicleFinanceRepository.kt
    │   ├── payment/TripPaymentRepository.kt
    │   ├── reports/ReportsRepository.kt
    │   ├── team/TeamRepository.kt
    │   ├── trip/TripRepository.kt
    │   ├── user/UserRepository.kt
    │   └── vehicle/VehicleRepository.kt
    └── usecase/
        ├── costs/
        │   ├── GetCostTypesUseCase.kt
        │   └── InitializeCostTypesUseCase.kt
        ├── customer/CustomerUseCases.kt
        ├── dashboard/
        │   ├── DashboardUseCases.kt
        │   └── GetFinancialSummaryUseCase.kt
        ├── driver/DriverUseCases.kt
        ├── trip/TripUseCases.kt
        └── vehicle/VehicleUseCases.kt
```

---

## 4. `sharedUI` After Consolidation

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── core/
│   ├── auth/                              # AuthManager (presentation-layer facade)
│   ├── constants/                         # StatusConstants
│   ├── error/                             # FleetErrorContext
│   ├── init/                              # AppInitializer
│   ├── mvi/                               # MviViewModel base
│   ├── ui/                                # Reusable UI components
│   └── util/                              # Utility functions
├── data/
│   ├── database/                          # Room database (stays here)
│   │   ├── FleetDatabase.kt
│   │   ├── dao/                           # DAOs
│   │   └── entity/                        # Room entities
│   └── datasource/                        # Local data source IMPLEMENTATIONS only
│       ├── costs/CostsLocalDataSourceImpl.kt
│       ├── customer/CustomerLocalDataSourceImpl.kt
│       ├── dashboard/DashboardLocalDataSourceImpl.kt
│       └── team/TeamLocalDataSourceImpl.kt
├── di/                                    # Metro DI graphs
├── navigation/                            # Routes + NavHost
├── presentation/                          # All screens, ViewModels, Contracts
└── theme/                                 # App theming
```

---

## 5. Auth Flow

### Current Auth Flow
```
UserLocalDataSource (ijs-network-lib) → saves/retrieves token via Settings
Repository methods → call requireAuthToken() → gets token from UserLocalDataSource
HttpClientProvider → intercepts 401 → emits AuthenticationManager.emitSessionExpired()
App.kt (sharedUI) → collects authEvents → navigates to Login
```

### After Consolidation
```
AuthManager (sharedUI)
  ├── Wraps UserLocalDataSource.getAuthToken()
  ├── Wraps UserLocalDataSource.clearSession()
  ├── Registers session clear callback with AuthenticationManager
  └── Provides token to all ViewModels via DI

All repositories (ijs-network-lib)
  ├── Use AuthTokenHelper.requireAuthTokenOrRedirect()
  ├── Get token from UserLocalDataSource (injected)
  └── AuthenticationManager handles 401 globally
```

### AuthManager in sharedUI
```kotlin
@Inject
class AuthManager(
    private val userLocalDataSource: UserLocalDataSource
) {
    suspend fun getToken(): String? = userLocalDataSource.getAuthToken()
    suspend fun getUserRole(): String? = userLocalDataSource.getUserRole()
    suspend fun isLoggedIn(): Boolean = userLocalDataSource.isLoggedIn()
    suspend fun clearSession() = userLocalDataSource.clearSession()
    
    fun registerSessionClearCallback() {
        AuthenticationManager.registerSessionClearCallback {
            userLocalDataSource.clearSession()
        }
    }
}
```

---

## 6. Local DataSource Interface/Implementation Split

### Problem
Local data sources use Room DAOs (a sharedUI dependency). Moving them entirely to `ijs-network-lib` would require adding Room to the network module.

### Solution
Split each local data source file into:
- **Interface** → `ijs-network-lib` (no Room dependency)
- **Implementation** → `sharedUI` (depends on Room DAOs)

### Files Affected

| Feature | Interface (→ ijs-network-lib) | Implementation (stays in sharedUI) |
|---------|-------------------------------|-------------------------------------|
| Costs | `CostsLocalDataSource` | `CostsLocalDataSourceImpl` |
| Customer | `CustomerLocalDataSource` | `CustomerLocalDataSourceImpl` |
| Dashboard | `DashboardLocalDataSource` | `DashboardLocalDataSourceImpl` |
| Team | `TeamLocalDataSource` | `TeamLocalDataSourceImpl` |
| User | Both stay in `ijs-network-lib` | Uses `Settings`, no Room |

### DI Wiring
Repository implementations in `ijs-network-lib` depend on `LocalDataSource` interfaces.
The concrete Room-based implementations are provided via Metro DI in `sharedUI`'s feature graphs.

```kotlin
// In sharedUI's feature graph
@Provides
fun provideCostsLocalDataSource(dao: CostTypesDao, json: Json): CostsLocalDataSource =
    CostsLocalDataSourceImpl(dao, json)
```

---

## 7. DashboardCacheMapper Dependency Issue

`DashboardCacheMapper` maps between `DashboardDataDto` (ijs-network-lib) and `DashboardCacheEntity` (Room entity in sharedUI). Since Room entities stay in sharedUI, `DashboardCacheMapper` must also stay in sharedUI.

### Solution
- `DashboardMapper` (DTO → Domain entity) → moves to `ijs-network-lib`
- `DashboardCacheMapper` (DTO ↔ Room entity) → stays in `sharedUI`

Same pattern applies to any mapper that references Room entities.

---

## 8. Dependencies Update

### ijs-network-lib/build.gradle.kts (additions)
```kotlin
commonMain.dependencies {
    // Existing
    api(project(":ijs-core-lib"))
    implementation(libs.ktor.client.core)
    // ... existing ktor deps ...
    implementation(libs.multiplatformSettings)
    
    // NEW — needed by entities/mappers
    implementation(libs.kotlinx.datetime)
}
```

### sharedUI/build.gradle.kts (removals)
```kotlin
// REMOVE these:
// implementation(project(":ijs-reports-lib"))
// implementation(project(":ijs-finance-lib"))
```

### settings.gradle.kts (removals)
```kotlin
// REMOVE these:
// include(":ijs-reports-lib")
// include(":ijs-finance-lib")
```

---

## 9. Migration Sequence

### Phase 1: Prepare ijs-network-lib
1. Update `build.gradle.kts` with `kotlinx-datetime`
2. Create directory structure for all features

### Phase 2: Move Domain Entities (10 features)
- `domain/entity/{feature}/` from sharedUI + reports-lib + finance-lib → ijs-network-lib
- No import changes needed (same package names)

### Phase 3: Move DTOs & Models (14 model dirs)
- `data/model/{feature}/` from sharedUI + reports-lib + finance-lib → ijs-network-lib

### Phase 4: Move Mappers (10 mappers)
- `data/mapper/{feature}/` from sharedUI → ijs-network-lib
- Exception: `DashboardCacheMapper` stays in sharedUI

### Phase 5: Move Remote Data Sources (10 features)
- `data/datasource/{feature}/RemoteDataSource.kt` from sharedUI → ijs-network-lib

### Phase 6: Split Local Data Sources
- Extract interfaces from 4 local data source files → ijs-network-lib
- Keep implementations in sharedUI

### Phase 7: Move Repository Interfaces (10 features)
- `domain/repository/{feature}/` from sharedUI → ijs-network-lib

### Phase 8: Move Repository Implementations (10 features)
- `data/repository/{feature}/` from sharedUI → ijs-network-lib

### Phase 9: Move Use Cases (6 feature groups)
- `domain/usecase/{feature}/` from sharedUI → ijs-network-lib

### Phase 10: Fold Reports & Finance Libs
- Move remaining files from ijs-reports-lib → ijs-network-lib
- Move remaining files from ijs-finance-lib → ijs-network-lib
- Remove modules from settings.gradle.kts

### Phase 11: Create AuthManager
- Create `sharedUI/core/auth/AuthManager.kt`
- Wire into DI

### Phase 12: Update Build Files & Clean Up
- Update sharedUI build.gradle.kts
- Update settings.gradle.kts
- Remove empty directories
- Verify build

---

## 10. Verification Checklist

- [ ] `./gradlew :ijs-network-lib:compileCommonMainKotlinMetadata` passes
- [ ] `./gradlew :sharedUI:compileCommonMainKotlinMetadata` passes
- [ ] `./gradlew :androidApp:assembleDebug` passes
- [ ] All package imports resolve correctly
- [ ] No circular dependencies between modules
- [ ] Room database/DAOs/entities remain in sharedUI
- [ ] Local data source interfaces accessible from ijs-network-lib
- [ ] All feature graphs in sharedUI can inject dependencies
- [ ] Auth flow works: login → token saved → API calls authenticated → 401 handled

---

## 11. File Inventory (Files Moving to ijs-network-lib)

### From sharedUI (76 files)

#### Domain Entities (10 files)
1. `domain/entity/customer/Customer.kt`
2. `domain/entity/dashboard/DashboardStats.kt`
3. `domain/entity/driver/Driver.kt`
4. `domain/entity/maps/MapEntities.kt`
5. `domain/entity/payment/PaymentEnums.kt`
6. `domain/entity/payment/TripPayment.kt`
7. `domain/entity/team/TeamMember.kt`
8. `domain/entity/trip/Trip.kt`
9. `domain/entity/vehicle/Vehicle.kt`
10. `domain/entity/vehicle/VehicleDetail.kt`

#### DTOs & Models (13 files)
11. `data/model/caretaker/CaretakerDto.kt`
12. `data/model/costs/CostModels.kt`
13. `data/model/customer/CustomerDto.kt`
14. `data/model/dashboard/DashboardModels.kt`
15. `data/model/driver/DriverCostModels.kt`
16. `data/model/driver/DriverDto.kt`
17. `data/model/history/HistoryDto.kt`
18. `data/model/payment/TripPaymentDto.kt`
19. `data/model/payment/TripPaymentRequest.kt`
20. `data/model/state/StateHistoryDto.kt`
21. `data/model/team/TeamDto.kt`
22. `data/model/trip/TripDto.kt`
23. `data/model/vehicle/VehicleDto.kt`

#### Mappers (8 files — DashboardCacheMapper stays)
24. `data/mapper/customer/CustomerMapper.kt`
25. `data/mapper/dashboard/DashboardMapper.kt`
26. `data/mapper/driver/DriverMapper.kt`
27. `data/mapper/payment/TripPaymentMapper.kt`
28. `data/mapper/team/TeamMapper.kt`
29. `data/mapper/trip/TripMapper.kt`
30. `data/mapper/vehicle/VehicleMapper.kt`

#### Remote Data Sources (8 files)
31. `data/datasource/costs/CostsRemoteDataSource.kt`
32. `data/datasource/customer/CustomerRemoteDataSource.kt`
33. `data/datasource/dashboard/DashboardRemoteDataSource.kt`
34. `data/datasource/driver/DriverRemoteDataSource.kt`
35. `data/datasource/payment/TripPaymentRemoteDataSource.kt`
36. `data/datasource/team/TeamRemoteDataSource.kt`
37. `data/datasource/trip/TripRemoteDataSource.kt`
38. `data/datasource/vehicle/VehicleRemoteDataSource.kt`

#### Local DataSource Interfaces (split from existing files, 4 interfaces)
39. `data/datasource/costs/CostsLocalDataSource.kt` (interface only)
40. `data/datasource/customer/CustomerLocalDataSource.kt` (interface only)
41. `data/datasource/dashboard/DashboardLocalDataSource.kt` (interface only)
42. `data/datasource/team/TeamLocalDataSource.kt` (interface only)

#### Repository Interfaces (9 files)
43. `domain/repository/costs/CostsRepository.kt`
44. `domain/repository/costs/CostTypesRepository.kt`
45. `domain/repository/customer/CustomerRepository.kt`
46. `domain/repository/dashboard/DashboardRepository.kt`
47. `domain/repository/driver/DriverRepository.kt`
48. `domain/repository/payment/TripPaymentRepository.kt`
49. `domain/repository/team/TeamRepository.kt`
50. `domain/repository/trip/TripRepository.kt`
51. `domain/repository/vehicle/VehicleRepository.kt`

#### Repository Implementations (9 files)
52. `data/repository/costs/CostsRepositoryImpl.kt`
53. `data/repository/costs/CostTypesRepositoryImpl.kt`
54. `data/repository/customer/CustomerRepositoryImpl.kt`
55. `data/repository/dashboard/DashboardRepositoryImpl.kt`
56. `data/repository/driver/DriverRepositoryImpl.kt`
57. `data/repository/payment/TripPaymentRepositoryImpl.kt`
58. `data/repository/team/TeamRepositoryImpl.kt`
59. `data/repository/trip/TripRepositoryImpl.kt`
60. `data/repository/vehicle/VehicleRepositoryImpl.kt`

#### Use Cases (8 files)
61. `domain/usecase/costs/GetCostTypesUseCase.kt`
62. `domain/usecase/costs/InitializeCostTypesUseCase.kt`
63. `domain/usecase/customer/CustomerUseCases.kt`
64. `domain/usecase/dashboard/DashboardUseCases.kt`
65. `domain/usecase/dashboard/GetFinancialSummaryUseCase.kt`
66. `domain/usecase/driver/DriverUseCases.kt`
67. `domain/usecase/trip/TripUseCases.kt`
68. `domain/usecase/vehicle/VehicleUseCases.kt`

### From ijs-reports-lib (7 files)
69. `data/datasource/reports/ReportsRemoteDataSource.kt`
70. `data/mapper/reports/ProfitLossMapper.kt`
71. `data/model/reports/ProfitLossDto.kt`
72. `data/model/reports/ProfitLossRequest.kt`
73. `data/repository/reports/ReportsRepositoryImpl.kt`
74. `domain/entity/reports/ProfitLossEntities.kt`
75. `domain/repository/reports/ReportsRepository.kt`

### From ijs-finance-lib (7 files)
76. `data/datasource/finance/VehicleFinanceRemoteDataSource.kt`
77. `data/mapper/finance/VehicleFinanceMapper.kt`
78. `data/model/finance/VehicleFinanceDto.kt`
79. `data/repository/finance/VehicleFinanceRepositoryImpl.kt`
80. `domain/entity/finance/LoanPayment.kt`
81. `domain/entity/finance/VehiclePurchase.kt`
82. `domain/repository/finance/VehicleFinanceRepository.kt`

**Total: 82 files moving to ijs-network-lib**

---

## 12. Risk Mitigation

| Risk | Mitigation |
|------|------------|
| Circular dependency (sharedUI ↔ ijs-network-lib) | Local data source interfaces in ijs-network-lib, impls in sharedUI; no upward dependency |
| Build break during migration | Move in small batches; verify compilation after each phase |
| Package conflicts | Same `com.indusjs.fleet.*` packages in both modules — Kotlin allows this across modules |
| DashboardCacheMapper references Room entities | Keep DashboardCacheMapper in sharedUI |
| Large file sizes (>500 lines) | Split during migration: e.g., CostsRemoteDataSource (714 lines) |

