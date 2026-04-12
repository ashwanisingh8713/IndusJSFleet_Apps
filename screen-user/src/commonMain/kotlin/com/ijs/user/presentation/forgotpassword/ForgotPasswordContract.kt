package com.ijs.user.presentation.forgotpassword

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText

/**
 * MVI Contract for the Forgot Password screen.
 */
object ForgotPasswordContract {

    /**
     * UI State for the Forgot Password screen.
     */
    data class State(
        val identifier: String = "",
        val isLoading: Boolean = false,
        val error: UiText? = null,
        val isSuccess: Boolean = false,
        // Reset Password fields
        val isResetMode: Boolean = false,
        val resetToken: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false
    ) : UiState

    /**
     * User intents for the Forgot Password screen.
     */
    sealed interface Intent : UiIntent {
        data class UpdateIdentifier(val identifier: String) : Intent
        data object SubmitForgotPassword : Intent
        data class UpdateResetToken(val token: String) : Intent
        data class UpdateNewPassword(val password: String) : Intent
        data class UpdateConfirmPassword(val password: String) : Intent
        data object TogglePasswordVisibility : Intent
        data object ToggleConfirmPasswordVisibility : Intent
        data object ResetPassword : Intent
        data object ClearError : Intent
        data object NavigateToLogin : Intent
    }

    /**
     * Side effects for the Forgot Password screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
        data object NavigateToLogin : Effect
        data object ShowResetPassword : Effect
    }
}
