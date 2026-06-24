package com.ijs.vehicle.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FilterDefinition
import com.indusjs.uicomponents.components.FleetFilterBar
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.entity.VehicleType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Vehicles List Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    viewModel: VehiclesViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VehiclesContract.Effect.NavigateToVehicleDetail -> onNavigateToDetail(effect.vehicleId)
                is VehiclesContract.Effect.NavigateToAddVehicle -> onNavigateToAdd()
                is VehiclesContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.vehicles_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(VehiclesContract.Intent.RefreshVehicles) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
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
        },
        floatingActionButton = {
            // Only show FAB when there's content (not empty and not loading)
            if (!state.isLoading && state.filteredVehicles.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.sendIntent(VehiclesContract.Intent.AddVehicle) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.vehicles_add),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(VehiclesContract.Intent.RefreshVehicles) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
            // Search Bar - using reusable component
            FleetSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(VehiclesContract.Intent.SearchVehicles(it)) },
                placeholder = stringResource(Res.string.vehicles_search_placeholder),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val statusCountByStatus = remember(state.vehicles) {
                state.vehicles.groupingBy { it.status }.eachCount()
            }
            val vehicleStatusFilters = remember(statusCountByStatus, state.stateLabels) {
                VehicleStatus.entries.map { status ->
                    val apiValue = VehicleStatus.toApiString(status)
                    FilterDefinition(
                        id = status,
                        label = state.stateLabels[apiValue] ?: VehicleStatus.getDisplayLabel(status),
                        count = statusCountByStatus[status]
                    )
                }
            }
            FleetFilterBar(
                filters = vehicleStatusFilters,
                selectedFilterId = state.selectedStatusFilter,
                onFilterSelected = { id ->
                    when {
                        id == null -> viewModel.sendIntent(VehiclesContract.Intent.FilterByStatus(null))
                        id == state.selectedStatusFilter ->
                            viewModel.sendIntent(VehiclesContract.Intent.FilterByStatus(null))
                        else -> viewModel.sendIntent(VehiclesContract.Intent.FilterByStatus(id))
                    }
                },
                allCount = state.vehicles.size.takeIf { it > 0 },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when {
                state.isLoading -> {
                    // Using reusable LoadingContent component
                    LoadingContent(message = stringResource(Res.string.loading))
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!.resolve(),
                        screenContext = FleetErrorContext.VEHICLES,
                        onRetry = { viewModel.sendIntent(VehiclesContract.Intent.LoadVehicles) }
                    )
                }
                state.filteredVehicles.isEmpty() -> {
                    // Using reusable EmptyContent component with SVG icon
                    val isFiltering = state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null
                    if (isFiltering) {
                        EmptyContent(
                            iconRes = Res.drawable.ic_search,
                            title = stringResource(Res.string.no_data_for_filter),
                            message = stringResource(Res.string.adjust_search_filters)
                        )
                    } else {
                        EmptyContent(
                            iconRes = Res.drawable.ic_vehicle,
                            title = stringResource(Res.string.vehicles_empty_title),
                            message = stringResource(Res.string.vehicles_empty_message),
                            actionLabel = stringResource(Res.string.vehicles_add),
                            onAction = { viewModel.sendIntent(VehiclesContract.Intent.AddVehicle) }
                        )
                    }
                }
                else -> {
                    VehicleList(
                        vehicles = state.filteredVehicles,
                        onVehicleClick = { viewModel.sendIntent(VehiclesContract.Intent.SelectVehicle(it)) },
                        onDeleteClick = { viewModel.sendIntent(VehiclesContract.Intent.DeleteVehicle(it)) },
                        stateLabels = state.stateLabels
                    )
                }
            }
        }
        }
    }

    // Delete confirmation dialog
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(VehiclesContract.Intent.DismissDelete) },
            title = { Text(stringResource(Res.string.delete_confirmation_title)) },
            text = { Text(stringResource(Res.string.vehicle_delete_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(VehiclesContract.Intent.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(VehiclesContract.Intent.DismissDelete) }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun VehicleList(
    vehicles: List<Vehicle>,
    onVehicleClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    stateLabels: Map<String, String> = emptyMap()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(vehicles, key = { it.id }) { vehicle ->
            VehicleCard(
                vehicle = vehicle,
                onClick = { onVehicleClick(vehicle.id) },
                onDeleteClick = { onDeleteClick(vehicle.id) },
                stateLabels = stateLabels
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    stateLabels: Map<String, String> = emptyMap()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row - Vehicle Icon, Registration, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        VehicleTypeIcon(
                            type = vehicle.type,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicle.registrationNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${vehicle.make} ${vehicle.model} (${vehicle.year})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = vehicle.status, stateLabels = stateLabels)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Info Row - Stats displayed inline without backgrounds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VehicleInfoItem(
                    icon = "⛽",
                    value = "${vehicle.fuelLevel}%",
                    label = stringResource(Res.string.vehicle_list_label_fuel)
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                VehicleInfoItem(
                    icon = "📏",
                    value = stringResource(Res.string.vehicle_trips_km, vehicle.mileage.toInt()),
                    label = stringResource(Res.string.vehicle_list_label_mileage)
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                VehicleInfoItem(
                    icon = "👤",
                    value = vehicle.assignedDriver?.fullName()?.take(12)
                        ?: vehicle.assignedDriverName?.take(12)
                        ?: stringResource(Res.string.label_not_applicable),
                    label = stringResource(Res.string.vehicle_list_label_driver)
                )
            }

            // Location if available
            vehicle.lastLocation?.let { location ->
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = location.address ?: stringResource(Res.string.vehicle_list_unknown_location),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoItem(
    icon: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    // Transparent, centered stat (no tinted box) — preserve original look via
    // showBackground = false + centered = true; emoji passes through, valueColor = onSurface.
    FleetMetricTile(
        value = value,
        label = label,
        emoji = icon,
        valueColor = MaterialTheme.colorScheme.onSurface,
        showBackground = false,
        centered = true,
        modifier = modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun StatusBadge(status: VehicleStatus, stateLabels: Map<String, String> = emptyMap()) {
    val colorScheme = VehicleStatus.getColorScheme(status)
    val color = com.indusjs.uicomponents.components.stateColorSchemeToColor(colorScheme)
    val apiValue = VehicleStatus.toApiString(status)
    val text = stateLabels[apiValue] ?: VehicleStatus.getDisplayLabel(status)

    // Using reusable FleetStatusBadge component
    FleetStatusBadge(
        status = text,
        color = color
    )
}


/**
 * Composable to display vehicle type icon.
 */
@Composable
private fun VehicleTypeIcon(
    type: VehicleType,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val iconRes = when (type) {
        VehicleType.TRUCK -> Res.drawable.ic_truck
        VehicleType.VAN -> Res.drawable.ic_van
        VehicleType.CAR -> Res.drawable.ic_car
        VehicleType.BUS -> Res.drawable.ic_bus
        VehicleType.MOTORCYCLE -> Res.drawable.ic_motorcycle
        VehicleType.TRAILER -> Res.drawable.ic_trailer
    }
    Icon(
        painter = painterResource(iconRes),
        contentDescription = type.name,
        modifier = modifier,
        tint = tint
    )
}
