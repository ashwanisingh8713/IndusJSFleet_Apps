package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSourceImpl
import com.indusjs.fleet.data.repository.user.UserRepositoryImpl
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Scope marker for the User feature.
 */
abstract class UserFeatureScope private constructor()

/**
 * Dependency graph for the User feature.
 * Contains all dependencies needed for user-related operations (auth, profile, etc.).
 *
 * Usage:
 * ```kotlin
 * val userGraph = UserFeatureGraph.Factory::class.create(
 *     httpClient = AppDependencies.httpClient,
 *     dispatcherProvider = AppDependencies.dispatcherProvider,
 *     settings = AppDependencies.settings
 * )
 * ```
 */
@SingleIn(UserFeatureScope::class)
@DependencyGraph
abstract class UserFeatureGraph {

    @Binds
    abstract fun bindUserRemoteDataSource(impl: UserRemoteDataSourceImpl): UserRemoteDataSource

    @Binds
    abstract fun bindUserLocalDataSource(impl: UserLocalDataSourceImpl): UserLocalDataSource

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    abstract val userRepository: UserRepository
    abstract val userLocalDataSource: UserLocalDataSource
    abstract val dispatcherProvider: DispatcherProvider

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides settings: Settings
        ): UserFeatureGraph
    }
}

