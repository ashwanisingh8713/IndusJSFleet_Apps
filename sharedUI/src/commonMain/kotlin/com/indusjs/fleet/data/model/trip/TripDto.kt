package com.indusjs.fleet.data.model.trip

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Trip DTO for API communication.
 */
@Serializable
data class TripDto(
    @SerialName("id")
    val id: String,
    @SerialName("tripNumber")
    val tripNumber: String,
    @SerialName("vehicleId")
    val vehicleId: String,
    @SerialName("vehicleNumber")
    val vehicleNumber: String,
    @SerialName("driverId")
    val driverId: String,
    @SerialName("driverName")
    val driverName: String,
    @SerialName("status")
    val status: String,
    @SerialName("startLocation")
    val startLocation: TripLocationDto,
    @SerialName("endLocation")
    val endLocation: TripLocationDto,
    @SerialName("currentLocation")
    val currentLocation: TripLocationDto? = null,
    @SerialName("distance")
    val distance: Double = 0.0,
    @SerialName("estimatedDuration")
    val estimatedDuration: Long = 0L,
    @SerialName("actualDuration")
    val actualDuration: Long? = null,
    @SerialName("scheduledStartTime")
    val scheduledStartTime: Long = 0L,
    @SerialName("actualStartTime")
    val actualStartTime: Long? = null,
    @SerialName("actualEndTime")
    val actualEndTime: Long? = null,
    @SerialName("cargo")
    val cargo: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Trip location DTO for API communication.
 */
@Serializable
data class TripLocationDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("address")
    val address: String
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
    @SerialName("vehicleId")
    val vehicleId: String,
    @SerialName("driverId")
    val driverId: String,
    @SerialName("startLocation")
    val startLocation: TripLocationDto,
    @SerialName("endLocation")
    val endLocation: TripLocationDto,
    @SerialName("scheduledStartTime")
    val scheduledStartTime: Long,
    @SerialName("cargo")
    val cargo: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

