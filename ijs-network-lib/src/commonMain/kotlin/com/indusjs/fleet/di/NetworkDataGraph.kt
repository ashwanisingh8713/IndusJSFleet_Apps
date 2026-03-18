package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.costs.CostsLocalDataSource
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSourceImpl
import com.indusjs.fleet.data.repository.costs.CostTypesRepositoryImpl
import com.indusjs.fleet.data.repository.costs.CostsRepositoryImpl
import com.indusjs.fleet.data.repository.dashboard.DashboardRepositoryImpl
import com.indusjs.fleet.data.repository.user.UserRepositoryImpl
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Centralized dependency graph for the SHARED data layer in ijs-network-lib.
 *
 * After modularization, this graph only manages:
 * - User/Auth repositories (login, profile, tokens)
 * - Dashboard repository (aggregation)
 * - Costs/CostTypes repositories (shared cost type cache)
 *
 * Feature-specific repositories (vehicle, trip, driver, customer, payment,
 * team, reports, finance) are now in their respective feature modules
 * and wired in sharedUI's DefaultViewModelProvider.
 *
 * ## External dependencies (provided via create())
 * - [HttpClient] — configured HTTP client for API calls
 * - [Json] — configured JSON serializer
 * - [Settings] — multiplatform-settings for local storage
 * - [DispatcherProvider] — coroutine dispatchers
 * - Room-backed local data sources (impls in sharedUI, interfaces here)
 */
class NetworkDataGraph private constructor(
    // ==================== Exposed Repositories ====================
    val userRepository: UserRepository,
    val dashboardRepository: DashboardRepository,
    val costsRepository: CostsRepository,
    val costTypesRepository: CostTypesRepository,
    // ==================== Exposed Data Sources ====================
    val userLocalDataSource: UserLocalDataSource
) {
    companion object {
        /**
         * Creates a [NetworkDataGraph] with shared/infrastructure dependencies.
         */
        fun create(
            httpClient: HttpClient,
            json: Json,
            settings: Settings,
            dispatcherProvider: DispatcherProvider,
            dashboardLocalDataSource: DashboardLocalDataSource,
            costsLocalDataSource: CostsLocalDataSource
        ): NetworkDataGraph {
            // --- Data Sources ---
            val userLocalDS = UserLocalDataSourceImpl(settings)
            val userRemoteDS = UserRemoteDataSourceImpl(httpClient)
            val dashboardRemoteDS = DashboardRemoteDataSourceImpl(httpClient)
            val costsRemoteDS = CostsRemoteDataSourceImpl(httpClient)

            // --- Repositories ---
            val userRepo = UserRepositoryImpl(userRemoteDS, userLocalDS)
            val dashboardRepo = DashboardRepositoryImpl(dashboardRemoteDS, dashboardLocalDataSource, userLocalDS)
            val costsRepo = CostsRepositoryImpl(costsRemoteDS, userLocalDS)
            val costTypesRepo = CostTypesRepositoryImpl(costsRemoteDS, costsLocalDataSource, userLocalDS)

            return NetworkDataGraph(
                userRepository = userRepo,
                dashboardRepository = dashboardRepo,
                costsRepository = costsRepo,
                costTypesRepository = costTypesRepo,
                userLocalDataSource = userLocalDS
            )
        }
    }
}
