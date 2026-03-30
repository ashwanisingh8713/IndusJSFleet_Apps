# Facade Pattern — IndusJS Fleet

## What It Is

Each `screen-*` module exposes a `{Feature}FeatureFacade` object with `@Composable` entry points.
This is the **only public API** a feature module exposes to `sharedUI`.

## Pattern

```kotlin
// screen-vehicle/.../presentation/VehicleFeatureFacade.kt
object VehicleFeatureFacade {

    @Composable
    fun VehiclesListEntry(
        viewModel: VehiclesViewModel,       // Created by sharedUI's DI
        onNavigateBack: () -> Unit,          // Lambda callback — NO FleetRoute
        onNavigateToDetail: (String) -> Unit,
        onNavigateToAdd: () -> Unit
    ) {
        VehiclesScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToAdd = onNavigateToAdd
        )
    }
}
```

## Key Rules

1. **One Facade per feature module** — named `{Feature}FeatureFacade`
2. **Object, not class** — no state, no constructor
3. **ViewModels passed in** — created by `sharedUI/di/DefaultViewModelProvider`
4. **Navigation via lambdas only** — `FleetRoute` is NEVER imported
5. **One entry function per screen** — e.g., `VehiclesListEntry`, `VehicleDetailEntry`
6. Screen composables are `internal` — only Facade is public

## Cross-Feature Communication

When module A needs data from module B (e.g., payment needs trip data):

1. Define adapter interface in module A: `TripProviderForPayment` (in `screen-payment`)
2. Implement adapter in `sharedUI/di/adapter/`: `TripProviderAdapter` bridges `TripRepository` → `TripProviderForPayment`
3. Pass adapter to module A's ViewModel via DI

## Key Files

- Reference: `screen-vehicle/.../presentation/VehicleFeatureFacade.kt` (88 lines)
- Adapter interface: `screen-payment/.../domain/repository/TripProviderForPayment.kt`
- Adapter impl: `sharedUI/.../di/adapter/TripProviderAdapter.kt`

## Every Feature Module Has

| File | Purpose |
|------|---------|
| `{Feature}FeatureFacade.kt` | Public @Composable entry points |
| `{Feature}Contract.kt` | State, Intent, Effect |
| `{Feature}ViewModel.kt` | MVI ViewModel |
| `{Feature}Screen.kt` | Compose UI (internal) |

