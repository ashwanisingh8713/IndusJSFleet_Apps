package com.indusjs.fleet.data.datasource.auditlogs

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.auditlogs.AuditLogsApiResponse
import com.indusjs.fleet.network.TAG_AUDIT_LOGS_REMOTE_DS
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

interface AuditLogsRemoteDataSource : RemoteDataSource {
    /**
     * Fetches paginated audit logs with optional filters.
     *
     * @param token Bearer token.
     * @param page Page number (1-based).
     * @param perPage Items per page (max 100).
     * @param entityType Optional filter: vehicle, driver, trip, trip_cost, etc.
     * @param action Optional filter: create, update, delete, state_change, toggle_active.
     */
    suspend fun getAuditLogs(
        token: String,
        page: Int = 1,
        perPage: Int = 20,
        entityType: String? = null,
        action: String? = null
    ): AuditLogsApiResponse
}

@Inject
class AuditLogsRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : AuditLogsRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun getAuditLogs(
        token: String,
        page: Int,
        perPage: Int,
        entityType: String?,
        action: String?
    ): AuditLogsApiResponse {
        return try {
            logger.d(TAG_AUDIT_LOGS_REMOTE_DS, "Fetching audit logs page=$page entityType=$entityType action=$action")
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.AUDIT_LOGS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
                entityType?.let { parameter("entity_type", it) }
                action?.let { parameter("action", it) }
            }
            if (response.status.isSuccess()) {
                val body = response.bodyAsText()
                json.decodeFromString<AuditLogsApiResponse>(body)
            } else {
                logger.w(TAG_AUDIT_LOGS_REMOTE_DS, "Audit logs API returned ${response.status}")
                AuditLogsApiResponse(success = false, message = "HTTP ${response.status.value}")
            }
        } catch (e: Exception) {
            logger.e(TAG_AUDIT_LOGS_REMOTE_DS, "Failed to fetch audit logs: ${e.message}", e)
            AuditLogsApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }
}
