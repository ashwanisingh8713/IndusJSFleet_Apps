package com.indusjs.fleet.domain.entity.trip

import com.indusjs.fleet.core.constants.StatusConstants

/**
 * Trip status enumeration.
 * Matches API values: planned, on_route, completed, cancelled, failed, delayed
 *
 * Note: 'assigned' state has been removed. Trips now transition directly from planned → on_route.
 * Legacy 'assigned' values from API are mapped to PLANNED for backward compatibility.
 */
enum class TripStatus {
    PLANNED,
    ON_ROUTE,
    COMPLETED,
    CANCELLED,
    FAILED,
    DELAYED;

    companion object {
        /**
         * Convert API string to TripStatus enum.
         * Maps legacy 'assigned' to PLANNED for backward compatibility.
         */
        fun fromApiString(value: String): TripStatus = when (value.lowercase()) {
            StatusConstants.TripState.PLANNED, "assigned" -> PLANNED // 'assigned' is legacy, map to PLANNED
            StatusConstants.TripState.ON_ROUTE, "in_progress" -> ON_ROUTE
            StatusConstants.TripState.COMPLETED -> COMPLETED
            StatusConstants.TripState.CANCELLED -> CANCELLED
            StatusConstants.TripState.FAILED -> FAILED
            StatusConstants.TripState.DELAYED -> DELAYED
            else -> PLANNED
        }

        /**
         * Convert TripStatus enum to API string.
         */
        fun toApiString(status: TripStatus): String = when (status) {
            PLANNED -> StatusConstants.TripState.PLANNED
            ON_ROUTE -> StatusConstants.TripState.ON_ROUTE
            COMPLETED -> StatusConstants.TripState.COMPLETED
            CANCELLED -> StatusConstants.TripState.CANCELLED
            FAILED -> StatusConstants.TripState.FAILED
            DELAYED -> StatusConstants.TripState.DELAYED
        }

        /**
         * Get display label for status.
         */
        fun getDisplayLabel(status: TripStatus): String =
            StatusConstants.TripState.getDisplayLabel(toApiString(status))

        /**
         * Get icon for status.
         */
        fun getIcon(status: TripStatus): String =
            StatusConstants.TripState.getIcon(toApiString(status))

        /**
         * Get color scheme for status.
         */
        fun getColorScheme(status: TripStatus): StatusConstants.StateColorScheme =
            StatusConstants.TripState.getColorScheme(toApiString(status))

        /**
         * Check if trip is in progress.
         */
        fun isInProgress(status: TripStatus): Boolean =
            StatusConstants.TripState.isInProgress(toApiString(status))

        /**
         * Check if trip is editable.
         */
        fun isEditable(status: TripStatus): Boolean =
            StatusConstants.TripState.isEditable(toApiString(status))

        /**
         * Check if trip can be cancelled.
         */
        fun isCancellable(status: TripStatus): Boolean =
            StatusConstants.TripState.isCancellable(toApiString(status))

        /**
         * Check if trip is finished.
         */
        fun isFinished(status: TripStatus): Boolean =
            StatusConstants.TripState.isFinished(toApiString(status))

        /**
         * Get valid transitions from current status.
         */
        fun getValidTransitions(status: TripStatus): List<TripStatus> {
            return StatusConstants.TripTransitions.getValidTransitions(toApiString(status))
                .mapNotNull { state ->
                    try { fromApiString(state) } catch (e: Exception) { null }
                }
        }

        /**
         * Check if transition is valid.
         */
        fun canTransition(from: TripStatus, to: TripStatus): Boolean {
            return StatusConstants.TripTransitions.canTransition(toApiString(from), toApiString(to))
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
    // Schedule - Departure
    val scheduledStartTime: String? = null,
    val plannedStart: String? = null,  // ISO 8601 format
    val scheduledDate: String? = null,  // DD-MM-YYYY format
    val startTime: String? = null,      // HH:MM format
    // Schedule - Arrival
    val plannedEnd: String? = null,     // ISO 8601 format
    val deliveryDate: String? = null,   // DD-MM-YYYY format
    val deliveryTime: String? = null,   // HH:MM format
    // Actual times
    val actualStartTime: String? = null,
    val actualEndTime: String? = null,
    val cargoType: String? = null,
    val cargoDescription: String? = null,
    val cargoLoadingWeight: Double? = null,
    val weightUnit: String? = null,
    val customerName: String? = null,
    val customerContact: String? = null,
    val priority: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    // Pricing
    val tripPrice: Double? = null,
    // Cost summary - null means not loaded, 0.0 means no costs
    val totalCost: Double? = null,
    // State-based display info for UI
    val displayInfo: TripDisplayInfo = TripDisplayInfo()
)

/**
 * Data class for creating a new trip.
 * Updated to match API v2 fields.
 * Note: v2 API requires plannedStart and plannedEnd in ISO 8601 format.
 */
data class CreateTripData(
    val vehicleId: Int,
    val driverId: Int,
    // v2 API requires planned_start and planned_end (ISO 8601 format)
    val plannedStart: String,
    val plannedEnd: String,
    // Legacy fields (optional, for backward compatibility)
    val scheduledDate: String? = null,
    val startTime: String? = null,
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
    val tripPrice: Double? = null,
    // Payment
    val paymentStatus: String? = null,
    val pendingAmount: Double? = null,
    val paymentMode: String? = null,
    // Customer - New API supports customer_id for Customer entity association
    val customerId: Int? = null,
    // Customer - Legacy fields (fallback when customerId not provided)
    val customerName: String? = null,
    val customerContact: String? = null,
    val priority: String? = null,
    val notes: String? = null
)

/**
 * Trip Stop entity representing a waypoint/stop in a trip.
 */
data class TripStop(
    val id: String,
    val tripId: String,
    val stopOrder: Int,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val arrivalTime: String? = null,
    val departureTime: String? = null,
    val stopDuration: Int? = null,
    val notes: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Data class for creating a new trip stop.
 */
data class CreateTripStopData(
    val stopOrder: Int,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val arrivalTime: String? = null,
    val stopDuration: Int? = null,
    val notes: String? = null
)

/**
 * Data class for updating a trip stop.
 */
data class UpdateTripStopData(
    val stopOrder: Int? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val arrivalTime: String? = null,
    val stopDuration: Int? = null,
    val notes: String? = null
)
