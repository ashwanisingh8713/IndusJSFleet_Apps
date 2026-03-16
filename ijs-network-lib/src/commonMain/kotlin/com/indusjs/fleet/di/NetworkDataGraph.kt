package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.costs.CostsLocalDataSource
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.customer.CustomerLocalDataSource
import com.indusjs.fleet.data.datasource.customer.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.finance.VehicleFinanceRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.payment.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.datasource.reports.ReportsRemoteDataSource
import com.indusjs.fleet.data.datasource.team.TeamLocalDataSource
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSourceImpl
import com.indusjs.fleet.data.mapper.driver.DriverMapper
import com.indusjs.fleet.data.mapper.trip.TripMapper
import com.indusjs.fleet.data.mapper.trip.TripStopMapper
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.repository.costs.CostTypesRepositoryImpl
import com.indusjs.fleet.data.repository.costs.CostsRepositoryImpl
import com.indusjs.fleet.data.repository.customer.CustomerRepositoryImpl
import com.indusjs.fleet.data.repository.dashboard.DashboardRepositoryImpl
import com.indusjs.fleet.data.repository.driver.DriverRepositoryImpl
import com.indusjs.fleet.data.repository.finance.VehicleFinanceRepositoryImpl
import com.indusjs.fleet.data.repository.payment.TripPaymentRepositoryImpl
import com.indusjs.fleet.data.repository.reports.ReportsRepositoryImpl
import com.indusjs.fleet.data.repository.team.TeamRepositoryImpl
import com.indusjs.fleet.data.repository.trip.TripRepositoryImpl
import com.indusjs.fleet.data.repository.user.UserRepositoryImpl
import com.indusjs.fleet.data.repository.vehicle.VehicleRepositoryImpl
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import com.indusjs.fleet.domain.repository.finance.VehicleFinanceRepository
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Centralized dependency graph for the entire data layer in ijs-network-lib.
 *
 * Wires all data sources, mappers, and repositories using constructor injection.
 * All implementation classes use @Inject constructors — this graph composes
 * them together and exposes domain repository interfaces to consumers.
 *
 * ## What this replaces
 * Previously, ~120 lines of manual data-layer wiring was scattered across
 * DefaultViewModelProvider in sharedUI. This class centralizes that wiring
 * inside ijs-network-lib where the implementations live.
 *
 * ## External dependencies (provided via create())
 * - [HttpClient] — configured HTTP client for API calls
 * - [Json] — configured JSON serializer
 * - [Settings] — multiplatform-settings for local storage
 * - [DispatcherProvider] — coroutine dispatchers
 * - Room-backed local data sources (impls in sharedUI, interfaces here)
 *
 * ## Usage
 * ```kotlin
 * val graph = NetworkDataGraph.create(
 *     httpClient = httpClient,
 *     json = json,
 *     settings = settings,
 *     dispatcherProvider = dispatcherProvider,
 *     dashboardLocalDataSource = dashboardLocalDataSourceImpl,
 *     costsLocalDataSource = costsLocalDataSourceImpl,
 *     teamLocalDataSource = teamLocalDataSourceImpl,
 *     customerLocalDataSource = customerLocalDataSourceImpl
 * )
 * val vehicleRepo = graph.vehicleRepository
 * ```
 */
class NetworkDataGraph private constructor(
    // ==================== Exposed Repositories ====================
    val userRepository: UserRepository,
    val vehicleRepository: VehicleRepository,
    val driverRepository: DriverRepository,
    val tripRepository: TripRepository,
    val dashboardRepository: DashboardRepository,
    val customerRepository: CustomerRepository,
    val teamRepository: TeamRepository,
    val tripPaymentRepository: TripPaymentRepository,
    val costsRepository: CostsRepository,
    val costTypesRepository: CostTypesRepository,
    val vehicleFinanceRepository: VehicleFinanceRepository,
    val reportsRepository: ReportsRepository,
    // ==================== Exposed Data Sources ====================
    val userLocalDataSource: UserLocalDataSource
) {
    companion object {
        /**
         * Creates a fully-wired [NetworkDataGraph] with all data layer dependencies.
         *
         * @param httpClient Configured HTTP client for API calls
         * @param json Configured JSON serializer
         * @param settings Multiplatform-settings for local storage (auth tokens)
         * @param dispatcherProvider Coroutine dispatchers
         * @param dashboardLocalDataSource Room-backed dashboard cache (impl in sharedUI)
         * @param costsLocalDataSource Room-backed cost types cache (impl in sharedUI)
         * @param teamLocalDataSource Room-backed team members cache (impl in sharedUI)
         * @param customerLocalDataSource Room-backed customer cache (impl in sharedUI)
         */
        fun create(
            httpClient: HttpClient,
            json: Json,
            settings: Settings,
            dispatcherProvider: DispatcherProvider,
            dashboardLocalDataSource: DashboardLocalDataSource,
            costsLocalDataSource: CostsLocalDataSource,
            teamLocalDataSource: TeamLocalDataSource,
            customerLocalDataSource: CustomerLocalDataSource
        ): NetworkDataGraph {
            // --- Data Sources ---
            val userLocalDS = UserLocalDataSourceImpl(settings)
            val userRemoteDS = UserRemoteDataSourceImpl(httpClient)
            val vehicleRemoteDS = VehicleRemoteDataSourceImpl(httpClient)
            val driverRemoteDS = DriverRemoteDataSourceImpl(httpClient)
            val tripRemoteDS = TripRemoteDataSourceImpl(httpClient)
            val dashboardRemoteDS = DashboardRemoteDataSourceImpl(httpClient)
            val teamRemoteDS = TeamRemoteDataSourceImpl(httpClient)
            val costsRemoteDS = CostsRemoteDataSourceImpl(httpClient)
            val customerRemoteDS = CustomerRemoteDataSource(httpClient, json)
            val paymentRemoteDS = TripPaymentRemoteDataSource(httpClient, json)
            val financeRemoteDS = VehicleFinanceRemoteDataSourceImpl(httpClient, json)
            val reportsRemoteDS = ReportsRemoteDataSource(httpClient, json)

            // --- Mappers ---
            val vehicleMapper = VehicleMapper()
            val driverMapper = DriverMapper()
            val tripMapper = TripMapper()
            val tripStopMapper = TripStopMapper()

            // --- Repositories ---
            val userRepo = UserRepositoryImpl(userRemoteDS, userLocalDS)
            val vehicleRepo = VehicleRepositoryImpl(vehicleRemoteDS, userLocalDS, vehicleMapper)
            val driverRepo = DriverRepositoryImpl(driverRemoteDS, userLocalDS, driverMapper)
            val tripRepo = TripRepositoryImpl(tripRemoteDS, userLocalDS, tripMapper, tripStopMapper)
            val dashboardRepo = DashboardRepositoryImpl(dashboardRemoteDS, dashboardLocalDataSource, userLocalDS)
            val customerRepo = CustomerRepositoryImpl(customerRemoteDS, customerLocalDataSource, userLocalDS)
            val teamRepo = TeamRepositoryImpl(teamRemoteDS, userLocalDS, teamLocalDataSource)
            val paymentRepo = TripPaymentRepositoryImpl(paymentRemoteDS, dashboardRemoteDS, userLocalDS)
            val costsRepo = CostsRepositoryImpl(costsRemoteDS, userLocalDS)
            val costTypesRepo = CostTypesRepositoryImpl(costsRemoteDS, costsLocalDataSource, userLocalDS)
            val financeRepo = VehicleFinanceRepositoryImpl(financeRemoteDS, userLocalDS, dispatcherProvider)
            val reportsRepo = ReportsRepositoryImpl(reportsRemoteDS, userLocalDS)

            return NetworkDataGraph(
                userRepository = userRepo,
                vehicleRepository = vehicleRepo,
                driverRepository = driverRepo,
                tripRepository = tripRepo,
                dashboardRepository = dashboardRepo,
                customerRepository = customerRepo,
                teamRepository = teamRepo,
                tripPaymentRepository = paymentRepo,
                costsRepository = costsRepo,
                costTypesRepository = costTypesRepo,
                vehicleFinanceRepository = financeRepo,
                reportsRepository = reportsRepo,
                userLocalDataSource = userLocalDS
            )
        }
    }
}
