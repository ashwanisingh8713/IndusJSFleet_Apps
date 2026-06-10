# IndusJS Fleet — Feature Module Modularization Plan

> **Version:** 1.0  
> **Date:** 15-Mar-2026  
> **Status:** Implementation In Progress

---

## 1. Executive Summary

Decompose the monolithic `sharedUI` module into **11 focused KMP library modules** while preserving the `com.indusjs.fleet.*` package namespace. This yields **zero import-path changes** in presentation code and ensures each feature can be developed, tested, and compiled independently.

### New Modules (in build order)

| # | Module | Package Root | Responsibility |
|---|--------|-------------|----------------|
| 1 | `ijs-core-lib` | `com.indusjs.fleet.core`, `.domain`, `.data` | Base contracts, MVI, StatusConstants, utilities, cost-type data models, user entities |
| 2 | `ijs-network-lib` | `com.indusjs.fleet.core.network`, `.core.auth`, `.data.datasource.user` | HttpClient, ApiConfig, ApiErrorHandler, AuthenticationManager, UserLocalDataSource, UserRepository |
| 3 | `ijs-costs-lib` | `com.indusjs.fleet.data.model.costs`, `.domain.repository.costs`, `.domain.usecase.costs` | All cost DTOs, cost type caching, cost CRUD (trip/maintenance/driver costs) |
| 4 | `screen-vehicle` | `com.indusjs.fleet.domain.entity.vehicle`, `.data.*.vehicle` | Vehicle domain entities, repository, use cases, DTOs, mapper, data sources |
| 5 | `screen-driver` | `com.indusjs.fleet.domain.entity.driver`, `.data.*.driver` | Driver domain entities, repository, use cases, DTOs, mapper, data sources |
| 6 | `screen-trip` | `com.indusjs.fleet.domain.entity.trip`, `.data.*.trip`, `.data.datasource.location` | Trip domain + data + GooglePlacesService |
| 7 | `screen-customer` | `com.indusjs.fleet.domain.entity.customer`, `.data.*.customer` | Customer domain + data + offline cache |
| 8 | `screen-payment` | `com.indusjs.fleet.domain.entity.payment`, `.data.*.payment` | Payment domain + data |
| 9 | `screen-team` | `com.indusjs.fleet.domain.entity.team`, `.data.*.team` | Team management domain + data + offline cache |
| 10 | `screen-report` | `com.indusjs.fleet.domain.entity.reports`, `.data.*.reports` | P&L reports, cost analysis, consolidated reports |
| 11 | `screen-finance` | `com.indusjs.fleet.domain.entity.finance`, `.data.*.finance` | Vehicle purchase, loan tracking, EMI payments |

### Existing Modules (unchanged)

| Module | Purpose |
|--------|---------|
| `ijs-error-lib` | `Result<T>`, exception hierarchy, ErrorHandler |
| `ijs-dispatcher-lib` | Coroutine dispatchers, DispatcherProvider |
| `ijs-datetime-utils` | FleetDateTime operations |
| `ijs-datetime-picker` | Compose date/time picker |
| `ijs-pdf-report` | PDF report generation |

---

## 2. Target Dependency Graph

```
sharedUI (presentation + navigation + DI + theme + UI components + dashboard)
  ├── screen-vehicle   ──→ ijs-network-lib ──→ ijs-core-lib ──→ ijs-error-lib
  ├── screen-driver    ──→ ijs-network-lib ──→ ijs-core-lib     ijs-dispatcher-lib
  ├── screen-trip      ──→ ijs-network-lib ──→ ijs-core-lib
  ├── screen-customer  ──→ ijs-network-lib ──→ ijs-core-lib
  ├── screen-payment   ──→ ijs-network-lib ──→ ijs-core-lib
  ├── screen-team      ──→ ijs-network-lib ──→ ijs-core-lib
  ├── ijs-costs-lib     ──→ ijs-network-lib ──→ ijs-core-lib
  ├── screen-report   ──→ ijs-network-lib ──→ ijs-core-lib
  ├── screen-finance   ──→ ijs-network-lib ──→ ijs-core-lib
  ├── ijs-network-lib   ──→ ijs-core-lib
  ├── ijs-core-lib      ──→ ijs-error-lib, ijs-dispatcher-lib
  ├── ijs-datetime-*    (standalone)
  └── ijs-pdf-report    (standalone)
```

---

## 3. Module Specifications

### 3.1 `ijs-core-lib` — Shared Contracts, MVI, Constants, Utilities

**Gradle namespace:** `com.indusjs.fleet.core`

**Files to move (preserving packages):**

| Current Location | Package | What |
|-----------------|---------|------|
| `domain/entity/Entity.kt` | `com.indusjs.fleet.domain.entity` | `Entity` marker interface |
| `domain/repository/Repository.kt` | `com.indusjs.fleet.domain.repository` | `Repository` marker interface |
| `domain/usecase/UseCase.kt` | `com.indusjs.fleet.domain.usecase` | `UseCase`, `UseCaseWithParams`, `SuspendUseCase`, `SuspendUseCaseWithParams` |
| `data/datasource/DataSource.kt` | `com.indusjs.fleet.data.datasource` | `LocalDataSource`, `RemoteDataSource` |
| `data/model/DataModels.kt` | `com.indusjs.fleet.data.model` | `Dto`, `DbEntity` |
| `data/mapper/Mapper.kt` | `com.indusjs.fleet.data.mapper` | `Mapper<D,E>` + extensions |
| `core/constants/StatusConstants.kt` | `com.indusjs.fleet.core.constants` | All state machines, `StateColorScheme` |
| `core/error/FleetErrorContext.kt` | `com.indusjs.fleet.core.error` | Error context enum |
| `core/mvi/MviContract.kt` | `com.indusjs.fleet.core.mvi` | `UiState`, `UiIntent`, `UiEffect` |
| `core/mvi/MviViewModel.kt` | `com.indusjs.fleet.core.mvi` | Base MVI ViewModel |
| `core/mvi/MviExtensions.kt` | `com.indusjs.fleet.core.mvi` | `HandleEffects`, `collectState` |
| `core/util/FormatUtils.kt` | `com.indusjs.fleet.core.util` | Currency/number formatting |
| `core/util/ValidationUtils.kt` | `com.indusjs.fleet.core.util` | Date/time/email validation |
| `core/util/PermissionUtils.kt` | `com.indusjs.fleet.core.util` | Role-based permission checks |
| `core/util/CostTypeUtils.kt` | `com.indusjs.fleet.core.util` | Cost display names/icons/colors |
| `core/util/TimeUtils.kt` + actuals | `com.indusjs.fleet.core.util` | `expect fun currentTimeMillis()` |
| `core/util/PhoneCallUtil.kt` + actuals | `com.indusjs.fleet.core.util` | `expect fun rememberPhoneDialer()` |
| `domain/entity/user/User.kt` | `com.indusjs.fleet.domain.entity.user` | `UserRole`, `User`, `UserProfile`, `AuthResult` |
| NEW: Extract from `CostTypeChipSelector.kt` | `com.indusjs.fleet.core.model` | `CostTypeGroup`, `CostTypeItem`, `CostTypeSelection` (data classes only) |

**Dependencies:**
- `ijs-error-lib`, `ijs-dispatcher-lib`
- `kotlinx-coroutines-core`, `kotlinx-serialization-json`, `kermit`
- `compose-runtime`, `compose-ui`, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`

**Platform source sets:** `androidMain`, `iosMain`, `jsMain`, `wasmJsMain` (for `TimeUtils` + `PhoneCallUtil` expect/actuals)

---

### 3.2 `ijs-network-lib` — HTTP Infrastructure, Auth, User Data Layer

**Gradle namespace:** `com.indusjs.fleet.network`

**Files to move:**

| Current Location | Package | What |
|-----------------|---------|------|
| `core/network/ApiConfig.kt` | `com.indusjs.fleet.core.network` | Base URL, endpoints, API key, timeout |
| `core/network/HttpClientProvider.kt` | `com.indusjs.fleet.core.network` | Ktor HttpClient factory + 401 interceptor |
| `core/network/ApiErrorHandler.kt` | `com.indusjs.fleet.core.network` | JSON error extraction |
| `core/network/NetworkError.kt` | `com.indusjs.fleet.core.network` | NetworkError sealed class |
| `core/auth/AuthenticationManager.kt` | `com.indusjs.fleet.core.auth` | Auth event bus singleton |
| `core/auth/AuthTokenHelper.kt` | `com.indusjs.fleet.core.auth` | Require-auth-or-redirect helper |
| `data/datasource/user/UserLocalDataSource.kt` | `com.indusjs.fleet.data.datasource.user` | Interface + UserLocalDataSourceImpl |
| `data/datasource/user/UserRemoteDataSource.kt` | `com.indusjs.fleet.data.datasource.user` | Login/signup/profile API calls |
| `data/model/user/*` | `com.indusjs.fleet.data.model.user` | User DTOs |
| `data/mapper/user/*` | `com.indusjs.fleet.data.mapper.user` | User mapper |
| `data/repository/user/UserRepositoryImpl.kt` | `com.indusjs.fleet.data.repository.user` | Repository impl |
| `domain/repository/user/UserRepository.kt` | `com.indusjs.fleet.domain.repository.user` | Repository interface |

**Dependencies:**
- `api(project(":ijs-core-lib"))` — transitively exposes ijs-error-lib, ijs-dispatcher-lib
- Ktor: `ktor-client-core`, `ktor-client-content-negotiation`, `ktor-client-serialization`, `ktor-serialization-json`, `ktor-client-logging`
- `kotlinx-serialization-json`, `kermit`, `multiplatform-settings`
- Platform: `ktor-client-okhttp` (Android), `ktor-client-darwin` (iOS)

---

### 3.3 `ijs-costs-lib` — Cross-Cutting Cost Domain

**Gradle namespace:** `com.indusjs.fleet.costs`

**Files to move:**

| Current Location | What |
|-----------------|------|
| `data/model/costs/CostModels.kt` (748 lines) | All trip/maintenance cost DTOs, requests, responses, fallback types |
| `data/model/driver/DriverCostModels.kt` (404 lines) | Driver cost DTOs, requests, responses, fallback types |
| `data/datasource/costs/CostsRemoteDataSource.kt` (714 lines) | Interface + impl for all cost API calls |
| `data/datasource/costs/CostsLocalDataSource.kt` | Cost types caching via Settings |
| `data/repository/costs/CostsRepositoryImpl.kt` | Cost operations repository impl |
| `data/repository/costs/CostTypesRepositoryImpl.kt` | Cost types cache repository impl |
| `domain/repository/costs/CostsRepository.kt` | Cost operations interface |
| `domain/repository/costs/CostTypesRepository.kt` | Cost types interface |
| `domain/usecase/costs/GetCostTypesUseCase.kt` | 3 use cases (trip/maintenance/driver) |
| `domain/usecase/costs/InitializeCostTypesUseCase.kt` | One-time initialization |
| `core/util/TripCostToDriverCostMapper.kt` | Trip → Driver cost sync |

**Dependencies:** `api(project(":ijs-network-lib"))`, `ijs-core-lib` (transitive)

---

### 3.4 `screen-vehicle`

**Gradle namespace:** `com.indusjs.fleet.vehicle`

**Files:** `Vehicle.kt`, `VehicleDetail.kt`, `VehicleRepository.kt`, 6 use cases, `VehicleDto`, `VehicleMapper`, `VehicleRemoteDataSourceImpl`, `VehicleRepositoryImpl`

**Dependencies:** `api(project(":ijs-network-lib"))`

---

### 3.5 `screen-driver`

**Gradle namespace:** `com.indusjs.fleet.driver`

**Files:** `Driver.kt`, `DriverRepository.kt`, 8 use cases, `DriverDto.kt` (driver DTOs only, NOT DriverCostModels), `DriverMapper`, `DriverRemoteDataSourceImpl`, `DriverRepositoryImpl`

**Dependencies:** `api(project(":ijs-network-lib"))`

---

### 3.6 `screen-trip`

**Gradle namespace:** `com.indusjs.fleet.trip`

**Files:** `Trip.kt`, `TripRepository.kt`, 6 use cases, trip DTOs, `TripMapper`, `TripStopMapper`, `TripRemoteDataSourceImpl`, `TripRepositoryImpl`, `GooglePlacesService` + Places DTOs

**Dependencies:** `api(project(":ijs-network-lib"))`

---

### 3.7 `screen-customer`

**Gradle namespace:** `com.indusjs.fleet.customer`

**Files:** Customer entity, repository interface, 8 use cases, DTOs, `CustomerRemoteDataSource`, `CustomerLocalDataSourceImpl`, `CustomerRepositoryImpl`

**Dependencies:** `api(project(":ijs-network-lib"))`, `kotlinx-serialization-json`

---

### 3.8 `screen-payment`

**Gradle namespace:** `com.indusjs.fleet.payment`

**Files:** `TripPayment.kt`, `PaymentEnums.kt`, `TripPaymentRepository.kt`, DTOs, `TripPaymentRemoteDataSource`, `TripPaymentRepositoryImpl`

**Dependencies:** `api(project(":ijs-network-lib"))`, `kotlinx-serialization-json`

---

### 3.9 `screen-team`

**Gradle namespace:** `com.indusjs.fleet.team`

**Files:** `TeamMember.kt`, `TeamRepository.kt`, DTOs, `TeamRemoteDataSourceImpl`, `TeamLocalDataSourceImpl`, `TeamRepositoryImpl`

**Dependencies:** `api(project(":ijs-network-lib"))`, `kotlinx-serialization-json`

---

### 3.10 `screen-report`

**Gradle namespace:** `com.indusjs.fleet.reports`

**Files:**

| Current Location | What |
|-----------------|------|
| `domain/entity/reports/ProfitLossEntities.kt` | All P&L domain entities |
| `domain/repository/reports/ReportsRepository.kt` | Repository interface |
| `data/model/reports/ProfitLossDto.kt` | P&L DTOs |
| `data/model/reports/ProfitLossRequest.kt` | P&L request DTOs |
| `data/mapper/reports/ProfitLossMapper.kt` | P&L mapper |
| `data/datasource/reports/ReportsRemoteDataSource.kt` | Reports API calls |
| `data/repository/reports/ReportsRepositoryImpl.kt` | Repository impl |

**Dependencies:** `api(project(":ijs-network-lib"))`, `kotlinx-serialization-json`

---

### 3.11 `screen-finance`

**Gradle namespace:** `com.indusjs.fleet.finance`

**Files:**

| Current Location | What |
|-----------------|------|
| `domain/entity/finance/VehiclePurchase.kt` | Purchase entity + enums |
| `domain/entity/finance/LoanPayment.kt` | Loan payment entity + enums |
| `domain/repository/finance/VehicleFinanceRepository.kt` | Repository interface |
| `data/model/finance/VehicleFinanceDto.kt` | Finance DTOs |
| `data/mapper/finance/VehicleFinanceMapper.kt` | Finance mapper |
| `data/datasource/finance/VehicleFinanceRemoteDataSource.kt` | Finance API calls |
| `data/repository/finance/VehicleFinanceRepositoryImpl.kt` | Repository impl |

**Dependencies:** `api(project(":ijs-network-lib"))`, `ijs-dispatcher-lib`, `kotlinx-serialization-json`

---

## 4. What Stays in `sharedUI`

After extraction, `sharedUI` becomes a **composition shell**:

| Category | Content |
|----------|---------|
| **Presentation** | All 40+ screens, ViewModels, Contracts (presentation/*) |
| **Navigation** | FleetRoute.kt, FleetNavigation.kt |
| **DI** | DefaultViewModelProvider, ViewModelProvider, all FeatureGraph files |
| **Theme** | Color.kt, Theme.kt, FleetTheme |
| **UI Components** | FleetTextField, FleetCard, CostTypeChipSelector, ButtonComponents, etc. (core/ui/*) |
| **App** | App.kt, AppInitializer |
| **Database** | FleetDatabase + DAOs (Settings-based caching for dashboard, cost types, team, customers) |
| **Dashboard** | Dashboard data/domain/presentation (cross-cutting aggregation) |
| **Alerts/Maps** | Alerts + MQTT Maps presentation + data |
| **PDF Handlers** | Platform-specific PDF export handlers (core/pdf/*) |
| **Shared Models** | caretaker, state, history DTOs (used by multiple features' presentation) |

---

## 5. Execution Sequence

Each step must compile before proceeding to the next.

| Step | Module | Compile Command | Depends On |
|------|--------|----------------|------------|
| 1 | `ijs-core-lib` | `./gradlew :ijs-core-lib:compileCommonMainKotlinMetadata` | ijs-error-lib, ijs-dispatcher-lib |
| 2 | `ijs-network-lib` | `./gradlew :ijs-network-lib:compileCommonMainKotlinMetadata` | Step 1 |
| 3 | `ijs-costs-lib` | `./gradlew :ijs-costs-lib:compileCommonMainKotlinMetadata` | Step 2 |
| 4 | `screen-vehicle` | `./gradlew :screen-vehicle:compileCommonMainKotlinMetadata` | Step 2 |
| 5 | `screen-driver` | `./gradlew :screen-driver:compileCommonMainKotlinMetadata` | Step 2 |
| 6 | `screen-trip` | `./gradlew :screen-trip:compileCommonMainKotlinMetadata` | Step 2 |
| 7 | `screen-customer` | `./gradlew :screen-customer:compileCommonMainKotlinMetadata` | Step 2 |
| 8 | `screen-payment` | `./gradlew :screen-payment:compileCommonMainKotlinMetadata` | Step 2 |
| 9 | `screen-team` | `./gradlew :screen-team:compileCommonMainKotlinMetadata` | Step 2 |
| 10 | `screen-report` | `./gradlew :screen-report:compileCommonMainKotlinMetadata` | Step 2 |
| 11 | `screen-finance` | `./gradlew :screen-finance:compileCommonMainKotlinMetadata` | Step 2 |
| 12 | Update `sharedUI` | `./gradlew :sharedUI:compileCommonMainKotlinMetadata` | Steps 1-11 |
| 13 | Full build | `./gradlew :androidApp:assembleDebug` | Step 12 |

---

## 6. Risk Mitigation

| Risk | Mitigation |
|------|-----------|
| Circular dependencies | `CostTypeGroup`/`CostTypeItem` extracted to `ijs-core-lib`; costs lib has no dependency on any feature lib |
| Package conflicts | All packages preserved as `com.indusjs.fleet.*` — no renaming needed |
| Build order breaks | Strict sequential build verification after each module |
| Missing platform source sets | Each module with expect/actual gets all 4 platform dirs |
| Import path breaks | Package namespace preserved = zero import changes in presentation layer |
| `CostsRepository` uses driver DTOs | Driver cost DTOs (`DriverCostModels.kt`) move to `ijs-costs-lib`, not `screen-driver` |
| `TripCostToDriverCostMapper` references presentation class `CostEntryRow` | Refactor to use plain parameters or move `CostEntryRow` data class to `ijs-costs-lib` |

---

## 7. Verification Checklist

After full migration:

- [ ] `./gradlew :ijs-core-lib:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :ijs-network-lib:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :ijs-costs-lib:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-vehicle:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-driver:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-trip:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-customer:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-payment:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-team:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-report:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :screen-finance:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :sharedUI:compileCommonMainKotlinMetadata` ✅
- [ ] `./gradlew :androidApp:assembleDebug` ✅
- [ ] `./gradlew :webApp:jsBrowserDevelopmentRun` ✅
- [ ] No `import` changes in any `presentation/` file
- [ ] All 40+ screens render correctly
- [ ] Login → Dashboard → all features navigation works
- [ ] Cost entry screens work (trip/maintenance/driver)
- [ ] Reports render with data
- [ ] Vehicle finance flow works

---

## 8. Implementation Progress

### Completed ✅

| Date | What | Details |
|------|------|---------|
| 15-Mar-2026 | `ijs-core-lib` created | MVI, StatusConstants, utilities, user entities, marker interfaces |
| 15-Mar-2026 | `ijs-network-lib` created | HttpClient, ApiConfig, ApiErrorHandler, Auth, UserLocalDataSource, UserRepository |
| 15-Mar-2026 | `screen-report` created | P&L entities, DTOs, mapper, remote data source, repository |
| 15-Mar-2026 | `screen-finance` created | Purchase/Loan entities, DTOs, mapper, remote data source, repository |
| 16-Mar-2026 | Network consolidation | Moved `NetworkConfig` factory methods to `ijs-network-lib` `HttpClientProvider` (added `createJson()`, `createHttpClient(json)`) |
| 16-Mar-2026 | `GooglePlacesService` moved | Moved from `sharedUI` to `ijs-network-lib` (data/datasource/location/) — zero import changes |
| 16-Mar-2026 | `ApiConfig.Endpoints` expanded | Added all 50+ endpoint constants for vehicles, drivers, trips, customers, payments, team, documents, finance, reports, caretaker |
| 16-Mar-2026 | JS/WasmJS Ktor engines | Added `ktor-client-js` to `jsMain` and `wasmJsMain` source sets in `ijs-network-lib` |
| 16-Mar-2026 | `NetworkModule.kt` simplified | Removed duplicate `NetworkConfig` object — now delegates to `HttpClientProvider` from `ijs-network-lib` |
| 16-Mar-2026 | `doc/` folders created | Added `doc/README.md` to all 9 modules: core, network, reports, finance, error, dispatcher, datetime-utils, datetime-picker, pdf-report |
| 16-Mar-2026 | Full build verified | ✅ `ijs-network-lib`, `screen-report`, `screen-finance`, `sharedUI`, `androidApp:assembleDebug` — all pass |

### Pending ⏳

| Module | Status |
|--------|--------|
| `ijs-costs-lib` | Not started — requires extracting CostModels, DriverCostModels, CostsRemoteDataSource, repositories, use cases |
| `screen-vehicle` | Not started |
| `screen-driver` | Not started |
| `screen-trip` | Not started — GooglePlacesService already moved to ijs-network-lib (shared utility) |
| `screen-customer` | Not started |
| `screen-payment` | Not started |
| `screen-team` | Not started |
| `settings.gradle.kts` update | Pending — needs new module includes when they're created |
| `sharedUI` cleanup | Pending — remove files that were moved to new modules |
