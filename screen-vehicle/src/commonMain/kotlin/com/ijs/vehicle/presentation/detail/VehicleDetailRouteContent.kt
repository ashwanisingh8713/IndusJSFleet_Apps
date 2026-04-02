package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.vehicle.domain.entity.RouteInfo
import com.ijs.vehicle.domain.entity.RouteStop
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RouteTabContent(
    routeInfo: RouteInfo?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    when {
        isLoading && routeInfo == null -> {
            LoadingContent(message = stringResource(Res.string.vehicle_route_loading))
        }
        error != null && routeInfo == null -> {
            ErrorContent(
                error = error,
                screenContext = FleetErrorContext.VEHICLE_DETAIL,
                onRetry = onRefresh
            )
        }
        else -> {
            val hasActiveTrip = routeInfo?.hasActiveTrip ?: false
            val stops = routeInfo?.stops ?: emptyList()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasActiveTrip && routeInfo != null) {
                    // Active Trip Info
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(Res.string.vehicle_route_active_trip_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = routeInfo.tripNumber ?: stringResource(
                                            Res.string.vehicle_route_trip_id,
                                            routeInfo.tripId.orEmpty()
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Route origin -> destination
                                if (routeInfo.origin != null || routeInfo.destination != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val originFallback = stringResource(Res.string.vehicle_route_origin)
                                    val destinationFallback = stringResource(Res.string.vehicle_route_destination)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📍", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${routeInfo.origin ?: originFallback} → ${routeInfo.destination ?: destinationFallback}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                // Driver info
                                routeInfo.driverName?.let { driverName ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "👤 $driverName",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress Bar
                                val progress = routeInfo.progress
                                if (progress != null) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.vehicle_route_progress),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${progress.percentage}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress.percentage / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.tertiary,
                                            trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.vehicle_route_eta),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = progress.eta ?: stringResource(Res.string.vehicle_route_na),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = stringResource(Res.string.vehicle_route_distance_left),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = stringResource(
                                                    Res.string.vehicle_route_km,
                                                    progress.distanceRemaining.toInt().toString()
                                                ),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stops Header
                    if (stops.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.vehicle_route_stops),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Stops List with Timeline
                        items(stops.size) { index ->
                            StopItemWithTimeline(
                                stop = stops[index],
                                isFirst = index == 0,
                                isLast = index == stops.size - 1
                            )
                        }
                    }
                } else {
                    // No Active Trip
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛣️", style = MaterialTheme.typography.displaySmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(Res.string.vehicle_route_no_trip),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(Res.string.vehicle_route_no_trip_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
internal fun StopItemWithTimeline(
    stop: RouteStop,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(
                            if (stop.status == "Completed") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Circle indicator
            val circleColor = when (stop.status) {
                "Completed" -> MaterialTheme.colorScheme.primary
                "Current" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(circleColor, CircleShape)
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (stop.status == "Completed") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Stop Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (stop.status == "Current")
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stop.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StopStatusBadge(status = stop.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stop.location,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                stop.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stop.scheduledTime ?: stop.actualTime ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun StopStatusBadge(status: String) {
    val done = stringResource(Res.string.vehicle_route_done)
    val now = stringResource(Res.string.vehicle_route_now)
    val pending = stringResource(Res.string.vehicle_route_pending)
    val (text, color) = when (status) {
        "Completed" -> "✓ $done" to MaterialTheme.colorScheme.primary
        "Current" -> "● $now" to MaterialTheme.colorScheme.tertiary
        else -> "○ $pending" to MaterialTheme.colorScheme.outline
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Documents Tab - Shows all vehicle documents
 */

