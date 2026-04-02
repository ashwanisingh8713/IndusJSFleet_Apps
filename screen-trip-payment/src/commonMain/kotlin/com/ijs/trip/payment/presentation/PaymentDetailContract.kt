package com.ijs.trip.payment.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.uicomponents.components.UiText
import com.ijs.trip.payment.domain.entity.TripPayment

/**
 * MVI Contract for Payment Detail Screen.
 */
object PaymentDetailContract {

    data class State(
        val paymentId: String = "",
        val isLoading: Boolean = false,
        val payment: TripPayment? = null,
        val error: String? = null,

        // Delete confirmation
        val showDeleteConfirmation: Boolean = false,
        val isDeleting: Boolean = false,

        // User role (for edit/delete visibility)
        val canEdit: Boolean = false,
        val canDelete: Boolean = false
    ) : UiState

    sealed interface Intent : UiIntent {
        data class LoadPayment(val paymentId: String) : Intent
        data object Refresh : Intent
        data object NavigateToEdit : Intent
        data object ShowDeleteConfirmation : Intent
        data object HideDeleteConfirmation : Intent
        data object ConfirmDelete : Intent
        data object DownloadReceipt : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToEdit(val paymentId: String) : Effect
        data object NavigateBack : Effect
        data class ShowSnackbar(val message: UiText) : Effect
        data class ShowError(val message: UiText) : Effect
        data object PaymentDeleted : Effect
    }
}

