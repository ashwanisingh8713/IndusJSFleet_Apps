package com.ijs.user.presentation.otp

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText

object OtpVerificationContract {

    data class State(
        val email: String = "",
        val mobile: String = "",
        val emailOtp: String = "",
        val mobileOtp: String = "",
        val needsEmailVerification: Boolean = false,
        val needsMobileVerification: Boolean = false,
        val isEmailVerified: Boolean = false,
        val isMobileVerified: Boolean = false,
        val isEmailVerifying: Boolean = false,
        val isMobileVerifying: Boolean = false,
        val isResendingEmail: Boolean = false,
        val isResendingMobile: Boolean = false,
        val error: UiText? = null,
        val emailError: UiText? = null,
        val mobileError: UiText? = null
    ) : UiState

    sealed interface Intent : UiIntent {
        data class UpdateEmailOtp(val otp: String) : Intent
        data class UpdateMobileOtp(val otp: String) : Intent
        data object VerifyEmail : Intent
        data object VerifyMobile : Intent
        data object ResendEmailOtp : Intent
        data object ResendMobileOtp : Intent
        data object ClearError : Intent
        data object NavigateToLogin : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
        data object VerificationComplete : Effect
        data object NavigateToLogin : Effect
    }
}
