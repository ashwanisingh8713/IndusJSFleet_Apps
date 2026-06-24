package com.ijs.trip.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.FleetMetricTile
import com.ijs.trip.domain.entity.Trip
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Trip header card showing trip number, status, vehicle, driver, and quick stats.
 */
@Composable
internal fun TripHeader(
    trip: Trip,
    canViewTripPrice: Boolean = false,
    onStatusClick: () -> Unit,
    stateLabels: Map<String, String> = emptyMap()
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Trip Title
            Text(
                text = trip.tripNumber ?: stringResource(Res.string.payment_trip_id, trip.id),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_label_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EnhancedStatusBadge(
                    status = trip.status,
                    onClick = onStatusClick,
                    stateLabels = stateLabels
                )
            }

            // Row 2: Trip Price (for Owner/GM)
            if (canViewTripPrice) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.payment_add_trip_price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val price = trip.tripPrice
                    Text(
                        text = if (price != null && price > 0) {
                            formatCurrency(price)
                        } else {
                            stringResource(Res.string.trip_detail_price_not_set)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (price != null && price > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Row 3: Vehicle Number
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_label_vehicle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.vehicleNumber ?: stringResource(Res.string.not_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Row 4: Driver Name
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_label_driver),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.driverName ?: stringResource(Res.string.not_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Divider
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row: Distance | Duration | Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FleetMetricTile(
                    value = trip.displayInfo.distanceValue,
                    label = trip.displayInfo.distanceLabel,
                    showBackground = false,
                    centered = true
                )
                FleetMetricTile(
                    value = trip.displayInfo.durationValue,
                    label = trip.displayInfo.durationLabel,
                    showBackground = false,
                    centered = true
                )
                FleetMetricTile(
                    value = trip.priority?.replaceFirstChar { it.uppercaseChar() }
                        ?: stringResource(Res.string.trip_detail_priority_normal),
                    label = stringResource(Res.string.trip_detail_label_priority),
                    showBackground = false,
                    centered = true
                )
            }
        }
    }
}

/**
 * Route and schedule section showing departure/arrival locations and times.
 */
@Composable
internal fun RouteAndScheduleSection(trip: Trip) {
    EnhancedSectionCard(
        title = stringResource(Res.string.trip_detail_route_schedule),
        icon = "📍"
    ) {
        // Departure Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_detail_departure),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.startLocation?.address ?: stringResource(Res.string.trip_detail_not_specified),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }

            val departureDateTime = formatScheduleDateTime(
                dateTimeMs = trip.plannedStart,
                fallbackDateMs = trip.scheduledDate
            )
            if (departureDateTime.isNotBlank()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🗓️ $departureDateTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connecting dots
        Row(modifier = Modifier.padding(start = 12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Arrival Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "B",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_detail_arrival),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.endLocation?.address ?: stringResource(Res.string.trip_detail_not_specified),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }

            val arrivalDateTime = formatScheduleDateTime(
                dateTimeMs = trip.plannedEnd,
                fallbackDateMs = trip.deliveryDate
            )
            if (arrivalDateTime.isNotBlank()) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "🗓️ $arrivalDateTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


