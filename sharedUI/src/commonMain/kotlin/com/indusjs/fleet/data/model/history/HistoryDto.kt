package com.indusjs.fleet.data.model.history

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * History DTOs for tracking changes to vehicles and drivers.
 */

// ==================== History Item DTOs ====================

/**
 * History item representing a single change/action.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class HistoryItemDto(
    val id: Int = 0,
    val action: String,
    val description: String? = null,
    @SerialName("entity_type")
    val entityType: String? = null,
    @SerialName("entity_id")
    val entityId: Int? = null,
    @SerialName("performed_by")
    val performedBy: HistoryPerformerDto? = null,
    @SerialName("performed_by_id")
    val performedById: Int? = null,
    val timestamp: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val metadata: HistoryMetadataDto? = null,
    @SerialName("old_value")
    val oldValue: String? = null,
    @SerialName("new_value")
    val newValue: String? = null,
    @SerialName("field_name")
    val fieldName: String? = null
) {
    /**
     * Get the display action with proper formatting.
     */
    val displayAction: String
        get() = action.replace("_", " ").split(" ").joinToString(" ") {
            it.replaceFirstChar { char -> char.uppercase() }
        }

    /**
     * Get formatted timestamp for display.
     */
    val displayTimestamp: String
        get() = (timestamp ?: createdAt)?.let { formatTimestamp(it) } ?: ""

    /**
     * Get icon for the action type.
     */
    val actionIcon: String
        get() = when {
            action.contains("created", ignoreCase = true) -> "➕"
            action.contains("updated", ignoreCase = true) -> "✏️"
            action.contains("deleted", ignoreCase = true) -> "🗑️"
            action.contains("assigned", ignoreCase = true) -> "👤"
            action.contains("removed", ignoreCase = true) -> "❌"
            action.contains("state", ignoreCase = true) -> "🔄"
            action.contains("status", ignoreCase = true) -> "📊"
            action.contains("trip", ignoreCase = true) -> "🚗"
            action.contains("cost", ignoreCase = true) -> "💰"
            action.contains("document", ignoreCase = true) -> "📄"
            action.contains("caretaker", ignoreCase = true) -> "👥"
            else -> "📝"
        }

    private fun formatTimestamp(timestamp: String): String {
        // Simple formatting - returns date part
        return timestamp.substringBefore("T").takeIf { it.isNotBlank() } ?: timestamp
    }
}

/**
 * Person who performed the action.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class HistoryPerformerDto(
    val id: Int = 0,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val role: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "System" }

    val displayName: String
        get() = if (fullName.isNotBlank() && fullName != "System") {
            "$fullName (${role?.replaceFirstChar { it.uppercase() } ?: "User"})"
        } else {
            "System"
        }
}

/**
 * Additional metadata for history item.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class HistoryMetadataDto(
    @SerialName("ip_address")
    val ipAddress: String? = null,
    @SerialName("user_agent")
    val userAgent: String? = null,
    val details: Map<String, String>? = null
)

// ==================== Vehicle History ====================

/**
 * Vehicle history API response.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class VehicleHistoryApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: VehicleHistoryDataDto? = null
)

/**
 * Vehicle history data with pagination.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class VehicleHistoryDataDto(
    @SerialName("vehicle_id")
    val vehicleId: Int? = null,
    val history: List<HistoryItemDto>? = null,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
)

// ==================== Driver History ====================

/**
 * Driver history API response.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class DriverHistoryApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DriverHistoryDataDto? = null
)

/**
 * Driver history data with pagination.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class DriverHistoryDataDto(
    @SerialName("driver_id")
    val driverId: Int? = null,
    val history: List<HistoryItemDto>? = null,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
)

// ==================== Generic History List ====================

/**
 * Generic history list response.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class HistoryListApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: HistoryListDataDto? = null
)

/**
 * Generic history list data.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonIgnoreUnknownKeys
@Serializable
data class HistoryListDataDto(
    val history: List<HistoryItemDto>? = null,
    val page: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 20,
    @SerialName("total_pages")
    val totalPages: Int = 1,
    @SerialName("total_count")
    val totalCount: Int = 0
)

