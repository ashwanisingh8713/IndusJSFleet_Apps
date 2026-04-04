package com.indusjs.fleet.data.model.auditlogs

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * API response for GET /audit-logs — paginated list of audit log entries.
 */
@Serializable
data class AuditLogsApiResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("data") val data: AuditLogsDataDto? = null
)

@Serializable
data class AuditLogsDataDto(
    @SerialName("items") val items: List<AuditLogItemDto> = emptyList(),
    @SerialName("count") val count: Int = 0,
    @SerialName("page") val page: Int = 1,
    @SerialName("per_page") val perPage: Int = 20,
    @SerialName("total_pages") val totalPages: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false
)

@Serializable
data class AuditLogItemDto(
    @SerialName("id") val id: Int,
    @SerialName("entity_type") val entityType: String,
    @SerialName("entity_id") val entityId: Int,
    @SerialName("action") val action: String,
    @SerialName("changes") val changes: JsonObject? = null,
    @SerialName("performed_by") val performedBy: AuditPerformedByDto? = null,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class AuditPerformedByDto(
    @SerialName("id") val id: Int,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("role") val role: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "Unknown" }
}
