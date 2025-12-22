package com.indusjs.fleet.feature.trips.domain.entity

/**
 * Trip status enumeration.
 */
enum class TripStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    DELAYED
}

/**
 * Location data class for trips.
 */
data class TripLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

/**
 * Trip entity representing a fleet trip/journey.
 */
data class Trip(
    val id: String,
    val tripNumber: String,
    val vehicleId: String,
    val vehicleNumber: String,
    val driverId: String,
    val driverName: String,
    val status: TripStatus,
    val startLocation: TripLocation,
    val endLocation: TripLocation,
    val currentLocation: TripLocation? = null,
    val distance: Double = 0.0,
    val estimatedDuration: Long = 0L,
    val actualDuration: Long? = null,
    val scheduledStartTime: Long = 0L,
    val actualStartTime: Long? = null,
    val actualEndTime: Long? = null,
    val cargo: String? = null,
    val notes: String? = null
)

