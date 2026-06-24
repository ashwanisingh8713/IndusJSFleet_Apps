package com.indusjs.fleet.data.model.state

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request DTO for updating entity state (vehicles, trips).
 * Used with PATCH /vehicles/{id}/state, PATCH /trips/{id}/state
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
 * Request DTO for updating driver status.
 * Used with PATCH /drivers/{id}/status
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

/**
 * Response DTO for state/status change history.
 * Used by vehicles, drivers, and trips state-history endpoints.
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
) : Dto

/**
 * Individual state change history item.
 */
@Serializable
data class StateHistoryItemDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("entity_id")
    val entityId: Int? = null,
    @SerialName("old_state")
    val oldState: String? = null,
    @SerialName("new_state")
    val newState: String? = null,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("notes")
    val notes: String? = null,
    @SerialName("performed_by")
    val performedBy: Int? = null,
    @SerialName("performed_by_name")
    val performedByName: String? = null,
    @SerialName("performed_by_role")
    val performedByRole: String? = null,
    @SerialName("created_at")
    val createdAt: Long? = null
) : Dto

