package com.indusjs.fleet.data.datasource.dashboard

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.dashboard.AlertsStatusApiResponse
import com.indusjs.fleet.data.model.dashboard.CostOverviewApiResponse
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.data.model.dashboard.DashboardApiResponse
import com.indusjs.fleet.data.model.dashboard.FinancialPeriod
import com.indusjs.fleet.data.model.dashboard.FinancialSummaryApiResponse
import com.indusjs.fleet.data.model.dashboard.PendingPaymentsApiResponse
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

/**
 * Remote data source interface for dashboard operations.
 */
interface DashboardRemoteDataSource : RemoteDataSource {
    /**
     * Fetches the unified dashboard data.
     * Auto-detects user role and returns appropriate data.
     */
    suspend fun getDashboard(token: String): DashboardApiResponse

    /**
     * Fetches cost overview with filter (today/weekly/monthly).
     */
    suspend fun getCostOverview(token: String, filter: CostOverviewFilter): CostOverviewApiResponse

    /**
     * Fetches pending payments list.
     */
    suspend fun getPendingPayments(token: String, page: Int = 1, perPage: Int = 20): PendingPaymentsApiResponse

    /**
     * Fetches alerts status with detailed counts by type/priority.
     * Includes document expiry, license expiry, maintenance vehicles, etc.
     */
    suspend fun getAlertsStatus(token: String): AlertsStatusApiResponse

    /**
     * Fetches financial summary with KPIs for dashboard.
     * Available to Owner and General Manager only.
     */
    suspend fun getFinancialSummary(token: String, period: FinancialPeriod): FinancialSummaryApiResponse
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
                message = ApiErrorHandler.getNetworkErrorMessage(e)
            )
        }
    }

    override suspend fun getCostOverview(token: String, filter: CostOverviewFilter): CostOverviewApiResponse {
        return try {
            log.d { "Fetching cost overview with filter: ${filter.value}" }
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.DASHBOARD_COST_OVERVIEW}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("filter", filter.value)
            }
            handleCostOverviewResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch cost overview: ${e.message}" }
            CostOverviewApiResponse(
                success = false,
                message = ApiErrorHandler.getNetworkErrorMessage(e)
            )
        }
    }

    override suspend fun getPendingPayments(token: String, page: Int, perPage: Int): PendingPaymentsApiResponse {
        return try {
            log.d { "Fetching pending payments page: $page" }
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.DASHBOARD_PENDING_PAYMENTS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
            }
            handlePendingPaymentsResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch pending payments: ${e.message}" }
            PendingPaymentsApiResponse(
                success = false,
                message = ApiErrorHandler.getNetworkErrorMessage(e)
            )
        }
    }

    override suspend fun getAlertsStatus(token: String): AlertsStatusApiResponse {
        return try {
            log.d { "Fetching alerts status" }
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.DASHBOARD_ALERTS_STATUS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleAlertsStatusResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch alerts status: ${e.message}" }
            AlertsStatusApiResponse(
                success = false,
                message = ApiErrorHandler.getNetworkErrorMessage(e)
            )
        }
    }

    override suspend fun getFinancialSummary(token: String, period: FinancialPeriod): FinancialSummaryApiResponse {
        return try {
            log.d { "Fetching financial summary with period: ${period.value}" }
            val response: HttpResponse = httpClient.get("$baseUrl/dashboard/financial-summary") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("period", period.value)
            }
            handleFinancialSummaryResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch financial summary: ${e.message}" }
            FinancialSummaryApiResponse(
                success = false,
                message = ApiErrorHandler.getNetworkErrorMessage(e)
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
                DashboardApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse dashboard response: ${e.message}" }
            DashboardApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleCostOverviewResponse(response: HttpResponse): CostOverviewApiResponse {
        val responseBody = response.bodyAsText()
        log.d { "Cost overview response status: ${response.status}" }

        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<CostOverviewApiResponse>(responseBody)
            } else {
                CostOverviewApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse cost overview response: ${e.message}" }
            CostOverviewApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handlePendingPaymentsResponse(response: HttpResponse): PendingPaymentsApiResponse {
        val responseBody = response.bodyAsText()
        log.d { "Pending payments response status: ${response.status}" }

        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<PendingPaymentsApiResponse>(responseBody)
            } else {
                PendingPaymentsApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse pending payments response: ${e.message}" }
            PendingPaymentsApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleAlertsStatusResponse(response: HttpResponse): AlertsStatusApiResponse {
        val responseBody = response.bodyAsText()
        log.d { "Alerts status response status: ${response.status}" }

        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<AlertsStatusApiResponse>(responseBody)
            } else {
                AlertsStatusApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse alerts status response: ${e.message}" }
            AlertsStatusApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleFinancialSummaryResponse(response: HttpResponse): FinancialSummaryApiResponse {
        val responseBody = response.bodyAsText()
        log.d { "Financial summary response status: ${response.status}" }

        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<FinancialSummaryApiResponse>(responseBody)
            } else {
                FinancialSummaryApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse financial summary response: ${e.message}" }
            FinancialSummaryApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }
}
