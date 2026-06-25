package com.ijs.user.presentation.login

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.handler.ErrorClassifier
import com.indusjs.error.handler.ErrorType
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import com.ijs.user.presentation.login.LoginContract.Effect
import com.ijs.user.presentation.login.LoginContract.Intent
import com.ijs.user.presentation.login.LoginContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class LoginViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        viewModelScope.launch {
            checkAuthStatus()
        }
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateIdentifier -> updateState {
                copy(identifier = intent.value, identifierError = null, error = null)
            }
            is Intent.UpdatePassword -> updateState {
                copy(password = intent.password, passwordError = null, error = null)
            }
            is Intent.SwitchLoginMode -> updateState {
                copy(loginMode = intent.mode, identifier = "", identifierError = null, error = null)
            }
            is Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is Intent.Login -> login()
            is Intent.ClearError -> updateState { copy(error = null, identifierError = null, passwordError = null) }
            is Intent.CheckAuthStatus -> checkAuthStatus()
        }
    }

    private suspend fun checkAuthStatus() {
        withContext(dispatcherProvider.io) {
            try {
                val isLoggedIn = userRepository.isLoggedIn()
                if (isLoggedIn) {
                    sendEffect(Effect.NavigateToDashboard)
                }
            } catch (_: Exception) {
            } finally {
                updateState { copy(isCheckingAuth = false) }
            }
        }
    }

    private suspend fun login() {
        val identifier = currentState.identifier.trim()
        val password = currentState.password
        val isEmailMode = currentState.loginMode == LoginContract.LoginMode.EMAIL

        // Inline identifier validation, per selected mode.
        val identifierError: UiText? = when {
            identifier.isEmpty() -> UiText.StringRes(
                if (isEmailMode) Res.string.login_error_email_required
                else Res.string.login_error_mobile_required
            )
            isEmailMode && !ValidationUtils.isValidEmail(identifier) ->
                UiText.StringRes(Res.string.error_email_invalid)
            !isEmailMode && !ValidationUtils.isValidIndianMobile(identifier) ->
                UiText.StringRes(Res.string.error_mobile_invalid)
            else -> null
        }

        // Inline password validation (presence only; policy stays backend-only).
        val passwordError: UiText? = if (password.isEmpty())
            UiText.StringRes(Res.string.login_error_password_required)
        else null

        if (identifierError != null || passwordError != null) {
            updateState {
                copy(identifierError = identifierError, passwordError = passwordError, error = null)
            }
            return
        }

        updateState {
            copy(isLoading = true, error = null, identifierError = null, passwordError = null)
        }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.login(
                    identifier = identifier,
                    password = password
                )

                result.fold(
                    onSuccess = {
                        updateState { copy(isLoading = false) }
                        sendEffect(Effect.NavigateToDashboard)
                    },
                    onFailure = { throwable ->
                        val errorMsg = throwable.message ?: ""

                        val emailNotVerified = errorMsg.contains("email", ignoreCase = true)
                            && errorMsg.contains("not verified", ignoreCase = true)
                        val mobileNotVerified = errorMsg.contains("mobile", ignoreCase = true)
                            && errorMsg.contains("not verified", ignoreCase = true)

                        if (emailNotVerified || mobileNotVerified) {
                            updateState { copy(isLoading = false) }
                            val isEmailIdentifier = currentState.loginMode == LoginContract.LoginMode.EMAIL
                            val email = if (isEmailIdentifier) identifier else ""
                            val mobile = if (!isEmailIdentifier) identifier else ""
                            sendEffect(Effect.NavigateToOtpVerification(
                                email = email,
                                mobile = mobile,
                                needsEmailVerification = emailNotVerified,
                                needsMobileVerification = mobileNotVerified
                            ))
                        } else {
                            updateState {
                                copy(
                                    isLoading = false,
                                    error = classifyError(throwable)
                                )
                            }
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

    private fun classifyError(throwable: Throwable): UiText {
        val msg = throwable.message
        if (!msg.isNullOrBlank()) {
            return UiText.Raw(msg)
        }

        val errorType = ErrorClassifier.classifyFromException(throwable)
        return when (errorType) {
            ErrorType.NETWORK_CONNECTION -> UiText.StringRes(Res.string.error_network)
            ErrorType.NETWORK_TIMEOUT -> UiText.StringRes(Res.string.error_timeout)
            ErrorType.SERVER_ERROR -> UiText.StringRes(Res.string.error_server)
            ErrorType.RATE_LIMITED -> UiText.StringRes(Res.string.error_rate_limited)
            else -> UiText.StringRes(Res.string.error_generic)
        }
    }
}
