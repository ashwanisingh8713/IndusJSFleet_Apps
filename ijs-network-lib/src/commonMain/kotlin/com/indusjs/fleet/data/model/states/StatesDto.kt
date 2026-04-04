package com.indusjs.fleet.data.model.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API response for GET /states — returns all valid entity states
 * for vehicles, drivers, trips, and payments.
 */
@Serializable
data class StatesApiResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: StatesDataDto? = null
)

@Serializable
data class StatesDataDto(
    @SerialName("vehicle_states") val vehicleStates: List<StateItemDto> = emptyList(),
    @SerialName("driver_states") val driverStates: List<StateItemDto> = emptyList(),
    @SerialName("trip_states") val tripStates: List<StateItemDto> = emptyList(),
    @SerialName("payment_states") val paymentStates: List<StateItemDto> = emptyList()
)

/**
 * A single state value with its display label and optional color hex.
 *
 * Example: `{ "value": "active", "label": "Active", "color": "#4CAF50" }`
 */
@Serializable
data class StateItemDto(
    @SerialName("value") val value: String,
    @SerialName("label") val label: String,
    @SerialName("color") val color: String? = null
)
