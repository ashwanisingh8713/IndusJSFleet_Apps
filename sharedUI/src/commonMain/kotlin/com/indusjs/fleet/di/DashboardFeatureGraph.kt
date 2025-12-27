package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.dashboard.DashboardRepositoryImpl
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.presentation.dashboard.DashboardViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Scope marker for the Dashboard feature.
 */
abstract class DashboardFeatureScope private constructor()

/**
 * Dependency graph for the Dashboard feature.
 * Contains all dependencies needed for dashboard operations.
 *
 * Usage:
 * ```kotlin
 * val dashboardGraph = DashboardFeatureGraph.Factory::class.create(
 *     httpClient = AppDependencies.httpClient,
 *     dispatcherProvider = AppDependencies.dispatcherProvider,
 *     userLocalDataSource = userGraph.userLocalDataSource
 * )
 * ```
 */
@SingleIn(DashboardFeatureScope::class)
@DependencyGraph
abstract class DashboardFeatureGraph {

    @Binds
    abstract fun bindDashboardRemoteDataSource(impl: DashboardRemoteDataSourceImpl): DashboardRemoteDataSource

    @Binds
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository

    abstract val dashboardRepository: DashboardRepository
    abstract val dashboardViewModel: DashboardViewModel
    abstract val dispatcherProvider: DispatcherProvider

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource
        ): DashboardFeatureGraph
    }
}

