package com.ijs.user.presentation.signup

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Sign Up screen implementing MVI pattern.
 */
@Inject
class SignUpViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<SignUpContract.State, SignUpContract.Intent, SignUpContract.Effect>(SignUpContract.State()) {

    override suspend fun handleIntent(intent: SignUpContract.Intent) {
        when (intent) {
            is SignUpContract.Intent.UpdateFirstName -> updateState { copy(firstName = intent.firstName) }
            is SignUpContract.Intent.UpdateLastName -> updateState { copy(lastName = intent.lastName) }
            is SignUpContract.Intent.UpdateEmail -> updateState { copy(email = intent.email) }
            is SignUpContract.Intent.UpdateMobile -> updateMobile(intent.mobile)
            is SignUpContract.Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is SignUpContract.Intent.UpdateConfirmPassword -> updateState { copy(confirmPassword = intent.confirmPassword) }
            is SignUpContract.Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is SignUpContract.Intent.ToggleConfirmPasswordVisibility -> updateState { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
            is SignUpContract.Intent.SignUp -> signUp()
            is SignUpContract.Intent.ClearError -> updateState { copy(error = null) }
            is SignUpContract.Intent.NavigateToLogin -> sendEffect(SignUpContract.Effect.NavigateToLogin)
        }
    }

    private fun updateMobile(mobile: String) {
        // Only allow digits and limit to 10 characters
        val filteredMobile = mobile.filter { it.isDigit() }.take(10)

        val mobileError: UiText? = when {
            filteredMobile.isEmpty() -> null
            filteredMobile.length < 10 -> UiText.StringRes(Res.string.error_mobile_10_digits)
            !isValidIndianMobile(filteredMobile) -> UiText.StringRes(Res.string.error_mobile_start_digit)
            else -> null
        }

        updateState { copy(mobile = filteredMobile, mobileError = mobileError) }
    }

    private suspend fun signUp() {
        val firstName = currentState.firstName.trim()
        val lastName = currentState.lastName.trim()
        val email = currentState.email.trim()
        val mobile = currentState.mobile.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword

        // Validation
        if (firstName.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_first_name_required)) }
            return
        }

        if (lastName.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_last_name_required)) }
            return
        }

        if (email.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_email_required)) }
            return
        }

        if (!isValidEmail(email)) {
            updateState { copy(error = UiText.StringRes(Res.string.error_email_invalid)) }
            return
        }

        if (mobile.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_mobile_required)) }
            return
        }

        if (!isValidIndianMobile(mobile)) {
            updateState { copy(error = UiText.StringRes(Res.string.error_mobile_invalid)) }
            return
        }

        if (password.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_required)) }
            return
        }

        // Password policy (length/complexity) is enforced by the backend only.

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_confirm_password_required)) }
            return
        }

        if (password != confirmPassword) {
            updateState { copy(error = UiText.StringRes(Res.string.error_passwords_mismatch)) }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.signUp(
                    email = email,
                    mobile = mobile,
                    password = password,
                    firstName = firstName,
                    lastName = lastName
                )

                result.fold(
                    onSuccess = { signUpResult ->
                        updateState { copy(isLoading = false) }
                        sendEffect(SignUpContract.Effect.NavigateToOtpVerification(
                            email = email,
                            mobile = mobile,
                            message = signUpResult.message,
                            isResend = signUpResult.isResend
                        ))
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.error_signup_failed)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = if (!e.message.isNullOrBlank()) UiText.Raw(e.message!!) else UiText.StringRes(Res.string.error_signup_failed)
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean = ValidationUtils.isValidEmail(email)

    /**
     * Validates Indian mobile number.
     * Indian mobile numbers:
     * - Must be exactly 10 digits
     * - Must start with 6, 7, 8, or 9
     */
    private fun isValidIndianMobile(mobile: String): Boolean = ValidationUtils.isValidIndianMobile(mobile)
}

