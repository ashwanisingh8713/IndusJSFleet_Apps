package com.indusjs.fleet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.auth.AuthenticationEvent
import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.di.DefaultViewModelProvider
import com.indusjs.fleet.di.ProvideViewModels
import com.indusjs.fleet.di.rememberViewModel
import com.indusjs.fleet.presentation.auth.LoginScreen
import com.indusjs.fleet.presentation.dashboard.DashboardScreen
import com.indusjs.fleet.presentation.drivers.DriversScreen
import com.indusjs.fleet.presentation.drivers.create.CreateDriverScreen
import com.indusjs.fleet.presentation.drivers.detail.DriverDetailScreen
import com.indusjs.fleet.presentation.maps.MapsScreen
import com.indusjs.fleet.presentation.trips.TripsScreen
import com.indusjs.fleet.presentation.trips.create.CreateTripScreen
import com.indusjs.fleet.presentation.trips.detail.TripDetailScreen
import com.indusjs.fleet.presentation.vehicles.AddVehicleScreen
import com.indusjs.fleet.presentation.vehicles.VehiclesScreen
import com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailScreen
import com.indusjs.fleet.presentation.user.profile.ProfileScreen
import com.indusjs.fleet.presentation.user.changepassword.ChangePasswordScreen
import com.indusjs.fleet.presentation.user.signup.SignUpScreen
import com.indusjs.fleet.presentation.user.forgotpassword.ForgotPasswordScreen
import com.indusjs.fleet.presentation.team.list.TeamListScreen
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberScreen
import com.indusjs.fleet.theme.AppTheme
import com.indusjs.fleet.domain.entity.vehicle.DocumentType

/**
 * File picker request data.
 */
data class FilePickerRequest(
    val documentType: DocumentType,
    val callback: (fileName: String, fileBytes: ByteArray, mimeType: String) -> Unit
)

/**
 * Navigation routes for the app
 */
sealed class AppRoute {
    data object Login : AppRoute()
    data object SignUp : AppRoute()
    data object ForgotPassword : AppRoute()
    data object Dashboard : AppRoute()
    data object Profile : AppRoute()
    data object ChangePassword : AppRoute()
    data object Vehicles : AppRoute()
    data class VehicleDetail(val vehicleId: String) : AppRoute()
    data object AddVehicle : AppRoute()
    data object Drivers : AppRoute()
    data class DriverDetail(val driverId: String) : AppRoute()
    data object CreateDriver : AppRoute()
    data object Trips : AppRoute()
    data class TripDetail(val tripId: String) : AppRoute()
    data object CreateTrip : AppRoute()
    data object Maps : AppRoute()
    data object TeamList : AppRoute()
    data object CreateTeamMember : AppRoute()
}

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    onPickFile: ((FilePickerRequest) -> Unit)? = null,
    onOpenDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onDownloadDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onSaveDocument: ((documentName: String, fileBytes: ByteArray, mimeType: String) -> Unit)? = null
) = AppTheme(onThemeChanged) {
    // Simple state-based navigation
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Login) }
    var isLoggedIn by remember { mutableStateOf(false) }

    // Snackbar for showing session expired message
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for authentication events (401 Unauthorized)
    // This will redirect to login screen when session expires
    // Note: Session is automatically cleared by AuthenticationManager before emitting the event
    LaunchedEffect(Unit) {
        AuthenticationManager.authEvents.collect { event ->
            when (event) {
                is AuthenticationEvent.Unauthorized,
                is AuthenticationEvent.SessionExpired -> {
                    // Session is already cleared by AuthenticationManager
                    isLoggedIn = false
                    currentRoute = AppRoute.Login

                    // Show message to user
                    val message = when (event) {
                        is AuthenticationEvent.SessionExpired -> event.message
                        else -> "Your session has expired. Please log in again."
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is AuthenticationEvent.LoggedOut -> {
                    isLoggedIn = false
                    currentRoute = AppRoute.Login
                }
            }
        }
    }


    // Create ViewModelProvider for dependency injection
    // In production with Metro DI, this would use the generated graphs
    val viewModelProvider = remember { DefaultViewModelProvider() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                ProvideViewModels(viewModelProvider) {
                    when (val route = currentRoute) {
                        is AppRoute.Login -> {
                            val loginViewModel = rememberViewModel { loginViewModel() }
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    isLoggedIn = true
                                    currentRoute = AppRoute.Dashboard
                                },
                                onNavigateToSignUp = { currentRoute = AppRoute.SignUp },
                                onNavigateToForgotPassword = { currentRoute = AppRoute.ForgotPassword }
                            )
                        }

                is AppRoute.SignUp -> {
                    val signUpViewModel = rememberViewModel { signUpViewModel() }
                    SignUpScreen(
                        viewModel = signUpViewModel,
                        onSignUpSuccess = {
                            isLoggedIn = true
                            currentRoute = AppRoute.Dashboard
                        },
                        onNavigateToLogin = { currentRoute = AppRoute.Login }
                    )
                }

                is AppRoute.ForgotPassword -> {
                    val forgotPasswordViewModel = rememberViewModel { forgotPasswordViewModel() }
                    ForgotPasswordScreen(
                        viewModel = forgotPasswordViewModel,
                        onNavigateToLogin = { currentRoute = AppRoute.Login }
                    )
                }

                is AppRoute.Dashboard -> {
                    val dashboardViewModel = rememberViewModel { dashboardViewModel() }
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToVehicles = { currentRoute = AppRoute.Vehicles },
                        onNavigateToDrivers = { currentRoute = AppRoute.Drivers },
                        onNavigateToTrips = { currentRoute = AppRoute.Trips },
                        onNavigateToMaps = { currentRoute = AppRoute.Maps },
                        onNavigateToProfile = { currentRoute = AppRoute.Profile },
                        onNavigateToTeam = { currentRoute = AppRoute.TeamList }
                    )
                }

                is AppRoute.Profile -> {
                    val profileViewModel = rememberViewModel { profileViewModel() }
                    ProfileScreen(
                        viewModel = profileViewModel,
                        onNavigateToChangePassword = { currentRoute = AppRoute.ChangePassword },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard },
                        onLogout = {
                            isLoggedIn = false
                            currentRoute = AppRoute.Login
                        }
                    )
                }

                is AppRoute.ChangePassword -> {
                    val changePasswordViewModel = rememberViewModel { changePasswordViewModel() }
                    ChangePasswordScreen(
                        viewModel = changePasswordViewModel,
                        onNavigateBack = { currentRoute = AppRoute.Profile }
                    )
                }

                is AppRoute.Vehicles -> {
                    val vehiclesViewModel = rememberViewModel { vehiclesViewModel() }
                    VehiclesScreen(
                        viewModel = vehiclesViewModel,
                        onNavigateToDetail = { vehicleId ->
                            currentRoute = AppRoute.VehicleDetail(vehicleId)
                        },
                        onNavigateToAdd = {
                            currentRoute = AppRoute.AddVehicle
                        },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard }
                    )
                }

                is AppRoute.AddVehicle -> {
                    val addVehicleViewModel = rememberViewModel { addVehicleViewModel() }
                    AddVehicleScreen(
                        viewModel = addVehicleViewModel,
                        onNavigateBack = { currentRoute = AppRoute.Vehicles },
                        onVehicleRegistered = { vehicleId ->
                            currentRoute = AppRoute.VehicleDetail(vehicleId)
                        },
                        onRequestFilePicker = { documentType, callback ->
                            val request = FilePickerRequest(documentType, callback)
                            if (onPickFile != null) {
                                onPickFile(request)
                            } else {
                                // Fallback: Show a message that file picking is not available
                                // This would be replaced by platform-specific implementation
                            }
                        }
                    )
                }

                is AppRoute.VehicleDetail -> {
                    val vehicleDetailViewModel = rememberViewModel { vehicleDetailViewModel() }
                    VehicleDetailScreen(
                        viewModel = vehicleDetailViewModel,
                        vehicleId = route.vehicleId,
                        onNavigateBack = { currentRoute = AppRoute.Vehicles },
                        onRequestFilePicker = { documentType, callback ->
                            if (onPickFile != null) {
                                // Create a DocumentType from the string
                                val docType = try {
                                    DocumentType.valueOf(documentType.uppercase())
                                } catch (e: Exception) {
                                    DocumentType.OTHER
                                }
                                val request = FilePickerRequest(docType, callback)
                                onPickFile(request)
                            }
                        },
                        onOpenDocumentPreview = onOpenDocument,
                        onDownloadDocument = onDownloadDocument,
                        onSaveDocument = onSaveDocument
                    )
                }

                is AppRoute.Drivers -> {
                    val driversViewModel = rememberViewModel { driversViewModel() }
                    DriversScreen(
                        viewModel = driversViewModel,
                        onNavigateToDetail = { driverId ->
                            currentRoute = AppRoute.DriverDetail(driverId)
                        },
                        onNavigateToAdd = {
                            currentRoute = AppRoute.CreateDriver
                        },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard }
                    )
                }

                is AppRoute.CreateDriver -> {
                    val createDriverViewModel = rememberViewModel { createDriverViewModel() }
                    CreateDriverScreen(
                        viewModel = createDriverViewModel,
                        onNavigateBack = { currentRoute = AppRoute.Drivers },
                        onDriverCreated = { driverId ->
                            currentRoute = AppRoute.DriverDetail(driverId)
                        }
                    )
                }

                is AppRoute.DriverDetail -> {
                    val driverDetailViewModel = rememberViewModel { driverDetailViewModel() }
                    DriverDetailScreen(
                        viewModel = driverDetailViewModel,
                        driverId = route.driverId,
                        onNavigateBack = { currentRoute = AppRoute.Drivers }
                    )
                }

                is AppRoute.Trips -> {
                    val tripsViewModel = rememberViewModel { tripsViewModel() }
                    TripsScreen(
                        viewModel = tripsViewModel,
                        onNavigateToDetail = { tripId ->
                            currentRoute = AppRoute.TripDetail(tripId)
                        },
                        onNavigateToCreate = {
                            currentRoute = AppRoute.CreateTrip
                        },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard }
                    )
                }

                is AppRoute.CreateTrip -> {
                    val createTripViewModel = rememberViewModel { createTripViewModel() }
                    CreateTripScreen(
                        viewModel = createTripViewModel,
                        onNavigateBack = { currentRoute = AppRoute.Trips },
                        onTripCreated = { tripId ->
                            currentRoute = AppRoute.TripDetail(tripId)
                        }
                    )
                }

                is AppRoute.TripDetail -> {
                    val tripDetailViewModel = rememberViewModel { tripDetailViewModel() }
                    TripDetailScreen(
                        viewModel = tripDetailViewModel,
                        tripId = route.tripId,
                        onNavigateBack = { currentRoute = AppRoute.Trips }
                    )
                }

                is AppRoute.Maps -> {
                    val mapsViewModel = rememberViewModel { mapsViewModel() }
                    MapsScreen(
                        viewModel = mapsViewModel,
                        onNavigateToVehicleDetail = { vehicleId ->
                            currentRoute = AppRoute.VehicleDetail(vehicleId)
                        },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard }
                    )
                }

                is AppRoute.TeamList -> {
                    val teamListViewModel = rememberViewModel { teamListViewModel() }
                    TeamListScreen(
                        viewModel = teamListViewModel,
                        onNavigateBack = { currentRoute = AppRoute.Dashboard },
                        onNavigateToCreateMember = { currentRoute = AppRoute.CreateTeamMember },
                        onNavigateToMemberDetail = { memberId ->
                            // TODO: Implement member detail navigation
                        }
                    )
                }

                is AppRoute.CreateTeamMember -> {
                    val createTeamMemberViewModel = rememberViewModel { createTeamMemberViewModel() }
                    CreateTeamMemberScreen(
                        viewModel = createTeamMemberViewModel,
                        onNavigateBack = { currentRoute = AppRoute.TeamList }
                    )
                }
            }
        }
            }
        }
    }
}

