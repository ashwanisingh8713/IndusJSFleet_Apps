package com.ijs.map.data

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.network.ApiConfig
import com.ijs.map.TAG_MAP_VM
import com.ijs.map.domain.AuthTokenProvider
import com.ijs.map.domain.LiveLocationSocket
import com.ijs.map.domain.LocationUpdate
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Ktor-based [LiveLocationSocket].
 *
 * Builds the WebSocket URL from [ApiConfig.BASE_URL] by stripping the `/api/v1`
 * suffix and swapping the scheme (`http`->`ws`, `https`->`wss`), then appends
 * the configured WebSocket path and `?token=<JWT>`.
 *
 * It owns its own [HttpClient] with the `WebSockets` plugin installed, so the
 * rest of the app does not need to depend on `ktor-client-websockets`. The
 * default-engine client is created lazily and reused across reconnects; it is
 * closed in [close].
 */
class KtorLiveLocationSocket(
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
    private val logger: FleetLogger,
    /** Path the backend exposes the socket on. Backend default is `/ws`. */
    private val webSocketPath: String = DEFAULT_WS_PATH,
    /**
     * Factory for the WS-capable client. Overridable in tests; the default
     * installs the [WebSockets] plugin on Ktor's default engine.
     */
    clientFactory: () -> HttpClient = { HttpClient { install(WebSockets) } }
) : LiveLocationSocket {

    private val client: HttpClient by lazy(clientFactory)

    override fun connect(): Flow<LocationUpdate> = flow {
        val token = tokenProvider.getToken()
        if (token.isNullOrBlank()) {
            logger.w(TAG_MAP_VM, "No auth token available; cannot open live-location socket")
            return@flow
        }

        val url = buildSocketUrl(token)
        logger.i(TAG_MAP_VM, "Opening live-location WebSocket")

        clientTouched = true
        client.webSocket(urlString = url) {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val update = parseFrame(frame.readText()) ?: continue
                    if (update.type == LocationUpdate.TYPE_LOCATION_UPDATE) {
                        emit(update)
                    }
                }
            }
        }
    }

    /** Releases the underlying client. Safe to call when never connected. */
    fun close() {
        if (clientInitialized()) {
            runCatchingClose()
        }
    }

    // `lazy` has no public "is initialized" check across all targets, so guard
    // with a simple flag set on first access instead.
    private var clientTouched = false
    private fun clientInitialized(): Boolean = clientTouched
    private fun runCatchingClose() {
        try {
            client.close()
        } catch (e: Exception) {
            logger.w(TAG_MAP_VM, "Error closing live-location WS client", e)
        }
    }

    private fun parseFrame(text: String): LocationUpdate? = try {
        json.decodeFromString(LocationUpdate.serializer(), text)
    } catch (e: Exception) {
        // A malformed/unknown frame must not tear down the stream.
        logger.w(TAG_MAP_VM, "Dropping unparseable WS frame", e)
        null
    }

    /**
     * Derives `ws(s)://<origin><webSocketPath>?token=<jwt>` from [ApiConfig.BASE_URL].
     *
     * BASE_URL ends in `/api/v1`; we strip that, then swap the scheme. The token
     * is sent as a query param because WS upgrades can't carry an auth header.
     */
    private fun buildSocketUrl(token: String): String {
        val origin = ApiConfig.BASE_URL.removeSuffix(API_VERSION_SUFFIX)
        val wsOrigin = when {
            origin.startsWith("https://") -> "wss://" + origin.removePrefix("https://")
            origin.startsWith("http://") -> "ws://" + origin.removePrefix("http://")
            else -> origin // already ws(s):// or scheme-less — leave untouched
        }
        val path = if (webSocketPath.startsWith("/")) webSocketPath else "/$webSocketPath"
        return "$wsOrigin$path?token=$token"
    }

    companion object {
        private const val API_VERSION_SUFFIX = "/api/v1"
        const val DEFAULT_WS_PATH = "/ws"
    }
}
