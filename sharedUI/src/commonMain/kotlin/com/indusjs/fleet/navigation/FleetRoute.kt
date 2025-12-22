package com.indusjs.fleet.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Fleet Management app.
 */
sealed interface FleetRoute {

    @Serializable
    data object Login : FleetRoute

    @Serializable
    data object Dashboard : FleetRoute

    @Serializable
    data object Vehicles : FleetRoute

    @Serializable
    data class VehicleDetail(val vehicleId: String) : FleetRoute

    @Serializable
    data object AddVehicle : FleetRoute

    @Serializable
    data object Drivers : FleetRoute

    @Serializable
    data class DriverDetail(val driverId: String) : FleetRoute

    @Serializable
    data object AddDriver : FleetRoute

    @Serializable
    data object Trips : FleetRoute

    @Serializable
    data class TripDetail(val tripId: String) : FleetRoute

    @Serializable
    data object CreateTrip : FleetRoute

    @Serializable
    data object Maps : FleetRoute
}

/**
 * Helper function to get the route name for logging/analytics.
 */
fun FleetRoute.routeName(): String = when (this) {
    is FleetRoute.Login -> "login"
    is FleetRoute.Dashboard -> "dashboard"
    is FleetRoute.Vehicles -> "vehicles"
    is FleetRoute.VehicleDetail -> "vehicle_detail"
    is FleetRoute.AddVehicle -> "add_vehicle"
    is FleetRoute.Drivers -> "drivers"
    is FleetRoute.DriverDetail -> "driver_detail"
    is FleetRoute.AddDriver -> "add_driver"
    is FleetRoute.Trips -> "trips"
    is FleetRoute.TripDetail -> "trip_detail"
    is FleetRoute.CreateTrip -> "create_trip"
    is FleetRoute.Maps -> "maps"
}

