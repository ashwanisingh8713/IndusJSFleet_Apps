# screen-dashboard — IndusJS Fleet

## Purpose

Main overview screen: fleet stats, cost overview, financial summary, alerts, quick actions.
Data layer lives in `ijs-network-lib` (DashboardRemoteDataSource, DashboardRepository).

## Package: `com.ijs.dashboard`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| DashboardScreen | `Dashboard` | Main hub — 6 sections, 18+ navigation callbacks |

## Dashboard Sections

1. **Fleet Overview** — Vehicle/Driver/Trip counts by status
2. **Cost Overview** — Today/Weekly/Monthly cost summaries
3. **Financial Summary** — Revenue, expenses, profit (Owner/GM only)
4. **Vehicle Status Summary** — Active, on_route, maintenance counts
5. **Alerts** — Document/license expiry warnings
6. **Quick Actions** — Add vehicle, create trip, add cost, etc.

## Key Files

| File | Purpose |
|------|---------|
| `presentation/DashboardFeatureFacade.kt` | Facade — 1 entry point, many callbacks |
| `presentation/DashboardContract.kt` | State/Intent/Effect |
| `presentation/DashboardViewModel.kt` | ViewModel — 6 use cases |
| `presentation/DashboardScreen.kt` | UI — section cards |

## Navigation (18+ callbacks)

Dashboard navigates to ALL other features. The Facade entry point receives many lambdas:
`onNavigateToVehicles`, `onNavigateToDrivers`, `onNavigateToTrips`, `onNavigateToCustomers`,
`onNavigateToPayments`, `onNavigateToReports`, `onNavigateToTeam`, `onNavigateToFinance`,
`onNavigateToMaps`, `onNavigateToAlerts`, `onNavigateToProfile`, etc.

## Role-Based Visibility

| Section | Owner | GM | Manager | Supervisor |
|---------|-------|----|---------|-----------| 
| Financial Summary | ✅ | ✅ | ❌ | ❌ |
| Cost Overview | ✅ | ✅ | ✅ | ❌ |
| Quick Actions (Finance) | ✅ | ✅ | ❌ | ❌ |

## Module Path

`screen-dashboard/src/commonMain/kotlin/com/ijs/dashboard/`

## Depends On: `ijs-network-lib`

