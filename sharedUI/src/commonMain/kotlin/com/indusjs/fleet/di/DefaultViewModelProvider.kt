package com.indusjs.fleet.di

import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.core.init.AppInitializer
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.dispatcher.DefaultDispatcherProvider
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.network.HttpClientProvider
import com.indusjs.fleet.data.database.FleetDatabase
import com.indusjs.logger.IjsLogger
import com.indusjs.fleet.data.datasource.costs.CostsLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.dashboard.DashboardLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.states.StatesLocalDataSourceImpl
import com.ijs.team.data.datasource.TeamLocalDataSourceImpl
import com.ijs.customer.data.datasource.CustomerLocalDataSourceImpl
import com.indusjs.fleet.data.mapper.dashboard.DashboardCacheMapper
// Domain repository interfaces
import com.indusjs.fleet.domain.repository.costs.CostTypesRepository
import com.indusjs.fleet.domain.repository.costs.CostsRepository
import com.ijs.customer.domain.repository.CustomerRepository
import com.indusjs.fleet.domain.repository.dashboard.DashboardRepository
import com.ijs.driver.domain.repository.DriverRepository
import com.ijs.finance.domain.repository.VehicleFinanceRepository
import com.ijs.trip.payment.domain.repository.TripPaymentRepository
import com.ijs.reports.domain.repository.ReportsRepository
import com.ijs.reports.domain.usecase.GetConsolidatedPLUseCase
import com.ijs.reports.domain.usecase.GetMultiCostTypeAnalysisUseCase
import com.ijs.reports.domain.usecase.GetMultiTripPLUseCase
import com.ijs.reports.domain.usecase.GetMultiVehiclePLUseCase
import com.ijs.reports.domain.usecase.GetPLSummaryUseCase
import com.ijs.reports.domain.usecase.GetVehicleProfitLossUseCase
import com.ijs.team.domain.repository.TeamRepository
import com.ijs.trip.domain.repository.TripRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.ijs.vehicle.domain.repository.VehicleRepository
import com.indusjs.fleet.domain.usecase.costs.GetMaintenanceCostTypesUseCase
import com.indusjs.fleet.domain.usecase.costs.GetDriverCostTypesUseCase
import com.indusjs.fleet.domain.usecase.costs.GetTripCostTypesUseCase
import com.indusjs.fleet.domain.usecase.costs.InitializeCostTypesUseCase
import com.indusjs.fleet.domain.usecase.states.InitializeStatesUseCase
import com.ijs.customer.domain.usecase.CreateCustomerUseCase
import com.ijs.customer.domain.usecase.GetCustomersUseCase
import com.ijs.customer.domain.usecase.GetLocalCustomersUseCase
import com.ijs.customer.domain.usecase.RefreshCustomersUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetAlertsStatusUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetCostOverviewUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetDashboardUseCase
import com.indusjs.fleet.domain.usecase.dashboard.GetPendingPaymentsUseCase
import com.indusjs.fleet.domain.usecase.dashboard.RefreshDashboardUseCase
import com.ijs.driver.domain.usecase.CreateDriverUseCase
import com.ijs.driver.domain.usecase.DeleteDriverUseCase
import com.ijs.driver.domain.usecase.GetAvailableDriversUseCase
import com.ijs.driver.domain.usecase.GetDriverByIdUseCase
import com.ijs.driver.domain.usecase.GetDriversUseCase
import com.ijs.driver.domain.usecase.ToggleDriverActiveUseCase
import com.ijs.driver.domain.usecase.UpdateDriverStatusUseCase
import com.ijs.driver.domain.usecase.UpdateDriverUseCase
import com.ijs.trip.domain.usecase.CancelTripUseCase
import com.ijs.trip.domain.usecase.CreateTripWithDataUseCase
import com.ijs.trip.domain.usecase.GetTripByIdUseCase
import com.ijs.trip.domain.usecase.GetTripsUseCase
import com.ijs.trip.domain.usecase.UpdateTripStatusUseCase
import com.ijs.trip.domain.usecase.UpdateTripUseCase
import com.ijs.vehicle.domain.usecase.CreateVehicleWithDocumentsUseCase
import com.ijs.vehicle.domain.usecase.DeleteVehicleUseCase
import com.ijs.vehicle.domain.usecase.GetAvailableVehiclesUseCase
import com.ijs.vehicle.domain.usecase.GetVehicleByIdUseCase
import com.ijs.vehicle.domain.usecase.GetVehiclesUseCase
import com.ijs.vehicle.domain.usecase.UpdateVehicleUseCase
import com.ijs.alerts.presentation.AlertsListViewModel
import com.ijs.user.presentation.login.LoginViewModel
import com.ijs.onboarding.presentation.OnboardingViewModel
import com.ijs.customer.presentation.create.CreateCustomerViewModel
import com.ijs.customer.presentation.detail.CustomerDetailViewModel
import com.ijs.customer.presentation.list.CustomersListViewModel
import com.ijs.dashboard.presentation.DashboardViewModel
import com.ijs.driver.presentation.DriversViewModel
import com.ijs.driver.presentation.cost.DriverCostEntryViewModel
import com.ijs.driver.presentation.create.CreateDriverViewModel
import com.ijs.driver.presentation.detail.DriverDetailViewModel
import com.ijs.finance.presentation.VehicleFinanceViewModel
import com.ijs.map.presentation.MapsViewModel
import com.ijs.trip.payment.presentation.AddPaymentViewModel
import com.ijs.trip.payment.presentation.PaymentDetailViewModel
import com.ijs.trip.payment.presentation.PaymentsViewModel
import com.ijs.reports.presentation.ReportsViewModel
import com.ijs.reports.presentation.consolidated.ConsolidatedPLViewModel
import com.ijs.reports.presentation.cost.CostAnalysisViewModel
import com.ijs.reports.presentation.trip.TripPLViewModel
import com.ijs.reports.presentation.vehicle.VehiclePLViewModel
import com.ijs.team.presentation.create.CreateTeamMemberViewModel
import com.ijs.team.presentation.detail.TeamMemberDetailViewModel
import com.ijs.team.presentation.list.TeamListViewModel
import com.ijs.trip.presentation.TripsViewModel
import com.ijs.trip.presentation.cost.TripCostEntryViewModel
import com.ijs.trip.presentation.create.CreateTripViewModel
import com.ijs.trip.presentation.detail.TripDetailViewModel
import com.ijs.user.presentation.changepassword.ChangePasswordViewModel
import com.ijs.user.presentation.forgotpassword.ForgotPasswordViewModel
import com.ijs.user.presentation.profile.ProfileViewModel
import com.ijs.user.presentation.signup.SignUpViewModel
import com.ijs.vehicle.presentation.AddVehicleViewModel
import com.ijs.vehicle.presentation.VehiclesViewModel
import com.ijs.vehicle.presentation.costs.MaintenanceCostEntryViewModel
import com.ijs.vehicle.presentation.detail.VehicleDetailViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Default implementation of ViewModelProvider.
 *
 * Uses Metro DI via [NetworkDataGraph] for all data layer wiring
 * (data sources, mappers, repositories). ViewModel creation remains
 * manual here, receiving dependencies from the graph.
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
    private val database: FleetDatabase by lazy { FleetDatabase(settings, json, fleetLogger) }

    // Single FleetLogger instance shared across the entire app
    val fleetLogger: FleetLogger by lazy { IjsLogger.createFleetLogger() }

    private val TAG = "DefaultViewModelProvider"

    init {
        // Register session clear callback with AuthenticationManager
        // This ensures the session is cleared before redirecting to login on 401
        AuthenticationManager.registerSessionClearCallback {
            fleetLogger.w(TAG, "Session clear callback invoked - clearing auth data only")
            // Only clear auth-related data, not all settings
            // Access networkDataGraph lazily — it's initialized on first use
            networkDataGraph.userLocalDataSource.clearSession()
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

    // ==================== NetworkDataGraph (ijs-network-lib) ====================
    // NetworkDataGraph manages ONLY shared/infrastructure repositories:
    // - User/Auth (login, profile, tokens)
    // - Dashboard (aggregation service)
    // - Costs/CostTypes (cross-cutting cost type cache)
    //
    // Feature-specific repositories are constructed directly below from
    // their respective feature modules (ijs-vehicle-lib, ijs-trip-lib, etc.).

    // Room-backed local data sources (impls live in sharedUI, interfaces in feature libs)
    private val dashboardCacheMapper by lazy { DashboardCacheMapper(json) }
    private val dashboardLocalDataSource by lazy {
        DashboardLocalDataSourceImpl(database.dashboardDao(), dashboardCacheMapper)
    }
    private val costsLocalDataSource by lazy { CostsLocalDataSourceImpl(database.costTypesDao(), json, fleetLogger) }
    private val statesLocalDataSource by lazy { StatesLocalDataSourceImpl(database.statesDao(), json, fleetLogger) }
    private val teamLocalDataSource by lazy { TeamLocalDataSourceImpl(database.teamMembersDao()) }
    private val customerLocalDataSource by lazy { CustomerLocalDataSourceImpl(database.customerDao(), fleetLogger) }

    /**
     * The NetworkDataGraph that wires shared data layer dependencies.
     * Only manages: user, dashboard, costs, costTypes repositories.
     */
    private val networkDataGraph: NetworkDataGraph by lazy {
        NetworkDataGraph.create(
            httpClient = httpClient,
            json = json,
            settings = settings,
            dispatcherProvider = dispatcherProvider,
            dashboardLocalDataSource = dashboardLocalDataSource,
            costsLocalDataSource = costsLocalDataSource,
            statesLocalDataSource = statesLocalDataSource,
            logger = fleetLogger
        )
    }

    // ==================== Feature Repositories (from feature libs) ====================
    // Constructed via FeatureRepositoryFactory from each feature module's classes.

    private val featureRepos: FeatureRepositoryFactory by lazy {
        FeatureRepositoryFactory(
            httpClient = httpClient,
            json = json,
            settings = settings,
            dispatcherProvider = dispatcherProvider,
            teamLocalDataSource = teamLocalDataSource,
            customerLocalDataSource = customerLocalDataSource,
            logger = fleetLogger
        )
    }

    private val userLocalDataSource get() = networkDataGraph.userLocalDataSource

    // ==================== Repository Accessors ====================

    override val userRepository: UserRepository get() = networkDataGraph.userRepository
    private val vehicleRepository: VehicleRepository get() = featureRepos.vehicleRepository
    private val driverRepository: DriverRepository get() = featureRepos.driverRepository
    private val tripRepository: TripRepository get() = featureRepos.tripRepository
    private val dashboardRepository: DashboardRepository get() = networkDataGraph.dashboardRepository
    private val customerRepository: CustomerRepository get() = featureRepos.customerRepository
    private val teamRepository: TeamRepository get() = featureRepos.teamRepository
    private val tripPaymentRepository: TripPaymentRepository get() = featureRepos.tripPaymentRepository
    private val tripProviderAdapter by lazy { com.indusjs.fleet.di.adapter.TripProviderAdapter(tripRepository) }
    private val costsRepository: CostsRepository get() = networkDataGraph.costsRepository
    private val costTypesRepository: CostTypesRepository get() = networkDataGraph.costTypesRepository
    private val statesRepository get() = networkDataGraph.statesRepository
    private val vehicleFinanceRepository: VehicleFinanceRepository get() = featureRepos.vehicleFinanceRepository
    private val reportsRepository: ReportsRepository get() = featureRepos.reportsRepository

    // ==================== Use Cases (created from graph-provided repositories) ====================

    // Reports use cases
    private val getPLSummaryUseCase by lazy { GetPLSummaryUseCase(reportsRepository) }
    private val getVehicleProfitLossUseCase by lazy { GetVehicleProfitLossUseCase(reportsRepository) }
    private val getMultiVehiclePLUseCase by lazy { GetMultiVehiclePLUseCase(reportsRepository) }
    private val getMultiTripPLUseCase by lazy { GetMultiTripPLUseCase(reportsRepository) }
    private val getMultiCostTypeAnalysisUseCase by lazy { GetMultiCostTypeAnalysisUseCase(reportsRepository) }
    private val getConsolidatedPLUseCase by lazy { GetConsolidatedPLUseCase(reportsRepository) }

    // Vehicle use cases
    private val getVehiclesUseCase by lazy { GetVehiclesUseCase(vehicleRepository) }
    private val getAvailableVehiclesUseCase by lazy { GetAvailableVehiclesUseCase(vehicleRepository) }
    private val getVehicleByIdUseCase by lazy { GetVehicleByIdUseCase(vehicleRepository) }
    private val updateVehicleUseCase by lazy { UpdateVehicleUseCase(vehicleRepository) }
    private val deleteVehicleUseCase by lazy { DeleteVehicleUseCase(vehicleRepository) }
    private val createVehicleWithDocumentsUseCase by lazy { CreateVehicleWithDocumentsUseCase(vehicleRepository) }

    // Driver use cases
    private val getDriversUseCase by lazy { GetDriversUseCase(driverRepository) }
    private val getAvailableDriversUseCase by lazy { GetAvailableDriversUseCase(driverRepository) }
    private val getDriverByIdUseCase by lazy { GetDriverByIdUseCase(driverRepository) }
    private val createDriverUseCase by lazy { CreateDriverUseCase(driverRepository) }
    private val updateDriverUseCase by lazy { UpdateDriverUseCase(driverRepository) }
    private val deleteDriverUseCase by lazy { DeleteDriverUseCase(driverRepository) }
    private val updateDriverStatusUseCase by lazy { UpdateDriverStatusUseCase(driverRepository) }
    private val toggleDriverActiveUseCase by lazy { ToggleDriverActiveUseCase(driverRepository) }

    // Trip use cases
    private val getTripsUseCase by lazy { GetTripsUseCase(tripRepository) }
    private val getTripByIdUseCase by lazy { GetTripByIdUseCase(tripRepository) }
    private val createTripWithDataUseCase by lazy { CreateTripWithDataUseCase(tripRepository) }
    private val updateTripUseCase by lazy { UpdateTripUseCase(tripRepository) }
    private val updateTripStatusUseCase by lazy { UpdateTripStatusUseCase(tripRepository) }
    private val cancelTripUseCase by lazy { CancelTripUseCase(tripRepository) }

    // Dashboard use cases
    private val getDashboardUseCase by lazy { GetDashboardUseCase(dashboardRepository) }
    private val refreshDashboardUseCase by lazy { RefreshDashboardUseCase(dashboardRepository) }
    private val getCostOverviewUseCase by lazy { GetCostOverviewUseCase(dashboardRepository) }
    private val getPendingPaymentsUseCase by lazy { GetPendingPaymentsUseCase(dashboardRepository) }
    private val getAlertsStatusUseCase by lazy { GetAlertsStatusUseCase(dashboardRepository) }

    // Cost Types use cases
    private val initializeCostTypesUseCase by lazy { InitializeCostTypesUseCase(costTypesRepository, fleetLogger) }
    private val getTripCostTypesUseCase by lazy { GetTripCostTypesUseCase(costTypesRepository) }
    private val getMaintenanceCostTypesUseCase by lazy { GetMaintenanceCostTypesUseCase(costTypesRepository) }
    private val getDriverCostTypesUseCase by lazy { GetDriverCostTypesUseCase(costTypesRepository) }

    // States use cases
    private val initializeStatesUseCase by lazy { InitializeStatesUseCase(statesRepository, fleetLogger) }

    // Customer use cases
    private val getCustomersUseCase by lazy { GetCustomersUseCase(customerRepository) }
    private val getLocalCustomersUseCase by lazy { GetLocalCustomersUseCase(customerRepository) }
    private val createCustomerUseCase by lazy { CreateCustomerUseCase(customerRepository) }
    private val refreshCustomersUseCase by lazy { RefreshCustomersUseCase(customerRepository) }

    // App Initializer - handles one-time initialization tasks
    val appInitializer: AppInitializer by lazy {
        AppInitializer(initializeCostTypesUseCase, initializeStatesUseCase, dispatcherProvider, fleetLogger)
    }

    // Onboarding
    override fun hasCompletedOnboarding(): Boolean =
        settings.getBoolean(OnboardingViewModel.KEY_ONBOARDING_COMPLETED, false)

    override fun onboardingViewModel() = OnboardingViewModel(settings)

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
        deleteVehicleUseCase,
        statesRepository
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
        costTypesRepository,
        statesRepository = statesRepository
    )

    override fun driversViewModel() = DriversViewModel(
        dispatcherProvider,
        getDriversUseCase,
        deleteDriverUseCase,
        updateDriverStatusUseCase,
        toggleDriverActiveUseCase,
        statesRepository
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
        userLocalDataSource,
        statesRepository = statesRepository
    )

    override fun tripsViewModel() = TripsViewModel(
        dispatcherProvider,
        getTripsUseCase,
        cancelTripUseCase,
        statesRepository
    )

    override fun createTripViewModel() = CreateTripViewModel(
        dispatcherProvider,
        getAvailableVehiclesUseCase,
        getAvailableDriversUseCase,
        createTripWithDataUseCase,
        userLocalDataSource,
        googlePlacesService,
        customerRepository,
        fleetLogger
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
        customerRepository,
        tripPaymentRepository,
        fleetLogger,
        statesRepository = statesRepository
    )

    override fun mapsViewModel() = MapsViewModel(dispatcherProvider)

    override fun teamListViewModel() = TeamListViewModel(dispatcherProvider, teamRepository, userLocalDataSource)

    override fun createTeamMemberViewModel() = CreateTeamMemberViewModel(dispatcherProvider, teamRepository, userLocalDataSource)

    override fun teamMemberDetailViewModel() =
        TeamMemberDetailViewModel(dispatcherProvider, teamRepository, userLocalDataSource, fleetLogger)

    override fun tripCostEntryViewModel() = TripCostEntryViewModel(
        dispatcherProvider,
        tripRepository,
        costsRepository,
        costTypesRepository,
        getTripCostTypesUseCase,
        fleetLogger
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
    override fun reportsViewModel() = ReportsViewModel(getPLSummaryUseCase, fleetLogger)

    override fun vehiclePLViewModel() = VehiclePLViewModel(
        getVehicleProfitLossUseCase, getMultiVehiclePLUseCase, vehicleRepository, fleetLogger
    )

    override fun tripPLViewModel() = TripPLViewModel(getMultiTripPLUseCase, tripRepository)

    override fun costAnalysisViewModel() = CostAnalysisViewModel(getMultiCostTypeAnalysisUseCase)

    override fun consolidatedPLViewModel() = ConsolidatedPLViewModel(getConsolidatedPLUseCase, vehicleRepository)

    // Customer ViewModels
    override fun customersListViewModel() = CustomersListViewModel(
        getCustomersUseCase,
        refreshCustomersUseCase,
        getLocalCustomersUseCase
    )

    override fun customerDetailViewModel() = CustomerDetailViewModel(customerRepository, userLocalDataSource, fleetLogger)

    override fun createCustomerViewModel() = CreateCustomerViewModel(createCustomerUseCase)

    // Payment ViewModels
    override fun paymentsViewModel() = PaymentsViewModel(tripPaymentRepository, fleetLogger, statesRepository)

    override fun addPaymentViewModel() = AddPaymentViewModel(tripPaymentRepository, tripProviderAdapter, fleetLogger, statesRepository)

    override fun paymentDetailViewModel() = PaymentDetailViewModel(tripPaymentRepository, userRepository, fleetLogger, statesRepository)

    // Vehicle Finance ViewModels
    override fun vehicleFinanceViewModel() = VehicleFinanceViewModel(vehicleRepository, vehicleFinanceRepository, dispatcherProvider, fleetLogger)
}
