package com.indusjs.fleet.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.feature.dashboard.domain.entity.Alert
import com.indusjs.fleet.feature.dashboard.domain.entity.AlertType
import com.indusjs.fleet.feature.dashboard.domain.entity.DashboardStats
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
                    title = { Text("Fleet Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text("☰", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.sendIntent(DashboardContract.Intent.RefreshDashboard) }) {
                            Text("↻", style = MaterialTheme.typography.titleLarge)
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Text("👤", style = MaterialTheme.typography.titleLarge)
                        }
                    }
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
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(DashboardContract.Intent.LoadDashboard) },
                        modifier = Modifier.align(Alignment.Center)
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
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "🚚",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fleet Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu Items
        NavigationDrawerItem(
            icon = { Text("🚗", style = MaterialTheme.typography.titleLarge) },
            label = { Text("Vehicles") },
            selected = false,
            onClick = onNavigateToVehicles,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Text("👤", style = MaterialTheme.typography.titleLarge) },
            label = { Text("Drivers") },
            selected = false,
            onClick = onNavigateToDrivers,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Text("🛣️", style = MaterialTheme.typography.titleLarge) },
            label = { Text("Trips") },
            selected = false,
            onClick = onNavigateToTrips,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Text("🗺️", style = MaterialTheme.typography.titleLarge) },
            label = { Text("Live Map") },
            selected = false,
            onClick = onNavigateToMaps,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        NavigationDrawerItem(
            icon = { Text("👥", style = MaterialTheme.typography.titleLarge) },
            label = { Text("Team Members") },
            selected = false,
            onClick = onNavigateToTeam,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        NavigationDrawerItem(
            icon = { Text("⚙️", style = MaterialTheme.typography.titleLarge) },
            label = { Text("Profile & Settings") },
            selected = false,
            onClick = onNavigateToProfile,
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
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Stats Row
        item {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StatCard(
                        title = "Vehicles",
                        value = "${stats.activeVehicles}/${stats.totalVehicles}",
                        subtitle = "Active",
                        emoji = "🚗",
                        onClick = onVehiclesClick
                    )
                }
                item {
                    StatCard(
                        title = "Drivers",
                        value = "${stats.activeDrivers}/${stats.totalDrivers}",
                        subtitle = "On Duty",
                        emoji = "👤",
                        onClick = onDriversClick
                    )
                }
                item {
                    StatCard(
                        title = "Trips",
                        value = "${stats.ongoingTrips}",
                        subtitle = "Ongoing",
                        emoji = "🛣️",
                        onClick = onTripsClick
                    )
                }
                item {
                    StatCard(
                        title = "Distance",
                        value = "${stats.totalDistance.toInt()} km",
                        subtitle = "Today",
                        emoji = "📏",
                        onClick = {}
                    )
                }
            }
        }


        // Alerts Section
        if (stats.alerts.isNotEmpty()) {
            item {
                Text(
                    text = "Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(stats.alerts, key = { it.id }) { alert ->
                AlertCard(
                    alert = alert,
                    onDismiss = { onAlertDismiss(alert.id) }
                )
            }
        }

        // Today's Summary
        item {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SummaryCard(
                completedTrips = stats.completedTripsToday,
                totalDistance = stats.totalDistance,
                fuelConsumption = stats.fuelConsumption
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
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
        colors = CardDefaults.cardColors(
            containerColor = alertColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDismiss) {
                Text("✕", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    completedTrips: Int,
    totalDistance: Double,
    fuelConsumption: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Completed Trips",
                    value = completedTrips.toString()
                )
                SummaryItem(
                    label = "Total Distance",
                    value = "${totalDistance.toInt()} km"
                )
                SummaryItem(
                    label = "Fuel Used",
                    value = "${fuelConsumption.toInt()} L"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "❌",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
