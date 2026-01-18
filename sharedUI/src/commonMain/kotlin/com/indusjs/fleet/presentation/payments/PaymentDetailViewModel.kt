package com.indusjs.fleet.presentation.payments

import co.touchlab.kermit.Logger
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.user.UserRole
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Payment Detail Screen.
 */
@Inject
class PaymentDetailViewModel(
    private val repository: TripPaymentRepository,
    private val userRepository: UserRepository
) : MviViewModel<PaymentDetailContract.State, PaymentDetailContract.Intent, PaymentDetailContract.Effect>(PaymentDetailContract.State()) {

    private val log = Logger.withTag("PaymentDetailViewModel")

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
                log.e(result.exception) { "Failed to load payment" }
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
                sendEffect(PaymentDetailContract.Effect.ShowSnackbar("Payment deleted"))
                sendEffect(PaymentDetailContract.Effect.PaymentDeleted)
                sendEffect(PaymentDetailContract.Effect.NavigateBack)
            }
            is Result.Error -> {
                log.e(result.exception) { "Failed to delete payment" }
                updateState { copy(isDeleting = false) }
                sendEffect(PaymentDetailContract.Effect.ShowError(result.message ?: "Failed to delete"))
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private fun downloadReceipt() {
        // TODO: Implement receipt download/generation
        sendEffect(PaymentDetailContract.Effect.ShowSnackbar("Receipt download coming soon"))
    }
}
