package com.ijs.trip.domain.entity

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
    // Schedule - Departure (all UTC epoch-millis; 0/null = unset)
    val scheduledStartTime: Long? = null,
    val plannedStart: Long? = null,
    val scheduledDate: Long? = null,
    val startTime: Long? = null,
    // Schedule - Arrival (UTC epoch-millis)
    val plannedEnd: Long? = null,
    val deliveryDate: Long? = null,
    val deliveryTime: Long? = null,
    // Actual times (UTC epoch-millis)
    val actualStartTime: Long? = null,
    val actualEndTime: Long? = null,
    val cargoType: String? = null,
    val cargoDescription: String? = null,
    val cargoLoadingWeight: Double? = null,
    val weightUnit: String? = null,
    // Customer info
    val customerId: String? = null,
    val customerName: String? = null,
    val customerContact: String? = null,
    val priority: String? = null,
    val notes: String? = null,
    val createdAt: Long? = null,  // UTC epoch-millis
    // Pricing
    val tripPrice: Double? = null,
    // purchase_price (COGS) and selling_value (ACTUAL revenue) round-tripped from the API.
    val purchasePrice: Double? = null,
    val sellingValue: Double? = null,
    val paidTripPrice: Double? = null,
    // Server-derived outstanding balance (selling_value − paid); authoritative, prefer
    // over computing quote − paid (quote can differ from the actual selling_value).
    val pendingAmount: Double? = null,
    val paymentStatus: String? = null,
    // Cost summary - null means not loaded, 0.0 means no costs
    val totalCost: Double? = null,
    // State-based display info for UI
    val displayInfo: TripDisplayInfo = TripDisplayInfo()
)

/**
 * Data class for creating a new trip.
 * Updated to match API v2 fields.
 * Note: schedule timestamps are UTC epoch-millis.
 */
data class CreateTripData(
    val vehicleId: Int,
    val driverId: Int,
    // v2 API requires planned_start and planned_end (UTC epoch-millis)
    val plannedStart: Long,
    val plannedEnd: Long,
    // Legacy fields (optional) - UTC epoch-millis
    val scheduledDate: Long? = null,
    val startTime: Long? = null,
    val deliveryDate: Long? = null,
    val deliveryTime: Long? = null,
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
    // Actual price (revenue) the customer owes; defaults to tripPrice (the quote)
    // when not explicitly overridden. Sent as selling_value to the backend.
    val sellingValue: Double? = null,
    val estimatedExpense: Double? = null,
    val tripPrice: Double? = null,
    // NOTE: payment_status / pending_amount / payment_mode are derived server-side
    // from payment records and are intentionally NOT part of the create payload.
    // Customer - New API supports customer_id for Customer entity association
    val customerId: Int? = null,
    // Customer - Legacy fields (fallback when customerId not provided)
    val customerName: String? = null,
    val customerContact: String? = null,
    // Consignee / delivery (receiver) details — REQUIRED by the backend on create.
    // Distinct from the billing customer, though they usually start the same.
    val deliveryAddress: String? = null,
    val deliveryPersonName: String? = null,
    val deliveryContactNumber: String? = null,
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
    // UTC epoch-millis
    val arrivalTime: Long? = null,
    val departureTime: Long? = null,
    val stopDuration: Int? = null,
    val notes: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

/**
 * Data class for creating a new trip stop.
 */
data class CreateTripStopData(
    val stopOrder: Int,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val arrivalTime: Long? = null,  // UTC epoch-millis
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
    val arrivalTime: Long? = null,  // UTC epoch-millis
    val stopDuration: Int? = null,
    val notes: String? = null
)
