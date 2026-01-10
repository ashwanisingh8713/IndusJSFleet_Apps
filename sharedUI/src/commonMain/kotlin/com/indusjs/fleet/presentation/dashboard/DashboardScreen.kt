package com.indusjs.fleet.presentation.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatLastUpdated
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertPriority
import com.indusjs.fleet.domain.entity.dashboard.AlertsSummary
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

// ============ Helper Functions ============

/**
 * Get time-based greeting message based on current hour
 * Uses platform-specific time utilities
 */
private fun getTimeBasedGreeting(): String {
    return try {
        // Get current hour using platform-specific time
        val currentTimeMs = com.indusjs.fleet.core.util.currentTimeMillis()
        // Convert to hours in day (UTC) - approximate calculation
        val hourOfDay = ((currentTimeMs / 3600000) % 24).toInt()
        // Adjust for typical timezone offset (IST = +5:30 ~ +5 hours)
        val localHour = (hourOfDay + 5) % 24

        when {
            localHour < 12 -> "Good Morning"
            localHour < 17 -> "Good Afternoon"
            else -> "Good Evening"
        }
    } catch (e: Exception) {
        "Hello" // Fallback greeting
    }
}

/**
 * Calculate fleet health percentage based on active vehicles
 */
private fun calculateFleetHealth(vehicleStatus: VehicleStatusSummary): Int {
    if (vehicleStatus.total == 0) return 100
    val activeCount = vehicleStatus.available + vehicleStatus.onTripPlanned + vehicleStatus.onTripInProgress
    return ((activeCount.toFloat() / vehicleStatus.total.toFloat()) * 100).toInt().coerceIn(0, 100)
}

/**
 * Get fleet health color based on percentage
 */
@Composable
private fun getFleetHealthColor(percentage: Int): Color {
    return when {
        percentage >= 80 -> Color(0xFF4CAF50) // Green
        percentage >= 60 -> Color(0xFFFFA726) // Orange
        else -> Color(0xFFEF5350) // Red
    }
}

/**
 * Get fleet health label
 */
private fun getFleetHealthLabel(percentage: Int): String {
    return when {
        percentage >= 80 -> "Excellent"
        percentage >= 60 -> "Good"
        percentage >= 40 -> "Fair"
        else -> "Needs Attention"
    }
}

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
    onNavigateToReports: () -> Unit = {},
    onNavigateToAddTripCost: () -> Unit = {},
    onNavigateToAddVehicleCost: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAddVehicle: () -> Unit = {},
    onNavigateToAddDriver: () -> Unit = {},
    onNavigateToCreateTrip: () -> Unit = {}
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
                is DashboardContract.Effect.NavigateToAddVehicle -> onNavigateToAddVehicle()
                is DashboardContract.Effect.NavigateToAddDriver -> onNavigateToAddDriver()
                is DashboardContract.Effect.NavigateToCreateTrip -> onNavigateToCreateTrip()
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
                    hasFinancialAccess = state.hasFinancialAccess,
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
                    onNavigateToReports = {
                        scope.launch { drawerState.close() }
                        onNavigateToReports()
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
                val greeting = remember { getTimeBasedGreeting() }

                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.semantics { heading() }
                        ) {
                            // Greeting on first line
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Username on second line (larger, bolder)
                            if (state.userName.isNotEmpty()) {
                                Text(
                                    text = state.userName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.semantics {
                                        contentDescription = "$greeting ${state.userName}"
                                    }
                                )
                            }
                            // Last updated info
                            if (state.lastUpdated != null) {
                                Text(
                                    text = "Updated: ${formatLastUpdated(state.lastUpdated)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.semantics {
                                contentDescription = "Open navigation menu"
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_menu),
                                contentDescription = null,
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
                            IconButton(
                                onClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToNotifications) },
                                modifier = Modifier.semantics {
                                    contentDescription = if (state.notificationCount > 0)
                                        "${state.notificationCount} notifications" else "Notifications"
                                }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_notifications),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Animated Refresh button
                        var isRefreshing by remember { mutableStateOf(false) }
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (state.isRefreshing) 360f else 0f,
                            animationSpec = tween(durationMillis = 1000),
                            finishedListener = { isRefreshing = false }
                        )

                        IconButton(
                            onClick = {
                                isRefreshing = true
                                viewModel.sendIntent(DashboardContract.Intent.RefreshDashboard)
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Refresh dashboard"
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(if (state.isRefreshing) rotationAngle else 0f)
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
                            screenContext = FleetErrorContext.DASHBOARD,
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
                                hasPendingPaymentsLoaded = state.hasPendingPaymentsLoaded,
                                vehicleStatus = state.vehicleStatus,
                                driverStatus = state.driverStatus,
                                tripSummary = state.tripSummary,
                                alertsSummary = state.alertsSummary,
                                hasFinancialAccess = state.hasFinancialAccess,
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
                                onAddVehicleCostClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddVehicleCost) },
                                onAddVehicleClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddVehicle) },
                                onAddDriverClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToAddDriver) },
                                onCreateTripClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToCreateTrip) }
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
    hasFinancialAccess: Boolean,
    onNavigateToVehicles: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToTrips: () -> Unit,
    onNavigateToMaps: () -> Unit,
    onNavigateToTeam: () -> Unit,
    onNavigateToReports: () -> Unit,
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

        // Reports & P/L - Only visible to Owner and General Manager
        if (hasFinancialAccess) {
            NavigationDrawerItem(
                icon = {
                    Text(
                        text = "📊",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Reports & P/L") },
                selected = false,
                onClick = onNavigateToReports,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

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
    hasPendingPaymentsLoaded: Boolean,
    vehicleStatus: VehicleStatusSummary,
    driverStatus: DriverStatusSummary,
    tripSummary: TripSummary,
    alertsSummary: AlertsSummary,
    hasFinancialAccess: Boolean,
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit,
    onMapsClick: () -> Unit,
    onAlertDismiss: (String) -> Unit,
    onCostFilterChange: (CostOverviewFilter) -> Unit,
    onAddTripCostClick: () -> Unit,
    onAddVehicleCostClick: () -> Unit,
    onAddVehicleClick: () -> Unit,
    onAddDriverClick: () -> Unit,
    onCreateTripClick: () -> Unit
    ) {
        // Determine if Cost Overview should be shown
        // Only show for Owner and General Manager (financial access)
        val hasFleet = vehicleStatus.total > 0 || tripSummary.total > 0
        val shouldShowCostOverview = hasFinancialAccess // Only show for roles with financial access

        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Fleet Overview Hero Card - Always show at top
        item {
            FleetOverviewHeroCard(
                vehicleStatus = vehicleStatus,
                driverStatus = driverStatus,
                tripSummary = tripSummary,
                onVehiclesClick = onVehiclesClick,
                onDriversClick = onDriversClick,
                onTripsClick = onTripsClick
            )
        }

        // 1. Quick Actions Section - Right after Fleet Overview
        item {
            QuickActionsSection(
                onVehiclesClick = onVehiclesClick,
                onDriversClick = onDriversClick,
                onTripsClick = onTripsClick,
                onMapsClick = onMapsClick,
                onAddTripCostClick = onAddTripCostClick,
                onAddVehicleCostClick = onAddVehicleCostClick,
                hasVehicles = vehicleStatus.total > 0,
                hasTrips = tripSummary.total > 0
            )
        }

        // 2. Cost Overview Section with Filter - only show for onboarding or when has cost data
        if (shouldShowCostOverview) {
            item {
                CostOverviewSection(
                    costOverview = costOverview,
                    selectedFilter = selectedCostFilter,
                    isLoading = isLoadingCostOverview,
                    onFilterChange = onCostFilterChange,
                    onAddTripCostClick = onAddTripCostClick,
                    onAddVehicleCostClick = onAddVehicleCostClick,
                    vehicleStatus = vehicleStatus,
                    tripSummary = tripSummary,
                    onAddVehicleClick = onAddVehicleClick,
                    onCreateTripClick = onCreateTripClick
                )
            }
        }

        // 3. Pending Payments Section - Only show for roles with financial access
        // Don't show during loading to avoid flickering when navigating back
        if (hasFinancialAccess && hasPendingPaymentsLoaded && tripSummary.total > 0 && (pendingPayments.isNotEmpty() || totalPendingAmount > 0)) {
            item {
                PendingPaymentsSection(
                    payments = pendingPayments,
                    totalPending = totalPendingAmount,
                    isLoading = false  // Never show loading state
                )
            }
        }

        // 4. Vehicle Status Section
        item {
            VehicleStatusSection(
                vehicleStatus = vehicleStatus,
                onClick = onVehiclesClick,
                onAddVehicleClick = onAddVehicleClick
            )
        }

        // 5. Trips Section
        item {
            TripsStatusSection(
                tripSummary = tripSummary,
                ongoingTrips = stats.liveStatus.ongoingTrips,
                onClick = onTripsClick,
                onCreateTripClick = onCreateTripClick
            )
        }

        // 6. Alerts Section - Always show to display alert status
        item {
            AlertsSection(
                alerts = stats.alerts,
                documentStats = stats.documentStats,
                alertsSummary = alertsSummary,
                vehicleStatus = vehicleStatus,
                onAlertDismiss = onAlertDismiss
            )
        }

        // 7. Drivers Section
        item {
            DriversStatusSection(
                driverStatus = driverStatus,
                onClick = onDriversClick,
                onAddDriverClick = onAddDriverClick
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
 * Get date range string for the selected filter.
 */
private fun getDateRangeForFilter(filter: CostOverviewFilter): String {
    // Use platform-specific currentTimeMillis and convert to date
    val currentTimeMs = com.indusjs.fleet.core.util.currentTimeMillis()

    // Approximate calculation for current date
    // Days since epoch (Jan 1, 1970)
    val daysSinceEpoch = currentTimeMs / (24 * 60 * 60 * 1000L)

    // Simple date calculation (approximate but sufficient for display)
    val year = 1970 + (daysSinceEpoch / 365.25).toInt()
    val dayOfYear = ((daysSinceEpoch % 365.25)).toInt()

    // Approximate month and day
    val monthDays = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var month = 1
    var remainingDays = dayOfYear
    for (i in 0 until 12) {
        if (remainingDays < monthDays[i]) {
            month = i + 1
            break
        }
        remainingDays -= monthDays[i]
    }
    val day = (remainingDays + 1).coerceIn(1, 31)

    return when (filter) {
        CostOverviewFilter.TODAY -> {
            val dayStr = if (day < 10) "0$day" else "$day"
            val monthStr = if (month < 10) "0$month" else "$month"
            "$dayStr-$monthStr-$year"
        }
        CostOverviewFilter.WEEKLY -> {
            // Calculate week range (simplified)
            val startDay = (day - (daysSinceEpoch % 7).toInt()).coerceAtLeast(1)
            val endDay = (startDay + 6).coerceAtMost(31)
            val startDayStr = if (startDay < 10) "0$startDay" else "$startDay"
            val endDayStr = if (endDay < 10) "0$endDay" else "$endDay"
            val monthStr = if (month < 10) "0$month" else "$month"
            "$startDayStr-$monthStr to $endDayStr-$monthStr"
        }
        CostOverviewFilter.MONTHLY -> {
            val monthName = when (month) {
                1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
                5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
                9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
                else -> ""
            }
            "$monthName $year"
        }
    }
}

/**
 * Cost Overview Section - Modern professional design with enhanced visuals.
 */
@Composable
private fun CostOverviewSection(
    costOverview: CostOverview,
    selectedFilter: CostOverviewFilter,
    isLoading: Boolean,
    onFilterChange: (CostOverviewFilter) -> Unit,
    onAddTripCostClick: () -> Unit,
    onAddVehicleCostClick: () -> Unit,
    vehicleStatus: VehicleStatusSummary,
    tripSummary: TripSummary,
    onAddVehicleClick: () -> Unit,
    onCreateTripClick: () -> Unit
) {
    val hasNoFleet = vehicleStatus.total == 0 && tripSummary.total == 0
    val profitColor = Color(0xFF10B981) // Green
    val lossColor = Color(0xFFEF4444) // Red
    val expenseColor = Color(0xFFF59E0B) // Amber

    // Calculate date range for display
    val dateRangeText = remember(selectedFilter) { getDateRangeForFilter(selectedFilter) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon with gradient background
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (costOverview.isProfit)
                                        listOf(Color(0xFF10B981), Color(0xFF059669))
                                    else
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Financial Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!hasNoFleet) {
                            Text(
                                text = when (selectedFilter) {
                                    CostOverviewFilter.TODAY -> "Today's summary"
                                    CostOverviewFilter.WEEKLY -> "This week's summary"
                                    CostOverviewFilter.MONTHLY -> "This month's summary"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Period Filter Tabs with date range (hide if no fleet)
            if (!hasNoFleet) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CostOverviewFilter.entries.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val label = when (filter) {
                                CostOverviewFilter.TODAY -> "Today"
                                CostOverviewFilter.WEEKLY -> "This Week"
                                CostOverviewFilter.MONTHLY -> "This Month"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { onFilterChange(filter) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Date range indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Content with fixed minimum height to prevent fluctuation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (hasNoFleet) {
                    // Empty State - Getting Started
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🚀", style = MaterialTheme.typography.headlineMedium)
                        }
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add vehicles and create trips to track your finances",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Financial Stats Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Financial Stats Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Expenses Card
                            FinancialStatCard(
                                modifier = Modifier.weight(1f),
                                icon = "💸",
                                label = "Expenses",
                                value = "₹${formatAmount(costOverview.totalExpenses)}",
                                backgroundColor = expenseColor.copy(alpha = 0.1f),
                                valueColor = expenseColor
                            )

                            // Profit/Loss Card
                            FinancialStatCard(
                                modifier = Modifier.weight(1f),
                                icon = if (costOverview.isProfit) "📈" else "📉",
                                label = if (costOverview.isProfit) "Profit" else "Loss",
                                value = "₹${formatAmount(kotlin.math.abs(costOverview.profitLoss))}",
                                backgroundColor = if (costOverview.isProfit) profitColor.copy(alpha = 0.1f) else lossColor.copy(alpha = 0.1f),
                                valueColor = if (costOverview.isProfit) profitColor else lossColor
                            )
                        }

                        // Trips Count Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🚛", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Completed Trips",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = costOverview.completedTrips.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Getting Started Buttons (only for empty state)
            if (!isLoading && hasNoFleet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddVehicleClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Vehicle", fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onCreateTripClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Trip", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Financial stat card with icon, label and value
 */
@Composable
private fun FinancialStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
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
 * Only shown when trips exist.
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pending Payments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                // Trips exist but no pending payments - success!
                SectionEmptyState(
                    iconRes = Res.drawable.ic_check,
                    title = "All payments collected!",
                    message = "Great job! No outstanding payments",
                    successStyle = true
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
 * Compact empty state for dashboard sections.
 * Shows an icon, message, and optional action button in a horizontal layout.
 */
@Composable
private fun SectionEmptyState(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    title: String,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    successStyle: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon with circular background
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (successStyle) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (successStyle) Color(0xFF4CAF50)
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (successStyle) Color(0xFF4CAF50)
                        else MaterialTheme.colorScheme.onSurface
            )
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Optional action button
        if (actionLabel != null && onAction != null) {
            FilledTonalButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium
                )
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
    onClick: () -> Unit,
    onAddVehicleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced header with icon container
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_truck),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Vehicles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${vehicleStatus.total} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_right),
                    contentDescription = "View all",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (vehicleStatus.total == 0) {
                // Empty state
                SectionEmptyState(
                    iconRes = Res.drawable.ic_vehicle,
                    title = "No vehicles yet",
                    message = "Add your first vehicle to start tracking",
                    actionLabel = "Add",
                    onAction = onAddVehicleClick
                )
            } else {
                // Enhanced stats row with better visual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedStatusChip(
                        label = "On Route",
                        count = vehicleStatus.onTripInProgress,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatusChip(
                        label = "Planned",
                        count = vehicleStatus.onTripPlanned,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatusChip(
                        label = "Available",
                        count = vehicleStatus.available,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Enhanced Status Chip with better visual design
 */
@Composable
private fun EnhancedStatusChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Trips Status Section - Enhanced UI.
 */
@Composable
private fun TripsStatusSection(
    tripSummary: TripSummary,
    ongoingTrips: List<OngoingTrip>,
    onClick: () -> Unit,
    onCreateTripClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced header with icon container
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_trip),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Trips",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tripSummary.total} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_right),
                    contentDescription = "View all",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tripSummary.total == 0) {
                // Empty state
                SectionEmptyState(
                    iconRes = Res.drawable.ic_trip,
                    title = "No trips yet",
                    message = "Create your first trip to start",
                    actionLabel = "Create",
                    onAction = onCreateTripClick
                )
            } else {
                // Enhanced stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedStatusChip(
                        label = "Active",
                        count = tripSummary.inProgress,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatusChip(
                        label = "Planned",
                        count = tripSummary.planned,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatusChip(
                        label = "Done",
                        count = tripSummary.completed,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Show ongoing trips preview
                if (ongoingTrips.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Active Trips",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ongoingTrips.take(2).forEach { trip ->
                        OngoingTripItem(trip = trip)
                    }
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
    alertsSummary: AlertsSummary,
    vehicleStatus: VehicleStatusSummary,
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
                // Alert count badge - use alertsSummary if available
                val alertCount = if (alertsSummary.totalAlerts > 0) alertsSummary.totalAlerts else alerts.size
                Text(
                    text = "$alertCount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Enhanced Alerts Summary with detailed counts
            if (alertsSummary.totalAlerts > 0) {
                // Priority breakdown row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (alertsSummary.criticalAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.criticalAlerts,
                            label = "Critical",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (alertsSummary.warningAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.warningAlerts,
                            label = "Warning",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    if (alertsSummary.infoAlerts > 0) {
                        AlertCountBadge(
                            count = alertsSummary.infoAlerts,
                            label = "Info",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Detailed expiry breakdown
                if (alertsSummary.documentExpired > 0 || alertsSummary.documentExpiring7Days > 0 ||
                    alertsSummary.licenseExpired > 0 || alertsSummary.licenseExpiring7Days > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Document expiry
                        if (alertsSummary.documentExpired > 0) {
                            ExpiryInfoChip(
                                icon = "📄",
                                count = alertsSummary.documentExpired,
                                label = "Docs Expired",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.documentExpiring7Days > 0) {
                            ExpiryInfoChip(
                                icon = "📄",
                                count = alertsSummary.documentExpiring7Days,
                                label = "Docs 7d",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        // License expiry
                        if (alertsSummary.licenseExpired > 0) {
                            ExpiryInfoChip(
                                icon = "📋",
                                count = alertsSummary.licenseExpired,
                                label = "License Expired",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (alertsSummary.licenseExpiring7Days > 0) {
                            ExpiryInfoChip(
                                icon = "📋",
                                count = alertsSummary.licenseExpiring7Days,
                                label = "License 7d",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            } else if (documentStats != null && (documentStats.expiringDocuments > 0 || documentStats.expiredDocuments > 0)) {
                // Fallback to document stats - clean text style
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

            // Determine which alerts to display - prefer alertsSummary.alerts if available
            val displayAlerts = if (alertsSummary.alerts.isNotEmpty()) alertsSummary.alerts else alerts

            // Check for document issues:
            // 1. Documents expired
            // 2. Documents expiring soon
            // 3. Vehicles exist but no/few documents uploaded
            val totalDocs = documentStats?.totalDocuments ?: 0
            val expiredDocs = documentStats?.expiredDocuments ?: 0
            val expiringDocs = documentStats?.expiringDocuments ?: 0

            // Missing documents: vehicles exist but total documents is less than vehicles
            // (Each vehicle should have at least 1 document like RC)
            val hasMissingDocuments = vehicleStatus.total > 0 && totalDocs < vehicleStatus.total
            val vehiclesWithoutDocs = if (hasMissingDocuments) (vehicleStatus.total - totalDocs).coerceAtLeast(0) else 0

            // Has document issues (expired or expiring)
            val hasDocumentIssues = expiredDocs > 0 || expiringDocs > 0

            val hasNoAlerts = displayAlerts.isEmpty() &&
                             alertsSummary.totalAlerts == 0 &&
                             !hasMissingDocuments &&
                             !hasDocumentIssues

            if (hasNoAlerts) {
                // No alerts - show success state
                SectionEmptyState(
                    iconRes = Res.drawable.ic_check,
                    title = "All clear!",
                    message = "No alerts at this time",
                    successStyle = true
                )
            } else {
                // Show missing documents warning
                if (hasMissingDocuments) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📄",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Missing Documents",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = if (vehiclesWithoutDocs > 0)
                                        "$vehiclesWithoutDocs vehicle(s) need documents uploaded"
                                    else
                                        "Some vehicles are missing required documents",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Show expired/expiring documents warning (when not showing missing docs warning)
                if (!hasMissingDocuments && hasDocumentIssues) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (expiredDocs > 0)
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (expiredDocs > 0) "⚠️" else "⏰",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (expiredDocs > 0) "Documents Expired" else "Documents Expiring Soon",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (expiredDocs > 0)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.tertiary
                                )
                                Text(
                                    text = buildString {
                                        if (expiredDocs > 0) append("$expiredDocs expired")
                                        if (expiredDocs > 0 && expiringDocs > 0) append(", ")
                                        if (expiringDocs > 0) append("$expiringDocs expiring soon")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (displayAlerts.isNotEmpty()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // Alert items - clean list style
                displayAlerts.take(3).forEach { alert ->
                    CleanAlertItem(
                        alert = alert,
                        onDismiss = { onAlertDismiss(alert.id) }
                    )
                }
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
            // Title with entity info for document/license alerts
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = alertColor
                )
                // Show days until expiry badge
                alert.daysUntilExpiry?.let { days ->
                    val badgeColor = when {
                        days < 0 -> MaterialTheme.colorScheme.error
                        days <= 7 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val badgeText = when {
                        days < 0 -> "${-days}d overdue"
                        days == 0 -> "Today"
                        else -> "${days}d left"
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = badgeColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Show vehicle registration for document alerts
            if (alert.type == AlertType.DOCUMENT_EXPIRY && alert.vehicleRegistrationNumber != null) {
                Text(
                    text = "🚛 ${alert.vehicleRegistrationNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Show driver name for license alerts
            if (alert.type == AlertType.LICENSE_EXPIRY && alert.driverName != null) {
                Text(
                    text = "👤 ${alert.driverName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

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
 * Drivers Status Section - Enhanced UI.
 */
@Composable
private fun DriversStatusSection(
    driverStatus: DriverStatusSummary,
    onClick: () -> Unit,
    onAddDriverClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enhanced header with icon container
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_driver),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Drivers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${driverStatus.total} total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_right),
                    contentDescription = "View all",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (driverStatus.total == 0) {
                // Empty state
                SectionEmptyState(
                    iconRes = Res.drawable.ic_driver,
                    title = "No drivers yet",
                    message = "Add your first driver to get started",
                    actionLabel = "Add",
                    onAction = onAddDriverClick
                )
            } else {
                // Enhanced stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnhancedStatusChip(
                        label = "On Route",
                        count = driverStatus.onTripInProgress,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatusChip(
                        label = "Planned",
                        count = driverStatus.onTripPlanned,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    EnhancedStatusChip(
                        label = "Available",
                        count = driverStatus.available,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
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
 * Quick Actions Section - Modern design with vector icons and improved accessibility.
 */
@Composable
private fun QuickActionsSection(
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit,
    onMapsClick: () -> Unit,
    onAddTripCostClick: () -> Unit,
    onAddVehicleCostClick: () -> Unit,
    hasVehicles: Boolean,
    hasTrips: Boolean
) {
    val showCostActions = hasVehicles || hasTrips

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Quick actions section" },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with accent line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Cost actions row (shown only when vehicles or trips exist)
            if (showCostActions) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Trip Cost - shown only when trips exist
                    if (hasTrips) {
                        VectorQuickActionButton(
                            iconRes = Res.drawable.ic_trip,
                            label = "Trip Cost",
                            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.12f),
                            iconTint = Color(0xFF4CAF50),
                            onClick = onAddTripCostClick,
                            modifier = Modifier.weight(1f),
                            contentDescription = "Add trip cost"
                        )
                    }
                    // Vehicle Cost - shown only when vehicles exist
                    if (hasVehicles) {
                        VectorQuickActionButton(
                            iconRes = Res.drawable.ic_settings,
                            label = "Vehicle Cost",
                            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.12f),
                            iconTint = Color(0xFFFF9800),
                            onClick = onAddVehicleCostClick,
                            modifier = Modifier.weight(1f),
                            contentDescription = "Add vehicle maintenance cost"
                        )
                    }
                }
            }

            // Navigation actions - 2x2 grid layout
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VectorQuickActionButton(
                        iconRes = Res.drawable.ic_truck,
                        label = "Vehicles",
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        onClick = onVehiclesClick,
                        modifier = Modifier.weight(1f),
                        contentDescription = "View vehicles"
                    )
                    VectorQuickActionButton(
                        iconRes = Res.drawable.ic_driver,
                        label = "Drivers",
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = onDriversClick,
                        modifier = Modifier.weight(1f),
                        contentDescription = "View drivers"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VectorQuickActionButton(
                        iconRes = Res.drawable.ic_trip,
                        label = "Trips",
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        onClick = onTripsClick,
                        modifier = Modifier.weight(1f),
                        contentDescription = "View trips"
                    )
                    VectorQuickActionButton(
                        iconRes = Res.drawable.ic_map,
                        label = "Live Map",
                        backgroundColor = Color(0xFF2196F3).copy(alpha = 0.12f),
                        iconTint = Color(0xFF2196F3),
                        onClick = onMapsClick,
                        modifier = Modifier.weight(1f),
                        contentDescription = "View live tracking map"
                    )
                }
            }
        }
    }
}

/**
 * Vector Quick Action Button with proper icon resources and animations.
 */
@Composable
private fun VectorQuickActionButton(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .semantics { this.contentDescription = contentDescription },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = iconTint
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    // Reset pressed state
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * Enhanced Quick Action Button with colored background and icon container.
 */
@Composable
private fun EnhancedQuickActionButton(
    icon: String,
    label: String,
    backgroundColor: Color,
    iconBackgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon with colored circle background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Clean Quick Action Button - no colored background (kept for backward compatibility)
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

// ============ Fleet Overview Hero Card ============

/**
 * Fleet Overview Hero Card - Key metrics at a glance with fleet health indicator
 */
@Composable
private fun FleetOverviewHeroCard(
    vehicleStatus: VehicleStatusSummary,
    driverStatus: DriverStatusSummary,
    tripSummary: TripSummary,
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit
) {
    // Simple Fleet Overview without border and health indicator
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Fleet overview summary" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Text(
            text = "Fleet Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vehicles Metric
            FleetMetricCard(
                icon = "🚛",
                value = vehicleStatus.total,
                label = "Vehicles",
                subLabel = "${vehicleStatus.available} available",
                onClick = onVehiclesClick,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.primary
            )

            // Drivers Metric
            FleetMetricCard(
                icon = "👨‍✈️",
                value = driverStatus.total,
                label = "Drivers",
                subLabel = "${driverStatus.available} available",
                onClick = onDriversClick,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.secondary
            )

            // Trips Metric
            FleetMetricCard(
                icon = "🗺️",
                value = tripSummary.total,
                label = "Trips",
                subLabel = "${tripSummary.inProgress} active",
                onClick = onTripsClick,
                modifier = Modifier.weight(1f),
                accentColor = MaterialTheme.colorScheme.tertiary
            )
        }

        // Bottom color range bar showing vehicle status distribution
        if (vehicleStatus.total > 0) {
            FleetStatusRatioBar(vehicleStatus = vehicleStatus)
        }
    }
}

/**
 * Fleet Status Ratio Bar - Linear color bar showing vehicle status distribution
 */
@Composable
private fun FleetStatusRatioBar(vehicleStatus: VehicleStatusSummary) {
    val total = vehicleStatus.total.toFloat().coerceAtLeast(1f)

    val onRouteCount = vehicleStatus.onTripInProgress
    val plannedCount = vehicleStatus.onTripPlanned
    val availableCount = vehicleStatus.available
    val maintenanceCount = vehicleStatus.underMaintenance
    val inactiveCount = vehicleStatus.inactive

    val onRouteFraction = onRouteCount / total
    val plannedFraction = plannedCount / total
    val availableFraction = availableCount / total
    val maintenanceFraction = maintenanceCount / total
    val inactiveFraction = inactiveCount / total

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Color bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // On Route - Green
            if (onRouteFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(onRouteFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF4CAF50))
                )
            }
            // Planned - Blue
            if (plannedFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(plannedFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF2196F3))
                )
            }
            // Available - Teal
            if (availableFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(availableFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF009688))
                )
            }
            // Maintenance - Orange
            if (maintenanceFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(maintenanceFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFFFF9800))
                )
            }
            // Inactive - Gray
            if (inactiveFraction > 0) {
                Box(
                    modifier = Modifier
                        .weight(inactiveFraction.coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(0xFF9E9E9E))
                )
            }
        }

        // Legend row - compact horizontal layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (onRouteCount > 0) {
                RatioLegendItem(color = Color(0xFF4CAF50), label = "Route", count = onRouteCount)
            }
            if (plannedCount > 0) {
                RatioLegendItem(color = Color(0xFF2196F3), label = "Planned", count = plannedCount)
            }
            if (availableCount > 0) {
                RatioLegendItem(color = Color(0xFF009688), label = "Available", count = availableCount)
            }
            if (maintenanceCount > 0) {
                RatioLegendItem(color = Color(0xFFFF9800), label = "Maint.", count = maintenanceCount)
            }
            if (inactiveCount > 0) {
                RatioLegendItem(color = Color(0xFF9E9E9E), label = "Inactive", count = inactiveCount)
            }
        }
    }
}

/**
 * Ratio Legend Item - Compact legend with color dot, count, and label
 */
@Composable
private fun RatioLegendItem(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Fleet Metric Card - Individual metric display
 */
@Composable
private fun FleetMetricCard(
    icon: String,
    value: Int,
    label: String,
    subLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color
) {
    // Use key to prevent recomposition animation issues
    // Show value directly without animation to prevent shuffling
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Fleet Utilization Bar - Stacked bar showing vehicle status distribution
 */
@Composable
private fun FleetUtilizationBar(vehicleStatus: VehicleStatusSummary) {
    val total = vehicleStatus.total.toFloat().coerceAtLeast(1f)

    val onTripFraction = (vehicleStatus.onTripPlanned + vehicleStatus.onTripInProgress) / total
    val maintenanceFraction = vehicleStatus.underMaintenance / total
    val availableFraction = vehicleStatus.available / total
    val inactiveFraction = vehicleStatus.inactive / total

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // On Trip - Green
        if (onTripFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(onTripFraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFF4CAF50))
            )
        }
        // Available - Blue
        if (availableFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(availableFraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFF2196F3))
            )
        }
        // Maintenance - Orange
        if (maintenanceFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(maintenanceFraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFFFF9800))
            )
        }
        // Inactive - Gray
        if (inactiveFraction > 0) {
            Box(
                modifier = Modifier
                    .weight(inactiveFraction.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .background(Color(0xFF9E9E9E))
            )
        }
    }

    // Legend
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UtilizationLegendItem(color = Color(0xFF4CAF50), label = "On Trip")
        UtilizationLegendItem(color = Color(0xFF2196F3), label = "Available")
        UtilizationLegendItem(color = Color(0xFFFF9800), label = "Maintenance")
        UtilizationLegendItem(color = Color(0xFF9E9E9E), label = "Inactive")
    }
}

/**
 * Utilization Legend Item
 */
@Composable
private fun UtilizationLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

/**
 * Alert Count Badge - Shows alert count by priority
 */
@Composable
private fun AlertCountBadge(
    count: Int,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/**
 * Expiry Info Chip - Shows expiry info with icon
 */
@Composable
private fun ExpiryInfoChip(
    icon: String,
    count: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
