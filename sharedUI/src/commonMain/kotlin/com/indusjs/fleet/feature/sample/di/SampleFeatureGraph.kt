package com.indusjs.fleet.feature.sample.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.feature.sample.data.datasource.UserRemoteDataSource
import com.indusjs.fleet.feature.sample.data.datasource.UserRemoteDataSourceImpl
import com.indusjs.fleet.feature.sample.data.repository.UserRepositoryImpl
import com.indusjs.fleet.feature.sample.domain.repository.UserRepository
import com.indusjs.fleet.feature.sample.presentation.UserListViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.*

/**
 * Scope marker for the sample feature.
 */
abstract class SampleFeatureScope private constructor()

/**
 * Dependency graph for the Sample User feature.
 * Contains all dependencies needed for this feature.
 *
 * Uses @DependencyGraph.Factory to receive dependencies from parent graph.
 * Metro generates the Factory implementation.
 */
@SingleIn(SampleFeatureScope::class)
@DependencyGraph
abstract class SampleFeatureGraph {

    /**
     * Binds UserRemoteDataSourceImpl to UserRemoteDataSource interface.
     */
    @Binds
    abstract fun bindUserRemoteDataSource(impl: UserRemoteDataSourceImpl): UserRemoteDataSource

    /**
     * Binds UserRepositoryImpl to UserRepository interface.
     */
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    /**
     * Provides the UserListViewModel.
     */
    abstract val userListViewModel: UserListViewModel

    /**
     * Factory for creating SampleFeatureGraph with parent dependencies.
     * Metro generates the implementation of this factory.
     */
    @DependencyGraph.Factory
    fun interface Factory {
        /**
         * Creates the SampleFeatureGraph with the required dependencies.
         */
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider
        ): SampleFeatureGraph
    }
}
