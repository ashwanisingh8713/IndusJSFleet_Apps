package com.indusjs.fleet.data.datasource.costs

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.costs.*
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Remote data source interface for cost operations.
 */
interface CostsRemoteDataSource : RemoteDataSource {
    suspend fun createTripCost(token: String, request: CreateTripCostRequest): TripCostApiResponse
    suspend fun bulkCreateTripCosts(token: String, tripId: String, request: BulkCreateTripCostsRequest): BulkTripCostsApiResponse
    suspend fun getTripCosts(token: String, tripId: String): TripCostsListApiResponse
    suspend fun getTripCostSummary(token: String, tripId: String): TripCostSummaryApiResponse
    suspend fun createMaintenanceCost(token: String, request: CreateMaintenanceCostRequest): MaintenanceCostApiResponse
    suspend fun bulkCreateMaintenanceCosts(token: String, vehicleId: String, request: BulkCreateMaintenanceCostsRequest): BulkMaintenanceCostsApiResponse
    suspend fun getMaintenanceCosts(token: String, vehicleId: String): MaintenanceCostsListApiResponse
}

/**
 * Implementation of CostsRemoteDataSource using Ktor.
 */
@Inject
class CostsRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : CostsRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL
    private val log = Logger.withTag("CostsRemoteDataSource")

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun createTripCost(token: String, request: CreateTripCostRequest): TripCostApiResponse {
        return try {
            log.d { "Creating trip cost for trip: ${request.tripId}" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.TRIP_COSTS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleTripCostResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to create trip cost: ${e.message}" }
            TripCostApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun bulkCreateTripCosts(
        token: String,
        tripId: String,
        request: BulkCreateTripCostsRequest
    ): BulkTripCostsApiResponse {
        return try {
            log.d { "Bulk creating ${request.costs.size} trip costs for trip: $tripId" }
            val response: HttpResponse = httpClient.post("$baseUrl/trips/$tripId/costs/bulk") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleBulkTripCostsResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to bulk create trip costs: ${e.message}" }
            BulkTripCostsApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getTripCosts(token: String, tripId: String): TripCostsListApiResponse {
        return try {
            log.d { "Fetching costs for trip: $tripId" }
            val response: HttpResponse = httpClient.get("$baseUrl/trips/$tripId/costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleTripCostsListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trip costs: ${e.message}" }
            TripCostsListApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getTripCostSummary(token: String, tripId: String): TripCostSummaryApiResponse {
        return try {
            log.d { "Fetching cost summary for trip: $tripId" }
            val response: HttpResponse = httpClient.get("$baseUrl/trips/$tripId/costs/summary") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleTripCostSummaryResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trip cost summary: ${e.message}" }
            TripCostSummaryApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun createMaintenanceCost(token: String, request: CreateMaintenanceCostRequest): MaintenanceCostApiResponse {
        return try {
            log.d { "Creating maintenance cost for vehicle: ${request.vehicleId}" }
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.MAINTENANCE_COSTS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleMaintenanceCostResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to create maintenance cost: ${e.message}" }
            MaintenanceCostApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun bulkCreateMaintenanceCosts(
        token: String,
        vehicleId: String,
        request: BulkCreateMaintenanceCostsRequest
    ): BulkMaintenanceCostsApiResponse {
        return try {
            log.d { "Bulk creating ${request.costs.size} maintenance costs for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.post("$baseUrl/vehicles/$vehicleId/maintenance-costs/bulk") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleBulkMaintenanceCostsResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to bulk create maintenance costs: ${e.message}" }
            BulkMaintenanceCostsApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getMaintenanceCosts(token: String, vehicleId: String): MaintenanceCostsListApiResponse {
        return try {
            log.d { "Fetching maintenance costs for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.get("$baseUrl/vehicles/$vehicleId/maintenance-costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleMaintenanceCostsListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch maintenance costs: ${e.message}" }
            MaintenanceCostsListApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    private suspend fun handleTripCostResponse(response: HttpResponse): TripCostApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<TripCostApiResponse>(responseBody)
            } else {
                TripCostApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            TripCostApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleTripCostsListResponse(response: HttpResponse): TripCostsListApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<TripCostsListApiResponse>(responseBody)
            } else {
                TripCostsListApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            TripCostsListApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleTripCostSummaryResponse(response: HttpResponse): TripCostSummaryApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<TripCostSummaryApiResponse>(responseBody)
            } else {
                TripCostSummaryApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            TripCostSummaryApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleMaintenanceCostResponse(response: HttpResponse): MaintenanceCostApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<MaintenanceCostApiResponse>(responseBody)
            } else {
                MaintenanceCostApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            MaintenanceCostApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleMaintenanceCostsListResponse(response: HttpResponse): MaintenanceCostsListApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<MaintenanceCostsListApiResponse>(responseBody)
            } else {
                MaintenanceCostsListApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            MaintenanceCostsListApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleBulkTripCostsResponse(response: HttpResponse): BulkTripCostsApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<BulkTripCostsApiResponse>(responseBody)
            } else {
                BulkTripCostsApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            BulkTripCostsApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    private suspend fun handleBulkMaintenanceCostsResponse(response: HttpResponse): BulkMaintenanceCostsApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<BulkMaintenanceCostsApiResponse>(responseBody)
            } else {
                BulkMaintenanceCostsApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            BulkMaintenanceCostsApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }
}
