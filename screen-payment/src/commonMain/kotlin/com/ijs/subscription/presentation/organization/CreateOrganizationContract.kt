package com.ijs.subscription.presentation.organization

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText

object CreateOrganizationContract {

    data class State(
        val organizationName: String = "",
        val organizationNameError: UiText? = null,
        val isCreating: Boolean = false,
        val error: UiText? = null
    ) : UiState {
        /** Submit is enabled only when the name is non-blank and has no field error. */
        val isValid: Boolean
            get() = organizationName.isNotBlank() && organizationNameError == null
    }

    sealed interface Intent : UiIntent {
        data class UpdateOrganizationName(val name: String) : Intent
        data object Submit : Intent
        data object DismissError : Intent
    }

    sealed interface Effect : UiEffect {
        data object NavigateToAddTeamMember : Effect
        data class ShowError(val message: UiText) : Effect
    }
}
