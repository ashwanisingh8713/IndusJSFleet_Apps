package com.indusjs.fleet.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.dashboard.Alert
import com.indusjs.fleet.domain.entity.dashboard.AlertType
import com.indusjs.fleet.domain.entity.dashboard.DashboardStats
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
    onNavigateToTeam: () -> Unit = {}
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
                            if (state.userRole.isNotEmpty()) {
                                Text(
                                    text = state.userRole,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                    state.isLoading -> {
                        // Using reusable LoadingContent component
                        LoadingContent(message = "Loading dashboard...")
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(DashboardContract.Intent.LoadDashboard) }
                    )
                }
                else -> {
                    DashboardContent(
                        stats = state.stats,
                        onVehiclesClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToVehicles) },
                        onDriversClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToDrivers) },
                        onTripsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToTrips) },
                        onMapsClick = { viewModel.sendIntent(DashboardContract.Intent.NavigateToMaps) },
                        onAlertDismiss = { alertId ->
                            viewModel.sendIntent(DashboardContract.Intent.DismissAlert(alertId))
                        }
                    )
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
    onVehiclesClick: () -> Unit,
    onDriversClick: () -> Unit,
    onTripsClick: () -> Unit,
    onMapsClick: () -> Unit,
    onAlertDismiss: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Banner Card
        item {
            WelcomeBannerCard(
                activeVehicles = stats.activeVehicles,
                ongoingTrips = stats.ongoingTrips
            )
        }

        // Quick Actions Section in Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                QuickActionsSectionContent(
                    onVehiclesClick = onVehiclesClick,
                    onDriversClick = onDriversClick,
                    onTripsClick = onTripsClick,
                    onMapsClick = onMapsClick
                )
            }
        }

        // Fleet Overview Section in Card
        item {
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
                    Text(
                        text = "Fleet Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Vehicles",
                            value = "${stats.activeVehicles}/${stats.totalVehicles}",
                            subtitle = "Active",
                            emoji = "🚛",
                            onClick = onVehiclesClick,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Drivers",
                            value = "${stats.activeDrivers}/${stats.totalDrivers}",
                            subtitle = "On Duty",
                            emoji = "👨‍✈️",
                            onClick = onDriversClick,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Trips",
                            value = "${stats.ongoingTrips}",
                            subtitle = "Active",
                            emoji = "🗺️",
                            onClick = onTripsClick,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Distance",
                            value = "${stats.totalDistance.toInt()}km",
                            subtitle = "Today",
                            emoji = "📍",
                            onClick = {},
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Alerts Section in Card
        if (stats.alerts.isNotEmpty()) {
            item {
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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Alerts",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "${stats.alerts.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Alert items
                        stats.alerts.forEach { alert ->
                            AlertCardCompact(
                                alert = alert,
                                onDismiss = { onAlertDismiss(alert.id) }
                            )
                        }
                    }
                }
            }
        }

        // Today's Summary Section in Card
        item {
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
                    Text(
                        text = "Today's Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    SummaryContent(
                        completedTrips = stats.completedTripsToday,
                        totalDistance = stats.totalDistance,
                        fuelConsumption = stats.fuelConsumption
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
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
                Text(
                    text = "🚚",
                    style = MaterialTheme.typography.headlineMedium
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
                    text = "⚠️",
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
                text = "⚠️",
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

