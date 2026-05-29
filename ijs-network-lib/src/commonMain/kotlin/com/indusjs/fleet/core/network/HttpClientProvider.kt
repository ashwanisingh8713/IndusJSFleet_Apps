package com.indusjs.fleet.core.network

import com.indusjs.fleet.core.auth.AuthenticationManager
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
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

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
     */
    fun createHttpClient(
        json: Json,
        getOldToken: (() -> String?)? = null,
        saveNewToken: ((String) -> Unit)? = null
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
                        if (hadAuthHeader) {
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

        if (getOldToken != null && saveNewToken != null) {
            client.plugin(HttpSend).intercept { request ->
                var call = execute(request)
                if (call.response.status == HttpStatusCode.Unauthorized) {
                    val authHeader = request.headers["Authorization"]
                    val hadAuthHeader = authHeader != null && authHeader.startsWith("Bearer")
                    if (hadAuthHeader) {
                        val oldToken = getOldToken()
                        if (oldToken != null) {
                            co.touchlab.kermit.Logger.i("HTTP") { "Access token expired (401). Attempting silent token refresh..." }
                            val refreshSuccess = tryToRefreshSession(json, oldToken, saveNewToken)
                            if (refreshSuccess) {
                                val newToken = getOldToken()
                                if (newToken != null) {
                                    co.touchlab.kermit.Logger.i("HTTP") { "Silent token refresh succeeded! Retrying original request with new token..." }
                                    // Prepare and execute a retry with the new token
                                    request.headers["Authorization"] = "Bearer $newToken"
                                    call = execute(request)
                                }
                            } else {
                                co.touchlab.kermit.Logger.w("HTTP") { "Silent token refresh failed." }
                            }
                        }
                    }
                }
                call
            }
        }

        return client
    }

    private suspend fun tryToRefreshSession(
        json: Json,
        oldToken: String,
        saveNewToken: (String) -> Unit
    ): Boolean {
        // Create an ephemeral, basic client to call /auth/refresh without interceptors
        val refreshClient = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
        }
        return try {
            val response: HttpResponse = refreshClient.post {
                url("${ApiConfig.BASE_URL}/auth/refresh")
                header(HttpHeaders.Authorization, "Bearer $oldToken")
            }
            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                val bodyText = response.bodyAsText()
                val jsonEl = json.parseToJsonElement(bodyText) as? JsonObject
                if (jsonEl != null) {
                    val success = jsonEl["success"]?.jsonPrimitive?.booleanOrNull ?: false
                    if (success) {
                        val dataObj = jsonEl["data"] as? JsonObject
                        val newToken = dataObj?.get("token")?.jsonPrimitive?.contentOrNull
                        if (!newToken.isNullOrBlank()) {
                            saveNewToken(newToken)
                            true
                        } else false
                    } else false
                } else false
            } else false
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("HTTP") { "Error during silent token refresh: ${e.message}" }
            false
        } finally {
            refreshClient.close()
        }
    }
}

