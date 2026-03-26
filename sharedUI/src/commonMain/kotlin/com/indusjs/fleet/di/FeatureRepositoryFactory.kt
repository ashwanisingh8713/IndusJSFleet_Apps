package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.customer.data.datasource.CustomerLocalDataSource
import com.ijs.customer.data.datasource.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.ijs.driver.data.datasource.DriverRemoteDataSourceImpl
import com.ijs.finance.data.datasource.VehicleFinanceRemoteDataSourceImpl
import com.ijs.payment.data.datasource.TripPaymentRemoteDataSource
import com.ijs.reports.data.datasource.ReportsRemoteDataSource
import com.ijs.team.data.datasource.TeamLocalDataSource
import com.ijs.team.data.datasource.TeamRemoteDataSourceImpl
import com.ijs.trip.data.datasource.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.ijs.vehicle.data.datasource.VehicleRemoteDataSourceImpl
import com.ijs.driver.data.mapper.DriverMapper
import com.ijs.trip.data.mapper.TripMapper
import com.ijs.trip.data.mapper.TripStopMapper
import com.ijs.vehicle.data.mapper.VehicleMapper
import com.ijs.customer.data.repository.CustomerRepositoryImpl
import com.ijs.driver.data.repository.DriverRepositoryImpl
import com.ijs.finance.data.repository.VehicleFinanceRepositoryImpl
import com.ijs.payment.data.repository.TripPaymentRepositoryImpl
import com.ijs.reports.data.repository.ReportsRepositoryImpl
import com.ijs.team.data.repository.TeamRepositoryImpl
import com.ijs.trip.data.repository.TripRepositoryImpl
import com.ijs.vehicle.data.repository.VehicleRepositoryImpl
import com.ijs.customer.domain.repository.CustomerRepository
import com.ijs.driver.domain.repository.DriverRepository
import com.ijs.finance.domain.repository.VehicleFinanceRepository
import com.ijs.payment.domain.repository.TripPaymentRepository
import com.ijs.reports.domain.repository.ReportsRepository
import com.ijs.team.domain.repository.TeamRepository
import com.ijs.trip.domain.repository.TripRepository
import com.ijs.vehicle.domain.repository.VehicleRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import com.russhwolf.settings.Settings

/**
 * Factory that constructs feature-specific repositories from their respective feature libs.
 *
 * Each feature repository is lazily constructed by wiring its data source, mapper, and
 * auth-token provider together. All repos share the same HttpClient, UserLocalDataSource,
 * and FleetLogger instance.
 */
class FeatureRepositoryFactory(
    private val httpClient: HttpClient,
    private val json: Json,
    private val settings: Settings,
    private val dispatcherProvider: DispatcherProvider,
    private val teamLocalDataSource: TeamLocalDataSource,
    private val customerLocalDataSource: CustomerLocalDataSource,
    private val logger: FleetLogger
) {
    // Shared UserLocalDataSource for auth tokens across all feature repos
    private val userLocalDataSource by lazy { UserLocalDataSourceImpl(settings, logger) }

    // ── Driver (ijs-driver-lib) ──
    val driverRepository: DriverRepository by lazy {
        DriverRepositoryImpl(
            remoteDataSource = DriverRemoteDataSourceImpl(httpClient, logger),
            userLocalDataSource = userLocalDataSource,
            mapper = DriverMapper(),
            logger = logger
        )
    }

    // ── Vehicle (ijs-vehicle-lib) ──
    val vehicleRepository: VehicleRepository by lazy {
        VehicleRepositoryImpl(
            remoteDataSource = VehicleRemoteDataSourceImpl(httpClient, logger),
            userLocalDataSource = userLocalDataSource,
            mapper = VehicleMapper()
        )
    }

    // ── Trip (ijs-trip-lib) ──
    val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(
            remoteDataSource = TripRemoteDataSourceImpl(httpClient, logger),
            userLocalDataSource = userLocalDataSource,
            mapper = TripMapper(),
            stopMapper = TripStopMapper()
        )
    }

    // ── Customer (ijs-customer-lib) ──
    val customerRepository: CustomerRepository by lazy {
        CustomerRepositoryImpl(
            remoteDataSource = CustomerRemoteDataSource(httpClient, json, logger),
            localDataSource = customerLocalDataSource,
            userLocalDataSource = userLocalDataSource,
            logger = logger
        )
    }

    // ── Payment (ijs-payment-lib) ──
    val tripPaymentRepository: TripPaymentRepository by lazy {
        TripPaymentRepositoryImpl(
            remoteDataSource = TripPaymentRemoteDataSource(httpClient, json, logger),
            dashboardDataSource = DashboardRemoteDataSourceImpl(httpClient, logger),
            userLocalDataSource = userLocalDataSource,
            logger = logger
        )
    }

    // ── Team (ijs-team-lib) ──
    val teamRepository: TeamRepository by lazy {
        TeamRepositoryImpl(
            remoteDataSource = TeamRemoteDataSourceImpl(httpClient, logger),
            userLocalDataSource = userLocalDataSource,
            localDataSource = teamLocalDataSource,
            logger = logger
        )
    }

    // ── Reports (ijs-reports-lib) ──
    val reportsRepository: ReportsRepository by lazy {
        ReportsRepositoryImpl(
            remoteDataSource = ReportsRemoteDataSource(httpClient, json, logger),
            userLocalDataSource = userLocalDataSource,
            logger = logger
        )
    }

    // ── Finance (ijs-finance-lib) ──
    val vehicleFinanceRepository: VehicleFinanceRepository by lazy {
        VehicleFinanceRepositoryImpl(
            remoteDataSource = VehicleFinanceRemoteDataSourceImpl(httpClient, json, logger),
            userLocalDataSource = userLocalDataSource,
            dispatcherProvider = dispatcherProvider,
            logger = logger
        )
    }
}
