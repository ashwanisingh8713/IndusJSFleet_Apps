package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import io.ktor.client.*
import kotlinx.serialization.json.Json

/**
 * Application dependency container.
 * Provides access to the DI graph throughout the application.
 *
 * Usage:
 * ```kotlin
 * // At app startup
 * AppDependencies.initialize { RootGraph.create() }
 *
 * // Access dependencies
 * val httpClient = AppDependencies.httpClient
 * ```
 */
object AppDependencies {

    private var _rootGraph: RootGraph? = null

    /**
     * The root dependency graph instance.
     * Call [initialize] before accessing this property.
     *
     * Note: Metro generates the graph implementation.
     */
    val rootGraph: RootGraph
        get() = _rootGraph
            ?: error("AppDependencies not initialized. Call initialize() first.")

    /**
     * Initialize the dependency graph.
     * Should be called at application startup.
     *
     * @param graphFactory A lambda that creates the RootGraph instance.
     *                     Use: AppDependencies.initialize { RootGraph.create() }
     *                     Metro generates the create() extension function.
     */
    fun initialize(graphFactory: () -> RootGraph) {
        if (_rootGraph == null) {
            _rootGraph = graphFactory()
        }
    }

    /**
     * Reset the dependency graph (useful for testing).
     */
    fun reset() {
        _rootGraph = null
    }

    // Convenience accessors
    val dispatcherProvider: DispatcherProvider
        get() = rootGraph.dispatcherProvider

    val httpClient: HttpClient
        get() = rootGraph.httpClient

    val json: Json
        get() = rootGraph.json
}

