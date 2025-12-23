package com.indusjs.fleet.di

import com.indusjs.fleet.core.network.ApiConfig
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
    }

    /**
     * Creates a configured HttpClient for API calls.
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

        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}

