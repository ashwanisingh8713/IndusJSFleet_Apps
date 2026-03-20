package com.indusjs.fleet.presentation.customer

import androidx.compose.runtime.Composable
import com.indusjs.fleet.presentation.customer.create.CreateCustomerViewModel
import com.indusjs.fleet.presentation.customer.detail.CustomerDetailViewModel
import com.indusjs.fleet.presentation.customer.list.CustomersListViewModel

/**
 * Facade for the Customer feature module.
 *
 * Provides @Composable entry points for each customer screen.
 * The sharedUI module uses this facade to render customer screens
 * without knowing internal implementation details.
 *
 * Navigation is handled via lambda callbacks — this module
 * never imports FleetRoute or any navigation infrastructure.
 *
 * ViewModels are passed from the outside (created by sharedUI's DI layer)
 * to maintain the existing rememberViewModel pattern.
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
        // Will wire to CustomersListScreen in next step
        // For now, this facade structure enables compilation
        CustomerScreensInternal.CustomersListScreenWrapper(
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
        CustomerScreensInternal.CustomerDetailScreenWrapper(
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
        CustomerScreensInternal.CreateCustomerScreenWrapper(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToCustomerDetail = onNavigateToCustomerDetail
        )
    }
}

/**
 * Internal screen wrappers that bridge the Facade to actual Screen composables.
 * This indirection allows the Screens to be refactored independently.
 */
internal object CustomerScreensInternal {

    @Composable
    fun CustomersListScreenWrapper(
        viewModel: CustomersListViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToCustomerDetail: (String) -> Unit,
        onNavigateToCreateCustomer: () -> Unit
    ) {
        // TODO: Wire to actual CustomersListScreen once Screens are migrated
        // For now, this compiles the Facade structure
    }

    @Composable
    fun CustomerDetailScreenWrapper(
        viewModel: CustomerDetailViewModel,
        customerId: String,
        onNavigateBack: () -> Unit
    ) {
        // TODO: Wire to actual CustomerDetailScreen once Screens are migrated
    }

    @Composable
    fun CreateCustomerScreenWrapper(
        viewModel: CreateCustomerViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToCustomerDetail: (String) -> Unit
    ) {
        // TODO: Wire to actual CreateCustomerScreen once Screens are migrated
    }
}

