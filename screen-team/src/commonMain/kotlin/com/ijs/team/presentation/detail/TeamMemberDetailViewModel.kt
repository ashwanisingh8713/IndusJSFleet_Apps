package com.ijs.team.presentation.detail

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.permission.PermissionChecker
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.team.TAG_TEAM_DETAIL_VM
import com.ijs.team.domain.entity.TeamMemberRole
import com.ijs.team.domain.repository.TeamRepository
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_invalid_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_required_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_first_name_required_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_last_name_required_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_team_member
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_invalid_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_required_team
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_update_team_member
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_team_member_updated
import kotlinx.coroutines.withContext

/**
 * ViewModel for Team Member Detail screen.
 */
@Inject
class TeamMemberDetailViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository,
    private val userLocalDataSource: UserLocalDataSource,
    private val permissionChecker: PermissionChecker,
    private val logger: FleetLogger
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
                val userId = try {
                    userLocalDataSource.getUserId() ?: ""
                } catch (e: Exception) {
                    ""
                }

                // Permission flags from the user's actual permission set.
                val hasUpdatePermission = permissionChecker.canManageTeam()
                val hasChangeRolePermission = permissionChecker.canChangeRole()
                val hasTogglePermission = permissionChecker.canToggleTeamMemberStatus()

                val availableTeamRoles = loadEditableRoles()

                val result = teamRepository.getTeamMember(memberId)

                result.fold(
                    onSuccess = { member ->
                        // Self-guard (identity, not role): users cannot act on own record here.
                        val isSelf = member.id == userId
                        val canEditMember = hasUpdatePermission && !isSelf
                        val canChangeRoleForMember = hasChangeRolePermission && !isSelf
                        val canToggleActiveMember = hasTogglePermission && !isSelf

                        updateState {
                            copy(
                                isLoading = false,
                                member = member,
                                currentUserId = userId,
                                availableRoles = availableTeamRoles,
                                canEdit = canEditMember,
                                canChangeRole = canChangeRoleForMember,
                                canToggleActive = canToggleActiveMember,
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
                                error = error.message?.let { UiText.Raw(it) }
                                    ?: UiText.StringRes(Res.string.error_load_team_member)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                logger.e(TAG_TEAM_DETAIL_VM, "loadMember failed", e)
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_load_team_member)
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
        if (!validateFields()) return

        updateState { copy(isSaving = true) }

        withContext(dispatcherProvider.io) {
            try {
                val state = currentState
                val roleToUpdate = if (state.canChangeRole && !state.isSelf) state.editRole else null

                logger.d(
                    TAG_TEAM_DETAIL_VM,
                    "Saving member=${state.memberId} canChangeRole=${state.canChangeRole} isSelf=${state.isSelf} " +
                        "editRole=${state.editRole} roleToUpdate=$roleToUpdate"
                )

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
                        sendEffect(
                            TeamMemberDetailContract.Effect.ShowSnackbar(
                                UiText.StringRes(Res.string.success_team_member_updated)
                            )
                        )
                        sendEffect(TeamMemberDetailContract.Effect.MemberUpdated)
                    },
                    onFailure = { error ->
                        updateState { copy(isSaving = false) }
                        sendEffect(
                            TeamMemberDetailContract.Effect.ShowSnackbar(
                                error.message?.let { UiText.Raw(it) }
                                    ?: UiText.StringRes(Res.string.error_update_team_member)
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                logger.e(TAG_TEAM_DETAIL_VM, "saveChanges failed", e)
                updateState { copy(isSaving = false) }
                sendEffect(
                    TeamMemberDetailContract.Effect.ShowSnackbar(
                        e.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_update_team_member)
                    )
                )
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val state = currentState

        if (state.editFirstName.isBlank()) {
            updateState { copy(firstNameError = UiText.StringRes(Res.string.error_first_name_required_team)) }
            isValid = false
        }

        if (state.editLastName.isBlank()) {
            updateState { copy(lastNameError = UiText.StringRes(Res.string.error_last_name_required_team)) }
            isValid = false
        }

        if (state.editEmail.isBlank()) {
            updateState { copy(emailError = UiText.StringRes(Res.string.error_email_required_team)) }
            isValid = false
        } else if (!isValidEmail(state.editEmail)) {
            updateState { copy(emailError = UiText.StringRes(Res.string.error_email_invalid_team)) }
            isValid = false
        }

        if (state.editMobile.isBlank()) {
            updateState { copy(mobileError = UiText.StringRes(Res.string.error_mobile_required_team)) }
            isValid = false
        } else if (!isValidMobile(state.editMobile)) {
            updateState { copy(mobileError = UiText.StringRes(Res.string.error_mobile_invalid_team)) }
            isValid = false
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean = ValidationUtils.isValidEmail(email)

    private fun isValidMobile(mobile: String): Boolean {
        val digitsOnly = mobile.filter { it.isDigit() }
        return digitsOnly.length >= 10
    }

    private suspend fun loadEditableRoles(): List<TeamMemberRole> {
        val rolesFromApi = teamRepository.getAssignableTeamRoles()
            .getOrNull()
            .orEmpty()
            .mapNotNull { role ->
                when (role.name.lowercase()) {
                    "admin" -> TeamMemberRole.ADMIN
                    "user" -> TeamMemberRole.USER
                    else -> null
                }
            }
            .distinct()

        return rolesFromApi.ifEmpty {
            listOf(TeamMemberRole.ADMIN, TeamMemberRole.USER)
        }
    }
}
