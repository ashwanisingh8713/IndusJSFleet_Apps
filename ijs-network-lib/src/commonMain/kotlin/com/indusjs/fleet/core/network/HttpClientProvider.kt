package com.indusjs.fleet.core.network

import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.core.auth.JwtHelper
import com.indusjs.fleet.data.model.user.RefreshTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Provides configured HttpClient and Json instances for API calls.
 *
 * This is the single source of truth for network configuration.
 * Use [create] for standalone HttpClient or [createHttpClient] with a shared Json instance.
 */
object HttpClientProvider {

    /**
     * Creates a configured Json instance for API serialization.
     * Use this when you need a shared Json instance across components.
     */
    fun createJson(): Json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = false // Don't include null values in JSON output
    }

    /**
     * Creates a configured HttpClient instance with its own internal Json.
     * Includes a 401 Unauthorized interceptor that triggers authentication events.
     */
    fun create(): HttpClient = createHttpClient(createJson())

    /**
     * Creates a configured HttpClient instance with a provided Json instance.
     * Includes a 401 Unauthorized interceptor that triggers authentication events.
     *
     * Use this when you want to share the same Json instance across the app
     * (e.g., via DI framework).
     *
     * @param json The Json instance to use for content negotiation
     * @param getRefreshToken Reads the current (rotating) refresh token. Null/blank means the
     *        session predates refresh support (older login) → cannot refresh → re-login.
     * @param onRefreshed Persists the NEW rotated pair + expiry. Receives the per-response
     *        `expires_in` (seconds) so the caller stores an absolute expiry with no hardcoded TTL.
     */
    fun createHttpClient(
        json: Json,
        getRefreshToken: (suspend () -> String?)? = null,
        onRefreshed: (suspend (accessToken: String, refreshToken: String, expiresInSeconds: Long) -> Unit)? = null
    ): HttpClient {
        val client = HttpClient {
            // Install JSON serialization
            install(ContentNegotiation) {
                json(json)
            }

            // Install logging for debugging
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.d("HTTP") { message }
                    }
                }
                level = LogLevel.BODY
            }

            // Install timeout
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.TIMEOUT_MS
                connectTimeoutMillis = ApiConfig.TIMEOUT_MS
                socketTimeoutMillis = ApiConfig.TIMEOUT_MS
            }

            // Install 401 Unauthorized interceptor
            // Only triggers session expiry if the request had an Authorization header
            // This prevents clearing session for unauthenticated requests (login, public APIs)
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status == HttpStatusCode.Unauthorized) {
                        // Check if the request had an Authorization header
                        val authHeader = response.call.request.headers["Authorization"]
                        val hadAuthHeader = authHeader != null && authHeader.startsWith("Bearer")
                        val sessionNeutral = isSessionNeutral401(response.call.request.url.encodedPath, authHeader)
                        if (sessionNeutral) {
                            // A valid (authenticated) token can legitimately get a 401 here
                            // pre-onboarding ("user is not linked to an owner yet"). This is an
                            // authorization gap, NOT an expired session — do not clear it, let the
                            // caller fall back gracefully (e.g. empty permission set).
                            co.touchlab.kermit.Logger.d("HTTP") {
                                "401 on session-neutral endpoint (${response.call.request.url.encodedPath}) - NOT clearing session"
                            }
                        } else if (hadAuthHeader) {
                            co.touchlab.kermit.Logger.w("HTTP") {
                                "401 Unauthorized received for authenticated request - triggering session expired event"
                            }
                            // This is called within a coroutine context, so we can use suspend function
                            AuthenticationManager.emitSessionExpired("Your session has expired. Please log in again.")
                        } else {
                            co.touchlab.kermit.Logger.d("HTTP") {
                                "401 Unauthorized received for unauthenticated request - ignoring (no session to expire)"
                            }
                        }
                    }
                }
            }

            // Default request configuration
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }

        if (getRefreshToken != null && onRefreshed != null) {
            client.plugin(HttpSend).intercept { request ->
                var call = execute(request)
                if (call.response.status == HttpStatusCode.Unauthorized) {
                    val authHeader = request.headers["Authorization"]
                    val hadAuthHeader = authHeader != null && authHeader.startsWith("Bearer")
                    // Skip silent refresh for session-neutral 401s (e.g. the pre-onboarding
                    // permissions probe): the token is valid, so refreshing is pointless noise.
                    if (hadAuthHeader && !isSessionNeutral401(request.url.encodedPathSegments.joinToString("/"), authHeader)) {
                        val refreshToken = getRefreshToken()
                        if (refreshToken.isNullOrBlank()) {
                            // Older session with no refresh token → cannot silently refresh.
                            // Let the 401 propagate so HttpResponseValidator clears the session
                            // and routes to re-login.
                            co.touchlab.kermit.Logger.w("HTTP") { "401 but no refresh token available — needs re-login." }
                        } else {
                            co.touchlab.kermit.Logger.i("HTTP") { "Access token expired (401). Attempting silent token refresh via /auth/refresh..." }
                            val newAccessToken = tryToRefreshSession(json, refreshToken, onRefreshed)
                            if (!newAccessToken.isNullOrBlank()) {
                                co.touchlab.kermit.Logger.i("HTTP") { "Silent token refresh succeeded! Retrying original request with new token..." }
                                // Retry the original request with the freshly rotated access token.
                                request.headers["Authorization"] = "Bearer $newAccessToken"
                                call = execute(request)
                            } else {
                                // Refresh 401 (invalid/expired/REUSED) or error → session is dead.
                                // The propagated original 401 triggers HttpResponseValidator → re-login.
                                co.touchlab.kermit.Logger.w("HTTP") { "Silent token refresh failed — session expired, re-login required." }
                            }
                        }
                    }
                }
                call
            }
        }

        return client
    }

    /**
     * Decides whether a 401 must NOT trigger session expiry / token clearing because it
     * reflects onboarding state rather than an invalid/expired session. Two cases:
     *
     * 1. The pre-onboarding permissions probe ([ApiConfig.Endpoints.ME_PERMISSIONS]) 401s
     *    for a valid token ("user is not linked to an owner yet").
     * 2. The Bearer token has NO tenant context (`tid`) — i.e. the user is still in the
     *    onboarding window before tenant creation. Any fleet endpoint that requires a
     *    tenant (e.g. `/dashboard`) legitimately 401s here; clearing the session would
     *    strand/eject the user mid-onboarding. A post-onboarding token always carries a
     *    `tid` (even when genuinely expired), so real expiries still log out.
     *
     * The genuine `401 -> re-login` rule still applies to every authenticated,
     * tenant-scoped request.
     */
    private fun isSessionNeutral401(encodedPath: String, authHeader: String?): Boolean {
        if (encodedPath.endsWith(ApiConfig.Endpoints.ME_PERMISSIONS)) return true
        val token = authHeader?.removePrefix("Bearer")?.trim()
        return !token.isNullOrBlank() && !JwtHelper.hasTenantContext(token)
    }

    /**
     * Performs a silent token refresh against POST /auth/refresh.
     *
     * Sends `{"refresh_token": "<opaque>"}` and, on 200, persists the NEW rotated pair
     * + per-response expiry via [onRefreshed], returning the new access token. Returns
     * null on a 401 (invalid/expired/REUSED refresh token → session dead → re-login) or
     * any error. Uses an ephemeral client with no interceptors to avoid recursion.
     */
    private suspend fun tryToRefreshSession(
        json: Json,
        refreshToken: String,
        onRefreshed: suspend (accessToken: String, refreshToken: String, expiresInSeconds: Long) -> Unit
    ): String? {
        val refreshClient = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
        return try {
            val response: HttpResponse = refreshClient.post {
                url("${ApiConfig.BASE_URL}${ApiConfig.Endpoints.REFRESH}")
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken = refreshToken))
            }
            if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
                // 401 = invalid/expired/REUSED → caller treats as session-expired → re-login.
                return null
            }
            val bodyText = response.bodyAsText()
            val jsonEl = json.parseToJsonElement(bodyText) as? JsonObject ?: return null
            val success = jsonEl["success"]?.jsonPrimitive?.booleanOrNull ?: false
            if (!success) return null
            val dataObj = jsonEl["data"] as? JsonObject ?: return null
            val newAccessToken = dataObj["access_token"]?.jsonPrimitive?.contentOrNull
            // Refresh token ROTATES — persist the NEW one and discard the old.
            val newRefreshToken = dataObj["refresh_token"]?.jsonPrimitive?.contentOrNull
            val expiresIn = dataObj["expires_in"]?.jsonPrimitive?.longOrNull ?: 0L
            if (newAccessToken.isNullOrBlank() || newRefreshToken.isNullOrBlank()) return null
            onRefreshed(newAccessToken, newRefreshToken, expiresIn)
            newAccessToken
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("HTTP") { "Error during silent token refresh: ${e.message}" }
            null
        } finally {
            refreshClient.close()
        }
    }
}

