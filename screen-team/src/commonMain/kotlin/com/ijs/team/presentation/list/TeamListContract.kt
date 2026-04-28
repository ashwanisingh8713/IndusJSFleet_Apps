package com.ijs.team.presentation.list

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole

/**
 * MVI Contract for Team Members List screen.
 */
object TeamListContract {

    /**
     * Filter type for team members.
     */
    enum class FilterType {
        ALL,
        ADMINS,
        USERS
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
        val adminsCount: Int get() = teamMembers.count { it.role == TeamMemberRole.MANAGER }
        val usersCount: Int get() = teamMembers.count { it.role == TeamMemberRole.SUPERVISOR }
        val activeCount: Int get() = teamMembers.count { it.isActive }

        // Role-based access helpers
        val isOwner: Boolean get() = currentUserRole.lowercase() == "owner"
        val isAdmin: Boolean get() = currentUserRole.lowercase() in setOf("admin", "manager", "general_manager")
        val isUser: Boolean get() = currentUserRole.lowercase() in setOf("user", "supervisor")

        // Check if current user can create team members (owner and admins)
        val canCreateTeamMember: Boolean get() = isOwner || isAdmin

        // Check if current user can edit a member
        fun canEdit(member: TeamMember): Boolean {
            if (member.id == currentUserId) return false  // Cannot edit self
            return when {
                isOwner -> true  // Owner can edit anyone
                isAdmin -> member.role == TeamMemberRole.SUPERVISOR
                else -> false
            }
        }

        // Check if current user can toggle active status
        fun canToggleActive(member: TeamMember): Boolean {
            if (member.id == currentUserId) return false  // Cannot toggle self
            return when {
                isOwner -> true  // Owner can toggle anyone
                isAdmin -> member.role == TeamMemberRole.SUPERVISOR
                else -> false
            }
        }

        // Check if current user can reset password
        fun canResetPassword(member: TeamMember): Boolean {
            if (member.id == currentUserId) return false  // Cannot reset own password here
            return when {
                isOwner -> true  // Owner can reset anyone's password
                isAdmin -> member.role == TeamMemberRole.SUPERVISOR
                else -> false
            }
        }

        // Check if current user can delete a member
        fun canDelete(member: TeamMember): Boolean {
            if (member.id == currentUserId) return false  // Cannot delete self
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
        data class ShowSnackbar(val message: UiText) : Effect
        data object NavigateToCreateMember : Effect
        data class NavigateToMemberDetail(val id: String) : Effect
    }
}
