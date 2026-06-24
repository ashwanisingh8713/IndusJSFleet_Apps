package com.ijs.driver.domain.entity

import com.indusjs.fleet.core.constants.StatusConstants

/**
 * Driver status enumeration.
 * Matches backend status oneof: inactive, active, on_trip, on_leave, suspended
 * (PATCH /drivers/:id/status). Legacy wire value "on_route" is tolerated on read
 * and mapped to ON_TRIP; it is never sent back to the backend.
 */
enum class DriverStatus {
    INACTIVE,
    ACTIVE,
    ON_TRIP,
    ON_LEAVE,
    SUSPENDED;

    companion object {
        /**
         * Convert API string to DriverStatus enum.
         * Accepts legacy "on_route" and maps it to ON_TRIP.
         */
        fun fromApiString(value: String): DriverStatus = when (value.lowercase()) {
            StatusConstants.DriverState.INACTIVE -> INACTIVE
            StatusConstants.DriverState.ACTIVE -> ACTIVE
            StatusConstants.DriverState.ON_TRIP, "on_route" -> ON_TRIP
            StatusConstants.DriverState.ON_LEAVE -> ON_LEAVE
            StatusConstants.DriverState.SUSPENDED -> SUSPENDED
            else -> ACTIVE
        }

        /**
         * Convert DriverStatus enum to API string (backend-accepted values only).
         */
        fun toApiString(status: DriverStatus): String = when (status) {
            INACTIVE -> StatusConstants.DriverState.INACTIVE
            ACTIVE -> StatusConstants.DriverState.ACTIVE
            ON_TRIP -> StatusConstants.DriverState.ON_TRIP
            ON_LEAVE -> StatusConstants.DriverState.ON_LEAVE
            SUSPENDED -> StatusConstants.DriverState.SUSPENDED
        }

        /**
         * Get display label for status.
         */
        fun getDisplayLabel(status: DriverStatus): String =
            StatusConstants.DriverState.getDisplayLabel(toApiString(status))

        /**
         * Get icon for status.
         */
        fun getIcon(status: DriverStatus): String =
            StatusConstants.DriverState.getIcon(toApiString(status))

        /**
         * Get color scheme for status.
         */
        fun getColorScheme(status: DriverStatus): StatusConstants.StateColorScheme =
            StatusConstants.DriverState.getColorScheme(toApiString(status))

        /**
         * Check if driver is available for assignment.
         */
        fun isAvailableForAssignment(status: DriverStatus): Boolean =
            StatusConstants.DriverState.isAvailableForAssignment(toApiString(status))

        /**
         * Get valid transitions from current status.
         */
        fun getValidTransitions(status: DriverStatus): List<DriverStatus> {
            return StatusConstants.DriverTransitions.getValidTransitions(toApiString(status))
                .mapNotNull { state ->
                    try { fromApiString(state) } catch (e: Exception) { null }
                }
        }

        /**
         * Check if transition is valid.
         */
        fun canTransition(from: DriverStatus, to: DriverStatus): Boolean {
            return StatusConstants.DriverTransitions.canTransition(toApiString(from), toApiString(to))
        }
    }
}

/**
 * License type enumeration.
 */
enum class LicenseType {
    LMV,    // Light Motor Vehicle
    HMV,    // Heavy Motor Vehicle
    MCWG,   // Motorcycle with Gear
    MCWOG;  // Motorcycle without Gear

    companion object {
        fun fromApiString(value: String): LicenseType = when (value.uppercase()) {
            "LMV" -> LMV
            "HMV" -> HMV
            "MCWG" -> MCWG
            "MCWOG" -> MCWOG
            else -> LMV
        }

        fun toApiString(type: LicenseType): String = type.name
    }
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
 * Owner info for driver.
 */
data class DriverOwner(
    val id: String,
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val role: String?
)

/**
 * Trip assignment info when driver is occupied.
 */
data class DriverTripAssignment(
    val tripId: String,
    val tripState: String,
    val scheduledDate: Long?,
    val startTime: Long?,
    val plannedStart: Long?,
    val plannedEnd: Long?,
    val startLocation: String?,
    val endLocation: String?,
    val customerName: String?
)

/**
 * Driver entity representing a fleet driver.
 * Aligned with Fleet Management API.
 */
data class Driver(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val mobile: String,
    val licenseNumber: String,
    val licenseExpiry: Long = 0L,
    val licenseType: LicenseType = LicenseType.LMV,
    val dateOfBirth: Long? = null,
    val address: String? = null,
    val emergencyContact: String? = null,
    val bloodGroup: String? = null,
    val profilePhoto: String? = null,
    val status: DriverStatus = DriverStatus.ACTIVE,
    val isActive: Boolean = true,
    val isOccupied: Boolean = false,
    val tripAssignment: DriverTripAssignment? = null,
    val ownerId: String? = null,
    val owner: DriverOwner? = null,
    val createdById: String? = null,
    val createdBy: DriverOwner? = null,
    val joiningDate: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    // Legacy fields for backward compatibility
    val rating: Double = 0.0,
    val totalTrips: Int = 0,
    val assignedVehicleId: String? = null,
    val assignedVehicleNumber: String? = null,
    val currentLocation: DriverLocation? = null
) {
    val fullName: String get() = "$firstName $lastName"

    // Alias for backward compatibility
    val phone: String get() = mobile
}

