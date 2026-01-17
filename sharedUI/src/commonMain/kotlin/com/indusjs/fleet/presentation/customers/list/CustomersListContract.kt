package com.indusjs.fleet.presentation.customers.list

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.customer.Customer
import com.indusjs.fleet.navigation.FleetRoute

/**
 * MVI Contract for Customers List Screen.
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

        /**
         * Only show empty state after initial load completes and there are no customers.
         */
        val isEmpty: Boolean
            get() = isInitialLoadComplete && !isLoading && displayedCustomers.isEmpty()

        /**
         * Show error only if there's an error AND no cached data to display.
         */
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
        data class NavigateTo(val route: FleetRoute) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
