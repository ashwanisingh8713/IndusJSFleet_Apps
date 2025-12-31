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
 * State-based display info for Trip card.
 * Contains pre-computed display values based on trip state.
 */
data class TripDisplayInfo(
    // Distance display - state-appropriate value
    val distanceValue: String = "NA",
    val distanceLabel: String = "Distance",
    val estimatedDistance: Double? = null,
    val coveredDistance: Double? = null,
    val totalDistance: Double? = null,

    // Duration display - state-appropriate value
    val durationValue: String = "NA",
    val durationLabel: String = "Duration",
    val plannedDurationMinutes: Long? = null,
    val actualDurationMinutes: Long? = null,

    // Cargo info
    val cargoTypeLabel: String? = null,

    // Cost info
    val hasCosts: Boolean = false,
    val totalCost: Double = 0.0,
    val totalCostLabel: String = "NA",
    val costCount: Int = 0,

    // Progress info (In Progress only)
    val progressPercent: Int? = null,
    val remainingDistance: Double? = null,
    val estimatedArrival: String? = null
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
    val createdAt: String? = null,
    // Cost summary - null means not loaded, 0.0 means no costs
    val totalCost: Double? = null,
    // State-based display info for UI
    val displayInfo: TripDisplayInfo = TripDisplayInfo()
)

/**
 * Data class for creating a new trip.
 * Updated to match API v2 fields.
 */
data class CreateTripData(
    val vehicleId: Int,
    val driverId: Int,
    val scheduledDate: String,
    val startTime: String,
    val deliveryDate: String? = null,
    val deliveryTime: String? = null,
    val startLocation: String,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLocation: String,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val estimatedDistance: Double? = null,
    val cargoType: String,
    val cargoDescription: String? = null,
    val cargoLoadingWeight: Double? = null,
    val cargoUnloadingWeight: Double? = null,
    val vehicleWeight: Double? = null,
    val weightUnit: String? = null,
    // Fuel info
    val fuelType: String? = null,
    val filledFuelQuantity: Double? = null,
    val usedFuelQuantity: Double? = null,
    val fuelRate: Double? = null,
    val kmPerLiter: Double? = null,
    // Pricing
    val purchasePrice: Double? = null,
    val sellingValue: Double? = null,
    val estimatedExpense: Double? = null,
    // Payment
    val paymentStatus: String? = null,
    val pendingAmount: Double? = null,
    val paymentMode: String? = null,
    // Customer
    val customerName: String? = null,
    val customerContact: String? = null,
    val priority: String? = null,
    val notes: String? = null
)

