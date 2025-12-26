package com.indusjs.fleet.locationtracker.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MQTT Location Message format as per the API specification.
 *
 * MQTT Topic: fleet/vehicle/{registration_number}/location
 *
 * Example payload:
 * {
 *   "registration_number": "MH12AB1234",
 *   "lat": 19.0760,
 *   "lng": 72.8777,
 *   "speed": 45.5,
 *   "ts": 1703500800000,
 *   "heading": 180.0,
 *   "accuracy": 5.0,
 *   "trip_id": 456,
 *   "driver_id": 789,
 *   "bat": 85,
 *   "provider": "gps"
 * }
 *
 * Note: vehicle_id and owner_id are auto-resolved from registration_number on the server.
 */
@Serializable
data class LocationMessage(
    @SerialName("registration_number")
    val registrationNumber: String,

    @SerialName("lat")
    val latitude: Double,

    @SerialName("lng")
    val longitude: Double,

    @SerialName("speed")
    val speed: Double,

    @SerialName("ts")
    val timestamp: Long,

    @SerialName("heading")
    val heading: Double? = null,

    @SerialName("altitude")
    val altitude: Double? = null,

    @SerialName("accuracy")
    val accuracy: Double? = null,

    @SerialName("trip_id")
    val tripId: Long? = null,

    @SerialName("driver_id")
    val driverId: Long? = null,

    @SerialName("bat")
    val batteryLevel: Int? = null,

    @SerialName("provider")
    val provider: String? = null
)

/**
 * Configuration for the tracker app.
 */
@Serializable
data class TrackerConfig(
    val registrationNumber: String = "",
    val driverId: Long? = null,
    val tripId: Long? = null,
    val mqttBrokerUrl: String = "tcp://10.0.2.2:1883", // Default to Android emulator localhost
    val mqttClientId: String = "",
    val mqttUsername: String = "",
    val mqttPassword: String = "",
    val updateIntervalMs: Long = 2000, // 2 seconds
    val isTrackingEnabled: Boolean = false
) {
    val isConfigured: Boolean
        get() = registrationNumber.isNotBlank() && mqttBrokerUrl.isNotBlank()

    /**
     * Generate the MQTT topic for vehicle location.
     * Format: fleet/vehicle/{registration_number}/location
     */
    fun getMqttTopic(): String {
        return "fleet/vehicle/$registrationNumber/location"
    }

    /**
     * Generate the MQTT topic for trip location (if trip is active).
     * Format: fleet/trip/{trip_id}/location
     */
    fun getTripMqttTopic(): String? {
        return tripId?.let { "fleet/trip/$it/location" }
    }
}

/**
 * Represents the current tracking status.
 */
data class TrackingStatus(
    val isTracking: Boolean = false,
    val isMqttConnected: Boolean = false,
    val lastLocation: LocationMessage? = null,
    val lastError: String? = null,
    val messagesPublished: Long = 0
)

