package com.ijs.user.presentation.forgotpassword

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Forgot Password screen implementing MVI pattern.
 */
@Inject
class ForgotPasswordViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<ForgotPasswordContract.State, ForgotPasswordContract.Intent, ForgotPasswordContract.Effect>(ForgotPasswordContract.State()) {

    override suspend fun handleIntent(intent: ForgotPasswordContract.Intent) {
        when (intent) {
            is ForgotPasswordContract.Intent.UpdateIdentifier -> updateState { copy(identifier = intent.identifier) }
            is ForgotPasswordContract.Intent.SubmitForgotPassword -> submitForgotPassword()
            is ForgotPasswordContract.Intent.UpdateNewPassword -> updateState { copy(newPassword = intent.password) }
            is ForgotPasswordContract.Intent.UpdateConfirmPassword -> updateState { copy(confirmPassword = intent.password) }
            is ForgotPasswordContract.Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is ForgotPasswordContract.Intent.ToggleConfirmPasswordVisibility -> updateState { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
            is ForgotPasswordContract.Intent.ResetPassword -> resetPassword()
            is ForgotPasswordContract.Intent.ClearError -> updateState { copy(error = null) }
            is ForgotPasswordContract.Intent.NavigateToLogin -> sendEffect(ForgotPasswordContract.Effect.NavigateToLogin)
        }
    }

    private suspend fun submitForgotPassword() {
        val identifier = currentState.identifier.trim()

        // Validation
        if (identifier.isEmpty()) {
            updateState { copy(error = "Please enter your email or mobile number") }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.forgotPassword(identifier)

                result.fold(
                    onSuccess = {
                        updateState {
                            copy(
                                isLoading = false,
                                isSuccess = true,
                                isResetMode = true
                            )
                        }
                        sendEffect(ForgotPasswordContract.Effect.ShowSnackbar("Reset instructions sent. Please enter your new password."))
                        sendEffect(ForgotPasswordContract.Effect.ShowResetPassword)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to send reset instructions"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to send reset instructions"
                    )
                }
            }
        }
    }

    private suspend fun resetPassword() {
        val identifier = currentState.identifier.trim()
        val newPassword = currentState.newPassword
        val confirmPassword = currentState.confirmPassword

        // Validation
        if (newPassword.isEmpty()) {
            updateState { copy(error = "Please enter a new password") }
            return
        }

        if (newPassword.length < 6) {
            updateState { copy(error = "Password must be at least 6 characters") }
            return
        }

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = "Please confirm your password") }
            return
        }

        if (newPassword != confirmPassword) {
            updateState { copy(error = "Passwords do not match") }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.resetPassword(
                    identifier = identifier,
                    newPassword = newPassword
                )

                result.fold(
                    onSuccess = {
                        updateState { copy(isLoading = false) }
                        sendEffect(ForgotPasswordContract.Effect.ShowSnackbar("Password reset successfully!"))
                        sendEffect(ForgotPasswordContract.Effect.NavigateToLogin)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to reset password"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to reset password"
                    )
                }
            }
        }
    }
}

