package com.indusjs.fleet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.dispatcher.DefaultDispatcherProvider
import com.indusjs.fleet.feature.auth.presentation.LoginScreen
import com.indusjs.fleet.feature.auth.presentation.LoginViewModel
import com.indusjs.fleet.feature.dashboard.presentation.DashboardScreen
import com.indusjs.fleet.feature.dashboard.presentation.DashboardViewModel
import com.indusjs.fleet.feature.drivers.presentation.DriversScreen
import com.indusjs.fleet.feature.drivers.presentation.DriversViewModel
import com.indusjs.fleet.feature.maps.presentation.MapsScreen
import com.indusjs.fleet.feature.maps.presentation.MapsViewModel
import com.indusjs.fleet.feature.trips.presentation.TripsScreen
import com.indusjs.fleet.feature.trips.presentation.TripsViewModel
import com.indusjs.fleet.feature.vehicles.presentation.AddVehicleScreen
import com.indusjs.fleet.feature.vehicles.presentation.AddVehicleViewModel
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesScreen
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesViewModel
import com.indusjs.fleet.feature.user.presentation.profile.ProfileScreen
import com.indusjs.fleet.feature.user.presentation.profile.ProfileViewModel
import com.indusjs.fleet.feature.user.presentation.changepassword.ChangePasswordScreen
import com.indusjs.fleet.feature.user.presentation.changepassword.ChangePasswordViewModel
import com.indusjs.fleet.feature.user.presentation.signup.SignUpScreen
import com.indusjs.fleet.feature.user.presentation.signup.SignUpViewModel
import com.indusjs.fleet.feature.user.presentation.forgotpassword.ForgotPasswordScreen
import com.indusjs.fleet.feature.user.presentation.forgotpassword.ForgotPasswordViewModel
import com.indusjs.fleet.feature.team.presentation.list.TeamListScreen
import com.indusjs.fleet.feature.team.presentation.list.TeamListViewModel
import com.indusjs.fleet.feature.team.presentation.create.CreateTeamMemberScreen
import com.indusjs.fleet.feature.team.presentation.create.CreateTeamMemberViewModel
import com.indusjs.fleet.theme.AppTheme

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
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {
    // Simple state-based navigation
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Login) }
    var isLoggedIn by remember { mutableStateOf(false) }

    // Create ViewModels with DefaultDispatcherProvider
    // In production, these would be provided via DI (Metro)
    val dispatcherProvider = remember { DefaultDispatcherProvider() }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val route = currentRoute) {
            is AppRoute.Login -> {
                val loginViewModel = remember { LoginViewModel(dispatcherProvider) }
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
                val signUpViewModel = remember { SignUpViewModel(dispatcherProvider) }
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
                val forgotPasswordViewModel = remember { ForgotPasswordViewModel(dispatcherProvider) }
                ForgotPasswordScreen(
                    viewModel = forgotPasswordViewModel,
                    onNavigateToLogin = { currentRoute = AppRoute.Login }
                )
            }

            is AppRoute.Dashboard -> {
                val dashboardViewModel = remember { DashboardViewModel(dispatcherProvider) }
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
                val profileViewModel = remember { ProfileViewModel(dispatcherProvider) }
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
                val changePasswordViewModel = remember { ChangePasswordViewModel(dispatcherProvider) }
                ChangePasswordScreen(
                    viewModel = changePasswordViewModel,
                    onNavigateBack = { currentRoute = AppRoute.Profile }
                )
            }

            is AppRoute.Vehicles -> {
                val vehiclesViewModel = remember { VehiclesViewModel(dispatcherProvider) }
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
                val addVehicleViewModel = remember { AddVehicleViewModel(dispatcherProvider) }
                AddVehicleScreen(
                    viewModel = addVehicleViewModel,
                    onNavigateBack = { currentRoute = AppRoute.Vehicles },
                    onVehicleRegistered = { vehicleId ->
                        currentRoute = AppRoute.VehicleDetail(vehicleId)
                    },
                    onRequestFilePicker = { documentType, callback ->
                        // Platform-specific file picker would be implemented here
                        // For now, this is a placeholder that platforms can override
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
                val driversViewModel = remember { DriversViewModel(dispatcherProvider) }
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
                val tripsViewModel = remember { TripsViewModel(dispatcherProvider) }
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
                val mapsViewModel = remember { MapsViewModel(dispatcherProvider) }
                MapsScreen(
                    viewModel = mapsViewModel,
                    onNavigateToVehicleDetail = { vehicleId ->
                        currentRoute = AppRoute.VehicleDetail(vehicleId)
                    },
                    onNavigateBack = { currentRoute = AppRoute.Dashboard }
                )
            }

            is AppRoute.TeamList -> {
                val teamListViewModel = remember { TeamListViewModel(dispatcherProvider) }
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
                val createTeamMemberViewModel = remember { CreateTeamMemberViewModel(dispatcherProvider) }
                CreateTeamMemberScreen(
                    viewModel = createTeamMemberViewModel,
                    onNavigateBack = { currentRoute = AppRoute.TeamList }
                )
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
