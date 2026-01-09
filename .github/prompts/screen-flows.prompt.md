# IndusJS Fleet - Screen Flows & Navigation

## Application Flow Overview

```
                         AUTH FLOW
  ┌──────────┐    ┌──────────┐    ┌────────────────┐    ┌──────────────┐
  │  Splash  │───▶│  Login   │───▶│  Sign Up       │───▶│  Forgot      │
  │  Screen  │    │  Screen  │◀───│  Screen        │    │  Password    │
  └──────────┘    └────┬─────┘    └────────────────┘    └──────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                           DASHBOARD SCREEN                                │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │Fleet Overview│ │Quick Actions│ │Cost Overview│ │   Alerts    │        │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘        │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐        │
│  │  Vehicles   │ │   Trips     │ │   Drivers   │ │  Payments   │        │
│  └──────┬──────┘ └──────┬──────┘ └──────┬──────┘ └─────────────┘        │
└─────────┼───────────────┼───────────────┼────────────────────────────────┘
          │               │               │
          ▼               ▼               ▼
   ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
   │  Vehicles   │ │   Trips     │ │  Drivers    │
   │   Screen    │ │   Screen    │ │   Screen    │
   └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
          │               │               │
     ┌────┴────┐     ┌────┴────┐     ┌────┴────┐
     ▼         ▼     ▼         ▼     ▼         ▼
  ┌──────┐ ┌──────┐┌──────┐ ┌──────┐┌──────┐ ┌──────┐
  │Detail│ │ Add  ││Detail│ │Create││Detail│ │Create│
  │Screen│ │Screen││Screen│ │Screen││Screen│ │Screen│
  └──┬───┘ └──────┘└──┬───┘ └──────┘└──┬───┘ └──────┘
     │               │                │
     ▼               ▼                ▼
  ┌──────┐        ┌──────┐         ┌──────┐
  │ Edit │        │ Edit │         │ Edit │
  │Screen│        │Screen│         │Screen│
  └──────┘        └──────┘         └──────┘
```

---

## Screen Categories

### 1. Authentication Screens
| Screen | Route | Description |
|--------|-------|-------------|
| `LoginScreen` | `FleetRoute.Login` | User login with email/mobile + password |
| `SignUpScreen` | `FleetRoute.SignUp` | New user registration |
| `ForgotPasswordScreen` | `FleetRoute.ForgotPassword` | Password reset request |

### 2. Dashboard Screen
| Screen | Route | Description |
|--------|-------|-------------|
| `DashboardScreen` | `FleetRoute.Dashboard` | Main overview with fleet stats, alerts, quick actions |

**Dashboard Sections:**
1. Fleet Overview - Vehicle/Driver/Trip counts with status bar
2. Quick Actions - Navigation buttons to main features
3. Cost Overview - Expenses, Profit/Loss with filter (Today/Weekly/Monthly)
4. Pending Payments - Outstanding payment list
5. Vehicle Status - Vehicle availability breakdown
6. Trips Status - Active/Planned/Completed trips
7. Alerts - Document expiry, license expiry, missing documents
8. Drivers - Driver availability status

### 3. Vehicle Screens
| Screen | Route | Description |
|--------|-------|-------------|
| `VehiclesScreen` | `FleetRoute.Vehicles` | List all vehicles with search/filter |
| `VehicleDetailScreen` | `FleetRoute.VehicleDetail(id)` | Tabs: Overview, Trips, Routes, Documents, Costs |
| `AddVehicleScreen` | `FleetRoute.AddVehicle` | Create new vehicle with documents |
| `EditVehicleScreen` | (inline in detail) | Edit within detail screen |

### 4. Driver Screens
| Screen | Route | Description |
|--------|-------|-------------|
| `DriversScreen` | `FleetRoute.Drivers` | List all drivers with status filters |
| `DriverDetailScreen` | `FleetRoute.DriverDetail(id)` | Driver details with license info |
| `CreateDriverScreen` | `FleetRoute.CreateDriver` | Create new driver |
| `EditDriverScreen` | (inline in detail) | Edit within detail screen |

### 5. Trip Screens
| Screen | Route | Description |
|--------|-------|-------------|
| `TripsScreen` | `FleetRoute.Trips` | List all trips with state filters |
| `TripDetailScreen` | `FleetRoute.TripDetail(id)` | Trip details with schedule, cargo, costs |
| `CreateTripScreen` | `FleetRoute.CreateTrip` | Create trip with Google Places integration |
| `EditTripScreen` | (inline in detail) | Edit within detail screen |

### 6. Cost Screens
| Screen | Route | Description |
|--------|-------|-------------|
| `TripCostEntryScreen` | `FleetRoute.TripCostEntry` | Add trip costs (fuel, toll, parking, etc.) |
| `MaintenanceCostEntryScreen` | `FleetRoute.MaintenanceCostEntry` | Add vehicle maintenance costs |

### 7. User/Team Screens
| Screen | Route | Description |
|--------|-------|-------------|
| `ProfileScreen` | `FleetRoute.Profile` | User profile management |
| `ChangePasswordScreen` | `FleetRoute.ChangePassword` | Change user password |
| `TeamListScreen` | `FleetRoute.TeamList` | List team members |
| `CreateTeamMemberScreen` | `FleetRoute.CreateTeamMember` | Add new team member |

### 8. Maps Screen
| Screen | Route | Description |
|--------|-------|-------------|
| `MapsScreen` | `FleetRoute.Maps` | Live vehicle tracking on map |

---

## Screen Structure Templates

### List Screen Structure
```
┌─────────────────────────────────────┐
│ [←] Title                    [⟲][+] │  ← TopAppBar with back, refresh, add
├─────────────────────────────────────┤
│ 🔍 Search...                        │  ← Search bar (optional)
├─────────────────────────────────────┤
│ [Filter1] [Filter2] [Filter3]       │  ← Filter chips (optional)
├─────────────────────────────────────┤
│ ┌─────────────────────────────────┐ │
│ │ 🚛 Item 1                    ➜  │ │  ← LazyColumn items
│ │    Status • Subtitle            │ │
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │ 🚛 Item 2                    ➜  │ │
│ │    Status • Subtitle            │ │
│ └─────────────────────────────────┘ │
│                                     │
│         [Empty State / Loading]     │  ← Handle loading/empty/error
└─────────────────────────────────────┘
│               [FAB +]               │  ← FloatingActionButton
└─────────────────────────────────────┘
```

### Detail Screen Structure
```
┌─────────────────────────────────────┐
│ [←] Title                    [✎][⋮] │  ← TopAppBar with edit, more actions
├─────────────────────────────────────┤
│                                     │
│ ┌─────────────────────────────────┐ │
│ │      Header / Hero Section      │ │  ← Main entity info
│ └─────────────────────────────────┘ │
│                                     │
│ [Tab 1] [Tab 2] [Tab 3] [Tab 4]     │  ← Tabs (for complex entities)
├─────────────────────────────────────┤
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Section Title                   │ │
│ │ ────────────────────────────    │ │
│ │ Label: Value                    │ │
│ │ Label: Value                    │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Section Title                   │ │
│ │ ────────────────────────────    │ │
│ │ [Action Button]                 │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### Form Screen Structure
```
┌─────────────────────────────────────┐
│ [←] Create/Edit Title        [Save] │  ← TopAppBar with save action
├─────────────────────────────────────┤
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Section 1: Basic Info           │ │
│ │ ────────────────────────────    │ │
│ │ ┌─────────────────────────────┐ │ │
│ │ │ Label                       │ │ │  ← FleetTextField
│ │ │ [Input Field.............. ]│ │ │
│ │ └─────────────────────────────┘ │ │
│ │ ┌─────────────────────────────┐ │ │
│ │ │ Label                       │ │ │
│ │ │ [Dropdown ▼............... ]│ │ │  ← Dropdown selection
│ │ └─────────────────────────────┘ │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Section 2: Schedule             │ │
│ │ ────────────────────────────    │ │
│ │ [Date Field] [Time Field]       │ │  ← FleetDateField, FleetTimeField
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ [       Submit Button         ] │ │  ← FleetPrimaryButton
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

---

## Navigation Flow Patterns

### 1. List → Detail → Edit Flow
```kotlin
// Navigate from list to detail
onNavigateToVehicleDetail = { vehicleId ->
    navController.navigate(FleetRoute.VehicleDetail(vehicleId))
}

// Navigate from detail to edit (inline or separate screen)
onNavigateToEdit = {
    // Toggle edit mode in detail screen OR
    navController.navigate(FleetRoute.EditVehicle(vehicleId))
}
```

### 2. Dashboard → Feature Flow
```kotlin
DashboardScreen(
    onNavigateToVehicles = { navController.navigate(FleetRoute.Vehicles) },
    onNavigateToDrivers = { navController.navigate(FleetRoute.Drivers) },
    onNavigateToTrips = { navController.navigate(FleetRoute.Trips) },
    onNavigateToMaps = { navController.navigate(FleetRoute.Maps) }
)
```

### 3. Create with Result
```kotlin
CreateDriverScreen(
    onNavigateBack = { navController.popBackStack() },
    onDriverCreated = { driverId ->
        navController.popBackStack()
        // Optionally navigate to detail
        navController.navigate(FleetRoute.DriverDetail(driverId))
    }
)
```

---

## State Management Pattern

### Common Screen States
```kotlin
when {
    state.isLoading -> LoadingContent(message = "Loading...")
    state.error != null -> ErrorContent(
        error = state.error,
        screenContext = FleetErrorContext.FEATURE,
        onRetry = { viewModel.sendIntent(Intent.LoadData) }
    )
    state.items.isEmpty() -> EmptyContent(
        iconRes = Res.drawable.ic_feature,
        title = "No items yet",
        message = "Add your first item to get started",
        actionLabel = "Add Item",
        onAction = { viewModel.sendIntent(Intent.AddItem) }
    )
    else -> ContentList(items = state.items)
}
```

---

## Input Components Usage

| Component | Use Case | Format | Example |
|-----------|----------|--------|---------|
| `FleetTextField` | Standard text | Any text | Name, description |
| `FleetDateField` | Date input | DD-MM-YYYY | "04-01-2026" |
| `FleetTimeField` | Time input | HH:MM (24hr) | "14:30" |
| `FleetMobileField` | Mobile number | 10 digits | "9876543210" |
| `FleetEmailField` | Email | Valid email | "user@example.com" |
| `FleetPasswordField` | Password | With toggle | Hidden input |
| `FleetDropdown` | Selection | From options | Vehicle type |
| `FleetLocationField` | Location | Google Places | "Pune, Maharashtra" |

---

## Error Handling by Screen

| Screen Context | Common Errors |
|----------------|---------------|
| `DASHBOARD` | "Unable to load dashboard data" |
| `VEHICLES` | "Unable to load vehicles", "Vehicle not found" |
| `DRIVERS` | "Unable to load drivers", "Driver not found" |
| `TRIPS` | "Unable to load trips", "Trip creation failed" |
| `COSTS` | "Unable to load costs", "Cost entry failed" |
| `AUTH` | "Invalid credentials", "Account not found" |

---

## Empty State Guidelines

| Screen | Icon | Title | Message | Action |
|--------|------|-------|---------|--------|
| Vehicles | ic_vehicle | "No vehicles yet" | "Add your first vehicle" | "Add Vehicle" |
| Drivers | ic_driver | "No drivers yet" | "Add your first driver" | "Add Driver" |
| Trips | ic_trip | "No trips yet" | "Create your first trip" | "Create Trip" |
| Costs | ic_cost | "No costs recorded" | "Add your first cost entry" | "Add Cost" |
| Alerts | ic_check | "All clear!" | "No alerts at this time" | None |

