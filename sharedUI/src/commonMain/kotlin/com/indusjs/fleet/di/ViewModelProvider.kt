package com.indusjs.fleet.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.presentation.auth.LoginViewModel
 import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryViewModel
import com.indusjs.fleet.presentation.trips.cost.TripCostEntryViewModel
import com.indusjs.fleet.presentation.dashboard.DashboardViewModel
import com.indusjs.fleet.presentation.drivers.DriversViewModel
import com.indusjs.fleet.presentation.drivers.create.CreateDriverViewModel
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailViewModel
import com.indusjs.fleet.presentation.maps.MapsViewModel
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel
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

    // Cost Entry ViewModels
    fun tripCostEntryViewModel(): TripCostEntryViewModel
    fun maintenanceCostEntryViewModel(): MaintenanceCostEntryViewModel
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

