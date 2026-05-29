package com.indusjs.fleet.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.ijs.user.presentation.login.LoginViewModel
import com.ijs.onboarding.presentation.OnboardingViewModel
 import com.ijs.vehicle.presentation.costs.MaintenanceCostEntryViewModel
import com.ijs.trip.presentation.cost.TripCostEntryViewModel
import com.ijs.dashboard.presentation.DashboardViewModel
import com.ijs.driver.presentation.DriversViewModel
import com.ijs.driver.presentation.create.CreateDriverViewModel
import com.ijs.driver.presentation.detail.DriverDetailViewModel
import com.ijs.map.presentation.MapsViewModel
import com.ijs.reports.presentation.ReportsViewModel
import com.ijs.reports.presentation.consolidated.ConsolidatedPLViewModel
import com.ijs.reports.presentation.cost.CostAnalysisViewModel
import com.ijs.reports.presentation.trip.TripPLViewModel
import com.ijs.reports.presentation.vehicle.VehiclePLViewModel
import com.ijs.team.presentation.create.CreateTeamMemberViewModel
import com.ijs.team.presentation.detail.TeamMemberDetailViewModel
import com.ijs.team.presentation.list.TeamListViewModel
import com.ijs.trip.presentation.TripsViewModel
import com.ijs.trip.presentation.create.CreateTripViewModel
import com.ijs.trip.presentation.detail.TripDetailViewModel
import com.ijs.user.presentation.changepassword.ChangePasswordViewModel
import com.ijs.user.presentation.forgotpassword.ForgotPasswordViewModel
import com.ijs.user.presentation.profile.ProfileViewModel
import com.ijs.user.presentation.otp.OtpVerificationViewModel
import com.ijs.user.presentation.signup.SignUpViewModel
import com.ijs.vehicle.presentation.AddVehicleViewModel
import com.ijs.vehicle.presentation.VehiclesViewModel
import com.ijs.vehicle.presentation.detail.VehicleDetailViewModel
import com.ijs.subscription.presentation.plans.PlanPageMode
import com.ijs.subscription.presentation.plans.PlansViewModel
import com.ijs.subscription.presentation.checkout.PaymentCheckoutViewModel
import com.ijs.subscription.presentation.organization.CreateOrganizationViewModel

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

    // Onboarding
    fun hasCompletedOnboarding(): Boolean
    fun onboardingViewModel(): OnboardingViewModel

    // Auth ViewModels
    fun loginViewModel(): LoginViewModel
    fun signUpViewModel(): SignUpViewModel
    fun forgotPasswordViewModel(): ForgotPasswordViewModel
    fun otpVerificationViewModel(): OtpVerificationViewModel

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
    fun driverCostEntryViewModel(): com.ijs.driver.presentation.cost.DriverCostEntryViewModel

    // Alerts ViewModels
    fun alertsListViewModel(): com.ijs.alerts.presentation.AlertsListViewModel

    // Reports ViewModels
    fun reportsViewModel(): ReportsViewModel
    fun vehiclePLViewModel(): VehiclePLViewModel
    fun tripPLViewModel(): TripPLViewModel
    fun costAnalysisViewModel(): CostAnalysisViewModel
    fun consolidatedPLViewModel(): ConsolidatedPLViewModel

    // Customer ViewModels
    fun customersListViewModel(): com.ijs.customer.presentation.list.CustomersListViewModel
    fun customerDetailViewModel(): com.ijs.customer.presentation.detail.CustomerDetailViewModel
    fun createCustomerViewModel(): com.ijs.customer.presentation.create.CreateCustomerViewModel

    // Payment ViewModels
    fun paymentsViewModel(): com.ijs.trip.payment.presentation.PaymentsViewModel
    fun addPaymentViewModel(): com.ijs.trip.payment.presentation.AddTripPaymentViewModel
    fun paymentDetailViewModel(): com.ijs.trip.payment.presentation.PaymentDetailViewModel

    // Vehicle Finance ViewModels
    fun vehicleFinanceViewModel(): com.ijs.finance.presentation.VehicleFinanceViewModel

    // Subscription / Billing ViewModels
    fun subscriptionPlansViewModel(pageMode: PlanPageMode = PlanPageMode.CHOOSE): PlansViewModel
    fun subscriptionCheckoutViewModel(): PaymentCheckoutViewModel
    fun createOrganizationViewModel(): CreateOrganizationViewModel

    // Subscription gate — called after sign-in to determine the first screen
    suspend fun checkSubscriptionGate(): SubscriptionGateResult

    /** Mark the team setup onboarding step as completed (member created or skipped). */
    suspend fun markTeamSetupCompleted()
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

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        return cache[key] as? T
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
