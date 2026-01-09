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

| Field Type | Display Format | API Request Format |
|------------|----------------|-------------------|
| Date | `DD-MM-YYYY` | ISO 8601: `2026-01-04T00:00:00Z` |
| Time | `HH:MM` (24hr) | Combined with date in ISO 8601 |

**Conversion Example:**
```kotlin
// UI input: "04-01-2026" + "14:30"
// API request: "2026-01-04T14:30:00Z"

fun toIsoDateTime(date: String, time: String): String {
    // date = "04-01-2026" (DD-MM-YYYY)
    // time = "14:30" (HH:MM)
    val (day, month, year) = date.split("-")
    return "${year}-${month}-${day}T${time}:00Z"
}
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
sealed class FleetException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NotAuthenticatedException(message: String = "Not authenticated") : FleetException(message)
class ApiException(message: String, val code: Int? = null) : FleetException(message)
class NetworkException(message: String = "Network unavailable") : FleetException(message)
class ValidationException(message: String, val field: String? = null) : FleetException(message)
```

### ErrorHandler Context

```kotlin
ErrorContent(
    error = state.error,
    screenContext = ErrorHandler.ScreenContext.VEHICLES,
    onRetry = { viewModel.sendIntent(Intent.LoadVehicles) }
)
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
import indusjs_fleet.sharedui.generated.resources.*

Icon(
    painter = painterResource(Res.drawable.ic_arrow_back),
    contentDescription = "Back"
)
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

## API Reference

See `Fleet_Management_API_v2.postman_collection.json` for complete API documentation including:
- Authentication (signup, login, forgot password)
- Vehicles CRUD, location, costs
- Drivers CRUD, availability
- Trips CRUD, state management, costs
- Dashboard statistics
- Team management

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

