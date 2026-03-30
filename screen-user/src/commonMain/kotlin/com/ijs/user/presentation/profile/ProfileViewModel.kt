package com.ijs.user.presentation.profile

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.uicomponents.components.UiText
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.withContext

/**
 * ViewModel for the User Profile screen implementing MVI pattern.
 */
@Inject
class ProfileViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val userRepository: UserRepository
) : MviViewModel<ProfileContract.State, ProfileContract.Intent, ProfileContract.Effect>(ProfileContract.State()) {

    init {
        // Auto-load profile on initialization
        sendIntent(ProfileContract.Intent.LoadProfile)
    }

    override suspend fun handleIntent(intent: ProfileContract.Intent) {
        when (intent) {
            is ProfileContract.Intent.LoadProfile -> loadProfile()
            is ProfileContract.Intent.RefreshProfile -> loadProfile(isRefresh = true)
            is ProfileContract.Intent.StartEditing -> startEditing()
            is ProfileContract.Intent.CancelEditing -> cancelEditing()
            is ProfileContract.Intent.UpdateFirstName -> updateState { copy(editFirstName = intent.firstName) }
            is ProfileContract.Intent.UpdateLastName -> updateState { copy(editLastName = intent.lastName) }
            is ProfileContract.Intent.UpdateEmail -> updateState { copy(editEmail = intent.email) }
            is ProfileContract.Intent.UpdateMobile -> updateState { copy(editMobile = intent.mobile) }
            is ProfileContract.Intent.SaveProfile -> saveProfile()
            is ProfileContract.Intent.ClearError -> updateState { copy(error = null, updateError = null) }
            is ProfileContract.Intent.NavigateToChangePassword -> sendEffect(ProfileContract.Effect.NavigateToChangePassword)
            is ProfileContract.Intent.Logout -> logout()
        }
    }

    private suspend fun loadProfile(isRefresh: Boolean = false) {
        if (!isRefresh) {
            updateState { copy(isLoading = true, error = null) }
        }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.getProfile()
                result.fold(
                    onSuccess = { profile ->
                        updateState {
                            copy(
                                isLoading = false,
                                profile = profile,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isLoading = false,
                                error = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.error_load_profile)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isLoading = false,
                        error = if (!e.message.isNullOrBlank()) UiText.Raw(e.message!!) else UiText.StringRes(Res.string.error_load_profile)
                    )
                }
            }
        }
    }

    private fun startEditing() {
        currentState.profile?.user?.let { user ->
            updateState {
                copy(
                    isEditing = true,
                    editFirstName = user.firstName,
                    editLastName = user.lastName,
                    editEmail = user.email,
                    editMobile = user.mobile
                )
            }
        }
    }

    private fun cancelEditing() {
        updateState {
            copy(
                isEditing = false,
                editFirstName = "",
                editLastName = "",
                editEmail = "",
                editMobile = "",
                updateError = null
            )
        }
    }

    private suspend fun saveProfile() {
        val firstName = currentState.editFirstName.trim()
        val lastName = currentState.editLastName.trim()
        val email = currentState.editEmail.trim()
        val mobile = currentState.editMobile.trim()

        // Validation
        if (firstName.isEmpty()) {
            updateState { copy(updateError = UiText.StringRes(Res.string.error_first_name_required)) }
            return
        }
        if (lastName.isEmpty()) {
            updateState { copy(updateError = UiText.StringRes(Res.string.error_last_name_required)) }
            return
        }
        if (email.isEmpty()) {
            updateState { copy(updateError = UiText.StringRes(Res.string.error_email_required)) }
            return
        }

        updateState { copy(isUpdating = true, updateError = null) }

        withContext(dispatcherProvider.io) {
            try {
                val result = userRepository.updateProfile(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    mobile = mobile
                )
                result.fold(
                    onSuccess = { updatedUser ->
                        updateState {
                            copy(
                                isUpdating = false,
                                isEditing = false,
                                profile = profile?.copy(user = updatedUser)
                            )
                        }
                        sendEffect(ProfileContract.Effect.ProfileUpdated)
                        sendEffect(ProfileContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_profile_updated)))
                    },
                    onFailure = { error ->
                        updateState {
                            copy(
                                isUpdating = false,
                                updateError = if (!error.message.isNullOrBlank()) UiText.Raw(error.message!!) else UiText.StringRes(Res.string.error_update_profile)
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                updateState {
                    copy(
                        isUpdating = false,
                        updateError = if (!e.message.isNullOrBlank()) UiText.Raw(e.message!!) else UiText.StringRes(Res.string.error_update_profile)
                    )
                }
            }
        }
    }

    private suspend fun logout() {
        withContext(dispatcherProvider.io) {
            try {
                userRepository.logout()
                sendEffect(ProfileContract.Effect.NavigateToLogin)
            } catch (_: Exception) {
                sendEffect(ProfileContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.error_logout_failed)))
            }
        }
    }
}
