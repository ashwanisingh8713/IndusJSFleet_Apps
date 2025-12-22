package com.indusjs.fleet.feature.drivers.domain.entity

/**
 * Driver status enumeration.
 */
enum class DriverStatus {
    AVAILABLE,
    ON_TRIP,
    OFF_DUTY,
    ON_BREAK,
    INACTIVE
}

/**
 * Location data class for drivers.
 */
data class DriverLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * Driver entity representing a fleet driver.
 */
data class Driver(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val licenseNumber: String,
    val status: DriverStatus,
    val rating: Double = 0.0,
    val totalTrips: Int = 0,
    val assignedVehicleId: String? = null,
    val assignedVehicleNumber: String? = null,
    val currentLocation: DriverLocation? = null,
    val hireDate: Long = 0L,
    val licenseExpiry: Long = 0L
) {
    val fullName: String get() = "$firstName $lastName"
}

