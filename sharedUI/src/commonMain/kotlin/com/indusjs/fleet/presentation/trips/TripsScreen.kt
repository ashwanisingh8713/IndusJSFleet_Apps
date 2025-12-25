package com.indusjs.fleet.presentation.trips

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetSearchField
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Trips List Screen composable.
 * Uses reusable UI components from core/ui for consistent styling.
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
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(TripsContract.Intent.RefreshTrips) }) {
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
                onClick = { viewModel.sendIntent(TripsContract.Intent.CreateTrip) }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = "Create Trip",
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
                onQueryChange = { viewModel.sendIntent(TripsContract.Intent.SearchTrips(it)) },
                placeholder = "Search trips...",
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
                    // Using reusable LoadingContent component
                    LoadingContent(message = "Loading trips...")
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!,
                        onRetry = { viewModel.sendIntent(TripsContract.Intent.LoadTrips) }
                    )
                }
                state.filteredTrips.isEmpty() -> {
                    // Using reusable EmptyContent component
                    EmptyContent(
                        icon = "🚗",
                        title = if (state.searchQuery.isNotEmpty() || state.selectedStatusFilter != null)
                            "No trips match your filters"
                        else
                            "No trips found",
                        message = "Try adjusting your search or filters"
                    )
                }
                else -> {
                    TripList(
                        trips = state.filteredTrips,
                        onTripClick = { viewModel.sendIntent(TripsContract.Intent.SelectTrip(it)) }
                    )
                }
            }
        }
    }
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
        TripStatus.PLANNED -> "Planned"
        TripStatus.IN_PROGRESS -> "In Progress"
        TripStatus.COMPLETED -> "Completed"
        TripStatus.CANCELLED -> "Cancelled"
    }
}

@Composable
private fun TripList(
    trips: List<Trip>,
    onTripClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(trips, key = { it.id }) { trip ->
            TripCard(
                trip = trip,
                onClick = { onTripClick(trip.id) }
            )
        }
    }
}

@Composable
private fun TripCard(
    trip: Trip,
    onClick: () -> Unit
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
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🗺️",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = trip.tripNumber ?: "Trip #${trip.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${trip.vehicleNumber ?: "Vehicle"} • ${trip.driverName ?: "Driver"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Using reusable FleetStatusBadge component
                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(14.dp))

            // Route Section
            RouteSection(trip = trip)

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            // Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TripInfoItem(
                    emoji = "🛣️",
                    label = "Distance",
                    value = "${trip.distance.toInt()} km",
                    modifier = Modifier.weight(1f)
                )
                TripInfoItem(
                    emoji = "⏱️",
                    label = "Duration",
                    value = formatDuration(trip.estimatedDuration),
                    modifier = Modifier.weight(1f)
                )
                trip.cargoType?.let { cargo ->
                    TripInfoItem(
                        emoji = "📦",
                        label = "Cargo",
                        value = cargo,
                        modifier = Modifier.weight(1f)
                    )
                } ?: TripInfoItem(
                    emoji = "📋",
                    label = "Status",
                    value = getStatusDisplayName(trip.status),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RouteSection(trip: Trip) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        // Start Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🟢",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.startLocation?.address ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Current Location (if in progress)
        if (trip.status == TripStatus.IN_PROGRESS && trip.currentLocation != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📍",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Current",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.currentLocation.address,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // End Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🔴",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.endLocation?.address ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
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
private fun StatusBadge(status: TripStatus) {
    val (color, text) = when (status) {
        TripStatus.PLANNED -> MaterialTheme.colorScheme.primary to "Planned"
        TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary to "In Progress"
        TripStatus.COMPLETED -> MaterialTheme.colorScheme.secondary to "Completed"
        TripStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
    }

    // Using reusable FleetStatusBadge component
    FleetStatusBadge(
        status = text,
        color = color
    )
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}
