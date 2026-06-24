package com.ijs.user.presentation.changepassword

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Change Password screen implementing MVI pattern.
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
            updateState { copy(error = UiText.StringRes(Res.string.error_current_password_required)) }
            return
        }

        if (newPassword.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_new_password_required)) }
            return
        }

        // Password policy (length/complexity) is enforced by the backend only.

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_confirm_new_password_required)) }
            return
        }

        if (newPassword != confirmPassword) {
            updateState { copy(error = UiText.StringRes(Res.string.error_passwords_mismatch)) }
            return
        }

        if (currentPassword == newPassword) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_same_as_current)) }
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
                        sendEffect(ChangePasswordContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_password_changed)))
                        sendEffect(ChangePasswordContract.Effect.NavigateBack)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.error_change_password_failed)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = if (!e.message.isNullOrBlank()) UiText.Raw(e.message!!) else UiText.StringRes(Res.string.error_change_password_failed)
                    )
                }
            }
        }
    }
}
