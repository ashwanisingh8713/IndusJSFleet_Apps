package com.indusjs.fleet.presentation.trips.detail

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
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus

/**
 * Trip header card showing trip number, status, vehicle, driver, and quick stats.
 */
@Composable
internal fun TripHeader(
    trip: Trip,
    canViewTripPrice: Boolean = false,
    onStatusClick: () -> Unit
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
                text = trip.tripNumber ?: "Trip #${trip.id}",
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
                    text = "Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EnhancedStatusBadge(
                    status = trip.status,
                    onClick = onStatusClick
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
                        text = "Trip Price",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val price = trip.tripPrice
                    Text(
                        text = if (price != null && price > 0) {
                            formatCurrency(price)
                        } else {
                            "Not Set"
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
                    text = "Vehicle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.vehicleNumber ?: "Not Assigned",
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
                    text = "Driver",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.driverName ?: "Not Assigned",
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trip.displayInfo.distanceValue,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trip.displayInfo.distanceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trip.displayInfo.durationValue,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trip.displayInfo.durationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trip.priority?.replaceFirstChar { it.uppercaseChar() } ?: "Normal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
        title = "Route & Schedule",
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
                        text = "Departure",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.startLocation?.address ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }

            val departureDateTime = formatScheduleDateTime(
                isoDateTime = trip.plannedStart,
                date = trip.scheduledDate,
                time = trip.startTime
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
                        text = "Arrival",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.endLocation?.address ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }

            val arrivalDateTime = formatScheduleDateTime(
                isoDateTime = trip.plannedEnd,
                date = trip.deliveryDate,
                time = trip.deliveryTime
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

/**
 * Actual Times Section - Shows actual start/end times for in-progress/completed trips.
 */
@Composable
internal fun ActualTimesSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Actual Times",
        icon = "⏱️"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Actual Start Time
            trip.actualStartTime?.let { startTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "▶️", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Started",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatIsoDateTime(startTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Separator if both times exist
            if (trip.actualStartTime != null && trip.actualEndTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Actual End Time
            trip.actualEndTime?.let { endTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "⏹️", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatIsoDateTime(endTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Duration if available
            if (trip.displayInfo.durationValue != "NA" && trip.actualStartTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ Actual Duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = trip.displayInfo.durationValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cargo and customer information section.
 */
@Composable
internal fun CargoSection(trip: Trip, canViewTripPrice: Boolean = false) {
    val hasCargo = trip.cargoType != null || trip.cargoDescription != null ||
        trip.cargoLoadingWeight != null || trip.customerName != null ||
        (canViewTripPrice && trip.tripPrice != null)

    if (hasCargo) {
        EnhancedSectionCard(
            title = "Cargo & Customer",
            icon = "📦"
        ) {
            // Cargo Type with Weight in same row if both available
            if (trip.cargoType != null || trip.cargoLoadingWeight != null) {
                val cargoTypeValue = trip.cargoType?.replaceFirstChar { c -> c.uppercaseChar() } ?: ""
                val weightValue = trip.cargoLoadingWeight?.let { weight ->
                    val unit = trip.weightUnit ?: "KG"
                    val formattedWeight = if (weight == weight.toLong().toDouble()) {
                        weight.toLong().toString()
                    } else {
                        val rounded = (weight * 100).toLong() / 100.0
                        val parts = rounded.toString().split(".")
                        val intPart = parts[0]
                        val decPart = if (parts.size > 1) parts[1].take(2).padEnd(2, '0') else "00"
                        "$intPart.$decPart"
                    }
                    "$formattedWeight $unit"
                }

                if (cargoTypeValue.isNotBlank() && weightValue != null) {
                    EnhancedInfoRow(icon = "📋", label = "Cargo", value = "$cargoTypeValue • $weightValue")
                } else if (cargoTypeValue.isNotBlank()) {
                    EnhancedInfoRow(icon = "📋", label = "Cargo Type", value = cargoTypeValue)
                } else if (weightValue != null) {
                    EnhancedInfoRow(icon = "⚖️", label = "Weight", value = weightValue)
                }
            }

            trip.cargoDescription?.let {
                EnhancedInfoRow(icon = "📝", label = "Description", value = it)
            }
            trip.customerName?.let {
                EnhancedInfoRow(icon = "👤", label = "Customer", value = it)
            }
            trip.customerContact?.let { contact ->
                if (contact.isNotBlank()) {
                    ClickablePhoneRow(
                        phoneNumber = contact,
                        label = "Customer Contact",
                        icon = "📞"
                    )
                }
            }
            if (canViewTripPrice) {
                trip.tripPrice?.let {
                    EnhancedInfoRow(icon = "💰", label = "Trip Price", value = formatCurrency(it))
                }
            }
            trip.priority?.let {
                EnhancedInfoRow(
                    icon = "",
                    label = "Priority",
                    value = it.replaceFirstChar { c -> c.uppercaseChar() },
                    isLast = true
                )
            }
        }
    }
}

/**
 * Additional info section showing trip ID, notes, and creation date.
 */
@Composable
internal fun AdditionalInfoSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Additional Info",
        icon = "ℹ️"
    ) {
        EnhancedInfoRow(icon = "🆔", label = "Trip ID", value = "#${trip.id}")
        trip.notes?.let {
            EnhancedInfoRow(icon = "📝", label = "Notes", value = it)
        }
        trip.createdAt?.let {
            EnhancedInfoRow(icon = "📅", label = "Created on", value = formatDateToHumanReadable(it, shortMonth = true), isLast = true)
        }
    }
}

/**
 * Formats schedule date/time from ISO or separate date/time fields.
 * Output format: DD-MMM-YYYY hh:mm AM/PM
 */
internal fun formatScheduleDateTime(
    isoDateTime: String?,
    date: String?,
    time: String?
): String {
    if (!isoDateTime.isNullOrBlank()) {
        return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDateTime)
    }
    if (!date.isNullOrBlank() && !time.isNullOrBlank()) {
        return FleetDateTime.formatAnyToDisplayDateTime12Hour(date, time)
    }
    if (!date.isNullOrBlank()) {
        return FleetDateTime.formatAnyToDisplayDate(date)
    }
    return ""
}

/**
 * Formats ISO 8601 datetime string to human-readable format.
 */
internal fun formatIsoDateTime(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return ""
    return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDateTime)
}

