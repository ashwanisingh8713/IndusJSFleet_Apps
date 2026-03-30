# Navigation 3 — IndusJS Fleet

## Route Definitions

All routes are `@Serializable` data objects/classes in a single sealed interface:

```kotlin
// sharedUI/.../navigation/FleetRoute.kt (THE ONLY PLACE)
@Serializable
sealed interface FleetRoute : NavKey {
    @Serializable data object Dashboard : FleetRoute
    @Serializable data class VehicleDetail(val vehicleId: String) : FleetRoute
    // ... 40+ routes
}
```

## Navigation Wiring

```kotlin
// sharedUI/.../navigation/FleetNavigation.kt
fun fleetEntryProvider(backStack, provider, snackbarHostState): NavEntryDecorator<FleetRoute> {
    return entryProvider<FleetRoute> { route ->
        when (route) {
            is FleetRoute.Vehicles -> entry(route) {
                val vm = rememberViewModel { vehiclesViewModel() }
                VehicleFeatureFacade.VehiclesListEntry(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToDetail = { id -> backStack.add(FleetRoute.VehicleDetail(id)) },
                    onNavigateToAdd = { backStack.add(FleetRoute.AddVehicle) }
                )
            }
            // ... all other routes
        }
    }
}
```

## Key Rules

- **FleetRoute is NEVER imported by feature modules** — only `sharedUI` knows about routes
- Feature modules receive navigation as **lambda callbacks**: `onNavigateBack: () -> Unit`
- Back navigation: `backStack.removeLastOrNull()`
- Forward navigation: `backStack.add(FleetRoute.SomeRoute(args))`
- Route parameters use `String` IDs (e.g., `vehicleId: String`)

## Adding a New Route

1. Add route to `FleetRoute.kt`:
   ```kotlin
   @Serializable data class NewFeature(val id: String) : FleetRoute
   ```
2. Add NavEntry in `FleetNavigation.kt` mapping route → Facade composable
3. Wire navigation lambdas in the entry

## Key Files

- `sharedUI/.../navigation/FleetRoute.kt` — All 40+ route definitions (100 lines)
- `sharedUI/.../navigation/FleetNavigation.kt` — Route → Screen mapping (536 lines)
- `sharedUI/.../App.kt` — NavDisplay setup with auth check (173 lines)

## Common Mistakes

- ❌ Importing `FleetRoute` in a `screen-*` module — use lambda callbacks
- ❌ Using string-based navigation — use type-safe `FleetRoute` sealed classes
- ❌ Passing complex objects as route params — use IDs and load in ViewModel

