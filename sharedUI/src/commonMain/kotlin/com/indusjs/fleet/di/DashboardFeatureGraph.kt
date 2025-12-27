package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.database.dao.DashboardDao
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.dashboard.DashboardCacheMapper
import com.indusjs.fleet.data.repository.dashboard.DashboardRepositoryImpl
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.domain.usecase.dashboard.GetDashboardUseCase
import com.indusjs.fleet.domain.usecase.dashboard.RefreshDashboardUseCase
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
 * Dependency graph for the Dashboard feature with offline-first support.
 *
 * Provides:
 * - Remote data source for API calls
 * - Local data source for Settings-based caching
 * - Repository with offline-first strategy
 * - Use cases for business logic
 * - ViewModel for UI state management
 */
@SingleIn(DashboardFeatureScope::class)
@DependencyGraph
abstract class DashboardFeatureGraph {

    @Binds
    abstract fun bindRemoteDataSource(impl: DashboardRemoteDataSourceImpl): DashboardRemoteDataSource

    @Binds
    abstract fun bindLocalDataSource(impl: DashboardLocalDataSourceImpl): DashboardLocalDataSource

    @Binds
    abstract fun bindRepository(impl: DashboardRepositoryImpl): DashboardRepository

    abstract val dashboardViewModel: DashboardViewModel
    abstract val dispatcherProvider: DispatcherProvider

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource,
            @Provides dashboardDao: DashboardDao,
            @Provides cacheMapper: DashboardCacheMapper
        ): DashboardFeatureGraph
    }
}

