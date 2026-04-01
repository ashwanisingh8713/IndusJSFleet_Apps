package com.ijs.reports.data.datasource

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.reports.TAG_REPORTS_REMOTE_DS
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.error.exception.ApiException
import com.ijs.reports.data.model.*
import dev.zacsweers.metro.Inject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Remote data source for Profit & Loss APIs.
 * All methods throw [ApiException] on failure instead of returning null,
 * so callers get meaningful error messages from the API response.
 */
@Inject
class ReportsRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json,
    private val logger: FleetLogger
) {

    /**
     * Extracts a user-friendly error message from an API error response body.
     */
    private fun extractErrorMessage(body: String, fallback: String): String {
        return try {
            val errorResponse = json.decodeFromString<ProfitLossResponse<Unit?>>(body)
            errorResponse.message.ifBlank { fallback }
        } catch (_: Exception) {
            fallback
        }
    }

    /**
     * Get single trip profit/loss
     * GET /trips/{trip_id}/profit-loss
     */
    suspend fun getTripProfitLoss(token: String, tripId: Int): TripProfitLossDto? {
        val url = "${ApiConfig.BASE_URL}/trips/$tripId/profit-loss"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching trip P&L: $tripId")

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch trip P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<TripProfitLossDto>>(body)
        return result.data
    }

    /**
     * Get single vehicle profit/loss
     * GET /vehicles/{vehicle_id}/profit-loss?period=monthly
     */
    suspend fun getVehicleProfitLoss(token: String, vehicleId: Int, period: String = "monthly"): VehicleProfitLossDto? {
        val url = "${ApiConfig.BASE_URL}/vehicles/$vehicleId/profit-loss"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching vehicle P&L: $vehicleId, period: $period")

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("period", period)
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch vehicle P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<VehicleProfitLossDto>>(body)
        return result.data
    }

    /**
     * Get fleet profit/loss report
     * GET /reports/profit-loss?period=monthly or ?start_date=&end_date=
     */
    suspend fun getFleetProfitLoss(
        token: String,
        period: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): FleetProfitLossDto? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching fleet P&L: period=$period, startDate=$startDate, endDate=$endDate")

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            period?.let { parameter("period", it) }
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch fleet P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<FleetProfitLossDto>>(body)
        return result.data
    }

    /**
     * Get multi-vehicle profit/loss
     * POST /reports/profit-loss/vehicles
     */
    suspend fun getMultiVehicleProfitLoss(token: String, request: MultiVehiclePLRequest): List<VehicleProfitLossDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/vehicles"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching multi-vehicle P&L: ${request.vehicleIds}")

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiVehiclePLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch multi-vehicle P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<MultiVehiclePLResponseDto>>(body)
        logger.d(TAG_REPORTS_REMOTE_DS, "Parsed vehicles: ${result.data?.vehicles?.size ?: 0}")
        return result.data?.vehicles
    }

    /**
     * Get multi-trip profit/loss
     * POST /reports/profit-loss/trips
     */
    suspend fun getMultiTripProfitLoss(token: String, request: MultiTripPLRequest): List<TripProfitLossDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/trips"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching multi-trip P&L: ${request.tripIds ?: "vehicle ${request.vehicleId}"}")

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiTripPLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch multi-trip P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<List<TripProfitLossDto>>>(body)
        return result.data
    }

    /**
     * Get single cost type analysis
     * GET /reports/profit-loss/cost-type/{type}?start_date=&end_date=
     */
    suspend fun getCostTypeAnalysis(
        token: String,
        costType: String,
        startDate: String? = null,
        endDate: String? = null
    ): CostTypeAnalysisDto? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/cost-type/$costType"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching cost type analysis: $costType")

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch cost type analysis (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<CostTypeAnalysisDto>>(body)
        return result.data
    }

    /**
     * Get multi cost type analysis
     * POST /reports/profit-loss/cost-types
     */
    suspend fun getMultiCostTypeAnalysis(token: String, request: MultiCostTypePLRequest): List<CostTypeAnalysisDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/cost-types"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching multi cost type analysis: ${request.costTypes}")

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiCostTypePLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch multi cost type analysis (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<List<CostTypeAnalysisDto>>>(body)
        return result.data
    }

    /**
     * Get consolidated P&L report
     * POST /reports/profit-loss/consolidated
     */
    suspend fun getConsolidatedPL(token: String, request: ConsolidatedPLRequest): ConsolidatedPLDto? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/consolidated"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching consolidated P&L report")

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(ConsolidatedPLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch consolidated P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<ConsolidatedPLDto>>(body)
        return result.data
    }

    /**
     * Get P&L summary with alerts
     * GET /reports/profit-loss/summary
     */
    suspend fun getPLSummary(
        token: String,
        startDate: String? = null,
        endDate: String? = null
    ): PLSummaryDto? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/summary"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching P&L summary: start_date=$startDate, end_date=$endDate")

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response status: ${response.status}, body length: ${body.length}")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch P&L summary (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<PLSummaryDto>>(body)
        logger.d(TAG_REPORTS_REMOTE_DS, "Parsed P&L summary: success=${result.success}")
        return result.data
    }
}
