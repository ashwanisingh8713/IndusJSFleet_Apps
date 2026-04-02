package com.ijs.trip.presentation

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FilterDefinition
import com.indusjs.uicomponents.components.FleetFilterBar
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    val pullRefreshState = rememberPullToRefreshState()
    val tripStatusFilters = TripStatus.entries.map { status ->
        val countForStatus = state.trips.count { it.status == status }
        FilterDefinition(
            id = status,
            label = TripStatus.getDisplayLabel(status),
            count = countForStatus.takeIf { it > 0 }
        )
    }

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
                title = { Text(stringResource(Res.string.trips_title)) },
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
                    // Refresh button
                    IconButton(onClick = { viewModel.sendIntent(TripsContract.Intent.RefreshTrips) }) {
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
            if (!state.isLoading && state.filteredTrips.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.sendIntent(TripsContract.Intent.CreateTrip) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.trips_add),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(TripsContract.Intent.RefreshTrips) },
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
                onQueryChange = { viewModel.sendIntent(TripsContract.Intent.SearchTrips(it)) },
                placeholder = stringResource(Res.string.trips_search_placeholder),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            FleetFilterBar(
                filters = tripStatusFilters,
                selectedFilterId = state.selectedStatusFilter,
                onFilterSelected = { id ->
                    when {
                        id == null -> viewModel.sendIntent(TripsContract.Intent.FilterByStatus(null))
                        id == state.selectedStatusFilter ->
                            viewModel.sendIntent(TripsContract.Intent.FilterByStatus(null))
                        else -> viewModel.sendIntent(TripsContract.Intent.FilterByStatus(id))
                    }
                },
                allLabel = stringResource(Res.string.all_filter),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            when {
                state.isLoading -> {
                    // Using reusable LoadingContent component
                    LoadingContent(message = stringResource(Res.string.loading))
                }
                state.error != null -> {
                    // Using reusable ErrorContent component
                    ErrorContent(
                        error = state.error!!,
                        screenContext = FleetErrorContext.TRIPS,
                        onRetry = { viewModel.sendIntent(TripsContract.Intent.LoadTrips) }
                    )
                }
                state.filteredTrips.isEmpty() -> {
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
                            iconRes = Res.drawable.ic_trip,
                            title = stringResource(Res.string.trips_empty_title),
                            message = stringResource(Res.string.trips_empty_message),
                            actionLabel = stringResource(Res.string.trips_add),
                            onAction = { viewModel.sendIntent(TripsContract.Intent.CreateTrip) }
                        )
                    }
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
}

private fun getStatusDisplayName(status: TripStatus): String {
    return TripStatus.getDisplayLabel(status)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(10.dp)
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
                            text = trip.tripNumber
                                ?: stringResource(Res.string.payment_trip_id, trip.id),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_car),
                                contentDescription = stringResource(Res.string.trip_list_cd_vehicle),
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trip.vehicleNumber
                                    ?: stringResource(Res.string.trip_list_vehicle_placeholder),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = " • ",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "👤",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = trip.driverName
                                    ?: stringResource(Res.string.trip_list_driver_placeholder),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                StatusBadge(status = trip.status)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Route Section
            RouteSection(trip = trip)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Info Row - State-based display using displayInfo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance - state-appropriate value (Est. Distance for Planned, Covered for In Progress, Total for Completed)
                TripInfoItem(
                    value = trip.displayInfo.distanceValue,
                    label = trip.displayInfo.distanceLabel,
                    isNA = trip.displayInfo.distanceValue == "NA"
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Duration - state-appropriate value (NA for Planned, Active for In Progress)
                TripInfoItem(
                    value = trip.displayInfo.durationValue,
                    label = trip.displayInfo.durationLabel,
                    isNA = trip.displayInfo.durationValue == "NA"
                )

                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Third item: Cost if available, otherwise Cargo Type
                if (trip.displayInfo.hasCosts) {
                    TripInfoItem(
                        value = trip.displayInfo.totalCostLabel,
                        label = stringResource(Res.string.trips_cost)
                    )
                } else {
                    val cargoLabel = trip.displayInfo.cargoTypeLabel
                    if (!cargoLabel.isNullOrBlank()) {
                        TripInfoItem(
                            value = cargoLabel.take(10),
                            label = stringResource(Res.string.trips_cargo)
                        )
                    } else {
                        // Fallback: Show estimated distance for Planned state
                        if (trip.status == TripStatus.PLANNED) {
                            TripInfoItem(
                                value = trip.displayInfo.estimatedDistance?.let { "${it.toInt()} km" }
                                    ?: stringResource(Res.string.vehicle_route_na),
                                label = stringResource(Res.string.trip_list_est_total),
                                isNA = trip.displayInfo.estimatedDistance == null
                            )
                        } else {
                            TripInfoItem(
                                value = getStatusDisplayName(trip.status).take(10),
                                label = stringResource(Res.string.trip_list_status)
                            )
                        }
                    }
                }
            }

            // Progress indicator for On Route trips
            val progressPct = trip.displayInfo.progressPercent
            if (trip.status == TripStatus.ON_ROUTE && progressPct != null) {
                Spacer(modifier = Modifier.height(10.dp))
                TripProgressIndicator(
                    progressPercent = progressPct,
                    remainingDistance = trip.displayInfo.remainingDistance
                )
            }
        }
    }
}

@Composable
private fun TripProgressIndicator(
    progressPercent: Int,
    remainingDistance: Double?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.trip_list_progress_percent, progressPercent),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (remainingDistance != null) {
                Text(
                    text = stringResource(
                        Res.string.trip_list_km_remaining,
                        remainingDistance.toInt()
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun RouteSection(trip: Trip) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Start Location
        Row(verticalAlignment = Alignment.Top) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🟢",
                    style = MaterialTheme.typography.labelMedium
                )
                // Vertical dotted line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.trip_list_from),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.startLocation?.address ?: stringResource(Res.string.vehicle_route_na),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Current Location (if on route)
        val currentLoc = trip.currentLocation
        if (trip.status == TripStatus.ON_ROUTE && currentLoc != null) {
            Row(verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📍",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(1.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_list_current),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = currentLoc.address,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        // End Location
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🔴",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.trip_list_to),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.endLocation?.address ?: stringResource(Res.string.vehicle_route_na),
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
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    isNA: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        if (icon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isNA) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                           else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isNA) FontWeight.Normal else FontWeight.Bold,
                    color = if (isNA) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                           else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isNA) FontWeight.Normal else FontWeight.SemiBold,
                color = if (isNA) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                       else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBadge(status: TripStatus) {
    val colorScheme = TripStatus.getColorScheme(status)
    val color = when (colorScheme) {
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.secondary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> MaterialTheme.colorScheme.tertiary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> MaterialTheme.colorScheme.primary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
    val text = TripStatus.getDisplayLabel(status)

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

private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        val k = amount / 1000
        if (k >= 100) {
            "${k.toInt()}K"
        } else {
            val formatted = ((k * 10).toInt() / 10.0)
            if (formatted == formatted.toInt().toDouble()) {
                "${formatted.toInt()}K"
            } else {
                "${formatted}K"
            }
        }
    } else {
        amount.toInt().toString()
    }
}
