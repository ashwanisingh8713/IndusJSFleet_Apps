package com.indusjs.fleet.presentation.customers.list

import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.customer.Customer
import com.indusjs.fleet.domain.usecase.customer.GetCustomersUseCase
import com.indusjs.fleet.domain.usecase.customer.GetLocalCustomersUseCase
import com.indusjs.fleet.domain.usecase.customer.RefreshCustomersUseCase
import com.indusjs.fleet.navigation.FleetRoute
import com.indusjs.fleet.presentation.customers.list.CustomersListContract.Effect
import com.indusjs.fleet.presentation.customers.list.CustomersListContract.Intent
import com.indusjs.fleet.presentation.customers.list.CustomersListContract.State
import com.indusjs.error.result.Result
import dev.zacsweers.metro.Inject

/**
 * ViewModel for Customers List Screen.
 * Loads customers from local DB first, then syncs from API.
 */
@Inject
class CustomersListViewModel(
    private val getCustomersUseCase: GetCustomersUseCase,
    private val refreshCustomersUseCase: RefreshCustomersUseCase,
    private val getLocalCustomersUseCase: GetLocalCustomersUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadCustomers)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadCustomers -> loadCustomers()
            is Intent.RefreshCustomers -> refreshCustomers()
            is Intent.LoadMore -> loadMore()
            is Intent.UpdateSearchQuery -> updateSearchQuery(intent.query)
            is Intent.OnCustomerClick -> navigateToDetail(intent.customer)
            is Intent.OnAddCustomerClick -> navigateToCreateCustomer()
            is Intent.ClearError -> updateState { copy(error = null) }
        }
    }

    /**
     * Load customers - first from local DB, then sync from API in background.
     */
    private suspend fun loadCustomers() {
        updateState { copy(isLoading = true, error = null) }

        // Step 1: Load from local DB first (fast)
        try {
            val localCustomers = getLocalCustomersUseCase()
            if (localCustomers.isNotEmpty()) {
                val customers = localCustomers.map { summary ->
                    Customer(
                        id = summary.id,
                        companyName = summary.companyName,
                        personName = summary.personName,
                        primaryContact = summary.primaryContact,
                        isActive = summary.isActive
                    )
                }
                updateState {
                    copy(
                        customers = customers,
                        filteredCustomers = customers,
                        isLoading = false,
                        isInitialLoadComplete = true
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore local DB errors, will try API
        }

        // Step 2: Sync from API in background
        when (val result = getCustomersUseCase(page = 1, perPage = 50)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoading = false,
                        isInitialLoadComplete = true,
                        customers = result.data,
                        filteredCustomers = filterCustomers(result.data, state.value.searchQuery),
                        currentPage = 1,
                        hasMore = result.data.size >= 50
                    )
                }
            }
            is Result.Error -> {
                // Only set error if we have no cached data
                if (state.value.customers.isEmpty()) {
                    updateState {
                        copy(
                            isLoading = false,
                            isInitialLoadComplete = true,
                            error = result.message ?: "Failed to load customers"
                        )
                    }
                } else {
                    // Have cached data, just mark loading complete
                    updateState { copy(isLoading = false, isInitialLoadComplete = true) }
                }
            }
            is Result.Loading -> { }
        }
    }

    /**
     * Refresh customers from API and save to local DB.
     */
    private suspend fun refreshCustomers() {
        updateState { copy(isRefreshing = true, error = null) }

        when (val result = refreshCustomersUseCase()) {
            is Result.Success -> {
                updateState {
                    copy(
                        isRefreshing = false,
                        customers = result.data,
                        filteredCustomers = filterCustomers(result.data, state.value.searchQuery),
                        currentPage = 1,
                        hasMore = result.data.size >= 50
                    )
                }
                sendEffect(Effect.ShowSnackbar("${result.data.size} customers synced"))
            }
            is Result.Error -> {
                updateState { copy(isRefreshing = false) }
                sendEffect(Effect.ShowSnackbar(result.message ?: "Sync failed"))
            }
            is Result.Loading -> { }
        }
    }

    private suspend fun loadMore() {
        if (state.value.isLoading || !state.value.hasMore) return

        val nextPage = state.value.currentPage + 1
        updateState { copy(isLoading = true) }

        when (val result = getCustomersUseCase(page = nextPage, perPage = 50)) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoading = false,
                        customers = customers + result.data,
                        filteredCustomers = filteredCustomers + result.data,
                        currentPage = nextPage,
                        hasMore = result.data.size >= 50
                    )
                }
            }
            is Result.Error -> {
                updateState {
                    copy(
                        isLoading = false,
                        error = result.message ?: result.exception.message
                    )
                }
            }
            is Result.Loading -> { }
        }
    }

    private fun updateSearchQuery(query: String) {
        val filtered = filterCustomers(state.value.customers, query)
        updateState {
            copy(
                searchQuery = query,
                filteredCustomers = filtered
            )
        }
    }

    private fun filterCustomers(customers: List<Customer>, query: String): List<Customer> {
        if (query.isBlank()) return customers
        val lowerQuery = query.lowercase()
        return customers.filter { customer ->
            customer.companyName.lowercase().contains(lowerQuery) ||
            customer.personName.lowercase().contains(lowerQuery) ||
            customer.primaryContact.contains(query)
        }
    }

    private fun navigateToDetail(customer: Customer) {
        sendEffect(Effect.NavigateTo(FleetRoute.CustomerDetail(customer.id)))
    }

    private fun navigateToCreateCustomer() {
        sendEffect(Effect.NavigateTo(FleetRoute.CreateCustomer))
    }
}
