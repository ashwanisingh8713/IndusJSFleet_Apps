package com.indusjs.fleet.data.datasource.reports

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.model.reports.*
import dev.zacsweers.metro.Inject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

/**
 * Remote data source for Profit & Loss APIs
 */
@Inject
class ReportsRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json
) {
    private val log = Logger.withTag("ReportsRemoteDataSource")

    /**
     * Get single trip profit/loss
     * GET /trips/{trip_id}/profit-loss
     */
    suspend fun getTripProfitLoss(token: String, tripId: Int): TripProfitLossDto? {
        val url = "${ApiConfig.BASE_URL}/trips/$tripId/profit-loss"
        log.d { "Fetching trip P&L: $tripId" }

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<TripProfitLossDto>>(body)
            result.data
        } else {
            log.e { "Failed to fetch trip P&L: ${response.status}" }
            null
        }
    }

    /**
     * Get single vehicle profit/loss
     * GET /vehicles/{vehicle_id}/profit-loss?period=monthly
     */
    suspend fun getVehicleProfitLoss(token: String, vehicleId: Int, period: String = "monthly"): VehicleProfitLossDto? {
        val url = "${ApiConfig.BASE_URL}/vehicles/$vehicleId/profit-loss"
        log.d { "Fetching vehicle P&L: $vehicleId, period: $period" }

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("period", period)
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<VehicleProfitLossDto>>(body)
            result.data
        } else {
            log.e { "Failed to fetch vehicle P&L: ${response.status}" }
            null
        }
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
        log.d { "Fetching fleet P&L: period=$period, startDate=$startDate, endDate=$endDate" }

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            period?.let { parameter("period", it) }
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<FleetProfitLossDto>>(body)
            result.data
        } else {
            log.e { "Failed to fetch fleet P&L: ${response.status}" }
            null
        }
    }

    /**
     * Get multi-vehicle profit/loss
     * POST /reports/profit-loss/vehicles
     */
    suspend fun getMultiVehicleProfitLoss(token: String, request: MultiVehiclePLRequest): List<VehicleProfitLossDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/vehicles"
        log.d { "Fetching multi-vehicle P&L: ${request.vehicleIds}" }

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiVehiclePLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<List<VehicleProfitLossDto>>>(body)
            result.data
        } else {
            log.e { "Failed to fetch multi-vehicle P&L: ${response.status}" }
            null
        }
    }

    /**
     * Get multi-trip profit/loss
     * POST /reports/profit-loss/trips
     */
    suspend fun getMultiTripProfitLoss(token: String, request: MultiTripPLRequest): List<TripProfitLossDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/trips"
        log.d { "Fetching multi-trip P&L: ${request.tripIds ?: "vehicle ${request.vehicleId}"}" }

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiTripPLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<List<TripProfitLossDto>>>(body)
            result.data
        } else {
            log.e { "Failed to fetch multi-trip P&L: ${response.status}" }
            null
        }
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
        log.d { "Fetching cost type analysis: $costType" }

        val response: HttpResponse = httpClient.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            startDate?.let { parameter("start_date", it) }
            endDate?.let { parameter("end_date", it) }
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<CostTypeAnalysisDto>>(body)
            result.data
        } else {
            log.e { "Failed to fetch cost type analysis: ${response.status}" }
            null
        }
    }

    /**
     * Get multi cost type analysis
     * POST /reports/profit-loss/cost-types
     */
    suspend fun getMultiCostTypeAnalysis(token: String, request: MultiCostTypePLRequest): List<CostTypeAnalysisDto>? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/cost-types"
        log.d { "Fetching multi cost type analysis: ${request.costTypes}" }

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(MultiCostTypePLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<List<CostTypeAnalysisDto>>>(body)
            result.data
        } else {
            log.e { "Failed to fetch multi cost type analysis: ${response.status}" }
            null
        }
    }

    /**
     * Get consolidated P&L report
     * POST /reports/profit-loss/consolidated
     */
    suspend fun getConsolidatedPL(token: String, request: ConsolidatedPLRequest): ConsolidatedPLDto? {
        val url = "${ApiConfig.BASE_URL}/reports/profit-loss/consolidated"
        log.d { "Fetching consolidated P&L report" }

        val response: HttpResponse = httpClient.post(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(ConsolidatedPLRequest.serializer(), request))
        }
        val body = response.bodyAsText()
        log.d { "Response: $body" }

        return if (response.status.isSuccess()) {
            val result = json.decodeFromString<ProfitLossResponse<ConsolidatedPLDto>>(body)
            result.data
        } else {
            log.e { "Failed to fetch consolidated P&L: ${response.status}" }
            null
        }
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
        log.d { "Fetching P&L summary: startDate=$startDate, endDate=$endDate" }

        return try {
            val response: HttpResponse = httpClient.get(url) {
                header(HttpHeaders.Authorization, "Bearer $token")
                startDate?.let { parameter("start_date", it) }
                endDate?.let { parameter("end_date", it) }
            }
            val body = response.bodyAsText()
            log.d { "Response status: ${response.status}" }
            log.d { "Response body: $body" }

            if (response.status.isSuccess()) {
                try {
                    val result = json.decodeFromString<ProfitLossResponse<PLSummaryDto>>(body)
                    result.data
                } catch (e: Exception) {
                    log.e { "Failed to parse P&L summary response: ${e.message}" }
                    log.e { "Response was: $body" }
                    null
                }
            } else {
                log.e { "Failed to fetch P&L summary: ${response.status}" }
                null
            }
        } catch (e: Exception) {
            log.e { "Error fetching P&L summary: ${e.message}" }
            null
        }
    }
}
