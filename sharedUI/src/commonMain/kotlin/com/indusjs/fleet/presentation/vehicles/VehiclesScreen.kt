package com.indusjs.fleet.presentation.vehicles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import kotlinx.coroutines.flow.collectLatest

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
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(VehiclesContract.Intent.RefreshVehicles) }) {
                        Text("↻", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(VehiclesContract.Intent.AddVehicle) }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.sendIntent(VehiclesContract.Intent.SearchVehicles(it)) },
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
                        onRetry = { viewModel.sendIntent(VehiclesContract.Intent.LoadVehicles) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.filteredVehicles.isEmpty() -> {
                    EmptyContent(
                        message = if (state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null)
                            "No vehicles match your filters"
                        else
                            "No vehicles found",
                        modifier = Modifier.fillMaxSize()
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
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search vehicles...") },
        leadingIcon = { Text("🔍") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Text("✕")
                }
            }
        },
        singleLine = true
    )
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
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getVehicleEmoji(vehicle.type),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vehicle.registrationNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${vehicle.make} ${vehicle.model} (${vehicle.year})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = vehicle.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VehicleInfoItem(
                    emoji = "⛽",
                    label = "Fuel",
                    value = "${vehicle.fuelLevel}%"
                )
                VehicleInfoItem(
                    emoji = "📏",
                    label = "Mileage",
                    value = "${vehicle.mileage.toInt()} km"
                )
                VehicleInfoItem(
                    emoji = "👤",
                    label = "Driver",
                    value = vehicle.assignedDriverName ?: "Unassigned"
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
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
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

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
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

@Composable
private fun EmptyContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🚗",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
