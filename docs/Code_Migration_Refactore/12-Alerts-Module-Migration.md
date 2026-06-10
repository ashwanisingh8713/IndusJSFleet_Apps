# 12 - Alerts Module Migration

## Migration Summary

| Item | Details |
|------|---------|
| **Module** | `screen-alerts` |
| **Namespace** | `com.ijs.alerts` |
| **Package** | `com.ijs.alerts.presentation` |
| **Migration Date** | March 2026 |
| **Status** | ✅ Complete |

## Files Migrated

| Source (sharedUI) | Destination (screen-alerts) |
|-------------------|--------------------------|
| `presentation/alerts/AlertsListContract.kt` | `presentation/AlertsListContract.kt` |
| `presentation/alerts/AlertsListViewModel.kt` | `presentation/AlertsListViewModel.kt` |
| `presentation/alerts/AlertsListScreen.kt` | `presentation/AlertsListScreen.kt` |
| *(new)* | `presentation/AlertsFeatureFacade.kt` |

## Facade Entry Points

| Entry Point | Parameters |
|------------|------------|
| `AlertsFeatureFacade.AlertsListEntry` | `viewModel`, `onNavigateBack` |

## Dependencies

- `ijs-network-lib` (via `api()`) → provides `ijs-core-lib`, domain entities, MVI base classes
- `ijs-ui-components-lib` (via `fleet-compose-conventions.gradle`) → provides `ErrorContent`, `LoadingContent`, theme utilities, resources

## Notes

- Uses `GetAlertsStatusUseCase` from `ijs-network-lib`
- AlertsListScreen is 563 lines — exceeds 500-line limit, recommended for splitting in future refactoring
- Simple single-screen feature with 1 use case

