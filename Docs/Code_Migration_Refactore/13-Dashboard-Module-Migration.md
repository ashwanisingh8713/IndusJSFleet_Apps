# 13 - Dashboard Module Migration

## Migration Summary

| Item | Details |
|------|---------|
| **Module** | `screen-dashboard` |
| **Namespace** | `com.ijs.dashboard` |
| **Package** | `com.ijs.dashboard.presentation` |
| **Migration Date** | March 2026 |
| **Status** | ✅ Complete |

## Files Migrated

| Source (sharedUI) | Destination (screen-dashboard) |
|-------------------|------------------------------|
| `presentation/dashboard/DashboardContract.kt` | `presentation/DashboardContract.kt` |
| `presentation/dashboard/DashboardViewModel.kt` | `presentation/DashboardViewModel.kt` |
| `presentation/dashboard/DashboardScreen.kt` | `presentation/DashboardScreen.kt` |
| `presentation/dashboard/components/FinancialSummarySection.kt` | `presentation/components/FinancialSummarySection.kt` |
| *(new)* | `presentation/DashboardFeatureFacade.kt` |

## Facade Entry Points

| Entry Point | Parameters |
|------------|------------|
| `DashboardFeatureFacade.DashboardEntry` | `viewModel`, `onNavigateToVehicles`, `onNavigateToDrivers`, `onNavigateToTrips`, `onNavigateToMaps`, `onNavigateToProfile`, `onNavigateToTeam`, `onNavigateToReports`, `onNavigateToAddTripCost`, `onNavigateToAddVehicleCost`, `onNavigateToAddVehicle`, `onNavigateToAddDriver`, `onNavigateToCreateTrip`, `onNavigateToAddDriverCost`, `onNavigateToAlertsList`, `onNavigateToCustomers`, `onNavigateToPayments`, `onNavigateToVehicleFinance` |

## Dependencies

- `ijs-network-lib` (via `api()`) → provides `ijs-core-lib`, domain entities, dashboard use cases, MVI base classes, `CostOverviewFilter`, `FinancialPeriod` data models
- `ijs-ui-components-lib` (via `fleet-compose-conventions.gradle`) → provides `ErrorContent`, `LoadingContent`, `isAppInDarkTheme()`, `rememberThemeToggle()`, resources

## 500-Line Violations (Deferred Refactoring)

| File | Lines | Recommendation |
|------|-------|----------------|
| `DashboardScreen.kt` | 4191 | Split into ~8 component files (NavigationDrawer, FleetOverviewHero, CostOverview, AlertsSection, VehicleStatus, DriverStatus, TripStatus, QuickActions, etc.) |
| `DashboardViewModel.kt` | 511 | Extract data loading helpers into `DashboardDataLoader.kt` |

## Notes

- Most complex feature — 6 use cases, 18+ navigation callbacks, companion object caching
- `FinancialSummarySection.kt` is currently orphaned (defined but not referenced by DashboardScreen) — preserved for future use
- Data layer (use cases, repositories, data sources) remains in `ijs-network-lib`
- This was the last presentation module to migrate — `sharedUI/presentation/` is now completely empty

