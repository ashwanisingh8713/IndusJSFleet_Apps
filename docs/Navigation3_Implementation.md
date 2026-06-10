# Navigation 3 Implementation Guide

> **IndusJS Fleet Management App** - Kotlin Multiplatform  
> Last Updated: December 27, 2025

---

## Overview

This app uses **JetBrains Navigation 3** (`org.jetbrains.androidx.navigation3:navigation3-ui`) for navigation management across Android and iOS platforms.

### Features
| Feature | Status |
|---------|--------|
| Automatic back stack management | ✅ |
| Android system back button | ✅ |
| iOS swipe-to-go-back gesture | ✅ |
| Type-safe routes with `@Serializable` | ✅ |
| Session expiry auto-redirect | ✅ |
| Predictive back (Android 13+) | ✅ |

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         App.kt                                  │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  val backStack = remember { NavBackStack(FleetRoute.Login)}│  │
│  │                              ↓                             │  │
│  │  NavDisplay(                                               │  │
│  │      backStack = backStack,                                │  │
│  │      entryProvider = fleetEntryProvider(backStack, ...),   │  │
│  │      onBack = { backStack.removeLastOrNull() }             │  │
│  │  )                                                         │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   FleetNavigation.kt                            │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  fun fleetEntryProvider(backStack) = { route ->           │  │
│  │      when (route) {                                       │  │
│  │          is FleetRoute.Login -> NavEntry(route) {         │  │
│  │              LoginScreen(onLoginSuccess = {               │  │
│  │                  backStack.navigateAndClear(Dashboard)    │  │
│  │              })                                           │  │
│  │          }                                                │  │
│  │          is FleetRoute.Dashboard -> NavEntry(route) {...} │  │
│  │          ...                                              │  │
│  │      }                                                    │  │
│  │  }                                                        │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## File Structure

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── App.kt                          # Main composable with NavDisplay
└── navigation/
    ├── FleetRoute.kt               # Route definitions (sealed interface)
    ├── FleetNavigation.kt          # Entry provider (route → screen mapping)
    └── NavigationExtensions.kt     # Helper extensions
```

---

## Routes (18 Screens)

### Route Definitions

All routes are defined in `FleetRoute.kt` as a sealed interface implementing `NavKey`:

```kotlin
@Serializable
sealed interface FleetRoute : NavKey {
    // Auth
    @Serializable data object Login : FleetRoute
    @Serializable data object SignUp : FleetRoute
    @Serializable data object ForgotPassword : FleetRoute
    
    // Main
    @Serializable data object Dashboard : FleetRoute
    
    // User
    @Serializable data object Profile : FleetRoute
    @Serializable data object ChangePassword : FleetRoute
    
    // Vehicles
    @Serializable data object Vehicles : FleetRoute
    @Serializable data class VehicleDetail(val vehicleId: String) : FleetRoute
    @Serializable data object AddVehicle : FleetRoute
    
    // Drivers
    @Serializable data object Drivers : FleetRoute
    @Serializable data class DriverDetail(val driverId: String) : FleetRoute
    @Serializable data object CreateDriver : FleetRoute
    
    // Trips
    @Serializable data object Trips : FleetRoute
    @Serializable data class TripDetail(val tripId: String) : FleetRoute
    @Serializable data object CreateTrip : FleetRoute
    
    // Other
    @Serializable data object Maps : FleetRoute
    @Serializable data object TeamList : FleetRoute
    @Serializable data object CreateTeamMember : FleetRoute
}
```

### Route Summary Table

| # | Screen | Route | Parameters | Category |
|---|--------|-------|------------|----------|
| 1 | LoginScreen | `Login` | - | Auth |
| 2 | SignUpScreen | `SignUp` | - | Auth |
| 3 | ForgotPasswordScreen | `ForgotPassword` | - | Auth |
| 4 | DashboardScreen | `Dashboard` | - | Main |
| 5 | ProfileScreen | `Profile` | - | User |
| 6 | ChangePasswordScreen | `ChangePassword` | - | User |
| 7 | VehiclesScreen | `Vehicles` | - | Vehicles |
| 8 | VehicleDetailScreen | `VehicleDetail` | `vehicleId: String` | Vehicles |
| 9 | AddVehicleScreen | `AddVehicle` | - | Vehicles |
| 10 | DriversScreen | `Drivers` | - | Drivers |
| 11 | DriverDetailScreen | `DriverDetail` | `driverId: String` | Drivers |
| 12 | CreateDriverScreen | `CreateDriver` | - | Drivers |
| 13 | TripsScreen | `Trips` | - | Trips |
| 14 | TripDetailScreen | `TripDetail` | `tripId: String` | Trips |
| 15 | CreateTripScreen | `CreateTrip` | - | Trips |
| 16 | MapsScreen | `Maps` | - | Other |
| 17 | TeamListScreen | `TeamList` | - | Other |
| 18 | CreateTeamMemberScreen | `CreateTeamMember` | - | Other |

---

## Navigation Flow Diagram

```
                                    ┌─────────────┐
                                    │    Login    │◄──────────────────────┐
                                    └──────┬──────┘                       │
                                           │                              │
                    ┌──────────────────────┼──────────────────────┐       │
                    │                      │                      │       │
                    ▼                      ▼                      ▼       │
             ┌──────────┐          ┌──────────────┐       ┌────────────┐  │
             │  SignUp  │          │  Dashboard   │       │  Forgot    │  │
             └────┬─────┘          │    (Home)    │       │  Password  │  │
                  │                └──────┬───────┘       └────────────┘  │
                  │                       │                               │
                  │    ┌─────────┬────────┼────────┬─────────┬───────────┤
                  │    │         │        │        │         │           │
                  │    ▼         ▼        ▼        ▼         ▼           │
                  │ ┌────────┐┌────────┐┌────┐┌────────┐┌─────────┐      │
                  │ │Vehicles││ Drivers││Trips││  Maps  ││  Team   │      │
                  │ └───┬────┘└───┬────┘└──┬─┘└────────┘└────┬────┘      │
                  │     │         │        │                 │           │
                  │ ┌───┴───┐ ┌───┴───┐ ┌──┴──┐         ┌────┴────┐     │
                  │ │       │ │       │ │     │         │         │     │
                  │ ▼       ▼ ▼       ▼ ▼     ▼         ▼         │     │
                  │┌───┐ ┌────┐┌───┐┌────┐┌───┐┌────┐ ┌────┐      │     │
                  ││Add│ │Det-││Cre││Det-││Cre││Det-│ │Cre-│      │     │
                  ││Veh││ ail ││ate││ail ││ate││ail │ │ate │      │     │
                  │└───┘ └────┘└───┘└────┘└───┘└────┘ └────┘      │     │
                  │                                               │     │
                  │                                   ┌───────────┘     │
                  │                                   │                 │
                  │                                   ▼                 │
                  │                            ┌─────────┐              │
                  │                            │ Profile │──────────────┘
                  │                            └────┬────┘   (Logout)
                  │                                 │
                  │                                 ▼
                  │                          ┌──────────┐
                  └──────────────────────────│ Change   │
                       (on success)          │ Password │
                                             └──────────┘
```

---

## Navigation Patterns

### 1. Basic Navigation (Push)
```kotlin
// Navigate to a new screen (adds to back stack)
backStack.add(FleetRoute.Vehicles)

// Navigate with parameters
backStack.add(FleetRoute.VehicleDetail(vehicleId = "abc123"))
```

### 2. Back Navigation (Pop)
```kotlin
// Go back to previous screen
backStack.removeLastOrNull()
```

### 3. Clear & Navigate (Auth Flows)
```kotlin
// Clear entire stack and navigate (login/logout)
backStack.navigateAndClear(FleetRoute.Dashboard)
backStack.navigateAndClear(FleetRoute.Login)
```

### 4. Pop & Navigate (Create → Detail)
```kotlin
// After creating entity, pop create screen and go to detail
backStack.popAndNavigate(FleetRoute.VehicleDetail(vehicleId))
```

### 5. Single Top (Prevent Duplicates)
```kotlin
// Only navigate if not already on this screen
backStack.navigateSingleTop(FleetRoute.Vehicles)
```

---

## Navigation Extensions

Defined in `NavigationExtensions.kt`:

```kotlin
// Clear stack and navigate (auth flows)
fun <T : NavKey> NavBackStack<T>.navigateAndClear(route: T) {
    clear()
    add(route)
}

// Prevent duplicate screens
fun <T : NavKey> NavBackStack<T>.navigateSingleTop(route: T) {
    if (lastOrNull() != route) {
        add(route)
    }
}

// Pop and replace (after create operations)
fun <T : NavKey> NavBackStack<T>.popAndNavigate(route: T) {
    removeLastOrNull()
    add(route)
}
```

---

## Session Expiry Handling

The app automatically handles session expiry (401 errors) in `App.kt`:

```kotlin
LaunchedEffect(Unit) {
    AuthenticationManager.authEvents.collect { event ->
        when (event) {
            is AuthenticationEvent.Unauthorized,
            is AuthenticationEvent.SessionExpired -> {
                backStack.navigateAndClear(FleetRoute.Login)
                snackbarHostState.showSnackbar("Session expired. Please log in again.")
            }
            is AuthenticationEvent.LoggedOut -> {
                backStack.navigateAndClear(FleetRoute.Login)
            }
        }
    }
}
```

---

## Dependencies

### Version Catalog (`gradle/libs.versions.toml`)
```toml
[versions]
androidx-nav3 = "1.1.0-alpha01"

[libraries]
compose-nav3 = { module = "org.jetbrains.androidx.navigation3:navigation3-ui", version.ref = "androidx-nav3" }
```

### Build Script (`sharedUI/build.gradle.kts`)
```kotlin
commonMain.dependencies {
    implementation(libs.compose.nav3)
}
```

---

## Key Imports

```kotlin
// Navigation 3 Runtime
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey

// Navigation 3 UI
import androidx.navigation3.ui.NavDisplay
```

---

## Adding a New Screen

1. **Add Route** in `FleetRoute.kt`:
   ```kotlin
   @Serializable data object NewScreen : FleetRoute
   // OR with parameters:
   @Serializable data class NewDetail(val id: String) : FleetRoute
   ```

2. **Add Entry** in `FleetNavigation.kt`:
   ```kotlin
   is FleetRoute.NewScreen -> NavEntry(route) {
       val viewModel = rememberViewModel { newScreenViewModel() }
       NewScreen(
           viewModel = viewModel,
           onNavigateBack = { backStack.removeLastOrNull() }
       )
   }
   ```

3. **Add Navigation Call** from source screen:
   ```kotlin
   onNavigateToNewScreen = { backStack.add(FleetRoute.NewScreen) }
   ```

---

## Testing Checklist

### Android
- [ ] Device back button navigates to previous screen
- [ ] Back button on Dashboard exits app
- [ ] Back button on Login exits app
- [ ] Predictive back gesture works (Android 13+)
- [ ] Deep navigation maintains correct stack

### iOS
- [ ] Swipe-from-left edge navigates back
- [ ] Navigation maintains correct back stack
- [ ] Deep navigation works correctly

### Auth Flows
- [ ] Login clears stack → Dashboard
- [ ] Logout clears stack → Login
- [ ] Session expiry (401) → Login with message
- [ ] SignUp success → Dashboard

### Create Flows
- [ ] Create Vehicle → Vehicle Detail (stack: Vehicles → Detail)
- [ ] Create Driver → Driver Detail (stack: Drivers → Detail)
- [ ] Create Trip → Trip Detail (stack: Trips → Detail)

