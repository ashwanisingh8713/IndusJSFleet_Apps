package com.indusjs.fleet.domain.entity.driver

/**
 * Driver status enumeration.
 * Matches API values: active, inactive, on_trip, on_leave, suspended
 */
enum class DriverStatus {
    ACTIVE,
    INACTIVE,
    ON_TRIP,
    ON_LEAVE,
    SUSPENDED;

    companion object {
        fun fromApiString(value: String): DriverStatus = when (value.lowercase()) {
            "active" -> ACTIVE
            "inactive" -> INACTIVE
            "on_trip" -> ON_TRIP
            "on_leave" -> ON_LEAVE
            "suspended" -> SUSPENDED
            else -> ACTIVE
        }

        fun toApiString(status: DriverStatus): String = when (status) {
            ACTIVE -> "active"
            INACTIVE -> "inactive"
            ON_TRIP -> "on_trip"
            ON_LEAVE -> "on_leave"
            SUSPENDED -> "suspended"
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

