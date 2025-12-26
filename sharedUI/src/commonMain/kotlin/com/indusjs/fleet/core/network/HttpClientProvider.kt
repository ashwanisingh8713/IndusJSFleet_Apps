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
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status == HttpStatusCode.Unauthorized) {
                    co.touchlab.kermit.Logger.w("HTTP") {
                        "401 Unauthorized received - triggering session expired event"
                    }
                    // This is called within a coroutine context, so we can use suspend function
                    AuthenticationManager.emitSessionExpired("Your session has expired. Please log in again.")
                }
            }
        }

        // Default request configuration
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}

