package com.ijs.customer.presentation.detail

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.customer.TAG_CUSTOMER_FINANCIALS
import com.ijs.customer.domain.entity.FinancialPeriod
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.customer.presentation.detail.CustomerDetailContract.State
import com.indusjs.error.result.Result

/**
 * Handles financials tab data loading, period filtering,
 * and date range management for CustomerDetailViewModel.
 */
class CustomerDetailFinancialsHandler(
    private val customerRepository: CustomerRepository,
    private val getState: () -> State,
    private val setState: (State.() -> State) -> Unit,
    private val logger: FleetLogger
) {

    suspend fun loadFinancialReport(customerId: String) {
        val state = getState()
        if (customerId.isBlank() || !state.canViewFinancials) return

        setState { copy(isLoadingFinancials = true, financialsError = null) }

        when (val result = customerRepository.getCustomerFinancialReport(
            customerId = customerId,
            period = state.financialsPeriod.apiValue,
            startDate = state.financialsStartDate.takeIf { it.isNotBlank() },
            endDate = state.financialsEndDate.takeIf { it.isNotBlank() }
        )) {
            is Result.Success -> {
                setState {
                    copy(
                        isLoadingFinancials = false,
                        financialReport = result.data,
                        financialsDataLoaded = true
                    )
                }
            }
            is Result.Error -> {
                setState {
                    copy(
                        isLoadingFinancials = false,
                        financialsError = result.message ?: "Failed to load financial report"
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    suspend fun setFinancialsPeriod(customerId: String, period: FinancialPeriod) {
        setState { copy(financialsPeriod = period, financialsDataLoaded = false) }
        loadFinancialReport(customerId)
    }

    suspend fun setFinancialsDateRange(customerId: String, startDate: String, endDate: String) {
        setState {
            copy(
                financialsStartDate = startDate,
                financialsEndDate = endDate,
                financialsPeriod = FinancialPeriod.CUSTOM,
                showDateRangePicker = false,
                financialsDataLoaded = false
            )
        }
        loadFinancialReport(customerId)
    }

    suspend fun refreshFinancials(customerId: String) {
        setState { copy(financialsDataLoaded = false) }
        loadFinancialReport(customerId)
    }
}

