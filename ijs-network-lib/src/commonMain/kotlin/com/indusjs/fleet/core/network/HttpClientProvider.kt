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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Provides configured HttpClient for API calls.
 */
object HttpClientProvider {

    /**
     * Creates a configured HttpClient instance.
     * Includes a 401 Unauthorized interceptor that triggers authentication events.
     */
    fun create(): HttpClient = HttpClient {
        // Install JSON serialization
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
                coerceInputValues = true
                encodeDefaults = true
                explicitNulls = false // Don't include null values in JSON output
            })
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
                    // Check if the request had an Authorization header by looking for "Bearer" in headers
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
}

