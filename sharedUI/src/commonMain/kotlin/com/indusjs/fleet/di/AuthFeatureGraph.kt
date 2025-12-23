package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.presentation.auth.LoginViewModel
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Scope marker for the Auth feature.
 */
abstract class AuthFeatureScope private constructor()

/**
 * Dependency graph for the Auth feature.
 * Contains all dependencies needed for authentication-related operations.
 *
 * Usage:
 * ```kotlin
 * val authGraph = AuthFeatureGraph.Factory::class.create(
 *     dispatcherProvider = AppDependencies.dispatcherProvider,
 *     userRepository = userGraph.userRepository
 * )
 * val loginViewModel = authGraph.loginViewModel
 * ```
 */
@SingleIn(AuthFeatureScope::class)
@DependencyGraph
abstract class AuthFeatureGraph {

    /**
     * Provides the LoginViewModel.
     */
    abstract val loginViewModel: LoginViewModel

    /**
     * Factory for creating AuthFeatureGraph with parent dependencies.
     */
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userRepository: UserRepository
        ): AuthFeatureGraph
    }
}

