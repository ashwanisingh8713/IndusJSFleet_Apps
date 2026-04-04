package com.ijs.trip.payment.presentation

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.payment.TAG_PAYMENT_DETAIL_VM
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.user.UserRole
import com.indusjs.fleet.domain.repository.states.StatesRepository
import com.indusjs.uicomponents.components.UiText
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import dev.zacsweers.metro.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * ViewModel for Payment Detail Screen.
 */
@Inject
class PaymentDetailViewModel(
    private val repository: TripPaymentRepository,
    private val userRepository: UserRepository,
    private val logger: FleetLogger,
    private val statesRepository: StatesRepository? = null
) : MviViewModel<PaymentDetailContract.State, PaymentDetailContract.Intent, PaymentDetailContract.Effect>(PaymentDetailContract.State()) {

    init {
        viewModelScope.launch {
            try {
                val labels = statesRepository?.getPaymentStatesFlat()?.toMap() ?: emptyMap()
                if (labels.isNotEmpty()) updateState { copy(paymentStateLabels = labels) }
            } catch (_: Exception) { /* fallback */ }
        }
    }

override suspend fun handleIntent(intent: PaymentDetailContract.Intent) {
        when (intent) {
            is PaymentDetailContract.Intent.LoadPayment -> loadPayment(intent.paymentId)
            is PaymentDetailContract.Intent.Refresh -> refresh()
            is PaymentDetailContract.Intent.NavigateToEdit -> {
                state.value.payment?.let {
                    sendEffect(PaymentDetailContract.Effect.NavigateToEdit(it.id))
                }
            }
            is PaymentDetailContract.Intent.ShowDeleteConfirmation -> updateState { copy(showDeleteConfirmation = true) }
            is PaymentDetailContract.Intent.HideDeleteConfirmation -> updateState { copy(showDeleteConfirmation = false) }
            is PaymentDetailContract.Intent.ConfirmDelete -> deletePayment()
            is PaymentDetailContract.Intent.DownloadReceipt -> downloadReceipt()
        }
    }

    private suspend fun loadPayment(paymentId: String) {
        updateState { copy(paymentId = paymentId, isLoading = true, error = null) }

        // Check user role for permissions
        checkPermissions()

        when (val result = repository.getPayment(paymentId)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, payment = result.data) }
            }
            is Result.Error -> {
                logger.e(TAG_PAYMENT_DETAIL_VM, "Failed to load payment", result.exception)
                updateState { copy(isLoading = false, error = result.message) }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun refresh() {
        val paymentId = state.value.paymentId
        if (paymentId.isNotBlank()) {
            loadPayment(paymentId)
        }
    }

    private suspend fun checkPermissions() {
        userRepository.getProfile().fold(
            onSuccess = { profile ->
                val role = profile.user.role
                val canEdit = role == UserRole.OWNER || role == UserRole.GENERAL_MANAGER
                val canDelete = role == UserRole.OWNER
                updateState { copy(canEdit = canEdit, canDelete = canDelete) }
            },
            onFailure = {
                // Default to no permissions
                updateState { copy(canEdit = false, canDelete = false) }
            }
        )
    }

    private suspend fun deletePayment() {
        val payment = state.value.payment ?: return

        updateState { copy(isDeleting = true) }

        when (val result = repository.deletePayment(payment.id)) {
            is Result.Success -> {
                updateState { copy(isDeleting = false, showDeleteConfirmation = false) }
                sendEffect(PaymentDetailContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_payment_deleted)))
                sendEffect(PaymentDetailContract.Effect.PaymentDeleted)
                sendEffect(PaymentDetailContract.Effect.NavigateBack)
            }
            is Result.Error -> {
                logger.e(TAG_PAYMENT_DETAIL_VM, "Failed to delete payment", result.exception)
                updateState { copy(isDeleting = false) }
                sendEffect(
                    PaymentDetailContract.Effect.ShowError(
                        result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_delete_payment)
                    )
                )
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private fun downloadReceipt() {
        // TODO: Implement receipt download/generation
        sendEffect(PaymentDetailContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.receipt_download_coming_soon)))
    }
}
