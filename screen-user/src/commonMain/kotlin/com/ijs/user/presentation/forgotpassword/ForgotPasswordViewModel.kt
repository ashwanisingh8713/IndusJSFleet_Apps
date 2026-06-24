package com.ijs.user.presentation.forgotpassword

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
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
            is ForgotPasswordContract.Intent.UpdateResetToken -> updateState { copy(resetToken = intent.token) }
            is ForgotPasswordContract.Intent.UpdateNewPassword -> updateState { copy(newPassword = intent.password) }
            is ForgotPasswordContract.Intent.UpdateConfirmPassword -> updateState { copy(confirmPassword = intent.password) }
            is ForgotPasswordContract.Intent.ResetPassword -> resetPassword()
            is ForgotPasswordContract.Intent.ClearError -> updateState { copy(error = null) }
            is ForgotPasswordContract.Intent.NavigateToLogin -> sendEffect(ForgotPasswordContract.Effect.NavigateToLogin)
        }
    }

    private suspend fun submitForgotPassword() {
        val identifier = currentState.identifier.trim()

        // Validation
        if (identifier.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_identifier_required)) }
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
                        sendEffect(ForgotPasswordContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_reset_instructions_sent)))
                        sendEffect(ForgotPasswordContract.Effect.ShowResetPassword)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.error_send_reset_failed)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = if (!e.message.isNullOrBlank()) UiText.Raw(e.message!!) else UiText.StringRes(Res.string.error_send_reset_failed)
                    )
                }
            }
        }
    }

    private suspend fun resetPassword() {
        val resetToken = currentState.resetToken.trim()
        val newPassword = currentState.newPassword
        val confirmPassword = currentState.confirmPassword

        // Validation
        if (resetToken.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_reset_token_required)) }
            return
        }

        if (newPassword.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_new_password_required)) }
            return
        }

        // Password policy (length/complexity) is enforced by the backend only.

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_confirm_password_required)) }
            return
        }

        if (newPassword != confirmPassword) {
            updateState { copy(error = UiText.StringRes(Res.string.error_passwords_mismatch)) }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.resetPassword(
                    resetToken = resetToken,
                    newPassword = newPassword
                )

                result.fold(
                    onSuccess = {
                        updateState { copy(isLoading = false) }
                        sendEffect(ForgotPasswordContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_password_reset)))
                        sendEffect(ForgotPasswordContract.Effect.NavigateToLogin)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.error_reset_password_failed)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = if (!e.message.isNullOrBlank()) UiText.Raw(e.message!!) else UiText.StringRes(Res.string.error_reset_password_failed)
                    )
                }
            }
        }
    }
}
