package com.indusjs.fleet.feature.maps.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.feature.maps.domain.entity.MapVehicle
import com.indusjs.fleet.feature.maps.domain.entity.MapVehicleStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

/**
 * Maps/Live Tracking Screen composable.
 * Note: This is a placeholder screen. Actual map integration requires
 * platform-specific implementations (Google Maps, MapBox, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    viewModel: MapsViewModel,
    onNavigateToVehicleDetail: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Auto-refresh locations when live tracking is enabled
    LaunchedEffect(state.isLiveTrackingEnabled) {
        while (state.isLiveTrackingEnabled) {
            delay(10000) // Refresh every 10 seconds
            viewModel.sendIntent(MapsContract.Intent.RefreshVehicleLocations)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MapsContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MapsContract.Effect.ZoomToVehicle -> {
                    // Handle map zoom - would be implemented with actual map SDK
                }
                is MapsContract.Effect.NavigateToVehicleDetail -> {
                    onNavigateToVehicleDetail(effect.vehicleId)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Live Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sendIntent(MapsContract.Intent.ToggleLiveTracking) }
                    ) {
                        Text(
                            text = if (state.isLiveTrackingEnabled) "📡" else "📴",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    IconButton(
                        onClick = { viewModel.sendIntent(MapsContract.Intent.RefreshVehicleLocations) }
                    ) {
                        Text("↻", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(MapsContract.Intent.LoadMapData) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Map placeholder with vehicle list
                    MapPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    // Vehicle List Bottom Sheet
                    VehicleListBottomSection(
                        vehicles = state.vehicles,
                        selectedVehicle = state.selectedVehicle,
                        onVehicleClick = { vehicleId ->
                            viewModel.sendIntent(MapsContract.Intent.SelectVehicle(vehicleId))
                        },
                        onNavigateToDetail = { vehicleId ->
                            viewModel.sendIntent(MapsContract.Intent.NavigateToVehicleDetail(vehicleId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🗺️",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Map View",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Integration with map SDK required",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Supports: Google Maps, MapBox, OpenStreetMap",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun VehicleListBottomSection(
    vehicles: List<MapVehicle>,
    selectedVehicle: MapVehicle?,
    onVehicleClick: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Handle bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }

            // Title and stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VehicleStatusCount(
                        count = vehicles.count { it.status == MapVehicleStatus.MOVING },
                        label = "Moving",
                        color = MaterialTheme.colorScheme.primary
                    )
                    VehicleStatusCount(
                        count = vehicles.count { it.status == MapVehicleStatus.IDLE || it.status == MapVehicleStatus.STOPPED },
                        label = "Stopped",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    VehicleStatusCount(
                        count = vehicles.count { it.status == MapVehicleStatus.OFFLINE },
                        label = "Offline",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider()

            // Vehicle list
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(vehicles, key = { it.id }) { vehicle ->
                    MapVehicleItem(
                        vehicle = vehicle,
                        isSelected = selectedVehicle?.id == vehicle.id,
                        onClick = { onVehicleClick(vehicle.id) },
                        onDetailClick = { onNavigateToDetail(vehicle.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleStatusCount(
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
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
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MapVehicleItem(
    vehicle: MapVehicle,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status indicator
            VehicleStatusIndicator(status = vehicle.status)

            Column {
                Text(
                    text = vehicle.vehicleNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = vehicle.driverName ?: "No driver",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                vehicle.location.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (vehicle.status == MapVehicleStatus.MOVING) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${vehicle.speed.toInt()} km/h",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDetailClick) {
                Text("›", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Composable
private fun VehicleStatusIndicator(status: MapVehicleStatus) {
    val color = when (status) {
        MapVehicleStatus.MOVING -> MaterialTheme.colorScheme.primary
        MapVehicleStatus.IDLE -> MaterialTheme.colorScheme.tertiary
        MapVehicleStatus.STOPPED -> MaterialTheme.colorScheme.secondary
        MapVehicleStatus.OFFLINE -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
