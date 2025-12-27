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

    // ==================== Other Routes ====================

    @Serializable data object Maps : FleetRoute
    @Serializable data object TeamList : FleetRoute
    @Serializable data object CreateTeamMember : FleetRoute
}

