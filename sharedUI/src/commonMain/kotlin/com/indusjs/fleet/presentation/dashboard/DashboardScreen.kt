package com.indusjs.fleet.presentation.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.ErrorHandler
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertPriority
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
import com.indusjs.fleet.domain.entity.dashboard.DocumentStats
import com.indusjs.fleet.domain.entity.dashboard.DriverStatusSummary
import com.indusjs.fleet.domain.entity.dashboard.LiveStatus
import com.indusjs.fleet.domain.entity.dashboard.OngoingTrip
import com.indusjs.fleet.domain.entity.dashboard.PendingPayment
import com.indusjs.fleet.domain.entity.dashboard.TeamStats
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.fleet.theme.isAppInDarkTheme
import com.indusjs.fleet.theme.rememberThemeToggle
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Dashboard Screen composable - Main overview screen for Fleet Management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToDrivers: () -> Unit = {},
    onNavigateToTrips: () -> Unit = {},
    onNavigateToMaps: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToAddTripCost: () -> Unit = {},
    onNavigateToAddVehicleCost: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Handle side effects
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
                is DashboardContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp)
            ) {
                NavigationDrawerContent(
                    userName = state.userName.ifEmpty { "User" },
                    userRole = state.userRole,
                    onNavigateToVehicles = {
                        scope.launch { drawerState.close() }
                        onNavigateToVehicles()
                    },
                    onNavigateToDrivers = {
                        scope.launch { drawerState.close() }
                        onNavigateToDrivers()
                    },
                    onNavigateToTrips = {
                        scope.launch { drawerState.close() }
                        onNavigateToTrips()
                    },
                    onNavigateToMaps = {
                        scope.launch { drawerState.close() }
                        onNavigateToMaps()
                    },
                    onNavigateToTeam = {
                        scope.launch { drawerState.close() }
                        onNavigateToTeam()
                    },
                    onNavigateToProfile = {
                        scope.launch { drawerState.close() }
                        onNavigateToProfile()
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (state.userName.isNotEmpty()) "Hi, ${state.userName}" else "Dashboard",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (state.userRole.isNotEmpty()) {
                                    Text(
                                        text = state.userRole,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (state.lastUpdated != null) {
                                    Text(
                                        text = "• Updated: ${state.lastUpdated}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_menu),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        // Notification bell with badge
                        BadgedBox(
                            badge = {
                                if (state.notificationCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text(
                                            text = if (state.notificationCount > 99) "99+" else state.notificationCount.toString(),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToNotifications) }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_notifications),
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Refresh button
                        IconButton(onClick = { viewModel.sendIntent(DashboardContract.Intent.RefreshDashboard) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    // Show loading only on initial load (no cached data yet)
                    state.isLoading && !state.hasCachedData -> {
                        LoadingContent(message = "Loading dashboard...")
                    }
                    // Show error screen ONLY if we have no cached data AND have an error AND not offline
                    state.error != null && !state.hasCachedData && !state.isOffline -> {
                        ErrorContent(
                            error = state.error!!,
                            screenContext = ErrorHandler.ScreenContext.DASHBOARD,
                            onRetry = { viewModel.sendIntent(DashboardContract.Intent.LoadDashboard) }
                        )
                    }
                    // Show content (fresh or cached) - this includes offline mode with cached data
                    else -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Offline banner when showing cached data due to network error
                            if (state.isOffline && state.hasCachedData) {
                                OfflineBanner(
                                    message = state.error ?: "You're offline",
                                    lastUpdated = state.lastUpdated,
                                    onRetry = { viewModel.sendIntent(DashboardContract.Intent.RetryConnection) },
                                    onDismiss = { viewModel.sendIntent(DashboardContract.Intent.DismissOfflineBanner) }
                                )
                            }

                            // Show refreshing indicator at top
                            if (state.isRefreshing) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            DashboardContent(
                                stats = state.stats,
                                costOverview = state.costOverview,
                                selectedCostFilter = state.selectedCostFilter,
                                isLoadingCostOverview = state.isLoadingCostOverview,
                                pendingPayments = state.pendingPayments,
                                totalPendingAmount = state.totalPendingAmount,
                                isLoadingPendingPayments = state.isLoadingPendingPayments,
                                vehicleStatus = state.vehicleStatus,
                                driverStatus = state.driverStatus,
                                tripSummary = state.tripSummary,
                                onVehiclesClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToVehicles) },
                                onDriversClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToDrivers) },
                                onTripsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToTrips) },
                                onMapsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToMaps) },
                                onAlertDismiss = { alertId ->
                                    viewModel.sendIntent(DashboardContract.Intent.DismissAlert(alertId))
                                },
                                onCostFilterChange = { filter ->
                                    viewModel.sendIntent(DashboardContract.Intent.ChangeCostFilter(filter))
                                },
                                onAddTripCostClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddTripCost) },
                                onAddVehicleCostClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddVehicleCost) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Navigation Drawer Content with Quick Actions menu items.
 */
@Composable
private fun NavigationDrawerContent(
    userName: String,
    userRole: String,
    onNavigateToVehicles: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight()
    ) {
        // Header with user info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            // User Info - clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToProfile)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
                ) {
                    // User Avatar
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (userRole.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userRole,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Menu Items
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_vehicle),
                    contentDescription = "Vehicles",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Vehicles") },
            selected = false,
            onClick = onNavigateToVehicles,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_driver),
                    contentDescription = "Drivers",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Drivers") },
            selected = false,
            onClick = onNavigateToDrivers,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_trip),
                    contentDescription = "Trips",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Trips") },
            selected = false,
            onClick = onNavigateToTrips,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_map),
                    contentDescription = "Live Map",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Live Map") },
            selected = false,
            onClick = onNavigateToMaps,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_team),
                    contentDescription = "Team Members",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Team Members") },
            selected = false,
            onClick = onNavigateToTeam,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = "Profile & Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Profile & Settings") },
            selected = false,
            onClick = onNavigateToProfile,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        // Theme Toggle
        val isDarkTheme = isAppInDarkTheme()
        val toggleTheme = rememberThemeToggle()
        NavigationDrawerItem(
            icon = {
                Icon(
                    painter = painterResource(
                        if (isDarkTheme) Res.drawable.ic_sun else Res.drawable.ic_moon
                    ),
                    contentDescription = if (isDarkTheme) "Light Mode" else "Dark Mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(if (isDarkTheme) "Light Mode" else "Dark Mode") },
            selected = false,
            onClick = toggleTheme,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Text(
            text = "IndusJS Fleet v1.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    costOverview: CostOverview,
    selectedCostFilter: CostOverviewFilter,
    isLoadingCostOverview: Boolean,
    pendingPayments: List<PendingPayment>,
    totalPendingAmount: Double,
    isLoadingPendingPayments: Boolean,
    vehicleStatus: VehicleStatusSummary,
    driverStatus: DriverStatusSummary,
    tripSummary: TripSummary,
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit,
    onMapsClick: () -> Unit,
    onAlertDismiss: (String) -> Unit,
    onCostFilterChange: (CostOverviewFilter) -> Unit,
    onAddTripCostClick: () -> Unit,
    onAddVehicleCostClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Cost Overview Section with Filter
        item {
            CostOverviewSection(
                costOverview = costOverview,
                selectedFilter = selectedCostFilter,
                isLoading = isLoadingCostOverview,
                onFilterChange = onCostFilterChange,
                onAddTripCostClick = onAddTripCostClick,
                onAddVehicleCostClick = onAddVehicleCostClick
            )
        }

        // 2. Pending Payments Section
        item {
            PendingPaymentsSection(
                payments = pendingPayments,
                totalPending = totalPendingAmount,
                isLoading = isLoadingPendingPayments
            )
        }

        // 3. Vehicle Status Section
        item {
            VehicleStatusSection(
                vehicleStatus = vehicleStatus,
                onClick = onVehiclesClick
            )
        }

        // 4. Trips Section
        item {
            TripsStatusSection(
                tripSummary = tripSummary,
                ongoingTrips = stats.liveStatus.ongoingTrips,
                onClick = onTripsClick
            )
        }

        // 5. Alerts Section
        if (stats.alerts.isNotEmpty() || (stats.documentStats?.expiringDocuments ?: 0) > 0) {
            item {
                AlertsSection(
                    alerts = stats.alerts,
                    documentStats = stats.documentStats,
                    onAlertDismiss = onAlertDismiss
                )
            }
        }

        // 6. Drivers Section
        item {
            DriversStatusSection(
                driverStatus = driverStatus,
                onClick = onDriversClick
            )
        }

        // Quick Actions Section
        item {
            QuickActionsSection(
                onVehiclesClick = onVehiclesClick,
                onDriversClick = onDriversClick,
                onTripsClick = onTripsClick,
                onMapsClick = onMapsClick
            )
        }
    }
}

/**
 * Welcome Banner Card
 */
@Composable
private fun WelcomeBannerCard(
    activeVehicles: Int,
    ongoingTrips: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Fleet Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All Systems Operational",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$activeVehicles Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$ongoingTrips Trips",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Fleet Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_fleet_logo),
                    contentDescription = "Fleet",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Quick Actions Section Content (used inside a Card)
 */
@Composable
private fun QuickActionsSectionContent(
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit,
    onMapsClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(14.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = "🚛",
                label = "Vehicles",
                onClick = onVehiclesClick
            )
            QuickActionButton(
                icon = "👨‍✈️",
                label = "Drivers",
                onClick = onDriversClick
            )
            QuickActionButton(
                icon = "🗺️",
                label = "Trips",
                onClick = onTripsClick
            )
            QuickActionButton(
                icon = "📍",
                label = "Live Map",
                onClick = onMapsClick
            )
        }
    }
}

/**
 * Quick Action Button Component
 */
@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun AlertCard(
    alert: Alert,
    onDismiss: () -> Unit
) {
    val alertColor = when (alert.type) {
        AlertType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
        AlertType.FUEL_LOW -> MaterialTheme.colorScheme.error
        AlertType.SPEED_VIOLATION -> MaterialTheme.colorScheme.error
        AlertType.GEOFENCE_VIOLATION -> MaterialTheme.colorScheme.secondary
        AlertType.DRIVER_BEHAVIOR -> MaterialTheme.colorScheme.secondary
        AlertType.SYSTEM -> MaterialTheme.colorScheme.primary
        AlertType.DOCUMENT_EXPIRY -> MaterialTheme.colorScheme.error
        AlertType.LICENSE_EXPIRY -> MaterialTheme.colorScheme.error
    }

    val alertIcon = when (alert.type) {
        AlertType.DOCUMENT_EXPIRY -> "📄"
        AlertType.LICENSE_EXPIRY -> "📋"
        else -> "⚠️"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = alertColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alert icon in colored box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = alertColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = alertIcon,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Compact Alert Card for inline display within a section Card
 */
@Composable
private fun AlertCardCompact(
    alert: Alert,
    onDismiss: () -> Unit
) {
    val alertColor = when (alert.type) {
        AlertType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
        AlertType.FUEL_LOW -> MaterialTheme.colorScheme.error
        AlertType.SPEED_VIOLATION -> MaterialTheme.colorScheme.error
        AlertType.GEOFENCE_VIOLATION -> MaterialTheme.colorScheme.secondary
        AlertType.DRIVER_BEHAVIOR -> MaterialTheme.colorScheme.secondary
        AlertType.SYSTEM -> MaterialTheme.colorScheme.primary
        AlertType.DOCUMENT_EXPIRY -> MaterialTheme.colorScheme.error
        AlertType.LICENSE_EXPIRY -> MaterialTheme.colorScheme.error
    }

    val alertIcon = when (alert.type) {
        AlertType.DOCUMENT_EXPIRY -> "📄"
        AlertType.LICENSE_EXPIRY -> "📋"
        else -> "⚠️"
    }

    // Priority-based border color
    val priorityColor = when (alert.priority) {
        AlertPriority.CRITICAL -> MaterialTheme.colorScheme.error
        AlertPriority.WARNING -> MaterialTheme.colorScheme.tertiary
        AlertPriority.INFO -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = alertColor.copy(alpha = 0.06f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = alertColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = alertIcon,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = alertColor
            )
            Text(
                text = alert.message,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Summary Content for inline display within a section Card
 */
@Composable
private fun SummaryContent(
    completedTrips: Int,
    totalDistance: Double,
    fuelConsumption: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SummaryItem(
            label = "Completed",
            value = completedTrips.toString(),
            emoji = "✅"
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        SummaryItem(
            label = "Distance",
            value = "${totalDistance.toInt()} km",
            emoji = "🛣️"
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
        SummaryItem(
            label = "Fuel",
            value = "${fuelConsumption.toInt()} L",
            emoji = "⛽"
        )
    }
}

@Composable
private fun SummaryCard(
    completedTrips: Int,
    totalDistance: Double,
    fuelConsumption: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Completed Trips",
                    value = completedTrips.toString(),
                    emoji = "✅"
                )
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                SummaryItem(
                    label = "Total Distance",
                    value = "${totalDistance.toInt()} km",
                    emoji = "🛣️"
                )
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                SummaryItem(
                    label = "Fuel Used",
                    value = "${fuelConsumption.toInt()} L",
                    emoji = "⛽"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    emoji: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        if (emoji.isNotEmpty()) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Live Status Section - Shows ongoing trips and live tracking info
 */
@Composable
private fun LiveStatusSection(
    liveStatus: LiveStatus,
    onMapsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔴",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = onMapsClick) {
                    Text("View Map")
                }
            }

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${liveStatus.liveTrackingVehicles}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Live Vehicles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${liveStatus.ongoingTripsCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Ongoing Trips",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${liveStatus.driversOnTrip}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Drivers On Trip",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Ongoing trips list (show first 3)
            if (liveStatus.ongoingTrips.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Active Trips",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                liveStatus.ongoingTrips.take(3).forEach { trip ->
                    OngoingTripItem(trip = trip)
                }
            }
        }
    }
}

/**
 * Individual ongoing trip item
 */
@Composable
private fun OngoingTripItem(trip: OngoingTrip) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vehicle icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚛",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trip.vehicleRegistration,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${trip.startLocation} → ${trip.endLocation}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = trip.status.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = trip.driverName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Team Stats Section (Owner only)
 */
@Composable
private fun TeamStatsSection(teamStats: TeamStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "👥",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Team Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TeamStatItem(
                    label = "Managers",
                    value = teamStats.totalManagers,
                    emoji = "👔"
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                TeamStatItem(
                    label = "Supervisors",
                    value = teamStats.totalSupervisors,
                    emoji = "👷"
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                TeamStatItem(
                    label = "Total",
                    value = teamStats.totalMembers,
                    emoji = "👥"
                )
            }
        }
    }
}

@Composable
private fun TeamStatItem(
    label: String,
    value: Int,
    emoji: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Document Stats Section (Owner/Manager)
 */
@Composable
private fun DocumentStatsSection(documentStats: DocumentStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📁",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Document Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DocumentStatItem(
                    label = "Total",
                    value = documentStats.totalDocuments,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                DocumentStatItem(
                    label = "Expiring",
                    value = documentStats.expiringDocuments,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                DocumentStatItem(
                    label = "Expired",
                    value = documentStats.expiredDocuments,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DocumentStatItem(
    label: String,
    value: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Offline banner shown when displaying cached data due to network error.
 */
@Composable
private fun OfflineBanner(
    message: String,
    lastUpdated: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Offline icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📡",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Offline Mode",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (lastUpdated != null) "Last updated: $lastUpdated" else "Showing cached data",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(
                    text = "Retry",
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============ CLEAN DASHBOARD SECTIONS ============

/**
 * Cost Overview Section - Clean design with clear visual hierarchy.
 */
@Composable
private fun CostOverviewSection(
    costOverview: CostOverview,
    selectedFilter: CostOverviewFilter,
    isLoading: Boolean,
    onFilterChange: (CostOverviewFilter) -> Unit,
    onAddTripCostClick: () -> Unit,
    onAddVehicleCostClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with filter tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cost Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Filter chips - clean pill style
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    CostOverviewFilter.entries.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { onFilterChange(filter) },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ) {
                            Text(
                                text = filter.label.take(1),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else {
                // Stats row - clean number display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Total Expenses
                    CleanStatItem(
                        label = "Expenses",
                        value = "₹${formatAmount(costOverview.totalExpenses)}",
                        valueColor = MaterialTheme.colorScheme.error
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Profit/Loss
                    CleanStatItem(
                        label = if (costOverview.isProfit) "Profit" else "Loss",
                        value = "₹${formatAmount(kotlin.math.abs(costOverview.profitLoss))}",
                        valueColor = if (costOverview.isProfit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    // Completed Trips
                    CleanStatItem(
                        label = "Trips",
                        value = costOverview.completedTrips.toString(),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Add Cost Buttons - consistent eye-catching design
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Trip Cost Button
                    FilledTonalButton(
                        onClick = onAddTripCostClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Trip Cost",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Vehicle Cost Button
                    FilledTonalButton(
                        onClick = onAddVehicleCostClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_vehicle),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Vehicle Cost",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean stat item - just number and label, no background
 */
@Composable
private fun CleanStatItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Enhanced Cost Stat Card with gradient background
 */
@Composable
private fun EnhancedCostStatCard(
    title: String,
    value: String,
    icon: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Pending Payments Section - Clean design.
 */
@Composable
private fun PendingPaymentsSection(
    payments: List<PendingPayment>,
    totalPending: Double,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💳 Pending Payments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (totalPending > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = "₹${formatAmount(totalPending)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else if (payments.isEmpty()) {
                Text(
                    text = "✅ No pending payments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            } else {
                // Show first 3 payments
                payments.take(3).forEach { payment ->
                    PendingPaymentItem(payment = payment)
                }

                if (payments.size > 3) {
                    TextButton(onClick = { /* Navigate to all payments */ }) {
                        Text("View all ${payments.size} pending payments →")
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingPaymentItem(payment: PendingPayment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = payment.customerName.ifEmpty { "Customer" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${payment.vehicleRegistration} • ${payment.startLocation} → ${payment.endLocation}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${formatAmount(payment.pendingAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                if (payment.daysOverdue > 0) {
                    Text(
                        text = "${payment.daysOverdue} days overdue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Vehicle Status Section.
 */
@Composable
private fun VehicleStatusSection(
    vehicleStatus: VehicleStatusSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚛 Vehicle Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total: ${vehicleStatus.total}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    label = "Planned",
                    count = vehicleStatus.onTripPlanned,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "On Route",
                    count = vehicleStatus.onTripInProgress,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "Maintenance",
                    count = vehicleStatus.underMaintenance,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Trips Status Section.
 */
@Composable
private fun TripsStatusSection(
    tripSummary: TripSummary,
    ongoingTrips: List<OngoingTrip>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗺️ Trips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total: ${tripSummary.total}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    label = "In Progress",
                    count = tripSummary.inProgress,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "Planned",
                    count = tripSummary.planned,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "Delayed",
                    count = tripSummary.delayed,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            // Show ongoing trips with fuel info
            if (ongoingTrips.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    text = "Active Trips",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ongoingTrips.take(2).forEach { trip ->
                    OngoingTripItem(trip = trip)
                }
            }
        }
    }
}

/**
 * Alerts Section - Clean design without colored background.
 */
@Composable
private fun AlertsSection(
    alerts: List<Alert>,
    documentStats: DocumentStats?,
    onAlertDismiss: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Alert count badge
                Text(
                    text = "${alerts.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Document expiry stats - clean text style
            if (documentStats != null && (documentStats.expiringDocuments > 0 || documentStats.expiredDocuments > 0)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (documentStats.expiredDocuments > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📄",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${documentStats.expiredDocuments} Expired",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (documentStats.expiringDocuments > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⏰",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${documentStats.expiringDocuments} Expiring Soon",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            if (alerts.isNotEmpty()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }

            // Alert items - clean list style
            alerts.take(3).forEach { alert ->
                CleanAlertItem(
                    alert = alert,
                    onDismiss = { onAlertDismiss(alert.id) }
                )
            }
        }
    }
}

/**
 * Clean Alert Item - minimal design without colored backgrounds
 */
@Composable
private fun CleanAlertItem(
    alert: Alert,
    onDismiss: () -> Unit
) {
    val alertColor = when (alert.type) {
        AlertType.MAINTENANCE -> MaterialTheme.colorScheme.tertiary
        AlertType.FUEL_LOW -> MaterialTheme.colorScheme.error
        AlertType.SPEED_VIOLATION -> MaterialTheme.colorScheme.error
        AlertType.GEOFENCE_VIOLATION -> MaterialTheme.colorScheme.secondary
        AlertType.DRIVER_BEHAVIOR -> MaterialTheme.colorScheme.secondary
        AlertType.SYSTEM -> MaterialTheme.colorScheme.primary
        AlertType.DOCUMENT_EXPIRY -> MaterialTheme.colorScheme.error
        AlertType.LICENSE_EXPIRY -> MaterialTheme.colorScheme.error
    }

    val alertIcon = when (alert.type) {
        AlertType.DOCUMENT_EXPIRY -> "📄"
        AlertType.LICENSE_EXPIRY -> "📋"
        AlertType.MAINTENANCE -> "🔧"
        AlertType.FUEL_LOW -> "⛽"
        else -> "⚠️"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = alertIcon,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = alertColor
            )
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "Dismiss",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Drivers Status Section.
 */
@Composable
private fun DriversStatusSection(
    driverStatus: DriverStatusSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👨‍✈️ Drivers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total: ${driverStatus.total}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusChip(
                    label = "On Route",
                    count = driverStatus.onTripInProgress,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "Planned",
                    count = driverStatus.onTripPlanned,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "Available",
                    count = driverStatus.available,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = "Leave",
                    count = driverStatus.onLeave,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

/**
 * Quick Actions Section - Clean design.
 */
@Composable
private fun QuickActionsSection(
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit,
    onMapsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⚡",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CleanQuickActionButton(
                    icon = "🚛",
                    label = "Vehicles",
                    onClick = onVehiclesClick
                )
                CleanQuickActionButton(
                    icon = "👨‍✈️",
                    label = "Drivers",
                    onClick = onDriversClick
                )
                CleanQuickActionButton(
                    icon = "🗺️",
                    label = "Trips",
                    onClick = onTripsClick
                )
                CleanQuickActionButton(
                    icon = "📍",
                    label = "Live Map",
                    onClick = onMapsClick
                )
            }
        }
    }
}

/**
 * Clean Quick Action Button - no colored background
 */
@Composable
private fun CleanQuickActionButton(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/**
 * Format amount with commas for Indian numbering system.
 */
private fun formatAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> {
            val value = amount / 10000000
            "${((value * 100).toLong() / 100.0)} Cr"
        }
        amount >= 100000 -> {
            val value = amount / 100000
            "${((value * 100).toLong() / 100.0)} L"
        }
        amount >= 1000 -> {
            val value = amount / 1000
            "${((value * 10).toLong() / 10.0)} K"
        }
        else -> amount.toLong().toString()
    }
}

