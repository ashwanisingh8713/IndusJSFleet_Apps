package com.indusjs.fleet.presentation.user.signup

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState

/**
 * MVI Contract for the Sign Up screen.
 */
object SignUpContract {

    /**
     * UI State for the Sign Up screen.
     */
    data class State(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val mobile: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : UiState

    /**
     * User intents for the Sign Up screen.
     */
    sealed interface Intent : UiIntent {
        data class UpdateFirstName(val firstName: String) : Intent
        data class UpdateLastName(val lastName: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdateMobile(val mobile: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent
        data object TogglePasswordVisibility : Intent
        data object ToggleConfirmPasswordVisibility : Intent
        data object SignUp : Intent
        data object ClearError : Intent
        data object NavigateToLogin : Intent
    }

    /**
     * Side effects for the Sign Up screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateToDashboard : Effect
        data object NavigateToLogin : Effect
    }
}

