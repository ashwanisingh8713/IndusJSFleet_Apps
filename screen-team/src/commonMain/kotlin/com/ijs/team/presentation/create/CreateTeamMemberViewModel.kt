package com.ijs.team.presentation.create

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.team.domain.entity.AssignableTeamRole
import com.ijs.team.domain.repository.TeamRepository
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_confirm_password_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_create_team_member
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_email_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_first_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_last_name_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_load_team_member
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_invalid
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_mobile_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_password_contains_name
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_password_min_chars
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_password_required
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_passwords_mismatch
import indusjsfleet.ijs_ui_components_lib.generated.resources.success_team_member_created
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Create Team Member screen.
 *
 * Loads assignable IAM roles from the Fleet API and creates the member with the selected role.
 * After creation, the backend syncs IAM direct permissions from the role definition.
 */
@Inject
class CreateTeamMemberViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val teamRepository: TeamRepository
) : MviViewModel<CreateTeamMemberContract.State, CreateTeamMemberContract.Intent, CreateTeamMemberContract.Effect>(
    CreateTeamMemberContract.State()
) {

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            loadAssignableRoles(excludeElevated = false)
        }
    }

    private suspend fun loadAssignableRoles(excludeElevated: Boolean) {
        updateState { copy(rolesLoading = true, error = null) }
        val result = teamRepository.getAssignableTeamRoles()
        val fromApi = result.getOrNull().orEmpty().let { roles ->
            if (excludeElevated) {
                roles.filter { !it.name.equals("owner", ignoreCase = true) }
            } else {
                roles
            }
        }
        val effective = if (fromApi.isNotEmpty()) {
            fromApi
        } else {
            defaultFallbackRoles()
        }
        val selected = effective.firstOrNull()?.name ?: ""
        updateState {
            copy(
                availableIamRoles = effective,
                selectedIamRoleName = selected,
                rolesLoading = false,
                error = if (fromApi.isEmpty() && result.isFailure) {
                    result.exceptionOrNull()?.message?.let { UiText.Raw(it) }
                        ?: UiText.StringRes(Res.string.error_load_team_member)
                } else {
                    null
                }
            )
        }
    }

    private fun defaultFallbackRoles(): List<AssignableTeamRole> = listOf(
        AssignableTeamRole(id = "", name = "admin", description = ""),
        AssignableTeamRole(id = "", name = "user", description = "")
    )

    override suspend fun handleIntent(intent: CreateTeamMemberContract.Intent) {
        when (intent) {
            is CreateTeamMemberContract.Intent.UpdateFirstName -> updateState { copy(firstName = intent.firstName) }
            is CreateTeamMemberContract.Intent.UpdateLastName -> updateState { copy(lastName = intent.lastName) }
            is CreateTeamMemberContract.Intent.UpdateEmail -> updateState { copy(email = intent.email) }
            is CreateTeamMemberContract.Intent.UpdateMobile -> updateState { copy(mobile = intent.mobile) }
            is CreateTeamMemberContract.Intent.UpdatePassword -> updateState { copy(password = intent.password) }
            is CreateTeamMemberContract.Intent.UpdateConfirmPassword ->
                updateState { copy(confirmPassword = intent.confirmPassword) }
            is CreateTeamMemberContract.Intent.SelectIamRole ->
                updateState { copy(selectedIamRoleName = intent.roleName) }
            is CreateTeamMemberContract.Intent.TogglePasswordVisibility ->
                updateState { copy(isPasswordVisible = !isPasswordVisible) }
            is CreateTeamMemberContract.Intent.ToggleConfirmPasswordVisibility ->
                updateState { copy(isConfirmPasswordVisible = !isConfirmPasswordVisible) }
            is CreateTeamMemberContract.Intent.CreateTeamMember -> createTeamMember()
            is CreateTeamMemberContract.Intent.ClearError -> updateState { copy(error = null) }
            is CreateTeamMemberContract.Intent.SetExcludeGeneralManager -> {
                if (intent.exclude) {
                    withContext(dispatcherProvider.io) {
                        loadAssignableRoles(excludeElevated = true)
                    }
                }
            }
        }
    }

    private suspend fun createTeamMember() {
        val firstName = currentState.firstName.trim()
        val lastName = currentState.lastName.trim()
        val email = currentState.email.trim()
        val mobile = currentState.mobile.trim()
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword
        val iamRole = currentState.selectedIamRoleName.trim()

        if (firstName.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_first_name_required)) }
            return
        }

        if (lastName.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_last_name_required)) }
            return
        }

        if (email.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_email_required)) }
            return
        }

        if (!isValidEmail(email)) {
            updateState { copy(error = UiText.StringRes(Res.string.error_email_invalid)) }
            return
        }

        if (mobile.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_mobile_required)) }
            return
        }

        if (mobile.length < 10) {
            updateState { copy(error = UiText.StringRes(Res.string.error_mobile_invalid)) }
            return
        }

        if (password.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_required)) }
            return
        }

        if (password.length < 8) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_min_chars)) }
            return
        }

        if (passwordContainsIdentity(password, firstName, lastName, email)) {
            updateState { copy(error = UiText.StringRes(Res.string.error_password_contains_name)) }
            return
        }

        if (confirmPassword.isEmpty()) {
            updateState { copy(error = UiText.StringRes(Res.string.error_confirm_password_required)) }
            return
        }

        if (password != confirmPassword) {
            updateState { copy(error = UiText.StringRes(Res.string.error_passwords_mismatch)) }
            return
        }

        if (iamRole.isEmpty() || currentState.availableIamRoles.none { it.name == iamRole }) {
            updateState { copy(error = UiText.StringRes(Res.string.error_load_team_member)) }
            return
        }

        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = teamRepository.createTeamMember(
                    email = email,
                    mobile = mobile,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    iamRole = iamRole
                )

                result.fold(
                    onSuccess = {
                        updateState { copy(isLoading = false) }
                        sendEffect(CreateTeamMemberContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_team_member_created)))
                        sendEffect(CreateTeamMemberContract.Effect.TeamMemberCreated)
                        sendEffect(CreateTeamMemberContract.Effect.NavigateBack)
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = error.message?.let { msg -> UiText.Raw(msg) }
                                    ?: UiText.StringRes(Res.string.error_create_team_member)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = e.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_create_team_member)
                    )
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return emailRegex.matches(email)
    }

    private fun passwordContainsIdentity(
        password: String,
        firstName: String,
        lastName: String,
        email: String
    ): Boolean {
        val lowerPass = password.lowercase()
        if (firstName.length >= 3 && lowerPass.contains(firstName.lowercase())) return true
        if (lastName.length >= 3 && lowerPass.contains(lastName.lowercase())) return true
        val emailLocal = email.substringBefore("@")
        if (emailLocal.length >= 3 && lowerPass.contains(emailLocal.lowercase())) return true
        return false
    }
}
