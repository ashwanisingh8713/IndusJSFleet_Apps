package com.ijs.dashboard.presentation

import androidx.compose.runtime.Composable

/**
 * Facade for the Dashboard feature module.
 * Exposes composable entry points for navigation from sharedUI.
 */
object DashboardFeatureFacade {

    /**
     * Dashboard screen entry point.
     *
     * @param viewModel The DashboardViewModel instance (created by sharedUI DI layer)
     * @param onNavigateToVehicles Navigate to vehicles list
     * @param onNavigateToDrivers Navigate to drivers list
     * @param onNavigateToTrips Navigate to trips list
     * @param onNavigateToMaps Navigate to maps screen
     * @param onNavigateToProfile Navigate to user profile
     * @param onNavigateToTeam Navigate to team list
     * @param onNavigateToReports Navigate to reports hub
     * @param onNavigateToAddTripCost Navigate to add trip cost
     * @param onNavigateToAddVehicleCost Navigate to add maintenance cost
     * @param onNavigateToAddVehicle Navigate to add vehicle
     * @param onNavigateToAddDriver Navigate to create driver
     * @param onNavigateToCreateTrip Navigate to create trip
     * @param onNavigateToAddDriverCost Navigate to add driver cost
     * @param onNavigateToAlertsList Navigate to alerts list
     * @param onNavigateToCustomers Navigate to customers list
     * @param onNavigateToPayments Navigate to payments list
     * @param onNavigateToVehicleFinance Navigate to vehicle finance list
     */
    @Composable
    fun DashboardEntry(
        viewModel: DashboardViewModel,
        onNavigateToVehicles: () -> Unit = {},
        onNavigateToDrivers: () -> Unit = {},
        onNavigateToTrips: () -> Unit = {},
        onNavigateToMaps: () -> Unit = {},
        onNavigateToProfile: () -> Unit = {},
        onNavigateToTeam: () -> Unit = {},
        onNavigateToReports: () -> Unit = {},
        onNavigateToAddTripCost: () -> Unit = {},
        onNavigateToAddVehicleCost: () -> Unit = {},
        onNavigateToAddVehicle: () -> Unit = {},
        onNavigateToAddDriver: () -> Unit = {},
        onNavigateToCreateTrip: () -> Unit = {},
        onNavigateToAddDriverCost: () -> Unit = {},
        onNavigateToAlertsList: () -> Unit = {},
        onNavigateToCustomers: () -> Unit = {},
        onNavigateToPayments: () -> Unit = {},
        onNavigateToVehicleFinance: () -> Unit = {}
    ) {
        DashboardScreen(
            viewModel = viewModel,
            onNavigateToVehicles = onNavigateToVehicles,
            onNavigateToDrivers = onNavigateToDrivers,
            onNavigateToTrips = onNavigateToTrips,
            onNavigateToMaps = onNavigateToMaps,
            onNavigateToProfile = onNavigateToProfile,
            onNavigateToTeam = onNavigateToTeam,
            onNavigateToReports = onNavigateToReports,
            onNavigateToAddTripCost = onNavigateToAddTripCost,
            onNavigateToAddVehicleCost = onNavigateToAddVehicleCost,
            onNavigateToAddVehicle = onNavigateToAddVehicle,
            onNavigateToAddDriver = onNavigateToAddDriver,
            onNavigateToCreateTrip = onNavigateToCreateTrip,
            onNavigateToAddDriverCost = onNavigateToAddDriverCost,
            onNavigateToAlertsList = onNavigateToAlertsList,
            onNavigateToCustomers = onNavigateToCustomers,
            onNavigateToPayments = onNavigateToPayments,
            onNavigateToVehicleFinance = onNavigateToVehicleFinance
        )
    }
}

