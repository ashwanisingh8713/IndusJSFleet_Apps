package com.indusjs.fleet.data.datasource.dashboard

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.dashboard.DashboardApiResponse
import com.indusjs.fleet.data.model.dashboard.DashboardDataDto
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Remote data source interface for dashboard operations.
 */
interface DashboardRemoteDataSource : RemoteDataSource {
    /**
     * Fetches the unified dashboard data.
     * Auto-detects user role and returns appropriate data.
     */
    suspend fun getDashboard(token: String): DashboardApiResponse
}

/**
 * Implementation of DashboardRemoteDataSource using Ktor.
 */
@Inject
class DashboardRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : DashboardRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL
    private val log = Logger.withTag("DashboardRemoteDataSource")

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun getDashboard(token: String): DashboardApiResponse {
        return try {
            log.d { "Fetching dashboard data" }
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.DASHBOARD}") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleDashboardResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch dashboard: ${e.message}" }
            DashboardApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    private suspend fun handleDashboardResponse(response: HttpResponse): DashboardApiResponse {
        val responseBody = response.bodyAsText()
        log.d { "Dashboard response status: ${response.status}" }
        log.d { "Dashboard response body: $responseBody" }

        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<DashboardApiResponse>(responseBody)
            } else {
                // Try to parse error message from response
                try {
                    val errorResponse = json.decodeFromString<DashboardApiResponse>(responseBody)
                    DashboardApiResponse(
                        success = false,
                        message = errorResponse.message ?: "Request failed with status: ${response.status}"
                    )
                } catch (e: Exception) {
                    DashboardApiResponse(
                        success = false,
                        message = "Request failed with status: ${response.status}"
                    )
                }
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse dashboard response: ${e.message}" }
            DashboardApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }
}

