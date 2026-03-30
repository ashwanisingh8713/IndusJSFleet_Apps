# Metro Dependency Injection — IndusJS Fleet

## Annotations

| Annotation | Where | Purpose |
|------------|-------|---------|
| `@Inject` | On class (not constructor) | Mark for constructor injection |
| `@DependencyGraph` | On interface/abstract class | Define DI graph |
| `@DependencyGraph.Factory` | On inner interface | Factory needing external deps |
| `@Provides` | On function in graph | Provide dependency instance |
| `@Binds` | On abstract function | Bind interface → implementation |
| `@SingleIn(Scope::class)` | On class/provides | Scope to lifecycle |

## DI Wiring Chain (sharedUI)

```
App.kt → DefaultViewModelProvider (singleton)
  → NetworkDataGraph (HttpClient, Json, Settings, UserLocalDataSource)
  → FeatureRepositoryFactory (constructs 8 feature repositories)
  → Use Cases (constructed with repositories)
  → ViewModels (constructed with use cases)
```

## Key Files

- `sharedUI/.../di/DefaultViewModelProvider.kt` — Central DI container (498 lines)
- `sharedUI/.../di/ViewModelProvider.kt` — Interface + CompositionLocal (199 lines)
- `sharedUI/.../di/FeatureRepositoryFactory.kt` — Wires 8 feature repos (138 lines)
- `ijs-network-lib/.../di/NetworkDataGraph.kt` — Metro graph for networking

## Adding a New ViewModel

1. Add factory method to `ViewModelProvider` interface
2. Implement in `DefaultViewModelProvider`:
   ```kotlin
   override fun newFeatureViewModel(): NewFeatureViewModel {
       return NewFeatureViewModel(newFeatureUseCase)
   }
   ```
3. Wire use case and repository in `DefaultViewModelProvider` lazy blocks
4. Add repository construction in `FeatureRepositoryFactory` if new data module

## Accessing ViewModels in Compose

```kotlin
// Single-use ViewModel
val vm = rememberViewModel { dashboardViewModel() }

// Shared ViewModel across related screens (same key = same instance)
val vm = rememberSharedViewModel("finance_$vehicleId") { vehicleFinanceViewModel() }

// Clear when leaving flow
clearSharedViewModel("finance_$vehicleId")
```

## Common Mistakes

- ❌ Using `@Inject constructor` — put `@Inject` on the class itself
- ❌ Creating ViewModel directly — always go through `ViewModelProvider`
- ❌ Forgetting to add factory method to `ViewModelProvider` interface

