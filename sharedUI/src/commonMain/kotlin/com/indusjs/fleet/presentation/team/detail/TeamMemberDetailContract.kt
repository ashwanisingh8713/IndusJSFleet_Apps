package com.indusjs.fleet.presentation.team.detail

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole

/**
 * MVI Contract for Team Member Detail screen.
 */
object TeamMemberDetailContract {

    /**
     * UI State for Team Member Detail screen.
     */
    data class State(
        val memberId: String = "",
        val member: TeamMember? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        // Edit mode
        val isEditMode: Boolean = false,
        val isSaving: Boolean = false,
        // Editable fields
        val editFirstName: String = "",
        val editLastName: String = "",
        val editEmail: String = "",
        val editMobile: String = "",
        val editRole: TeamMemberRole = TeamMemberRole.SUPERVISOR,
        val editIsActive: Boolean = true,
        // Validation errors
        val firstNameError: String? = null,
        val lastNameError: String? = null,
        val emailError: String? = null,
        val mobileError: String? = null,
        // Current user permissions
        val currentUserRole: String = "owner",
        val currentUserId: String = "",
        val availableRoles: List<TeamMemberRole> = emptyList(),
        val canEdit: Boolean = true,
        val canChangeRole: Boolean = true,
        val canToggleActive: Boolean = true
    ) : UiState {
        val isOwner: Boolean get() = currentUserRole.lowercase() == "owner"
        val isGeneralManager: Boolean get() = currentUserRole.lowercase() == "general_manager"
        val isSelf: Boolean get() = member?.id == currentUserId
        val displayName: String get() = member?.fullName ?: "Team Member"
        val initials: String get() = member?.initials ?: "TM"

        // Computed: Can change role based on permissions and not editing self
        val canChangeRoleComputed: Boolean get() {
            if (isSelf) return false
            val memberRole = member?.role ?: return false
            return when {
                isOwner -> true  // Owner can change any role
                isGeneralManager -> memberRole == TeamMemberRole.MANAGER || memberRole == TeamMemberRole.SUPERVISOR
                else -> false
            }
        }
    }

    /**
     * User intents for Team Member Detail screen.
     */
    sealed interface Intent : UiIntent {
        data class LoadMember(val memberId: String) : Intent
        data object RefreshMember : Intent
        data object EnterEditMode : Intent
        data object ExitEditMode : Intent
        data object SaveChanges : Intent
        // Edit field updates
        data class UpdateFirstName(val value: String) : Intent
        data class UpdateLastName(val value: String) : Intent
        data class UpdateEmail(val value: String) : Intent
        data class UpdateMobile(val value: String) : Intent
        data class UpdateRole(val role: TeamMemberRole) : Intent
        data class UpdateIsActive(val isActive: Boolean) : Intent
        data object ClearError : Intent
    }

    /**
     * Side effects for Team Member Detail screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data object NavigateBack : Effect
        data object MemberUpdated : Effect
    }
}

