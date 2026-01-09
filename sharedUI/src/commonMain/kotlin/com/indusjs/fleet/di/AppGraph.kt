package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.dispatcher.createPlatformDispatcherProvider
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Application-wide scope marker for Metro DI.
 * Dependencies marked with @SingleIn(AppScope::class) will be singletons.
 */
abstract class AppScope private constructor()

/**
 * Feature-level scope marker for Metro DI.
 * Dependencies marked with @SingleIn(FeatureScope::class) will be scoped to features.
 */
abstract class FeatureScope private constructor()

/**
 * Main application dependency graph.
 * This is the root graph that provides application-wide dependencies.
 */
@SingleIn(AppScope::class)
@DependencyGraph
abstract class AppGraph {

    /**
     * Provides the dispatcher provider for coroutines.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideDispatcherProvider(): DispatcherProvider = createPlatformDispatcherProvider()

    companion object
}
