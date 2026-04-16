# AGENTS.md - sharedUI

## Purpose

The **core KMP module** — all shared business logic, UI screens, and architecture for IndusJS Fleet. This is the single source of truth consumed by `androidApp`, `webApp`, and `iosApp`. Contains every domain entity, use case, repository, data source, DTO, mapper, ViewModel, Compose screen, DI graph, and navigation wiring.

**Package:** `com.indusjs.fleet`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS (browser), WasmJS (browser)

**Razorpay:** [../Docs/Razorpay/sharedUI_RAZORPAY_INTEGRATION.md](../Docs/Razorpay/sharedUI_RAZORPAY_INTEGRATION.md) — [../Docs/Razorpay/README.md](../Docs/Razorpay/README.md).

---

## Architecture: Clean Architecture + MVI

```
Presentation (Screens + ViewModels + Contracts)
      ↓ sendIntent / collectState
Domain (Use Cases + Entities + Repository interfaces)
      ↓ implements
Data (DTOs + Mappers + DataSources + Repository impls)
```

### Data Flow (Every Feature Follows This)

```
User tap → Screen calls viewModel.sendIntent(Intent.X)
  → ViewModel.handleIntent() dispatches to private handler
    → UseCase(params) invoked → Flow<Result<T>>
      → Repository:
          1. emit(Result.Loading)
          2. token = userLocalDataSource.getAuthToken()
          3. response = remoteDataSource.apiCall(token)
          4. dto = parse response
          5. entity = mapper.toDomain(dto)
          6. emit(Result.Success(entity)) or emit(Result.Error(...))
    → ViewModel collects → updateState { copy(data = ...) }
  → Screen observes state via collectAsStateWithLifecycle()
  → UI recomposes
```

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/fleet/
├── App.kt                            # Root composable (theme → auth check → NavDisplay)
├── core/
│   ├── auth/AuthenticationManager.kt # Singleton: 401 → clear session → emit event → redirect to Login
│   ├── auth/AuthTokenHelper.kt      # Token extraction helpers
│   ├── constants/StatusConstants.kt  # ALL entity states, valid transitions, labels, icons, colors (539 lines)
│   ├── error/FleetErrorContext.kt    # Screen-specific error contexts (VEHICLES, DRIVERS, TRIPS, etc.)
│   ├── init/AppInitializer.kt        # One-time startup: cache cost types from API
│   ├── mvi/MviViewModel.kt          # Base ViewModel: state, intent, effect (93 lines)
│   ├── network/
│   │   ├── ApiConfig.kt              # BASE_URL + all endpoint paths + Google Places key
│   │   ├── ApiErrorHandler.kt        # JSON error extraction, DB constraint parsing
│   │   ├── HttpClientProvider.kt     # Ktor HttpClient factory with 401 interceptor
│   │   └── NetworkError.kt           # Network error classification
│   ├── ui/                           # Reusable Compose components (see section below)
│   └── util/                         # TimeUtils, FormatUtils, ValidationUtils, PermissionUtils
├── data/
│   ├── database/FleetDatabase.kt    # Settings-based offline cache (cost types, dashboard, customers, team)
│   ├── datasource/{feature}/        # Remote (API calls) + Local (cache) data sources
│   ├── mapper/{feature}/            # DTO ↔ Entity mappers
│   ├── model/{feature}/             # DTOs: @Serializable + @SerialName(snake_case)
│   └── repository/{feature}/       # Repository implementations
├── di/
│   ├── ViewModelProvider.kt         # Interface listing ALL ViewModels + LocalViewModelProvider + rememberViewModel
│   ├── DefaultViewModelProvider.kt  # Manual DI wiring (singleton, lazy deps, 494 lines)
│   ├── RootGraph.kt                 # Metro app-scope graph (HttpClient, Json, Settings)
│   ├── {Feature}FeatureGraph.kt     # Metro feature graphs (10 graphs)
│   └── AppDependencies.kt          # Scope markers: AppScope, FeatureScope
├── domain/
│   ├── entity/{feature}/            # Domain entities (immutable data classes)
│   ├── repository/{feature}/        # Repository interfaces
│   └── usecase/{feature}/           # Use cases (single-responsibility)
├── navigation/
│   ├── FleetRoute.kt               # 40+ @Serializable sealed routes (96 lines)
│   └── FleetNavigation.kt          # fleetEntryProvider() maps routes → NavEntry composables (560 lines)
├── presentation/{feature}/          # MVI Contract + ViewModel + Screen per feature
└── theme/                           # FleetTheme (Color, Typography, Shapes)
```

---

## MVI Pattern (Every Feature Follows This)

### Contract (State + Intent + Effect)

```kotlin
object FeatureContract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        // feature-specific fields
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadData : Intent
        data class OnItemClick(val id: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateTo(val route: FleetRoute) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
```

### ViewModel

```kotlin
class FeatureViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val useCase: FeatureUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init { sendIntent(Intent.LoadData) }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
            is Intent.OnItemClick -> sendEffect(Effect.NavigateTo(...))
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }
        useCase().collectLatest { result ->
            when (result) {
                is Result.Loading -> {} // already set
                is Result.Success -> updateState { copy(isLoading = false, data = result.data) }
                is Result.Error -> updateState { copy(isLoading = false, error = result.errorMessage) }
            }
        }
    }
}
```

### Screen

```kotlin
@Composable
fun FeatureScreen(viewModel: FeatureViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateTo -> { /* navigate */ }
            is Effect.ShowSnackbar -> { /* show snackbar */ }
        }
    }

    ScreenContent(isLoading = state.isLoading, error = state.error,
                  onRetry = { viewModel.sendIntent(Intent.LoadData) }) {
        // Compose UI
    }
}
```

---

## DI: DefaultViewModelProvider (Current Production DI)

**File:** `di/DefaultViewModelProvider.kt` (494 lines)

All dependencies are manually wired with `by lazy`. Singleton via `getInstance()`. Every ViewModel is created through the `ViewModelProvider` interface.

```kotlin
// In FleetNavigation.kt — how screens get ViewModels:
val viewModel = rememberViewModel { vehiclesViewModel() }
val sharedVM = rememberSharedViewModel("finance_$vehicleId") { vehicleFinanceViewModel() }
```

**Key pattern:** `rememberSharedViewModel(key)` is used for Vehicle Finance flow where VehicleFinanceDetailScreen, AddPurchaseInfoScreen, EditPurchaseInfoScreen, and EmiPaymentHistoryScreen share one ViewModel.

---

## Navigation: 40+ Routes

**File:** `navigation/FleetRoute.kt` — `@Serializable sealed interface FleetRoute : NavKey`

| Category | Routes |
|----------|--------|
| Auth | `Login`, `SignUp`, `ForgotPassword` |
| User | `Profile`, `ChangePassword` |
| Dashboard | `Dashboard` |
| Vehicles | `Vehicles`, `VehicleDetail(vehicleId)`, `AddVehicle` |
| Drivers | `Drivers`, `DriverDetail(driverId)`, `CreateDriver` |
| Trips | `Trips`, `TripDetail(tripId)`, `CreateTrip` |
| Costs | `TripCostEntry(tripId?, vehicleId?)`, `MaintenanceCostEntry(vehicleId?)`, `DriverCostEntry(driverId?)` |
| Customers | `Customers`, `CustomerDetail(customerId)`, `CreateCustomer` |
| Payments | `Payments`, `PaymentDetail(paymentId)`, `AddPayment(tripId?, vehicleId?)`, `EditPayment(paymentId)` |
| Reports | `Reports`, `VehicleProfitLoss`, `TripProfitLoss`, `CostAnalysis`, `ConsolidatedPL` |
| Team | `TeamList`, `CreateTeamMember(excludeGeneralManager)`, `TeamMemberDetail(memberId)` |
| Finance | `VehicleFinance`, `VehicleFinanceDetail(vehicleId)`, `AddPurchaseInfo`, `EditPurchaseInfo(vehicleId)`, `EmiPaymentHistory(vehicleId)` |
| Other | `Maps`, `AlertsList` |

**Wiring:** `FleetNavigation.kt` maps every route to `NavEntry { Screen(...) }` with navigation callbacks.

---

## 13 Feature Modules

### 1. Auth (Login, SignUp, ForgotPassword)
- **Data:** `UserRemoteDataSource` → `/auth/login`, `/auth/signup`, `/auth/forgot-password`
- **Storage:** `UserLocalDataSource` stores auth token in `Settings`
- **Flow:** Login success → token saved → navigate to Dashboard

### 2. Dashboard
- **Sections:** Fleet Overview, Cost Overview, Financial Summary (Owner/GM), Alerts, Quick Actions
- **Data:** `DashboardRemoteDataSource` → 5 endpoints
- **Offline:** Cached via `DashboardLocalDataSource` + Room DAO

### 3. Vehicles (List → Detail → Add/Edit)
- **Detail tabs:** Overview, Trips, Documents, Costs
- **Operations:** CRUD, state transitions, document upload, assign caretaker
- **States:** inactive → active → on_route → maintenance → damaged → decommissioned

### 4. Drivers (List → Detail → Create)
- **Operations:** CRUD, status toggle, state transitions, cost tracking
- **States:** inactive → active → on_route → on_leave → suspended → terminated

### 5. Trips (List → Detail → Create)
- **Create flow:** Select vehicle → driver → route (Google Places) → schedule → cargo → customer → pricing
- **States:** planned → on_route → completed (or cancelled/failed/delayed)
- **Cross-entity sync:** Trip started → vehicle/driver state → `on_route`; completed → `active`

### 6. Costs (Trip / Maintenance / Driver)
- **3 cost entry screens** with cost type chip selection from cached types
- **Cost IDs:** `TC-001-002` (Diesel), `VMC-002-003` (Tyres), `DC-001-001` (Monthly Salary)
- **Date format:** `DD-MM-YYYY` + `HH:MM` sent as-is (NOT ISO 8601)

### 7. Customers (List → Detail → Create)
- **Detail tabs:** Info, Trips, Financials
- **Offline:** Cached via `CustomerLocalDataSource`

### 8. Payments (List → Add → Detail → Edit)
- **Types:** advance, partial, final, refund
- **Modes:** cash, UPI, bank_transfer, card, credit
- **Status:** received, pending, cancelled

### 9. Reports (Hub → Vehicle P&L, Trip P&L, Cost Analysis, Consolidated P&L)
- **Owner/GM only** — financial data hidden from Manager/Supervisor
- **Periods:** daily, weekly, 15 days, monthly, quarterly, half-yearly, yearly, custom
- **Formula:** Revenue (`paid_trip_price`) - Expenses (trip + maintenance + driver + EMI) = Profit

### 10. Team Management (List → Detail → Create)
- **Roles:** Owner > General Manager > Manager > Supervisor
- **Operations:** Create members (role-restricted), toggle active, change role

### 11. Vehicle Finance (List → Detail → Add/Edit Purchase → EMI History)
- **Shared ViewModel:** `rememberSharedViewModel("finance_$vehicleId")` across all finance screens
- **Data:** Purchase info (cash/loan), loan tracking, EMI payments

### 12. Maps (Real-time vehicle tracking)
- MQTT subscribe to `fleet/vehicle/{reg}/location` from locationTracker app

### 13. Alerts (Document/license expiry, maintenance due)
- Data from `/dashboard/alerts-status`

---

## Reusable UI Components (`core/ui/`)

| Component | File | Usage |
|-----------|------|-------|
| `FleetTextField` | InputComponents.kt | Standard text input |
| `FleetDateField` | InputComponents.kt | DD-MM-YYYY with auto-delimiters |
| `FleetTimeField` | InputComponents.kt | HH:MM 24hr with auto-colon |
| `FleetMobileField` | PhoneComponents.kt | 10-digit mobile with country code |
| `FleetEmailField` | InputFields.kt | Email with validation |
| `FleetPasswordField` | InputFields.kt | Password with visibility toggle |
| `ScreenContent` | CommonComponents.kt | Wrapper handling loading/error/content states |
| `LoadingContent` | CommonComponents.kt | Centered spinner |
| `ErrorContent` | CommonComponents.kt | Error display with retry |
| `EmptyContent` | CommonComponents.kt | Empty state with icon + action |
| `FleetCard` | CardComponents.kt | Standard card styling |
| `FleetPrimaryButton` | ButtonComponents.kt | Primary action button |
| `FleetSecondaryButton` | ButtonComponents.kt | Secondary action button |
| `CostTypeChipSelector` | CostTypeChipSelector.kt | Cost type selection chips |
| `DateRangePickerDialog` | DateRangePickerDialog.kt | Date range filter |
| `PieChart` | PieChart.kt | Canvas-based pie chart |

---

## Entity State Machines (StatusConstants.kt)

### Vehicle States
```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned
```

### Driver States
```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active | terminated
```

### Trip States
```
planned → on_route → completed
planned → cancelled
on_route → delayed → completed | failed
on_route → cancelled | failed
```

---

## API Configuration (ApiConfig.kt)

**Base URL:** `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2`

**Key Endpoints:**
- Auth: `/auth/login`, `/auth/signup`, `/auth/forgot-password`
- Dashboard: `/dashboard`, `/dashboard/cost-overview`, `/dashboard/alerts-status`, `/dashboard/pending-payments`
- Vehicles: standard REST at `/vehicles`
- Drivers: standard REST at `/drivers`
- Trips: standard REST at `/trips`
- Cost Types: `/cost-types/trip`, `/cost-types/maintenance`, `/cost-types/driver`
- Costs: `/trip-costs`, `/maintenance-costs`
- Payments: `/trip-payments`
- Reports: `/reports/profit-loss`, `/vehicles/{id}/profit-loss`, `/trips/{id}/profit-loss`
- Team: `/team-members`
- Customers: `/customers`
- Finance: `/vehicles/{id}/purchase-info`, `/vehicles/{id}/loan-payments`

---

## Date Format Rules (CRITICAL)

| Context | Date Format | Time Format | Conversion |
|---------|-------------|-------------|------------|
| Trip scheduling (API) | ISO 8601 | Part of ISO | `FleetDateTime.toIso8601(date, time)` |
| Cost entries (API) | `DD-MM-YYYY` | `HH:MM` | Send as-is |
| Document expiry (API) | `DD-MM-YYYY` | N/A | Send as-is |
| UI display | `DD-MM-YYYY` | `HH:MM` (24hr) | Default |
| UI display (human) | `DD-MMM-YYYY` | `hh:mm AM/PM` | `FleetDateTime.formatAnyToDisplayDate()` |

---

## Role-Based Access Control

| Feature | Owner | GM | Manager | Supervisor |
|---------|-------|----|---------|------------|
| Financial data (trip_price, P&L) | ✅ | ✅ | ❌ | ❌ |
| Create vehicles/drivers/trips | ✅ | ✅ | ✅ | ❌ |
| Add costs | ✅ | ✅ | ✅ | ✅ |
| Delete costs | ✅ | ✅ | ✅ | ❌ |
| Create team members | ✅ | ✅ (M/S) | ❌ | ❌ |
| Reports | ✅ | ✅ | ❌ | ❌ |

---

## Adding a New Feature Checklist

1. Domain entity → `domain/entity/{feature}/`
2. Repository interface → `domain/repository/{feature}/`
3. Use cases → `domain/usecase/{feature}/`
4. DTOs → `data/model/{feature}/` (`@Serializable` + `@SerialName`)
5. Mapper → `data/mapper/{feature}/`
6. DataSource → `data/datasource/{feature}/`
7. Repository impl → `data/repository/{feature}/`
8. Contract (State, Intent, Effect) → `presentation/{feature}/`
9. ViewModel → `presentation/{feature}/`
10. Screen → `presentation/{feature}/`
11. Wire in `DefaultViewModelProvider.kt` (lazy deps + override)
12. Add to `ViewModelProvider.kt` interface
13. Route → `navigation/FleetRoute.kt`
14. NavEntry → `navigation/FleetNavigation.kt`

---

## Key Dependencies

| Library | Purpose | Version |
|---------|---------|---------|
| Compose Multiplatform | UI framework | 1.10.0 |
| Material 3 | Design system | 1.10.0-alpha05 |
| Ktor Client | HTTP networking | 3.3.3 |
| Metro (ZacSweers) | DI framework | 0.9.1 |
| Navigation 3 | Type-safe navigation | 1.1.0-alpha01 |
| kotlinx-serialization | JSON | 1.9.0 |
| Room | Local database | 2.8.4 |
| multiplatform-settings | Key-value storage | 1.3.0 |
| MaterialKolor | Dynamic theming | 4.0.5 |
| Kermit | Logging | 2.0.8 |
| ijs-error-lib | Result<T>, exceptions | local |
| ijs-dispatcher-lib | DispatcherProvider | local |
| ijs-datetime-picker | Date/time picker | local |
| ijs-datetime-utils | FleetDateTime | local |
| ijs-pdf-report | PDF generation | local |

## Build

```bash
./gradlew :sharedUI:compileCommonMainKotlinMetadata  # Verify compilation
```
