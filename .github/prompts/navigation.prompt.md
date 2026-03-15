# Navigation Guide

> Use this prompt when adding a new screen to Navigation 3.

## Adding a Route

### 1. Add to `FleetRoute.kt`

```kotlin
@Serializable
sealed interface FleetRoute : NavKey {
    // ...existing routes...

    // ===== My Feature Routes =====
    @Serializable data object MyList : FleetRoute
    @Serializable data class MyDetail(val myId: String) : FleetRoute
    @Serializable data object CreateMy : FleetRoute
}
```

**Rules:**
- Use `data object` for routes without parameters
- Use `data class` for routes with parameters (always `String` type for IDs)
- Optional parameters use `val param: String? = null`

### 2. Add NavEntry in `FleetNavigation.kt`

```kotlin
@Composable
fun fleetEntryProvider(
    backStack: NavBackStack<FleetRoute>,
    // ...existing params...
): (FleetRoute) -> NavEntry<FleetRoute> = { route ->
    when (route) {
        // ...existing routes...

        is FleetRoute.MyList -> NavEntry(route) {
            val viewModel = rememberViewModel { myListViewModel() }
            MyListScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToDetail = { id -> backStack.add(FleetRoute.MyDetail(id)) },
                onNavigateToCreate = { backStack.add(FleetRoute.CreateMy) }
            )
        }

        is FleetRoute.MyDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { myDetailViewModel() }
            MyDetailScreen(
                viewModel = viewModel,
                myId = route.myId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CreateMy -> NavEntry(route) {
            val viewModel = rememberViewModel { createMyViewModel() }
            CreateMyScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
    }
}
```

## Navigation Patterns

### Basic Navigation
```kotlin
backStack.add(FleetRoute.MyDetail(id))       // Push
backStack.removeLastOrNull()                   // Pop (back)
```

### Navigate and Clear Stack (Login/Logout)
```kotlin
backStack.navigateAndClear(FleetRoute.Dashboard)  // Clear all, go to Dashboard
backStack.navigateAndClear(FleetRoute.Login)       // Clear all, go to Login
```

### Shared ViewModel (Multi-screen flows)
Used when related screens share state (e.g., Vehicle Finance):

```kotlin
is FleetRoute.VehicleFinanceDetail -> NavEntry(route) {
    val viewModel = rememberSharedViewModel("finance_${route.vehicleId}") {
        vehicleFinanceViewModel()
    }
    VehicleFinanceDetailScreen(viewModel = viewModel, ...)
}

is FleetRoute.EmiPaymentHistory -> NavEntry(route) {
    val viewModel = rememberSharedViewModel("finance_${route.vehicleId}") {
        vehicleFinanceViewModel()
    }
    EmiPaymentHistoryScreen(viewModel = viewModel, ...)
}
```

### Passing Callbacks from Dashboard (Quick Actions)
```kotlin
is FleetRoute.Dashboard -> NavEntry(route) {
    val viewModel = rememberViewModel { dashboardViewModel() }
    DashboardScreen(
        viewModel = viewModel,
        onNavigateToVehicles = { backStack.add(FleetRoute.Vehicles) },
        onNavigateToDrivers = { backStack.add(FleetRoute.Drivers) },
        onNavigateToTrips = { backStack.add(FleetRoute.Trips) },
        onNavigateToAddCost = { backStack.add(FleetRoute.TripCostEntry()) },
        onNavigateToProfile = { backStack.add(FleetRoute.Profile) },
        // ...all navigation callbacks
    )
}
```

## ViewModel Creation

- `rememberViewModel { factoryMethod() }` — standard, creates per-composition
- `rememberSharedViewModel(key) { factoryMethod() }` — shared across screens by key
- `clearSharedViewModel(key)` — cleanup when leaving a shared flow entirely

