package com.indusjs.fleet.presentation.team.list

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole

/**
 * MVI Contract for Team Members List screen.
 */
object TeamListContract {

    /**
     * Filter type for team members.
     */
    enum class FilterType {
        ALL,
        GENERAL_MANAGERS,
        MANAGERS,
        SUPERVISORS
    }

    /**
     * UI State for Team Members List screen.
     */
    data class State(
        val teamMembers: List<TeamMember> = emptyList(),
        val filteredMembers: List<TeamMember> = emptyList(),
        val selectedFilter: FilterType = FilterType.ALL,
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        // Current user info for role-based access control
        val currentUserRole: String = "owner",
        val currentUserId: String = "",
        // Action states
        val isTogglingActive: String? = null,  // Member ID being toggled
        val isResettingPassword: String? = null,  // Member ID being reset
        val showResetPasswordDialogForMemberId: String? = null
    ) : UiState {
        val generalManagersCount: Int get() = teamMembers.count { it.role == TeamMemberRole.GENERAL_MANAGER }
        val managersCount: Int get() = teamMembers.count { it.role == TeamMemberRole.MANAGER }
        val supervisorsCount: Int get() = teamMembers.count { it.role == TeamMemberRole.SUPERVISOR }

        // Role-based access helpers
        val isOwner: Boolean get() = currentUserRole.lowercase() == "owner"
        val isManager: Boolean get() = currentUserRole.lowercase() == "manager"
        val isSupervisor: Boolean get() = currentUserRole.lowercase() == "supervisor"

        // Check if current user can edit a member
        fun canEdit(member: TeamMember): Boolean {
            return isOwner || (isManager && member.role == TeamMemberRole.SUPERVISOR)
        }

        // Check if current user can toggle active status
        fun canToggleActive(member: TeamMember): Boolean {
            if (member.id == currentUserId) return false  // Cannot toggle self
            return isOwner || (isManager && member.role == TeamMemberRole.SUPERVISOR)
        }

        // Check if current user can reset password
        fun canResetPassword(member: TeamMember): Boolean {
            return isOwner || (isManager && member.role == TeamMemberRole.SUPERVISOR)
        }

        // Check if current user can delete a member
        fun canDelete(member: TeamMember): Boolean {
            return isOwner  // Only owner can delete
        }
    }

    /**
     * User intents for Team Members List screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadTeamMembers : Intent
        data object RefreshTeamMembers : Intent
        data class SelectFilter(val filter: FilterType) : Intent
        data class UpdateSearchQuery(val query: String) : Intent
        data class DeleteTeamMember(val id: String) : Intent
        data class ToggleTeamMemberActive(val id: String) : Intent
        data class ShowResetPasswordDialog(val memberId: String) : Intent
        data object DismissResetPasswordDialog : Intent
        data class ResetTeamMemberPassword(val id: String, val newPassword: String) : Intent
        data object NavigateToCreateMember : Intent
        data class NavigateToMemberDetail(val id: String) : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for Team Members List screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateToCreateMember : Effect
        data class NavigateToMemberDetail(val id: String) : Effect
    }
}
