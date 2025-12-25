package com.indusjs.fleet.data.model.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip DTO for API communication.
 * Uses snake_case to match API response format.
 */
@Serializable
data class TripDto(
    @SerialName("id")
    val id: Int,
    @SerialName("trip_number")
    val tripNumber: String? = null,
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("vehicle")
    val vehicle: TripVehicleDto? = null,
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("driver")
    val driver: TripDriverDto? = null,
    @SerialName("state")
    val state: String = "planned",
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("start_lat")
    val startLat: Double? = null,
    @SerialName("start_lng")
    val startLng: Double? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("end_lat")
    val endLat: Double? = null,
    @SerialName("end_lng")
    val endLng: Double? = null,
    @SerialName("current_lat")
    val currentLat: Double? = null,
    @SerialName("current_lng")
    val currentLng: Double? = null,
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,
    @SerialName("actual_distance")
    val actualDistance: Double? = null,
    @SerialName("scheduled_date")
    val scheduledDate: String? = null,
    @SerialName("start_time")
    val startTime: String? = null,
    @SerialName("planned_start")
    val plannedStart: String? = null,
    @SerialName("planned_end")
    val plannedEnd: String? = null,
    @SerialName("actual_start")
    val actualStart: String? = null,
    @SerialName("actual_end")
    val actualEnd: String? = null,
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_weight")
    val cargoWeight: Double? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("customer_contact")
    val customerContact: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("created_by_id")
    val createdById: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Embedded vehicle info in trip response.
 */
@Serializable
data class TripVehicleDto(
    @SerialName("id")
    val id: Int,
    @SerialName("registration_number")
    val registrationNumber: String? = null,
    @SerialName("make")
    val make: String? = null,
    @SerialName("model")
    val model: String? = null
)

/**
 * Embedded driver info in trip response.
 */
@Serializable
data class TripDriverDto(
    @SerialName("id")
    val id: Int,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("mobile")
    val mobile: String? = null
)

/**
 * API response wrapper for trip operations.
 */
@Serializable
data class TripApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: T? = null
)

/**
 * Request body for creating a trip.
 */
@Serializable
data class CreateTripRequest(
    @SerialName("vehicle_id")
    val vehicleId: Int,
    @SerialName("driver_id")
    val driverId: Int,
    @SerialName("scheduled_date")
    val scheduledDate: String,
    @SerialName("start_time")
    val startTime: String,
    @SerialName("planned_start")
    val plannedStart: String,
    @SerialName("planned_end")
    val plannedEnd: String,
    @SerialName("start_location")
    val startLocation: String,
    @SerialName("start_lat")
    val startLat: Double? = null,
    @SerialName("start_lng")
    val startLng: Double? = null,
    @SerialName("end_location")
    val endLocation: String,
    @SerialName("end_lat")
    val endLat: Double? = null,
    @SerialName("end_lng")
    val endLng: Double? = null,
    @SerialName("cargo_type")
    val cargoType: String,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("cargo_weight")
    val cargoWeight: Double? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("customer_contact")
    val customerContact: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request body for updating a trip.
 */
@Serializable
data class UpdateTripRequest(
    @SerialName("start_location")
    val startLocation: String? = null,
    @SerialName("start_lat")
    val startLat: Double? = null,
    @SerialName("start_lng")
    val startLng: Double? = null,
    @SerialName("end_location")
    val endLocation: String? = null,
    @SerialName("end_lat")
    val endLat: Double? = null,
    @SerialName("end_lng")
    val endLng: Double? = null,
    @SerialName("estimated_distance")
    val estimatedDistance: Double? = null,
    @SerialName("planned_start")
    val plannedStart: String? = null,
    @SerialName("planned_end")
    val plannedEnd: String? = null,
    @SerialName("cargo_type")
    val cargoType: String? = null,
    @SerialName("cargo_description")
    val cargoDescription: String? = null,
    @SerialName("customer_name")
    val customerName: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request body for updating trip state.
 */
@Serializable
data class UpdateTripStateRequest(
    @SerialName("state")
    val state: String
)

