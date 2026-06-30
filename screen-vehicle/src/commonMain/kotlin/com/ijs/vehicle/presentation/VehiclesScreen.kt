package com.ijs.vehicle.presentation

import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.DeleteConfirmationDialog
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FilterDefinition
import com.indusjs.uicomponents.components.FleetFilterBar
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
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
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(VehiclesContract.Intent.RefreshVehicles) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                        modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                modifier = Modifier.padding(
                    horizontal = FleetTokens.Spacing.ScreenHorizontal,
                    vertical = FleetTokens.Spacing.S
                )
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
                modifier = Modifier.padding(
                    horizontal = FleetTokens.Spacing.ScreenHorizontal,
                    vertical = FleetTokens.Spacing.S
                )
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
    DeleteConfirmationDialog(
        showDialog = state.showDeleteConfirmation,
        entityName = stringResource(Res.string.vehicle_entity_singular),
        onConfirmDelete = { viewModel.sendIntent(VehiclesContract.Intent.ConfirmDelete) },
        onDismiss = { viewModel.sendIntent(VehiclesContract.Intent.DismissDelete) }
    )
}

@Composable
private fun VehicleList(
    vehicles: List<Vehicle>,
    onVehicleClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    stateLabels: Map<String, String> = emptyMap()
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact stays 1-up; Medium/Expanded show two cards per row. On Expanded
        // the grid is capped to a readable width and centered instead of stretching.
        val columns = if (breakpoint.isAtLeastMedium) 2 else 1
        val contentWidthModifier = if (breakpoint.isExpanded) {
            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxWidth()
        }

        LazyColumn(
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            if (columns == 1) {
                items(vehicles, key = { it.id }) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onClick = { onVehicleClick(vehicle.id) },
                        onDeleteClick = { onDeleteClick(vehicle.id) },
                        stateLabels = stateLabels
                    )
                }
            } else {
                val rows = vehicles.chunked(columns)
                items(rows, key = { row -> row.first().id }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        row.forEach { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onClick = { onVehicleClick(vehicle.id) },
                                onDeleteClick = { onDeleteClick(vehicle.id) },
                                stateLabels = stateLabels,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Keep the last odd card aligned to a single column width.
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

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    stateLabels: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    FleetSectionCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
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
                            .size(FleetTokens.IconSize.XL)
                            .clip(RoundedCornerShape(FleetTokens.Radius.L))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        VehicleTypeIcon(
                            type = vehicle.type,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                    Column {
                        Text(
                            text = vehicle.registrationNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXS))
                        Text(
                            text = "${vehicle.make} ${vehicle.model} (${vehicle.year})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = vehicle.status, stateLabels = stateLabels)
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Info Row - Stats displayed inline without backgrounds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VehicleInfoItem(
                    iconRes = Res.drawable.ic_fuel,
                    value = "${vehicle.fuelLevel}%",
                    label = stringResource(Res.string.vehicle_list_label_fuel)
                )

                VerticalDivider(
                    modifier = Modifier.height(FleetTokens.Spacing.XXL),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                VehicleInfoItem(
                    iconRes = Res.drawable.ic_speed,
                    value = stringResource(Res.string.vehicle_trips_km, vehicle.mileage.toInt()),
                    label = stringResource(Res.string.vehicle_list_label_mileage)
                )

                VerticalDivider(
                    modifier = Modifier.height(FleetTokens.Spacing.XXL),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                VehicleInfoItem(
                    iconRes = Res.drawable.ic_profile,
                    value = vehicle.assignedDriver?.fullName()?.take(12)
                        ?: vehicle.assignedDriverName?.take(12)
                        ?: stringResource(Res.string.label_not_applicable),
                    label = stringResource(Res.string.vehicle_list_label_driver)
                )
            }

            // Location if available
            vehicle.lastLocation?.let { location ->
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_map),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.S),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
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
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    // Transparent, centered stat (no tinted box) — preserve original look via
    // showBackground = false + centered = true; vector icon chip, valueColor = onSurface.
    FleetMetricTile(
        value = value,
        label = label,
        iconRes = iconRes,
        valueColor = MaterialTheme.colorScheme.onSurface,
        showBackground = false,
        centered = true,
        // Compact value style — the 3-up Fuel/Mileage/Driver stats don't need headline size
        // (matches the Drivers list); keeps "0 km" / "N/A" from looking oversized.
        valueStyle = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(horizontal = FleetTokens.Spacing.S)
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
