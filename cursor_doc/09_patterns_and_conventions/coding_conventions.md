# Coding Conventions & Best Practices

## Strict Rules

### Class Size Enforcement
- Every class file **must not exceed 500 lines**
- If exceeded, split into focused classes:
  ```
  ❌ VehicleDetailViewModel.kt (650 lines)
  ✅ VehicleDetailViewModel.kt + VehicleDetailDataLoader.kt
     + VehicleDetailStateReducer.kt + VehicleDocumentHandler.kt
  ```

### Architecture Compliance
- Every module with business logic → **Clean Architecture**
- Every module with UI/ViewModel → **MVI pattern**
- DI → **Metro annotations** (even if wiring is manual)
- All async operations → **Result<T>** from `ijs-error-lib`
- All background work → **DispatcherProvider** (never `Dispatchers.IO` directly)

## DO (Best Practices)

| Practice | Example |
|----------|---------|
| Use `@Inject` on class, not constructor | `@Inject class MyRepo(...)` |
| Use `Result<T>` for all async ops | `Flow<Result<List<Vehicle>>>` |
| Use `updateState { copy(...) }` | Immutable state updates |
| Use `sendEffect()` for one-time events | Navigation, snackbars |
| Use core UI components | `FleetTextField`, `FleetDateField`, etc. |
| Use `@SerialName` with snake_case for DTOs | `@SerialName("vehicle_id") val vehicleId` |
| Handle loading, error, empty in every screen | `ScreenContent(isLoading, error, data)` |
| Use `DispatcherProvider` for background work | `withContext(dispatcherProvider.io)` |
| Place shared DTOs in `ijs-core-lib` | Cross-feature cost/history DTOs |
| Use `FeatureRepositoryFactory` for repo wiring | In `sharedUI/di/` |
| Use `FleetDateTime` for date operations | Never raw string manipulation |
| Use `MaterialTheme.colorScheme` for colors | Never hardcode hex values |

## DON'T (Anti-Patterns)

| Anti-Pattern | Why |
|-------------|-----|
| `mutableStateOf` in ViewModels | Use `MviViewModel` instead |
| Direct API calls from UI | Go through UseCase → Repository → DataSource |
| Hardcoded colors | Use `MaterialTheme.colorScheme` |
| DD-MM-YYYY to API without conversion | Convert to ISO 8601 for trip scheduling |
| New TextField variants | Use existing `FleetTextField`, `FleetDateField` |
| Skip error handling in DataSource | Always catch and wrap in `Result.Error` |
| Set SDK values in module build files | Use `fleet-android-conventions.gradle` |
| Domain/data logic in `sharedUI` | Put in appropriate feature-lib module |
| `Dispatchers.IO` directly | Use `DispatcherProvider.io` |
| Comments narrating code | Only explain non-obvious intent/constraints |

## DTO Conventions

```kotlin
@Serializable
data class TripDto(
    @SerialName("id") val id: Int,
    @SerialName("vehicle_id") val vehicleId: Int,     // snake_case for API
    @SerialName("scheduled_date") val scheduledDate: String? = null,
    // All fields with @SerialName, nullable with defaults
)
```

## API Response Wrapper

```kotlin
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T? = null
)
```

## Screen Templates

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

## Resource Access

```kotlin
// Icons
import indusjs_fleet.ijs_ui_components_lib.generated.resources.*
Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Back")

// Strings (if using string resources)
stringResource(Res.string.app_name)
```

## Theme Access

```kotlin
// Colors — ALWAYS from MaterialTheme
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.surfaceContainerLow

// Status colors — from FleetColors
FleetColors.vehicleActive
FleetColors.tripCompleted

// Typography
MaterialTheme.typography.headlineSmall
MaterialTheme.typography.bodyMedium
```
