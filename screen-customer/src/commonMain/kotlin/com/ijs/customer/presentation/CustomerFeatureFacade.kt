package com.ijs.customer.presentation

import androidx.compose.runtime.Composable
import com.ijs.customer.presentation.create.CreateCustomerScreen
import com.ijs.customer.presentation.create.CreateCustomerViewModel
import com.ijs.customer.presentation.detail.CustomerDetailScreen
import com.ijs.customer.presentation.detail.CustomerDetailViewModel
import com.ijs.customer.presentation.list.CustomersListScreen
import com.ijs.customer.presentation.list.CustomersListViewModel

/**
 * Facade for the Customer feature module.
 *
 * Provides @Composable entry points for each customer screen.
 * Navigation is handled via lambda callbacks — never FleetRoute.
 * ViewModels are passed from the outside (created by sharedUI's DI layer).
 */
object CustomerFeatureFacade {

    /**
     * Entry point for the Customers List screen.
     *
     * @param viewModel Pre-created ViewModel (from sharedUI's DI/rememberViewModel)
     * @param onNavigateBack Callback to navigate back
     * @param onNavigateToCustomerDetail Callback to navigate to customer detail by ID
     * @param onNavigateToCreateCustomer Callback to navigate to create customer screen
     */
    @Composable
    fun CustomersListEntry(
        viewModel: CustomersListViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToCustomerDetail: (customerId: String) -> Unit,
        onNavigateToCreateCustomer: () -> Unit
    ) {
        CustomersListScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToCustomerDetail = onNavigateToCustomerDetail,
            onNavigateToCreateCustomer = onNavigateToCreateCustomer
        )
    }

    /**
     * Entry point for the Customer Detail screen.
     *
     * @param viewModel Pre-created ViewModel
     * @param customerId Customer ID to load
     * @param onNavigateBack Callback to navigate back
     */
    @Composable
    fun CustomerDetailEntry(
        viewModel: CustomerDetailViewModel,
        customerId: String,
        onNavigateBack: () -> Unit
    ) {
        CustomerDetailScreen(
            viewModel = viewModel,
            customerId = customerId,
            onNavigateBack = onNavigateBack
        )
    }

    /**
     * Entry point for the Create Customer screen.
     *
     * @param viewModel Pre-created ViewModel
     * @param onNavigateBack Callback to navigate back
     * @param onNavigateToCustomerDetail Callback to navigate to detail after creation
     */
    @Composable
    fun CreateCustomerEntry(
        viewModel: CreateCustomerViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToCustomerDetail: (customerId: String) -> Unit
    ) {
        CreateCustomerScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToCustomerDetail = onNavigateToCustomerDetail
        )
    }
}
