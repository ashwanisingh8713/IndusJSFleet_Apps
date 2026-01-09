package com.indusjs.fleet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import com.indusjs.fleet.FilePickerRequest
import com.indusjs.fleet.di.rememberViewModel
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.presentation.auth.LoginScreen
import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryScreen
import com.indusjs.fleet.presentation.trips.cost.TripCostEntryScreen
import com.indusjs.fleet.presentation.dashboard.DashboardScreen
import com.indusjs.fleet.presentation.drivers.DriversScreen
import com.indusjs.fleet.presentation.drivers.create.CreateDriverScreen
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailScreen
import com.indusjs.fleet.presentation.maps.MapsScreen
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberScreen
import com.indusjs.fleet.presentation.team.detail.TeamMemberDetailScreen
import com.indusjs.fleet.presentation.team.list.TeamListScreen
import com.indusjs.fleet.presentation.trips.TripsScreen
import com.indusjs.fleet.presentation.trips.create.CreateTripScreen
import com.indusjs.fleet.presentation.trips.detail.TripDetailScreen
import com.indusjs.fleet.presentation.user.changepassword.ChangePasswordScreen
import com.indusjs.fleet.presentation.user.forgotpassword.ForgotPasswordScreen
import com.indusjs.fleet.presentation.user.profile.ProfileScreen
import com.indusjs.fleet.presentation.user.signup.SignUpScreen
import com.indusjs.fleet.presentation.vehicles.AddVehicleScreen
import com.indusjs.fleet.presentation.vehicles.VehiclesScreen
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailScreen

/**
 * Navigation 3 entry provider for the Fleet Management app.
 *
 * Maps each FleetRoute to its screen composable with proper navigation handling.
 *
 * Features:
 * - Type-safe navigation with @Serializable routes
 * - Automatic back stack management
 * - System back button/gesture support (Android & iOS)
 */
@Composable
fun fleetEntryProvider(
    backStack: NavBackStack<FleetRoute>,
    onPickFile: ((FilePickerRequest) -> Unit)? = null,
    onOpenDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onDownloadDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onSaveDocument: ((documentName: String, fileBytes: ByteArray, mimeType: String) -> Unit)? = null
): (FleetRoute) -> NavEntry<FleetRoute> = { route ->
    when (route) {
        // ==================== Auth ====================

        is FleetRoute.Login -> NavEntry(route) {
            val viewModel = rememberViewModel { loginViewModel() }
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    backStack.navigateAndClear(FleetRoute.Dashboard)
                },
                onNavigateToSignUp = { backStack.add(FleetRoute.SignUp) },
                onNavigateToForgotPassword = { backStack.add(FleetRoute.ForgotPassword) }
            )
        }

        is FleetRoute.SignUp -> NavEntry(route) {
            val viewModel = rememberViewModel { signUpViewModel() }
            SignUpScreen(
                viewModel = viewModel,
                onSignUpSuccess = {
                    backStack.navigateAndClear(FleetRoute.Dashboard)
                },
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.ForgotPassword -> NavEntry(route) {
            val viewModel = rememberViewModel { forgotPasswordViewModel() }
            ForgotPasswordScreen(
                viewModel = viewModel,
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Main ====================

        is FleetRoute.Dashboard -> NavEntry(route) {
            val viewModel = rememberViewModel { dashboardViewModel() }
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToVehicles = { backStack.add(FleetRoute.Vehicles) },
                onNavigateToDrivers = { backStack.add(FleetRoute.Drivers) },
                onNavigateToTrips = { backStack.add(FleetRoute.Trips) },
                onNavigateToMaps = { backStack.add(FleetRoute.Maps) },
                onNavigateToProfile = { backStack.add(FleetRoute.Profile) },
                onNavigateToTeam = { backStack.add(FleetRoute.TeamList) },
                onNavigateToAddTripCost = { backStack.add(FleetRoute.TripCostEntry) },
                onNavigateToAddVehicleCost = { backStack.add(FleetRoute.MaintenanceCostEntry) },
                onNavigateToAddVehicle = { backStack.add(FleetRoute.AddVehicle) },
                onNavigateToAddDriver = { backStack.add(FleetRoute.CreateDriver) },
                onNavigateToCreateTrip = { backStack.add(FleetRoute.CreateTrip) }
            )
        }

        // ==================== User ====================

        is FleetRoute.Profile -> NavEntry(route) {
            val viewModel = rememberViewModel { profileViewModel() }
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToChangePassword = { backStack.add(FleetRoute.ChangePassword) },
                onNavigateBack = { backStack.removeLastOrNull() },
                onLogout = {
                    backStack.navigateAndClear(FleetRoute.Login)
                }
            )
        }

        is FleetRoute.ChangePassword -> NavEntry(route) {
            val viewModel = rememberViewModel { changePasswordViewModel() }
            ChangePasswordScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Vehicles ====================

        is FleetRoute.Vehicles -> NavEntry(route) {
            val viewModel = rememberViewModel { vehiclesViewModel() }
            VehiclesScreen(
                viewModel = viewModel,
                onNavigateToDetail = { backStack.add(FleetRoute.VehicleDetail(it)) },
                onNavigateToAdd = { backStack.add(FleetRoute.AddVehicle) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.VehicleDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { vehicleDetailViewModel() }
            VehicleDetailScreen(
                viewModel = viewModel,
                vehicleId = route.vehicleId,
                onNavigateBack = { backStack.removeLastOrNull() },
                onRequestFilePicker = { documentType, callback ->
                    onPickFile?.invoke(
                        FilePickerRequest(
                            documentType = try {
                                DocumentType.valueOf(documentType.uppercase())
                            } catch (_: Exception) {
                                DocumentType.OTHER
                            },
                            callback = callback
                        )
                    )
                },
                onOpenDocumentPreview = onOpenDocument,
                onDownloadDocument = onDownloadDocument,
                onSaveDocument = onSaveDocument
            )
        }

        is FleetRoute.AddVehicle -> NavEntry(route) {
            val viewModel = rememberViewModel { addVehicleViewModel() }
            AddVehicleScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onVehicleRegistered = { vehicleId ->
                    backStack.popAndNavigate(FleetRoute.VehicleDetail(vehicleId))
                },
                onRequestFilePicker = { documentType, callback ->
                    onPickFile?.invoke(FilePickerRequest(documentType, callback))
                }
            )
        }

        // ==================== Drivers ====================

        is FleetRoute.Drivers -> NavEntry(route) {
            val viewModel = rememberViewModel { driversViewModel() }
            DriversScreen(
                viewModel = viewModel,
                onNavigateToDetail = { backStack.add(FleetRoute.DriverDetail(it)) },
                onNavigateToAdd = { backStack.add(FleetRoute.CreateDriver) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.DriverDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { driverDetailViewModel() }
            DriverDetailScreen(
                viewModel = viewModel,
                driverId = route.driverId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CreateDriver -> NavEntry(route) {
            val viewModel = rememberViewModel { createDriverViewModel() }
            CreateDriverScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onDriverCreated = { driverId ->
                    backStack.popAndNavigate(FleetRoute.DriverDetail(driverId))
                }
            )
        }

        // ==================== Trips ====================

        is FleetRoute.Trips -> NavEntry(route) {
            val viewModel = rememberViewModel { tripsViewModel() }
            TripsScreen(
                viewModel = viewModel,
                onNavigateToDetail = { backStack.add(FleetRoute.TripDetail(it)) },
                onNavigateToCreate = { backStack.add(FleetRoute.CreateTrip) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TripDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { tripDetailViewModel() }
            TripDetailScreen(
                viewModel = viewModel,
                tripId = route.tripId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CreateTrip -> NavEntry(route) {
            val viewModel = rememberViewModel { createTripViewModel() }
            CreateTripScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onTripCreated = { tripId ->
                    backStack.popAndNavigate(FleetRoute.TripDetail(tripId))
                }
            )
        }

        // ==================== Cost Entry ====================

        is FleetRoute.TripCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { tripCostEntryViewModel() }
            TripCostEntryScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.MaintenanceCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { maintenanceCostEntryViewModel() }
            MaintenanceCostEntryScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Other ====================

        is FleetRoute.Maps -> NavEntry(route) {
            val viewModel = rememberViewModel { mapsViewModel() }
            MapsScreen(
                viewModel = viewModel,
                onNavigateToVehicleDetail = { backStack.add(FleetRoute.VehicleDetail(it)) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TeamList -> NavEntry(route) {
            val viewModel = rememberViewModel { teamListViewModel() }
            TeamListScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToCreateMember = { backStack.add(FleetRoute.CreateTeamMember) },
                onNavigateToMemberDetail = { memberId -> backStack.add(FleetRoute.TeamMemberDetail(memberId)) }
            )
        }

        is FleetRoute.CreateTeamMember -> NavEntry(route) {
            val viewModel = rememberViewModel { createTeamMemberViewModel() }
            CreateTeamMemberScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TeamMemberDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { teamMemberDetailViewModel() }
            TeamMemberDetailScreen(
                viewModel = viewModel,
                memberId = route.memberId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
    }
}
