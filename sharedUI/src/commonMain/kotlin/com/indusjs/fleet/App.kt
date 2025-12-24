package com.indusjs.fleet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.di.DefaultViewModelProvider
import com.indusjs.fleet.di.ProvideViewModels
import com.indusjs.fleet.di.rememberViewModel
import com.indusjs.fleet.presentation.auth.LoginScreen
import com.indusjs.fleet.presentation.dashboard.DashboardScreen
import com.indusjs.fleet.presentation.drivers.DriversScreen
import com.indusjs.fleet.presentation.maps.MapsScreen
import com.indusjs.fleet.presentation.trips.TripsScreen
import com.indusjs.fleet.presentation.vehicles.AddVehicleScreen
import com.indusjs.fleet.presentation.vehicles.VehiclesScreen
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
    data object Trips : AppRoute()
    data class TripDetail(val tripId: String) : AppRoute()
    data object Maps : AppRoute()
    data object TeamList : AppRoute()
    data object CreateTeamMember : AppRoute()
}

@Preview
@Composable
fun App(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    onPickFile: ((FilePickerRequest) -> Unit)? = null
) = AppTheme(onThemeChanged) {
    // Simple state-based navigation
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Login) }
    var isLoggedIn by remember { mutableStateOf(false) }


    // Create ViewModelProvider for dependency injection
    // In production with Metro DI, this would use the generated graphs
    val viewModelProvider = remember { DefaultViewModelProvider() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
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
                    // TODO: Implement Vehicle Detail screen
                    PlaceholderScreen(
                        title = "Vehicle Details",
                        subtitle = "Vehicle ID: ${route.vehicleId}",
                        onBack = { currentRoute = AppRoute.Vehicles }
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
                            // TODO: Navigate to Add Driver screen
                        },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard }
                    )
                }

                is AppRoute.DriverDetail -> {
                    // TODO: Implement Driver Detail screen
                    PlaceholderScreen(
                        title = "Driver Details",
                        subtitle = "Driver ID: ${route.driverId}",
                        onBack = { currentRoute = AppRoute.Drivers }
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
                            // TODO: Navigate to Create Trip screen
                        },
                        onNavigateBack = { currentRoute = AppRoute.Dashboard }
                    )
                }

                is AppRoute.TripDetail -> {
                    // TODO: Implement Trip Detail screen
                    PlaceholderScreen(
                        title = "Trip Details",
                        subtitle = "Trip ID: ${route.tripId}",
                        onBack = { currentRoute = AppRoute.Trips }
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

/**
 * Placeholder screen for features not yet implemented
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "🚧",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Coming Soon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
