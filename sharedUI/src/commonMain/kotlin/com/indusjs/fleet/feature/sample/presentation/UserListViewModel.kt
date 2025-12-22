package com.indusjs.fleet.feature.sample.presentation

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.feature.sample.domain.usecase.GetUserByIdUseCase
import com.indusjs.fleet.feature.sample.domain.usecase.GetUsersUseCase
import com.indusjs.fleet.feature.sample.presentation.UserListContract.Effect
import com.indusjs.fleet.feature.sample.presentation.UserListContract.Intent
import com.indusjs.fleet.feature.sample.presentation.UserListContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/**
 * ViewModel for the User List screen implementing MVI pattern.
 * Handles user intents and updates state accordingly.
 */
@Inject
class UserListViewModel(
    private val getUsersUseCase: GetUsersUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val dispatcherProvider: DispatcherProvider
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        // Load users on initialization
        sendIntent(Intent.LoadUsers)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadUsers -> loadUsers()
            is Intent.RefreshUsers -> loadUsers()
            is Intent.SelectUser -> selectUser(intent.userId)
            is Intent.DeleteUser -> deleteUser(intent.userId)
            is Intent.ClearError -> clearError()
        }
    }

    private suspend fun loadUsers() {
        withContext(dispatcherProvider.io) {
            getUsersUseCase().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        updateState { copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        updateState {
                            copy(
                                isLoading = false,
                                users = result.data,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isLoading = false,
                                error = result.message ?: "An error occurred"
                            )
                        }
                        sendEffect(Effect.ShowSnackbar(result.message ?: "An error occurred"))
                    }
                }
            }
        }
    }

    private suspend fun selectUser(userId: String) {
        withContext(dispatcherProvider.io) {
            val result = getUserByIdUseCase(userId)
            when (result) {
                is Result.Success -> {
                    updateState { copy(selectedUser = result.data) }
                    sendEffect(Effect.NavigateToUserDetail(userId))
                }
                is Result.Error -> {
                    sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to load user"))
                }
                is Result.Loading -> { /* Not applicable for suspend function */ }
            }
        }
    }

    private suspend fun deleteUser(userId: String) {
        // TODO: Implement delete user use case
        // For now, just remove from local list
        updateState {
            copy(users = users.filter { it.id != userId })
        }
        sendEffect(Effect.UserDeleted)
        sendEffect(Effect.ShowSnackbar("User deleted successfully"))
    }

    private fun clearError() {
        updateState { copy(error = null) }
    }
}

