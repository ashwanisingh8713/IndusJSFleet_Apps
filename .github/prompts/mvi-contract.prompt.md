# MVI Contract Template

Generate an MVI Contract for a feature in the IndusJS Fleet app.

## Feature Information
- **Feature Name**: [FEATURE_NAME]
- **Entity Name**: [ENTITY_NAME]
- **Description**: [BRIEF_DESCRIPTION]

## Template

```kotlin
package com.indusjs.fleet.presentation.{feature}

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.{feature}.{Entity}

/**
 * MVI Contract for the {Feature} screen.
 * 
 * Follows unidirectional data flow:
 * UI -> Intent -> ViewModel -> State -> UI
 *                          -> Effect (one-time events)
 */
object {Feature}Contract {

    /**
     * UI State for the {Feature} screen.
     * Must be immutable - use copy() for updates.
     */
    data class State(
        // Loading states
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isSubmitting: Boolean = false,
        
        // Data
        val {entities}: List<{Entity}> = emptyList(),
        val selected{Entity}: {Entity}? = null,
        
        // Filters & Search
        val searchQuery: String = "",
        val selectedFilter: {Filter}? = null,
        
        // Form fields (for create/edit screens)
        val name: String = "",
        val nameError: String? = null,
        
        // Error state
        val error: String? = null
    ) : UiState {
        /**
         * Computed property for filtered list
         */
        val filtered{Entities}: List<{Entity}>
            get() = {entities}.filter { entity ->
                searchQuery.isBlank() || 
                entity.name.contains(searchQuery, ignoreCase = true)
            }
        
        /**
         * Computed property for form validation
         */
        val isFormValid: Boolean
            get() = name.isNotBlank() && nameError == null
    }

    /**
     * User intents (actions) for the {Feature} screen.
     * Each intent represents a user action or system event.
     */
    sealed interface Intent : UiIntent {
        // Data loading
        data object Load{Entities} : Intent
        data object Refresh{Entities} : Intent
        
        // Navigation triggers
        data class Select{Entity}(val {entity}Id: String) : Intent
        data object Add{Entity} : Intent
        data class Edit{Entity}(val {entity}Id: String) : Intent
        data class Delete{Entity}(val {entity}Id: String) : Intent
        
        // Search & Filter
        data class Search(val query: String) : Intent
        data class ApplyFilter(val filter: {Filter}?) : Intent
        data object ClearFilters : Intent
        
        // Form input (for create/edit screens)
        data class UpdateName(val name: String) : Intent
        data class UpdateField(val field: String, val value: String) : Intent
        
        // Form submission
        data object Submit : Intent
        data object Cancel : Intent
        
        // Confirmation dialogs
        data object ConfirmDelete : Intent
        data object DismissDialog : Intent
        
        // Error handling
        data object DismissError : Intent
        data object Retry : Intent
    }

    /**
     * Side effects for one-time events.
     * These are consumed once and don't persist in state.
     */
    sealed interface Effect : UiEffect {
        // Navigation
        data class NavigateTo{Entity}Detail(val {entity}Id: String) : Effect
        data object NavigateToAdd{Entity} : Effect
        data class NavigateToEdit{Entity}(val {entity}Id: String) : Effect
        data object NavigateBack : Effect
        
        // Feedback
        data class ShowSnackbar(val message: String) : Effect
        data class ShowError(val message: String) : Effect
        data object ShowDeleteConfirmation : Effect
        
        // Success callbacks
        data object {Entity}Created : Effect
        data object {Entity}Updated : Effect
        data object {Entity}Deleted : Effect
    }
}
```

## Usage Guidelines

### State Best Practices
- All fields must have default values
- Use `null` for optional/error states
- Add computed properties for derived state
- Keep state flat (avoid deep nesting)

### Intent Naming Conventions
- Load/Refresh for data fetching
- Select/Add/Edit/Delete for CRUD operations
- Update{Field} for form inputs
- Submit/Cancel for form actions
- Confirm/Dismiss for dialogs

### Effect Types
- **Navigation**: Screen transitions
- **Snackbar/Toast**: Feedback messages
- **Dialog**: Confirmation prompts
- **Success callbacks**: Notify parent screens

## Common Patterns

### List Screen Contract
```kotlin
data class State(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false
) : UiState

sealed interface Intent : UiIntent {
    data object LoadItems : Intent
    data object RefreshItems : Intent
    data class SearchItems(val query: String) : Intent
    data class SelectItem(val itemId: String) : Intent
    data object AddItem : Intent
}

sealed interface Effect : UiEffect {
    data class NavigateToDetail(val itemId: String) : Effect
    data object NavigateToAdd : Effect
    data class ShowSnackbar(val message: String) : Effect
}
```

### Detail Screen Contract
```kotlin
data class State(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
) : UiState

sealed interface Intent : UiIntent {
    data object LoadItem : Intent
    data object EditItem : Intent
    data object DeleteItem : Intent
    data object ConfirmDelete : Intent
    data object DismissDialog : Intent
}

sealed interface Effect : UiEffect {
    data object NavigateToEdit : Effect
    data object NavigateBack : Effect
    data class ShowSnackbar(val message: String) : Effect
}
```

### Form Screen Contract
```kotlin
data class State(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    
    // Form fields
    val name: String = "",
    val nameError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val date: String = "",  // DD-MM-YYYY format
    val dateError: String? = null,
    
    val error: String? = null
) : UiState {
    val isFormValid: Boolean
        get() = name.isNotBlank() && 
                nameError == null && 
                emailError == null &&
                dateError == null
}

sealed interface Intent : UiIntent {
    data class UpdateName(val value: String) : Intent
    data class UpdateEmail(val value: String) : Intent
    data class UpdateDate(val value: String) : Intent
    data object Submit : Intent
    data object Cancel : Intent
}

sealed interface Effect : UiEffect {
    data object NavigateBack : Effect
    data object SubmitSuccess : Effect
    data class ShowSnackbar(val message: String) : Effect
}
```

