package com.indusjs.fleet.data.model.vehicle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vehicle DTO for API communication.
 * Uses snake_case field names to match API response format.
 */
@Serializable
data class VehicleDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("registration_number")
    val registrationNumber: String = "",
    @SerialName("make")
    val make: String = "",
    @SerialName("model")
    val model: String = "",
    @SerialName("year")
    val year: Int = 0,
    @SerialName("vehicle_type")
    val type: String = "",
    @SerialName("state")
    val status: String = "active",
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("fuel_level")
    val fuelLevel: Int = 0,
    @SerialName("mileage")
    val mileage: Double = 0.0,
    @SerialName("capacity")
    val capacity: Int? = null,
    @SerialName("color")
    val color: String? = null,
    @SerialName("last_location")
    val lastLocation: LocationDto? = null,
    @SerialName("assigned_driver_id")
    val assignedDriverId: Int? = null,
    @SerialName("assigned_driver_name")
    val assignedDriverName: String? = null,
    @SerialName("assigned_driver")
    val assignedDriver: AssignedDriverDto? = null,
    @SerialName("last_service_date")
    val lastServiceDate: String? = null,
    @SerialName("next_service_date")
    val nextServiceDate: String? = null,
    @SerialName("is_occupied")
    val isOccupied: Boolean = false,
    @SerialName("trip_assignment")
    val tripAssignment: VehicleTripAssignmentDto? = null,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("owner")
    val owner: VehicleOwnerDto? = null,
    @SerialName("registered_by_id")
    val registeredById: Int? = null,
    @SerialName("registered_by")
    val registeredBy: VehicleOwnerDto? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Trip assignment details when vehicle is occupied.
 */
@Serializable
data class VehicleTripAssignmentDto(
    @SerialName("trip_id")
    val tripId: Int = 0,
    @SerialName("trip_state")
    val tripState: String = "",
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("planned_start")
    val plannedStart: String? = null,
    @SerialName("planned_end")
    val plannedEnd: String? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("customer_name")
    val customerName: String? = null
)

/**
 * Owner/User DTO for nested objects in vehicle response.
 */
@Serializable
data class VehicleOwnerDto(
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
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Assigned Driver DTO for vehicle response.
 * Contains driver details when a driver is assigned to a vehicle.
 */
@Serializable
data class AssignedDriverDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("mobile")
    val mobile: String? = null,
    @SerialName("license_number")
    val licenseNumber: String? = null
)

/**
 * Location DTO for API communication.
 */
@Serializable
data class LocationDto(
    @SerialName("latitude")
    val latitude: Double = 0.0,
    @SerialName("longitude")
    val longitude: Double = 0.0,
    @SerialName("address")
    val address: String? = null,
    @SerialName("timestamp")
    val timestamp: String? = null
)

/**
 * API response wrapper for vehicle operations.
 */
@Serializable
data class VehicleApiResponse<T>(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: T? = null,
    @SerialName("vehicles")
    val vehicles: List<VehicleDto>? = null,
    @SerialName("vehicle")
    val vehicle: VehicleDto? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("per_page")
    val perPage: Int? = null,
    @SerialName("total")
    val total: Int? = null,
    @SerialName("total_pages")
    val totalPages: Int? = null
)

/**
 * Request body for creating/updating a vehicle.
 * Uses snake_case field names to match API request format.
 */
@Serializable
data class CreateVehicleRequest(
    @SerialName("registration_number")
    val registrationNumber: String,
    @SerialName("make")
    val make: String,
    @SerialName("model")
    val model: String,
    @SerialName("year")
    val year: Int,
    @SerialName("vehicle_type")
    val type: String,
    @SerialName("fuel_type")
    val fuelType: String = "petrol",
    @SerialName("capacity")
    val capacity: Int = 4,
    @SerialName("color")
    val color: String = "white",
    @SerialName("assigned_driver_id")
    val assignedDriverId: Int? = null
)

/**
 * Document file data for multipart upload.
 */
data class DocumentFileData(
    val fileName: String,
    val fileBytes: ByteArray,
    val mimeType: String,
    val expiryDate: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as DocumentFileData
        return fileName == other.fileName && fileBytes.contentEquals(other.fileBytes) && mimeType == other.mimeType && expiryDate == other.expiryDate
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (expiryDate?.hashCode() ?: 0)
        return result
    }
}

/**
 * Request for creating a vehicle with optional documents.
 * Used for multipart form upload to /vehicles/with-documents endpoint.
 */
data class CreateVehicleWithDocumentsRequest(
    // Vehicle details
    val registrationNumber: String,
    val make: String,
    val model: String,
    val year: Int,
    val vehicleType: String,
    val fuelType: String = "petrol",
    val capacity: Int = 4,
    val color: String = "white",
    // Optional documents
    val registrationCertificate: DocumentFileData? = null,
    val insurance: DocumentFileData? = null,
    val pucCertificate: DocumentFileData? = null,
    val fitnessCertificate: DocumentFileData? = null,
    val roadTax: DocumentFileData? = null,
    val permit: DocumentFileData? = null
)

