package com.indusjs.fleet.di

import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.core.network.ApiConfig
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

/**
 * Interface defining network-related dependencies.
 * Implementations are provided through Metro DI in RootGraph.
 *
 * Network dependencies available:
 * - HttpClient: configured with ContentNegotiation, Logging, and Timeout
 * - Json: configured for API serialization with lenient parsing
 *
 * Usage:
 * - Dependencies are injected via constructor injection with @Inject
 * - Access through feature-specific graphs that extend RootGraph dependencies
 */
interface NetworkModule {
    val httpClient: HttpClient
    val json: Json
}

/**
 * Network configuration constants and factory methods.
 */
object NetworkConfig {

    /**
     * Creates a configured Json instance for API serialization.
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
     * Creates a configured HttpClient for API calls.
     * Includes a 401 Unauthorized interceptor that triggers authentication events.
     */
    fun createHttpClient(json: Json): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d("HTTP") { message }
                }
            }
            level = LogLevel.BODY
        }

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

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}

