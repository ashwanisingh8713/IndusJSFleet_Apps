package com.ijs.dashboard.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ijs.dashboard.presentation.components.AlertsSection
import com.ijs.dashboard.presentation.components.CostOverviewSection
import com.ijs.dashboard.presentation.components.DriversStatusSection
import com.ijs.dashboard.presentation.components.FleetOverviewHeroCard
import com.ijs.dashboard.presentation.components.NavigationDrawerContent
import com.ijs.dashboard.presentation.components.OfflineBanner
import com.ijs.dashboard.presentation.components.TripsStatusSection
import com.ijs.dashboard.presentation.components.VehicleStatusSection
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.util.formatLastUpdated
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Returns a greeting string resource key based on time of day.
 * Call inside a @Composable to resolve with stringResource().
 */
private fun getTimeBasedGreetingIndex(): Int {
    return try {
        val currentTimeMs = com.indusjs.fleet.core.util.currentTimeMillis()
        val hourOfDay = ((currentTimeMs / 3600000) % 24).toInt()
        val localHour = (hourOfDay + 5) % 24
        when {
            localHour < 12 -> 0  // Morning
            localHour < 17 -> 1  // Afternoon
            else -> 2  // Evening
        }
    } catch (e: Exception) {
        0
    }
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToDrivers: () -> Unit = {},
    onNavigateToTrips: () -> Unit = {},
    onNavigateToMaps: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAddTripCost: () -> Unit = {},
    onNavigateToAddVehicleCost: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAddVehicle: () -> Unit = {},
    onNavigateToAddDriver: () -> Unit = {},
    onNavigateToCreateTrip: () -> Unit = {},
    onNavigateToAddDriverCost: () -> Unit = {},
    onNavigateToAlertsList: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToVehicleFinance: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DashboardContract.Effect.NavigateToVehicles -> onNavigateToVehicles()
                is DashboardContract.Effect.NavigateToDrivers -> onNavigateToDrivers()
                is DashboardContract.Effect.NavigateToTrips -> onNavigateToTrips()
                is DashboardContract.Effect.NavigateToMaps -> onNavigateToMaps()
                is DashboardContract.Effect.NavigateToAddTripCost -> onNavigateToAddTripCost()
                is DashboardContract.Effect.NavigateToAddVehicleCost -> onNavigateToAddVehicleCost()
                is DashboardContract.Effect.NavigateToNotifications -> onNavigateToNotifications()
                is DashboardContract.Effect.NavigateToAddVehicle -> onNavigateToAddVehicle()
                is DashboardContract.Effect.NavigateToAddDriver -> onNavigateToAddDriver()
                is DashboardContract.Effect.NavigateToCreateTrip -> onNavigateToCreateTrip()
                is DashboardContract.Effect.NavigateToAddDriverCost -> onNavigateToAddDriverCost()
                is DashboardContract.Effect.NavigateToAlertsList -> onNavigateToAlertsList()
                is DashboardContract.Effect.NavigateToCustomers -> onNavigateToCustomers()
                is DashboardContract.Effect.ShowSnackbar -> { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                NavigationDrawerContent(
                    userName = state.userName.ifEmpty { "User" },
                    userRole = state.userRole,
                    hasFinancialAccess = state.hasFinancialAccess,
                    onNavigateToVehicles = { scope.launch { drawerState.close() }; onNavigateToVehicles() },
                    onNavigateToDrivers = { scope.launch { drawerState.close() }; onNavigateToDrivers() },
                    onNavigateToTrips = { scope.launch { drawerState.close() }; onNavigateToTrips() },
                    onNavigateToMaps = { scope.launch { drawerState.close() }; onNavigateToMaps() },
                    onNavigateToTeam = { scope.launch { drawerState.close() }; onNavigateToTeam() },
                    onNavigateToReports = { scope.launch { drawerState.close() }; onNavigateToReports() },
                    onNavigateToProfile = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                    onNavigateToCustomers = { scope.launch { drawerState.close() }; onNavigateToCustomers() },
                    onNavigateToPayments = { scope.launch { drawerState.close() }; onNavigateToPayments() },
                    onNavigateToVehicleFinance = { scope.launch { drawerState.close() }; onNavigateToVehicleFinance() }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                DashboardTopBar(
                    userName = state.userName, lastUpdated = state.lastUpdated,
                    notificationCount = state.notificationCount, isRefreshing = state.isRefreshing,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNotificationsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToNotifications) },
                    onRefreshClick = { viewModel.sendIntent(DashboardContract.Intent.RefreshDashboard) }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when {
                    state.isLoading && !state.hasCachedData -> LoadingContent(message = stringResource(Res.string.loading))
                    state.error != null && !state.hasCachedData && !state.isOffline -> ErrorContent(
                        error = state.error!!, screenContext = FleetErrorContext.DASHBOARD,
                        onRetry = { viewModel.sendIntent(DashboardContract.Intent.LoadDashboard) }
                    )
                    else -> Column(modifier = Modifier.fillMaxSize()) {
                        if (state.isOffline && state.hasCachedData) {
                            OfflineBanner(message = state.error ?: "You're offline", lastUpdated = state.lastUpdated,
                                onRetry = { viewModel.sendIntent(DashboardContract.Intent.RetryConnection) },
                                onDismiss = { viewModel.sendIntent(DashboardContract.Intent.DismissOfflineBanner) })
                        }
                        if (state.isRefreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
                        DashboardContent(
                            stats = state.stats, costOverview = state.costOverview,
                            selectedCostFilter = state.selectedCostFilter, isLoadingCostOverview = state.isLoadingCostOverview,
                            vehicleStatus = state.vehicleStatus, driverStatus = state.driverStatus,
                            tripSummary = state.tripSummary, alertsSummary = state.alertsSummary,
                            hasFinancialAccess = state.hasFinancialAccess,
                            onVehiclesClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToVehicles) },
                            onDriversClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToDrivers) },
                            onTripsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToTrips) },
                            onMapsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToMaps) },
                            onAlertDismiss = { viewModel.sendIntent(DashboardContract.Intent.DismissAlert(it)) },
                            onCostFilterChange = { viewModel.sendIntent(DashboardContract.Intent.ChangeCostFilter(it)) },
                            onAddTripCostClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddTripCost) },
                            onAddVehicleCostClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddVehicleCost) },
                            onAddVehicleClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddVehicle) },
                            onAddDriverClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddDriver) },
                            onCreateTripClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToCreateTrip) },
                            onAddDriverCostClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddDriverCost) },
                            onAlertsListClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAlertsList) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(
    userName: String, lastUpdated: String?, notificationCount: Int, isRefreshing: Boolean,
    onMenuClick: () -> Unit, onNotificationsClick: () -> Unit, onRefreshClick: () -> Unit
) {
    val greetingIndex = remember { getTimeBasedGreetingIndex() }
    val greeting = when (greetingIndex) {
        0 -> stringResource(Res.string.dashboard_greeting_morning)
        1 -> stringResource(Res.string.dashboard_greeting_afternoon)
        else -> stringResource(Res.string.dashboard_greeting_evening)
    }
    TopAppBar(
        title = {
            Column(modifier = Modifier.semantics { heading() }) {
                Text(text = greeting, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (userName.isNotEmpty()) {
                    Text(text = userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { contentDescription = "$greeting $userName" })
                }
                if (lastUpdated != null) {
                    Text(text = "Updated: ${formatLastUpdated(lastUpdated)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick, modifier = Modifier.semantics { contentDescription = "Open navigation menu" }) {
                Icon(painter = painterResource(Res.drawable.ic_menu), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
        },
        actions = {
            BadgedBox(badge = {
                if (notificationCount > 0) Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text(text = if (notificationCount > 99) "99+" else notificationCount.toString(), style = MaterialTheme.typography.labelSmall)
                }
            }) {
                IconButton(onClick = onNotificationsClick, modifier = Modifier.semantics {
                    contentDescription = if (notificationCount > 0) "$notificationCount notifications" else "Notifications"
                }) {
                    Icon(painter = painterResource(Res.drawable.ic_notifications), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
            var isRefreshPressed by remember { mutableStateOf(false) }
            val rotationAngle by animateFloatAsState(targetValue = if (isRefreshing) 360f else 0f, animationSpec = tween(durationMillis = 1000), finishedListener = { isRefreshPressed = false })
            IconButton(onClick = { isRefreshPressed = true; onRefreshClick() }, modifier = Modifier.semantics { contentDescription = "Refresh dashboard" }) {
                Icon(painter = painterResource(Res.drawable.ic_refresh), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp).rotate(if (isRefreshing) rotationAngle else 0f))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, titleContentColor = MaterialTheme.colorScheme.onSurface)
    )
}

@Composable
private fun DashboardContent(
    stats: DashboardStats, costOverview: CostOverview, selectedCostFilter: CostOverviewFilter,
    isLoadingCostOverview: Boolean, vehicleStatus: VehicleStatusSummary, driverStatus: DriverStatusSummary,
    tripSummary: TripSummary, alertsSummary: AlertsSummary, hasFinancialAccess: Boolean,
    onVehiclesClick: () -> Unit, onDriversClick: () -> Unit, onTripsClick: () -> Unit, onMapsClick: () -> Unit,
    onAlertDismiss: (String) -> Unit, onCostFilterChange: (CostOverviewFilter) -> Unit,
    onAddTripCostClick: () -> Unit, onAddVehicleCostClick: () -> Unit,
    onAddVehicleClick: () -> Unit, onAddDriverClick: () -> Unit, onCreateTripClick: () -> Unit,
    onAddDriverCostClick: () -> Unit, onAlertsListClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { FleetOverviewHeroCard(vehicleStatus = vehicleStatus, driverStatus = driverStatus, tripSummary = tripSummary, onVehiclesClick = onVehiclesClick, onDriversClick = onDriversClick, onTripsClick = onTripsClick) }
        if (hasFinancialAccess) { item { CostOverviewSection(costOverview = costOverview, selectedFilter = selectedCostFilter, isLoading = isLoadingCostOverview, onFilterChange = onCostFilterChange, onAddTripCostClick = onAddTripCostClick, onAddVehicleCostClick = onAddVehicleCostClick, vehicleStatus = vehicleStatus, tripSummary = tripSummary, onAddVehicleClick = onAddVehicleClick, onCreateTripClick = onCreateTripClick) } }
        item { TripsStatusSection(tripSummary = tripSummary, ongoingTrips = stats.liveStatus.ongoingTrips, onClick = onTripsClick, onCreateTripClick = onCreateTripClick, onAddTripCostClick = onAddTripCostClick) }
        item { AlertsSection(alerts = stats.alerts, documentStats = stats.documentStats, alertsSummary = alertsSummary, vehicleStatus = vehicleStatus, onAlertDismiss = onAlertDismiss, onViewAllClick = onAlertsListClick) }
        item { VehicleStatusSection(vehicleStatus = vehicleStatus, onClick = onVehiclesClick, onAddVehicleClick = onAddVehicleClick, onAddMaintenanceCostClick = onAddVehicleCostClick) }
        item { DriversStatusSection(driverStatus = driverStatus, onClick = onDriversClick, onAddDriverClick = onAddDriverClick, onAddDriverCostClick = onAddDriverCostClick) }
    }
}
