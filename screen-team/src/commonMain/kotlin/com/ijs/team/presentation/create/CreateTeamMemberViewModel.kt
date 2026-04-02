package com.ijs.team.presentation.create

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.indusjs.fleet.core.util.PermissionUtils
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.team.domain.entity.TeamMemberRole
import com.indusjs.fleet.domain.entity.user.UserRole
import com.ijs.team.domain.repository.TeamRepository
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Create Team Member screen.
 *
 * Dependencies are provided via DefaultViewModelProvider.
 */
@Inject
class CreateTeamMemberViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository,
    private val userLocalDataSource: UserLocalDataSource
) : MviViewModel<CreateTeamMemberContract.State, CreateTeamMemberContract.Intent, CreateTeamMemberContract.Effect>(
    CreateTeamMemberContract.State()
) {

    init {
        // Load user role and determine available roles for creation
        viewModelScope.launch(dispatcherProvider.io) {
            val userRole = try {
                userLocalDataSource.getUserRole() ?: ""
            } catch (e: Exception) {
                ""
            }

            // Determine which roles the current user can create
            val creatableRoles = PermissionUtils.getCreatableRoles(userRole)
            val availableTeamRoles = creatableRoles.mapNotNull {
                when (it) {
                    UserRole.GENERAL_MANAGER -> TeamMemberRole.GENERAL_MANAGER
                    UserRole.MANAGER -> TeamMemberRole.MANAGER
                    UserRole.SUPERVISOR -> TeamMemberRole.SUPERVISOR
                    else -> null
                }
            }

            // Default to first available role or Manager
            val defaultRole = availableTeamRoles.firstOrNull() ?: TeamMemberRole.MANAGER

            updateState {
                copy(
                    currentUserRole = userRole,
                    availableRoles = availableTeamRoles.ifEmpty { listOf(TeamMemberRole.MANAGER, TeamMemberRole.SUPERVISOR) },
                    selectedRole = defaultRole
                )
            }
        }
    }

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
            is CreateTeamMemberContract.Intent.SetExcludeGeneralManager -> {
                if (intent.exclude) {
                    // Filter out General Manager from available roles
                    val filteredRoles = currentState.availableRoles.filter { it != TeamMemberRole.GENERAL_MANAGER }
                    val newSelectedRole = if (currentState.selectedRole == TeamMemberRole.GENERAL_MANAGER) {
                        filteredRoles.firstOrNull() ?: TeamMemberRole.MANAGER
                    } else {
                        currentState.selectedRole
                    }
                    updateState {
                        copy(
                            availableRoles = filteredRoles.ifEmpty { listOf(TeamMemberRole.MANAGER, TeamMemberRole.SUPERVISOR) },
                            selectedRole = newSelectedRole
                        )
                    }
                }
            }
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

        if (mobile.length < 10) {
            updateState { copy(error = UiText.StringRes(Res.string.error_mobile_invalid)) }
            return
        }

        if (password.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_required)) }
            return
        }

        if (password.length < 6) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_min_chars)) }
            return
        }

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
                        sendEffect(CreateTeamMemberContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_team_member_created)))
                        sendEffect(CreateTeamMemberContract.Effect.TeamMemberCreated)
                        sendEffect(CreateTeamMemberContract.Effect.NavigateBack)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message?.let { UiText.Raw(it) }
                                    ?: UiText.StringRes(Res.string.error_create_team_member)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_create_team_member)
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }
}

