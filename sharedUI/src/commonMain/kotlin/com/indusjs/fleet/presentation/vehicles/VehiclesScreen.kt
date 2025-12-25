package com.indusjs.fleet.presentation.vehicles

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetSearchField
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

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

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VehiclesContract.Effect.NavigateToVehicleDetail -> onNavigateToDetail(effect.vehicleId)
                is VehiclesContract.Effect.NavigateToAddVehicle -> onNavigateToAdd()
                is VehiclesContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Vehicles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(VehiclesContract.Intent.RefreshVehicles) }) {
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(VehiclesContract.Intent.AddVehicle) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = "Add Vehicle",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Search Bar - using reusable component
            FleetSearchField(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(VehiclesContract.Intent.SearchVehicles(it)) },
                placeholder = "Search vehicles...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Status Filter Chips
            StatusFilterChips(
                selectedStatus = state.selectedStatusFilter,
                onStatusSelected = { viewModel.sendIntent(VehiclesContract.Intent.FilterByStatus(it)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            when {
                state.isLoading -> {
                    // Using reusable LoadingContent component
                    LoadingContent(message = "Loading vehicles...")
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(VehiclesContract.Intent.LoadVehicles) }
                    )
                }
                state.filteredVehicles.isEmpty() -> {
                    // Using reusable EmptyContent component
                    EmptyContent(
                        icon = "🚗",
                        title = if (state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null)
                            "No vehicles match your filters"
                        else
                            "No vehicles found",
                        message = "Try adjusting your search or filters"
                    )
                }
                else -> {
                    VehicleList(
                        vehicles = state.filteredVehicles,
                        onVehicleClick = { viewModel.sendIntent(VehiclesContract.Intent.SelectVehicle(it)) },
                        onDeleteClick = { viewModel.sendIntent(VehiclesContract.Intent.DeleteVehicle(it)) }
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusFilterChips(
    selectedStatus: VehicleStatus?,
    onStatusSelected: (VehicleStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedStatus == null,
                onClick = { onStatusSelected(null) },
                label = { Text("All") }
            )
        }
        items(VehicleStatus.entries.toList()) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                label = { Text(status.name.replace("_", " ")) }
            )
        }
    }
}

@Composable
private fun VehicleList(
    vehicles: List<Vehicle>,
    onVehicleClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
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
                onDeleteClick = { onDeleteClick(vehicle.id) }
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getVehicleEmoji(vehicle.type),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicle.registrationNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${vehicle.make} ${vehicle.model} (${vehicle.year})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = vehicle.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleInfoItem(
                    emoji = "⛽",
                    label = "Fuel",
                    value = "${vehicle.fuelLevel}%",
                    modifier = Modifier.weight(1f)
                )
                VehicleInfoItem(
                    emoji = "📏",
                    label = "Mileage",
                    value = "${vehicle.mileage.toInt()} km",
                    modifier = Modifier.weight(1f)
                )
                VehicleInfoItem(
                    emoji = "👤",
                    label = "Driver",
                    value = vehicle.assignedDriverName ?: "N/A",
                    modifier = Modifier.weight(1f)
                )
            }

            vehicle.lastLocation?.let { location ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location.address ?: "Unknown location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleInfoItem(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBadge(status: VehicleStatus) {
    val (color, text) = when (status) {
        VehicleStatus.ACTIVE -> MaterialTheme.colorScheme.primary to "Active"
        VehicleStatus.INACTIVE -> MaterialTheme.colorScheme.secondary to "Inactive"
        VehicleStatus.IN_MAINTENANCE -> MaterialTheme.colorScheme.tertiary to "Maintenance"
        VehicleStatus.OUT_OF_SERVICE -> MaterialTheme.colorScheme.error to "Out of Service"
    }

    // Using reusable FleetStatusBadge component
    FleetStatusBadge(
        status = text,
        color = color
    )
}

private fun getVehicleEmoji(type: VehicleType): String {
    return when (type) {
        VehicleType.TRUCK -> "🚚"
        VehicleType.VAN -> "🚐"
        VehicleType.CAR -> "🚗"
        VehicleType.BUS -> "🚌"
        VehicleType.MOTORCYCLE -> "🏍️"
        VehicleType.TRAILER -> "🚛"
    }
}

