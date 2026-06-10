# 11 - Map Module Migration

## Migration Summary

| Item | Details |
|------|---------|
| **Module** | `screen-map` |
| **Namespace** | `com.ijs.map` |
| **Package** | `com.ijs.map.presentation` |
| **Migration Date** | March 2026 |
| **Status** | ✅ Complete |

## Files Migrated

| Source (sharedUI) | Destination (screen-map) |
|-------------------|----------------------|
| `presentation/maps/MapsContract.kt` | `presentation/MapsContract.kt` |
| `presentation/maps/MapsViewModel.kt` | `presentation/MapsViewModel.kt` |
| `presentation/maps/MapsScreen.kt` | `presentation/MapsScreen.kt` |
| *(new)* | `presentation/MapFeatureFacade.kt` |

## Facade Entry Points

| Entry Point | Parameters |
|------------|------------|
| `MapFeatureFacade.MapsEntry` | `viewModel`, `onNavigateToVehicleDetail`, `onNavigateBack` |

## Dependencies

- `ijs-network-lib` (via `api()`) → provides `ijs-core-lib`, domain entities, MVI base classes
- `ijs-ui-components-lib` (via `fleet-compose-conventions.gradle`) → provides `ErrorContent`, `LoadingContent`, theme utilities, resources

## Notes

- Maps feature currently uses mock data only (no real API calls)
- Single `DispatcherProvider` dependency in ViewModel
- No data layer — presentation only

