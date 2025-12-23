package com.indusjs.fleet.feature.auth.presentation

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.feature.auth.presentation.LoginContract.Effect
import com.indusjs.fleet.feature.auth.presentation.LoginContract.Intent
import com.indusjs.fleet.feature.auth.presentation.LoginContract.State
import com.indusjs.fleet.feature.user.di.UserModule
import com.indusjs.fleet.feature.user.domain.repository.UserRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Login screen implementing MVI pattern.
 */
@Inject
class LoginViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository = UserModule.provideUserRepository()
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

