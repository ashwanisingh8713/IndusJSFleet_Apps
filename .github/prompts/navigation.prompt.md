# Navigation Route Template

Add navigation routes for the IndusJS Fleet app using Navigation 3.

## Feature Information
- **Feature Name**: [FEATURE_NAME]
- **Screens**: [LIST_SCREENS]

---

## Navigation 3 Key Concepts

- **Type-safe routes**: Use `@Serializable` data classes/objects
- **NavKey interface**: All routes implement `NavKey`
- **No string-based routes**: Compile-time type safety
- **Automatic back handling**: System back button/gestures work automatically

---

## Route Definition Template

### FleetRoute.kt

```kotlin
package com.indusjs.fleet.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Fleet Management app.
 *
 * Uses Navigation 3 with:
 * - @Serializable for type-safe navigation
 * - NavKey for back stack management
 * - Automatic system back button/gesture handling
 */
@Serializable
sealed interface FleetRoute : NavKey {

    // ==================== Auth Routes ====================

    @Serializable 
    data object Login : FleetRoute
    
    @Serializable 
    data object SignUp : FleetRoute
    
    @Serializable 
    data object ForgotPassword : FleetRoute

    // ==================== Main Routes ====================

    @Serializable 
    data object Dashboard : FleetRoute

    // ==================== User Routes ====================

    @Serializable 
    data object Profile : FleetRoute
    
    @Serializable 
    data object ChangePassword : FleetRoute

    // ==================== {Feature} Routes ====================

    /**
     * {Feature} list screen.
     */
    @Serializable 
    data object {Feature}s : FleetRoute
    
    /**
     * {Feature} detail screen.
     * @param {feature}Id The ID of the {feature} to display
     */
    @Serializable 
    data class {Feature}Detail(val {feature}Id: String) : FleetRoute
    
    /**
     * Create new {feature} screen.
     */
    @Serializable 
    data object Create{Feature} : FleetRoute
    
    /**
     * Edit existing {feature} screen.
     * @param {feature}Id The ID of the {feature} to edit
     */
    @Serializable 
    data class Edit{Feature}(val {feature}Id: String) : FleetRoute

    // ==================== Nested/Related Routes ====================

    /**
     * {Feature} costs screen.
     * @param {feature}Id The ID of the {feature}
     */
    @Serializable 
    data class {Feature}Costs(val {feature}Id: String) : FleetRoute
    
    /**
     * Add cost to {feature}.
     * @param {feature}Id The ID of the {feature}
     */
    @Serializable 
    data class Add{Feature}Cost(val {feature}Id: String) : FleetRoute
}
```

---

## Navigation Host Template

### FleetNavigation.kt

```kotlin
package com.indusjs.fleet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.indusjs.fleet.di.ViewModelProvider
import com.indusjs.fleet.presentation.{feature}.*

/**
 * Main navigation host for the Fleet app.
 * Handles all screen navigation with type-safe routes.
 */
@Composable
fun FleetNavHost(
    viewModelProvider: ViewModelProvider,
    startRoute: FleetRoute = FleetRoute.Login,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(startRoute)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = entryProvider {
            // Auth routes
            entry<FleetRoute.Login> {
                LoginScreen(
                    viewModel = viewModelProvider.getLoginViewModel(),
                    onNavigateToSignUp = { backStack.add(FleetRoute.SignUp) },
                    onNavigateToForgotPassword = { backStack.add(FleetRoute.ForgotPassword) },
                    onLoginSuccess = {
                        backStack.removeAll()
                        backStack.add(FleetRoute.Dashboard)
                    }
                )
            }
            
            entry<FleetRoute.SignUp> {
                SignUpScreen(
                    viewModel = viewModelProvider.getSignUpViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onSignUpSuccess = {
                        backStack.removeAll()
                        backStack.add(FleetRoute.Dashboard)
                    }
                )
            }

            entry<FleetRoute.Dashboard> {
                DashboardScreen(
                    viewModel = viewModelProvider.getDashboardViewModel(),
                    onNavigateToVehicles = { backStack.add(FleetRoute.Vehicles) },
                    onNavigateToDrivers = { backStack.add(FleetRoute.Drivers) },
                    onNavigateToTrips = { backStack.add(FleetRoute.Trips) },
                    onNavigateToProfile = { backStack.add(FleetRoute.Profile) },
                    onNavigateToSettings = { /* TODO */ }
                )
            }

            // {Feature} routes
            entry<FleetRoute.{Feature}s> {
                {Feature}Screen(
                    viewModel = viewModelProvider.get{Feature}ViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateTo{Feature}Detail = { id -> 
                        backStack.add(FleetRoute.{Feature}Detail(id)) 
                    },
                    onNavigateToAdd{Feature} = { 
                        backStack.add(FleetRoute.Create{Feature}) 
                    }
                )
            }

            entry<FleetRoute.{Feature}Detail> { entry ->
                val route = entry.key as FleetRoute.{Feature}Detail
                {Feature}DetailScreen(
                    viewModel = viewModelProvider.get{Feature}DetailViewModel(route.{feature}Id),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEdit = { id -> 
                        backStack.add(FleetRoute.Edit{Feature}(id)) 
                    },
                    onNavigateToCosts = { id ->
                        backStack.add(FleetRoute.{Feature}Costs(id))
                    }
                )
            }

            entry<FleetRoute.Create{Feature}> {
                Create{Feature}Screen(
                    viewModel = viewModelProvider.getCreate{Feature}ViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<FleetRoute.Edit{Feature}> { entry ->
                val route = entry.key as FleetRoute.Edit{Feature}
                Edit{Feature}Screen(
                    viewModel = viewModelProvider.getEdit{Feature}ViewModel(route.{feature}Id),
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<FleetRoute.{Feature}Costs> { entry ->
                val route = entry.key as FleetRoute.{Feature}Costs
                {Feature}CostsScreen(
                    viewModel = viewModelProvider.get{Feature}CostsViewModel(route.{feature}Id),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToAddCost = { id ->
                        backStack.add(FleetRoute.Add{Feature}Cost(id))
                    }
                )
            }
        }
    )
}
```

---

## Navigation Extensions

### NavigationExtensions.kt

```kotlin
package com.indusjs.fleet.navigation

import androidx.navigation3.runtime.NavBackStack

/**
 * Navigation helper extensions.
 */

/**
 * Navigate and clear the back stack (for login/logout flows).
 */
fun NavBackStack.navigateAndClear(route: FleetRoute) {
    removeAll()
    add(route)
}

/**
 * Navigate back safely.
 */
fun NavBackStack.navigateBack(): Boolean {
    return removeLastOrNull() != null
}

/**
 * Replace current screen (no back navigation to previous).
 */
fun NavBackStack.replace(route: FleetRoute) {
    removeLastOrNull()
    add(route)
}

/**
 * Pop to a specific route.
 */
inline fun <reified T : FleetRoute> NavBackStack.popTo() {
    while (current != null && current !is T) {
        removeLastOrNull()
    }
}

/**
 * Check if a route is in the back stack.
 */
inline fun <reified T : FleetRoute> NavBackStack.contains(): Boolean {
    return any { it is T }
}
```

---

## Navigation from ViewModel (via Effects)

```kotlin
// In Contract
sealed interface Effect : UiEffect {
    // Navigation effects
    data object NavigateBack : Effect
    data class NavigateTo{Feature}Detail(val {feature}Id: String) : Effect
    data object NavigateToCreate{Feature} : Effect
    data class NavigateToEdit{Feature}(val {feature}Id: String) : Effect
}

// In Screen - handle effects
HandleEffects(viewModel) { effect ->
    when (effect) {
        is Effect.NavigateBack -> onNavigateBack()
        is Effect.NavigateTo{Feature}Detail -> onNavigateTo{Feature}Detail(effect.{feature}Id)
        is Effect.NavigateToCreate{Feature} -> onNavigateToCreate{Feature}()
        is Effect.NavigateToEdit{Feature} -> onNavigateToEdit{Feature}(effect.{feature}Id)
        // ... other effects
    }
}
```

---

## Deep Linking (Optional)

```kotlin
/**
 * Deep link configuration for {Feature} screens.
 */
object {Feature}DeepLinks {
    const val BASE = "fleet://{feature}"
    
    fun detail(id: String) = "$BASE/$id"
    fun edit(id: String) = "$BASE/$id/edit"
    fun costs(id: String) = "$BASE/$id/costs"
}

// Handling deep links
fun handleDeepLink(uri: String, backStack: NavBackStack) {
    val segments = uri.removePrefix("fleet://").split("/")
    
    when {
        segments[0] == "{feature}" && segments.size == 2 -> {
            backStack.add(FleetRoute.{Feature}Detail(segments[1]))
        }
        segments[0] == "{feature}" && segments.size == 3 && segments[2] == "edit" -> {
            backStack.add(FleetRoute.Edit{Feature}(segments[1]))
        }
        // Add more patterns
    }
}
```

---

## Best Practices

### DO ✅
- Use `data object` for screens without parameters
- Use `data class` for screens with parameters
- Pass navigation callbacks to screens (not NavBackStack)
- Use Effects for ViewModel-triggered navigation
- Keep route names consistent with screen names

### DON'T ❌
- Don't pass `NavBackStack` directly to ViewModels
- Don't hardcode route strings anywhere
- Don't duplicate navigation logic in multiple places
- Don't navigate in `LaunchedEffect` without proper keys

---

## Validation Checklist

- [ ] All routes have `@Serializable` annotation
- [ ] Routes extend `FleetRoute : NavKey`
- [ ] `data object` for no-param routes
- [ ] `data class` for parameterized routes
- [ ] Descriptive parameter names (`{feature}Id`, not `id`)
- [ ] Entry added in `FleetNavHost`
- [ ] ViewModelProvider has getter for new ViewModels
- [ ] Navigation callbacks passed to Screen composables
- [ ] Effects handle navigation in screens

