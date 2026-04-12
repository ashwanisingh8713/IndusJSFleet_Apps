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
import com.indusjs.fleet.core.debug.postDebugLog9fbb5d
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
            is Intent.UpdateIdentifier -> updateState { copy(identifier = intent.value) }
            is Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is Intent.SwitchLoginMode -> updateState { copy(loginMode = intent.mode, identifier = "", error = null) }
            is Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is Intent.Login -> login()
            is Intent.ClearError -> updateState { copy(error = null) }
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

        if (identifier.isEmpty()) {
            val errorRes = if (currentState.loginMode == LoginContract.LoginMode.EMAIL)
                Res.string.login_error_email_required
            else
                Res.string.login_error_mobile_required
            updateState { copy(error = UiText.StringRes(errorRes)) }
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
                    identifier = identifier,
                    password = password
                )

                result.fold(
                    onSuccess = {
                        // #region agent log
                        postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"login_nav","location":"LoginVM:login:onSuccess","message":"login_success_navigating_to_dashboard","data":{"identifier":"${identifier.take(20)}"},"timestamp":0}""")
                        // #endregion
                        updateState { copy(isLoading = false) }
                        sendEffect(Effect.NavigateToDashboard)
                    },
                    onFailure = { throwable ->
                        val errorMsg = throwable.message ?: ""
                        // #region agent log
                        postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"login_nav","location":"LoginVM:login:onFailure","message":"login_failed","data":{"error":"${errorMsg.take(150).replace("\"","'")}","loginMode":"${currentState.loginMode}","identifier":"${identifier.take(20)}"},"timestamp":0}""")
                        // #endregion

                        val emailNotVerified = errorMsg.contains("email", ignoreCase = true)
                            && errorMsg.contains("not verified", ignoreCase = true)
                        val mobileNotVerified = errorMsg.contains("mobile", ignoreCase = true)
                            && errorMsg.contains("not verified", ignoreCase = true)

                        if (emailNotVerified || mobileNotVerified) {
                            updateState { copy(isLoading = false) }
                            val isEmailIdentifier = currentState.loginMode == LoginContract.LoginMode.EMAIL
                            val email = if (isEmailIdentifier) identifier else ""
                            val mobile = if (!isEmailIdentifier) identifier else ""
                            // #region agent log
                            postDebugLog9fbb5d("""{"sessionId":"9fbb5d","hypothesisId":"login_nav","location":"LoginVM:login:navToOtp","message":"navigating_to_otp","data":{"emailNotVerified":$emailNotVerified,"mobileNotVerified":$mobileNotVerified,"email":"${email.take(20)}","mobile":"${mobile.take(20)}"},"timestamp":0}""")
                            // #endregion
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
