package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSource
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.driver.DriverRepositoryImpl
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.usecase.driver.CreateDriverUseCase
import com.indusjs.fleet.domain.usecase.driver.DeleteDriverUseCase
import com.indusjs.fleet.domain.usecase.driver.GetAvailableDriversUseCase
import com.indusjs.fleet.domain.usecase.driver.GetDriverByIdUseCase
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.driver.ToggleDriverActiveUseCase
import com.indusjs.fleet.domain.usecase.driver.UpdateDriverStatusUseCase
import com.indusjs.fleet.domain.usecase.driver.UpdateDriverUseCase
import com.indusjs.fleet.presentation.drivers.DriversViewModel
import com.indusjs.fleet.presentation.drivers.create.CreateDriverViewModel
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Scope marker for the Drivers feature.
 */
abstract class DriversFeatureScope private constructor()

/**
 * Dependency graph for the Drivers feature.
 * Contains all dependencies needed for driver-related operations.
 *
 * Usage:
 * ```kotlin
 * val driversGraph = DriversFeatureGraph.Factory::class.create(
 *     httpClient = AppDependencies.httpClient,
 *     dispatcherProvider = AppDependencies.dispatcherProvider,
 *     userLocalDataSource = userGraph.userLocalDataSource
 * )
 * ```
 */
@SingleIn(DriversFeatureScope::class)
@DependencyGraph
abstract class DriversFeatureGraph {

    @Binds
    abstract fun bindDriverRemoteDataSource(impl: DriverRemoteDataSourceImpl): DriverRemoteDataSource

    @Binds
    abstract fun bindDriverRepository(impl: DriverRepositoryImpl): DriverRepository

    abstract val driversViewModel: DriversViewModel
    abstract val createDriverViewModel: CreateDriverViewModel
    abstract val driverDetailViewModel: DriverDetailViewModel
    abstract val driverRepository: DriverRepository
    abstract val getDriversUseCase: GetDriversUseCase
    abstract val getAvailableDriversUseCase: GetAvailableDriversUseCase
    abstract val getDriverByIdUseCase: GetDriverByIdUseCase
    abstract val createDriverUseCase: CreateDriverUseCase
    abstract val updateDriverUseCase: UpdateDriverUseCase
    abstract val updateDriverStatusUseCase: UpdateDriverStatusUseCase
    abstract val toggleDriverActiveUseCase: ToggleDriverActiveUseCase
    abstract val deleteDriverUseCase: DeleteDriverUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource,
            @Provides teamRepository: TeamRepository,
            @Provides costsRepository: CostsRepository
        ): DriversFeatureGraph
    }
}

