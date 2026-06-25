package com.ijs.user.presentation.signup

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText

/**
 * MVI Contract for the Sign Up screen.
 */
object SignUpContract {

    /**
     * UI State for the Sign Up screen.
     */
    data class State(
        val firstName: String = "",
        val firstNameError: UiText? = null,
        val lastName: String = "",
        val lastNameError: UiText? = null,
        val email: String = "",
        val emailError: UiText? = null,
        val mobile: String = "",
        val mobileError: UiText? = null,
        val password: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val error: UiText? = null
    ) : UiState {

        /** Confirm-password mismatch is a UI-only check (password policy stays backend-only). */
        val isConfirmPasswordMismatch: Boolean
            get() = confirmPassword.isNotEmpty() && confirmPassword != password

        /**
         * Whether every field is filled and individually valid, so the submit
         * action can be enabled. Mirrors the per-field validation in the ViewModel
         * (name/email/mobile via ValidationUtils; passwords must be non-empty and match).
         */
        val isSubmitEnabled: Boolean
            get() = firstNameError == null && firstName.isNotBlank() &&
                lastNameError == null && lastName.isNotBlank() &&
                emailError == null && email.isNotBlank() &&
                mobileError == null && mobile.isNotBlank() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() && !isConfirmPasswordMismatch
    }

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
        data class ShowSnackbar(val message: UiText) : Effect
        data class NavigateToOtpVerification(val email: String, val mobile: String, val message: String, val isResend: Boolean) : Effect
        data object NavigateToLogin : Effect
    }
}
