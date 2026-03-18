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
 * Request body for updating a vehicle (partial update).
 * All fields are optional - only provided fields will be updated.
 */
@Serializable
data class UpdateVehicleRequest(
    @SerialName("make")
    val make: String? = null,
    @SerialName("model")
    val model: String? = null,
    @SerialName("year")
    val year: Int? = null,
    @SerialName("vehicle_type")
    val type: String? = null,
    @SerialName("fuel_type")
    val fuelType: String? = null,
    @SerialName("capacity")
    val capacity: Int? = null,
    @SerialName("color")
    val color: String? = null,
    @SerialName("mileage")
    val mileage: Double? = null,
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

// ==================== Vehicle Detail DTOs ====================

/**
 * Vehicle Detail DTO - Combined data for all tabs.
 * GET /vehicles/{id}/detail
 */
@Serializable
data class VehicleDetailDto(
    @SerialName("vehicle")
    val vehicle: VehicleDto? = null,
    @SerialName("assigned_driver")
    val assignedDriver: AssignedDriverDto? = null,
    @SerialName("current_location")
    val currentLocation: LocationDto? = null,
    @SerialName("stats")
    val stats: VehicleStatsDto? = null,
    @SerialName("documents")
    val documents: DocumentsSummaryDto? = null,
    @SerialName("trips")
    val trips: TripsSummaryDto? = null,
    @SerialName("route")
    val route: VehicleRouteDto? = null
)

/**
 * Vehicle statistics DTO.
 */
@Serializable
data class VehicleStatsDto(
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0,
    @SerialName("total_distance")
    val totalDistance: Double = 0.0,
    @SerialName("trips_this_month")
    val tripsThisMonth: Int = 0,
    @SerialName("distance_this_month")
    val distanceThisMonth: Double = 0.0
)

/**
 * Documents summary DTO.
 */
@Serializable
data class DocumentsSummaryDto(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("uploaded")
    val uploaded: Int = 0,
    @SerialName("not_uploaded")
    val notUploaded: Int = 0,
    @SerialName("active")
    val active: Int = 0,
    @SerialName("expiring_soon")
    val expiringSoon: Int = 0,
    @SerialName("expired")
    val expired: Int = 0,
    @SerialName("pending")
    val pending: Int = 0,
    @SerialName("alerts")
    val alerts: List<DocumentAlertDto> = emptyList()
)

/**
 * Document alert DTO.
 */
@Serializable
data class DocumentAlertDto(
    @SerialName("type")
    val type: String = "",
    @SerialName("type_name")
    val typeName: String = "",
    @SerialName("alert_type")
    val alertType: String = "",
    @SerialName("message")
    val message: String = "",
    @SerialName("days_remaining")
    val daysRemaining: Int? = null
)

/**
 * Trips summary DTO.
 */
@Serializable
data class TripsSummaryDto(
    @SerialName("total")
    val total: Int = 0,
    @SerialName("planned")
    val planned: Int = 0,
    @SerialName("in_progress")
    val inProgress: Int = 0,
    @SerialName("completed")
    val completed: Int = 0,
    @SerialName("cancelled")
    val cancelled: Int = 0,
    @SerialName("recent_trips")
    val recentTrips: List<VehicleTripItemDto> = emptyList()
)

/**
 * Vehicle Trips DTO - Paginated trips for Trips tab.
 * GET /vehicles/{id}/trips
 */
@Serializable
data class VehicleTripsDto(
    @SerialName("summary")
    val summary: TripsSummaryDto? = null,
    @SerialName("trips")
    val trips: List<VehicleTripItemDto> = emptyList(),
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 10,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("has_more")
    val hasMore: Boolean = false
)

/**
 * Trip item DTO.
 * Supports both field names from API: origin/destination OR start_location/end_location
 */
@Serializable
data class VehicleTripItemDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("trip_number")
    val tripNumber: String? = null,
    // Primary: origin/destination (some API responses use this)
    @SerialName("origin")
    val origin: String? = null,
    @SerialName("destination")
    val destination: String? = null,
    // Alternative: start_location/end_location (some API responses use this)
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("state")
    val state: String = "",
    @SerialName("state_label")
    val stateLabel: String = "",
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("distance")
    val distance: Double? = null,
    @SerialName("duration")
    val duration: String? = null
) {
    /** Resolved origin - prefers origin, falls back to startLocation */
    val resolvedOrigin: String get() = origin?.takeIf { it.isNotBlank() } ?: startLocation ?: ""

    /** Resolved destination - prefers destination, falls back to endLocation */
    val resolvedDestination: String get() = destination?.takeIf { it.isNotBlank() } ?: endLocation ?: ""
}

/**
 * Vehicle Route DTO - Route & Stops tab.
 * GET /vehicles/{id}/route
 */
@Serializable
data class VehicleRouteDto(
    @SerialName("has_active_trip")
    val hasActiveTrip: Boolean = false,
    @SerialName("trip_id")
    val tripId: Int? = null,
    @SerialName("trip_number")
    val tripNumber: String? = null,
    @SerialName("driver_name")
    val driverName: String? = null,
    @SerialName("origin")
    val origin: String? = null,
    @SerialName("destination")
    val destination: String? = null,
    @SerialName("current_position")
    val currentPosition: LocationDto? = null,
    @SerialName("progress")
    val progress: RouteProgressDto? = null,
    @SerialName("stops")
    val stops: List<RouteStopDto> = emptyList()
)

/**
 * Route progress DTO.
 */
@Serializable
data class RouteProgressDto(
    @SerialName("percentage")
    val percentage: Int = 0,
    @SerialName("distance_covered")
    val distanceCovered: Double = 0.0,
    @SerialName("distance_remaining")
    val distanceRemaining: Double = 0.0,
    @SerialName("time_elapsed")
    val timeElapsed: String? = null,
    @SerialName("eta")
    val eta: String? = null
)

/**
 * Route stop DTO.
 */
@Serializable
data class RouteStopDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("sequence")
    val sequence: Int = 0,
    @SerialName("type")
    val type: String = "",
    @SerialName("location")
    val location: String = "",
    @SerialName("address")
    val address: String? = null,
    @SerialName("status")
    val status: String = "",
    @SerialName("scheduled_time")
    val scheduledTime: String? = null,
    @SerialName("actual_time")
    val actualTime: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Vehicle Documents Detail DTO - Documents tab.
 * GET /vehicles/{id}/documents/detail
 */
@Serializable
data class VehicleDocumentsDetailDto(
    @SerialName("summary")
    val summary: DocumentsSummaryDto? = null,
    @SerialName("document_types")
    val documentTypes: List<DocumentTypeDetailDto> = emptyList(),
    @SerialName("alert_docs")
    val alertDocs: List<DocumentAlertDto> = emptyList(),
    @SerialName("other_documents")
    val otherDocuments: List<VehicleDocumentInfoDto> = emptyList()
)

/**
 * Document type detail DTO.
 */
@Serializable
data class DocumentTypeDetailDto(
    @SerialName("type")
    val type: String = "",
    @SerialName("tag")
    val tag: String = "",
    @SerialName("type_name")
    val typeName: String = "",
    @SerialName("description")
    val description: String? = null,
    @SerialName("is_required")
    val isRequired: Boolean = false,
    @SerialName("is_uploaded")
    val isUploaded: Boolean = false,
    @SerialName("document")
    val document: VehicleDocumentInfoDto? = null
)

/**
 * Vehicle document info DTO.
 */
@Serializable
data class VehicleDocumentInfoDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String = "",
    @SerialName("document_number")
    val documentNumber: String? = null,
    @SerialName("expiry_date")
    val expiryDate: String? = null,
    @SerialName("status")
    val status: String = "",
    @SerialName("status_label")
    val statusLabel: String = "",
    @SerialName("days_remaining")
    val daysRemaining: Int? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("uploaded_at")
    val uploadedAt: String? = null
)

// ==================== Existing Backend API DTOs ====================

/**
 * Vehicle document DTO from GET /vehicles/:id/documents
 */
@Serializable
data class VehicleDocumentDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("vehicle_id")
    val vehicleId: Int = 0,
    @SerialName("document_type")
    val documentType: String = "",
    @SerialName("document_name")
    val documentName: String = "",
    @SerialName("document_number")
    val documentNumber: String? = null,
    @SerialName("file_name")
    val fileName: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    @SerialName("file_size")
    val fileSize: Long = 0,
    @SerialName("mime_type")
    val mimeType: String = "",
    @SerialName("issue_date")
    val issueDate: String? = null,
    @SerialName("expiry_date")
    val expiryDate: String? = null,
    @SerialName("issuing_authority")
    val issuingAuthority: String? = null,
    @SerialName("status")
    val status: String = "pending",
    @SerialName("verified_at")
    val verifiedAt: String? = null,
    @SerialName("verified_by")
    val verifiedBy: Int? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Trip DTO from GET /trips?vehicle_id= (simplified for vehicle detail)
 */
@Serializable
data class VehicleTripDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("state")
    val state: String = "",
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("end_time")
    val endTime: String? = null,
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("driver")
    val driver: AssignedDriverDto? = null,
    @SerialName("driver_id")
    val driverId: Int? = null,
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    @SerialName("distance")
    val distance: Double? = null,
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

