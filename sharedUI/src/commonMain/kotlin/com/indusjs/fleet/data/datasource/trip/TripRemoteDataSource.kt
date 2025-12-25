package com.indusjs.fleet.data.datasource.trip

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.trip.CreateTripRequest
import com.indusjs.fleet.data.model.trip.TripApiResponse
import com.indusjs.fleet.data.model.trip.TripDto
import com.indusjs.fleet.data.model.trip.UpdateTripRequest
import com.indusjs.fleet.data.model.trip.UpdateTripStateRequest
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
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
 * Interface for remote trip data operations.
 */
interface TripRemoteDataSource : RemoteDataSource {
    suspend fun getTrips(token: String): TripApiResponse<List<TripDto>>
    suspend fun getTripById(token: String, id: String): TripApiResponse<TripDto>
    suspend fun createTrip(token: String, request: CreateTripRequest): TripApiResponse<TripDto>
    suspend fun updateTrip(token: String, id: String, request: UpdateTripRequest): TripApiResponse<TripDto>
    suspend fun updateTripStatus(token: String, id: String, status: String): TripApiResponse<TripDto>
    suspend fun cancelTrip(token: String, id: String): TripApiResponse<Unit>
}

/**
 * Implementation of TripRemoteDataSource using Ktor.
 */
@Inject
class TripRemoteDataSourceImpl(
    private val httpClient: HttpClient
) : TripRemoteDataSource {

    private val baseUrl = "${ApiConfig.BASE_URL}/trips"
    private val log = Logger.withTag("TripRemoteDataSource")

    private val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    override suspend fun getTrips(token: String): TripApiResponse<List<TripDto>> {
        return try {
            log.d { "Fetching trips" }
            val response: HttpResponse = httpClient.get(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trips: ${e.message}" }
            TripApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun getTripById(token: String, id: String): TripApiResponse<TripDto> {
        return try {
            log.d { "Fetching trip: $id" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trip: ${e.message}" }
            TripApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun createTrip(token: String, request: CreateTripRequest): TripApiResponse<TripDto> {
        return try {
            log.d { "Creating trip at URL: $baseUrl" }
            log.d { "Request body: $request" }
            val response: HttpResponse = httpClient.post(baseUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            log.d { "Response status: ${response.status}" }
            val bodyText = response.bodyAsText()
            log.d { "Response body: $bodyText" }

            if (response.status.isSuccess()) {
                try {
                    json.decodeFromString<TripApiResponse<TripDto>>(bodyText)
                } catch (e: Exception) {
                    log.e(e) { "Failed to parse response: $bodyText" }
                    TripApiResponse(success = false, message = "Failed to parse response: ${e.message}")
                }
            } else {
                // Try to parse error message from response body
                val errorMessage = try {
                    val errorResponse = json.decodeFromString<TripApiResponse<TripDto>>(bodyText)
                    errorResponse.message ?: "Request failed with status: ${response.status}"
                } catch (e: Exception) {
                    "Request failed with status: ${response.status}. Body: $bodyText"
                }
                TripApiResponse(success = false, message = errorMessage)
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to create trip: ${e.message}" }
            TripApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateTrip(token: String, id: String, request: UpdateTripRequest): TripApiResponse<TripDto> {
        return try {
            log.d { "Updating trip: $id" }
            val response: HttpResponse = httpClient.put("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip: ${e.message}" }
            TripApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun updateTripStatus(token: String, id: String, status: String): TripApiResponse<TripDto> {
        return try {
            log.d { "Updating trip state: $id -> $status" }
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/state") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(UpdateTripStateRequest(state = status))
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip state: ${e.message}" }
            TripApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    override suspend fun cancelTrip(token: String, id: String): TripApiResponse<Unit> {
        return try {
            log.d { "Cancelling trip: $id" }
            val response: HttpResponse = httpClient.delete("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.isSuccess()) {
                TripApiResponse(success = true, message = "Trip cancelled successfully")
            } else {
                TripApiResponse(success = false, message = "Failed to cancel trip")
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to cancel trip: ${e.message}" }
            TripApiResponse(success = false, message = e.message ?: "Network error occurred")
        }
    }

    private suspend fun parseSingleResponse(response: HttpResponse): TripApiResponse<TripDto> {
        val bodyText = response.bodyAsText()
        return if (response.status.isSuccess()) {
            try {
                json.decodeFromString<TripApiResponse<TripDto>>(bodyText)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse response: $bodyText" }
                TripApiResponse(success = false, message = "Failed to parse response")
            }
        } else {
            TripApiResponse(success = false, message = "Request failed with status: ${response.status}")
        }
    }

    private suspend fun parseListResponse(response: HttpResponse): TripApiResponse<List<TripDto>> {
        val bodyText = response.bodyAsText()
        return if (response.status.isSuccess()) {
            try {
                json.decodeFromString<TripApiResponse<List<TripDto>>>(bodyText)
            } catch (e: Exception) {
                log.e(e) { "Failed to parse response: $bodyText" }
                TripApiResponse(success = false, message = "Failed to parse response")
            }
        } else {
            TripApiResponse(success = false, message = "Request failed with status: ${response.status}")
        }
    }
}
