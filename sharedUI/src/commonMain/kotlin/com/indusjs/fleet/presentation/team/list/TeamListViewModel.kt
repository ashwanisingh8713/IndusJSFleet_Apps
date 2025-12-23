package com.indusjs.fleet.presentation.team.list

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import com.indusjs.fleet.domain.repository.team.TeamRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Team Members List screen.
 *
 * Dependencies are injected via Metro DI through the TeamFeatureGraph.
 */
@Inject
class TeamListViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository
) : MviViewModel<TeamListContract.State, TeamListContract.Intent, TeamListContract.Effect>(
    TeamListContract.State()
) {

    init {
        sendIntent(TeamListContract.Intent.LoadTeamMembers)
    }

    override suspend fun handleIntent(intent: TeamListContract.Intent) {
        when (intent) {
            is TeamListContract.Intent.LoadTeamMembers -> loadTeamMembers()
            is TeamListContract.Intent.RefreshTeamMembers -> refreshTeamMembers()
            is TeamListContract.Intent.SelectFilter -> selectFilter(intent.filter)
            is TeamListContract.Intent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is TeamListContract.Intent.DeleteTeamMember -> deleteTeamMember(intent.id)
            is TeamListContract.Intent.NavigateToCreateMember -> sendEffect(TeamListContract.Effect.NavigateToCreateMember)
            is TeamListContract.Intent.NavigateToMemberDetail -> sendEffect(TeamListContract.Effect.NavigateToMemberDetail(intent.id))
            is TeamListContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadTeamMembers() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.getTeamMembers()

                result.fold(
                    onSuccess = { members ->
                        updateState {
                            copy(
                                isLoading = false,
                                teamMembers = members,
                                filteredMembers = applyFilters(members, selectedFilter, searchQuery)
                            )
                        }
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load team members"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load team members"
                    )
                }
            }
        }
    }

    private suspend fun refreshTeamMembers() {
        updateState { copy(isRefreshing = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.getTeamMembers()

                result.fold(
                    onSuccess = { members ->
                        updateState {
                            copy(
                                isRefreshing = false,
                                teamMembers = members,
                                filteredMembers = applyFilters(members, selectedFilter, searchQuery)
                            )
                        }
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isRefreshing = false,
                                error = error.message ?: "Failed to refresh team members"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isRefreshing = false,
                        error = e.message ?: "Failed to refresh team members"
                    )
                }
            }
        }
    }

    private fun selectFilter(filter: TeamListContract.FilterType) {
        updateState {
            copy(
                selectedFilter = filter,
                filteredMembers = applyFilters(teamMembers, filter, searchQuery)
            )
        }
    }

    private fun updateSearchQuery(query: String) {
        updateState {
            copy(
                searchQuery = query,
                filteredMembers = applyFilters(teamMembers, selectedFilter, query)
            )
        }
    }

    private suspend fun deleteTeamMember(id: String) {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.deleteTeamMember(id)

                result.fold(
                    onSuccess = {
                        val updatedMembers = currentState.teamMembers.filter { it.id != id }
                        updateState {
                            copy(
                                isLoading = false,
                                teamMembers = updatedMembers,
                                filteredMembers = applyFilters(updatedMembers, selectedFilter, searchQuery)
                            )
                        }
                        sendEffect(TeamListContract.Effect.ShowSnackbar("Team member deleted successfully"))
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to delete team member"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to delete team member"
                    )
                }
            }
        }
    }

    private fun applyFilters(
        members: List<TeamMember>,
        filter: TeamListContract.FilterType,
        searchQuery: String
    ): List<TeamMember> {
        return members
            .filter { member ->
                when (filter) {
                    TeamListContract.FilterType.ALL -> true
                    TeamListContract.FilterType.MANAGERS -> member.role == TeamMemberRole.MANAGER
                    TeamListContract.FilterType.SUPERVISORS -> member.role == TeamMemberRole.SUPERVISOR
                }
            }
            .filter { member ->
                if (searchQuery.isBlank()) true
                else {
                    member.fullName.contains(searchQuery, ignoreCase = true) ||
                    member.email.contains(searchQuery, ignoreCase = true) ||
                    member.mobile.contains(searchQuery, ignoreCase = true)
                }
            }
    }
}

