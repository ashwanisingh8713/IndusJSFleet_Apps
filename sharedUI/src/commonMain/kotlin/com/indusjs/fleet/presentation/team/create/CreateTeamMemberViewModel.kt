package com.indusjs.fleet.presentation.team.create

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import com.indusjs.fleet.domain.repository.team.TeamRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Create Team Member screen.
 *
 * Dependencies are injected via Metro DI through the TeamFeatureGraph.
 */
@Inject
class CreateTeamMemberViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository
) : MviViewModel<CreateTeamMemberContract.State, CreateTeamMemberContract.Intent, CreateTeamMemberContract.Effect>(
    CreateTeamMemberContract.State()
) {

    override suspend fun handleIntent(intent: CreateTeamMemberContract.Intent) {
        when (intent) {
            is CreateTeamMemberContract.Intent.UpdateFirstName -> updateState { copy(firstName = intent.firstName) }
            is CreateTeamMemberContract.Intent.UpdateLastName -> updateState { copy(lastName = intent.lastName) }
            is CreateTeamMemberContract.Intent.UpdateEmail -> updateState { copy(email = intent.email) }
            is CreateTeamMemberContract.Intent.UpdateMobile -> updateState { copy(mobile = intent.mobile) }
            is CreateTeamMemberContract.Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is CreateTeamMemberContract.Intent.UpdateConfirmPassword -> updateState { copy(confirmPassword = intent.confirmPassword) }
            is CreateTeamMemberContract.Intent.SelectRole -> updateState { copy(selectedRole = intent.role) }
            is CreateTeamMemberContract.Intent.TogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is CreateTeamMemberContract.Intent.ToggleConfirmPasswordVisibility -> updateState { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
            is CreateTeamMemberContract.Intent.CreateTeamMember -> createTeamMember()
            is CreateTeamMemberContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun createTeamMember() {
        val firstName = currentState.firstName.trim()
        val lastName = currentState.lastName.trim()
        val email = currentState.email.trim()
        val mobile = currentState.mobile.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword
        val role = currentState.selectedRole

        // Validation
        if (firstName.isEmpty()) {
            updateState { copy(error = "Please enter first name") }
            return
        }

        if (lastName.isEmpty()) {
            updateState { copy(error = "Please enter last name") }
            return
        }

        if (email.isEmpty()) {
            updateState { copy(error = "Please enter email") }
            return
        }

        if (!isValidEmail(email)) {
            updateState { copy(error = "Please enter a valid email address") }
            return
        }

        if (mobile.isEmpty()) {
            updateState { copy(error = "Please enter mobile number") }
            return
        }

        if (mobile.length < 10) {
            updateState { copy(error = "Please enter a valid mobile number") }
            return
        }

        if (password.isEmpty()) {
            updateState { copy(error = "Please enter password") }
            return
        }

        if (password.length < 6) {
            updateState { copy(error = "Password must be at least 6 characters") }
            return
        }

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = "Please confirm password") }
            return
        }

        if (password != confirmPassword) {
            updateState { copy(error = "Passwords do not match") }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.createTeamMember(
                    email = email,
                    mobile = mobile,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    role = role
                )

                result.fold(
                    onSuccess = { teamMember ->
                        updateState { copy(isLoading = false) }
                        val roleText = if (role == TeamMemberRole.MANAGER) "Manager" else "Supervisor"
                        sendEffect(CreateTeamMemberContract.Effect.ShowSnackbar("$roleText ${teamMember.fullName} created successfully!"))
                        sendEffect(CreateTeamMemberContract.Effect.TeamMemberCreated)
                        sendEffect(CreateTeamMemberContract.Effect.NavigateBack)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to create team member"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create team member"
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }
}

