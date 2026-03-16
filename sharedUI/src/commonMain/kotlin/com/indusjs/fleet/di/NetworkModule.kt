package com.indusjs.fleet.di

import com.indusjs.fleet.core.network.HttpClientProvider
import io.ktor.client.HttpClient
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
 * Network configuration factory methods.
 *
 * Delegates to [HttpClientProvider] from ijs-network-lib for the actual
 * HttpClient and Json creation. This ensures a single source of truth
 * for network configuration across all modules.
 */
object NetworkConfig {

    /**
     * Creates a configured Json instance for API serialization.
     */
    fun createJson(): Json = HttpClientProvider.createJson()

    /**
     * Creates a configured HttpClient for API calls.
     * Includes a 401 Unauthorized interceptor that triggers authentication events.
     */
    fun createHttpClient(json: Json): HttpClient = HttpClientProvider.createHttpClient(json)
}

