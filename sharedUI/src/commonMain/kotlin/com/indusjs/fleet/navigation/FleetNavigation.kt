package com.indusjs.fleet.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import com.indusjs.fleet.FilePickerRequest
import com.indusjs.fleet.di.rememberViewModel
import com.indusjs.fleet.di.rememberSharedViewModel
import com.indusjs.fleet.di.clearSharedViewModel
import com.indusjs.fleet.domain.entity.vehicle.DocumentType
import com.indusjs.fleet.presentation.auth.LoginScreen
import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryScreen
import com.indusjs.fleet.presentation.trips.cost.TripCostEntryScreen
import com.indusjs.fleet.presentation.drivers.cost.DriverCostEntryScreen
import com.indusjs.fleet.presentation.alerts.AlertsListScreen
import com.indusjs.fleet.presentation.dashboard.DashboardScreen
import com.indusjs.fleet.presentation.drivers.DriversScreen
import com.indusjs.fleet.presentation.drivers.create.CreateDriverScreen
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailScreen
import com.indusjs.fleet.presentation.maps.MapsScreen
import com.indusjs.fleet.presentation.reports.ReportsScreen
import com.indusjs.fleet.presentation.reports.consolidated.ConsolidatedPLScreen
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisScreen
import com.indusjs.fleet.presentation.reports.trip.TripProfitLossScreen
import com.indusjs.fleet.presentation.reports.vehicle.VehicleProfitLossScreen
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
import com.indusjs.fleet.presentation.customers.list.CustomersListScreen
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailScreen
import com.indusjs.fleet.presentation.customers.create.CreateCustomerScreen
import com.indusjs.fleet.presentation.payments.PaymentsScreen
import com.indusjs.fleet.presentation.payments.PaymentDetailScreen
import com.indusjs.fleet.presentation.payments.AddPaymentScreen
import com.indusjs.fleet.presentation.finance.VehicleFinanceScreen
import com.indusjs.fleet.presentation.finance.VehicleFinanceDetailScreen
import com.indusjs.fleet.presentation.finance.AddPurchaseInfoScreen
import com.indusjs.fleet.presentation.finance.EmiPaymentHistoryScreen

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
                onNavigateToReports = { backStack.add(FleetRoute.Reports) },
                onNavigateToAddTripCost = { backStack.add(FleetRoute.TripCostEntry()) },
                onNavigateToAddVehicleCost = { backStack.add(FleetRoute.MaintenanceCostEntry()) },
                onNavigateToAddVehicle = { backStack.add(FleetRoute.AddVehicle) },
                onNavigateToAddDriver = { backStack.add(FleetRoute.CreateDriver) },
                onNavigateToCreateTrip = { backStack.add(FleetRoute.CreateTrip) },
                onNavigateToAddDriverCost = { backStack.add(FleetRoute.DriverCostEntry) },
                onNavigateToAlertsList = { backStack.add(FleetRoute.AlertsList) },
                onNavigateToCustomers = { backStack.add(FleetRoute.Customers) },
                onNavigateToPayments = { backStack.add(FleetRoute.Payments) },
                onNavigateToVehicleFinance = { backStack.add(FleetRoute.VehicleFinance) }
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
                onNavigateToMaintenanceCost = { vehicleId -> backStack.add(FleetRoute.MaintenanceCostEntry(vehicleId = vehicleId)) },
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
                onNavigateToCreateTeamMember = {
                    // Exclude General Manager as only Manager/Supervisor can be caretakers
                    backStack.add(FleetRoute.CreateTeamMember(excludeGeneralManager = true))
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
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToAddTripCost = { tripId, vehicleId ->
                    backStack.add(FleetRoute.TripCostEntry(tripId = tripId, vehicleId = vehicleId))
                },
                onNavigateToAddPayment = { tripId, vehicleId ->
                    backStack.add(FleetRoute.AddPayment(tripId = tripId, vehicleId = vehicleId))
                }
            )
        }

        is FleetRoute.CreateTrip -> NavEntry(route) {
            val viewModel = rememberViewModel { createTripViewModel() }
            CreateTripScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onTripCreated = { tripId ->
                    backStack.popAndNavigate(FleetRoute.TripDetail(tripId))
                },
                onNavigateToAddCustomer = {
                    backStack.add(FleetRoute.CreateCustomer)
                }
            )
        }

        // ==================== Cost Entry ====================

        is FleetRoute.TripCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { tripCostEntryViewModel() }
            TripCostEntryScreen(
                viewModel = viewModel,
                initialTripId = route.tripId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.MaintenanceCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { maintenanceCostEntryViewModel() }
            MaintenanceCostEntryScreen(
                viewModel = viewModel,
                initialVehicleId = route.vehicleId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.DriverCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { driverCostEntryViewModel() }
            DriverCostEntryScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.AlertsList -> NavEntry(route) {
            val viewModel = rememberViewModel { alertsListViewModel() }
            AlertsListScreen(
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
                onNavigateToCreateMember = { backStack.add(FleetRoute.CreateTeamMember()) },
                onNavigateToMemberDetail = { memberId -> backStack.add(FleetRoute.TeamMemberDetail(memberId)) }
            )
        }

        is FleetRoute.CreateTeamMember -> NavEntry(route) {
            val viewModel = rememberViewModel { createTeamMemberViewModel() }
            CreateTeamMemberScreen(
                viewModel = viewModel,
                excludeGeneralManager = route.excludeGeneralManager,
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

        // ==================== Reports ====================

        is FleetRoute.Reports -> NavEntry(route) {
            val viewModel = rememberViewModel { reportsViewModel() }
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToVehiclePL = { backStack.add(FleetRoute.VehicleProfitLoss) },
                onNavigateToTripPL = { backStack.add(FleetRoute.TripProfitLoss) },
                onNavigateToCostAnalysis = { backStack.add(FleetRoute.CostAnalysis) },
                onNavigateToConsolidatedPL = { backStack.add(FleetRoute.ConsolidatedPL) }
            )
        }

        is FleetRoute.VehicleProfitLoss -> NavEntry(route) {
            val viewModel = rememberViewModel { vehiclePLViewModel() }
            VehicleProfitLossScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TripProfitLoss -> NavEntry(route) {
            val viewModel = rememberViewModel { tripPLViewModel() }
            TripProfitLossScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CostAnalysis -> NavEntry(route) {
            val viewModel = rememberViewModel { costAnalysisViewModel() }
            CostAnalysisScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.ConsolidatedPL -> NavEntry(route) {
            val viewModel = rememberViewModel { consolidatedPLViewModel() }
            ConsolidatedPLScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Customers ====================

        is FleetRoute.Customers -> NavEntry(route) {
            val viewModel = rememberViewModel { customersListViewModel() }
            CustomersListScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigate = { route ->
                    when (route) {
                        is FleetRoute.CustomerDetail -> backStack.add(route)
                        is FleetRoute.CreateCustomer -> backStack.add(route)
                        else -> {}
                    }
                }
            )
        }

        is FleetRoute.CustomerDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { customerDetailViewModel() }
            CustomerDetailScreen(
                viewModel = viewModel,
                customerId = route.customerId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CreateCustomer -> NavEntry(route) {
            val viewModel = rememberViewModel { createCustomerViewModel() }
            CreateCustomerScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigate = { navRoute ->
                    when (navRoute) {
                        is FleetRoute.CustomerDetail -> backStack.popAndNavigate(navRoute)
                        else -> {}
                    }
                }
            )
        }

        // ==================== Payments ====================

        is FleetRoute.Payments -> NavEntry(route) {
            val viewModel = rememberViewModel { paymentsViewModel() }
            PaymentsScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToDetail = { paymentId ->
                    backStack.add(FleetRoute.PaymentDetail(paymentId))
                },
                onNavigateToAddPayment = { tripId ->
                    backStack.add(FleetRoute.AddPayment(tripId))
                }
            )
        }

        is FleetRoute.PaymentDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { paymentDetailViewModel() }
            PaymentDetailScreen(
                viewModel = viewModel,
                paymentId = route.paymentId,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToEdit = { paymentId ->
                    backStack.add(FleetRoute.EditPayment(paymentId))
                }
            )
        }

        is FleetRoute.AddPayment -> NavEntry(route) {
            val viewModel = rememberViewModel { addPaymentViewModel() }
            AddPaymentScreen(
                viewModel = viewModel,
                tripId = route.tripId,
                paymentId = null,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.EditPayment -> NavEntry(route) {
            val viewModel = rememberViewModel { addPaymentViewModel() }
            AddPaymentScreen(
                viewModel = viewModel,
                tripId = null,
                paymentId = route.paymentId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Vehicle Finance ====================
        // All finance screens share the same ViewModel instance using "vehicle_finance_flow" key
        // This prevents reloading when navigating back from Detail to List

        is FleetRoute.VehicleFinance -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            VehicleFinanceScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    // Clear shared ViewModel when leaving the finance flow completely
                    clearSharedViewModel(financeFlowKey)
                    backStack.removeLastOrNull()
                },
                onNavigateToDetail = { vehicleId ->
                    backStack.add(FleetRoute.VehicleFinanceDetail(vehicleId.toString()))
                },
                onNavigateToAddPurchase = { backStack.add(FleetRoute.AddPurchaseInfo) }
            )
        }

        is FleetRoute.VehicleFinanceDetail -> NavEntry(route) {
            // Use same shared ViewModel key as VehicleFinance list
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            VehicleFinanceDetailScreen(
                vehicleId = route.vehicleId.toIntOrNull() ?: 0,
                viewModel = viewModel,
                onNavigateBack = {
                    // Don't clear - let VehicleFinance list screen clear it when leaving
                    backStack.removeLastOrNull()
                },
                onNavigateToEdit = { backStack.add(FleetRoute.EditPurchaseInfo(route.vehicleId)) },
                onNavigateToHistory = { backStack.add(FleetRoute.EmiPaymentHistory(route.vehicleId)) }
            )
        }

        is FleetRoute.AddPurchaseInfo -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            AddPurchaseInfoScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.EditPurchaseInfo -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            AddPurchaseInfoScreen(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.EmiPaymentHistory -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            EmiPaymentHistoryScreen(
                vehicleId = route.vehicleId.toIntOrNull() ?: 0,
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }
    }
}
