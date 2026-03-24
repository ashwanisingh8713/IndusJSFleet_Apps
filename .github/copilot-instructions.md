# IndusJS Fleet - Copilot Instructions

## Project Overview

**IndusJS Fleet** is a Kotlin Multiplatform (KMP) Fleet Management application targeting:
- **Android** (primary)
- **iOS** (Swift/Kotlin interop)
- **Web** (JS + WasmJS)
- **Desktop** (future)

The app manages vehicles, drivers, trips, costs, payments, customers, team members, vehicle finances, and real-time GPS tracking for fleet operations.

---

## Technology Stack

| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin | 2.3.0 |
| UI Framework | Compose Multiplatform | 1.10.0-rc01 |
| Design System | Material 3 | 1.10.0-alpha05 |
| Networking | Ktor Client | 3.3.3 |
| DI Framework | Metro (ZacSweers) | 0.9.1 |
| Navigation | Navigation 3 | 1.1.0-alpha01 |
| Serialization | kotlinx-serialization | 1.9.0 |
| Local Storage | Room | 2.8.4 |
| Date/Time | kotlinx-datetime | 0.7.1 |
| Logging | Kermit | 2.0.8 |
| Preferences | multiplatform-settings | 1.3.0 |
| Theming | MaterialKolor | 4.0.5 |
| Crash Reporting | Firebase Crashlytics | BOM-managed |
| Tracking | MQTT (HiveMQ) via locationTracker | - |
| Location | Google Places + Distance Matrix API | - |

---

## Module Catalog

> **STRICT RULE:** Every module **must** follow **Clean Architecture** (Domain → Data → Presentation layering). Every feature module **must** use **Metro DI** for dependency injection. Every module containing UI or ViewModel logic **must** follow the **MVI** (Model-View-Intent) pattern.

### Module Dependency Graph

```
androidApp ──→ sharedUI
webApp ─────→ sharedUI
iosApp ─────→ sharedUI (via framework)

sharedUI ──→ ijs-core-lib
sharedUI ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-vehicle ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-driver  ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-trip    ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-customer──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-payment ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-team    ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-report ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-finance ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-user    ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-onboarding ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-dashboard ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-alerts  ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ feat-map     ──→ ijs-network-lib ──→ ijs-core-lib
sharedUI ──→ ijs-datetime-picker ──→ ijs-datetime-utils
sharedUI ──→ ijs-pdf-report  ──→ ijs-datetime-utils

ijs-core-lib ──→ ijs-error-lib
ijs-core-lib ──→ ijs-dispatcher-lib
ijs-core-lib ──→ ijs-datetime-utils

locationTracker (standalone Android app, no dependencies on sharedUI)
```

### Foundation Modules (No Feature Logic)

| Module | Namespace | Description | Architecture |
|--------|-----------|-------------|--------------|
| **`ijs-error-lib`** | `com.indusjs.error` | **Error handling foundation.** Provides `Result<T>` sealed class, exception hierarchy (`IjsException`, `ApiException`, `NetworkException`, `AuthException`, `ValidationException`), `ErrorHandler`, `ErrorClassifier`, `ErrorContext`, and HTTP error codes. Zero external dependencies beyond coroutines. | Pure utility — no DI/MVI needed |
| **`ijs-dispatcher-lib`** | `com.indusjs.dispatcher` | **Coroutine dispatcher abstraction.** Provides `DispatcherProvider` interface, `DefaultDispatcherProvider`, and platform-specific implementations (`AndroidDispatcherProvider`, `IosDispatcherProvider`, `JsDispatcherProvider`, `WasmJsDispatcherProvider`). Enables testable coroutine code via dispatcher injection. **Must be used wherever background work is required.** | Pure utility — no DI/MVI needed |
| **`ijs-datetime-utils`** | `com.indusjs.datetimeutils` | **Date/time conversion utilities.** Provides `FleetDateTime` object for DD-MM-YYYY ↔ ISO 8601 conversion, date formatting, and time manipulation. Single-file module wrapping `kotlinx-datetime`. | Pure utility — no DI/MVI needed |
| **`ijs-core-lib`** | `com.indusjs.fleet.core` | **Shared foundation for all feature modules.** Aggregates `ijs-error-lib`, `ijs-dispatcher-lib`, `ijs-datetime-utils` via `api()`. Provides: MVI base classes (`MviViewModel`, `MviContract`, `MviExtensions`), `FleetErrorContext`, `StatusConstants`, utilities (`ValidationUtils`, `FormatUtils`, `CostTypeUtils`, `TimeUtils`, `PhoneCallUtil`), shared DTOs (`CostModels`, `DataModels`, `DriverCostModels`, `HistoryDto`, `StateHistoryDto`), and base interfaces (`DataSource`, `Mapper`, `Repository`, `UseCase`, `Entity`). **Every feature module depends on this transitively via `ijs-network-lib`.** | Clean Architecture base classes — no DI/MVI needed |

### Infrastructure Module

| Module | Namespace | Description | Architecture |
|--------|-----------|-------------|--------------|
| **`ijs-network-lib`** | `com.indusjs.fleet.network` | **Networking & cross-cutting data layer.** Provides: HTTP client setup (`HttpClientProvider`, `ApiConfig`, `ApiErrorHandler`, `NetworkError`), authentication (`AuthenticationManager`, `AuthTokenHelper`), `UserLocalDataSource`/`UserRemoteDataSource` (auth tokens, user session), `DashboardRemoteDataSource`/`DashboardRepositoryImpl` (dashboard stats), `CostsRemoteDataSource`/`CostsRepositoryImpl` (shared cost operations), `GooglePlacesService` (location autocomplete), `NetworkDataGraph` (Metro DI graph), and domain entities for dashboard/maps. Exposes `ijs-core-lib` transitively via `api()`. **Every feature module depends on this.** | **Clean Architecture + Metro DI** |

### Feature Data Modules

Each feature module contains the **data layer + domain layer** for its feature, following Clean Architecture strictly:
- `domain/entity/` — Domain entities (pure Kotlin data classes)
- `domain/repository/` — Repository interfaces
- `domain/usecase/` — Use case classes
- `data/model/` — DTOs with `@Serializable` + `@SerialName`
- `data/datasource/` — Remote data source implementations
- `data/mapper/` — DTO ↔ Entity mappers
- `data/repository/` — Repository implementations

| Module | Namespace | Description | Key Files |
|--------|-----------|-------------|-----------|
| **`feat-vehicle`** | `com.ijs.vehicle` | **Vehicle feature data layer.** CRUD operations for vehicles, document management, maintenance costs, vehicle status transitions (`inactive → active → on_route → maintenance → damaged → decommissioned`). | `VehicleRemoteDataSource`, `VehicleRepositoryImpl`, `VehicleMapper`, `VehicleDto`, `Vehicle`, `VehicleDetail`, `VehicleUseCases` |
| **`feat-driver`** | `com.ijs.driver` | **Driver feature data layer.** CRUD for drivers, license tracking, driver cost management (salary, advance, bonus, penalty), status transitions (`inactive → active → on_route → on_leave → suspended → terminated`). | `DriverRemoteDataSource`, `DriverRepositoryImpl`, `DriverMapper`, `DriverDto`, `DriverCostModels`, `Driver`, `DriverUseCases` |
| **`feat-trip`** | `com.ijs.trip` | **Trip feature data layer.** Trip planning, route management, cargo tracking, scheduling, trip cost recording, state machine (`planned → on_route → completed`, or `cancelled`/`failed`/`delayed`). | `TripRemoteDataSource`, `TripRepositoryImpl`, `TripMapper`, `TripDto`, `Trip`, `TripUseCases` |
| **`feat-customer`** | `com.ijs.customer` | **Customer feature data layer.** Customer CRUD, company/contact/GST info, trip history by customer, financial summaries. Includes both remote and local data sources for caching. | `CustomerRemoteDataSource`, `CustomerLocalDataSource`, `CustomerRepositoryImpl`, `CustomerMapper`, `CustomerDto`, `Customer`, `CustomerUseCases` |
| **`feat-payment`** | `com.ijs.payment` | **Payment feature data layer.** Trip payment recording (cash, UPI, bank transfer, cheque, card), payment status tracking (`received`, `pending`, `cancelled`), payment history. | `TripPaymentRemoteDataSource`, `TripPaymentRepositoryImpl`, `TripPaymentMapper`, `TripPaymentDto`, `TripPaymentRequest`, `TripPayment`, `PaymentEnums` |
| **`feat-team`** | `com.ijs.team` | **Team management data layer.** Team member CRUD (General Manager, Manager, Supervisor), role-based access, member status management. Includes local data source for caching. | `TeamRemoteDataSource`, `TeamLocalDataSource`, `TeamRepositoryImpl`, `TeamMapper`, `TeamDto`, `TeamMember` |
| **`feat-report`** | `com.ijs.reports` | **Reports & analytics data layer.** Profit/Loss by vehicle, by trip, cost analysis with date ranges, consolidated P&L statements. Owner/GM-only access. | `ReportsRemoteDataSource`, `ReportsRepositoryImpl`, `ProfitLossDto`, `ProfitLossRequest`, `ProfitLossEntities` |
| **`feat-finance`** | `com.ijs.finance` | **Vehicle finance data layer.** Vehicle purchase records, loan tracking, EMI payment history, finance status summaries. | `VehicleFinanceRemoteDataSource`, `VehicleFinanceRepositoryImpl`, `VehicleFinanceMapper`, `VehicleFinanceDto`, `VehiclePurchase`, `LoanPayment` |

### Presentation-Only Feature Modules

Each presentation-only feature module contains the **presentation layer** (Contract, ViewModel, Screen, Facade) for its feature. Data layer stays in `ijs-network-lib`.

| Module | Namespace | Description | Key Files |
|--------|-----------|-------------|-----------|
| **`feat-user`** | `com.ijs.user` | **User/Auth feature presentation.** Login, signup, forgot password, profile, change password screens. | `LoginViewModel`, `SignUpViewModel`, `ProfileViewModel`, `UserFeatureFacade` |
| **`feat-onboarding`** | `com.ijs.onboarding` | **Onboarding feature presentation.** First-time user onboarding flow. | `OnboardingViewModel`, `OnboardingFeatureFacade` |
| **`feat-dashboard`** | `com.ijs.dashboard` | **Dashboard feature presentation.** Main overview screen with fleet stats, cost overview, financial summary, alerts, quick actions. 6 use cases, 18+ navigation callbacks. | `DashboardViewModel`, `DashboardScreen`, `DashboardContract`, `DashboardFeatureFacade` |
| **`feat-alerts`** | `com.ijs.alerts` | **Alerts feature presentation.** Document expiry, license expiry, maintenance due alerts list. | `AlertsListViewModel`, `AlertsListScreen`, `AlertsFeatureFacade` |
| **`feat-map`** | `com.ijs.map` | **Maps feature presentation.** Real-time vehicle tracking on map (MQTT subscribe). Currently mock data. | `MapsViewModel`, `MapsScreen`, `MapFeatureFacade` |

### UI Component Modules

| Module | Namespace | Description | Architecture |
|--------|-----------|-------------|--------------|
| **`ijs-datetime-picker`** | `com.indusjs.datetimepicker` | **Compose date/time picker component.** Provides `FleetDateTimePicker`, `DatePickerSection`, `TimePickerSection`, `QuickDateShortcuts`. Material 3 styled. Depends on `ijs-datetime-utils`. | Pure Compose UI — no DI/MVI needed |
| **`ijs-pdf-report`** | `com.indusjs.pdfreport` | **PDF report generation.** HTML→PDF conversion with platform-specific generators (Android WebView, iOS WKWebView, JS/WASM browser). Provides `PdfReportFacade`, `PdfExportDialog`, and specialized handlers for each report type (vehicle costs, trip costs, driver costs, P&L, payments, customer financials). | Platform-specific — no DI/MVI needed |

### Application Modules

| Module | Namespace | Description |
|--------|-----------|-------------|
| **`sharedUI`** | `com.indusjs.fleet` | **Core orchestration module.** Contains Navigation, DI wiring (ViewModelProvider, DefaultViewModelProvider, FeatureRepositoryFactory), theming, and shared utilities. **NO presentation code** — all screens/ViewModels/contracts live in their respective `feat-*` modules. Depends on all feature modules. Produces `SharedUI` iOS framework. |
| **`androidApp`** | `com.indusjs.fleet.androidApp` | **Android entry point (thin shell).** `FleetApplication` + `AppActivity`. Firebase Crashlytics integration. |
| **`webApp`** | N/A | **Web entry point (thin shell).** JS + WasmJS browser targets. Single `main.kt`. |
| **`locationTracker`** | `com.indusjs.fleet.locationtracker` | **Standalone Android GPS tracking app (separate APK).** MQTT-based location publishing via HiveMQ. Contains `LocationTrackingService`, `MqttClientManager`, `TrackerPreferencesRepository`. No dependency on `sharedUI`. |

---

## Build Configuration

### Centralized Android Conventions

All modules use `gradle/fleet-android-conventions.gradle` for shared build settings:

```groovy
// Applied in each module's build.gradle.kts:
apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
```

**Centralized values:**
- `compileSdk = 36`
- `minSdk = 23` (locationTracker overrides to 24)
- `targetSdk = 36`
- `jvmTarget = JVM_17`

**Do NOT set these values in individual module build files.** They are auto-configured.

### Build Commands

```bash
./gradlew :androidApp:assembleDebug              # Android debug APK
./gradlew :androidApp:assembleRelease             # Android release APK
./gradlew :webApp:jsBrowserDevelopmentRun          # Web JS dev server
./gradlew :webApp:wasmJsBrowserDevelopmentRun      # Web WASM dev server
./gradlew :locationTracker:assembleDebug           # Location tracker APK
```

---

## Architecture: Clean Architecture + MVI + Metro DI

> **STRICT RULE:** Every module containing business logic **must** follow Clean Architecture. Presentation modules **must** use MVI pattern. DI **must** use Metro annotations.

### Layer Structure

```
┌──────────────────────────────────────────────────────────────────┐
│                      Presentation Layer (sharedUI)               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  UI (Compose)  ←→  ViewModel (MVI)  ←→  State/Intent/Effect │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              ↓ Uses
┌──────────────────────────────────────────────────────────────────┐
│                        Domain Layer (feature-lib modules)        │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │        Use Cases  ←→  Entities  ←→  Repository Interfaces  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              ↓ Implements
┌──────────────────────────────────────────────────────────────────┐
│                         Data Layer (feature-lib modules)         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  Repository Impl  ←→  Data Sources  ←→  DTOs/Mappers       │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### Data Flow (Request Lifecycle)

```
User taps button
  → Screen calls viewModel.sendIntent(Intent.LoadData)
    → ViewModel.handleIntent() routes to handler function
      → handler calls useCase() → returns Flow<Result<T>>
        → UseCase calls repository.getData() → Flow<Result<T>>
          → Repository:
              1. emit(Result.Loading)
              2. token = userLocalDataSource.getAuthToken()
              3. response = remoteDataSource.apiCall(token)
              4. if success → emit(Result.Success(mapper.toDomain(dto)))
              5. if error → emit(Result.Error(exception, message))
    → ViewModel collects flow → updateState { copy(data = result.data) }
  → Screen observes state via collectAsStateWithLifecycle()
  → UI recomposes with new data
```

### Folder Structure (Feature Module — e.g., `feat-vehicle`)

```
feat-vehicle/src/commonMain/kotlin/com/indusjs/fleet/
├── data/
│   ├── datasource/vehicle/    # VehicleRemoteDataSource + Impl
│   ├── mapper/vehicle/        # VehicleMapper (DTO ↔ Entity)
│   ├── model/vehicle/         # VehicleDto (@Serializable)
│   └── repository/vehicle/    # VehicleRepositoryImpl
└── domain/
    ├── entity/vehicle/        # Vehicle, VehicleDetail (data classes)
    ├── repository/vehicle/    # VehicleRepository (interface)
    └── usecase/vehicle/       # VehicleUseCases
```

### Folder Structure (Presentation — in `sharedUI`)

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── core/                          # Shared utilities, constants, UI components
│   ├── auth/                      # Authentication state management
│   ├── constants/                 # StatusConstants
│   ├── error/                     # FleetErrorContext
│   ├── init/                      # AppInitializer
│   ├── mvi/                       # MVI base classes (from ijs-core-lib)
│   ├── network/                   # Network configuration
│   ├── ui/                        # Reusable Compose components
│   └── util/                      # Utilities
├── data/                          # sharedUI-specific data (Room DB, dashboard caching)
├── di/                            # DI wiring
│   ├── DefaultViewModelProvider   # Production ViewModel factory
│   ├── FeatureRepositoryFactory   # Wires feature-lib repos
│   └── ViewModelProvider          # Interface + CompositionLocal
├── navigation/                    # Type-safe navigation
│   ├── FleetRoute.kt             # All route definitions (@Serializable)
│   └── FleetNavigation.kt        # NavHost setup with all NavEntries
├── presentation/{feature}/        # Feature screens (Compose + MVI)
│   ├── {Feature}Contract.kt      # State, Intent, Effect
│   ├── {Feature}ViewModel.kt     # MVI ViewModel
│   └── {Feature}Screen.kt        # Compose UI
└── theme/                         # FleetTheme, Color, Typography
```

---

## MVI Pattern

### Contract Structure

Every feature follows this MVI contract pattern:

```kotlin
object FeatureContract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        // Feature-specific state
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

### ViewModel Pattern

```kotlin
@Inject
class FeatureViewModel(
    private val useCase: FeatureUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadData)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
            is Intent.OnItemClick -> handleItemClick(intent.id)
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }
        when (val result = useCase()) {
            is Result.Success -> updateState { copy(isLoading = false, data = result.data) }
            is Result.Error -> updateState { copy(isLoading = false, error = result.message) }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}
```

### Screen Pattern

```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateTo -> { /* Navigate */ }
            is Effect.ShowSnackbar -> { /* Show snackbar */ }
        }
    }

    ScreenContent(
        isLoading = state.isLoading,
        error = state.error,
        onRetry = { viewModel.sendIntent(Intent.LoadData) }
    ) {
        // Screen content
    }
}
```

---

## Metro Dependency Injection

### Key Annotations

| Annotation | Usage |
|------------|-------|
| `@Inject` | Mark class for constructor injection (on class, not constructor) |
| `@DependencyGraph` | Define a DI graph/component |
| `@DependencyGraph.Factory` | Factory for graphs needing external dependencies |
| `@Provides` | Provide a dependency instance |
| `@Binds` | Bind interface to implementation |
| `@SingleIn(Scope::class)` | Scope dependency to a lifecycle |

### DI Wiring in sharedUI

The `sharedUI` module wires all feature module dependencies via:

1. **`FeatureRepositoryFactory`** — Constructs repositories from feature libs using `HttpClient`, `Json`, `Settings`, `DispatcherProvider`
2. **`DefaultViewModelProvider`** — Creates all ViewModels, injecting use cases and repositories
3. **`ViewModelProvider`** interface — Exposed via `CompositionLocal` for Compose access

```kotlin
// Usage in Compose
val viewModel = rememberViewModel { dashboardViewModel() }
// or for shared ViewModels across related screens
val financeVM = rememberSharedViewModel("finance_$vehicleId") { vehicleFinanceViewModel() }
```

---

## API Conventions

### Base URL
```
Production: https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2
Local: http://localhost:8080/api/v2
```

### Date & Time Formats (CRITICAL)

| API Type | Date Format | Time Format | Example |
|----------|-------------|-------------|---------|
| Trip Create/Update | ISO 8601 | ISO 8601 | `2026-01-04T14:30:00Z` |
| Trip Costs (individual) | `DD-MM-YYYY` | `HH:MM` | `"date": "20-12-2025", "time": "10:30"` |
| Trip Costs (bulk) | ISO 8601 | ISO 8601 | `2026-12-20T10:30:00Z` |
| Maintenance Costs | `DD-MM-YYYY` | `HH:MM` | `"date": "20-12-2025", "time": "14:00"` |
| Document Expiry | `DD-MM-YYYY` | N/A | `"expiry_date": "31-12-2026"` |
| Driver License | `DD-MM-YYYY` | N/A | `"license_expiry": "31-12-2026"` |
| UI Display | `DD-MM-YYYY` | `HH:MM` (24hr) | `04-01-2026`, `14:30` |

**Conversion Example:**
```kotlin
// UI to API (for Trip scheduling)
fun toIsoDateTime(date: String, time: String): String {
    val (day, month, year) = date.split("-")
    return "${year}-${month}-${day}T${time}:00Z"
}

// UI to API (for Cost entries - simple format)
// Just send as-is: "date": "04-01-2026", "time": "14:30"
```

### DTO Conventions

```kotlin
@Serializable
data class TripDto(
    @SerialName("id")
    val id: Int,
    @SerialName("vehicle_id")           // snake_case for API
    val vehicleId: Int,                  // camelCase for Kotlin
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    // ... all fields with @SerialName
)
```

### API Response Wrapper

```kotlin
@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: T? = null
)
```

---

## Core UI Components

### Always Use These Reusable Components

| Component | Usage |
|-----------|-------|
| `FleetTextField` | Standard text input |
| `FleetDateField` | Date input (DD-MM-YYYY with auto-delimiters) |
| `FleetTimeField` | Time input (HH:MM 24hr with auto-colon) |
| `FleetMobileField` | Mobile input (10 digits) |
| `FleetEmailField` | Email input with validation |
| `FleetPasswordField` | Password with visibility toggle |
| `LoadingContent` | Centered loading spinner |
| `ErrorContent` | Error display with retry button |
| `EmptyContent` | Empty state with icon and action |
| `ScreenContent` | Wrapper handling loading/error/content states |
| `FleetCard` | Standard card with consistent styling |
| `FleetPrimaryButton` | Primary action button |
| `FleetSecondaryButton` | Secondary action button |

### Example Usage

```kotlin
FleetDateField(
    value = state.departureDate,
    onValueChange = { viewModel.sendIntent(Intent.UpdateDepartureDate(it)) },
    label = "Departure Date",
    placeholder = "DD-MM-YYYY",
    isError = state.departureDateError != null,
    errorMessage = state.departureDateError
)
```

---

## Error Handling

> **Note:** Error handling is provided by `ijs-error-lib`. Import from `com.indusjs.error`.

### Result Sealed Class

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

### Exception Hierarchy

```kotlin
// From ijs-error-lib (com.indusjs.error.exception)
sealed class IjsException(message: String, cause: Throwable? = null) : Exception(message, cause)
class ApiException(message: String, val code: Int? = null) : IjsException(message)
class NetworkException(message: String = "Network unavailable") : IjsException(message)
class AuthException(message: String = "Not authenticated") : IjsException(message)
class ValidationException(message: String, val field: String? = null) : IjsException(message)
```

### Error Context

```kotlin
ErrorContent(
    error = state.error,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadVehicles) }
)
```

**Available Contexts:**
`FleetErrorContext.DASHBOARD`, `VEHICLES`, `DRIVERS`, `TRIPS`, `COSTS`, `AUTH`

---

## Validation

### Use ValidationUtils (from `ijs-core-lib`)

```kotlin
val dateResult = ValidationUtils.validateDate("04-01-2026")
if (dateResult is ValidationResult.Error) { showError(dateResult.message) }

val timeResult = ValidationUtils.validateTime("14:30", required = true)
if (!isValidEmail(email)) { /* error */ }
if (!isValidMobile(mobile)) { /* error */ }
```

---

## Navigation

### Type-Safe Routes

```kotlin
@Serializable
sealed interface FleetRoute : NavKey {
    @Serializable data object Dashboard : FleetRoute
    @Serializable data object Vehicles : FleetRoute
    @Serializable data class VehicleDetail(val vehicleId: String) : FleetRoute
    @Serializable data object CreateTrip : FleetRoute
    // ... 40+ routes
}
```

### Navigation Usage

```kotlin
sendEffect(Effect.NavigateTo(FleetRoute.VehicleDetail(vehicleId)))
```

---

## Theme

### Day/Night Support

```kotlin
@Composable
fun FleetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FleetTypography,
        shapes = FleetShapes,
        content = content
    )
}
```

### Color Usage

```kotlin
// Always use MaterialTheme colors — NEVER hardcode
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.surfaceContainerLow
```

---

## External APIs

### Google Places API
Used for location autocomplete in CreateTripScreen and EditTripScreen.
**API Key:** Stored in `local.properties` as `GOOGLE_PLACES_API_KEY`

### Google Distance Matrix API
Used to calculate road distance between locations.

---

## Resources

### Icons (SVG in composeResources/drawable/)
```kotlin
import indusjs_fleet.sharedui.generated.resources.*
Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Back")
```

---

## Class Size Enforcement

> **STRICT RULE — No Exceptions**

- Every class file **must not exceed 500 lines**.
- If a class grows beyond 500 lines, it **must be refactored** by splitting it into multiple focused classes.
- Follow proper architectural patterns when splitting:
  - Extract cohesive groups of methods into dedicated service, helper, or utility classes.
  - Use composition over inheritance when delegating responsibilities to new classes.
  - Ensure each new class has a **single, clear responsibility** (follow the Single Responsibility Principle).
  - Name new classes explicitly based on their responsibility (e.g., `UserValidator`, `UserRepository`, `UserNotificationService`).
- New class files must be placed in appropriate directories/modules consistent with the existing project architecture.
- Do **not** merge unrelated logic into a single class just to avoid creating new files.

### Refactoring Examples

```
❌ VehicleDetailViewModel.kt (650 lines) — too large

✅ Split into:
   VehicleDetailViewModel.kt       (handles intents, state)
   VehicleDetailDataLoader.kt      (loads vehicle, trips, documents)
   VehicleDetailStateReducer.kt    (state update logic)
   VehicleDocumentHandler.kt       (document upload/delete logic)
```

```
❌ TripRepositoryImpl.kt (520 lines) — too large

✅ Split into:
   TripRepositoryImpl.kt           (CRUD operations)
   TripCostRepository.kt           (cost-related operations)
   TripStatusManager.kt            (status transition logic)
```

---

## Best Practices

### DO ✅
- Use `@Inject` on class, not constructor
- Use `Result<T>` for all async operations
- Use `updateState { copy(...) }` for immutable state updates
- Use `sendEffect()` for one-time events (navigation, snackbars)
- Use core UI components for consistency
- Convert dates to ISO 8601 before API calls
- Add `@SerialName` with snake_case for all DTO fields
- Handle loading, error, and empty states in every screen
- Use `DispatcherProvider` from `ijs-dispatcher-lib` for all background work
- Place shared DTOs in `ijs-core-lib`, feature DTOs in feature-lib modules
- Use `FeatureRepositoryFactory` to wire repos in `sharedUI`

### DON'T ❌
- Don't use `mutableStateOf` in ViewModels (use `MviViewModel`)
- Don't call APIs directly from UI (use UseCase → Repository → DataSource)
- Don't hardcode colors (use `MaterialTheme.colorScheme`)
- Don't send DD-MM-YYYY format to API (convert to ISO 8601)
- Don't create new TextField variants (use `FleetTextField`, `FleetDateField`, etc.)
- Don't skip error handling in DataSource calls
- Don't set `compileSdk`/`minSdk`/`jvmTarget` in module build files (use `fleet-android-conventions.gradle`)
- Don't add domain/data logic to `sharedUI` — put it in the appropriate feature-lib module
- Don't use `Dispatchers.IO` directly — use `DispatcherProvider.io`

---

## Application Use Cases

### Vehicle Management
| Use Case | API | Description |
|----------|-----|-------------|
| Add Vehicle | `POST /vehicles` | Register new vehicle with documents |
| View Vehicle | `GET /vehicles/{id}` | See details with tabs (Overview, Trips, Documents, Costs) |
| Edit Vehicle | `PUT /vehicles/{id}` | Update vehicle info and assigned driver |
| Upload Document | `POST /vehicles/{id}/documents` | Add document with expiry tracking |
| Add Maintenance Cost | `POST /vehicles/{id}/maintenance-costs` | Record maintenance expense |
| View Costs | `GET /vehicles/{id}/maintenance-costs` | Filter by date range, cost type |

### Driver Management
| Use Case | API | Description |
|----------|-----|-------------|
| Add Driver | `POST /drivers` | Register new driver with license info |
| View Driver | `GET /drivers/{id}` | See details with license, assignments |
| Edit Driver | `PUT /drivers/{id}` | Update driver information |
| Toggle Status | `PATCH /drivers/{id}/toggle-active` | Activate/deactivate driver |

### Trip Management
| Use Case | API | Description |
|----------|-----|-------------|
| Create Trip | `POST /trips` | Plan trip with vehicle, driver, route, schedule |
| View Trip | `GET /trips/{id}` | See details with route, cargo, costs |
| Edit Trip | `PUT /trips/{id}` | Update trip before departure |
| Cancel Trip | `PATCH /trips/{id}/cancel` | Cancel planned trip |
| Add Trip Cost | `POST /trips/{id}/costs` | Record trip expense |

### Dashboard Features
| Use Case | API | Description |
|----------|-----|-------------|
| Fleet Overview | `GET /dashboard` | Vehicle, driver, trip stats |
| Cost Overview | `GET /dashboard/cost-overview` | Financial summary with filter |
| Pending Payments | `GET /dashboard/pending-payments` | Outstanding payments |
| Alerts Status | `GET /dashboard/alerts-status` | Document/license expiry alerts |

### Customer Management
| Use Case | API | Description |
|----------|-----|-------------|
| Add Customer | `POST /customers` | Register company + contact + GST + address |
| View Customer | `GET /customers/{id}` | Info, trip history, financials |
| List Customers | `GET /customers` | Search and browse customers |

### Payment Management
| Use Case | API | Description |
|----------|-----|-------------|
| Add Payment | `POST /payments` | Record payment (mode, amount, reference) |
| View Payment | `GET /payments/{id}` | Payment details |
| Edit Payment | `PUT /payments/{id}` | Update existing payment |

### Reports (Owner/GM only)
| Use Case | API | Description |
|----------|-----|-------------|
| Vehicle P&L | `GET /reports/vehicle-pl` | Revenue vs expenses per vehicle |
| Trip P&L | `GET /reports/trip-pl` | Revenue vs expenses per trip |
| Cost Analysis | `GET /reports/cost-analysis` | Breakdown by cost type |
| Consolidated P&L | `GET /reports/consolidated-pl` | Overall P&L statement |

### Vehicle Finance
| Use Case | API | Description |
|----------|-----|-------------|
| Add Purchase | `POST /vehicle-finance` | Record purchase (cash/loan) |
| View Finance | `GET /vehicle-finance/{id}` | Purchase info, loan summary |
| EMI Payments | `GET /vehicle-finance/{id}/payments` | EMI payment history |

### Team Management
| Use Case | API | Description |
|----------|-----|-------------|
| Add Member | `POST /team` | Add GM/Manager/Supervisor |
| View Member | `GET /team/{id}` | Member details + permissions |
| List Members | `GET /team` | All team members |

---

## Data Types Reference

### Cargo Types
```
Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others
```

### Trip Cost Types
```
fuel, toll, driver_allowance, parking, loading_charges,
unloading_charges, chalan, permit, insurance, other
```

### Maintenance Cost Types
```
tyre, battery, servicing, engine_repair, body_repair,
electrical, ac_repair, other
```

### Payment Status
```
pending, partial, paid
```

### Payment Modes
```
cash, upi, bank_transfer, cheque, card
```

### Trip States
```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

### Vehicle States
```
inactive → active → on_route → active
                  → maintenance ←→ damaged → decommissioned
```

### Driver States
```
inactive → active → on_route → active
                  → on_leave → active
                  → suspended → active
                  → terminated (terminal)
```

### User Roles (Hierarchical)
```
owner > general_manager > manager > supervisor > driver
```

---

## Screen Types & Templates

### List Screen
- TopAppBar with back, refresh, add actions
- Optional search bar and filter chips
- LazyColumn with item cards
- Empty state with add action
- FAB for quick add

### Detail Screen
- TopAppBar with back, edit, more actions
- Hero/header section
- Tabs for complex entities
- Section cards with labels/values
- Action buttons

### Form Screen
- TopAppBar with back, save actions
- Section cards with inputs
- FleetTextField, FleetDateField, FleetTimeField
- Dropdowns for selections
- Submit button at bottom

### Dashboard Screen
- Hamburger menu navigation
- Section cards in LazyColumn
- Quick action buttons
- Status summaries
- Alerts section

---

## Prompts Reference

See `.github/prompts/` for detailed implementation prompts:
- `screen-flows.prompt.md` - Navigation and screen structure
- `dashboard-sections.prompt.md` - Dashboard section details
- `use-cases.prompt.md` - Application use cases
- `ui-components.prompt.md` - Reusable UI components reference
- `date-time-handling.prompt.md` - Date/Time conversion and validation
- `new-feature.prompt.md` - Feature implementation guide
- `compose-screen.prompt.md` - Screen templates
- `mvi-contract.prompt.md` - MVI pattern templates
- `api-integration.prompt.md` - API integration guide
- `dto-mapper.prompt.md` - DTO and mapper templates
- `di-graph.prompt.md` - Metro DI graph templates
- `navigation.prompt.md` - Navigation setup guide
- `usecase.prompt.md` - Use case implementation

---

## Adding a New Feature Checklist

1. [ ] Add domain entity in `{feature-lib}/domain/entity/{feature}/`
2. [ ] Add repository interface in `{feature-lib}/domain/repository/{feature}/`
3. [ ] Add use cases in `{feature-lib}/domain/usecase/{feature}/`
4. [ ] Add DTOs in `{feature-lib}/data/model/{feature}/`
5. [ ] Add mapper in `{feature-lib}/data/mapper/{feature}/`
6. [ ] Add data source in `{feature-lib}/data/datasource/{feature}/`
7. [ ] Add repository impl in `{feature-lib}/data/repository/{feature}/`
8. [ ] Wire repository in `sharedUI/di/FeatureRepositoryFactory.kt`
9. [ ] Add Contract (State, Intent, Effect) in `sharedUI/presentation/{feature}/`
10. [ ] Add ViewModel in `sharedUI/presentation/{feature}/`
11. [ ] Add Screen in `sharedUI/presentation/{feature}/`
12. [ ] Add ViewModel factory method in `sharedUI/di/ViewModelProvider.kt`
13. [ ] Wire ViewModel in `sharedUI/di/DefaultViewModelProvider.kt`
14. [ ] Add route in `sharedUI/navigation/FleetRoute.kt`
15. [ ] Wire NavEntry in `sharedUI/navigation/FleetNavigation.kt`
