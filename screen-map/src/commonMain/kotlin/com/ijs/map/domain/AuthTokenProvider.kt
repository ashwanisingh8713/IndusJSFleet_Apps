package com.ijs.map.domain

/**
 * Supplies the current auth JWT for the live-location WebSocket handshake.
 *
 * Browsers cannot attach an `Authorization` header to a WebSocket upgrade, so
 * the backend accepts the token via the `?token=<JWT>` query parameter. The
 * implementation (in the sharedUI DI layer) reads the same stored token the
 * REST `HttpClient` uses, so the socket inherits the tenant scope (`tid`).
 */
fun interface AuthTokenProvider {
    /** Returns the current JWT, or `null` when there is no active session. */
    suspend fun getToken(): String?
}
