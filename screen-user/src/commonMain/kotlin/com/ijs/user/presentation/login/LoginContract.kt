package com.ijs.user.presentation.login

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText

/**
 * MVI Contract for the Login screen.
 */
object LoginContract {

    enum class LoginMode { EMAIL, MOBILE }

    data class State(
        val loginMode: LoginMode = LoginMode.EMAIL,
        val identifier: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val isCheckingAuth: Boolean = true,
        val error: UiText? = null,
        val identifierError: UiText? = null,
        val passwordError: UiText? = null,
        val isPasswordVisible: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data class UpdateIdentifier(val value: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class SwitchLoginMode(val mode: LoginMode) : Intent
        data object TogglePasswordVisibility : Intent
        data object Login : Intent
        data object ClearError : Intent
        data object CheckAuthStatus : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToDashboard : Effect
        data class ShowError(val message: UiText) : Effect
        data class NavigateToOtpVerification(
            val email: String,
            val mobile: String,
            val needsEmailVerification: Boolean,
            val needsMobileVerification: Boolean
        ) : Effect
    }
}
