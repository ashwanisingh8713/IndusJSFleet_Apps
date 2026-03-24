package com.ijs.user.presentation.profile

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.user.UserProfile

/**
 * MVI Contract for the User Profile screen.
 */
object ProfileContract {

    /**
     * UI State for the Profile screen.
     */
    data class State(
        val isLoading: Boolean = false,
        val profile: UserProfile? = null,
        val error: String? = null,
        val isEditing: Boolean = false,
        val editFirstName: String = "",
        val editLastName: String = "",
        val editEmail: String = "",
        val editMobile: String = "",
        val isUpdating: Boolean = false,
        val updateError: String? = null
    ) : UiState

    /**
     * User intents for the Profile screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadProfile : Intent
        data object RefreshProfile : Intent
        data object StartEditing : Intent
        data object CancelEditing : Intent
        data class UpdateFirstName(val firstName: String) : Intent
        data class UpdateLastName(val lastName: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdateMobile(val mobile: String) : Intent
        data object SaveProfile : Intent
        data object ClearError : Intent
        data object NavigateToChangePassword : Intent
        data object Logout : Intent
    }

    /**
     * Side effects for the Profile screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateToChangePassword : Effect
        data object NavigateToLogin : Effect
        data object ProfileUpdated : Effect
    }
}

