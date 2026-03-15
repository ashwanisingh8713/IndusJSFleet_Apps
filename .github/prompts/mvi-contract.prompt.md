# MVI Contract Template

> Use this prompt when creating a new MVI Contract (State + Intent + Effect) for a feature.

## Base Classes (from `core/mvi/`)

```kotlin
interface UiState
interface UiIntent
interface UiEffect
```

## Contract File Pattern

Place at: `presentation/{feature}/{Feature}Contract.kt`

### List Screen Contract

```kotlin
object {Feature}Contract {
    data class State(
        val isLoading: Boolean = false,
        val items: List<{Entity}> = emptyList(),
        val filteredItems: List<{Entity}> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedFilter: String? = null,
        val isRefreshing: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data object LoadData : Intent
        data object Refresh : Intent
        data class Search(val query: String) : Intent
        data class Filter(val filter: String?) : Intent
        data class SelectItem(val id: String) : Intent
        data class DeleteItem(val id: String) : Intent
        data object AddNew : Intent
        data object ClearFilters : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToDetail(val id: String) : Effect
        data object NavigateToAdd : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
```

### Detail Screen Contract

```kotlin
object {Feature}DetailContract {
    data class State(
        val isLoading: Boolean = false,
        val entity: {Entity}? = null,
        val error: String? = null,
        val isEditing: Boolean = false,
        val selectedTab: Int = 0,
        // Edit fields
        val editName: String = "",
        val editStatus: String = "",
        val isSaving: Boolean = false,
        val saveError: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadDetail(val id: String) : Intent
        data object ToggleEdit : Intent
        data class UpdateField(val field: String, val value: String) : Intent
        data object SaveChanges : Intent
        data class ChangeTab(val index: Int) : Intent
        data class ChangeStatus(val newStatus: String) : Intent
        data object Delete : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: String) : Effect
        data object SaveSuccess : Effect
    }
}
```

### Create/Form Screen Contract

```kotlin
object Create{Feature}Contract {
    data class State(
        // Form fields
        val name: String = "",
        val description: String = "",
        val date: String = "",    // DD-MM-YYYY
        val time: String = "",    // HH:MM
        // Validation
        val nameError: String? = null,
        val dateError: String? = null,
        // Submission
        val isSubmitting: Boolean = false,
        val error: String? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data class UpdateName(val value: String) : Intent
        data class UpdateDescription(val value: String) : Intent
        data class UpdateDate(val value: String) : Intent
        data class UpdateTime(val value: String) : Intent
        data object Submit : Intent
        data object ClearErrors : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: String) : Effect
        data object CreateSuccess : Effect
    }
}
```

## ViewModel Pattern

```kotlin
class {Feature}ViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val useCase: Get{Feature}UseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init { sendIntent(Intent.LoadData) }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadData -> loadData()
            is Intent.SelectItem -> sendEffect(Effect.NavigateToDetail(intent.id))
            is Intent.Search -> search(intent.query)
            // ... all intent handlers
        }
    }

    private suspend fun loadData() {
        updateState { copy(isLoading = true, error = null) }
        useCase().collectLatest { result ->
            when (result) {
                is Result.Loading -> { /* already set */ }
                is Result.Success -> updateState { copy(isLoading = false, items = result.data) }
                is Result.Error -> updateState { copy(isLoading = false, error = result.errorMessage) }
            }
        }
    }
}
```

## Rules

- **State**: Always immutable `data class`, use `copy()` via `updateState { copy(...) }`
- **Intent**: All user actions. Never call repository directly from UI
- **Effect**: One-time events only (navigation, snackbar). Never put continuous state here
- **ViewModel**: Extends `MviViewModel<State, Intent, Effect>(initialState)`
- Always include `isLoading`, `error` in State
- Use `Result.Loading/Success/Error` from `ijs-error-lib`

