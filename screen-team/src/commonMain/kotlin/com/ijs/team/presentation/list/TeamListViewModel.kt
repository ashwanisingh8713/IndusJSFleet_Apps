package com.ijs.team.presentation.list

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import com.ijs.team.domain.repository.TeamRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Team Members List screen.
 *
 * Dependencies are provided via DefaultViewModelProvider.
 */
@Inject
class TeamListViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository,
    private val userLocalDataSource: UserLocalDataSource
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
            is TeamListContract.Intent.ToggleTeamMemberActive -> toggleTeamMemberActive(intent.id)
            is TeamListContract.Intent.ShowResetPasswordDialog -> showResetPasswordDialog(intent.memberId)
            is TeamListContract.Intent.DismissResetPasswordDialog -> dismissResetPasswordDialog()
            is TeamListContract.Intent.ResetTeamMemberPassword -> resetTeamMemberPassword(intent.id, intent.newPassword)
            is TeamListContract.Intent.NavigateToCreateMember -> sendEffect(TeamListContract.Effect.NavigateToCreateMember)
            is TeamListContract.Intent.NavigateToMemberDetail -> sendEffect(TeamListContract.Effect.NavigateToMemberDetail(intent.id))
            is TeamListContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadTeamMembers() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // Load current user info
                val userRole = try {
                    userLocalDataSource.getUserRole() ?: "supervisor"
                } catch (e: Exception) { "supervisor" }

                val userId = try {
                    userLocalDataSource.getUserId() ?: ""
                } catch (e: Exception) { "" }

                // First try to load from cache
                val cacheResult = teamRepository.getTeamMembersFromCache()

                cacheResult.fold(
                    onSuccess = { members ->
                        if (members.isNotEmpty()) {
                            updateState {
                                copy(
                                    isLoading = false,
                                    teamMembers = members,
                                    filteredMembers = applyFilters(members, selectedFilter, searchQuery),
                                    currentUserRole = userRole,
                                    currentUserId = userId
                                )
                            }
                        } else {
                            // Cache is empty, fetch from API
                            fetchFromApi(userRole, userId)
                        }
                    },
                    onFailure = {
                        // Cache failed, fetch from API
                        fetchFromApi(userRole, userId)
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

    private suspend fun fetchFromApi(userRole: String, userId: String) {
        val result = teamRepository.refreshTeamMembers()

        result.fold(
            onSuccess = { members ->
                updateState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        teamMembers = members,
                        filteredMembers = applyFilters(members, selectedFilter, searchQuery),
                        currentUserRole = userRole,
                        currentUserId = userId
                    )
                }
            },
            onFailure = { error ->
                updateState {
                    copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "Failed to load team members",
                        currentUserRole = userRole,
                        currentUserId = userId
                    )
                }
            }
        )
    }

    private suspend fun refreshTeamMembers() {
        updateState { copy(isRefreshing = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // Load current user info
                val userRole = try {
                    userLocalDataSource.getUserRole() ?: "supervisor"
                } catch (e: Exception) { "supervisor" }

                val userId = try {
                    userLocalDataSource.getUserId() ?: ""
                } catch (e: Exception) { "" }

                // Refresh from API and update local cache
                fetchFromApi(userRole, userId)
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

    private suspend fun toggleTeamMemberActive(id: String) {
        updateState { copy(isTogglingActive = id) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.toggleTeamMemberActive(id)

                result.fold(
                    onSuccess = { updatedMember ->
                        val updatedMembers = currentState.teamMembers.map {
                            if (it.id == id) updatedMember else it
                        }
                        updateState {
                            copy(
                                isTogglingActive = null,
                                teamMembers = updatedMembers,
                                filteredMembers = applyFilters(updatedMembers, selectedFilter, searchQuery)
                            )
                        }
                        val statusText = if (updatedMember.isActive) "enabled" else "disabled"
                        sendEffect(TeamListContract.Effect.ShowSnackbar("${updatedMember.fullName} has been $statusText"))
                    },
                    onFailure = { error ->
                        updateState { copy(isTogglingActive = null) }
                        sendEffect(TeamListContract.Effect.ShowSnackbar(error.message ?: "Failed to toggle status"))
                    }
                )
            } catch (e: Exception) {
                updateState { copy(isTogglingActive = null) }
                sendEffect(TeamListContract.Effect.ShowSnackbar(e.message ?: "Failed to toggle status"))
            }
        }
    }

    private fun showResetPasswordDialog(memberId: String) {
        updateState { copy(showResetPasswordDialogForMemberId = memberId) }
    }

    private fun dismissResetPasswordDialog() {
        updateState { copy(showResetPasswordDialogForMemberId = null) }
    }

    private suspend fun resetTeamMemberPassword(id: String, newPassword: String) {
        updateState { copy(isResettingPassword = id, showResetPasswordDialogForMemberId = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.resetTeamMemberPassword(id, newPassword)

                result.fold(
                    onSuccess = {
                        updateState { copy(isResettingPassword = null) }
                        sendEffect(TeamListContract.Effect.ShowSnackbar("Password reset successfully"))
                    },
                    onFailure = { error ->
                        updateState { copy(isResettingPassword = null) }
                        sendEffect(TeamListContract.Effect.ShowSnackbar(error.message ?: "Failed to reset password"))
                    }
                )
            } catch (e: Exception) {
                updateState { copy(isResettingPassword = null) }
                sendEffect(TeamListContract.Effect.ShowSnackbar(e.message ?: "Failed to reset password"))
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
                    TeamListContract.FilterType.GENERAL_MANAGERS -> member.role == TeamMemberRole.GENERAL_MANAGER
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

