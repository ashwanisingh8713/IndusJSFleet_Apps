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
     * Get P&L by customer (fleet-wide per-customer comparison).
     * GET /reports/profit-loss/customers?period=monthly
     */
    suspend fun getCustomerProfitLoss(token: String, period: String = "monthly"): CustomerPLReportDto? {
        val url = "${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REPORTS_PL_CUSTOMERS}"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching customer P&L, period: $period")

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("period", period)
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Response: $body")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch customer P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<CustomerPLReportDto>>(body)
        return result.data
    }

    /**
     * Get fleet profit/loss report
     * GET /reports/profit-loss?period=monthly or ?start_date=&end_date=
     */
    suspend fun getFleetProfitLoss(
        token: String,
        period: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
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
        logger.d(TAG_REPORTS_REMOTE_DS, "Fleet P&L response status: ${response.status}, body length: ${body.length}")
        logger.d(TAG_REPORTS_REMOTE_DS, "Fleet P&L raw (first 1000 chars): ${body.take(1000)}")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch fleet P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<FleetProfitLossDto>>(body)
        result.data?.let { dto ->
            logger.d(TAG_REPORTS_REMOTE_DS, "Fleet P&L parsed: vehicles=${dto.vehicles?.size}, vehicleCount=${dto.vehicleCount}, fleetSummary=${dto.fleetSummary}")
            dto.fleetSummary?.let { s ->
                logger.d(TAG_REPORTS_REMOTE_DS, "  fleetSummary: revenue=${s.totalRevenue}, totalCost=${s.totalCost}, driverCost=${s.driverCost}")
                logger.d(TAG_REPORTS_REMOTE_DS, "  fleetSummary: netProfit=${s.netProfit}, trips=${s.totalTrips}, activeVehicles=${s.activeVehicles}")
            }
        }
        return result.data
    }

    /**
     * Get multi-vehicle profit/loss
     * POST /reports/profit-loss/vehicles
     */
    suspend fun getMultiVehicleProfitLoss(token: String, request: MultiVehiclePLRequest): List<VehicleProfitLossDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/vehicles"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching multi-vehicle P&L: ids=${request.vehicleIds}, dates=${request.startDate}..${request.endDate}")

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiVehiclePLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        logger.d(TAG_REPORTS_REMOTE_DS, "Multi-vehicle P&L response status: ${response.status}, body length: ${body.length}")
        logger.d(TAG_REPORTS_REMOTE_DS, "Multi-vehicle P&L raw (first 1000 chars): ${body.take(1000)}")

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch multi-vehicle P&L (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<MultiVehiclePLResponseDto>>(body)
        logger.d(TAG_REPORTS_REMOTE_DS, "Parsed vehicles: ${result.data?.vehicles?.size ?: 0}")
        result.data?.vehicles?.firstOrNull()?.let { first ->
            logger.d(TAG_REPORTS_REMOTE_DS, "First vehicle: id=${first.vehicleId}, revenue=${first.totalRevenue}, totalCost=${first.totalCost}, totalExpenses=${first.totalExpenses}, grossProfit=${first.grossProfit}, netProfit=${first.netProfit}")
        }
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
        // Backend returns { period, trips, summary }, not a bare array.
        val result = json.decodeFromString<ProfitLossResponse<MultiTripPLResponseDto>>(body)
        return result.data?.trips
    }

    /**
     * Get single cost type analysis
     * GET /reports/profit-loss/cost-type/{type}?start_date=&end_date=
     */
    suspend fun getCostTypeAnalysis(
        token: String,
        costType: String,
        startDate: Long? = null,
        endDate: Long? = null
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
        // Backend returns { period, cost_type_analysis }, not a flat object.
        val result = json.decodeFromString<ProfitLossResponse<CostTypePLResponseDto>>(body)
        return result.data?.costTypeAnalysis
    }

    /**
     * Get multi cost type analysis
     * POST /reports/profit-loss/cost-types
     */
    suspend fun getMultiCostTypeAnalysis(token: String, request: MultiCostTypePLRequest): List<CostTypeAnalysisDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/cost-types"
        logger.d(TAG_REPORTS_REMOTE_DS, "Fetching multi cost type analysis: ${request.costIds}")

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
        // Backend returns { period, cost_types, summary }, not a bare array.
        val result = json.decodeFromString<ProfitLossResponse<MultiCostTypePLResponseDto>>(body)
        return result.data?.costTypes
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
        startDate: Long? = null,
        endDate: Long? = null
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
        // Log more of the raw response for debugging
        logger.d(TAG_REPORTS_REMOTE_DS, "Raw P&L summary response (first 1000 chars): ${body.take(1000)}")
        if (body.length > 1000) {
            logger.d(TAG_REPORTS_REMOTE_DS, "Raw P&L summary response (next 1000 chars): ${body.drop(1000).take(1000)}")
        }

        if (!response.status.isSuccess()) {
            val msg = extractErrorMessage(body, "Failed to fetch P&L summary (HTTP ${response.status.value})")
            logger.e(TAG_REPORTS_REMOTE_DS, msg)
            throw ApiException(msg, response.status.value)
        }
        val result = json.decodeFromString<ProfitLossResponse<PLSummaryDto>>(body)
        logger.d(TAG_REPORTS_REMOTE_DS, "Parsed P&L summary: success=${result.success}, message=${result.message}")
        logger.d(TAG_REPORTS_REMOTE_DS, "PLSummaryDto data null? ${result.data == null}")
        result.data?.let { dto ->
            logger.d(TAG_REPORTS_REMOTE_DS, "  period: ${dto.period}")
            logger.d(TAG_REPORTS_REMOTE_DS, "  overview (field 'overview'): ${dto.overview}")
            logger.d(TAG_REPORTS_REMOTE_DS, "  summary (field 'summary'): ${dto.summary}")
            logger.d(TAG_REPORTS_REMOTE_DS, "  financialOverview (resolved): ${dto.financialOverview}")
            dto.financialOverview?.let { ov ->
                logger.d(TAG_REPORTS_REMOTE_DS, "    totalRevenue=${ov.totalRevenue}, totalExpenses=${ov.totalExpenses}")
                logger.d(TAG_REPORTS_REMOTE_DS, "    grossProfit=${ov.grossProfit}, netProfit=${ov.netProfit}")
                logger.d(TAG_REPORTS_REMOTE_DS, "    profitMarginPercentage=${ov.profitMarginPercentage}, profitMargin=${ov.profitMargin}")
                logger.d(TAG_REPORTS_REMOTE_DS, "    status=${ov.status}, profitStatus=${ov.profitStatus}")
                logger.d(TAG_REPORTS_REMOTE_DS, "    effectiveProfit=${ov.effectiveProfit}, effectiveMargin=${ov.effectiveMargin}")
                logger.d(TAG_REPORTS_REMOTE_DS, "    effectiveStatus=${ov.effectiveStatus}, effectiveIsProfitable=${ov.effectiveIsProfitable}")
            }
            logger.d(TAG_REPORTS_REMOTE_DS, "  fleetSummary: ${dto.fleetSummary}")
            logger.d(TAG_REPORTS_REMOTE_DS, "  tripSummary: ${dto.tripSummary}")
            logger.d(TAG_REPORTS_REMOTE_DS, "  expenseBreakdown keys: ${dto.expenseBreakdown?.keys}")
        }
        return result.data
    }
}
