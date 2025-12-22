package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DefaultDispatcherProvider
import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Root dependency graph for the entire application.
 * Provides all application-wide dependencies.
 */
@SingleIn(AppScope::class)
@DependencyGraph
abstract class RootGraph {

    /**
     * Provides the DispatcherProvider for coroutines.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    /**
     * Provides a configured Json instance for serialization.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        encodeDefaults = true
        coerceInputValues = true
    }

    /**
     * Provides a configured HttpClient for API calls.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(json: Json): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d { message }
                }
            }
            level = LogLevel.BODY
        }
    }

    /**
     * Accessor for DispatcherProvider
     */
    abstract val dispatcherProvider: DispatcherProvider

    /**
     * Accessor for HttpClient
     */
    abstract val httpClient: HttpClient

    /**
     * Accessor for Json
     */
    abstract val json: Json

    companion object
}

