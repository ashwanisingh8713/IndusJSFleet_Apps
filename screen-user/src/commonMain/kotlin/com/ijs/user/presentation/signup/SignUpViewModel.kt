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
            is SignUpContract.Intent.UpdateFirstName -> updateFirstName(intent.firstName)
            is SignUpContract.Intent.UpdateLastName -> updateLastName(intent.lastName)
            is SignUpContract.Intent.UpdateEmail -> updateEmail(intent.email)
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

    private fun updateFirstName(firstName: String) {
        updateState { copy(firstName = firstName, firstNameError = nameInlineError(firstName, isFirstName = true)) }
    }

    private fun updateLastName(lastName: String) {
        updateState { copy(lastName = lastName, lastNameError = nameInlineError(lastName, isFirstName = false)) }
    }

    private fun updateEmail(email: String) {
        updateState { copy(email = email, emailError = emailInlineError(email)) }
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

    /**
     * Inline name validation while typing. Uses [ValidationUtils.getNameError] for the
     * rule (2–50 chars, letters/space/.'- only) but maps to a localized resource so the
     * message stays English/Hindi-correct. Empty is not an error while typing — the
     * required check happens on submit.
     *
     * TODO(i18n): swap [Res.string.error_validation] for dedicated
     * error_first_name_invalid / error_last_name_invalid once those strings are added.
     */
    private fun nameInlineError(name: String, isFirstName: Boolean): UiText? {
        if (name.isBlank()) return null
        val fieldName = if (isFirstName) "First name" else "Last name"
        val ruleError = ValidationUtils.getNameError(name = name, fieldName = fieldName, required = false)
        return if (ruleError == null) null else UiText.StringRes(Res.string.error_validation)
    }

    /** Inline email validation while typing; empty defers to the submit-time required check. */
    private fun emailInlineError(email: String): UiText? = when {
        email.isBlank() -> null
        ValidationUtils.getEmailError(email, required = false) != null ->
            UiText.StringRes(Res.string.error_email_invalid)
        else -> null
    }

    private suspend fun signUp() {
        val firstName = currentState.firstName.trim()
        val lastName = currentState.lastName.trim()
        val email = currentState.email.trim()
        val mobile = currentState.mobile.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword

        // Validation — surface failures inline on the offending field as well as the banner.
        if (firstName.isEmpty()) {
            updateState {
                copy(
                    firstNameError = UiText.StringRes(Res.string.error_first_name_required),
                    error = UiText.StringRes(Res.string.error_first_name_required)
                )
            }
            return
        }

        if (!ValidationUtils.isValidName(firstName)) {
            updateState {
                copy(
                    firstNameError = UiText.StringRes(Res.string.error_validation),
                    error = UiText.StringRes(Res.string.error_validation)
                )
            }
            return
        }

        if (lastName.isEmpty()) {
            updateState {
                copy(
                    lastNameError = UiText.StringRes(Res.string.error_last_name_required),
                    error = UiText.StringRes(Res.string.error_last_name_required)
                )
            }
            return
        }

        if (!ValidationUtils.isValidName(lastName)) {
            updateState {
                copy(
                    lastNameError = UiText.StringRes(Res.string.error_validation),
                    error = UiText.StringRes(Res.string.error_validation)
                )
            }
            return
        }

        if (email.isEmpty()) {
            updateState {
                copy(
                    emailError = UiText.StringRes(Res.string.error_email_required),
                    error = UiText.StringRes(Res.string.error_email_required)
                )
            }
            return
        }

        if (!isValidEmail(email)) {
            updateState {
                copy(
                    emailError = UiText.StringRes(Res.string.error_email_invalid),
                    error = UiText.StringRes(Res.string.error_email_invalid)
                )
            }
            return
        }

        if (mobile.isEmpty()) {
            updateState {
                copy(
                    mobileError = UiText.StringRes(Res.string.error_mobile_required),
                    error = UiText.StringRes(Res.string.error_mobile_required)
                )
            }
            return
        }

        if (!isValidIndianMobile(mobile)) {
            updateState {
                copy(
                    mobileError = UiText.StringRes(Res.string.error_mobile_invalid),
                    error = UiText.StringRes(Res.string.error_mobile_invalid)
                )
            }
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
                    onFailure = { error -> applySignUpFailure(error.message) }
                )
            } catch (e: Exception) {
                applySignUpFailure(e.message)
            }
        }
    }

    /**
     * Maps a sign-up failure message to a clear, localized error and surfaces it on
     * the banner plus the offending field. The backend distinguishes a duplicate
     * mobile from a duplicate email, but the wire message is English (the network
     * error layer is not localized), so we keyword-match it here — the same approach
     * [com.ijs.user.presentation.login.LoginViewModel] uses for "not verified".
     */
    private fun applySignUpFailure(raw: String?) {
        val m = raw?.lowercase().orEmpty()
        val isDuplicate = m.contains("exist") || m.contains("already") || m.contains("registered")
        when {
            isDuplicate && (m.contains("mobile") || m.contains("phone")) -> {
                val msg = UiText.StringRes(Res.string.error_signup_mobile_exists)
                updateState { copy(isLoading = false, error = msg, mobileError = msg) }
            }
            isDuplicate && (m.contains("email") || m.contains("account") || m.contains("user")) -> {
                val msg = UiText.StringRes(Res.string.error_signup_email_exists)
                updateState { copy(isLoading = false, error = msg, emailError = msg) }
            }
            isDuplicate -> {
                updateState { copy(isLoading = false, error = UiText.StringRes(Res.string.error_account_exists_generic)) }
            }
            !raw.isNullOrBlank() -> {
                updateState { copy(isLoading = false, error = UiText.Raw(raw)) }
            }
            else -> {
                updateState { copy(isLoading = false, error = UiText.StringRes(Res.string.error_signup_failed)) }
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

