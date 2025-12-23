package com.indusjs.fleet.presentation.trips

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import kotlinx.coroutines.flow.collectLatest

/**
 * Trips List Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    viewModel: TripsViewModel,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TripsContract.Effect.NavigateToTripDetail -> onNavigateToDetail(effect.tripId)
                is TripsContract.Effect.NavigateToCreateTrip -> onNavigateToCreate()
                is TripsContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Trips") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(TripsContract.Intent.RefreshTrips) }) {
                        Text("↻", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(TripsContract.Intent.CreateTrip) }
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
                onQueryChange = { viewModel.sendIntent(TripsContract.Intent.SearchTrips(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Status Filter Chips
            StatusFilterChips(
                selectedStatus = state.selectedStatusFilter,
                onStatusSelected = { viewModel.sendIntent(TripsContract.Intent.FilterByStatus(it)) },
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
                        onRetry = { viewModel.sendIntent(TripsContract.Intent.LoadTrips) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                state.filteredTrips.isEmpty() -> {
                    EmptyContent(
                        message = if (state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null)
                            "No trips match your filters"
                        else
                            "No trips found",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    TripList(
                        trips = state.filteredTrips,
                        onTripClick = { viewModel.sendIntent(TripsContract.Intent.SelectTrip(it)) },
                        onCancelClick = { viewModel.sendIntent(TripsContract.Intent.CancelTrip(it)) }
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
        placeholder = { Text("Search trips...") },
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
    selectedStatus: TripStatus?,
    onStatusSelected: (TripStatus?) -> Unit,
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
        items(TripStatus.entries.toList()) { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onStatusSelected(if (selectedStatus == status) null else status) },
                label = { Text(getStatusDisplayName(status)) }
            )
        }
    }
}

private fun getStatusDisplayName(status: TripStatus): String {
    return when (status) {
        TripStatus.SCHEDULED -> "Scheduled"
        TripStatus.IN_PROGRESS -> "In Progress"
        TripStatus.COMPLETED -> "Completed"
        TripStatus.CANCELLED -> "Cancelled"
        TripStatus.DELAYED -> "Delayed"
    }
}

@Composable
private fun TripList(
    trips: List<Trip>,
    onTripClick: (String) -> Unit,
    onCancelClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(trips, key = { it.id }) { trip ->
            TripCard(
                trip = trip,
                onClick = { onTripClick(trip.id) },
                onCancelClick = { onCancelClick(trip.id) }
            )
        }
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = trip.tripNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${trip.vehicleNumber} • ${trip.driverName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Route Section
            RouteSection(trip = trip)

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TripInfoItem(
                    emoji = "🛣️",
                    label = "Distance",
                    value = "${trip.distance.toInt()} km"
                )
                TripInfoItem(
                    emoji = "⏱️",
                    label = "Duration",
                    value = formatDuration(trip.estimatedDuration)
                )
                trip.cargo?.let { cargo ->
                    TripInfoItem(
                        emoji = "📦",
                        label = "Cargo",
                        value = cargo
                    )
                } ?: TripInfoItem(
                    emoji = "📋",
                    label = "Status",
                    value = getStatusDisplayName(trip.status)
                )
            }

            // Cancel button for scheduled trips
            if (trip.status == TripStatus.SCHEDULED || trip.status == TripStatus.DELAYED) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("❌ Cancel Trip")
                }
            }
        }
    }
}

@Composable
private fun RouteSection(trip: Trip) {
    Column {
        // Start Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🟢",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.startLocation.address,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Current Location (if in progress)
        if (trip.status == TripStatus.IN_PROGRESS && trip.currentLocation != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📍",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.currentLocation.address,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // End Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🔴",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.endLocation.address,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TripInfoItem(
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
private fun StatusBadge(status: TripStatus) {
    val (color, text) = when (status) {
        TripStatus.SCHEDULED -> MaterialTheme.colorScheme.primary to "Scheduled"
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary to "In Progress"
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.secondary to "Completed"
        TripStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
        TripStatus.DELAYED -> MaterialTheme.colorScheme.error to "Delayed"
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

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
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
            text = "🛣️",
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
