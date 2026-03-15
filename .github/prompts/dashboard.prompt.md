# Dashboard Feature

> Use this prompt when working on the main dashboard screen.

## Screen & Route

| Screen | Route | Description |
|--------|-------|-------------|
| `DashboardScreen` | `Dashboard` | Fleet overview hub — hamburger menu for navigation |

## Dashboard Sections

### 1. Fleet Overview (All Roles)
- Total vehicles, active/inactive/on_route counts
- Total drivers, active/inactive/on_route counts
- Total trips by status (planned/on_route/completed)

### 2. Cost Overview (All Roles)
- Today's costs
- This week's costs
- This month's costs
- Cost by type (trip/maintenance/driver)

### 3. Financial Summary (Owner/GM Only)
- Total revenue
- Total expenses
- Net profit/loss
- Profit margin percentage

### 4. Pending Payments (Owner/GM Only)
- Outstanding payment amounts
- Recent pending payments list

### 5. Alerts (All Roles)
- Document expiry warnings (insurance, fitness, permit, PUC)
- Driver license expiry warnings
- Maintenance due alerts

### 6. Quick Actions (Role-filtered)
- Add Trip Cost
- Add Maintenance Cost
- Add Driver Cost
- Create Trip
- Add Vehicle
- Add Driver

## API Endpoints

```
GET /dashboard                    → Fleet counts, summary stats
GET /dashboard/cost-overview      → Cost breakdown (supports period filter)
GET /dashboard/pending-payments   → Outstanding payments
GET /dashboard/alerts-status      → Document/license expiry alerts
GET /dashboard/vehicle-status     → Vehicle status distribution
GET /dashboard/trips-status       → Trip status distribution
GET /dashboard/drivers-status     → Driver status distribution
```

## Offline Caching

Dashboard data is cached in `FleetDatabase` via `DashboardLocalDataSourceImpl`. On app open, cached data displays immediately while a background refresh fetches fresh data from API.

## Navigation from Dashboard

The dashboard uses a hamburger menu drawer for primary navigation to all features:

```kotlin
DashboardScreen(
    viewModel = viewModel,
    onNavigateToVehicles = { backStack.add(FleetRoute.Vehicles) },
    onNavigateToDrivers = { backStack.add(FleetRoute.Drivers) },
    onNavigateToTrips = { backStack.add(FleetRoute.Trips) },
    onNavigateToCustomers = { backStack.add(FleetRoute.Customers) },
    onNavigateToPayments = { backStack.add(FleetRoute.Payments) },
    onNavigateToReports = { backStack.add(FleetRoute.Reports) },
    onNavigateToTeam = { backStack.add(FleetRoute.TeamList) },
    onNavigateToFinance = { backStack.add(FleetRoute.VehicleFinance) },
    onNavigateToMaps = { backStack.add(FleetRoute.Maps) },
    onNavigateToAlerts = { backStack.add(FleetRoute.AlertsList) },
    onNavigateToProfile = { backStack.add(FleetRoute.Profile) },
    onNavigateToAddTripCost = { backStack.add(FleetRoute.TripCostEntry()) },
    onNavigateToAddMaintenanceCost = { backStack.add(FleetRoute.MaintenanceCostEntry()) },
    onNavigateToAddDriverCost = { backStack.add(FleetRoute.DriverCostEntry()) },
    // ...etc
)
```

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/dashboard/DashboardStats.kt` |
| Repository | `domain/repository/dashboard/DashboardRepository.kt` |
| Use Cases | `domain/usecase/dashboard/` (5 use cases) |
| DataSource | `data/datasource/dashboard/` (Remote + Local) |
| Repository Impl | `data/repository/dashboard/DashboardRepositoryImpl.kt` |
| Presentation | `presentation/dashboard/` |

