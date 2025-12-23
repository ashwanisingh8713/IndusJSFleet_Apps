package com.indusjs.fleet.presentation.user.signup

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Sign Up screen implementing MVI pattern.
 *
 * Dependencies are injected via Metro DI through the UserFeatureGraph.
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
            is SignUpContract.Intent.UpdateMobile -> updateState { copy(mobile = intent.mobile) }
            is SignUpContract.Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is SignUpContract.Intent.UpdateConfirmPassword -> updateState { copy(confirmPassword = intent.confirmPassword) }
            is SignUpContract.Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is SignUpContract.Intent.ToggleConfirmPasswordVisibility -> updateState { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
            is SignUpContract.Intent.SignUp -> signUp()
            is SignUpContract.Intent.ClearError -> updateState { copy(error = null) }
            is SignUpContract.Intent.NavigateToLogin -> sendEffect(SignUpContract.Effect.NavigateToLogin)
        }
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
            updateState { copy(error = "Please enter your first name") }
            return
        }

        if (lastName.isEmpty()) {
            updateState { copy(error = "Please enter your last name") }
            return
        }

        if (email.isEmpty()) {
            updateState { copy(error = "Please enter your email") }
            return
        }

        if (!isValidEmail(email)) {
            updateState { copy(error = "Please enter a valid email address") }
            return
        }

        if (mobile.isEmpty()) {
            updateState { copy(error = "Please enter your mobile number") }
            return
        }

        if (mobile.length < 10) {
            updateState { copy(error = "Please enter a valid mobile number") }
            return
        }

        if (password.isEmpty()) {
            updateState { copy(error = "Please enter a password") }
            return
        }

        if (password.length < 6) {
            updateState { copy(error = "Password must be at least 6 characters") }
            return
        }

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = "Please confirm your password") }
            return
        }

        if (password != confirmPassword) {
            updateState { copy(error = "Passwords do not match") }
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
                    onSuccess = { authResult ->
                        updateState { copy(isLoading = false) }
                        sendEffect(SignUpContract.Effect.ShowSnackbar("Account created successfully!"))
                        sendEffect(SignUpContract.Effect.NavigateToDashboard)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Sign up failed"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Sign up failed"
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

