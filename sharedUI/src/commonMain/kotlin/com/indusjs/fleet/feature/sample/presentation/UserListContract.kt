package com.indusjs.fleet.feature.sample.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.feature.sample.domain.entity.User

/**
 * MVI Contract for the User List screen.
 * Defines the state, intents, and effects for this feature.
 */
object UserListContract {

    /**
     * UI State representing all possible states of the User List screen.
     */
    data class State(
        val isLoading: Boolean = false,
        val users: List<User> = emptyList(),
        val error: String? = null,
        val selectedUser: User? = null
    ) : UiState

    /**
     * User intents (actions) that can be performed on the User List screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadUsers : Intent
        data object RefreshUsers : Intent
        data class SelectUser(val userId: String) : Intent
        data class DeleteUser(val userId: String) : Intent
        data object ClearError : Intent
    }

    /**
     * One-time side effects for the User List screen.
     */
    sealed interface Effect : UiEffect {
        data class NavigateToUserDetail(val userId: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
        data object UserDeleted : Effect
    }
}

