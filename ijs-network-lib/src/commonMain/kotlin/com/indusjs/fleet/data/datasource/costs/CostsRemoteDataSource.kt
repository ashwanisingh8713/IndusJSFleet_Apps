package com.indusjs.fleet.data.datasource.costs

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.network.TAG_COSTS_REMOTE_DS
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.costs.*
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
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
    // Cost Types APIs (no auth required for initial fetch)
    suspend fun getTripCostTypes(): CostTypesApiResponse
    suspend fun getMaintenanceCostTypes(): CostTypesApiResponse
    suspend fun getDriverCostTypes(): CostTypesApiResponse

    suspend fun createTripCost(token: String, request: CreateTripCostRequest): TripCostApiResponse
    suspend fun bulkCreateTripCosts(token: String, tripId: String, request: BulkCreateTripCostsRequest): BulkTripCostsApiResponse
    suspend fun getTripCosts(token: String, tripId: String): TripCostsListApiResponse
    suspend fun getTripCostSummary(token: String, tripId: String): TripCostSummaryApiResponse
    suspend fun createMaintenanceCost(token: String, request: CreateMaintenanceCostRequest): MaintenanceCostApiResponse
    suspend fun bulkCreateMaintenanceCosts(token: String, vehicleId: String, request: BulkCreateMaintenanceCostsRequest): BulkMaintenanceCostsApiResponse
    suspend fun getMaintenanceCosts(token: String, vehicleId: String): MaintenanceCostsListApiResponse

    // Vehicle Costs APIs
    suspend fun getVehicleTripCosts(
        token: String,
        vehicleId: String,
        page: Int,
        perPage: Int,
        costType: String?,
        startDate: String?,
        endDate: String?,
        sortBy: String,
        sortOrder: String
    ): VehicleTripCostsApiResponse

    suspend fun getVehicleMaintenanceCosts(
        token: String,
        vehicleId: String,
        page: Int,
        perPage: Int,
        costType: String?,
        startDate: String?,
        endDate: String?,
        sortBy: String,
        sortOrder: String
    ): VehicleMaintenanceCostsApiResponse

    suspend fun deleteTripCost(token: String, costId: String): DeleteCostApiResponse
    suspend fun deleteMaintenanceCost(token: String, costId: String): DeleteCostApiResponse

    // Driver Costs APIs
    suspend fun getDriverCosts(
        token: String,
        driverId: String,
        page: Int,
        perPage: Int,
        groupId: String?,
        month: String?,
        startDate: String?,
        endDate: String?
    ): com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse

    suspend fun createDriverCost(
        token: String,
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.CreateDriverCostRequest
    ): com.indusjs.fleet.data.model.driver.DriverCostApiResponse

    suspend fun bulkCreateDriverCosts(
        token: String,
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.BulkCreateDriverCostsRequest
    ): com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse
}

/**
 * Implementation of CostsRemoteDataSource using Ktor.
 */
@Inject
class CostsRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : CostsRemoteDataSource {

    private val baseUrl = ApiConfig.BASE_URL

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun getTripCostTypes(): CostTypesApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching trip cost types")
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.TRIP_COST_TYPES}")
            handleCostTypesResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch trip cost types: ${e.message}", e)
            CostTypesApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getMaintenanceCostTypes(): CostTypesApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching maintenance cost types")
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.MAINTENANCE_COST_TYPES}")
            handleCostTypesResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch maintenance cost types: ${e.message}", e)
            CostTypesApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getDriverCostTypes(): CostTypesApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching driver cost types")
            val response: HttpResponse = httpClient.get("$baseUrl${ApiConfig.Endpoints.DRIVER_COST_TYPES}")
            handleCostTypesResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch driver cost types: ${e.message}", e)
            CostTypesApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    private suspend fun handleCostTypesResponse(response: HttpResponse): CostTypesApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<CostTypesApiResponse>(responseBody)
            } else {
                CostTypesApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to parse cost types response: ${e.message}", e)
            CostTypesApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    override suspend fun createTripCost(token: String, request: CreateTripCostRequest): TripCostApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Creating trip cost for trip: ${request.tripId}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.TRIP_COSTS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleTripCostResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to create trip cost: ${e.message}", e)
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
            logger.d(TAG_COSTS_REMOTE_DS, "Bulk creating ${request.costs.size} trip costs for trip: $tripId")
            val response: HttpResponse = httpClient.post("$baseUrl/trips/$tripId/costs/bulk") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleBulkTripCostsResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to bulk create trip costs: ${e.message}", e)
            BulkTripCostsApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getTripCosts(token: String, tripId: String): TripCostsListApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching costs for trip: $tripId")
            val response: HttpResponse = httpClient.get("$baseUrl/trips/$tripId/costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleTripCostsListResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch trip costs: ${e.message}", e)
            TripCostsListApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getTripCostSummary(token: String, tripId: String): TripCostSummaryApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching cost summary for trip: $tripId")
            val response: HttpResponse = httpClient.get("$baseUrl/trips/$tripId/costs/summary") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleTripCostSummaryResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch trip cost summary: ${e.message}", e)
            TripCostSummaryApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun createMaintenanceCost(token: String, request: CreateMaintenanceCostRequest): MaintenanceCostApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Creating maintenance cost for vehicle: ${request.vehicleId}")
            val response: HttpResponse = httpClient.post("$baseUrl${ApiConfig.Endpoints.MAINTENANCE_COSTS}") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleMaintenanceCostResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to create maintenance cost: ${e.message}", e)
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
            logger.d(TAG_COSTS_REMOTE_DS, "Bulk creating ${request.costs.size} maintenance costs for vehicle: $vehicleId")
            val response: HttpResponse = httpClient.post("$baseUrl/vehicles/$vehicleId/maintenance-costs/bulk") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleBulkMaintenanceCostsResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to bulk create maintenance costs: ${e.message}", e)
            BulkMaintenanceCostsApiResponse(
                success = false,
                message = e.message ?: "Network error occurred"
            )
        }
    }

    override suspend fun getMaintenanceCosts(token: String, vehicleId: String): MaintenanceCostsListApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching maintenance costs for vehicle: $vehicleId")
            val response: HttpResponse = httpClient.get("$baseUrl/vehicles/$vehicleId/maintenance-costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleMaintenanceCostsListResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch maintenance costs: ${e.message}", e)
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

    // ==================== Vehicle Costs API Implementations ====================

    override suspend fun getVehicleTripCosts(
        token: String,
        vehicleId: String,
        page: Int,
        perPage: Int,
        costType: String?,
        startDate: String?,
        endDate: String?,
        sortBy: String,
        sortOrder: String
    ): VehicleTripCostsApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching trip costs for vehicle: $vehicleId, page: $page, costId: $costType")
            val response: HttpResponse = httpClient.get("$baseUrl/vehicles/$vehicleId/trip-costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url {
                    parameters.append("page", page.toString())
                    parameters.append("per_page", perPage.toString())
                    // API uses cost_id parameter for filtering
                    costType?.let { parameters.append("cost_id", it) }
                    startDate?.let { parameters.append("start_date", it) }
                    endDate?.let { parameters.append("end_date", it) }
                    parameters.append("sort_by", sortBy)
                    parameters.append("sort_order", sortOrder)
                }
            }
            handleVehicleTripCostsResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch vehicle trip costs: ${e.message}", e)
            VehicleTripCostsApiResponse(success = false, message = e.message ?: "Network error")
        }
    }

    override suspend fun getVehicleMaintenanceCosts(
        token: String,
        vehicleId: String,
        page: Int,
        perPage: Int,
        costType: String?,
        startDate: String?,
        endDate: String?,
        sortBy: String,
        sortOrder: String
    ): VehicleMaintenanceCostsApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching maintenance costs for vehicle: $vehicleId, page: $page, costId: $costType")
            val response: HttpResponse = httpClient.get("$baseUrl/vehicles/$vehicleId/maintenance-costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url {
                    parameters.append("page", page.toString())
                    parameters.append("per_page", perPage.toString())
                    // API uses cost_id parameter for filtering
                    costType?.let { parameters.append("cost_id", it) }
                    startDate?.let { parameters.append("start_date", it) }
                    endDate?.let { parameters.append("end_date", it) }
                    parameters.append("sort_by", sortBy)
                    parameters.append("sort_order", sortOrder)
                }
            }
            handleVehicleMaintenanceCostsResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch vehicle maintenance costs: ${e.message}", e)
            VehicleMaintenanceCostsApiResponse(success = false, message = e.message ?: "Network error")
        }
    }

    override suspend fun deleteTripCost(token: String, costId: String): DeleteCostApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Deleting trip cost: $costId")
            val response: HttpResponse = httpClient.delete("$baseUrl/trip-costs/$costId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleDeleteCostResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to delete trip cost: ${e.message}", e)
            DeleteCostApiResponse(success = false, message = e.message ?: "Network error")
        }
    }

    override suspend fun deleteMaintenanceCost(token: String, costId: String): DeleteCostApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Deleting maintenance cost: $costId")
            val response: HttpResponse = httpClient.delete("$baseUrl/maintenance-costs/$costId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            handleDeleteCostResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to delete maintenance cost: ${e.message}", e)
            DeleteCostApiResponse(success = false, message = e.message ?: "Network error")
        }
    }

    private suspend fun handleVehicleTripCostsResponse(response: HttpResponse): VehicleTripCostsApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<VehicleTripCostsApiResponse>(responseBody)
            } else {
                VehicleTripCostsApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            VehicleTripCostsApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleVehicleMaintenanceCostsResponse(response: HttpResponse): VehicleMaintenanceCostsApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<VehicleMaintenanceCostsApiResponse>(responseBody)
            } else {
                VehicleMaintenanceCostsApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            VehicleMaintenanceCostsApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun handleDeleteCostResponse(response: HttpResponse): DeleteCostApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<DeleteCostApiResponse>(responseBody)
            } else {
                DeleteCostApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            DeleteCostApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    // ==================== Driver Costs Implementation ====================

    override suspend fun getDriverCosts(
        token: String,
        driverId: String,
        page: Int,
        perPage: Int,
        groupId: String?,
        month: String?,
        startDate: String?,
        endDate: String?
    ): com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Fetching driver costs for driver: $driverId, page: $page")
            val response: HttpResponse = httpClient.get("$baseUrl/drivers/$driverId/costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url {
                    parameters.append("page", page.toString())
                    parameters.append("per_page", perPage.toString())
                    groupId?.let { parameters.append("group_id", it) }
                    month?.let { parameters.append("month", it) }
                    startDate?.let { parameters.append("start_date", it) }
                    endDate?.let { parameters.append("end_date", it) }
                }
            }
            handleDriverCostsResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to fetch driver costs: ${e.message}", e)
            com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse(
                success = false,
                message = e.message ?: "Network error"
            )
        }
    }

    private suspend fun handleDriverCostsResponse(response: HttpResponse): com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse>(responseBody)
            } else {
                com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to parse driver costs response: ${e.message}", e)
            com.indusjs.fleet.data.model.driver.DriverCostsListApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    override suspend fun createDriverCost(
        token: String,
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.CreateDriverCostRequest
    ): com.indusjs.fleet.data.model.driver.DriverCostApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Creating driver cost for driver: $driverId, trip: ${request.tripId}")
            val response: HttpResponse = httpClient.post("$baseUrl/drivers/$driverId/costs") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleDriverCostResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to create driver cost: ${e.message}", e)
            com.indusjs.fleet.data.model.driver.DriverCostApiResponse(
                success = false,
                message = e.message ?: "Network error"
            )
        }
    }

    private suspend fun handleDriverCostResponse(response: HttpResponse): com.indusjs.fleet.data.model.driver.DriverCostApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<com.indusjs.fleet.data.model.driver.DriverCostApiResponse>(responseBody)
            } else {
                com.indusjs.fleet.data.model.driver.DriverCostApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to parse driver cost response: ${e.message}", e)
            com.indusjs.fleet.data.model.driver.DriverCostApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }

    override suspend fun bulkCreateDriverCosts(
        token: String,
        driverId: String,
        request: com.indusjs.fleet.data.model.driver.BulkCreateDriverCostsRequest
    ): com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse {
        return try {
            logger.d(TAG_COSTS_REMOTE_DS, "Creating bulk driver costs for driver: $driverId")
            val response: HttpResponse = httpClient.post("$baseUrl/drivers/$driverId/costs/bulk") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(request))
            }
            handleBulkDriverCostsResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to create bulk driver costs: ${e.message}", e)
            com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse(
                success = false,
                message = e.message ?: "Network error"
            )
        }
    }

    private suspend fun handleBulkDriverCostsResponse(response: HttpResponse): com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse {
        val responseBody = response.bodyAsText()
        return try {
            if (response.status.isSuccess()) {
                json.decodeFromString<com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse>(responseBody)
            } else {
                com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse(
                    success = false,
                    message = ApiErrorHandler.extractErrorMessage(response.status, responseBody)
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_COSTS_REMOTE_DS, "Failed to parse bulk driver costs response: ${e.message}", e)
            com.indusjs.fleet.data.model.driver.BulkDriverCostsApiResponse(
                success = false,
                message = "Failed to parse response: ${e.message}"
            )
        }
    }
}
