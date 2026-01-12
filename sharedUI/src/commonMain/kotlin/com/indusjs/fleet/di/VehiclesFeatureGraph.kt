package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSource
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSourceImpl
import com.indusjs.fleet.data.repository.vehicle.VehicleRepositoryImpl
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.domain.usecase.vehicle.CreateVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.DeleteVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.domain.usecase.vehicle.UpdateVehicleUseCase
import com.indusjs.fleet.presentation.vehicles.AddVehicleViewModel
import com.indusjs.fleet.presentation.vehicles.VehiclesViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Scope marker for the Vehicles feature.
 */
abstract class VehiclesFeatureScope private constructor()

/**
 * Dependency graph for the Vehicles feature.
 * Contains all dependencies needed for vehicle-related operations.
 *
 * Usage:
 * ```kotlin
 * val vehiclesGraph = VehiclesFeatureGraph.Factory::class.create(
 *     httpClient = AppDependencies.httpClient,
 *     dispatcherProvider = AppDependencies.dispatcherProvider,
 *     userLocalDataSource = userGraph.userLocalDataSource
 * )
 * ```
 */
@SingleIn(VehiclesFeatureScope::class)
@DependencyGraph
abstract class VehiclesFeatureGraph {

    @Binds
    abstract fun bindVehicleRemoteDataSource(impl: VehicleRemoteDataSourceImpl): VehicleRemoteDataSource

    @Binds
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    abstract val vehiclesViewModel: VehiclesViewModel
    abstract val addVehicleViewModel: AddVehicleViewModel
    abstract val vehicleRepository: VehicleRepository
    abstract val getVehiclesUseCase: GetVehiclesUseCase
    abstract val getVehicleByIdUseCase: GetVehicleByIdUseCase
    abstract val createVehicleUseCase: CreateVehicleUseCase
    abstract val updateVehicleUseCase: UpdateVehicleUseCase
    abstract val deleteVehicleUseCase: DeleteVehicleUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource,
            @Provides teamRepository: TeamRepository
        ): VehiclesFeatureGraph
    }
}

