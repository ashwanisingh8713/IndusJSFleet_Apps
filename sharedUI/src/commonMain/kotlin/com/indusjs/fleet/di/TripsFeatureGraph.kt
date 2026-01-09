package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSource
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.trip.TripRepositoryImpl
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.CreateTripUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripByIdUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripsUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripStatusUseCase
import com.indusjs.fleet.presentation.trips.TripsViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Scope marker for the Trips feature.
 */
abstract class TripsFeatureScope private constructor()

/**
 * Dependency graph for the Trips feature.
 * Contains all dependencies needed for trip-related operations.
 *
 * Note: Cost summary is now embedded in Trip API response,
 * so CostsRepository is no longer needed here.
 */
@SingleIn(TripsFeatureScope::class)
@DependencyGraph
abstract class TripsFeatureGraph {

    @Binds
    abstract fun bindTripRemoteDataSource(impl: TripRemoteDataSourceImpl): TripRemoteDataSource

    @Binds
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository

    abstract val tripsViewModel: TripsViewModel
    abstract val tripRepository: TripRepository
    abstract val getTripsUseCase: GetTripsUseCase
    abstract val getTripByIdUseCase: GetTripByIdUseCase
    abstract val createTripUseCase: CreateTripUseCase
    abstract val updateTripStatusUseCase: UpdateTripStatusUseCase
    abstract val cancelTripUseCase: CancelTripUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource
        ): TripsFeatureGraph
    }
}
