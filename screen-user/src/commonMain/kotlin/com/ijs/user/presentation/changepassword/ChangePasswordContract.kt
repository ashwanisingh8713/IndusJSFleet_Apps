package com.ijs.user.presentation.changepassword

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText

/**
 * MVI Contract for the Change Password screen.
 */
object ChangePasswordContract {

    /**
     * UI State for the Change Password screen.
     */
    data class State(
        val currentPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: UiText? = null
    ) : UiState

    /**
     * User intents for the Change Password screen.
     */
    sealed interface Intent : UiIntent {
        data class UpdateCurrentPassword(val password: String) : Intent
        data class UpdateNewPassword(val password: String) : Intent
        data class UpdateConfirmPassword(val password: String) : Intent
        data object ChangePassword : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for the Change Password screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
        data object PasswordChanged : Effect
        data object NavigateBack : Effect
    }
}
