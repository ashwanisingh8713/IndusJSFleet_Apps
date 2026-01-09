# IndusJS Fleet - Copilot Instructions

## Project Overview

**IndusJS Fleet** is a Kotlin Multiplatform (KMP) Fleet Management application targeting:
- **Android** (primary)
- **iOS** (Swift/Kotlin interop)
- **Web** (Kobweb - planned migration)
- **Desktop** (future)

The app manages vehicles, drivers, trips, costs, and real-time tracking for fleet operations.

---

## Technology Stack

| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin | 2.3.0 |
| UI Framework | Compose Multiplatform | 1.10.0 |
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

### Shared Libraries

| Library | Purpose |
|---------|---------|
| `ijs-error-lib` | Error handling, FleetException hierarchy, error context |
| `ijs-dispatcher-lib` | Coroutine dispatchers, DispatcherProvider |

---

## Architecture: Clean Architecture + MVI

### Layer Structure

```
┌──────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  UI (Compose)  ←→  ViewModel (MVI)  ←→  State/Intent/Effect │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              ↓ Uses
┌──────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │        Use Cases  ←→  Entities  ←→  Repository Interfaces  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              ↓ Implements
┌──────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  Repository Impl  ←→  Data Sources  ←→  DTOs/Mappers       │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### Folder Structure

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── core/                          # Core utilities
│   ├── dispatcher/                # Coroutine dispatchers
│   ├── error/                     # FleetException hierarchy
│   ├── mvi/                       # MVI base classes
│   ├── network/                   # ApiConfig, HttpClient
│   ├── result/                    # Result<T> sealed class
│   ├── ui/                        # Reusable UI components
│   └── util/                      # TimeUtils, ValidationUtils
├── data/                          # Data layer
│   ├── datasource/{feature}/      # Remote/Local data sources
│   ├── mapper/{feature}/          # DTO ↔ Entity mappers
│   ├── model/{feature}/           # DTOs with @Serializable
│   └── repository/{feature}/      # Repository implementations
├── di/                            # Metro DI graphs
│   ├── RootGraph.kt               # App-level dependencies
│   ├── {Feature}FeatureGraph.kt   # Feature-specific graphs
│   └── ViewModelProvider.kt       # ViewModel factory
├── domain/                        # Domain layer
│   ├── entity/{feature}/          # Domain entities
│   ├── repository/{feature}/      # Repository interfaces
│   └── usecase/{feature}/         # Use cases
├── navigation/                    # Type-safe navigation
│   ├── FleetRoute.kt              # Route definitions
│   └── FleetNavigation.kt         # Nav host setup
├── presentation/{feature}/        # Presentation layer
│   ├── {Feature}Contract.kt       # State, Intent, Effect
│   ├── {Feature}ViewModel.kt      # MVI ViewModel
│   └── {Feature}Screen.kt         # Compose UI
└── theme/                         # App theming
    ├── Color.kt                   # Color definitions
    └── Theme.kt                   # FleetTheme, Typography
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

### RootGraph (No External Dependencies)

```kotlin
@SingleIn(AppScope::class)
@DependencyGraph
abstract class RootGraph : NetworkModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(json: Json): HttpClient = NetworkConfig.createHttpClient(json)

    abstract val httpClient: HttpClient

    companion object
}
```

### FeatureGraph (With External Dependencies)

```kotlin
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class FeatureGraph {
    @Binds
    abstract fun bindRepository(impl: RepositoryImpl): Repository

    abstract val viewModel: FeatureViewModel

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider
        ): FeatureGraph
    }
}
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
    // date = "04-01-2026" (DD-MM-YYYY), time = "14:30" (HH:MM)
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
    @SerialName("planned_start")
    val plannedStart: String? = null,
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
// From ijs-error-lib
sealed class FleetException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NotAuthenticatedException(message: String = "Not authenticated") : FleetException(message)
class ApiException(message: String, val code: Int? = null) : FleetException(message)
class NetworkException(message: String = "Network unavailable") : FleetException(message)
class ValidationException(message: String, val field: String? = null) : FleetException(message)
```

### Error Context

```kotlin
// Use FleetErrorContext for screen-specific error handling
ErrorContent(
    error = state.error,
    screenContext = FleetErrorContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadVehicles) }
)
```

**Available Contexts:**
- `FleetErrorContext.DASHBOARD`
- `FleetErrorContext.VEHICLES`
- `FleetErrorContext.DRIVERS`
- `FleetErrorContext.TRIPS`
- `FleetErrorContext.COSTS`
- `FleetErrorContext.AUTH`
```

---

## Validation

### Use ValidationUtils

```kotlin
// Date validation
val dateResult = ValidationUtils.validateDate("04-01-2026")
if (dateResult is ValidationResult.Error) {
    showError(dateResult.message)
}

// Time validation
val timeResult = ValidationUtils.validateTime("14:30", required = true)

// Email validation
if (!isValidEmail(email)) { /* error */ }

// Mobile validation
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
    // ...
}
```

### Navigation Usage

```kotlin
// Navigate to detail
sendEffect(Effect.NavigateTo(FleetRoute.VehicleDetail(vehicleId)))

// In NavHost
composable<FleetRoute.VehicleDetail> { backStackEntry ->
    val route = backStackEntry.toRoute<FleetRoute.VehicleDetail>()
    VehicleDetailScreen(vehicleId = route.vehicleId)
}
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
// Always use MaterialTheme colors
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.surfaceContainerLow
```

---

## External APIs

### Google Places API
Used for location autocomplete in CreateTripScreen and EditTripScreen.

```kotlin
// Location field with autocomplete
FleetLocationField(
    value = state.startLocation,
    onValueChange = { viewModel.sendIntent(Intent.UpdateStartLocation(it)) },
    onLocationSelected = { place ->
        viewModel.sendIntent(Intent.SetStartLocation(
            location = place.description,
            lat = place.lat,
            lng = place.lng
        ))
    },
    label = "Start Location"
)
```

### Google Distance Matrix API
Used to calculate road distance between locations.

```kotlin
// Auto-calculate distance when both locations are set
if (startLat != null && endLat != null) {
    calculateDistance(startLat, startLng, endLat, endLng)
}
```

**API Key:** Stored in `local.properties` as `GOOGLE_PLACES_API_KEY`

---

## Resources

### Icons (SVG in composeResources/drawable/)
- `ic_arrow_back.xml` - Back navigation
- `ic_menu.xml` - Hamburger menu
- `ic_add.xml` - Add action
- `ic_edit.xml` - Edit action
- `ic_delete.xml` - Delete action
- `ic_vehicle.xml`, `ic_driver.xml`, `ic_trip.xml` - Entity icons

### Usage
```kotlin
// Import generated resources
import indusjs_fleet.sharedui.generated.resources.*

// Use in composable
Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Back")
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

### DON'T ❌
- Don't use `mutableStateOf` in ViewModels (use `MviViewModel`)
- Don't call APIs directly from UI (use UseCase → Repository → DataSource)
- Don't hardcode colors (use `MaterialTheme.colorScheme`)
- Don't send DD-MM-YYYY format to API (convert to ISO 8601)
- Don't create new TextField variants (use `FleetTextField`, `FleetDateField`, etc.)
- Don't skip error handling in DataSource calls

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

### Trip States
```
planned → in_progress → completed
    ↓
cancelled
```

### Vehicle States
```
active, maintenance, inactive
```

### User Roles
```
owner, manager, supervisor, driver
```

---

## Screen Types & Templates

### List Screen
- TopAppBar with back, refresh, add actions
- Optional search bar
- Optional filter chips
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

## API Reference

See `Fleet_Management_API_v2.postman_collection.json` for complete API documentation including:
- Authentication (signup, login, forgot password)
- Vehicles CRUD, location, costs
- Drivers CRUD, availability
- Trips CRUD, state management, costs
- Dashboard statistics
- Team management

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

1. [ ] Add domain entity in `domain/entity/{feature}/`
2. [ ] Add repository interface in `domain/repository/{feature}/`
3. [ ] Add use cases in `domain/usecase/{feature}/`
4. [ ] Add DTOs in `data/model/{feature}/`
5. [ ] Add mapper in `data/mapper/{feature}/`
6. [ ] Add data source in `data/datasource/{feature}/`
7. [ ] Add repository impl in `data/repository/{feature}/`
8. [ ] Add Contract (State, Intent, Effect) in `presentation/{feature}/`
9. [ ] Add ViewModel in `presentation/{feature}/`
10. [ ] Add Screen in `presentation/{feature}/`
11. [ ] Create/update FeatureGraph in `di/`
12. [ ] Add route in `navigation/FleetRoute.kt`
13. [ ] Wire up in `FleetNavigation.kt`

