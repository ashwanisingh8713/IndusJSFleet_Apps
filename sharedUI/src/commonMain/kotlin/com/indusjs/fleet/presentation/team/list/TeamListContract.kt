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
        val searchQuery: String = ""
    ) : UiState {
        val managersCount: Int get() = teamMembers.count { it.role == TeamMemberRole.MANAGER }
        val supervisorsCount: Int get() = teamMembers.count { it.role == TeamMemberRole.SUPERVISOR }
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

