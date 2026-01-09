package com.indusjs.fleet.presentation.auth

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.presentation.auth.LoginContract.Effect
import com.indusjs.fleet.presentation.auth.LoginContract.Intent
import com.indusjs.fleet.presentation.auth.LoginContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Login screen implementing MVI pattern.
 *
 * Dependencies are injected via Metro DI through the AuthFeatureGraph.
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
            } catch (e: Exception) {
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
            updateState { copy(error = "Please enter your email or mobile") }
            return
        }
        if (password.isEmpty()) {
            updateState { copy(error = "Please enter your password") }
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
                    onSuccess = { authResult ->
                        updateState { copy(isLoading = false) }
                        sendEffect(Effect.NavigateToDashboard)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Login failed"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Login failed"
                    )
                }
            }
        }
    }
}

