package com.indusjs.fleet.feature.user.presentation.changepassword

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState

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
        val isCurrentPasswordVisible: Boolean = false,
        val isNewPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : UiState

    /**
     * User intents for the Change Password screen.
     */
    sealed interface Intent : UiIntent {
        data class UpdateCurrentPassword(val password: String) : Intent
        data class UpdateNewPassword(val password: String) : Intent
        data class UpdateConfirmPassword(val password: String) : Intent
        data object ToggleCurrentPasswordVisibility : Intent
        data object ToggleNewPasswordVisibility : Intent
        data object ToggleConfirmPasswordVisibility : Intent
        data object ChangePassword : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for the Change Password screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object PasswordChanged : Effect
        data object NavigateBack : Effect
    }
}

