package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DefaultDispatcherProvider
import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.network.HttpClientProvider
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.trip.TripRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.vehicle.VehicleRemoteDataSourceImpl
import com.indusjs.fleet.data.mapper.trip.TripMapper
import com.indusjs.fleet.data.mapper.vehicle.VehicleMapper
import com.indusjs.fleet.data.repository.team.TeamRepositoryImpl
import com.indusjs.fleet.data.repository.trip.TripRepositoryImpl
import com.indusjs.fleet.data.repository.user.UserRepositoryImpl
import com.indusjs.fleet.data.repository.vehicle.VehicleRepositoryImpl
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.domain.repository.trip.TripRepository
import com.indusjs.fleet.domain.repository.user.UserRepository
import com.indusjs.fleet.domain.repository.vehicle.VehicleRepository
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripsUseCase
import com.indusjs.fleet.domain.usecase.vehicle.CreateVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.DeleteVehicleUseCase
import com.indusjs.fleet.domain.usecase.vehicle.GetVehiclesUseCase
import com.indusjs.fleet.presentation.auth.LoginViewModel
import com.indusjs.fleet.presentation.dashboard.DashboardViewModel
import com.indusjs.fleet.presentation.drivers.DriversViewModel
import com.indusjs.fleet.presentation.maps.MapsViewModel
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel
import com.indusjs.fleet.presentation.team.list.TeamListViewModel
import com.indusjs.fleet.presentation.trips.TripsViewModel
import com.indusjs.fleet.presentation.user.changepassword.ChangePasswordViewModel
import com.indusjs.fleet.presentation.user.forgotpassword.ForgotPasswordViewModel
import com.indusjs.fleet.presentation.user.profile.ProfileViewModel
import com.indusjs.fleet.presentation.user.signup.SignUpViewModel
import com.indusjs.fleet.presentation.vehicles.AddVehicleViewModel
import com.indusjs.fleet.presentation.vehicles.VehiclesViewModel
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
    private val deleteVehicleUseCase by lazy { DeleteVehicleUseCase(vehicleRepository) }
    private val createVehicleUseCase by lazy { CreateVehicleUseCase(vehicleRepository) }

    // Lazy-initialized Trip feature dependencies
    private val tripMapper by lazy { TripMapper() }
    private val tripRemoteDataSource by lazy { TripRemoteDataSourceImpl(httpClient) }
    private val tripRepository: TripRepository by lazy {
        TripRepositoryImpl(tripRemoteDataSource, userLocalDataSource, tripMapper)
    }
    private val getTripsUseCase by lazy { GetTripsUseCase(tripRepository) }
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
    override fun dashboardViewModel() = DashboardViewModel(dispatcherProvider)

    override fun vehiclesViewModel() = VehiclesViewModel(
        dispatcherProvider,
        getVehiclesUseCase,
        deleteVehicleUseCase
    )

    override fun addVehicleViewModel() = AddVehicleViewModel(
        dispatcherProvider,
        createVehicleUseCase
    )

    override fun driversViewModel() = DriversViewModel(dispatcherProvider)

    override fun tripsViewModel() = TripsViewModel(
        dispatcherProvider,
        getTripsUseCase,
        cancelTripUseCase
    )

    override fun mapsViewModel() = MapsViewModel(dispatcherProvider)

    override fun teamListViewModel() = TeamListViewModel(dispatcherProvider, teamRepository)

    override fun createTeamMemberViewModel() = CreateTeamMemberViewModel(dispatcherProvider, teamRepository)
}

