package com.ijs.trip.presentation.detail

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

