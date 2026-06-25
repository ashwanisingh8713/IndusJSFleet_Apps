package com.ijs.trip.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.fleet.core.util.formatDateTimeForDisplay
import com.indusjs.uicomponents.components.PhoneChip
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.domain.entity.Trip
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Actual Times Section - Shows actual start/end times for in-progress/completed trips.
 */
@Composable
internal fun ActualTimesSection(trip: Trip) {
    EnhancedSectionCard(
        title = stringResource(Res.string.trip_detail_actual_times),
        icon = "⏱️"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Actual Start Time
            trip.actualStartTime?.takeIf { it > 0L }?.let { startTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(FleetTokens.IconSize.L),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "▶️", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                        Column {
                            Text(
                                text = stringResource(Res.string.trip_detail_started),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatEpochDateTime(startTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Separator if both times exist
            if ((trip.actualStartTime ?: 0L) > 0L && (trip.actualEndTime ?: 0L) > 0L) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            }

            // Actual End Time
            trip.actualEndTime?.takeIf { it > 0L }?.let { endTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(FleetTokens.IconSize.L),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "⏹️", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                        Column {
                            Text(
                                text = stringResource(Res.string.trip_state_completed),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatDateTimeForDisplay(endTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Duration if available
            if (trip.displayInfo.durationValue != "NA" && (trip.actualStartTime ?: 0L) > 0L) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ " + stringResource(Res.string.trip_detail_actual_duration),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.M),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = trip.displayInfo.durationValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS)
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
            title = stringResource(Res.string.trip_detail_cargo_customer),
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
                    EnhancedInfoRow(
                        icon = "📋",
                        label = stringResource(Res.string.trips_cargo),
                        value = "$cargoTypeValue • $weightValue"
                    )
                } else if (cargoTypeValue.isNotBlank()) {
                    EnhancedInfoRow(
                        icon = "📋",
                        label = stringResource(Res.string.trip_label_cargo_type),
                        value = cargoTypeValue
                    )
                } else if (weightValue != null) {
                    EnhancedInfoRow(
                        icon = "⚖️",
                        label = stringResource(Res.string.trip_detail_weight),
                        value = weightValue
                    )
                }
            }

            trip.cargoDescription?.let {
                EnhancedInfoRow(
                    icon = "📝",
                    label = stringResource(Res.string.label_description),
                    value = it
                )
            }
            trip.customerName?.let {
                EnhancedInfoRow(
                    icon = "👤",
                    label = stringResource(Res.string.trip_detail_customer),
                    value = it
                )
            }
            trip.customerContact?.let { contact ->
                if (contact.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = FleetTokens.Spacing.S),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(0.4f)
                        ) {
                            Text("📞", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                            Text(
                                text = stringResource(Res.string.trip_detail_contact),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PhoneChip(
                            phoneNumber = contact,
                            modifier = Modifier.weight(0.6f)
                        )
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        thickness = FleetTokens.Height.Divider
                    )
                }
            }
            if (canViewTripPrice) {
                trip.tripPrice?.let {
                    EnhancedInfoRow(
                        icon = "💰",
                        label = stringResource(Res.string.payment_add_trip_price),
                        value = formatCurrency(it)
                    )
                }
            }
            trip.priority?.let {
                EnhancedInfoRow(
                    icon = "",
                    label = stringResource(Res.string.trip_edit_label_priority),
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
        title = stringResource(Res.string.trip_detail_additional_info),
        icon = "ℹ️"
    ) {
        EnhancedInfoRow(
            icon = "🆔",
            label = stringResource(Res.string.trip_detail_trip_id_label),
            value = "#${trip.id}"
        )
        trip.notes?.let {
            EnhancedInfoRow(
                icon = "📝",
                label = stringResource(Res.string.trip_detail_notes),
                value = it
            )
        }
        trip.createdAt?.takeIf { it > 0L }?.let {
            EnhancedInfoRow(
                icon = "📅",
                label = stringResource(Res.string.trip_detail_created_on),
                value = formatDateToHumanReadable(it, shortMonth = true),
                isLast = true
            )
        }
    }
}

/**
 * Formats a schedule timestamp (UTC epoch-millis) to "DD-MMM-YYYY hh:mm AM/PM".
 * Prefers [dateTimeMs], falling back to [fallbackDateMs]. Treats null/0 as unset.
 */
internal fun formatScheduleDateTime(
    dateTimeMs: Long?,
    fallbackDateMs: Long? = null
): String {
    val ms = dateTimeMs?.takeIf { it > 0L } ?: fallbackDateMs?.takeIf { it > 0L } ?: return ""
    return formatDateTimeForDisplay(ms)
}

/**
 * Formats a timestamp (UTC epoch-millis) to "DD-MMM-YYYY hh:mm AM/PM".
 */
internal fun formatEpochDateTime(timestampMs: Long?): String {
    val ms = timestampMs?.takeIf { it > 0L } ?: return ""
    return formatDateTimeForDisplay(ms)
}

