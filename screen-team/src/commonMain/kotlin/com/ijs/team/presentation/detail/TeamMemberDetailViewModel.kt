package com.ijs.team.presentation.detail

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.PermissionUtils
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.team.domain.entity.TeamMemberRole
import com.indusjs.fleet.domain.entity.user.UserRole
import com.ijs.team.domain.repository.TeamRepository
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

/**
 * ViewModel for Team Member Detail screen.
 */
@Inject
class TeamMemberDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository,
    private val userLocalDataSource: UserLocalDataSource
) : MviViewModel<TeamMemberDetailContract.State, TeamMemberDetailContract.Intent, TeamMemberDetailContract.Effect>(
    TeamMemberDetailContract.State()
) {

    override suspend fun handleIntent(intent: TeamMemberDetailContract.Intent) {
        when (intent) {
            is TeamMemberDetailContract.Intent.LoadMember -> loadMember(intent.memberId)
            is TeamMemberDetailContract.Intent.RefreshMember -> refreshMember()
            is TeamMemberDetailContract.Intent.EnterEditMode -> enterEditMode()
            is TeamMemberDetailContract.Intent.ExitEditMode -> exitEditMode()
            is TeamMemberDetailContract.Intent.SaveChanges -> saveChanges()
            is TeamMemberDetailContract.Intent.UpdateFirstName -> updateState {
                copy(editFirstName = intent.value, firstNameError = null)
            }
            is TeamMemberDetailContract.Intent.UpdateLastName -> updateState {
                copy(editLastName = intent.value, lastNameError = null)
            }
            is TeamMemberDetailContract.Intent.UpdateEmail -> updateState {
                copy(editEmail = intent.value, emailError = null)
            }
            is TeamMemberDetailContract.Intent.UpdateMobile -> updateState {
                copy(editMobile = intent.value, mobileError = null)
            }
            is TeamMemberDetailContract.Intent.UpdateRole -> updateState {
                copy(editRole = intent.role)
            }
            is TeamMemberDetailContract.Intent.UpdateIsActive -> updateState {
                copy(editIsActive = intent.isActive)
            }
            is TeamMemberDetailContract.Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    private suspend fun loadMember(memberId: String) {
        updateState { copy(memberId = memberId, isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // Load current user info
                val userRole = try {
                    userLocalDataSource.getUserRole() ?: "owner"
                } catch (e: Exception) { "owner" }

                val userId = try {
                    userLocalDataSource.getUserId() ?: ""
                } catch (e: Exception) { "" }

                // Get available roles based on current user role
                val creatableRoles = PermissionUtils.getCreatableRoles(userRole)
                val availableTeamRoles = creatableRoles.mapNotNull {
                    when (it) {
                        UserRole.GENERAL_MANAGER -> TeamMemberRole.GENERAL_MANAGER
                        UserRole.MANAGER -> TeamMemberRole.MANAGER
                        UserRole.SUPERVISOR -> TeamMemberRole.SUPERVISOR
                        else -> null
                    }
                }

                val result = teamRepository.getTeamMember(memberId)

                result.fold(
                    onSuccess = { member ->
                        // Determine if user can edit this member
                        val canEditMember = when {
                            member.id == userId -> false  // Cannot edit self
                            userRole.lowercase() == "owner" -> true
                            userRole.lowercase() == "general_manager" ->
                                member.role == TeamMemberRole.MANAGER || member.role == TeamMemberRole.SUPERVISOR
                            else -> false
                        }

                        // Determine if user can change this member's role
                        val canChangeRoleForMember = when {
                            member.id == userId -> false  // Cannot change own role
                            userRole.lowercase() == "owner" -> true
                            userRole.lowercase() == "general_manager" ->
                                member.role == TeamMemberRole.MANAGER || member.role == TeamMemberRole.SUPERVISOR
                            else -> false
                        }

                        // Determine if user can toggle this member's active status
                        val canToggleActiveMember = when {
                            member.id == userId -> false  // Cannot toggle own status
                            userRole.lowercase() == "owner" -> true
                            userRole.lowercase() == "general_manager" ->
                                member.role == TeamMemberRole.MANAGER || member.role == TeamMemberRole.SUPERVISOR
                            else -> false
                        }

                        updateState {
                            copy(
                                isLoading = false,
                                member = member,
                                currentUserRole = userRole,
                                currentUserId = userId,
                                availableRoles = availableTeamRoles,
                                canEdit = canEditMember,
                                canChangeRole = canChangeRoleForMember,
                                canToggleActive = canToggleActiveMember,
                                // Initialize edit fields with current values
                                editFirstName = member.firstName,
                                editLastName = member.lastName,
                                editEmail = member.email,
                                editMobile = member.mobile,
                                editRole = member.role,
                                editIsActive = member.isActive
                            )
                        }
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message ?: "Failed to load team member"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load team member"
                    )
                }
            }
        }
    }

    private suspend fun refreshMember() {
        val memberId = currentState.memberId
        if (memberId.isNotEmpty()) {
            loadMember(memberId)
        }
    }

    private fun enterEditMode() {
        val member = currentState.member ?: return
        updateState {
            copy(
                isEditMode = true,
                editFirstName = member.firstName,
                editLastName = member.lastName,
                editEmail = member.email,
                editMobile = member.mobile,
                editRole = member.role,
                editIsActive = member.isActive,
                // Clear any previous errors
                firstNameError = null,
                lastNameError = null,
                emailError = null,
                mobileError = null
            )
        }
    }

    private fun exitEditMode() {
        updateState { copy(isEditMode = false) }
    }

    private suspend fun saveChanges() {
        // Validate fields
        if (!validateFields()) return

        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            try {
                val state = currentState
                val roleToUpdate = if (state.canChangeRole && !state.isSelf) state.editRole else null

                // Debug logging
                println("TeamMemberDetailVM: Saving changes for member ${state.memberId}")
                println("TeamMemberDetailVM: canChangeRole=${state.canChangeRole}, isSelf=${state.isSelf}")
                println("TeamMemberDetailVM: editRole=${state.editRole}, roleToUpdate=$roleToUpdate")
                println("TeamMemberDetailVM: currentUserRole=${state.currentUserRole}")

                val result = teamRepository.updateTeamMember(
                    id = state.memberId,
                    firstName = state.editFirstName.trim(),
                    lastName = state.editLastName.trim(),
                    email = state.editEmail.trim(),
                    mobile = state.editMobile.trim(),
                    role = roleToUpdate,
                    isActive = if (state.canToggleActive) state.editIsActive else null
                )

                result.fold(
                    onSuccess = { updatedMember ->
                        updateState {
                            copy(
                                isSaving = false,
                                isEditMode = false,
                                member = updatedMember,
                                editFirstName = updatedMember.firstName,
                                editLastName = updatedMember.lastName,
                                editEmail = updatedMember.email,
                                editMobile = updatedMember.mobile,
                                editRole = updatedMember.role,
                                editIsActive = updatedMember.isActive
                            )
                        }
                        sendEffect(TeamMemberDetailContract.Effect.ShowSnackbar("Team member updated successfully"))
                        sendEffect(TeamMemberDetailContract.Effect.MemberUpdated)
                    },
                    onFailure = { error ->
                        updateState { copy(isSaving = false) }
                        sendEffect(TeamMemberDetailContract.Effect.ShowSnackbar(error.message ?: "Failed to update team member"))
                    }
                )
            } catch (e: Exception) {
                updateState { copy(isSaving = false) }
                sendEffect(TeamMemberDetailContract.Effect.ShowSnackbar(e.message ?: "Failed to update team member"))
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val state = currentState

        if (state.editFirstName.isBlank()) {
            updateState { copy(firstNameError = "First name is required") }
            isValid = false
        }

        if (state.editLastName.isBlank()) {
            updateState { copy(lastNameError = "Last name is required") }
            isValid = false
        }

        if (state.editEmail.isBlank()) {
            updateState { copy(emailError = "Email is required") }
            isValid = false
        } else if (!isValidEmail(state.editEmail)) {
            updateState { copy(emailError = "Invalid email format") }
            isValid = false
        }

        if (state.editMobile.isBlank()) {
            updateState { copy(mobileError = "Mobile is required") }
            isValid = false
        } else if (!isValidMobile(state.editMobile)) {
            updateState { copy(mobileError = "Invalid mobile number") }
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun isValidMobile(mobile: String): Boolean {
        val digitsOnly = mobile.filter { it.isDigit() }
        return digitsOnly.length >= 10
    }
}

