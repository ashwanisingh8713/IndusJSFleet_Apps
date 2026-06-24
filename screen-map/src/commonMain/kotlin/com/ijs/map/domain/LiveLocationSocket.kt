package com.ijs.map.domain

import kotlinx.coroutines.flow.Flow

/**
 * Streams live-location frames from the backend WebSocket.
 *
 * Implementations open `ws(s)://<origin>/ws?token=<JWT>`, read frames, parse
 * `location_update` payloads and emit them as a cold [Flow]. Collection drives
 * the connection lifecycle: the socket opens when collection starts and closes
 * when the collecting coroutine is cancelled (i.e. when the screen leaves and
 * the ViewModel scope is cleared).
 *
 * Connection/parse failures are surfaced by the flow terminating (or throwing),
 * never by crashing the caller — the ViewModel keeps the seeded markers and
 * shows a disconnected status.
 */
interface LiveLocationSocket {

    /**
     * Cold flow of location frames. One collection == one socket connection.
     * The flow completes when the server closes the socket; it throws if the
     * handshake fails or the connection drops — callers should catch and treat
     * that as "disconnected".
     */
    fun connect(): Flow<LocationUpdate>
}
