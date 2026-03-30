# Role Access & sharedUI Orchestrator — IndusJS Fleet

## User Roles (Hierarchical)

```
owner > general_manager > manager > supervisor > driver
```

| Role | Financials | Team Mgmt | CRUD | Reports | Trip Status |
|------|-----------|-----------|------|---------|-------------|
| **Owner** | ✅ Full | ✅ All roles | ✅ All | ✅ All | ✅ |
| **General Manager** | ✅ Full | ✅ Manager/Supervisor | ✅ All | ✅ All | ✅ |
| **Manager** | ❌ No trip_price, no P&L | ❌ | ✅ Create trips/costs | ❌ | ✅ |
| **Supervisor** | ❌ | ❌ | ❌ View-only | ❌ | ✅ Update only |

## Role Checks in UI

Financial data (trip_price, revenue, profit) is hidden from Manager/Supervisor.
Use `userRole` from state to conditionally show sections.

## sharedUI Module — The Orchestrator

`sharedUI` is the **central wiring module**. It contains:

| Responsibility | Key Files |
|----------------|-----------|
| Navigation | `navigation/FleetRoute.kt`, `navigation/FleetNavigation.kt` |
| DI Container | `di/DefaultViewModelProvider.kt`, `di/ViewModelProvider.kt` |
| Repository Wiring | `di/FeatureRepositoryFactory.kt` |
| Cross-Feature Adapters | `di/adapter/TripProviderAdapter.kt` |
| App Entry | `App.kt` |
| Theme | `theme/FleetTheme.kt` |
| Auth State | `core/auth/AuthenticationManager.kt` |
| Initialization | `core/init/AppInitializer.kt` |

## sharedUI Rules

- **NO presentation code** — all Screens/ViewModels/Contracts live in `screen-*` modules
- **NO domain/data logic** — put in appropriate feature module
- Only navigation wiring, DI wiring, and app orchestration

## Adding a New Feature (Full Checklist)

1. Domain entity → `screen-{feature}/domain/entity/`
2. Repository interface → `screen-{feature}/domain/repository/`
3. Use cases → `screen-{feature}/domain/usecase/`
4. DTOs → `screen-{feature}/data/model/`
5. Mapper → `screen-{feature}/data/mapper/`
6. DataSource → `screen-{feature}/data/datasource/`
7. Repository impl → `screen-{feature}/data/repository/`
8. Wire repository → `sharedUI/di/FeatureRepositoryFactory.kt`
9. Contract → `screen-{feature}/presentation/`
10. ViewModel → `screen-{feature}/presentation/`
11. Screen → `screen-{feature}/presentation/`
12. Facade → `screen-{feature}/presentation/{Feature}FeatureFacade.kt`
13. ViewModel factory → `sharedUI/di/ViewModelProvider.kt`
14. Wire ViewModel → `sharedUI/di/DefaultViewModelProvider.kt`
15. Route → `sharedUI/navigation/FleetRoute.kt`
16. NavEntry → `sharedUI/navigation/FleetNavigation.kt`

## Key Files (sharedUI)

- `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/App.kt` (173 lines)
- `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/DefaultViewModelProvider.kt` (498 lines)
- `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/ViewModelProvider.kt` (199 lines)
- `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/FeatureRepositoryFactory.kt` (138 lines)
- `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/navigation/FleetRoute.kt` (100 lines)
- `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/navigation/FleetNavigation.kt` (536 lines)

