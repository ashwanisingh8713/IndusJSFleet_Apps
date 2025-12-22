# Clean Architecture with Metro DI, MVI Pattern, and ViewModel

This project follows **Clean Architecture** principles with the following structure:

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                          │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  UI (Compose)  ←→  ViewModel (MVI)  ←→  State/Intent/Effect │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              ↓ Uses
┌──────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │        Use Cases  ←→  Entities  ←→  Repository Interfaces  │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
                              ↓ Implements
┌──────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │  Repository Impl  ←→  Data Sources  ←→  DTOs/Mappers       │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

## Folder Structure

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/
├── core/                          # Core utilities and base classes
│   ├── dispatcher/                # Coroutine dispatcher abstraction
│   │   └── DispatcherProvider.kt
│   ├── mvi/                       # MVI pattern base classes
│   │   ├── MviContract.kt         # UiState, UiIntent, UiEffect interfaces
│   │   ├── MviViewModel.kt        # Base MVI ViewModel
│   │   └── MviExtensions.kt       # Compose extensions for MVI
│   ├── network/                   # Network utilities
│   │   └── NetworkError.kt        # Standardized network errors
│   └── result/                    # Result wrapper
│       └── Result.kt              # Success/Error/Loading wrapper
│
├── data/                          # Data layer base interfaces
│   ├── datasource/
│   │   └── DataSource.kt          # LocalDataSource, RemoteDataSource
│   ├── mapper/
│   │   └── Mapper.kt              # Data ↔ Domain mappers
│   └── model/
│       └── DataModels.kt          # Dto, DbEntity markers
│
├── di/                            # Dependency Injection with Metro
│   ├── AppGraph.kt                # App-level DI graph
│   ├── AppDependencies.kt         # Dependency container
│   ├── DataModule.kt              # Data layer bindings
│   ├── DomainModule.kt            # Domain layer bindings
│   ├── NetworkModule.kt           # Network dependencies
│   ├── PresentationModule.kt      # Presentation bindings
│   └── RootGraph.kt               # Root dependency graph
│
├── domain/                        # Domain layer base interfaces
│   ├── entity/
│   │   └── Entity.kt              # Base entity marker
│   ├── repository/
│   │   └── Repository.kt          # Base repository marker
│   └── usecase/
│       └── UseCase.kt             # Use case interfaces
│
├── feature/                       # Feature modules
│   └── sample/                    # Sample feature demonstrating architecture
│       ├── data/
│       │   ├── datasource/
│       │   │   └── UserRemoteDataSource.kt
│       │   ├── mapper/
│       │   │   └── UserMapper.kt
│       │   ├── model/
│       │   │   └── UserDto.kt
│       │   └── repository/
│       │       └── UserRepositoryImpl.kt
│       ├── di/
│       │   └── SampleFeatureGraph.kt
│       ├── domain/
│       │   ├── entity/
│       │   │   └── User.kt
│       │   ├── repository/
│       │   │   └── UserRepository.kt
│       │   └── usecase/
│       │       ├── GetUserByIdUseCase.kt
│       │       └── GetUsersUseCase.kt
│       └── presentation/
│           ├── UserListContract.kt     # MVI contract
│           ├── UserListScreen.kt       # Compose UI
│           └── UserListViewModel.kt    # MVI ViewModel
│
└── theme/                         # App theming
    ├── Color.kt
    └── Theme.kt
```

## MVI Pattern

The **Model-View-Intent** pattern provides unidirectional data flow:

```
┌────────────┐    Intent     ┌────────────────┐    State    ┌──────────────┐
│            │ ───────────→  │                │ ─────────→  │              │
│    View    │               │   ViewModel    │             │    State     │
│  (Compose) │  ←───────────  │    (MVI)       │ ←─────────  │   (Data)     │
│            │    Effect     │                │   Reduce    │              │
└────────────┘               └────────────────┘             └──────────────┘
```

### Usage Example

```kotlin
// 1. Define the contract
object MyFeatureContract {
    data class State(
        val isLoading: Boolean = false,
        val data: String = ""
    ) : UiState
    
    sealed interface Intent : UiIntent {
        data object LoadData : Intent
    }
    
    sealed interface Effect : UiEffect {
        data class ShowError(val message: String) : Effect
    }
}

// 2. Create the ViewModel
class MyFeatureViewModel @Inject constructor(
    private val useCase: MyUseCase
) : MviViewModel<State, Intent, Effect>(State()) {
    
    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
        }
    }
    
    private suspend fun loadData() {
        updateState { copy(isLoading = true) }
        // ... load data
        updateState { copy(isLoading = false, data = result) }
    }
}

// 3. Use in Compose
@Composable
fun MyFeatureScreen(viewModel: MyFeatureViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.ShowError -> // show snackbar
        }
    }
    
    Button(onClick = { viewModel.sendIntent(Intent.LoadData) }) {
        Text("Load")
    }
}
```

## Metro Dependency Injection

[Metro](https://github.com/ZacSweers/metro) is a compile-time DI framework for Kotlin Multiplatform.

### Key Annotations

- `@DependencyGraph` - Defines a DI graph/component
- `@DependencyGraph.Factory` - Factory interface for graphs with external dependencies
- `@Provides` - Provides a dependency
- `@Binds` - Binds interface to implementation
- `@Inject` - Marks class for constructor injection (put on class, not constructor)
- `@SingleIn(Scope::class)` - Scopes dependency

### Root Graph (No External Dependencies)

```kotlin
@SingleIn(AppScope::class)
@DependencyGraph
abstract class RootGraph {
    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(): HttpClient = HttpClient { ... }
    
    abstract val httpClient: HttpClient
    
    companion object
}

// Initialize at app startup:
// AppDependencies.initialize { RootGraph.create() }
```

### Feature Graph (With External Dependencies)

```kotlin
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class FeatureGraph {
    @Binds
    abstract fun bindRepository(impl: RepositoryImpl): Repository
    
    abstract val viewModel: FeatureViewModel
    
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider
        ): FeatureGraph
    }
}

// Create feature graph:
// val featureGraph = FeatureGraph.Factory.create(httpClient, dispatcherProvider)
```

### Using @Inject

```kotlin
// Preferred: @Inject on class (for single constructor)
@Inject
class UserRepository(
    private val httpClient: HttpClient
) { ... }

// Alternative: @Inject on constructor
class UserRepository @Inject constructor(
    private val httpClient: HttpClient
) { ... }
```

## Clean Architecture Layers

### 1. Presentation Layer
- **UI**: Compose screens
- **ViewModel**: MVI ViewModels handling state
- **Contract**: State, Intent, Effect definitions

### 2. Domain Layer
- **Entities**: Core business models
- **Use Cases**: Business logic
- **Repository Interfaces**: Contracts for data access

### 3. Data Layer
- **Repository Implementations**: Coordinate data sources
- **Data Sources**: Remote (API) and Local (DB)
- **Mappers**: Convert DTOs ↔ Entities
- **DTOs**: Data Transfer Objects for API/DB

## Best Practices

1. **Keep Domain layer pure** - No framework dependencies
2. **Use interfaces for repositories** - Defined in Domain, implemented in Data
3. **Single responsibility** - One use case = one business operation
4. **Immutable state** - Use `copy()` for state updates
5. **Handle effects properly** - One-time events via Effect channel

## Adding a New Feature

1. Create feature folder under `feature/`
2. Add domain layer (entity, repository interface, use cases)
3. Add data layer (DTO, data source, repository impl, mapper)
4. Add presentation layer (contract, viewmodel, screen)
5. Create feature DI graph
6. Wire up navigation

