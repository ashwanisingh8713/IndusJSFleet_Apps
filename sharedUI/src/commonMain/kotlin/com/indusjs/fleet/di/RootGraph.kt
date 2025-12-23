package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DefaultDispatcherProvider
import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Root dependency graph for the entire application.
 * Provides all application-wide dependencies following CLEAN architecture.
 *
 * This is the main entry point for dependency injection using Metro.
 * Feature-specific graphs should use @DependencyGraph.Factory to receive
 * dependencies from this root graph.
 */
@SingleIn(AppScope::class)
@DependencyGraph
abstract class RootGraph : NetworkModule {

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
    fun provideJson(): Json = NetworkConfig.createJson()

    /**
     * Provides a configured HttpClient for API calls.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(json: Json): HttpClient = NetworkConfig.createHttpClient(json)

    /**
     * Provides Settings for local storage (auth tokens, preferences).
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideSettings(): Settings = Settings()

    /**
     * Accessor for DispatcherProvider
     */
    abstract val dispatcherProvider: DispatcherProvider

    /**
     * Accessor for HttpClient
     */
    abstract override val httpClient: HttpClient

    /**
     * Accessor for Json
     */
    abstract override val json: Json

    /**
     * Accessor for Settings
     */
    abstract val settings: Settings

    companion object
}

