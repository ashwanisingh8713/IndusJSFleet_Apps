package com.indusjs.fleet.data.model.caretaker

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * Caretaker DTOs for managing caretaker assignments.
 * Caretakers are team members (managers/supervisors) assigned to oversee vehicles/drivers.
 */

// ==================== Request DTOs ====================

/**
 * Request to assign a caretaker to a vehicle or driver.
 */
@Serializable
data class AssignCaretakerRequest(
    @SerialName("caretaker_id")
    val caretakerId: Int
)

/**
 * Request for bulk caretaker assignment.
 */
@Serializable
data class BulkAssignCaretakerRequest(
    @SerialName("caretaker_id")
    val caretakerId: Int,
    @SerialName("vehicle_ids")
    val vehicleIds: List<Int>? = null,
    @SerialName("driver_ids")
    val driverIds: List<Int>? = null
)

/**
 * Request to reassign caretaker in bulk.
 */
@Serializable
data class ReassignCaretakerRequest(
    @SerialName("from_caretaker_id")
    val fromCaretakerId: Int,
    @SerialName("to_caretaker_id")
    val toCaretakerId: Int
)

// ==================== Response DTOs ====================

/**
 * Caretaker information DTO.
 * Used when a caretaker is assigned to a vehicle/driver.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CaretakerDto(
    val id: Int,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val role: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }

    val displayName: String
        get() = if (fullName.isNotBlank() && fullName != "Unknown") {
            "$fullName (${role?.replaceFirstChar { it.uppercase() } ?: "Team"})"
        } else {
            "Not Assigned"
        }
}

/**
 * API response for caretaker operations.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CaretakerApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CaretakerAssignmentDto? = null
)

/**
 * Caretaker assignment details.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CaretakerAssignmentDto(
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("driver_id")
    val driverId: Int? = null,
    @SerialName("caretaker_id")
    val caretakerId: Int? = null,
    val caretaker: CaretakerDto? = null
)

/**
 * Caretaker assignments list API response.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CaretakerAssignmentsApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: CaretakerAssignmentsDataDto? = null
)

/**
 * Caretaker assignments data.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CaretakerAssignmentsDataDto(
    val caretaker: CaretakerDto? = null,
    val vehicles: List<AssignedVehicleDto>? = null,
    val drivers: List<AssignedDriverDto>? = null,
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("total_drivers")
    val totalDrivers: Int = 0
)

/**
 * Assigned vehicle summary.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class AssignedVehicleDto(
    val id: Int,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    val make: String? = null,
    val model: String? = null,
    val state: String? = null
)

/**
 * Assigned driver summary.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class AssignedDriverDto(
    val id: Int,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val mobile: String? = null,
    val status: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }
}

/**
 * Orphaned assignments API response.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class OrphanedAssignmentsApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: OrphanedAssignmentsDataDto? = null
)

/**
 * Orphaned assignments data.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class OrphanedAssignmentsDataDto(
    @SerialName("vehicles_without_caretaker")
    val vehiclesWithoutCaretaker: List<AssignedVehicleDto>? = null,
    @SerialName("drivers_without_caretaker")
    val driversWithoutCaretaker: List<AssignedDriverDto>? = null
)

/**
 * Simple caretaker operation response.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class CaretakerSimpleApiResponse(
    val success: Boolean = false,
    val message: String? = null
)

