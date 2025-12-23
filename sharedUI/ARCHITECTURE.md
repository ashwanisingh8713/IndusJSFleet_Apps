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
│   ├── error/                     # Custom exception types
│   │   └── FleetExceptions.kt     # NotAuthenticatedException, ApiException, etc.
│   ├── mvi/                       # MVI pattern base classes
│   │   ├── MviContract.kt         # UiState, UiIntent, UiEffect interfaces
│   │   ├── MviViewModel.kt        # Base MVI ViewModel
│   │   └── MviExtensions.kt       # Compose extensions for MVI
│   ├── network/                   # Network utilities
│   │   ├── ApiConfig.kt           # API configuration
│   │   ├── HttpClientProvider.kt  # HTTP client factory
│   │   └── NetworkError.kt        # Standardized network errors
│   ├── result/                    # Result wrapper
│   │   └── Result.kt              # Success/Error/Loading wrapper
│   └── ui/                        # Reusable UI components
│       ├── CommonComponents.kt    # LoadingContent, ErrorContent, EmptyContent, ScreenContent
│       ├── InputComponents.kt     # FleetTextField, FleetEmailField, FleetPasswordField, etc.
│       ├── ButtonComponents.kt    # FleetPrimaryButton, FleetSecondaryButton, etc.
│       └── CardComponents.kt      # FleetItemCard, FleetStatusBadge, FleetIconAvatar, etc.
│
├── data/                          # Centralized Data layer
│   ├── datasource/                # Data sources by feature
│   │   ├── DataSource.kt          # Base interfaces
│   │   ├── user/
│   │   │   ├── UserLocalDataSource.kt
│   │   │   └── UserRemoteDataSource.kt
│   │   ├── vehicle/
│   │   │   └── VehicleRemoteDataSource.kt
│   │   ├── trip/
│   │   │   └── TripRemoteDataSource.kt
│   │   └── team/
│   │       └── TeamRemoteDataSource.kt
│   ├── mapper/                    # DTOs ↔ Domain mappers by feature
│   │   ├── Mapper.kt              # Base mapper interface
│   │   ├── user/
│   │   │   └── UserMapper.kt
│   │   ├── vehicle/
│   │   │   └── VehicleMapper.kt
│   │   ├── trip/
│   │   │   └── TripMapper.kt
│   │   └── team/
│   │       └── TeamMapper.kt
│   ├── model/                     # DTOs by feature
│   │   ├── DataModels.kt          # Dto, DbEntity markers
│   │   ├── user/
│   │   │   └── UserDto.kt
│   │   ├── vehicle/
│   │   │   └── VehicleDto.kt
│   │   ├── trip/
│   │   │   └── TripDto.kt
│   │   └── team/
│   │       └── TeamDto.kt
│   └── repository/                # Repository implementations by feature
│       ├── user/
│       │   └── UserRepositoryImpl.kt
│       ├── vehicle/
│       │   └── VehicleRepositoryImpl.kt
│       ├── trip/
│       │   └── TripRepositoryImpl.kt
│       └── team/
│           └── TeamRepositoryImpl.kt
│
├── di/                            # Centralized Dependency Injection
│   ├── AppGraph.kt                # App-level DI graph scopes
│   ├── AppDependencies.kt         # Dependency container
│   ├── RootGraph.kt               # Root dependency graph
│   ├── ViewModelProvider.kt       # ViewModel provider interface
│   ├── DefaultViewModelProvider.kt # Default ViewModel provider
│   ├── AuthFeatureGraph.kt        # Auth feature DI graph
│   ├── UserFeatureGraph.kt        # User feature DI graph
│   ├── VehiclesFeatureGraph.kt    # Vehicles feature DI graph
│   ├── TripsFeatureGraph.kt       # Trips feature DI graph
│   └── TeamFeatureGraph.kt        # Team feature DI graph
│
├── domain/                        # Centralized Domain layer
│   ├── entity/                    # Domain entities by feature
│   │   ├── Entity.kt              # Base entity marker
│   │   ├── dashboard/
│   │   │   └── DashboardStats.kt
│   │   ├── driver/
│   │   │   └── Driver.kt
│   │   ├── team/
│   │   │   └── TeamMember.kt
│   │   ├── trip/
│   │   │   └── Trip.kt
│   │   ├── user/
│   │   │   └── User.kt
│   │   └── vehicle/
│   │       └── Vehicle.kt
│   ├── repository/                # Repository interfaces by feature
│   │   ├── Repository.kt          # Base repository marker
│   │   ├── team/
│   │   │   └── TeamRepository.kt
│   │   ├── trip/
│   │   │   └── TripRepository.kt
│   │   ├── user/
│   │   │   └── UserRepository.kt
│   │   └── vehicle/
│   │       └── VehicleRepository.kt
│   └── usecase/                   # Use cases by feature
│       ├── UseCase.kt             # Base use case interface
│       ├── trip/
│       │   └── TripUseCases.kt
│       └── vehicle/
│           └── VehicleUseCases.kt
│
├── presentation/                  # Presentation Layer (UI + ViewModels)
│   ├── auth/                      # Authentication
│   │   ├── LoginContract.kt
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   │
│   ├── user/                      # User management
│   │   ├── signup/
│   │   ├── profile/
│   │   ├── changepassword/
│   │   └── forgotpassword/
│   │
│   ├── vehicles/                  # Vehicle management
│   │   ├── VehiclesContract.kt
│   │   ├── VehiclesScreen.kt
│   │   ├── VehiclesViewModel.kt
│   │   ├── AddVehicleContract.kt
│   │   ├── AddVehicleScreen.kt
│   │   └── AddVehicleViewModel.kt
│   │
│   ├── trips/                     # Trip management
│   │   ├── TripsContract.kt
│   │   ├── TripsScreen.kt
│   │   └── TripsViewModel.kt
│   │
│   ├── team/                      # Team management
│   │   ├── list/
│   │   └── create/
│   │
│   ├── drivers/                   # Driver management
│   ├── dashboard/                 # Dashboard
│   └── maps/                      # Maps
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

1. Add domain entities under `domain/entity/{feature}/`
2. Add repository interface under `domain/repository/{feature}/`
3. Add use cases under `domain/usecase/{feature}/` (if needed)
4. Add DTOs under `data/model/{feature}/`
5. Add data source under `data/datasource/{feature}/`
6. Add mapper under `data/mapper/{feature}/`
7. Add repository implementation under `data/repository/{feature}/`
8. Add presentation files under `presentation/{feature}/`
   - Contract (State, Intent, Effect)
   - ViewModel
   - Screen (Compose UI)
9. Create/update feature DI graph in `di/`
10. Wire up navigation

