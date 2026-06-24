package com.ijs.driver.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Driver DTO for API communication.
 * Uses snake_case field names to match API response format.
 */
@Serializable
data class DriverDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    @SerialName("email")
    val email: String? = null,
    @SerialName("mobile")
    val mobile: String = "",
    @SerialName("license_number")
    val licenseNumber: String = "",
    @SerialName("license_expiry")
    val licenseExpiry: Long? = null,
    @SerialName("license_type")
    val licenseType: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: Long? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("emergency_contact")
    val emergencyContact: String? = null,
    @SerialName("blood_group")
    val bloodGroup: String? = null,
    @SerialName("profile_photo")
    val profilePhoto: String? = null,
    @SerialName("status")
    val status: String = "active",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("is_occupied")
    val isOccupied: Boolean = false,
    @SerialName("trip_assignment")
    val tripAssignment: TripAssignmentDto? = null,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("owner")
    val owner: DriverOwnerDto? = null,
    @SerialName("created_by_id")
    val createdById: Int? = null,
    @SerialName("created_by")
    val createdBy: DriverOwnerDto? = null,
    @SerialName("joining_date")
    val joiningDate: Long? = null,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null
)

/**
 * Trip assignment details when driver/vehicle is occupied.
 */
@Serializable
data class TripAssignmentDto(
    @SerialName("trip_id")
    val tripId: Int = 0,
    @SerialName("trip_state")
    val tripState: String = "",
    @SerialName("scheduled_date")
    val scheduledDate: Long? = null,
    @SerialName("start_time")
    val startTime: Long? = null,
    @SerialName("planned_start")
    val plannedStart: Long? = null,
    @SerialName("planned_end")
    val plannedEnd: Long? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("customer_name")
    val customerName: String? = null
)

/**
 * Owner/User DTO for nested objects in driver response.
 */
@Serializable
data class DriverOwnerDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("email")
    val email: String? = null,
    @SerialName("mobile")
    val mobile: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null
)

/**
 * API response wrapper for driver operations.
 */
@Serializable
data class DriverApiResponse<T>(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: T? = null,
    @SerialName("pagination")
    val pagination: DriverPaginationDto? = null
)

/**
 * Paginated driver list payload returned by GET /drivers.
 */
@Serializable
data class DriverListDataDto(
    @SerialName("items")
    val items: List<DriverDto> = emptyList(),
    @SerialName("count")
    val count: Int = 0,
    @SerialName("has_more")
    val hasMore: Boolean = false,
    @SerialName("next_page")
    val nextPage: Int? = null,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 50,
    @SerialName("total_pages")
    val totalPages: Int = 1
) {
    fun toPagination(): DriverPaginationDto = DriverPaginationDto(
        page = page,
        perPage = perPage,
        total = count,
        totalPages = totalPages
    )
}

/**
 * Pagination info for driver list responses.
 */
@Serializable
data class DriverPaginationDto(
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 10,
    @SerialName("total")
    val total: Int = 0,
    @SerialName("total_pages")
    val totalPages: Int = 0
)

/**
 * Request body for creating a driver.
 */
@Serializable
data class CreateDriverRequest(
    @SerialName("password")
    val password: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("email")
    val email: String? = null,
    @SerialName("mobile")
    val mobile: String,
    @SerialName("license_number")
    val licenseNumber: String,
    @SerialName("license_expiry")
    val licenseExpiry: Long? = null,
    @SerialName("license_type")
    val licenseType: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: Long? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("emergency_contact")
    val emergencyContact: String? = null,
    @SerialName("blood_group")
    val bloodGroup: String? = null,
    @SerialName("joining_date")
    val joiningDate: Long? = null,
    @SerialName("caretaker_id")
    val caretakerId: Int? = null
)

/**
 * Request body for updating a driver.
 */
@Serializable
data class UpdateDriverRequest(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("mobile")
    val mobile: String? = null,
    @SerialName("license_number")
    val licenseNumber: String? = null,
    @SerialName("license_expiry")
    val licenseExpiry: Long? = null,
    @SerialName("license_type")
    val licenseType: String? = null,
    @SerialName("date_of_birth")
    val dateOfBirth: Long? = null,
    @SerialName("address")
    val address: String? = null,
    @SerialName("emergency_contact")
    val emergencyContact: String? = null,
    @SerialName("blood_group")
    val bloodGroup: String? = null,
    @SerialName("joining_date")
    val joiningDate: Long? = null,
    @SerialName("caretaker_id")
    val caretakerId: Int? = null
)

/**
 * Request body for updating driver status.
 */
@Serializable
data class UpdateDriverStatusRequest(
    // Backend oneof: active|inactive|on_trip|on_leave|suspended
    @SerialName("status")
    val status: String = "active"
)

