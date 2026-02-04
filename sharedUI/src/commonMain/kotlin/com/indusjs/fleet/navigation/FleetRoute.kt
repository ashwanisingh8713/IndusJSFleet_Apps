package com.indusjs.fleet.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Fleet Management app.
 *
 * Uses Navigation 3 with:
 * - @Serializable for type-safe navigation
 * - NavKey for back stack management
 * - Automatic system back button/gesture handling
 */
@Serializable
sealed interface FleetRoute : NavKey {

    // ==================== Auth Routes ====================

    @Serializable data object Login : FleetRoute
    @Serializable data object SignUp : FleetRoute
    @Serializable data object ForgotPassword : FleetRoute

    // ==================== Main Routes ====================

    @Serializable data object Dashboard : FleetRoute

    // ==================== User Routes ====================

    @Serializable data object Profile : FleetRoute
    @Serializable data object ChangePassword : FleetRoute

    // ==================== Vehicle Routes ====================

    @Serializable data object Vehicles : FleetRoute
    @Serializable data class VehicleDetail(val vehicleId: String) : FleetRoute
    @Serializable data object AddVehicle : FleetRoute

    // ==================== Driver Routes ====================

    @Serializable data object Drivers : FleetRoute
    @Serializable data class DriverDetail(val driverId: String) : FleetRoute
    @Serializable data object CreateDriver : FleetRoute

    // ==================== Trip Routes ====================

    @Serializable data object Trips : FleetRoute
    @Serializable data class TripDetail(val tripId: String) : FleetRoute
    @Serializable data object CreateTrip : FleetRoute

    // ==================== Cost Entry Routes ====================

    @Serializable data class TripCostEntry(val tripId: String? = null, val vehicleId: String? = null) : FleetRoute
    @Serializable data class MaintenanceCostEntry(val vehicleId: String? = null) : FleetRoute
    @Serializable data object DriverCostEntry : FleetRoute

    // ==================== Alerts Routes ====================

    @Serializable data object AlertsList : FleetRoute

    // ==================== Other Routes ====================

    @Serializable data object Maps : FleetRoute
    @Serializable data object TeamList : FleetRoute
    @Serializable data class CreateTeamMember(val excludeGeneralManager: Boolean = false) : FleetRoute
    @Serializable data class TeamMemberDetail(val memberId: String) : FleetRoute

    // ==================== Reports Routes ====================

    @Serializable data object Reports : FleetRoute
    @Serializable data object VehicleProfitLoss : FleetRoute
    @Serializable data object TripProfitLoss : FleetRoute
    @Serializable data object CostAnalysis : FleetRoute
    @Serializable data object ConsolidatedPL : FleetRoute

    // ==================== Customer Routes ====================

    @Serializable data object Customers : FleetRoute
    @Serializable data class CustomerDetail(val customerId: String) : FleetRoute
    @Serializable data object CreateCustomer : FleetRoute

    // ==================== Payment Routes ====================

    @Serializable data object Payments : FleetRoute
    @Serializable data class PaymentDetail(val paymentId: String) : FleetRoute
    @Serializable data class AddPayment(val tripId: String? = null, val vehicleId: String? = null) : FleetRoute
    @Serializable data class EditPayment(val paymentId: String) : FleetRoute

    // ==================== Vehicle Finance Routes ====================

    @Serializable data object VehicleFinance : FleetRoute
    @Serializable data object AddPurchaseInfo : FleetRoute
    @Serializable data class VehicleFinanceDetail(val vehicleId: String) : FleetRoute
    @Serializable data class EditPurchaseInfo(val vehicleId: String) : FleetRoute
    @Serializable data class EmiPaymentHistory(val vehicleId: String) : FleetRoute
}

