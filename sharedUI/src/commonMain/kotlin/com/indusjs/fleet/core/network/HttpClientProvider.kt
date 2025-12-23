package com.indusjs.fleet.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Provides configured HttpClient for API calls.
 */
object HttpClientProvider {

    /**
     * Creates a configured HttpClient instance.
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

        // Default request configuration
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}

