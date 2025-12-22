package com.indusjs.fleet.feature.vehicles.domain.entity

/**
 * Vehicle status enumeration.
 */
enum class VehicleStatus {
    ACTIVE,
    INACTIVE,
    IN_MAINTENANCE,
    OUT_OF_SERVICE
}

/**
 * Vehicle type enumeration.
 */
enum class VehicleType {
    TRUCK,
    VAN,
    CAR,
    BUS,
    MOTORCYCLE,
    TRAILER
}

/**
 * Location data class for vehicles.
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * Vehicle entity representing a fleet vehicle.
 */
data class Vehicle(
    val id: String,
    val registrationNumber: String,
    val make: String,
    val model: String,
    val year: Int,
    val type: VehicleType,
    val status: VehicleStatus,
    val fuelLevel: Int = 0,
    val mileage: Double = 0.0,
    val lastLocation: Location? = null,
    val assignedDriverId: String? = null,
    val assignedDriverName: String? = null,
    val lastServiceDate: Long? = null,
    val nextServiceDate: Long? = null
)

