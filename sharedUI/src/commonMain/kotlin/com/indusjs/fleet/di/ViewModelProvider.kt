package com.indusjs.fleet.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.presentation.auth.LoginViewModel
 import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryViewModel
import com.indusjs.fleet.presentation.trips.cost.TripCostEntryViewModel
import com.indusjs.fleet.presentation.dashboard.DashboardViewModel
import com.indusjs.fleet.presentation.drivers.DriversViewModel
import com.indusjs.fleet.presentation.drivers.create.CreateDriverViewModel
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailViewModel
import com.indusjs.fleet.presentation.maps.MapsViewModel
import com.indusjs.fleet.presentation.reports.ReportsViewModel
import com.indusjs.fleet.presentation.reports.consolidated.ConsolidatedPLViewModel
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisViewModel
import com.indusjs.fleet.presentation.reports.trip.TripPLViewModel
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLViewModel
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel
import com.indusjs.fleet.presentation.team.detail.TeamMemberDetailViewModel
import com.indusjs.fleet.presentation.team.list.TeamListViewModel
import com.indusjs.fleet.presentation.trips.TripsViewModel
import com.indusjs.fleet.presentation.trips.create.CreateTripViewModel
import com.indusjs.fleet.presentation.trips.detail.TripDetailViewModel
import com.indusjs.fleet.presentation.user.changepassword.ChangePasswordViewModel
import com.indusjs.fleet.presentation.user.forgotpassword.ForgotPasswordViewModel
import com.indusjs.fleet.presentation.user.profile.ProfileViewModel
import com.indusjs.fleet.presentation.user.signup.SignUpViewModel
import com.indusjs.fleet.presentation.vehicles.AddVehicleViewModel
import com.indusjs.fleet.presentation.vehicles.VehiclesViewModel
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailViewModel

/**
 * Provider for all application ViewModels.
 * Encapsulates the DI graph creation and provides ViewModels to the UI layer.
 *
 * In production, Metro generates the graph implementations.
 * For development/preview, stub implementations can be provided.
 */
interface ViewModelProvider {
    val dispatcherProvider: DispatcherProvider
    val userRepository: UserRepository

    // Auth ViewModels
    fun loginViewModel(): LoginViewModel
    fun signUpViewModel(): SignUpViewModel
    fun forgotPasswordViewModel(): ForgotPasswordViewModel

    // User ViewModels
    fun profileViewModel(): ProfileViewModel
    fun changePasswordViewModel(): ChangePasswordViewModel

    // Feature ViewModels
    fun dashboardViewModel(): DashboardViewModel
    fun vehiclesViewModel(): VehiclesViewModel
    fun addVehicleViewModel(): AddVehicleViewModel
    fun vehicleDetailViewModel(): VehicleDetailViewModel
    fun driversViewModel(): DriversViewModel
    fun createDriverViewModel(): CreateDriverViewModel
    fun driverDetailViewModel(): DriverDetailViewModel
    fun tripsViewModel(): TripsViewModel
    fun createTripViewModel(): CreateTripViewModel
    fun tripDetailViewModel(): TripDetailViewModel
    fun mapsViewModel(): MapsViewModel
    fun teamListViewModel(): TeamListViewModel
    fun createTeamMemberViewModel(): CreateTeamMemberViewModel
    fun teamMemberDetailViewModel(): TeamMemberDetailViewModel

    // Cost Entry ViewModels
    fun tripCostEntryViewModel(): TripCostEntryViewModel
    fun maintenanceCostEntryViewModel(): MaintenanceCostEntryViewModel
    fun driverCostEntryViewModel(): com.indusjs.fleet.presentation.drivers.cost.DriverCostEntryViewModel

    // Alerts ViewModels
    fun alertsListViewModel(): com.indusjs.fleet.presentation.alerts.AlertsListViewModel

    // Reports ViewModels
    fun reportsViewModel(): ReportsViewModel
    fun vehiclePLViewModel(): VehiclePLViewModel
    fun tripPLViewModel(): TripPLViewModel
    fun costAnalysisViewModel(): CostAnalysisViewModel
    fun consolidatedPLViewModel(): ConsolidatedPLViewModel

    // Customer ViewModels
    fun customersListViewModel(): com.indusjs.fleet.presentation.customers.list.CustomersListViewModel
    fun customerDetailViewModel(): com.indusjs.fleet.presentation.customers.detail.CustomerDetailViewModel
    fun createCustomerViewModel(): com.indusjs.fleet.presentation.customers.create.CreateCustomerViewModel

    // Payment ViewModels
    fun paymentsViewModel(): com.indusjs.fleet.presentation.payments.PaymentsViewModel
    fun addPaymentViewModel(): com.indusjs.fleet.presentation.payments.AddPaymentViewModel
    fun paymentDetailViewModel(): com.indusjs.fleet.presentation.payments.PaymentDetailViewModel

    // Vehicle Finance ViewModels
    fun vehicleFinanceViewModel(): com.indusjs.fleet.presentation.finance.VehicleFinanceViewModel
}

/**
 * CompositionLocal for providing ViewModelProvider throughout the app.
 */
val LocalViewModelProvider = staticCompositionLocalOf<ViewModelProvider> {
    error("No ViewModelProvider provided. Call ProvideViewModels first.")
}

/**
 * Composable that provides ViewModelProvider to the composition.
 *
 * Usage:
 * ```kotlin
 * ProvideViewModels(provider) {
 *     val loginViewModel = LocalViewModelProvider.current.loginViewModel()
 *     LoginScreen(viewModel = loginViewModel)
 * }
 * ```
 */
@Composable
fun ProvideViewModels(
    provider: ViewModelProvider,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalViewModelProvider provides provider) {
        content()
    }
}

/**
 * Remembers a ViewModel instance scoped to the current composition.
 *
 * Usage:
 * ```kotlin
 * val loginViewModel = rememberViewModel { loginViewModel() }
 * ```
 */
@Composable
inline fun <T> rememberViewModel(crossinline factory: ViewModelProvider.() -> T): T {
    val provider = LocalViewModelProvider.current
    return remember { provider.factory() }
}

/**
 * Object to manage shared ViewModel instances across navigation.
 * This allows ViewModels to be shared across different NavEntry compositions.
 */
object SharedViewModelStore {
    private val cache = mutableMapOf<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrCreate(key: String, factory: () -> T): T {
        return cache.getOrPut(key) { factory() as Any } as T
    }

    fun clear(key: String) {
        cache.remove(key)
    }

    fun clearAll() {
        cache.clear()
    }
}

/**
 * Remembers a ViewModel instance with a key, allowing sharing across related screens.
 * ViewModels with the same key will return the same instance.
 *
 * This is useful for related screens in a flow (e.g., Finance Detail and Payment History)
 * that need to share the same ViewModel state.
 *
 * Usage:
 * ```kotlin
 * val financeViewModel = rememberSharedViewModel("finance_$vehicleId") { vehicleFinanceViewModel() }
 * ```
 */
@Composable
fun <T> rememberSharedViewModel(
    key: String,
    factory: ViewModelProvider.() -> T
): T {
    val provider = LocalViewModelProvider.current
    return remember(key) {
        SharedViewModelStore.getOrCreate(key) { provider.factory() }
    }
}

/**
 * Clears a shared ViewModel from cache when no longer needed.
 * Call this when leaving the flow completely (not when navigating between related screens).
 */
fun clearSharedViewModel(key: String) {
    SharedViewModelStore.clear(key)
}
