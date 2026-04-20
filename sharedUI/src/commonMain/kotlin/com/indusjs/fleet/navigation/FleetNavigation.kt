package com.indusjs.fleet.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import com.indusjs.fleet.FilePickerRequest
import com.indusjs.fleet.di.LocalViewModelProvider
import com.indusjs.fleet.di.SubscriptionGateResult
import com.ijs.subscription.presentation.plans.PlanPageMode
import com.indusjs.fleet.di.rememberViewModel
import com.indusjs.fleet.di.rememberSharedViewModel
import com.indusjs.fleet.di.clearSharedViewModel
import kotlinx.coroutines.launch
import com.ijs.vehicle.domain.entity.DocumentType
import com.ijs.alerts.presentation.AlertsFeatureFacade
import com.ijs.dashboard.presentation.DashboardFeatureFacade
import com.ijs.map.presentation.MapFeatureFacade
// Feature module facades
import com.ijs.user.presentation.UserFeatureFacade
import com.ijs.onboarding.presentation.OnboardingFeatureFacade
import com.ijs.vehicle.presentation.VehicleFeatureFacade
import com.ijs.driver.presentation.DriverFeatureFacade
import com.ijs.trip.presentation.TripFeatureFacade
import com.ijs.team.presentation.TeamFeatureFacade
import com.ijs.customer.presentation.CustomerFeatureFacade
import com.ijs.trip.payment.presentation.PaymentFeatureFacade
import com.ijs.reports.presentation.ReportsFeatureFacade
import com.ijs.finance.presentation.FinanceFeatureFacade
import com.ijs.subscription.domain.entity.BillingInterval
import com.ijs.subscription.domain.entity.FeatureLimit
import com.ijs.subscription.domain.entity.Plan
import com.ijs.subscription.presentation.SubscriptionFeatureFacade

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
        // ==================== Onboarding ====================

        is FleetRoute.Onboarding -> NavEntry(route) {
            val viewModel = rememberViewModel { onboardingViewModel() }
            OnboardingFeatureFacade.OnboardingEntry(
                viewModel = viewModel,
                onComplete = {
                    backStack.navigateAndClear(FleetRoute.Login)
                }
            )
        }

        // ==================== Auth ====================

        is FleetRoute.Login -> NavEntry(route) {
            val viewModel = rememberViewModel { loginViewModel() }
            val viewModelProvider = LocalViewModelProvider.current
            val scope = rememberCoroutineScope()
            UserFeatureFacade.LoginEntry(
                viewModel = viewModel,
                onLoginSuccess = {
                    scope.launch {
                        val gate = viewModelProvider.checkSubscriptionGate()
                        when (gate) {
                            SubscriptionGateResult.RequiresPlanSelection ->
                                backStack.navigateAndClear(FleetRoute.SubscriptionPlans(isPaymentPending = false))
                            SubscriptionGateResult.RequiresPayment ->
                                backStack.navigateAndClear(FleetRoute.SubscriptionPlans(isPaymentPending = true))
                            SubscriptionGateResult.RequiresTenantCreation ->
                                backStack.navigateAndClear(FleetRoute.CreateOrganization)
                            SubscriptionGateResult.NoGate ->
                                backStack.navigateAndClear(FleetRoute.Dashboard)
                        }
                    }
                },
                onNavigateToSignUp = { backStack.add(FleetRoute.SignUp) },
                onNavigateToForgotPassword = { backStack.add(FleetRoute.ForgotPassword) },
                onNavigateToOtpVerification = { email, mobile, needsEmail, needsMobile ->
                    backStack.add(FleetRoute.OtpVerification(
                        email = email, mobile = mobile,
                        needsEmailVerification = needsEmail, needsMobileVerification = needsMobile
                    ))
                }
            )
        }

        is FleetRoute.SignUp -> NavEntry(route) {
            val viewModel = rememberViewModel { signUpViewModel() }
            UserFeatureFacade.SignUpEntry(
                viewModel = viewModel,
                onNavigateToOtpVerification = { email, mobile, message, isResend ->
                    backStack.add(FleetRoute.OtpVerification(
                        email = email, mobile = mobile,
                        needsEmailVerification = true, needsMobileVerification = true
                    ))
                },
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.SignUpSuccess -> NavEntry(route) {
            UserFeatureFacade.SignUpSuccessEntry(
                message = route.message,
                isResend = route.isResend,
                onNavigateToLogin = {
                    backStack.navigateAndClear(FleetRoute.Login)
                }
            )
        }

        is FleetRoute.OtpVerification -> NavEntry(route) {
            val viewModel = rememberViewModel { otpVerificationViewModel() }
            UserFeatureFacade.OtpVerificationEntry(
                viewModel = viewModel,
                email = route.email,
                mobile = route.mobile,
                needsEmailVerification = route.needsEmailVerification,
                needsMobileVerification = route.needsMobileVerification,
                onVerificationComplete = {
                    backStack.navigateAndClear(FleetRoute.Login)
                },
                onNavigateToLogin = {
                    backStack.navigateAndClear(FleetRoute.Login)
                }
            )
        }

        is FleetRoute.ForgotPassword -> NavEntry(route) {
            val viewModel = rememberViewModel { forgotPasswordViewModel() }
            UserFeatureFacade.ForgotPasswordEntry(
                viewModel = viewModel,
                onNavigateToLogin = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Main ====================

        is FleetRoute.Dashboard -> NavEntry(route) {
            val viewModel = rememberViewModel { dashboardViewModel() }
            DashboardFeatureFacade.DashboardEntry(
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
                onNavigateToAddDriverCost = { backStack.add(FleetRoute.DriverCostEntry()) },
                onNavigateToAlertsList = { backStack.add(FleetRoute.AlertsList) },
                onNavigateToCustomers = { backStack.add(FleetRoute.Customers) },
                onNavigateToPayments = { backStack.add(FleetRoute.Payments) },
                onNavigateToVehicleFinance = { backStack.add(FleetRoute.VehicleFinance) }
            )
        }

        // ==================== User ====================

        is FleetRoute.Profile -> NavEntry(route) {
            val viewModel = rememberViewModel { profileViewModel() }
            UserFeatureFacade.ProfileEntry(
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
            UserFeatureFacade.ChangePasswordEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Vehicles ====================

        is FleetRoute.Vehicles -> NavEntry(route) {
            val viewModel = rememberViewModel { vehiclesViewModel() }
            VehicleFeatureFacade.VehiclesListEntry(
                viewModel = viewModel,
                onNavigateToDetail = { backStack.add(FleetRoute.VehicleDetail(it)) },
                onNavigateToAdd = { backStack.add(FleetRoute.AddVehicle) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.VehicleDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { vehicleDetailViewModel() }
            VehicleFeatureFacade.VehicleDetailEntry(
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
            val addVehicleKey = "add_vehicle"
            val viewModel = rememberSharedViewModel(addVehicleKey) { addVehicleViewModel() }
            VehicleFeatureFacade.AddVehicleEntry(
                viewModel = viewModel,
                onNavigateBack = {
                    clearSharedViewModel(addVehicleKey)
                    backStack.removeLastOrNull()
                },
                onVehicleRegistered = { vehicleId ->
                    clearSharedViewModel(addVehicleKey)
                    backStack.popAndNavigate(FleetRoute.VehicleDetail(vehicleId))
                },
                onNavigateToCreateTeamMember = {
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
            DriverFeatureFacade.DriversListEntry(
                viewModel = viewModel,
                onNavigateToDetail = { backStack.add(FleetRoute.DriverDetail(it)) },
                onNavigateToAdd = { backStack.add(FleetRoute.CreateDriver) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.DriverDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { driverDetailViewModel() }
            DriverFeatureFacade.DriverDetailEntry(
                viewModel = viewModel,
                driverId = route.driverId,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToAddDriverCost = { driverId ->
                    backStack.add(FleetRoute.DriverCostEntry(driverId = driverId))
                }
            )
        }

        is FleetRoute.CreateDriver -> NavEntry(route) {
            val viewModel = rememberViewModel { createDriverViewModel() }
            DriverFeatureFacade.CreateDriverEntry(
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
            TripFeatureFacade.TripsListEntry(
                viewModel = viewModel,
                onNavigateToDetail = { backStack.add(FleetRoute.TripDetail(it)) },
                onNavigateToCreate = { backStack.add(FleetRoute.CreateTrip) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TripDetail -> NavEntry(route) {
            val tripDetailKey = "trip_detail_${route.tripId}"
            val viewModel = rememberSharedViewModel(tripDetailKey) { tripDetailViewModel() }
            TripFeatureFacade.TripDetailEntry(
                viewModel = viewModel,
                tripId = route.tripId,
                onNavigateBack = {
                    clearSharedViewModel(tripDetailKey)
                    backStack.removeLastOrNull()
                },
                onNavigateToAddTripCost = { tripId, vehicleId ->
                    backStack.add(FleetRoute.TripCostEntry(tripId = tripId, vehicleId = vehicleId))
                },
                onNavigateToAddPayment = { tripId, vehicleId ->
                    backStack.add(FleetRoute.AddPayment(tripId = tripId, vehicleId = vehicleId))
                },
                onNavigateToAddCustomer = {
                    backStack.add(FleetRoute.CreateCustomer)
                }
            )
        }

        is FleetRoute.CreateTrip -> NavEntry(route) {
            val createTripKey = "create_trip"
            val viewModel = rememberSharedViewModel(createTripKey) { createTripViewModel() }
            TripFeatureFacade.CreateTripEntry(
                viewModel = viewModel,
                onNavigateBack = {
                    clearSharedViewModel(createTripKey)
                    backStack.removeLastOrNull()
                },
                onTripCreated = { tripId ->
                    clearSharedViewModel(createTripKey)
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
            TripFeatureFacade.TripCostEntryEntry(
                viewModel = viewModel,
                initialTripId = route.tripId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.MaintenanceCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { maintenanceCostEntryViewModel() }
            VehicleFeatureFacade.MaintenanceCostEntryEntry(
                viewModel = viewModel,
                initialVehicleId = route.vehicleId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.DriverCostEntry -> NavEntry(route) {
            val viewModel = rememberViewModel { driverCostEntryViewModel() }
            DriverFeatureFacade.DriverCostEntryEntry(
                viewModel = viewModel,
                initialDriverId = route.driverId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.AlertsList -> NavEntry(route) {
            val viewModel = rememberViewModel { alertsListViewModel() }
            AlertsFeatureFacade.AlertsListEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Other ====================

        is FleetRoute.Maps -> NavEntry(route) {
            val viewModel = rememberViewModel { mapsViewModel() }
            MapFeatureFacade.MapsEntry(
                viewModel = viewModel,
                onNavigateToVehicleDetail = { backStack.add(FleetRoute.VehicleDetail(it)) },
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TeamList -> NavEntry(route) {
            val viewModel = rememberViewModel { teamListViewModel() }
            TeamFeatureFacade.TeamListEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToCreateMember = { backStack.add(FleetRoute.CreateTeamMember()) },
                onNavigateToMemberDetail = { memberId -> backStack.add(FleetRoute.TeamMemberDetail(memberId)) }
            )
        }

        is FleetRoute.CreateTeamMember -> NavEntry(route) {
            val viewModel = rememberViewModel { createTeamMemberViewModel() }
            TeamFeatureFacade.CreateTeamMemberEntry(
                viewModel = viewModel,
                excludeGeneralManager = route.excludeGeneralManager,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TeamMemberDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { teamMemberDetailViewModel() }
            TeamFeatureFacade.TeamMemberDetailEntry(
                viewModel = viewModel,
                memberId = route.memberId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Reports ====================

        is FleetRoute.Reports -> NavEntry(route) {
            val viewModel = rememberViewModel { reportsViewModel() }
            ReportsFeatureFacade.ReportsHubEntry(
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
            ReportsFeatureFacade.VehicleProfitLossEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.TripProfitLoss -> NavEntry(route) {
            val viewModel = rememberViewModel { tripPLViewModel() }
            ReportsFeatureFacade.TripProfitLossEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CostAnalysis -> NavEntry(route) {
            val viewModel = rememberViewModel { costAnalysisViewModel() }
            ReportsFeatureFacade.CostAnalysisEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.ConsolidatedPL -> NavEntry(route) {
            val viewModel = rememberViewModel { consolidatedPLViewModel() }
            ReportsFeatureFacade.ConsolidatedPLEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Customers ====================

        is FleetRoute.Customers -> NavEntry(route) {
            val viewModel = rememberViewModel { customersListViewModel() }
            CustomerFeatureFacade.CustomersListEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToCustomerDetail = { customerId -> backStack.add(FleetRoute.CustomerDetail(customerId)) },
                onNavigateToCreateCustomer = { backStack.add(FleetRoute.CreateCustomer) }
            )
        }

        is FleetRoute.CustomerDetail -> NavEntry(route) {
            val viewModel = rememberViewModel { customerDetailViewModel() }
            CustomerFeatureFacade.CustomerDetailEntry(
                viewModel = viewModel,
                customerId = route.customerId,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.CreateCustomer -> NavEntry(route) {
            val viewModel = rememberViewModel { createCustomerViewModel() }
            CustomerFeatureFacade.CreateCustomerEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() },
                onNavigateToCustomerDetail = { customerId ->
                    backStack.popAndNavigate(FleetRoute.CustomerDetail(customerId))
                }
            )
        }

        // ==================== Payments ====================

        is FleetRoute.Payments -> NavEntry(route) {
            val viewModel = rememberViewModel { paymentsViewModel() }
            PaymentFeatureFacade.PaymentsListEntry(
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
            PaymentFeatureFacade.PaymentDetailEntry(
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
            PaymentFeatureFacade.AddPaymentEntry(
                viewModel = viewModel,
                tripId = route.tripId,
                paymentId = null,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.EditPayment -> NavEntry(route) {
            val viewModel = rememberViewModel { addPaymentViewModel() }
            PaymentFeatureFacade.AddPaymentEntry(
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
            FinanceFeatureFacade.VehicleFinanceListEntry(
                viewModel = viewModel,
                onNavigateBack = {
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
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            FinanceFeatureFacade.VehicleFinanceDetailEntry(
                viewModel = viewModel,
                vehicleId = route.vehicleId.toIntOrNull() ?: 0,
                onNavigateBack = {
                    backStack.removeLastOrNull()
                },
                onNavigateToEdit = { backStack.add(FleetRoute.EditPurchaseInfo(route.vehicleId)) },
                onNavigateToHistory = { backStack.add(FleetRoute.EmiPaymentHistory(route.vehicleId)) }
            )
        }

        is FleetRoute.AddPurchaseInfo -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            FinanceFeatureFacade.AddPurchaseInfoEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.EditPurchaseInfo -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            FinanceFeatureFacade.AddPurchaseInfoEntry(
                viewModel = viewModel,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.EmiPaymentHistory -> NavEntry(route) {
            val financeFlowKey = "vehicle_finance_flow"
            val viewModel = rememberSharedViewModel(financeFlowKey) { vehicleFinanceViewModel() }
            FinanceFeatureFacade.EmiPaymentHistoryEntry(
                viewModel = viewModel,
                vehicleId = route.vehicleId.toIntOrNull() ?: 0,
                onNavigateBack = { backStack.removeLastOrNull() }
            )
        }

        // ==================== Subscription / Billing ====================

        is FleetRoute.SubscriptionPlans -> NavEntry(route) {
            val mode = if (route.isPaymentPending) PlanPageMode.COMPLETE_PAYMENT else PlanPageMode.CHOOSE
            val viewModel = rememberViewModel { subscriptionPlansViewModel(mode) }
            SubscriptionFeatureFacade.PlansEntry(
                viewModel = viewModel,
                onNavigateToPayment = { plan, interval ->
                    backStack.add(
                        FleetRoute.SubscriptionCheckout(
                            planId = plan.id,
                            planName = plan.name,
                            monthlyPrice = plan.monthlyPrice,
                            annualPrice = plan.annualPrice,
                            discountPercent = plan.discountPercent,
                            effectiveMonthlyPriceAnnual = plan.effectiveMonthlyPriceAnnual,
                            annualSavings = plan.annualSavings,
                            currency = plan.currency,
                            trialDays = plan.trialDays,
                            billingInterval = interval.apiValue
                        )
                    )
                },
                onNavigateToDashboard = { backStack.navigateAndClear(FleetRoute.Dashboard) },
                onLogout = { backStack.navigateAndClear(FleetRoute.Login) }
            )
        }

        is FleetRoute.SubscriptionCheckout -> NavEntry(route) {
            val viewModel = rememberViewModel { subscriptionCheckoutViewModel() }
            val plan = Plan(
                id = route.planId,
                name = route.planName,
                description = "",
                monthlyPrice = route.monthlyPrice,
                annualPrice = route.annualPrice,
                discountPercent = route.discountPercent,
                effectiveMonthlyPriceAnnual = route.effectiveMonthlyPriceAnnual,
                annualSavings = route.annualSavings,
                currency = route.currency,
                trialDays = route.trialDays,
                features = emptyList(),
                featureLimits = emptyList<FeatureLimit>(),
                isActive = true
            )
            val interval = BillingInterval.from(route.billingInterval)
            SubscriptionFeatureFacade.PaymentCheckoutEntry(
                viewModel = viewModel,
                plan = plan,
                billingInterval = interval,
                razorpayLauncher = LocalRazorpayLauncher.current,
                onPaymentSuccess = { planName, amount, currency ->
                    backStack.navigateAndClear(
                        FleetRoute.SubscriptionSuccess(planName, amount, currency)
                    )
                },
                onNavigateBackToPlans = { backStack.removeLastOrNull() }
            )
        }

        is FleetRoute.SubscriptionSuccess -> NavEntry(route) {
            SubscriptionFeatureFacade.PaymentSuccessEntry(
                planName = route.planName,
                amount = route.amount,
                currency = route.currency,
                onContinue = {
                    backStack.navigateAndClear(FleetRoute.CreateOrganization)
                }
            )
        }

        is FleetRoute.CreateOrganization -> NavEntry(route) {
            val viewModel = rememberViewModel { createOrganizationViewModel() }
            SubscriptionFeatureFacade.CreateOrganizationEntry(
                viewModel = viewModel,
                onOrganizationCreated = {
                    backStack.navigateAndClear(FleetRoute.Dashboard)
                    backStack.add(FleetRoute.CreateTeamMember())
                },
                onSkipToTeamMember = {
                    backStack.navigateAndClear(FleetRoute.Dashboard)
                }
            )
        }
    }
}
