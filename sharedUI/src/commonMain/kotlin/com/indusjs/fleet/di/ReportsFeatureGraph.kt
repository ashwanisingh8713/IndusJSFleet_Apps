package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.reports.ReportsRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.reports.ReportsRepositoryImpl
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.presentation.reports.ReportsViewModel
import com.indusjs.fleet.presentation.reports.consolidated.ConsolidatedPLViewModel
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisViewModel
import com.indusjs.fleet.presentation.reports.trip.TripPLViewModel
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLViewModel
import io.ktor.client.*
import kotlinx.serialization.json.Json
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Dependency graph for Reports feature
 */
@SingleIn(FeatureScope::class)
@DependencyGraph
abstract class ReportsFeatureGraph {

    @Provides
    fun provideReportsRemoteDataSource(
        httpClient: HttpClient,
        json: Json
    ): ReportsRemoteDataSource = ReportsRemoteDataSource(httpClient, json)

    @Provides
    fun provideReportsRepository(
        remoteDataSource: ReportsRemoteDataSource,
        userLocalDataSource: UserLocalDataSource
    ): ReportsRepository = ReportsRepositoryImpl(remoteDataSource, userLocalDataSource)

    abstract val reportsViewModel: ReportsViewModel
    abstract val vehiclePLViewModel: VehiclePLViewModel
    abstract val tripPLViewModel: TripPLViewModel
    abstract val costAnalysisViewModel: CostAnalysisViewModel
    abstract val consolidatedPLViewModel: ConsolidatedPLViewModel

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides json: Json,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides vehicleRepository: VehicleRepository,
            @Provides userLocalDataSource: UserLocalDataSource
        ): ReportsFeatureGraph
    }
}

