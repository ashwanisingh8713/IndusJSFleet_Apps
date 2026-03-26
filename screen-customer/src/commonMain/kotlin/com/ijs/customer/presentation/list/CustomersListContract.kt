package com.ijs.customer.presentation.list

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.customer.domain.entity.Customer

/**
 * MVI Contract for Customers List Screen.
 * Navigation effects use specific callbacks instead of FleetRoute
 * to keep this feature module isolated from navigation infrastructure.
 */
object CustomersListContract {

    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isInitialLoadComplete: Boolean = false,
        val customers: List<Customer> = emptyList(),
        val filteredCustomers: List<Customer> = emptyList(),
        val searchQuery: String = "",
        val error: String? = null,
        val currentPage: Int = 1,
        val hasMore: Boolean = false
    ) : UiState {

        val displayedCustomers: List<Customer>
            get() = if (searchQuery.isBlank()) customers else filteredCustomers

        val isEmpty: Boolean
            get() = isInitialLoadComplete && !isLoading && displayedCustomers.isEmpty()

        val showError: Boolean
            get() = error != null && customers.isEmpty() && isInitialLoadComplete
    }

    sealed interface Intent : UiIntent {
        data object LoadCustomers : Intent
        data object RefreshCustomers : Intent
        data object LoadMore : Intent
        data class UpdateSearchQuery(val query: String) : Intent
        data class OnCustomerClick(val customer: Customer) : Intent
        data object OnAddCustomerClick : Intent
        data object ClearError : Intent
    }

    sealed interface Effect : UiEffect {
        data class NavigateToCustomerDetail(val customerId: String) : Effect
        data object NavigateToCreateCustomer : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}

