package com.ijs.trip.data.datasource

import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.fleet.core.network.ApiErrorHandler
import com.indusjs.fleet.data.datasource.RemoteDataSource
import com.indusjs.fleet.data.model.state.StateHistoryResponseDto
import com.indusjs.fleet.data.model.state.StateUpdateRequestDto
import com.ijs.trip.data.model.CreateTripRequest
import com.ijs.trip.data.model.CreateTripStopRequest
import com.ijs.trip.data.model.TripApiResponse
import com.ijs.trip.data.model.TripDto
import com.ijs.trip.data.model.TripStopApiResponse
import com.ijs.trip.data.model.TripStopDto
import com.ijs.trip.data.model.TripStopsApiResponse
import com.ijs.trip.data.model.UpdateTripRequest
import com.ijs.trip.data.model.UpdateTripStateRequest
import com.ijs.trip.data.model.UpdateTripStopRequest
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
 * Interface for remote trip data operations.
 */
interface TripRemoteDataSource : RemoteDataSource {
    suspend fun getTrips(token: String): TripApiResponse<List<TripDto>>
    suspend fun getTripById(token: String, id: String): TripApiResponse<TripDto>
    suspend fun createTrip(token: String, request: CreateTripRequest): TripApiResponse<TripDto>
    suspend fun updateTrip(token: String, id: String, request: UpdateTripRequest): TripApiResponse<TripDto>
    suspend fun updateTripStatus(token: String, id: String, status: String): TripApiResponse<TripDto>
    suspend fun updateTripProgress(
        token: String,
        tripId: String,
        coveredDistance: Double?,
        coveredDurationMinutes: Long?,
        currentLat: Double?,
        currentLng: Double?
    ): TripApiResponse<TripDto>
    suspend fun updateTripLocation(token: String, tripId: String, lat: Double, lng: Double): TripApiResponse<TripDto>
    suspend fun cancelTrip(token: String, id: String): TripApiResponse<Unit>
    suspend fun getTripsByVehicleId(token: String, vehicleId: String): TripApiResponse<List<TripDto>>

    // State Management APIs
    suspend fun updateTripState(
        token: String,
        id: String,
        state: String,
        reason: String? = null,
        notes: String? = null
    ): TripApiResponse<TripDto>

    suspend fun getTripStateHistory(
        token: String,
        id: String,
        page: Int,
        perPage: Int
    ): TripApiResponse<StateHistoryResponseDto>

    // Trip Stops
    suspend fun getTripStops(token: String, tripId: String): TripStopsApiResponse
    suspend fun createTripStop(token: String, tripId: String, request: CreateTripStopRequest): TripStopApiResponse
    suspend fun updateTripStop(token: String, tripId: String, stopId: String, request: UpdateTripStopRequest): TripStopApiResponse
    suspend fun markStopCompleted(token: String, tripId: String, stopId: String): TripStopApiResponse
    suspend fun deleteTripStop(token: String, tripId: String, stopId: String): TripApiResponse<Unit>
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
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
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
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
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
                TripApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to create trip: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun updateTrip(token: String, id: String, request: UpdateTripRequest): TripApiResponse<TripDto> {
        return try {
            log.d { "Updating trip: $id" }
            log.d { "Update request - tripPrice: ${request.tripPrice}, weightUnit: ${request.weightUnit}, cargoWeight: ${request.cargoLoadingWeight}" }
            val response: HttpResponse = httpClient.put("$baseUrl/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
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
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun updateTripProgress(
        token: String,
        tripId: String,
        coveredDistance: Double?,
        coveredDurationMinutes: Long?,
        currentLat: Double?,
        currentLng: Double?
    ): TripApiResponse<TripDto> {
        return try {
            log.d { "Updating trip progress: $tripId" }
            val requestBody = buildMap {
                coveredDistance?.let { put("covered_distance", it) }
                coveredDurationMinutes?.let { put("covered_duration_minutes", it) }
                currentLat?.let { put("current_lat", it) }
                currentLng?.let { put("current_lng", it) }
            }
            val response: HttpResponse = httpClient.patch("$baseUrl/$tripId/progress") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip progress: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun updateTripLocation(token: String, tripId: String, lat: Double, lng: Double): TripApiResponse<TripDto> {
        return try {
            log.d { "Updating trip location: $tripId" }
            val requestBody = mapOf(
                "current_lat" to lat,
                "current_lng" to lng
            )
            val response: HttpResponse = httpClient.patch("$baseUrl/$tripId/location") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip location: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
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
                val bodyText = response.bodyAsText()
                TripApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to cancel trip: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
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
            TripApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
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
            TripApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
        }
    }

    override suspend fun getTripsByVehicleId(token: String, vehicleId: String): TripApiResponse<List<TripDto>> {
        return try {
            log.d { "Fetching trips for vehicle: $vehicleId" }
            val response: HttpResponse = httpClient.get("${ApiConfig.BASE_URL}/vehicles/$vehicleId/trips") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            parseListResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trips for vehicle: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    // ============ TRIP STOPS ============

    override suspend fun getTripStops(token: String, tripId: String): TripStopsApiResponse {
        return try {
            log.d { "Fetching stops for trip: $tripId" }
            val response: HttpResponse = httpClient.get("$baseUrl/$tripId/stops") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess()) {
                json.decodeFromString<TripStopsApiResponse>(bodyText)
            } else {
                TripStopsApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trip stops: ${e.message}" }
            TripStopsApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun createTripStop(token: String, tripId: String, request: CreateTripStopRequest): TripStopApiResponse {
        return try {
            log.d { "Creating stop for trip: $tripId" }
            val response: HttpResponse = httpClient.post("$baseUrl/$tripId/stops") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess()) {
                json.decodeFromString<TripStopApiResponse>(bodyText)
            } else {
                TripStopApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to create trip stop: ${e.message}" }
            TripStopApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun updateTripStop(token: String, tripId: String, stopId: String, request: UpdateTripStopRequest): TripStopApiResponse {
        return try {
            log.d { "Updating stop $stopId for trip: $tripId" }
            val response: HttpResponse = httpClient.put("$baseUrl/$tripId/stops/$stopId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess()) {
                json.decodeFromString<TripStopApiResponse>(bodyText)
            } else {
                TripStopApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip stop: ${e.message}" }
            TripStopApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun markStopCompleted(token: String, tripId: String, stopId: String): TripStopApiResponse {
        return try {
            log.d { "Marking stop $stopId as completed for trip: $tripId" }
            val response: HttpResponse = httpClient.patch("$baseUrl/$tripId/stops/$stopId/complete") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess()) {
                json.decodeFromString<TripStopApiResponse>(bodyText)
            } else {
                TripStopApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to mark stop as completed: ${e.message}" }
            TripStopApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun deleteTripStop(token: String, tripId: String, stopId: String): TripApiResponse<Unit> {
        return try {
            log.d { "Deleting stop $stopId from trip: $tripId" }
            val response: HttpResponse = httpClient.delete("$baseUrl/$tripId/stops/$stopId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val bodyText = response.bodyAsText()
            if (response.status.isSuccess()) {
                TripApiResponse(success = true, message = "Stop deleted successfully")
            } else {
                TripApiResponse(success = false, message = ApiErrorHandler.extractErrorMessage(response.status, bodyText))
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to delete trip stop: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    // ==================== State Management APIs ====================

    override suspend fun updateTripState(
        token: String,
        id: String,
        state: String,
        reason: String?,
        notes: String?
    ): TripApiResponse<TripDto> {
        return try {
            log.d { "Updating trip state: $id -> $state" }
            val request = StateUpdateRequestDto(
                state = state,
                reason = reason,
                notes = notes
            )
            val response: HttpResponse = httpClient.patch("$baseUrl/$id/state") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            parseSingleResponse(response)
        } catch (e: Exception) {
            log.e(e) { "Failed to update trip state: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }

    override suspend fun getTripStateHistory(
        token: String,
        id: String,
        page: Int,
        perPage: Int
    ): TripApiResponse<StateHistoryResponseDto> {
        return try {
            log.d { "Fetching trip state history: $id, page=$page" }
            val response: HttpResponse = httpClient.get("$baseUrl/$id/state-history") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("page", page)
                parameter("per_page", perPage)
            }
            val bodyText = response.bodyAsText()
            log.d { "Trip state history response: $bodyText" }
            if (response.status.isSuccess()) {
                json.decodeFromString<TripApiResponse<StateHistoryResponseDto>>(bodyText)
            } else {
                TripApiResponse(success = false, message = "Request failed with status ${response.status}")
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to fetch trip state history: ${e.message}" }
            TripApiResponse(success = false, message = ApiErrorHandler.getNetworkErrorMessage(e))
        }
    }
}
