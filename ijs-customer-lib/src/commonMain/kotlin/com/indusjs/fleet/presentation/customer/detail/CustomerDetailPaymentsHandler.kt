package com.indusjs.fleet.presentation.customer.detail

import co.touchlab.kermit.Logger
import com.indusjs.fleet.domain.entity.customer.PaymentMode
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.presentation.customer.detail.CustomerDetailContract.State
import com.indusjs.error.result.Result

/**
 * Handles payments tab data loading (pending + received),
 * pagination, filtering, and summary for CustomerDetailViewModel.
 */
class CustomerDetailPaymentsHandler(
    private val customerRepository: CustomerRepository,
    private val getState: () -> State,
    private val setState: (State.() -> State) -> Unit,
    private val log: Logger
) {

    // ============= Pending Payments =============

    suspend fun loadPendingPayments(customerId: String) {
        val state = getState()
        if (customerId.isBlank() || !state.canViewFinancials) return

        setState { copy(isLoadingPendingPayments = true, pendingPaymentsError = null, pendingPaymentsPage = 1) }

        when (val result = customerRepository.getCustomerPendingPayments(customerId)) {
            is Result.Success -> {
                setState {
                    copy(
                        isLoadingPendingPayments = false,
                        pendingPayments = result.data.payments,
                        totalPendingAmount = result.data.totalPending,
                        overdueCount = result.data.overdueCount,
                        hasMorePendingPayments = result.data.hasMore,
                        pendingPaymentsDataLoaded = true
                    )
                }
            }
            is Result.Error -> {
                setState {
                    copy(
                        isLoadingPendingPayments = false,
                        pendingPaymentsError = result.message ?: "Failed to load pending payments"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun loadMorePendingPayments(customerId: String) {
        val state = getState()
        if (state.isLoadingPendingPayments || !state.hasMorePendingPayments) return

        val nextPage = state.pendingPaymentsPage + 1
        setState { copy(isLoadingPendingPayments = true) }

        when (val result = customerRepository.getCustomerPendingPayments(customerId, nextPage)) {
            is Result.Success -> {
                setState {
                    copy(
                        isLoadingPendingPayments = false,
                        pendingPayments = pendingPayments + result.data.payments,
                        pendingPaymentsPage = nextPage,
                        hasMorePendingPayments = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                setState { copy(isLoadingPendingPayments = false) }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun refreshPendingPayments(customerId: String) {
        setState { copy(pendingPaymentsDataLoaded = false) }
        loadPendingPayments(customerId)
    }

    // ============= Received Payments =============

    suspend fun loadPayments(customerId: String) {
        val state = getState()
        if (customerId.isBlank() || !state.canViewFinancials) return

        setState { copy(isLoadingPayments = true, paymentsError = null, paymentsPage = 1) }

        when (val result = customerRepository.getCustomerPayments(
            customerId = customerId,
            mode = state.paymentModeFilter?.apiValue
        )) {
            is Result.Success -> {
                setState {
                    copy(
                        isLoadingPayments = false,
                        receivedPayments = result.data.payments,
                        totalReceivedAmount = result.data.totalReceived,
                        hasMorePayments = result.data.hasMore,
                        paymentsDataLoaded = true
                    )
                }
                loadPaymentSummary(customerId)
            }
            is Result.Error -> {
                setState {
                    copy(
                        isLoadingPayments = false,
                        paymentsError = result.message ?: "Failed to load payments"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun loadPaymentSummary(customerId: String) {
        when (val result = customerRepository.getCustomerPaymentSummary(customerId)) {
            is Result.Success -> {
                setState { copy(paymentSummary = result.data) }
            }
            is Result.Error -> {
                log.w { "Failed to load payment summary: ${result.message}" }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun loadMorePayments(customerId: String) {
        val state = getState()
        if (state.isLoadingPayments || !state.hasMorePayments) return

        val nextPage = state.paymentsPage + 1
        setState { copy(isLoadingPayments = true) }

        when (val result = customerRepository.getCustomerPayments(
            customerId = customerId,
            page = nextPage,
            mode = state.paymentModeFilter?.apiValue
        )) {
            is Result.Success -> {
                setState {
                    copy(
                        isLoadingPayments = false,
                        receivedPayments = receivedPayments + result.data.payments,
                        paymentsPage = nextPage,
                        hasMorePayments = result.data.hasMore
                    )
                }
            }
            is Result.Error -> {
                setState { copy(isLoadingPayments = false) }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun setPaymentModeFilter(customerId: String, mode: PaymentMode?) {
        setState { copy(paymentModeFilter = mode, paymentsDataLoaded = false) }
        loadPayments(customerId)
    }

    suspend fun refreshPayments(customerId: String) {
        setState { copy(paymentsDataLoaded = false) }
        loadPayments(customerId)
    }
}

