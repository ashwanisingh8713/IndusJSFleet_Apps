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
        val error: UiText? = null,
        val searchQuery: String = "",
        // Current user identity (for the self-guard; NOT authorization)
        val currentUserId: String = "",
        // Permission flags (computed from PermissionChecker — never role names).
        // The self-guard (cannot act on own record) is applied on top of these.
        val canCreateTeamMember: Boolean = false,
        val canEditPermission: Boolean = false,
        val canTogglePermission: Boolean = false,
        val canResetPasswordPermission: Boolean = false,
        val canDeletePermission: Boolean = false,
        // Action states
        val isTogglingActive: String? = null,  // Member ID being toggled
        val isResettingPassword: String? = null,  // Member ID being reset
        val showResetPasswordDialogForMemberId: String? = null
    ) : UiState {

        // Check if current user can edit a member (permission AND not self)
        fun canEdit(member: TeamMember): Boolean =
            canEditPermission && member.id != currentUserId

        // Check if current user can toggle active status (permission AND not self)
        fun canToggleActive(member: TeamMember): Boolean =
            canTogglePermission && member.id != currentUserId

        // Check if current user can reset password (permission AND not self)
        fun canResetPassword(member: TeamMember): Boolean =
            canResetPasswordPermission && member.id != currentUserId

        // Check if current user can delete a member (permission AND not self)
        fun canDelete(member: TeamMember): Boolean =
            canDeletePermission && member.id != currentUserId
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
