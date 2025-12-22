package com.indusjs.fleet.feature.auth.presentation

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.feature.auth.presentation.LoginContract.Effect
import com.indusjs.fleet.feature.auth.presentation.LoginContract.Intent
import com.indusjs.fleet.feature.auth.presentation.LoginContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Login screen implementing MVI pattern.
 */
@Inject
class LoginViewModel(
    private val dispatcherProvider: DispatcherProvider
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateEmail -> updateState { copy(email = intent.email) }
            is Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is Intent.Login -> login()
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun login() {
        val email = currentState.email.trim()
        val password = currentState.password

        // Validate inputs
        if (email.isEmpty()) {
            updateState { copy(error = "Please enter your email") }
            return
        }
        if (password.isEmpty()) {
            updateState { copy(error = "Please enter your password") }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // TODO: Replace with actual authentication
                delay(1000) // Simulate network call

                // Mock authentication - accept any non-empty credentials
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    sendEffect(Effect.NavigateToDashboard)
                } else {
                    updateState { copy(isLoading = false, error = "Invalid credentials") }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Login failed"
                    )
                }
            }
        }

        updateState { copy(isLoading = false) }
    }
}

