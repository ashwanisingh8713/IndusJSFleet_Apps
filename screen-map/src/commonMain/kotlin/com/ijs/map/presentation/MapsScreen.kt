package com.ijs.map.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.fleet.domain.entity.maps.MapVehicle
import com.indusjs.fleet.domain.entity.maps.MapVehicleStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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

    // Live positions arrive via push over the WebSocket (driven by the ViewModel),
    // so there is no client-side polling loop. The manual refresh action re-seeds
    // the markers from the tenant's vehicle list and reconnects the live feed.

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
                title = { Text(stringResource(Res.string.maps_title)) },
                actions = {
                    IconButton(
                        onClick = { viewModel.sendIntent(MapsContract.Intent.ToggleLiveTracking) }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_map),
                            contentDescription = null,
                            tint = if (state.isLiveTrackingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.sendIntent(MapsContract.Intent.RefreshVehicleLocations) }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error!!,
                        screenContext = FleetErrorContext.MAPS,
                        onRetry = { viewModel.sendIntent(MapsContract.Intent.LoadMapData) }
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
                        liveConnectionStatus = state.liveConnectionStatus,
                        onVehicleClick = { vehicleId ->
                            viewModel.sendIntent(MapsContract.Intent.SelectVehicle(vehicleId))
                        },
                        onNavigateToDetail = { vehicleId ->
                            viewModel.sendIntent(MapsContract.Intent.NavigateToVehicleDetail(vehicleId))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = BOTTOM_SHEET_MAX_HEIGHT)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XXL)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_map),
                contentDescription = null,
                modifier = Modifier.size(FleetTokens.IconSize.XXL),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            Text(
                text = stringResource(Res.string.maps_map_view),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.maps_sdk_required),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = stringResource(Res.string.maps_sdk_supports),
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
    liveConnectionStatus: MapsContract.LiveConnectionStatus,
    onVehicleClick: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shadowElevation = FleetTokens.Elevation.Dialog,
        color = MaterialTheme.colorScheme.surface
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val breakpoint = rememberFleetBreakpoint()
            // Compact stays 1-up; Medium/Expanded show two vehicles per row. On
            // Expanded the list is capped to a readable width and centered so the
            // rows don't stretch edge-to-edge on tablets / the web app.
            val columns = if (breakpoint.isAtLeastMedium) 2 else 1
            val contentWidthModifier = if (breakpoint.isExpanded) {
                Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(modifier = contentWidthModifier.align(Alignment.TopCenter)) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = FleetTokens.Spacing.S),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(DRAG_HANDLE_WIDTH)
                            .height(FleetTokens.Spacing.XS)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }

                // Title and stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.S),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        // Live-feed connection indicator: a coloured dot plus a
                        // localized status label so the user knows whether markers
                        // are updating in real time or are last-known seeds.
                        LiveStatusIndicator(status = liveConnectionStatus)
                        Text(
                            text = stringResource(Res.string.org_stats_vehicles),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                        VehicleStatusCount(
                            count = vehicles.count { it.status == MapVehicleStatus.MOVING },
                            label = stringResource(Res.string.maps_moving),
                            color = MaterialTheme.colorScheme.primary
                        )
                        VehicleStatusCount(
                            count = vehicles.count { it.status == MapVehicleStatus.IDLE || it.status == MapVehicleStatus.STOPPED },
                            label = stringResource(Res.string.maps_stopped),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        VehicleStatusCount(
                            count = vehicles.count { it.status == MapVehicleStatus.OFFLINE },
                            label = stringResource(Res.string.maps_offline),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider()

                // Vehicle list
                if (vehicles.isEmpty()) {
                    EmptyContent(
                        iconRes = Res.drawable.ic_map,
                        title = stringResource(Res.string.maps_no_live_vehicles),
                        fillMaxSize = false,
                        modifier = Modifier.padding(vertical = FleetTokens.Spacing.XL)
                    )
                } else if (columns == 1) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = FleetTokens.Spacing.S)
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
                } else {
                    val rows = vehicles.chunked(columns)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = FleetTokens.Spacing.L,
                            vertical = FleetTokens.Spacing.S
                        ),
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        items(rows, key = { row -> row.first().id }) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                            ) {
                                row.forEach { vehicle ->
                                    MapVehicleItem(
                                        vehicle = vehicle,
                                        isSelected = selectedVehicle?.id == vehicle.id,
                                        onClick = { onVehicleClick(vehicle.id) },
                                        onDetailClick = { onNavigateToDetail(vehicle.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Keep the trailing odd item aligned to a single column.
                                if (row.size < columns) {
                                    repeat(columns - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
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
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Box(
            modifier = Modifier
                .size(FleetTokens.Spacing.S)
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
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(FleetTokens.Radius.M))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.M),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
            modifier = Modifier.weight(1f)
        ) {
            // Status indicator
            VehicleStatusIndicator(status = vehicle.status)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.vehicleNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = vehicle.driverName ?: stringResource(Res.string.maps_no_driver),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                vehicle.location.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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
private fun LiveStatusIndicator(status: MapsContract.LiveConnectionStatus) {
    val color = when (status) {
        MapsContract.LiveConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
        MapsContract.LiveConnectionStatus.CONNECTING -> MaterialTheme.colorScheme.tertiary
        MapsContract.LiveConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.error
        MapsContract.LiveConnectionStatus.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val label = when (status) {
        MapsContract.LiveConnectionStatus.CONNECTED -> stringResource(Res.string.maps_live_connected)
        MapsContract.LiveConnectionStatus.CONNECTING -> stringResource(Res.string.maps_live_connecting)
        MapsContract.LiveConnectionStatus.DISCONNECTED -> stringResource(Res.string.maps_live_disconnected)
        MapsContract.LiveConnectionStatus.IDLE -> stringResource(Res.string.maps_live_idle)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Box(
            modifier = Modifier
                .size(FleetTokens.Spacing.M)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
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
            .size(FleetTokens.Spacing.M)
            .clip(CircleShape)
            .background(color)
    )
}

/** Drag-handle pill width on the bottom sheet (decorative, not in the token scale). */
private val DRAG_HANDLE_WIDTH = FleetTokens.Spacing.XXL + FleetTokens.Spacing.S

/** Max height of the live-vehicle bottom sheet so the map stays visible. */
private val BOTTOM_SHEET_MAX_HEIGHT = FleetTokens.Spacing.XXXL * 6 + FleetTokens.Spacing.M
