package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.vehicle.domain.entity.TripsSummary
import com.ijs.vehicle.domain.entity.VehicleTripItem

@Composable
internal fun TripsTabContent(
    tripsList: List<VehicleTripItem>,
    tripsSummary: TripsSummary,
    isLoading: Boolean,
    error: String?,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    when {
        isLoading && tripsList.isEmpty() -> {
            LoadingContent(message = "Loading trips...")
        }
        error != null && tripsList.isEmpty() -> {
            ErrorContent(
                error = error,
                screenContext = FleetErrorContext.TRIPS,
                onRetry = onRefresh
            )
        }
        tripsList.isEmpty() -> {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚀", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No trips found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This vehicle hasn't completed any trips yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trip Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TripStatItem(
                                count = tripsSummary.total.toString(),
                                label = "Total",
                                color = MaterialTheme.colorScheme.primary
                            )
                            TripStatItem(
                                count = tripsSummary.inProgress.toString(),
                                label = "Active",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            TripStatItem(
                                count = tripsSummary.completed.toString(),
                                label = "Completed",
                                color = MaterialTheme.colorScheme.secondary
                            )
                            TripStatItem(
                                count = tripsSummary.planned.toString(),
                                label = "Planned",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Trip List Header
                item {
                    Text(
                        text = "Trips History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Trip Items
                items(tripsList.size) { index ->
                    TripItemCard(trip = tripsList[index])

                    // Load more when reaching end
                    if (index == tripsList.size - 1 && hasMore && !isLoading) {
                        LaunchedEffect(Unit) {
                            onLoadMore()
                        }
                    }
                }

                // Loading more indicator
                if (isLoading && tripsList.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
internal fun TripStatItem(count: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun TripItemCard(trip: VehicleTripItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.tripNumber ?: "Trip #${trip.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TripStatusChip(status = trip.stateLabel.ifEmpty { trip.state })
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.origin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " → ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.scheduledDate?.let { formatIsoDateToDisplay(it) } ?: trip.duration ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                trip.distance?.let { distance ->
                    Text(
                        text = "${distance.toInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Driver info if available
            trip.driverName?.let { driverName ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👤 $driverName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun TripStatusChip(status: String) {
    val (color, bgColor) = when (status) {
        "Completed" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        "In Progress" -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        "Planned" -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Route & Stops Tab - Shows active route with stops
 */

