package com.ijs.trip.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.domain.entity.Trip
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
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
    FleetSectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null,
        elevation = FleetTokens.Elevation.None
    ) {
            // Trip Title
            Text(
                text = trip.tripNumber ?: stringResource(Res.string.payment_trip_id, trip.id),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Row 1: Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_label_status),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                EnhancedStatusBadge(
                    status = trip.status,
                    onClick = onStatusClick,
                    stateLabels = stateLabels
                )
            }

            // Row 2: Trip Price (for Owner/GM)
            if (canViewTripPrice) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.payment_add_trip_price),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
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
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        }
                    )
                }
            }

            // Row 3: Vehicle Number
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_label_vehicle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Text(
                    text = trip.vehicleNumber ?: stringResource(Res.string.not_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Row 4: Driver Name
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.trip_detail_label_driver),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Text(
                    text = trip.driverName ?: stringResource(Res.string.not_assigned),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Divider
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Stats Row: Distance | Duration | Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FleetMetricTile(
                    value = trip.displayInfo.distanceValue,
                    label = trip.displayInfo.distanceLabel,
                    valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    showBackground = false,
                    centered = true
                )
                FleetMetricTile(
                    value = trip.displayInfo.durationValue,
                    label = trip.displayInfo.durationLabel,
                    valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    showBackground = false,
                    centered = true
                )
                FleetMetricTile(
                    value = trip.priority?.replaceFirstChar { it.uppercaseChar() }
                        ?: stringResource(Res.string.trip_detail_priority_normal),
                    label = stringResource(Res.string.trip_detail_label_priority),
                    valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    showBackground = false,
                    centered = true
                )
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
        iconRes = Res.drawable.ic_map
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
                    modifier = Modifier.size(FleetTokens.IconSize.L),
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
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_detail_departure),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_calendar),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                        Text(
                            text = departureDateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        // Connecting dots
        Row(modifier = Modifier.padding(start = FleetTokens.Spacing.M)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(FleetTokens.Spacing.XS)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                }
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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
                    modifier = Modifier.size(FleetTokens.IconSize.L),
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
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_detail_arrival),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_calendar),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                        Text(
                            text = arrivalDateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}


