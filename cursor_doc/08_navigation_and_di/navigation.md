# Navigation System

## Technology

**Navigation 3** (`org.jetbrains.androidx.navigation3:navigation3-ui` v1.1.0-alpha01) — type-safe, Compose-native navigation.

## Route Definitions

**File:** `sharedUI/.../navigation/FleetRoute.kt`

```kotlin
@Serializable
sealed interface FleetRoute : NavKey {
    @Serializable data object Dashboard : FleetRoute
    @Serializable data class VehicleDetail(val vehicleId: String) : FleetRoute
    // ... 41 total routes
}
```

All routes are `@Serializable` and implement `NavKey` for type-safe navigation.

## Navigation Host

**File:** `sharedUI/.../navigation/FleetNavigation.kt`

`fleetEntryProvider` is a function `(FleetRoute) -> NavEntry<FleetRoute>` that maps each route to its screen:

```kotlin
fun fleetEntryProvider(
    backStack: NavBackStack<FleetRoute>,
    viewModelProvider: ViewModelProvider,
    // ... platform callbacks
): (FleetRoute) -> NavEntry<FleetRoute> = { route ->
    when (route) {
        is FleetRoute.Dashboard -> NavEntry(route) {
            val vm = rememberViewModel { viewModelProvider.dashboardViewModel() }
            DashboardFeatureFacade.DashboardEntry(
                viewModel = vm,
                onNavigateToVehicles = { backStack.add(FleetRoute.Vehicles) },
                onNavigateToDrivers = { backStack.add(FleetRoute.Drivers) },
                // ... 18+ navigation callbacks
            )
        }
        is FleetRoute.VehicleDetail -> NavEntry(route) {
            val vm = rememberViewModel { viewModelProvider.vehicleDetailViewModel(route.vehicleId) }
            VehicleFeatureFacade.VehicleDetailEntry(vm, ...)
        }
        // ... all 41 routes
    }
}
```

## Navigation Extensions

**File:** `sharedUI/.../navigation/NavigationExtensions.kt`

```kotlin
fun NavBackStack<FleetRoute>.navigateAndClear(route: FleetRoute)
    // Clears entire stack, pushes new route

fun NavBackStack<FleetRoute>.popAndNavigate(route: FleetRoute)
    // Pops current, pushes new

fun NavBackStack<FleetRoute>.removeLastOrNull()
    // Safe pop
```

## App.kt Wiring

```kotlin
@Composable
fun App(...) {
    // 1. Determine initial route (Onboarding / Dashboard / Login)
    // 2. Create NavBackStack<FleetRoute>(initialRoute)
    // 3. Collect AuthenticationManager.authEvents → navigateAndClear(Login)
    // 4. Render NavDisplay with fleetEntryProvider(...)
}
```

## Feature Module Isolation

Feature modules (screen-*) are **completely isolated from navigation**:
- They never import `FleetRoute`
- They never reference `NavBackStack`
- Navigation is expressed as **lambdas** passed through facades:
  ```kotlin
  onNavigateToVehicleDetail: (vehicleId: String) -> Unit
  onNavigateBack: () -> Unit
  ```
- `FleetNavigation.kt` connects these lambdas to actual `backStack.add(FleetRoute.XYZ)` calls

## Shared ViewModel Pattern (Finance)

Vehicle Finance uses a shared ViewModel across 4 screens:

```kotlin
// In FleetNavigation.kt
is FleetRoute.VehicleFinance -> NavEntry(route) {
    val vm = rememberSharedViewModel("vehicle_finance_flow") {
        viewModelProvider.vehicleFinanceViewModel()
    }
    FinanceFeatureFacade.VehicleFinanceListEntry(vm, ...)
}

is FleetRoute.VehicleFinanceDetail -> NavEntry(route) {
    val vm = rememberSharedViewModel("vehicle_finance_flow") {
        viewModelProvider.vehicleFinanceViewModel()
    }
    FinanceFeatureFacade.VehicleFinanceDetailEntry(vm, route.vehicleId, ...)
}
```

When leaving finance flow: `clearSharedViewModel("vehicle_finance_flow")`

## Initial Route Decision

```
App starts
  → Check hasCompletedOnboarding() (multiplatform-settings)
    → false → Onboarding
    → true → Check userRepository.isLoggedIn()
      → false → Login
      → true → Dashboard
```

## Auth Event Handling

```kotlin
LaunchedEffect(backStack) {
    AuthenticationManager.authEvents.collect { event ->
        when (event) {
            is Unauthorized, SessionExpired, LoggedOut ->
                backStack.navigateAndClear(FleetRoute.Login)
        }
        // Optional snackbar with event message
    }
}
```
