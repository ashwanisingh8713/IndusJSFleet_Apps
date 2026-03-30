# MVI Pattern — IndusJS Fleet

## Contract Structure (Every Feature)

```kotlin
object {Feature}Contract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        // feature-specific fields
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadData : Intent
        data class OnAction(val id: String) : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        // Navigation via lambda callbacks, NOT via Effect
    }
}
```

## ViewModel Pattern

```kotlin
@Inject
class {Feature}ViewModel(
    private val useCase: {Feature}UseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init { sendIntent(Intent.LoadData) }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }
        useCase().collect { result ->
            when (result) {
                is Result.Success -> updateState { copy(isLoading = false, data = result.data) }
                is Result.Error -> updateState { copy(isLoading = false, error = result.message) }
                is Result.Loading -> updateState { copy(isLoading = true) }
            }
        }
    }
}
```

## Key Rules

- **State**: Immutable data class, always use `updateState { copy(...) }` — never `mutableStateOf`
- **Intent**: Sealed interface for all user actions — sent via `viewModel.sendIntent()`
- **Effect**: One-time events only (snackbar, toast) — navigation uses lambda callbacks
- Base class: `MviViewModel<State, Intent, Effect>` from `ijs-core-lib`
- Marker interfaces: `UiState`, `UiIntent`, `UiEffect` from `com.indusjs.fleet.core.mvi`

## Screen Pattern

```kotlin
@Composable
fun {Feature}Screen(
    viewModel: {Feature}ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Handle effects, render UI based on state
}
```

## Key Files

- `ijs-core-lib/.../core/mvi/MviViewModel.kt` — Base ViewModel (93 lines)
- `ijs-core-lib/.../core/mvi/MviContract.kt` — UiState, UiIntent, UiEffect interfaces
- `ijs-core-lib/.../core/mvi/MviExtensions.kt` — HandleEffects composable

## Common Mistakes

- ❌ Using `mutableStateOf` in ViewModel — use `updateState { copy() }`
- ❌ Putting navigation in Effect — use lambda callbacks on Screen
- ❌ Calling API in Screen — always go through ViewModel → UseCase
- ❌ Mutating state directly — State is immutable, return new copy

