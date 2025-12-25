package com.indusjs.fleet.data.datasource.location

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Google Places API Service for location autocomplete, place details, and distance calculation.
 */
class GooglePlacesService(
    private val httpClient: HttpClient,
    private val apiKey: String
) {
    companion object {
        private const val PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place"
        private const val AUTOCOMPLETE_ENDPOINT = "$PLACES_BASE_URL/autocomplete/json"
        private const val DETAILS_ENDPOINT = "$PLACES_BASE_URL/details/json"
        private const val DISTANCE_MATRIX_ENDPOINT = "https://maps.googleapis.com/maps/api/distancematrix/json"
    }

    /**
     * Search for place predictions based on input text.
     */
    suspend fun searchPlaces(
        query: String,
        sessionToken: String? = null
    ): Result<List<PlacePrediction>> {
        if (query.length < 3) {
            return Result.success(emptyList())
        }

        return try {
            val response: PlacesAutocompleteResponse = httpClient.get(AUTOCOMPLETE_ENDPOINT) {
                parameter("input", query)
                parameter("key", apiKey)
                parameter("components", "country:in") // Restrict to India
                parameter("types", "geocode|establishment")
                sessionToken?.let { parameter("sessiontoken", it) }
            }.body()

            if (response.status == "OK" || response.status == "ZERO_RESULTS") {
                Result.success(response.predictions)
            } else {
                Result.failure(Exception("Places API error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get place details including coordinates for a place ID.
     */
    suspend fun getPlaceDetails(
        placeId: String,
        sessionToken: String? = null
    ): Result<PlaceDetails> {
        return try {
            val response: PlaceDetailsResponse = httpClient.get(DETAILS_ENDPOINT) {
                parameter("place_id", placeId)
                parameter("key", apiKey)
                parameter("fields", "formatted_address,geometry,name")
                sessionToken?.let { parameter("sessiontoken", it) }
            }.body()

            if (response.status == "OK" && response.result != null) {
                Result.success(response.result)
            } else {
                Result.failure(Exception("Place details error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get road distance between two locations using Google Distance Matrix API.
     * @param originLat Origin latitude
     * @param originLng Origin longitude
     * @param destLat Destination latitude
     * @param destLng Destination longitude
     * @return DistanceResult containing distance in km and duration in minutes
     */
    suspend fun getRoadDistance(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Result<DistanceResult> {
        return try {
            val origins = "$originLat,$originLng"
            val destinations = "$destLat,$destLng"

            val response: DistanceMatrixResponse = httpClient.get(DISTANCE_MATRIX_ENDPOINT) {
                parameter("origins", origins)
                parameter("destinations", destinations)
                parameter("key", apiKey)
                parameter("mode", "driving")
                parameter("units", "metric")
            }.body()

            if (response.status == "OK") {
                val element = response.rows.firstOrNull()?.elements?.firstOrNull()
                if (element != null && element.status == "OK") {
                    val distanceKm = element.distance.value / 1000.0 // Convert meters to km
                    val durationMinutes = element.duration.value / 60 // Convert seconds to minutes
                    Result.success(
                        DistanceResult(
                            distanceKm = distanceKm,
                            distanceText = element.distance.text,
                            durationMinutes = durationMinutes,
                            durationText = element.duration.text
                        )
                    )
                } else {
                    Result.failure(Exception("No route found: ${element?.status}"))
                }
            } else {
                Result.failure(Exception("Distance Matrix API error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ============== Distance Result ==============

/**
 * Result of distance calculation containing distance and duration.
 */
data class DistanceResult(
    val distanceKm: Double,
    val distanceText: String,
    val durationMinutes: Long,
    val durationText: String
)

// ============== Distance Matrix Response Models ==============

@Serializable
data class DistanceMatrixResponse(
    val status: String,
    val rows: List<DistanceMatrixRow> = emptyList(),
    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
data class DistanceMatrixRow(
    val elements: List<DistanceMatrixElement> = emptyList()
)

@Serializable
data class DistanceMatrixElement(
    val status: String,
    val distance: DistanceValue = DistanceValue(),
    val duration: DurationValue = DurationValue()
)

@Serializable
data class DistanceValue(
    val value: Long = 0, // Distance in meters
    val text: String = ""
)

@Serializable
data class DurationValue(
    val value: Long = 0, // Duration in seconds
    val text: String = ""
)

// ============== Places Response Models ==============

@Serializable
data class PlacesAutocompleteResponse(
    val status: String,
    val predictions: List<PlacePrediction> = emptyList(),
    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
data class PlacePrediction(
    @SerialName("place_id")
    val placeId: String,
    val description: String,
    @SerialName("structured_formatting")
    val structuredFormatting: StructuredFormatting? = null
)

@Serializable
data class StructuredFormatting(
    @SerialName("main_text")
    val mainText: String,
    @SerialName("secondary_text")
    val secondaryText: String? = null
)

@Serializable
data class PlaceDetailsResponse(
    val status: String,
    val result: PlaceDetails? = null,
    @SerialName("error_message")
    val errorMessage: String? = null
)

@Serializable
data class PlaceDetails(
    val name: String? = null,
    @SerialName("formatted_address")
    val formattedAddress: String? = null,
    val geometry: Geometry? = null
)

@Serializable
data class Geometry(
    val location: LatLng? = null
)

@Serializable
data class LatLng(
    val lat: Double,
    val lng: Double
)

