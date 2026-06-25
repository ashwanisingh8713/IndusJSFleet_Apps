package com.ijs.subscription.presentation.organization

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.subscription.domain.usecase.CreateTenantUseCase
import com.ijs.subscription.presentation.organization.CreateOrganizationContract.Effect
import com.ijs.subscription.presentation.organization.CreateOrganizationContract.Intent
import com.ijs.subscription.presentation.organization.CreateOrganizationContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.withContext

@Inject
class CreateOrganizationViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val createTenantUseCase: CreateTenantUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateOrganizationName -> updateOrganizationName(intent.name)
            is Intent.Submit -> submit()
            is Intent.DismissError -> updateState { copy(error = null) }
        }
    }

    private fun updateOrganizationName(value: String) {
        updateState {
            copy(
                organizationName = value,
                organizationNameError = validateName(value)
            )
        }
    }

    /** Inline field-error rule for the org name (non-blank, >=2 chars, >=2 alphanumeric). */
    private fun validateName(name: String): UiText? {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> null // empty is gated by isValid, not shown as an error while typing
            trimmed.length < 2 -> UiText.Raw("Organization name must be at least 2 characters.")
            deriveSlug(trimmed).length < 2 ->
                UiText.Raw("Organization name must contain at least 2 alphanumeric characters.")
            else -> null
        }
    }

    private fun deriveSlug(name: String): String =
        name.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]"), "")
            .take(48)

    private suspend fun submit() {
        val name = currentState.organizationName.trim()

        val nameError = if (name.isBlank()) {
            UiText.Raw("Organization name is required.")
        } else {
            validateName(name)
        }
        if (nameError != null) {
            updateState { copy(organizationNameError = nameError) }
            return
        }

        val slug = deriveSlug(name)

        updateState { copy(isCreating = true, error = null) }

        withContext(dispatcherProvider.io) {
            createTenantUseCase(name, slug).fold(
                onSuccess = {
                    updateState { copy(isCreating = false) }
                    sendEffect(Effect.NavigateToAddTeamMember)
                },
                onFailure = { e ->
                    val msg = e.message ?: "Failed to create organization. Please try again."
                    updateState { copy(isCreating = false, error = UiText.Raw(msg)) }
                    sendEffect(Effect.ShowError(UiText.Raw(msg)))
                }
            )
        }
    }
}
