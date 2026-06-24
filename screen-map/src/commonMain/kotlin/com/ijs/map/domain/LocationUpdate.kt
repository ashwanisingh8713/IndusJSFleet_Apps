package com.ijs.map.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single live-location frame pushed by the backend WebSocket.
 *
 * Backend contract (see fleet backend `websocket/hub.go` `LocationUpdate`):
 * ```
 * {"type":"location_update","registration_number":"MH12AB1234","vehicle_id":42,
 *  "trip_id":7,"driver_id":3,"lat":28.6139,"lng":77.2090,"speed":55.5,
 *  "heading":90.0,"ts":1735200000000}
 * ```
 * The feed is tenant-scoped server-side (each socket is bound to the JWT owner).
 *
 * DTO discipline: every field has `@SerialName` and a default so a missing or
 * extra field never breaks decoding. `vehicle_id` / `trip_id` / `driver_id` are
 * numeric on the wire; we keep them as [Long] to match the backend `int64`.
 */
@Serializable
data class LocationUpdate(
    @SerialName("type") val type: String = "",
    @SerialName("registration_number") val registrationNumber: String = "",
    @SerialName("vehicle_id") val vehicleId: Long = 0L,
    @SerialName("trip_id") val tripId: Long? = null,
    @SerialName("driver_id") val driverId: Long? = null,
    @SerialName("lat") val lat: Double = 0.0,
    @SerialName("lng") val lng: Double = 0.0,
    @SerialName("speed") val speed: Double = 0.0,
    @SerialName("heading") val heading: Double? = null,
    @SerialName("ts") val ts: Long = 0L
) {
    companion object {
        /** The only frame type the map currently consumes. */
        const val TYPE_LOCATION_UPDATE = "location_update"
    }
}
