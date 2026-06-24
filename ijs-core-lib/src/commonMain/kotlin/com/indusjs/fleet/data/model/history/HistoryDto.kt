package com.indusjs.fleet.data.model.history

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== Shared History Item DTOs ====================

/**
 * Individual history change item.
 * Represents a single audit log entry for entity changes.
 * Used by vehicles, drivers, and trips history endpoints.
 */
@Serializable
data class HistoryItemDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("entity_id")
    val entityId: Int? = null,
    @SerialName("action")
    val action: String? = null,
    @SerialName("field_name")
    val fieldName: String? = null,
    @SerialName("old_value")
    val oldValue: String? = null,
    @SerialName("new_value")
    val newValue: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("performed_by")
    val performedBy: HistoryPerformerDto? = null,
    @SerialName("performed_by_id")
    val performedById: Int? = null,
    @SerialName("created_at")
    val createdAt: Long? = null
) : Dto {
    /**
     * Display-friendly action text.
     */
    val displayAction: String
        get() = action?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "Unknown action"

    /**
     * Emoji icon based on action type.
     */
    val actionIcon: String
        get() = when (action?.lowercase()) {
            "created", "create" -> "✨"
            "updated", "update" -> "✏️"
            "deleted", "delete" -> "🗑️"
            "status_changed", "state_changed" -> "🔄"
            "assigned", "assign" -> "🔗"
            "unassigned", "unassign" -> "⛓️‍💥"
            "activated", "activate" -> "✅"
            "deactivated", "deactivate" -> "⛔"
            "document_added" -> "📄"
            "document_removed" -> "📄"
            else -> "📝"
        }

    /**
     * Display-friendly timestamp.
     */
    val displayTimestamp: String
        get() {
            val ms = createdAt ?: return ""
            if (ms <= 0L) return ""
            // epoch-millis -> "DD-MM-YYYY HH:mm" (24h), preserving the prior format.
            val v = com.indusjs.datetimeutils.FleetEpoch.toValue(ms) ?: return ""
            val dd = v.day.toString().padStart(2, '0')
            val mm = v.month.toString().padStart(2, '0')
            val hh = v.hour.toString().padStart(2, '0')
            val mi = v.minute.toString().padStart(2, '0')
            return "$dd-$mm-${v.year} $hh:$mi"
        }
}

/**
 * Performer (user) who made the change.
 */
@Serializable
data class HistoryPerformerDto(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("role")
    val role: String? = null
) : Dto {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { email ?: "Unknown" }
}

// ==================== Driver History DTOs ====================

/**
 * API response wrapper for driver history.
 * Used for GET /drivers/{id}/history
 */
@Serializable
data class DriverHistoryApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: DriverHistoryDataDto? = null
)

/**
 * Driver history data containing the list of history items with pagination.
 */
@Serializable
data class DriverHistoryDataDto(
    @SerialName("history")
    val history: List<HistoryItemDto>? = null,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
) : Dto

// ==================== Vehicle History DTOs ====================

/**
 * API response wrapper for vehicle history.
 * Used for GET /vehicles/{id}/history
 */
@Serializable
data class VehicleHistoryApiResponse(
    @SerialName("success")
    val success: Boolean = false,
    @SerialName("message")
    val message: String? = null,
    @SerialName("data")
    val data: VehicleHistoryDataDto? = null
)

/**
 * Vehicle history data containing the list of history items with pagination.
 */
@Serializable
data class VehicleHistoryDataDto(
    @SerialName("history")
    val history: List<HistoryItemDto>? = null,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
) : Dto

