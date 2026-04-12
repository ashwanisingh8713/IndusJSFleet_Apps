package com.ijs.user.presentation.otp

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.withContext

@Inject
class OtpVerificationViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<OtpVerificationContract.State, OtpVerificationContract.Intent, OtpVerificationContract.Effect>(
    OtpVerificationContract.State()
) {

    fun initialize(
        email: String,
        mobile: String,
        needsEmail: Boolean = email.isNotBlank(),
        needsMobile: Boolean = mobile.isNotBlank()
    ) {
        updateState {
            copy(
                email = email,
                mobile = mobile,
                needsEmailVerification = needsEmail,
                needsMobileVerification = needsMobile
            )
        }
    }

    override suspend fun handleIntent(intent: OtpVerificationContract.Intent) {
        when (intent) {
            is OtpVerificationContract.Intent.UpdateEmailOtp -> {
                val filtered = intent.otp.filter { it.isDigit() }.take(6)
                updateState { copy(emailOtp = filtered, emailError = null) }
            }
            is OtpVerificationContract.Intent.UpdateMobileOtp -> {
                val filtered = intent.otp.filter { it.isDigit() }.take(4)
                updateState { copy(mobileOtp = filtered, mobileError = null) }
            }
            is OtpVerificationContract.Intent.VerifyEmail -> verifyEmail()
            is OtpVerificationContract.Intent.VerifyMobile -> verifyMobile()
            is OtpVerificationContract.Intent.ResendEmailOtp -> resendEmailOtp()
            is OtpVerificationContract.Intent.ResendMobileOtp -> resendMobileOtp()
            is OtpVerificationContract.Intent.ClearError -> updateState { copy(error = null, emailError = null, mobileError = null) }
            is OtpVerificationContract.Intent.NavigateToLogin -> sendEffect(OtpVerificationContract.Effect.NavigateToLogin)
        }
    }

    private suspend fun verifyEmail() {
        val otp = currentState.emailOtp.trim()
        if (otp.length != 6) {
            updateState { copy(emailError = UiText.StringRes(Res.string.otp_error_6_digits)) }
            return
        }

        updateState { copy(isEmailVerifying = true, emailError = null) }

        withContext(dispatcherProvider.io) {
            val result = userRepository.verifyEmailOtp(currentState.email, otp)
            result.fold(
                onSuccess = {
                    updateState { copy(isEmailVerifying = false, isEmailVerified = true) }
                    sendEffect(OtpVerificationContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.otp_email_verified)))
                    checkAllVerified()
                },
                onFailure = { error ->
                    updateState {
                        copy(
                            isEmailVerifying = false,
                            emailError = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.otp_email_verify_failed)
                        )
                    }
                }
            )
        }
    }

    private suspend fun verifyMobile() {
        val otp = currentState.mobileOtp.trim()
        if (otp.length != 4) {
            updateState { copy(mobileError = UiText.StringRes(Res.string.otp_error_4_digits)) }
            return
        }

        updateState { copy(isMobileVerifying = true, mobileError = null) }

        withContext(dispatcherProvider.io) {
            val result = userRepository.verifyMobile(otp)
            result.fold(
                onSuccess = {
                    updateState { copy(isMobileVerifying = false, isMobileVerified = true) }
                    sendEffect(OtpVerificationContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.otp_mobile_verified)))
                    checkAllVerified()
                },
                onFailure = { error ->
                    updateState {
                        copy(
                            isMobileVerifying = false,
                            mobileError = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.otp_mobile_verify_failed)
                        )
                    }
                }
            )
        }
    }

    private suspend fun resendEmailOtp() {
        updateState { copy(isResendingEmail = true) }
        withContext(dispatcherProvider.io) {
            // Re-trigger signup to resend email OTP
            val result = userRepository.signUp(
                email = currentState.email,
                mobile = currentState.mobile,
                password = "",
                firstName = "",
                lastName = ""
            )
            // Even if this fails, the original OTP may still be valid
            updateState { copy(isResendingEmail = false) }
            sendEffect(OtpVerificationContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.otp_resend_email_sent)))
        }
    }

    private suspend fun resendMobileOtp() {
        updateState { copy(isResendingMobile = true) }
        withContext(dispatcherProvider.io) {
            val result = userRepository.signUp(
                email = currentState.email,
                mobile = currentState.mobile,
                password = "",
                firstName = "",
                lastName = ""
            )
            updateState { copy(isResendingMobile = false) }
            sendEffect(OtpVerificationContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.otp_resend_mobile_sent)))
        }
    }

    private suspend fun checkAllVerified() {
        val s = currentState
        val emailDone = !s.needsEmailVerification || s.isEmailVerified
        val mobileDone = !s.needsMobileVerification || s.isMobileVerified
        if (emailDone && mobileDone) {
            sendEffect(OtpVerificationContract.Effect.VerificationComplete)
        }
    }
}
