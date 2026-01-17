package com.indusjs.fleet.presentation.team.create

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.team.TeamMemberRole

/**
 * MVI Contract for Create Team Member screen.
 */
object CreateTeamMemberContract {

    /**
     * UI State for Create Team Member screen.
     */
    data class State(
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val mobile: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val selectedRole: TeamMemberRole = TeamMemberRole.MANAGER,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        // Current user's role for permission filtering
        val currentUserRole: String = "",
        // Roles that the current user can create
        val availableRoles: List<TeamMemberRole> = listOf(TeamMemberRole.MANAGER, TeamMemberRole.SUPERVISOR)
    ) : UiState

    /**
     * User intents for Create Team Member screen.
     */
    sealed interface Intent : UiIntent {
        data class UpdateFirstName(val firstName: String) : Intent
        data class UpdateLastName(val lastName: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdateMobile(val mobile: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent
        data class SelectRole(val role: TeamMemberRole) : Intent
        data object TogglePasswordVisibility : Intent
        data object ToggleConfirmPasswordVisibility : Intent
        data object CreateTeamMember : Intent
        data object ClearError : Intent
        // When coming from Caretaker assignment, exclude General Manager
        data class SetExcludeGeneralManager(val exclude: Boolean) : Intent
    }

    /**
     * Side effects for Create Team Member screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object TeamMemberCreated : Effect
        data object NavigateBack : Effect
    }
}

