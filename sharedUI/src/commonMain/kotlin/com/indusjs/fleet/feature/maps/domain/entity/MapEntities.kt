package com.indusjs.fleet.feature.maps.domain.entity

/**
 * Vehicle status on the map.
 */
enum class MapVehicleStatus {
    MOVING,
    IDLE,
    STOPPED,
    OFFLINE
}

/**
 * Location data class for maps.
 */
data class MapLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * Vehicle representation on the map.
 */
data class MapVehicle(
    val id: String,
    val vehicleNumber: String,
    val driverName: String? = null,
    val location: MapLocation,
    val status: MapVehicleStatus,
    val speed: Double = 0.0,
    val heading: Double = 0.0,
    val lastUpdated: Long = 0L
)

/**
 * Geofence type enumeration.
 */
enum class GeofenceType {
    DEPOT,
    CUSTOMER,
    RESTRICTED,
    DELIVERY_ZONE
}

/**
 * Geofence entity for map zones.
 */
data class Geofence(
    val id: String,
    val name: String,
    val type: GeofenceType,
    val center: MapLocation,
    val radius: Double,
    val isActive: Boolean = true
)

