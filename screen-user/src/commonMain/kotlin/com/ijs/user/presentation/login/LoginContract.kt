package com.ijs.user.presentation.login

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState

/**
 * MVI Contract for the Login screen.
 */
object LoginContract {

    /**
     * UI State for the Login screen.
     */
    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isCheckingAuth: Boolean = true, // Initially checking auth status
        val error: String? = null,
        val isPasswordVisible: Boolean = false
    ) : UiState

    /**
     * User intents for the Login screen.
     */
    sealed interface Intent : UiIntent {
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data object TogglePasswordVisibility : Intent
        data object Login : Intent
        data object ClearError : Intent
        data object CheckAuthStatus : Intent // Check if already logged in
    }

    /**
     * Side effects for the Login screen.
     */
    sealed interface Effect : UiEffect {
        data object NavigateToDashboard : Effect
        data class ShowError(val message: String) : Effect
    }
}

