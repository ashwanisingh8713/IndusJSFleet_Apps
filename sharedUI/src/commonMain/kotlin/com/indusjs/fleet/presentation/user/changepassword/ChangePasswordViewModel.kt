package com.indusjs.fleet.presentation.user.changepassword

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Change Password screen implementing MVI pattern.
 *
 * Dependencies are provided via DefaultViewModelProvider.
 */
@Inject
class ChangePasswordViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<ChangePasswordContract.State, ChangePasswordContract.Intent, ChangePasswordContract.Effect>(ChangePasswordContract.State()) {

    override suspend fun handleIntent(intent: ChangePasswordContract.Intent) {
        when (intent) {
            is ChangePasswordContract.Intent.UpdateCurrentPassword -> updateState { copy(currentPassword = intent.password) }
            is ChangePasswordContract.Intent.UpdateNewPassword -> updateState { copy(newPassword = intent.password) }
            is ChangePasswordContract.Intent.UpdateConfirmPassword -> updateState { copy(confirmPassword = intent.password) }
            is ChangePasswordContract.Intent.ToggleCurrentPasswordVisibility -> updateState { copy(isCurrentPasswordVisible = !isCurrentPasswordVisible) }
            is ChangePasswordContract.Intent.ToggleNewPasswordVisibility -> updateState { copy(isNewPasswordVisible = !isNewPasswordVisible) }
            is ChangePasswordContract.Intent.ToggleConfirmPasswordVisibility -> updateState { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
            is ChangePasswordContract.Intent.ChangePassword -> changePassword()
            is ChangePasswordContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun changePassword() {
        val currentPassword = currentState.currentPassword
        val newPassword = currentState.newPassword
        val confirmPassword = currentState.confirmPassword

        // Validation
        if (currentPassword.isEmpty()) {
            updateState { copy(error = "Please enter your current password") }
            return
        }

        if (newPassword.isEmpty()) {
            updateState { copy(error = "Please enter a new password") }
            return
        }

        if (newPassword.length < 6) {
            updateState { copy(error = "Password must be at least 6 characters") }
            return
        }

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = "Please confirm your new password") }
            return
        }

        if (newPassword != confirmPassword) {
            updateState { copy(error = "Passwords do not match") }
            return
        }

        if (currentPassword == newPassword) {
            updateState { copy(error = "New password must be different from current password") }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )

                result.fold(
                    onSuccess = {
                        updateState { copy(isLoading = false) }
                        sendEffect(ChangePasswordContract.Effect.PasswordChanged)
                        sendEffect(ChangePasswordContract.Effect.ShowSnackbar("Password changed successfully"))
                        sendEffect(ChangePasswordContract.Effect.NavigateBack)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to change password"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to change password"
                    )
                }
            }
        }
    }
}

