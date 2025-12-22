package com.indusjs.fleet.navigation

import androidx.compose.runtime.Composable
import com.indusjs.fleet.feature.dashboard.presentation.DashboardScreen
import com.indusjs.fleet.feature.dashboard.presentation.DashboardViewModel
import com.indusjs.fleet.feature.drivers.presentation.DriversScreen
import com.indusjs.fleet.feature.drivers.presentation.DriversViewModel
import com.indusjs.fleet.feature.maps.presentation.MapsScreen
import com.indusjs.fleet.feature.maps.presentation.MapsViewModel
import com.indusjs.fleet.feature.trips.presentation.TripsScreen
import com.indusjs.fleet.feature.trips.presentation.TripsViewModel
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesScreen
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesViewModel

/**
 * Main navigation host for the Fleet Management app.
 *
 * This file provides a simplified navigation example.
 * For full navigation, integrate with Compose Navigation or Nav3.
 *
 * Usage example with Metro DI:
 * ```
 * @Composable
 * fun FleetApp(graph: FleetGraph) {
 *     var currentRoute by remember { mutableStateOf<FleetRoute>(FleetRoute.Dashboard) }
 *
 *     when (currentRoute) {
 *         is FleetRoute.Dashboard -> DashboardScreen(
 *             viewModel = graph.dashboardViewModel(),
 *             onNavigateToVehicles = { currentRoute = FleetRoute.Vehicles },
 *             onNavigateToDrivers = { currentRoute = FleetRoute.Drivers },
 *             onNavigateToTrips = { currentRoute = FleetRoute.Trips },
 *             onNavigateToMaps = { currentRoute = FleetRoute.Maps }
 *         )
 *         is FleetRoute.Vehicles -> VehiclesScreen(
 *             viewModel = graph.vehiclesViewModel(),
 *             onNavigateBack = { currentRoute = FleetRoute.Dashboard }
 *         )
 *         // ... other routes
 *     }
 * }
 * ```
 */

/**
 * Navigation actions for Fleet app
 */
data class FleetNavigationActions(
    val navigateToDashboard: () -> Unit = {},
    val navigateToVehicles: () -> Unit = {},
    val navigateToVehicleDetail: (String) -> Unit = {},
    val navigateToAddVehicle: () -> Unit = {},
    val navigateToDrivers: () -> Unit = {},
    val navigateToDriverDetail: (String) -> Unit = {},
    val navigateToAddDriver: () -> Unit = {},
    val navigateToTrips: () -> Unit = {},
    val navigateToTripDetail: (String) -> Unit = {},
    val navigateToCreateTrip: () -> Unit = {},
    val navigateToMaps: () -> Unit = {},
    val navigateToSettings: () -> Unit = {},
    val navigateBack: () -> Unit = {}
)

/**
 * Simple screen composable wrappers for navigation
 */

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel,
    navigationActions: FleetNavigationActions
) {
    DashboardScreen(
        viewModel = viewModel,
        onNavigateToVehicles = navigationActions.navigateToVehicles,
        onNavigateToDrivers = navigationActions.navigateToDrivers,
        onNavigateToTrips = navigationActions.navigateToTrips,
        onNavigateToMaps = navigationActions.navigateToMaps
    )
}

@Composable
fun VehiclesRoute(
    viewModel: VehiclesViewModel,
    navigationActions: FleetNavigationActions
) {
    VehiclesScreen(
        viewModel = viewModel,
        onNavigateToDetail = navigationActions.navigateToVehicleDetail,
        onNavigateToAdd = navigationActions.navigateToAddVehicle,
        onNavigateBack = navigationActions.navigateBack
    )
}

@Composable
fun DriversRoute(
    viewModel: DriversViewModel,
    navigationActions: FleetNavigationActions
) {
    DriversScreen(
        viewModel = viewModel,
        onNavigateToDetail = navigationActions.navigateToDriverDetail,
        onNavigateToAdd = navigationActions.navigateToAddDriver,
        onNavigateBack = navigationActions.navigateBack
    )
}

@Composable
fun TripsRoute(
    viewModel: TripsViewModel,
    navigationActions: FleetNavigationActions
) {
    TripsScreen(
        viewModel = viewModel,
        onNavigateToDetail = navigationActions.navigateToTripDetail,
        onNavigateToCreate = navigationActions.navigateToCreateTrip,
        onNavigateBack = navigationActions.navigateBack
    )
}

@Composable
fun MapsRoute(
    viewModel: MapsViewModel,
    navigationActions: FleetNavigationActions
) {
    MapsScreen(
        viewModel = viewModel,
        onNavigateToVehicleDetail = navigationActions.navigateToVehicleDetail,
        onNavigateBack = navigationActions.navigateBack
    )
}

