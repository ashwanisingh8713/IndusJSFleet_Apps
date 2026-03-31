# MVI Pattern (Model-View-Intent)

## Overview

Every feature with UI follows MVI. The base classes live in `ijs-core-lib` at `com.indusjs.fleet.core.mvi`.

## Core Contracts

```kotlin
// Marker interfaces (MviContract.kt)
interface UiState       // Immutable state for the screen
interface UiIntent      // User actions / events
interface UiEffect      // One-time side effects (navigation, snackbars)
```

## MviViewModel Base Class

**File:** `ijs-core-lib/.../core/mvi/MviViewModel.kt`

```kotlin
abstract class MviViewModel<State : UiState, Intent : UiIntent, Effect : UiEffect>(
    initialState: State
) : ViewModel() {

    // State: MutableStateFlow → exposed as StateFlow
    val state: StateFlow<State>
    protected val currentState: State

    // Effects: Channel → exposed as Flow
    val effect: Flow<Effect>

    // Intents: MutableSharedFlow (collected in init)
    fun sendIntent(intent: Intent)

    // Subclass must implement
    abstract suspend fun handleIntent(intent: Intent)

    // State mutation (thread-safe via StateFlow)
    protected fun updateState(reduce: State.() -> State)

    // Fire one-time effects
    protected suspend fun sendEffect(effect: Effect)
}
```

**Flow:**
1. `init {}` block collects intents from `SharedFlow` and calls `handleIntent()` sequentially
2. State updates use `updateState { copy(...) }` pattern
3. Effects use `sendEffect()` which writes to a `Channel`
4. UI collects effects via `HandleEffects` composable

## Contract Pattern

Every feature defines a contract object:

```kotlin
object FeatureContract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val data: List<Item> = emptyList(),
        // ... feature-specific fields
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadData : Intent
        data class OnItemClick(val id: String) : Intent
        data class OnSearchQueryChanged(val query: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateTo(val route: Any) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
```

## ViewModel Pattern

```kotlin
@Inject
class FeatureViewModel(
    private val useCase: FeatureUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadData)  // Auto-load on creation
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
            is Intent.OnItemClick -> handleItemClick(intent.id)
            is Intent.OnSearchQueryChanged -> handleSearch(intent.query)
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }
        useCase().collect { result ->
            when (result) {
                is Result.Loading -> { /* already handled */ }
                is Result.Success -> updateState { copy(isLoading = false, data = result.data) }
                is Result.Error -> updateState { copy(isLoading = false, error = result.errorMessage) }
            }
        }
    }
}
```

## Screen Pattern

```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect one-time effects
    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateTo -> { /* delegate to nav lambda */ }
            is Effect.ShowSnackbar -> { /* show snackbar */ }
        }
    }

    // Render UI based on state
    ScreenContent(
        isLoading = state.isLoading,
        error = state.error,
        data = state.data,
        onRetry = { viewModel.sendIntent(Intent.LoadData) }
    ) { data ->
        // Screen content when data is available
    }
}
```

## Compose Helpers (MviExtensions.kt)

| Helper | Purpose |
|--------|---------|
| `HandleEffects(vm) { effect -> ... }` | Collects effects in LaunchedEffect, runs handler |
| `vm.collectState()` | Returns `State` as Compose state |
| `vm.intentHandler(block)` | Returns `(Intent) -> Unit` callback |

## Data Flow Sequence

```
User taps button
  → Screen calls viewModel.sendIntent(Intent.LoadData)
    → MviViewModel.init collector routes to handleIntent()
      → handleIntent() calls updateState { copy(isLoading = true) }
      → handleIntent() calls useCase() → returns Flow<Result<T>>
        → UseCase calls repository.getData()
          → Repository emits Result.Loading
          → Repository calls remoteDataSource.apiCall(token)
          → Repository maps DTO → Entity
          → Repository emits Result.Success(entity) or Result.Error(exception)
      → ViewModel collects flow → updateState { copy(data = result.data) }
  → Screen observes state via collectAsStateWithLifecycle()
  → UI recomposes with new data
```

## Key Rules

- **Never** use `mutableStateOf` in ViewModels — always use `MviViewModel`
- State must be immutable — update only via `updateState { copy(...) }`
- Navigation and toasts go through Effects, not direct calls
- `handleIntent` is the single entry point for all business logic
- Init block typically sends a `LoadData` intent for auto-loading
