package com.indusjs.fleet.domain.entity.trip

/**
 * Trip status enumeration.
 * Matches API values: planned, in_progress, completed, cancelled
 */
enum class TripStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    companion object {
        fun fromApiString(value: String): TripStatus = when (value.lowercase()) {
            "planned" -> PLANNED
            "in_progress" -> IN_PROGRESS
            "completed" -> COMPLETED
            "cancelled" -> CANCELLED
            else -> PLANNED
        }

        fun toApiString(status: TripStatus): String = when (status) {
            PLANNED -> "planned"
            IN_PROGRESS -> "in_progress"
            COMPLETED -> "completed"
            CANCELLED -> "cancelled"
        }
    }
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
    val tripNumber: String?,
    val vehicleId: String,
    val vehicleNumber: String?,
    val driverId: String,
    val driverName: String?,
    val status: TripStatus,
    val startLocation: TripLocation?,
    val endLocation: TripLocation?,
    val currentLocation: TripLocation? = null,
    val distance: Double = 0.0,
    val estimatedDuration: Long = 0L,
    val actualDuration: Long? = null,
    val scheduledStartTime: String? = null,
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,
    val cargoType: String? = null,
    val cargoDescription: String? = null,
    val customerName: String? = null,
    val priority: String? = null,
    val notes: String? = null,
    val createdAt: String? = null
)

/**
 * Data class for creating a new trip.
 */
data class CreateTripData(
    val vehicleId: Int,
    val driverId: Int,
    val scheduledDate: String,
    val startTime: String,
    val plannedStart: String,
    val plannedEnd: String,
    val startLocation: String,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLocation: String,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val cargoType: String,
    val cargoDescription: String? = null,
    val cargoWeight: Double? = null,
    val customerName: String? = null,
    val customerContact: String? = null,
    val priority: String? = null,
    val notes: String? = null
)

