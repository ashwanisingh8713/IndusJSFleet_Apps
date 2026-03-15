# DI Wiring Guide

> Use this prompt when wiring a new feature into the dependency injection system.

## Current DI: DefaultViewModelProvider (Manual Wiring)

The app uses manual lazy initialization in `DefaultViewModelProvider.kt`. Metro DI graphs exist but `DefaultViewModelProvider` is the active production container.

## Steps to Wire a New Feature

### 1. Add to `ViewModelProvider.kt` interface

```kotlin
interface ViewModelProvider {
    // ...existing methods...

    // My Feature ViewModels
    fun myListViewModel(): MyListViewModel
    fun myDetailViewModel(): MyDetailViewModel
    fun createMyViewModel(): CreateMyViewModel
}
```

### 2. Add lazy dependencies in `DefaultViewModelProvider.kt`

```kotlin
class DefaultViewModelProvider private constructor() : ViewModelProvider {
    // ...existing code...

    // ===== My Feature Dependencies =====
    private val myMapper by lazy { MyMapper() }
    private val myRemoteDataSource by lazy { MyRemoteDataSourceImpl(httpClient) }
    private val myRepository: MyRepository by lazy {
        MyRepositoryImpl(myRemoteDataSource, userLocalDataSource, myMapper)
    }
    private val getMyListUseCase by lazy { GetMyListUseCase(myRepository) }
    private val getMyByIdUseCase by lazy { GetMyByIdUseCase(myRepository) }
    private val createMyUseCase by lazy { CreateMyUseCase(myRepository) }

    // ===== My Feature ViewModels =====
    override fun myListViewModel() = MyListViewModel(
        dispatcherProvider,
        getMyListUseCase
    )

    override fun myDetailViewModel() = MyDetailViewModel(
        dispatcherProvider,
        getMyByIdUseCase,
        myRepository
    )

    override fun createMyViewModel() = CreateMyViewModel(
        dispatcherProvider,
        createMyUseCase
    )
}
```

### Dependency Chain

```
HttpClient (shared singleton)
    ↓
RemoteDataSource (lazy per feature)
    ↓
UserLocalDataSource (shared — provides auth token)
    ↓
Mapper (lazy per feature)
    ↓
Repository (lazy per feature — combines remote + local + mapper)
    ↓
UseCase (lazy — wraps repository method)
    ↓
ViewModel (factory — created fresh per screen)
```

## Key Patterns

**Shared dependencies** (already exist, just reference):
- `httpClient` — singleton Ktor HttpClient
- `settings` — `com.russhwolf.settings.Settings` for key-value storage
- `json` — `kotlinx.serialization.json.Json` config
- `database` — `FleetDatabase` for Room/Settings offline cache
- `userLocalDataSource` — provides auth token, user info
- `dispatcherProvider` — `DispatcherProvider` for coroutine dispatching
- `googlePlacesService` — Google Places API client (nullable)
- `costTypesRepository` — cached cost types

**ViewModels are always created fresh** (not lazy). They are instantiated each time via `rememberViewModel { }` in FleetNavigation.kt.

**Shared ViewModels** use `rememberSharedViewModel(key)` for flows where multiple screens share state (e.g., Vehicle Finance flow).

## Optional: Metro DI Graph

If creating a Metro graph for the feature:

```kotlin
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class MyFeatureGraph {
    @Binds
    abstract fun bindRepository(impl: MyRepositoryImpl): MyRepository

    abstract val myListViewModel: MyListViewModel
    abstract val myDetailViewModel: MyDetailViewModel

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider
        ): MyFeatureGraph
    }
}
```

Place at: `di/MyFeatureGraph.kt`

