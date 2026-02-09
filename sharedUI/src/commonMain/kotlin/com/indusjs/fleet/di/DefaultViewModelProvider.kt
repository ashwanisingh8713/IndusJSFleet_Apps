package com.indusjs.fleet.di

import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.core.init.AppInitializer
import com.indusjs.dispatcher.DefaultDispatcherProvider
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.network.HttpClientProvider
import com.indusjs.fleet.data.database.FleetDatabase
import com.indusjs.fleet.data.datasource.costs.CostsLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.costs.CostsRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.dashboard.DashboardRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.reports.ReportsRemoteDataSource
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSourceImpl
import com.indusjs.fleet.data.mapper.dashboard.DashboardCacheMapper
import com.indusjs.fleet.data.mapper.driver.DriverMapper
import com.indusjs.fleet.data.mapper.trip.TripMapper
import com.indusjs.fleet.data.mapper.trip.TripStopMapper
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.repository.costs.CostTypesRepositoryImpl
import com.indusjs.fleet.data.repository.costs.CostsRepositoryImpl
import com.indusjs.fleet.data.repository.dashboard.DashboardRepositoryImpl
import com.indusjs.fleet.data.repository.driver.DriverRepositoryImpl
import com.indusjs.fleet.data.repository.reports.ReportsRepositoryImpl
import com.indusjs.fleet.data.repository.team.TeamRepositoryImpl
import com.indusjs.fleet.data.repository.trip.TripRepositoryImpl
import com.indusjs.fleet.data.repository.user.UserRepositoryImpl
import com.indusjs.fleet.data.repository.vehicle.VehicleRepositoryImpl
import com.indusjs.fleet.data.datasource.team.TeamLocalDataSourceImpl
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import com.indusjs.fleet.domain.repository.reports.ReportsRepository
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.domain.usecase.costs.GetMaintenanceCostTypesUseCase
import com.indusjs.fleet.domain.usecase.costs.GetDriverCostTypesUseCase
import com.indusjs.fleet.domain.usecase.costs.GetTripCostTypesUseCase
import com.indusjs.fleet.domain.usecase.costs.InitializeCostTypesUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetAlertsStatusUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetCostOverviewUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetDashboardUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetPendingPaymentsUseCase
import com.indusjs.fleet.domain.usecase.dashboard.RefreshDashboardUseCase
import com.indusjs.fleet.domain.usecase.driver.CreateDriverUseCase
import com.indusjs.fleet.domain.usecase.driver.DeleteDriverUseCase
import com.indusjs.fleet.domain.usecase.driver.GetAvailableDriversUseCase
import com.indusjs.fleet.domain.usecase.driver.GetDriverByIdUseCase
import com.indusjs.fleet.domain.usecase.driver.GetDriversUseCase
import com.indusjs.fleet.domain.usecase.driver.ToggleDriverActiveUseCase
import com.indusjs.fleet.domain.usecase.driver.UpdateDriverStatusUseCase
import com.indusjs.fleet.domain.usecase.driver.UpdateDriverUseCase
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.CreateTripWithDataUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripByIdUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripsUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripStatusUseCase
import com.indusjs.fleet.domain.usecase.trip.UpdateTripUseCase
import com.indusjs.fleet.domain.usecase.vehicle.CreateVehicleWithDocumentsUseCase
import com.indusjs.fleet.domain.usecase.vehicle.DeleteVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetAvailableVehiclesUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.domain.usecase.vehicle.UpdateVehicleUseCase
import com.indusjs.fleet.presentation.auth.LoginViewModel
import com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryViewModel
import com.indusjs.fleet.presentation.trips.cost.TripCostEntryViewModel
import com.indusjs.fleet.presentation.drivers.cost.DriverCostEntryViewModel
import com.indusjs.fleet.presentation.alerts.AlertsListViewModel
import com.indusjs.fleet.presentation.dashboard.DashboardViewModel
import com.indusjs.fleet.presentation.drivers.DriversViewModel
import com.indusjs.fleet.presentation.drivers.create.CreateDriverViewModel
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailViewModel
import com.indusjs.fleet.presentation.maps.MapsViewModel
import com.indusjs.fleet.presentation.reports.ReportsViewModel
import com.indusjs.fleet.presentation.reports.consolidated.ConsolidatedPLViewModel
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisViewModel
import com.indusjs.fleet.presentation.reports.trip.TripPLViewModel
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLViewModel
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel
import com.indusjs.fleet.presentation.team.detail.TeamMemberDetailViewModel
import com.indusjs.fleet.presentation.team.list.TeamListViewModel
import com.indusjs.fleet.presentation.trips.TripsViewModel
import com.indusjs.fleet.presentation.trips.create.CreateTripViewModel
import com.indusjs.fleet.presentation.trips.detail.TripDetailViewModel
import com.indusjs.fleet.presentation.user.changepassword.ChangePasswordViewModel
import com.indusjs.fleet.presentation.user.forgotpassword.ForgotPasswordViewModel
import com.indusjs.fleet.presentation.user.profile.ProfileViewModel
import com.indusjs.fleet.presentation.user.signup.SignUpViewModel
import com.indusjs.fleet.presentation.vehicles.AddVehicleViewModel
import com.indusjs.fleet.presentation.vehicles.VehiclesViewModel
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailViewModel
import com.indusjs.fleet.presentation.customers.list.CustomersListViewModel
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailViewModel
import com.indusjs.fleet.presentation.customers.create.CreateCustomerViewModel
import com.indusjs.fleet.data.datasource.customer.CustomerRemoteDataSource
import com.indusjs.fleet.data.datasource.customer.CustomerLocalDataSourceImpl
import com.indusjs.fleet.data.repository.customer.CustomerRepositoryImpl
import com.indusjs.fleet.domain.usecase.customer.*
import com.indusjs.fleet.data.datasource.payment.TripPaymentRemoteDataSource
import com.indusjs.fleet.data.repository.payment.TripPaymentRepositoryImpl
import com.indusjs.fleet.domain.repository.payment.TripPaymentRepository
import com.indusjs.fleet.presentation.payments.PaymentsViewModel
import com.indusjs.fleet.presentation.payments.AddPaymentViewModel
import com.indusjs.fleet.presentation.payments.PaymentDetailViewModel
import com.indusjs.fleet.data.datasource.finance.VehicleFinanceRemoteDataSourceImpl
import com.indusjs.fleet.data.repository.finance.VehicleFinanceRepositoryImpl
import com.indusjs.fleet.domain.repository.finance.VehicleFinanceRepository
import com.indusjs.fleet.presentation.finance.VehicleFinanceViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Development/Stub implementation of ViewModelProvider.
 * Creates dependencies manually without Metro code generation.
 *
 * In production with Metro DI fully configured, use MetroViewModelProvider instead.
 * This serves as a bridge during the migration to full Metro DI.
 *
 * IMPORTANT: Use getInstance() to get a shared singleton instance.
 * This ensures the same Settings instance is used throughout the app lifecycle,
 * which is critical for auth token persistence across app restarts.
 */
class DefaultViewModelProvider private constructor() : ViewModelProvider {

    companion object {
        // Single instance - created lazily on first access
        // In KMP, object initialization is thread-safe
        private val _instance: DefaultViewModelProvider by lazy { DefaultViewModelProvider() }

        /**
         * Get the singleton instance of DefaultViewModelProvider.
         * This ensures a single Settings instance is used throughout the app.
         */
        fun getInstance(): DefaultViewModelProvider = _instance
    }

    override val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()

    // Lazy-initialized core dependencies
    private val httpClient: HttpClient by lazy { HttpClientProvider.create() }
    private val settings: Settings by lazy { Settings() }
    private val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    // Lazy-initialized database for offline caching
    private val database: FleetDatabase by lazy { FleetDatabase(settings, json) }

    private val log = co.touchlab.kermit.Logger.withTag("DefaultViewModelProvider")

    init {
        // Register session clear callback with AuthenticationManager
        // This ensures the session is cleared before redirecting to login on 401
        AuthenticationManager.registerSessionClearCallback {
            log.w { "Session clear callback invoked - clearing auth data only" }
            // Only clear auth-related data, not all settings
            userLocalDataSource.clearSession()
        }
    }

    // Google Places API Service (for location search)
    // Note: Replace with your actual Google Places API key
    private val googlePlacesService: GooglePlacesService? by lazy {
        val apiKey = getGooglePlacesApiKey()
        if (apiKey.isNotBlank()) {
            GooglePlacesService(httpClient, apiKey)
        } else {
            null
        }
    }

    /**
     * Gets the Google Places API key from ApiConfig.
     * Configure your API key in ApiConfig.GOOGLE_PLACES_API_KEY
     */
    private fun getGooglePlacesApiKey(): String {
        val apiKey = com.indusjs.fleet.core.network.ApiConfig.GOOGLE_PLACES_API_KEY
        // Return empty if placeholder key is still set
        return if (apiKey == "YOUR_GOOGLE_PLACES_API_KEY_HERE" || apiKey.isBlank()) {
            ""
        } else {
            apiKey
        }
    }

    // Lazy-initialized User feature dependencies
    private val userLocalDataSource by lazy { UserLocalDataSourceImpl(settings) }
    private val userRemoteDataSource by lazy { UserRemoteDataSourceImpl(httpClient) }
    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userRemoteDataSource, userLocalDataSource)
    }

    // Lazy-initialized Vehicle feature dependencies
    private val vehicleMapper by lazy { VehicleMapper() }
    private val vehicleRemoteDataSource by lazy { VehicleRemoteDataSourceImpl(httpClient) }
    private val vehicleRepository: VehicleRepository by lazy {
        VehicleRepositoryImpl(vehicleRemoteDataSource, userLocalDataSource, vehicleMapper)
    }
    private val getVehiclesUseCase by lazy { GetVehiclesUseCase(vehicleRepository) }
    private val getAvailableVehiclesUseCase by lazy { GetAvailableVehiclesUseCase(vehicleRepository) }
    private val getVehicleByIdUseCase by lazy { GetVehicleByIdUseCase(vehicleRepository) }
    private val updateVehicleUseCase by lazy { UpdateVehicleUseCase(vehicleRepository) }
    private val deleteVehicleUseCase by lazy { DeleteVehicleUseCase(vehicleRepository) }
    private val createVehicleWithDocumentsUseCase by lazy { CreateVehicleWithDocumentsUseCase(vehicleRepository) }

    // Lazy-initialized Driver feature dependencies
    private val driverMapper by lazy { DriverMapper() }
    private val driverRemoteDataSource by lazy { DriverRemoteDataSourceImpl(httpClient) }
    private val driverRepository: DriverRepository by lazy {
        DriverRepositoryImpl(driverRemoteDataSource, userLocalDataSource, driverMapper)
    }
    private val getDriversUseCase by lazy { GetDriversUseCase(driverRepository) }
    private val getAvailableDriversUseCase by lazy { GetAvailableDriversUseCase(driverRepository) }
    private val getDriverByIdUseCase by lazy { GetDriverByIdUseCase(driverRepository) }
    private val createDriverUseCase by lazy { CreateDriverUseCase(driverRepository) }
    private val updateDriverUseCase by lazy { UpdateDriverUseCase(driverRepository) }
    private val deleteDriverUseCase by lazy { DeleteDriverUseCase(driverRepository) }
    private val updateDriverStatusUseCase by lazy { UpdateDriverStatusUseCase(driverRepository) }
    private val toggleDriverActiveUseCase by lazy { ToggleDriverActiveUseCase(driverRepository) }

    // Lazy-initialized Trip feature dependencies
    private val tripMapper by lazy { TripMapper() }
    private val tripStopMapper by lazy { TripStopMapper() }
    private val tripRemoteDataSource by lazy { TripRemoteDataSourceImpl(httpClient) }
    private val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(tripRemoteDataSource, userLocalDataSource, tripMapper, tripStopMapper)
    }
    private val getTripsUseCase by lazy { GetTripsUseCase(tripRepository) }
    private val getTripByIdUseCase by lazy { GetTripByIdUseCase(tripRepository) }
    private val createTripWithDataUseCase by lazy { CreateTripWithDataUseCase(tripRepository) }
    private val updateTripUseCase by lazy { UpdateTripUseCase(tripRepository) }
    private val updateTripStatusUseCase by lazy { UpdateTripStatusUseCase(tripRepository) }
    private val cancelTripUseCase by lazy { CancelTripUseCase(tripRepository) }

    // Lazy-initialized Team feature dependencies
    private val teamRemoteDataSource by lazy { TeamRemoteDataSourceImpl(httpClient) }
    private val teamLocalDataSource by lazy { TeamLocalDataSourceImpl(database.teamMembersDao()) }
    private val teamRepository: TeamRepository by lazy {
        TeamRepositoryImpl(teamRemoteDataSource, userLocalDataSource, teamLocalDataSource)
    }

    // Lazy-initialized Dashboard feature dependencies with Settings-based caching
    private val dashboardRemoteDataSource by lazy { DashboardRemoteDataSourceImpl(httpClient) }
    private val dashboardCacheMapper by lazy { DashboardCacheMapper(json) }
    private val dashboardLocalDataSource by lazy {
        DashboardLocalDataSourceImpl(database.dashboardDao(), dashboardCacheMapper)
    }
    private val dashboardRepository: DashboardRepository by lazy {
        DashboardRepositoryImpl(dashboardRemoteDataSource, dashboardLocalDataSource, userLocalDataSource)
    }
    private val getDashboardUseCase by lazy { GetDashboardUseCase(dashboardRepository) }
    private val refreshDashboardUseCase by lazy { RefreshDashboardUseCase(dashboardRepository) }
    private val getCostOverviewUseCase by lazy { GetCostOverviewUseCase(dashboardRepository) }
    private val getPendingPaymentsUseCase by lazy { GetPendingPaymentsUseCase(dashboardRepository) }
    private val getAlertsStatusUseCase by lazy { GetAlertsStatusUseCase(dashboardRepository) }

    // Lazy-initialized Costs feature dependencies
    private val costsRemoteDataSource by lazy { CostsRemoteDataSourceImpl(httpClient) }
    private val costsLocalDataSource by lazy { CostsLocalDataSourceImpl(database.costTypesDao(), json) }
    private val costsRepository: CostsRepository by lazy {
        CostsRepositoryImpl(costsRemoteDataSource, userLocalDataSource)
    }

    // Cost Types Repository and Use Cases
    private val costTypesRepository: CostTypesRepository by lazy {
        CostTypesRepositoryImpl(costsRemoteDataSource, costsLocalDataSource, userLocalDataSource)
    }
    private val initializeCostTypesUseCase by lazy { InitializeCostTypesUseCase(costTypesRepository) }
    private val getTripCostTypesUseCase by lazy { GetTripCostTypesUseCase(costTypesRepository) }
    private val getMaintenanceCostTypesUseCase by lazy { GetMaintenanceCostTypesUseCase(costTypesRepository) }
    private val getDriverCostTypesUseCase by lazy { GetDriverCostTypesUseCase(costTypesRepository) }

    // App Initializer - handles one-time initialization tasks
    val appInitializer: AppInitializer by lazy {
        AppInitializer(initializeCostTypesUseCase, dispatcherProvider)
    }

    // Lazy-initialized Reports feature dependencies
    private val reportsRemoteDataSource by lazy { ReportsRemoteDataSource(httpClient, json) }
    private val reportsRepository: ReportsRepository by lazy {
        ReportsRepositoryImpl(reportsRemoteDataSource, userLocalDataSource)
    }

    // Lazy-initialized Customer feature dependencies
    private val customerRemoteDataSource by lazy { CustomerRemoteDataSource(httpClient, json) }
    private val customerLocalDataSource by lazy { CustomerLocalDataSourceImpl(database.customerDao()) }
    private val customerRepository by lazy {
        CustomerRepositoryImpl(customerRemoteDataSource, customerLocalDataSource, userLocalDataSource)
    }
    private val getCustomersUseCase by lazy { GetCustomersUseCase(customerRepository) }
    private val getCustomerUseCase by lazy { GetCustomerUseCase(customerRepository) }
    private val getLocalCustomersUseCase by lazy { GetLocalCustomersUseCase(customerRepository) }
    private val createCustomerUseCase by lazy { CreateCustomerUseCase(customerRepository) }
    private val updateCustomerUseCase by lazy { UpdateCustomerUseCase(customerRepository) }
    private val toggleCustomerStatusUseCase by lazy { ToggleCustomerStatusUseCase(customerRepository) }
    private val getCustomerStatisticsUseCase by lazy { GetCustomerStatisticsUseCase(customerRepository) }
    private val refreshCustomersUseCase by lazy { RefreshCustomersUseCase(customerRepository) }

    // Lazy-initialized Payment feature dependencies
    private val tripPaymentRemoteDataSource by lazy { TripPaymentRemoteDataSource(httpClient, json) }
    private val tripPaymentRepository: TripPaymentRepository by lazy {
        TripPaymentRepositoryImpl(tripPaymentRemoteDataSource, dashboardRemoteDataSource, userLocalDataSource)
    }

    // Lazy-initialized Vehicle Finance feature dependencies
    private val vehicleFinanceRemoteDataSource by lazy { VehicleFinanceRemoteDataSourceImpl(httpClient, json) }
    private val vehicleFinanceRepository: VehicleFinanceRepository by lazy {
        VehicleFinanceRepositoryImpl(vehicleFinanceRemoteDataSource, userLocalDataSource, dispatcherProvider)
    }

    // Auth ViewModels
    override fun loginViewModel() = LoginViewModel(dispatcherProvider, userRepository)
    override fun signUpViewModel() = SignUpViewModel(dispatcherProvider, userRepository)
    override fun forgotPasswordViewModel() = ForgotPasswordViewModel(dispatcherProvider, userRepository)

    // User ViewModels
    override fun profileViewModel() = ProfileViewModel(dispatcherProvider, userRepository)
    override fun changePasswordViewModel() = ChangePasswordViewModel(dispatcherProvider, userRepository)

    // Feature ViewModels
    override fun dashboardViewModel() = DashboardViewModel(
        dispatcherProvider,
        getDashboardUseCase,
        refreshDashboardUseCase,
        getCostOverviewUseCase,
        getPendingPaymentsUseCase,
        getAlertsStatusUseCase
    )

    override fun vehiclesViewModel() = VehiclesViewModel(
        dispatcherProvider,
        getVehiclesUseCase,
        deleteVehicleUseCase
    )

    override fun addVehicleViewModel() = AddVehicleViewModel(
        dispatcherProvider,
        createVehicleWithDocumentsUseCase,
        teamRepository
    )

    override fun vehicleDetailViewModel() = VehicleDetailViewModel(
        dispatcherProvider,
        getVehicleByIdUseCase,
        updateVehicleUseCase,
        deleteVehicleUseCase,
        vehicleRepository,
        getDriversUseCase,
        costsRepository,
        teamRepository,
        costTypesRepository
    )

    override fun driversViewModel() = DriversViewModel(
        dispatcherProvider,
        getDriversUseCase,
        deleteDriverUseCase,
        updateDriverStatusUseCase,
        toggleDriverActiveUseCase
    )

    override fun createDriverViewModel() = CreateDriverViewModel(
        dispatcherProvider,
        createDriverUseCase,
        teamRepository
    )

    override fun driverDetailViewModel() = DriverDetailViewModel(
        dispatcherProvider,
        getDriverByIdUseCase,
        updateDriverUseCase,
        updateDriverStatusUseCase,
        toggleDriverActiveUseCase,
        deleteDriverUseCase,
        driverRepository,
        teamRepository,
        costsRepository,
        userLocalDataSource
    )

    override fun tripsViewModel() = TripsViewModel(
        dispatcherProvider,
        getTripsUseCase,
        cancelTripUseCase
    )

    override fun createTripViewModel() = CreateTripViewModel(
        dispatcherProvider,
        getAvailableVehiclesUseCase,
        getAvailableDriversUseCase,
        createTripWithDataUseCase,
        userLocalDataSource,
        googlePlacesService,
        customerRepository
    )

    override fun tripDetailViewModel() = TripDetailViewModel(
        dispatcherProvider,
        getTripByIdUseCase,
        updateTripStatusUseCase,
        cancelTripUseCase,
        costsRepository,
        tripRepository,
        getVehiclesUseCase,
        getDriversUseCase,
        userLocalDataSource,
        googlePlacesService,
        customerRepository
    )

    override fun mapsViewModel() = MapsViewModel(dispatcherProvider)

    override fun teamListViewModel() = TeamListViewModel(dispatcherProvider, teamRepository, userLocalDataSource)

    override fun createTeamMemberViewModel() = CreateTeamMemberViewModel(dispatcherProvider, teamRepository, userLocalDataSource)

    override fun teamMemberDetailViewModel() = TeamMemberDetailViewModel(dispatcherProvider, teamRepository, userLocalDataSource)

    override fun tripCostEntryViewModel() = TripCostEntryViewModel(
        dispatcherProvider,
        tripRepository,
        costsRepository,
        costTypesRepository,
        getTripCostTypesUseCase
    )

    override fun maintenanceCostEntryViewModel() = MaintenanceCostEntryViewModel(
        dispatcherProvider,
        vehicleRepository,
        costsRepository,
        costTypesRepository,
        getMaintenanceCostTypesUseCase
    )

    override fun driverCostEntryViewModel() = DriverCostEntryViewModel(
        dispatcherProvider,
        driverRepository,
        costsRepository,
        costTypesRepository,
        getDriverCostTypesUseCase
    )

    override fun alertsListViewModel() = AlertsListViewModel(
        dispatcherProvider,
        getAlertsStatusUseCase
    )

    // Reports ViewModels
    override fun reportsViewModel() = ReportsViewModel(reportsRepository)

    override fun vehiclePLViewModel() = VehiclePLViewModel(reportsRepository, vehicleRepository)

    override fun tripPLViewModel() = TripPLViewModel(reportsRepository, tripRepository)

    override fun costAnalysisViewModel() = CostAnalysisViewModel(reportsRepository)

    override fun consolidatedPLViewModel() = ConsolidatedPLViewModel(reportsRepository, vehicleRepository)

    // Customer ViewModels
    override fun customersListViewModel() = CustomersListViewModel(
        getCustomersUseCase,
        refreshCustomersUseCase,
        getLocalCustomersUseCase
    )

    override fun customerDetailViewModel() = CustomerDetailViewModel(customerRepository)

    override fun createCustomerViewModel() = CreateCustomerViewModel(createCustomerUseCase)

    // Payment ViewModels
    override fun paymentsViewModel() = PaymentsViewModel(tripPaymentRepository)

    override fun addPaymentViewModel() = AddPaymentViewModel(tripPaymentRepository, tripRepository)

    override fun paymentDetailViewModel() = PaymentDetailViewModel(tripPaymentRepository, userRepository)

    // Vehicle Finance ViewModels
    override fun vehicleFinanceViewModel() = VehicleFinanceViewModel(vehicleRepository, vehicleFinanceRepository, dispatcherProvider)
}
