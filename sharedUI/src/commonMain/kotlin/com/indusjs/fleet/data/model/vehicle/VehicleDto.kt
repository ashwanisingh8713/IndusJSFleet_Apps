package com.indusjs.fleet.data.model.vehicle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Vehicle DTO for API communication.
 */
@Serializable
data class VehicleDto(
    @SerialName("id")
    val id: String,
    @SerialName("registrationNumber")
    val registrationNumber: String,
    @SerialName("make")
    val make: String,
    @SerialName("model")
    val model: String,
    @SerialName("year")
    val year: Int,
    @SerialName("type")
    val type: String,
    @SerialName("status")
    val status: String,
    @SerialName("fuelLevel")
    val fuelLevel: Int = 0,
    @SerialName("mileage")
    val mileage: Double = 0.0,
    @SerialName("lastLocation")
    val lastLocation: LocationDto? = null,
    @SerialName("assignedDriverId")
    val assignedDriverId: String? = null,
    @SerialName("assignedDriverName")
    val assignedDriverName: String? = null,
    @SerialName("lastServiceDate")
    val lastServiceDate: Long? = null,
    @SerialName("nextServiceDate")
    val nextServiceDate: Long? = null
)

/**
 * Location DTO for API communication.
 */
@Serializable
data class LocationDto(
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double,
    @SerialName("address")
    val address: String? = null
)

/**
 * API response wrapper for vehicle operations.
 */
@Serializable
data class VehicleApiResponse<T>(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: T? = null
)

/**
 * Request body for creating/updating a vehicle.
 */
@Serializable
data class CreateVehicleRequest(
    @SerialName("registrationNumber")
    val registrationNumber: String,
    @SerialName("make")
    val make: String,
    @SerialName("model")
    val model: String,
    @SerialName("year")
    val year: Int,
    @SerialName("type")
    val type: String,
    @SerialName("status")
    val status: String = "ACTIVE",
    @SerialName("assignedDriverId")
    val assignedDriverId: String? = null
)

