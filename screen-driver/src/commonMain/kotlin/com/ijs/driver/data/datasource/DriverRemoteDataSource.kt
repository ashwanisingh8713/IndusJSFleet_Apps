package com.ijs.driver.data.datasource

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.ijs.driver.data.model.CreateDriverRequest
import com.ijs.driver.data.model.DriverApiResponse
import com.ijs.driver.data.model.DriverDto
import com.ijs.driver.data.model.DriverListDataDto
import com.ijs.driver.data.model.UpdateDriverRequest
import com.ijs.driver.data.model.UpdateDriverStatusRequest
import com.indusjs.fleet.data.model.history.DriverHistoryApiResponse
import com.indusjs.fleet.data.model.state.StateHistoryResponseDto
import com.indusjs.fleet.data.model.state.StatusUpdateRequestDto
import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.driver.TAG_DRIVER_REMOTE_DS
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Interface for remote driver data operations.
 */
interface DriverRemoteDataSource : RemoteDataSource {
    suspend fun getDrivers(
        token: String,
        status: String? = null,
        search: String? = null,
        page: Int = 1,
        perPage: Int = 50
    ): DriverApiResponse<List<DriverDto>>

    suspend fun getAvailableDrivers(token: String): DriverApiResponse<List<DriverDto>>
    suspend fun getDriverById(token: String, id: String): DriverApiResponse<DriverDto>
    suspend fun createDriver(token: String, request: CreateDriverRequest): DriverApiResponse<DriverDto>
    suspend fun updateDriver(token: String, id: String, request: UpdateDriverRequest): DriverApiResponse<DriverDto>
    suspend fun updateDriverStatus(token: String, id: String, status: String): DriverApiResponse<DriverDto>
    suspend fun toggleDriverActive(token: String, id: String): DriverApiResponse<DriverDto>
    suspend fun deleteDriver(token: String, id: String): DriverApiResponse<Unit>

    // Status update with reason and notes
    suspend fun updateDriverStatusWithReason(
        token: String,
        id: String,
        status: String,
        reason: String? = null,
        notes: String? = null
    ): DriverApiResponse<DriverDto>

    // History API
    suspend fun getDriverHistory(token: String, id: String, page: Int, perPage: Int): DriverHistoryApiResponse

    // State History API
    suspend fun getDriverStateHistory(token: String, id: String, page: Int, perPage: Int): DriverApiResponse<StateHistoryResponseDto>
}

/**
 * Implementation of DriverRemoteDataSource using Ktor.
 */
@Inject
class DriverRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val logger: FleetLogger
) : DriverRemoteDataSource {

    private val baseUrl = "${ApiConfig.BASE_URL}/drivers"

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false // Don't include null values in JSON output
    }

    override suspend fun getDrivers(
        token: String,
        status: String?,
        search: String?,
        page: Int,
        perPage: Int
    ): DriverApiResponse<List<DriverDto>> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Fetching drivers - status: $status, search: $search")
            val response: HttpResponse = httpClient.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
                status?.let { parameter("status", it) }
                search?.let { parameter("search", it) }
            }
            parseListResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to fetch drivers: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getAvailableDrivers(token: String): DriverApiResponse<List<DriverDto>> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Fetching available drivers")
            val response: HttpResponse = httpClient.get("$baseUrl/available") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseListResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to fetch available drivers: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getDriverById(token: String, id: String): DriverApiResponse<DriverDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Fetching driver: $id")
            val response: HttpResponse = httpClient.get("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to fetch driver: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun createDriver(
        token: String,
        request: CreateDriverRequest
    ): DriverApiResponse<DriverDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Creating driver: ${request.firstName} ${request.lastName}")
            val response: HttpResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val result = parseSingleResponse(response)
            logger.d(TAG_DRIVER_REMOTE_DS, "Create driver response - success: ${result.success}, message: ${result.message}")
            result
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to create driver: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateDriver(
        token: String,
        id: String,
        request: UpdateDriverRequest
    ): DriverApiResponse<DriverDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Updating driver: $id")
            val response: HttpResponse = httpClient.put("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to update driver: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateDriverStatus(
        token: String,
        id: String,
        status: String
    ): DriverApiResponse<DriverDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Updating driver status: $id -> $status")
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/status") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(UpdateDriverStatusRequest(status))
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to update driver status: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun toggleDriverActive(token: String, id: String): DriverApiResponse<DriverDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Toggling driver active state: $id")
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/toggle-active") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to toggle driver active: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun deleteDriver(token: String, id: String): DriverApiResponse<Unit> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Deleting driver: $id")
            val response: HttpResponse = httpClient.delete("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseDeleteResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to delete driver: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    private suspend fun parseListResponse(response: HttpResponse): DriverApiResponse<List<DriverDto>> {
        val body = response.bodyAsText()
        logger.d(TAG_DRIVER_REMOTE_DS, "Response: ${response.status}, body length: ${body.length}")

        return try {
            if (response.status.isSuccess()) {
                parseSuccessfulListResponse(body)
            } else {
                val errorMessage = ApiErrorHandler.extractErrorMessage(response.status, body)
                DriverApiResponse(
                    success = false,
                    message = errorMessage
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to parse list response: ${e.message}", e)
            DriverApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private fun parseSuccessfulListResponse(body: String): DriverApiResponse<List<DriverDto>> {
        val pagedResponse = runCatching {
            json.decodeFromString<DriverApiResponse<DriverListDataDto>>(body)
        }.getOrNull()

        if (pagedResponse?.data != null) {
            val data = pagedResponse.data
            return DriverApiResponse(
                success = pagedResponse.success,
                message = pagedResponse.message,
                data = data.items,
                pagination = data.toPagination()
            )
        }

        return json.decodeFromString<DriverApiResponse<List<DriverDto>>>(body)
    }

    private suspend fun parseSingleResponse(response: HttpResponse): DriverApiResponse<DriverDto> {
        val body = response.bodyAsText()
        logger.d(TAG_DRIVER_REMOTE_DS, "Response: ${response.status}, body: $body")

        return try {
            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString<DriverApiResponse<DriverDto>>(body)
                parsed
            } else {
                val errorMessage = ApiErrorHandler.extractErrorMessage(response.status, body)

                logger.e(TAG_DRIVER_REMOTE_DS, "API error: $errorMessage")
                DriverApiResponse(
                    success = false,
                    message = errorMessage
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to parse single response: ${e.message}", e)
            DriverApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun parseDeleteResponse(response: HttpResponse): DriverApiResponse<Unit> {
        val body = response.bodyAsText()
        logger.d(TAG_DRIVER_REMOTE_DS, "Delete response: ${response.status}")

        return try {
            if (response.status.isSuccess()) {
                DriverApiResponse(success = true, message = "Driver deleted successfully")
            } else {
                val errorResponse = try {
                    json.decodeFromString<DriverApiResponse<Unit>>(body)
                } catch (e: Exception) {
                    null
                }
                DriverApiResponse(
                    success = false,
                    message = errorResponse?.message ?: "Request failed with status ${response.status}"
                )
            }
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to parse delete response: ${e.message}", e)
            DriverApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    override suspend fun getDriverHistory(
        token: String,
        id: String,
        page: Int,
        perPage: Int
    ): DriverHistoryApiResponse {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Fetching driver history for id: $id, page: $page")
            val response: HttpResponse = httpClient.get("$baseUrl/$id/history") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
            }
            val bodyText = response.bodyAsText()
            logger.d(TAG_DRIVER_REMOTE_DS, "Driver history response: $bodyText")
            json.decodeFromString<DriverHistoryApiResponse>(bodyText)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to fetch driver history: ${e.message}", e)
            DriverHistoryApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(e))
        }
    }

    override suspend fun updateDriverStatusWithReason(
        token: String,
        id: String,
        status: String,
        reason: String?,
        notes: String?
    ): DriverApiResponse<DriverDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Updating driver status: $id -> $status")
            val request = StatusUpdateRequestDto(
                status = status,
                reason = reason,
                notes = notes
            )
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/status") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to update driver status: ${e.message}", e)
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getDriverStateHistory(
        token: String,
        id: String,
        page: Int,
        perPage: Int
    ): DriverApiResponse<StateHistoryResponseDto> {
        return try {
            logger.d(TAG_DRIVER_REMOTE_DS, "Fetching driver state history: $id, page=$page")
            val response: HttpResponse = httpClient.get("$baseUrl/$id/state-history") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
            }
            val bodyText = response.bodyAsText()
            logger.d(TAG_DRIVER_REMOTE_DS, "State history response: $bodyText")
            if (response.status.isSuccess()) {
                json.decodeFromString<DriverApiResponse<StateHistoryResponseDto>>(bodyText)
            } else {
                DriverApiResponse(success = false, message = "Request failed with status ${response.status}")
            }
        } catch (e: Exception) {
            logger.e(TAG_DRIVER_REMOTE_DS, "Failed to fetch driver state history: ${e.message}", e)
            DriverApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(e))
        }
    }
}

