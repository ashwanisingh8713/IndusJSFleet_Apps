package com.indusjs.fleet.data.datasource.driver

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.driver.CreateDriverRequest
import com.indusjs.fleet.data.model.driver.DriverApiResponse
import com.indusjs.fleet.data.model.driver.DriverDto
import com.indusjs.fleet.data.model.driver.UpdateDriverRequest
import com.indusjs.fleet.data.model.driver.UpdateDriverStatusRequest
import com.indusjs.fleet.data.model.history.DriverHistoryApiResponse
import co.touchlab.kermit.Logger
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

    // History API
    suspend fun getDriverHistory(token: String, id: String, page: Int, perPage: Int): DriverHistoryApiResponse
}

/**
 * Implementation of DriverRemoteDataSource using Ktor.
 */
@Inject
class DriverRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : DriverRemoteDataSource {

    private val baseUrl = "${ApiConfig.BASE_URL}/drivers"
    private val log = Logger.withTag("DriverRemoteDataSource")

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
            log.d { "Fetching drivers - status: $status, search: $search" }
            val response: HttpResponse = httpClient.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
                status?.let { parameter("status", it) }
                search?.let { parameter("search", it) }
            }
            parseListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch drivers: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getAvailableDrivers(token: String): DriverApiResponse<List<DriverDto>> {
        return try {
            log.d { "Fetching available drivers" }
            val response: HttpResponse = httpClient.get("$baseUrl/available") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch available drivers: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getDriverById(token: String, id: String): DriverApiResponse<DriverDto> {
        return try {
            log.d { "Fetching driver: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch driver: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun createDriver(
        token: String,
        request: CreateDriverRequest
    ): DriverApiResponse<DriverDto> {
        return try {
            log.d { "Creating driver: ${request.firstName} ${request.lastName}" }
            log.d { "Request body: $request" }
            val response: HttpResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val result = parseSingleResponse(response)
            log.d { "Create driver response - success: ${result.success}, message: ${result.message}" }
            result
        } catch (e: Exception) {
            log.e(e) { "Failed to create driver: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateDriver(
        token: String,
        id: String,
        request: UpdateDriverRequest
    ): DriverApiResponse<DriverDto> {
        return try {
            log.d { "Updating driver: $id" }
            val response: HttpResponse = httpClient.put("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update driver: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateDriverStatus(
        token: String,
        id: String,
        status: String
    ): DriverApiResponse<DriverDto> {
        return try {
            log.d { "Updating driver status: $id -> $status" }
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/status") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(UpdateDriverStatusRequest(status))
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update driver status: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun toggleDriverActive(token: String, id: String): DriverApiResponse<DriverDto> {
        return try {
            log.d { "Toggling driver active state: $id" }
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/toggle-active") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to toggle driver active: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun deleteDriver(token: String, id: String): DriverApiResponse<Unit> {
        return try {
            log.d { "Deleting driver: $id" }
            val response: HttpResponse = httpClient.delete("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseDeleteResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to delete driver: ${e.message}" }
            DriverApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    private suspend fun parseListResponse(response: HttpResponse): DriverApiResponse<List<DriverDto>> {
        val body = response.bodyAsText()
        log.d { "Response: ${response.status}, body length: ${body.length}" }

        return try {
            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString<DriverApiResponse<List<DriverDto>>>(body)
                parsed
            } else {
                val errorResponse = try {
                    json.decodeFromString<DriverApiResponse<List<DriverDto>>>(body)
                } catch (e: Exception) {
                    null
                }
                DriverApiResponse(
                    success = false,
                    message = errorResponse?.message ?: "Request failed with status ${response.status}"
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse list response: ${e.message}" }
            DriverApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    private suspend fun parseSingleResponse(response: HttpResponse): DriverApiResponse<DriverDto> {
        val body = response.bodyAsText()
        log.d { "Response: ${response.status}, body: $body" }

        return try {
            if (response.status.isSuccess()) {
                val parsed = json.decodeFromString<DriverApiResponse<DriverDto>>(body)
                parsed
            } else {
                // Extract the best error message from response
                val errorMessage = tryExtractErrorMessage(body)
                    ?: "Request failed with status ${response.status}"

                log.e { "API error: $errorMessage" }
                DriverApiResponse(
                    success = false,
                    message = errorMessage
                )
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to parse single response: ${e.message}" }
            DriverApiResponse(success = false, message = "Failed to parse response: ${e.message}")
        }
    }

    /**
     * Try to extract error message from response body when standard parsing fails.
     * Uses centralized ApiErrorHandler for consistent error messages.
     */
    private fun tryExtractErrorMessage(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val jsonElement = json.parseToJsonElement(body)
            val jsonObject = jsonElement as? kotlinx.serialization.json.JsonObject ?: return null

            // Try message field first
            val messageField = jsonObject["message"]?.toString()?.trim('"')
            if (!messageField.isNullOrBlank() && messageField != "null") {
                return messageField
            }

            // Try error field with DB constraint parsing
            val errorField = jsonObject["error"]?.toString()?.trim('"')
            if (!errorField.isNullOrBlank() && errorField != "null") {
                return ApiErrorHandler.parseDbConstraintError(errorField)
            }

            // Try detail field
            val detailField = jsonObject["detail"]?.toString()?.trim('"')
            if (!detailField.isNullOrBlank() && detailField != "null") {
                return detailField
            }

            null
        } catch (e: Exception) {
            null
        }
    }


    private suspend fun parseDeleteResponse(response: HttpResponse): DriverApiResponse<Unit> {
        val body = response.bodyAsText()
        log.d { "Delete response: ${response.status}" }

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
            log.e(e) { "Failed to parse delete response: ${e.message}" }
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
            log.d { "Fetching driver history for id: $id, page: $page" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/history") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
            }
            val bodyText = response.bodyAsText()
            log.d { "Driver history response: $bodyText" }
            json.decodeFromString<DriverHistoryApiResponse>(bodyText)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch driver history: ${e.message}" }
            DriverHistoryApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(e))
        }
    }
}

