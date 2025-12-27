package com.indusjs.fleet.di

import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.core.dispatcher.DefaultDispatcherProvider
import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.network.HttpClientProvider
import com.indusjs.fleet.data.datasource.driver.DriverRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.location.GooglePlacesService
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSourceImpl
import com.indusjs.fleet.data.mapper.driver.DriverMapper
import com.indusjs.fleet.data.mapper.trip.TripMapper
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.repository.driver.DriverRepositoryImpl
import com.indusjs.fleet.data.repository.team.TeamRepositoryImpl
import com.indusjs.fleet.data.repository.trip.TripRepositoryImpl
import com.indusjs.fleet.data.repository.user.UserRepositoryImpl
import com.indusjs.fleet.data.repository.vehicle.VehicleRepositoryImpl
import com.indusjs.fleet.domain.repository.driver.DriverRepository
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.domain.usecase.driver.CreateDriverUseCase
import com.indusjs.fleet.domain.usecase.driver.DeleteDriverUseCase
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
import com.indusjs.fleet.domain.usecase.vehicle.GetVehicleByIdUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.domain.usecase.vehicle.UpdateVehicleUseCase
import com.indusjs.fleet.presentation.auth.LoginViewModel
import com.indusjs.fleet.presentation.dashboard.DashboardViewModel
import com.indusjs.fleet.presentation.drivers.DriversViewModel
import com.indusjs.fleet.presentation.drivers.create.CreateDriverViewModel
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailViewModel
import com.indusjs.fleet.presentation.maps.MapsViewModel
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel
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
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient

/**
 * Development/Stub implementation of ViewModelProvider.
 * Creates dependencies manually without Metro code generation.
 *
 * In production with Metro DI fully configured, use MetroViewModelProvider instead.
 * This serves as a bridge during the migration to full Metro DI.
 */
class DefaultViewModelProvider : ViewModelProvider {

    override val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider()

    // Lazy-initialized core dependencies
    private val httpClient: HttpClient by lazy { HttpClientProvider.create() }
    private val settings: Settings by lazy { Settings() }

    init {
        // Register session clear callback with AuthenticationManager
        // This ensures the session is cleared before redirecting to login on 401
        AuthenticationManager.registerSessionClearCallback {
            settings.clear()
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
    private val userRepository: UserRepository by lazy {
        UserRepositoryImpl(userRemoteDataSource, userLocalDataSource)
    }

    // Lazy-initialized Vehicle feature dependencies
    private val vehicleMapper by lazy { VehicleMapper() }
    private val vehicleRemoteDataSource by lazy { VehicleRemoteDataSourceImpl(httpClient) }
    private val vehicleRepository: VehicleRepository by lazy {
        VehicleRepositoryImpl(vehicleRemoteDataSource, userLocalDataSource, vehicleMapper)
    }
    private val getVehiclesUseCase by lazy { GetVehiclesUseCase(vehicleRepository) }
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
    private val getDriverByIdUseCase by lazy { GetDriverByIdUseCase(driverRepository) }
    private val createDriverUseCase by lazy { CreateDriverUseCase(driverRepository) }
    private val updateDriverUseCase by lazy { UpdateDriverUseCase(driverRepository) }
    private val deleteDriverUseCase by lazy { DeleteDriverUseCase(driverRepository) }
    private val updateDriverStatusUseCase by lazy { UpdateDriverStatusUseCase(driverRepository) }
    private val toggleDriverActiveUseCase by lazy { ToggleDriverActiveUseCase(driverRepository) }

    // Lazy-initialized Trip feature dependencies
    private val tripMapper by lazy { TripMapper() }
    private val tripRemoteDataSource by lazy { TripRemoteDataSourceImpl(httpClient) }
    private val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(tripRemoteDataSource, userLocalDataSource, tripMapper)
    }
    private val getTripsUseCase by lazy { GetTripsUseCase(tripRepository) }
    private val getTripByIdUseCase by lazy { GetTripByIdUseCase(tripRepository) }
    private val createTripWithDataUseCase by lazy { CreateTripWithDataUseCase(tripRepository) }
    private val updateTripUseCase by lazy { UpdateTripUseCase(tripRepository) }
    private val updateTripStatusUseCase by lazy { UpdateTripStatusUseCase(tripRepository) }
    private val cancelTripUseCase by lazy { CancelTripUseCase(tripRepository) }

    // Lazy-initialized Team feature dependencies
    private val teamRemoteDataSource by lazy { TeamRemoteDataSourceImpl(httpClient) }
    private val teamRepository: TeamRepository by lazy {
        TeamRepositoryImpl(teamRemoteDataSource, userLocalDataSource)
    }

    // Auth ViewModels
    override fun loginViewModel() = LoginViewModel(dispatcherProvider, userRepository)
    override fun signUpViewModel() = SignUpViewModel(dispatcherProvider, userRepository)
    override fun forgotPasswordViewModel() = ForgotPasswordViewModel(dispatcherProvider, userRepository)

    // User ViewModels
    override fun profileViewModel() = ProfileViewModel(dispatcherProvider, userRepository)
    override fun changePasswordViewModel() = ChangePasswordViewModel(dispatcherProvider, userRepository)

    // Feature ViewModels
    override fun dashboardViewModel() = DashboardViewModel(dispatcherProvider, userRepository)

    override fun vehiclesViewModel() = VehiclesViewModel(
        dispatcherProvider,
        getVehiclesUseCase,
        deleteVehicleUseCase
    )

    override fun addVehicleViewModel() = AddVehicleViewModel(
        dispatcherProvider,
        createVehicleWithDocumentsUseCase
    )

    override fun vehicleDetailViewModel() = VehicleDetailViewModel(
        dispatcherProvider,
        getVehicleByIdUseCase,
        updateVehicleUseCase,
        deleteVehicleUseCase,
        vehicleRepository
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
        createDriverUseCase
    )

    override fun driverDetailViewModel() = DriverDetailViewModel(
        dispatcherProvider,
        getDriverByIdUseCase,
        updateDriverUseCase,
        updateDriverStatusUseCase,
        toggleDriverActiveUseCase,
        deleteDriverUseCase
    )

    override fun tripsViewModel() = TripsViewModel(
        dispatcherProvider,
        getTripsUseCase,
        cancelTripUseCase
    )

    override fun createTripViewModel() = CreateTripViewModel(
        dispatcherProvider,
        getVehiclesUseCase,
        getDriversUseCase,
        createTripWithDataUseCase,
        googlePlacesService
    )

    override fun tripDetailViewModel() = TripDetailViewModel(
        dispatcherProvider,
        getTripByIdUseCase,
        updateTripUseCase,
        updateTripStatusUseCase,
        cancelTripUseCase
    )

    override fun mapsViewModel() = MapsViewModel(dispatcherProvider)

    override fun teamListViewModel() = TeamListViewModel(dispatcherProvider, teamRepository)

    override fun createTeamMemberViewModel() = CreateTeamMemberViewModel(dispatcherProvider, teamRepository)
}

