package com.ijs.team.presentation.create

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.team.domain.entity.AssignableTeamRole

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
        /** IAM role name from API (e.g. admin, user). */
        val selectedIamRoleName: String = "",
        val availableIamRoles: List<AssignableTeamRole> = emptyList(),
        val rolesLoading: Boolean = true,
        val isLoading: Boolean = false,
        val error: UiText? = null
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
        data class SelectIamRole(val roleName: String) : Intent
        data object CreateTeamMember : Intent
        data object ClearError : Intent
        /** When true, removes elevated IAM roles such as owner from the selectable list. */
        data class SetExcludeGeneralManager(val exclude: Boolean) : Intent
    }

    /**
     * Side effects for Create Team Member screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: UiText) : Effect
        data object TeamMemberCreated : Effect
        data object NavigateBack : Effect
    }
}
