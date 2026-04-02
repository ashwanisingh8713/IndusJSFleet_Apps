package com.ijs.trip.payment.presentation

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.trip.payment.TAG_PAYMENTS_VM
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.uicomponents.components.UiText
import com.ijs.trip.payment.domain.entity.*
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * ViewModel for Payments List Screen.
 */
@Inject
class PaymentsViewModel(
    private val repository: TripPaymentRepository,
    private val logger: FleetLogger
) : MviViewModel<PaymentsContract.State, PaymentsContract.Intent, PaymentsContract.Effect>(PaymentsContract.State()) {
init {
        sendIntent(PaymentsContract.Intent.LoadPayments)
    }

    override suspend fun handleIntent(intent: PaymentsContract.Intent) {
        when (intent) {
            is PaymentsContract.Intent.LoadPayments -> loadPayments()
            is PaymentsContract.Intent.Refresh -> refresh()
            is PaymentsContract.Intent.LoadMore -> loadMore()

            // Filter
            is PaymentsContract.Intent.ShowFilterSheet -> updateState { copy(showFilterSheet = true, tempFilter = filter) }
            is PaymentsContract.Intent.HideFilterSheet -> updateState { copy(showFilterSheet = false) }
            is PaymentsContract.Intent.UpdateTempFilterTrip -> updateState { copy(tempFilter = tempFilter.copy(tripId = intent.tripId)) }
            is PaymentsContract.Intent.UpdateTempFilterCustomer -> updateState { copy(tempFilter = tempFilter.copy(customerId = intent.customerId)) }
            is PaymentsContract.Intent.UpdateTempFilterType -> updateState { copy(tempFilter = tempFilter.copy(paymentType = intent.type)) }
            is PaymentsContract.Intent.UpdateTempFilterMode -> updateState { copy(tempFilter = tempFilter.copy(paymentMode = intent.mode)) }
            is PaymentsContract.Intent.UpdateTempFilterStatus -> updateState { copy(tempFilter = tempFilter.copy(paymentStatus = intent.status)) }
            is PaymentsContract.Intent.UpdateTempFilterDateRange -> updateState {
                copy(tempFilter = tempFilter.copy(startDate = intent.startDate, endDate = intent.endDate))
            }
            is PaymentsContract.Intent.ApplyFilter -> applyFilter()
            is PaymentsContract.Intent.ResetFilter -> resetFilter()

            // Navigation
            is PaymentsContract.Intent.NavigateToPaymentDetail -> sendEffect(PaymentsContract.Effect.NavigateToDetail(intent.paymentId))
            is PaymentsContract.Intent.NavigateToAddPayment -> sendEffect(PaymentsContract.Effect.NavigateToAddPayment)
            is PaymentsContract.Intent.NavigateToAddPaymentForTrip -> sendEffect(PaymentsContract.Effect.NavigateToAddPaymentForTrip(intent.tripId))

            // Delete
            is PaymentsContract.Intent.ShowDeleteConfirmation -> updateState {
                copy(showDeleteConfirmation = true, paymentToDelete = intent.payment)
            }
            is PaymentsContract.Intent.HideDeleteConfirmation -> updateState {
                copy(showDeleteConfirmation = false, paymentToDelete = null)
            }
            is PaymentsContract.Intent.ConfirmDelete -> deletePayment()

            // PDF Export
            is PaymentsContract.Intent.ExportPaymentsToPdf -> {
                // PDF export is handled in the Screen composable
                // Just send effect to signal export started
                sendEffect(PaymentsContract.Effect.PdfExportStarted)
            }
        }
    }

    private suspend fun loadPayments() {
        updateState { copy(isLoading = true, error = null) }

        // Load payments list
        when (val result = repository.listPayments(state.value.filter)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoading = false,
                        payments = result.data.payments,
                        summary = result.data.summary,
                        page = result.data.page,
                        hasMore = result.data.hasMore
                    )
                }
                // Also load pending payments summary for accurate pending amount display
                loadPendingSummary()
            }
            is Result.Error -> {
                logger.e(TAG_PAYMENTS_VM, "Failed to load payments", result.exception)
                updateState { copy(isLoading = false, error = result.message) }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    /**
     * Load pending payments summary from dashboard API.
     * This provides the total pending amount across all trips.
     */
    private suspend fun loadPendingSummary() {
        when (val result = repository.getPendingPaymentsSummary()) {
            is Result.Success -> {
                logger.d(TAG_PAYMENTS_VM, "Loaded pending summary: ${result.data.totalPending}")
                updateState { copy(pendingSummary = result.data) }
            }
            is Result.Error -> {
                // Don't fail the whole screen, just log the error
                logger.w(TAG_PAYMENTS_VM, "Failed to load pending summary: ${result.message}")
            }
            is Result.Loading -> { /* Ignored */ }
        }
    }

    private suspend fun refresh() {
        updateState { copy(isRefreshing = true, error = null, page = 1) }

        val newFilter = state.value.filter.copy(page = 1)
        when (val result = repository.listPayments(newFilter)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isRefreshing = false,
                        payments = result.data.payments,
                        summary = result.data.summary,
                        page = result.data.page,
                        hasMore = result.data.hasMore
                    )
                }
                // Also reload pending summary on refresh
                loadPendingSummary()
            }
            is Result.Error -> {
                logger.e(TAG_PAYMENTS_VM, "Failed to refresh payments", result.exception)
                updateState { copy(isRefreshing = false) }
                sendEffect(
                    PaymentsContract.Effect.ShowError(
                        result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_refresh_data)
                    )
                )
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun loadMore() {
        if (state.value.isLoadingMore || !state.value.hasMore) return

        updateState { copy(isLoadingMore = true) }

        val nextPage = state.value.page + 1
        val newFilter = state.value.filter.copy(page = nextPage)

        when (val result = repository.listPayments(newFilter)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoadingMore = false,
                        payments = payments + result.data.payments,
                        page = result.data.page,
                        hasMore = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                logger.e(TAG_PAYMENTS_VM, "Failed to load more payments", result.exception)
                updateState { copy(isLoadingMore = false) }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }

    private suspend fun applyFilter() {
        updateState {
            copy(
                showFilterSheet = false,
                filter = tempFilter.copy(page = 1),
                page = 1
            )
        }
        loadPayments()
    }

    private suspend fun resetFilter() {
        updateState {
            copy(
                showFilterSheet = false,
                filter = TripPaymentFilter(),
                tempFilter = TripPaymentFilter(),
                page = 1
            )
        }
        loadPayments()
    }

    private suspend fun deletePayment() {
        val payment = state.value.paymentToDelete ?: return

        updateState { copy(isDeleting = true) }

        when (val result = repository.deletePayment(payment.id)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isDeleting = false,
                        showDeleteConfirmation = false,
                        paymentToDelete = null,
                        payments = payments.filter { it.id != payment.id }
                    )
                }
                sendEffect(PaymentsContract.Effect.ShowSnackbar(UiText.StringRes(Res.string.success_payment_deleted)))
                sendEffect(PaymentsContract.Effect.PaymentDeleted)
            }
            is Result.Error -> {
                logger.e(TAG_PAYMENTS_VM, "Failed to delete payment", result.exception)
                updateState { copy(isDeleting = false) }
                sendEffect(
                    PaymentsContract.Effect.ShowError(
                        result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_delete_payment)
                    )
                )
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}
