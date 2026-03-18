package com.indusjs.fleet.di

import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.customer.CustomerLocalDataSource
import com.indusjs.fleet.data.datasource.customer.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.finance.VehicleFinanceRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.payment.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.datasource.reports.ReportsRemoteDataSource
import com.indusjs.fleet.data.datasource.team.TeamLocalDataSource
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSourceImpl
import com.indusjs.fleet.data.mapper.driver.DriverMapper
import com.indusjs.fleet.data.mapper.trip.TripMapper
import com.indusjs.fleet.data.mapper.trip.TripStopMapper
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.repository.customer.CustomerRepositoryImpl
import com.indusjs.fleet.data.repository.driver.DriverRepositoryImpl
import com.indusjs.fleet.data.repository.finance.VehicleFinanceRepositoryImpl
import com.indusjs.fleet.data.repository.payment.TripPaymentRepositoryImpl
import com.indusjs.fleet.data.repository.reports.ReportsRepositoryImpl
import com.indusjs.fleet.data.repository.team.TeamRepositoryImpl
import com.indusjs.fleet.data.repository.trip.TripRepositoryImpl
import com.indusjs.fleet.data.repository.vehicle.VehicleRepositoryImpl
import com.indusjs.fleet.domain.repository.customer.CustomerRepository
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import com.indusjs.fleet.domain.repository.finance.VehicleFinanceRepository
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import com.russhwolf.settings.Settings

/**
 * Factory that constructs feature-specific repositories from their respective feature libs.
 *
 * Each feature repository is lazily constructed by wiring its data source, mapper, and
 * auth-token provider together. All repos share the same HttpClient and UserLocalDataSource.
 */
class FeatureRepositoryFactory(
    private val httpClient: HttpClient,
    private val json: Json,
    private val settings: Settings,
    private val dispatcherProvider: DispatcherProvider,
    private val teamLocalDataSource: TeamLocalDataSource,
    private val customerLocalDataSource: CustomerLocalDataSource
) {
    // Shared UserLocalDataSource for auth tokens across all feature repos
    private val userLocalDataSource by lazy { UserLocalDataSourceImpl(settings) }

    // ── Driver (ijs-driver-lib) ──
    val driverRepository: DriverRepository by lazy {
        DriverRepositoryImpl(
            remoteDataSource = DriverRemoteDataSourceImpl(httpClient),
            userLocalDataSource = userLocalDataSource,
            mapper = DriverMapper()
        )
    }

    // ── Vehicle (ijs-vehicle-lib) ──
    val vehicleRepository: VehicleRepository by lazy {
        VehicleRepositoryImpl(
            remoteDataSource = VehicleRemoteDataSourceImpl(httpClient),
            userLocalDataSource = userLocalDataSource,
            mapper = VehicleMapper()
        )
    }

    // ── Trip (ijs-trip-lib) ──
    val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(
            remoteDataSource = TripRemoteDataSourceImpl(httpClient),
            userLocalDataSource = userLocalDataSource,
            mapper = TripMapper(),
            stopMapper = TripStopMapper()
        )
    }

    // ── Customer (ijs-customer-lib) ──
    val customerRepository: CustomerRepository by lazy {
        CustomerRepositoryImpl(
            remoteDataSource = CustomerRemoteDataSource(httpClient, json),
            localDataSource = customerLocalDataSource,
            userLocalDataSource = userLocalDataSource
        )
    }

    // ── Payment (ijs-payment-lib) ──
    val tripPaymentRepository: TripPaymentRepository by lazy {
        TripPaymentRepositoryImpl(
            remoteDataSource = TripPaymentRemoteDataSource(httpClient, json),
            dashboardDataSource = DashboardRemoteDataSourceImpl(httpClient),
            userLocalDataSource = userLocalDataSource
        )
    }

    // ── Team (ijs-team-lib) ──
    val teamRepository: TeamRepository by lazy {
        TeamRepositoryImpl(
            remoteDataSource = TeamRemoteDataSourceImpl(httpClient),
            userLocalDataSource = userLocalDataSource,
            localDataSource = teamLocalDataSource
        )
    }

    // ── Reports (ijs-reports-lib) ──
    val reportsRepository: ReportsRepository by lazy {
        ReportsRepositoryImpl(
            remoteDataSource = ReportsRemoteDataSource(httpClient, json),
            userLocalDataSource = userLocalDataSource
        )
    }

    // ── Finance (ijs-finance-lib) ──
    val vehicleFinanceRepository: VehicleFinanceRepository by lazy {
        VehicleFinanceRepositoryImpl(
            remoteDataSource = VehicleFinanceRemoteDataSourceImpl(httpClient, json),
            userLocalDataSource = userLocalDataSource,
            dispatcherProvider = dispatcherProvider
        )
    }
}

