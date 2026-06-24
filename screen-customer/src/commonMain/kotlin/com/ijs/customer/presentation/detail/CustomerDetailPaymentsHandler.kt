package com.ijs.customer.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.customer.TAG_CUSTOMER_PAYMENTS
import com.ijs.customer.domain.entity.PaymentMode
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.indusjs.error.result.Result
import com.indusjs.uicomponents.components.UiText
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.error_generic

/**
 * Handles payments tab data loading (pending + received),
 * pagination, filtering, and summary for CustomerDetailViewModel.
 */
class CustomerDetailPaymentsHandler(
    private val customerRepository: CustomerRepository,
    private val getState: () -> State,
    private val setState: (State.() -> State) -> Unit,
    private val logger: FleetLogger
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
                        pendingPaymentsError = result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_generic)
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
                        paymentsError = result.message?.let { UiText.Raw(it) }
                            ?: UiText.StringRes(Res.string.error_generic)
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
                logger.w(TAG_CUSTOMER_PAYMENTS, "Failed to load payment summary: ${result.message}")
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

