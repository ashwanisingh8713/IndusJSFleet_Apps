package com.indusjs.fleet.data.model.state

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Individual state change history record.
 */
@Serializable
data class StateHistoryItemDto(
    @SerialName("id")
    val id: Int,
    @SerialName("entity_type")
    val entityType: String,
    @SerialName("entity_id")
    val entityId: Int,
    @SerialName("from_state")
    val fromState: String,
    @SerialName("to_state")
    val toState: String,
    @SerialName("changed_at")
    val changedAt: String,
    @SerialName("changed_by")
    val changedBy: StateChangedByDto? = null,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * User who made the state change.
 */
@Serializable
data class StateChangedByDto(
    @SerialName("id")
    val id: Int,
    @SerialName("email")
    val email: String? = null,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("role")
    val role: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifEmpty { email ?: "Unknown" }
}

/**
 * Response wrapper for state history list.
 */
@Serializable
data class StateHistoryResponseDto(
    @SerialName("history")
    val history: List<StateHistoryItemDto> = emptyList(),
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total")
    val total: Int = 0,
    @SerialName("total_pages")
    val totalPages: Int = 1
)

/**
 * State information with description and valid transitions.
 */
@Serializable
data class StateInfoDto(
    @SerialName("value")
    val value: String,
    @SerialName("label")
    val label: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("valid_transitions")
    val validTransitions: List<String> = emptyList()
)

/**
 * Response for GET /states/vehicles, /states/drivers, /states/trips
 */
@Serializable
data class EntityStatesResponseDto(
    @SerialName("entity_type")
    val entityType: String,
    @SerialName("states")
    val states: List<StateInfoDto> = emptyList()
)

/**
 * Response for GET /states (all states)
 */
@Serializable
data class AllStatesResponseDto(
    @SerialName("vehicle_states")
    val vehicleStates: List<StateInfoDto> = emptyList(),
    @SerialName("driver_statuses")
    val driverStatuses: List<StateInfoDto> = emptyList(),
    @SerialName("trip_states")
    val tripStates: List<StateInfoDto> = emptyList()
)

/**
 * Request body for state update APIs.
 */
@Serializable
data class StateUpdateRequestDto(
    @SerialName("state")
    val state: String,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

/**
 * Request body for driver status update APIs.
 */
@Serializable
data class StatusUpdateRequestDto(
    @SerialName("status")
    val status: String,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("notes")
    val notes: String? = null
)

