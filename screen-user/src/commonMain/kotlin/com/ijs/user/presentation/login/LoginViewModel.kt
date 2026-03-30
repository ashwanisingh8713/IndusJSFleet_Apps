package com.ijs.user.presentation.login

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.handler.ErrorClassifier
import com.indusjs.error.handler.ErrorType
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import com.ijs.user.presentation.login.LoginContract.Effect
import com.ijs.user.presentation.login.LoginContract.Intent
import com.ijs.user.presentation.login.LoginContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Login screen implementing MVI pattern.
 *
 * On initialization, checks if user is already logged in and auto-navigates to dashboard.
 */
@Inject
class LoginViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        // Check auth status on initialization
        viewModelScope.launch {
            checkAuthStatus()
        }
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateEmail -> updateState { copy(email = intent.email) }
            is Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is Intent.Login -> login()
            is Intent.ClearError -> updateState { copy(error = null) }
            is Intent.CheckAuthStatus -> checkAuthStatus()
        }
    }

    /**
     * Checks if user is already logged in.
     * If logged in, auto-navigates to dashboard.
     */
    private suspend fun checkAuthStatus() {
        withContext(dispatcherProvider.io) {
            try {
                val isLoggedIn = userRepository.isLoggedIn()
                if (isLoggedIn) {
                    // User is already logged in, navigate to dashboard
                    sendEffect(Effect.NavigateToDashboard)
                }
            } catch (_: Exception) {
                // If check fails, just show login screen
                // User will need to login manually
            } finally {
                updateState { copy(isCheckingAuth = false) }
            }
        }
    }

    private suspend fun login() {
        val email = currentState.email.trim()
        val password = currentState.password

        // Validate inputs
        if (email.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.login_error_email_required)) }
            return
        }
        if (password.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.login_error_password_required)) }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.login(
                    identifier = email,
                    password = password
                )

                result.fold(
                    onSuccess = {
                        updateState { copy(isLoading = false) }
                        sendEffect(Effect.NavigateToDashboard)
                    },
                    onFailure = { throwable ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = classifyError(throwable)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = classifyError(e)
                    )
                }
            }
        }
    }

    /**
     * Maps a [Throwable] to a [UiText] using [ErrorClassifier].
     */
    private fun classifyError(throwable: Throwable): UiText {
        val errorType = ErrorClassifier.classifyFromException(throwable)
        return when (errorType) {
            ErrorType.AUTHENTICATION -> UiText.StringRes(Res.string.login_error_invalid_credentials)
            ErrorType.NETWORK_CONNECTION -> UiText.StringRes(Res.string.error_network)
            ErrorType.NETWORK_TIMEOUT -> UiText.StringRes(Res.string.error_timeout)
            ErrorType.SERVER_ERROR -> UiText.StringRes(Res.string.error_server)
            ErrorType.AUTHORIZATION -> UiText.StringRes(Res.string.error_forbidden)
            ErrorType.NOT_FOUND -> UiText.StringRes(Res.string.error_not_found)
            ErrorType.RATE_LIMITED -> UiText.StringRes(Res.string.error_rate_limited)
            ErrorType.VALIDATION -> UiText.StringRes(Res.string.login_error_invalid_credentials)
            ErrorType.UNKNOWN -> {
                val msg = throwable.message
                if (!msg.isNullOrBlank()) {
                    UiText.Raw(msg)
                } else {
                    UiText.StringRes(Res.string.error_generic)
                }
            }
        }
    }
}
