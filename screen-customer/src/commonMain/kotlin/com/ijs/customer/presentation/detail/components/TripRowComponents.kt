package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.domain.entity.CustomerTrip
import com.ijs.customer.presentation.localizedStateDisplay
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Enhanced Trip Row with dates, vehicle, and payment info.
 */
@Composable
internal fun EnhancedTripRow(
    trip: CustomerTrip,
    onClick: () -> Unit,
    showFinancials: Boolean
) {
    // Calculate due amount
    val dueAmount = trip.pendingAmount ?: run {
        val price = trip.tripPrice ?: 0.0
        val paid = trip.paidAmount ?: 0.0
        (price - paid).coerceAtLeast(0.0)
    }
    val hasPending = dueAmount > 0
    val isPaid = trip.paymentStatus?.lowercase() == "paid"

    FleetSectionCard(
        onClick = onClick,
        contentPadding = FleetTokens.Spacing.M
    ) {
        Column {
            // Row 1: Trip ID, State, Vehicle Reg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(trip.stateIcon, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "#${trip.id}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TripStateChip(trip.localizedStateDisplay(), trip.state)
                }
                trip.vehicleRegistration?.let { reg ->
                    Text(
                        text = reg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

            // Row 2: Route
            Text(
                text = trip.routeDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            // Row 3: Start & End Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DateLabel(
                    label = stringResource(Res.string.customer_trip_label_start),
                    date = formatDateToHumanReadable(trip.plannedStart ?: trip.scheduledDate)
                )
                Text(
                    text = "\u2192",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DateLabel(
                    label = stringResource(Res.string.customer_trip_label_end),
                    date = formatDateToHumanReadable(trip.plannedEnd)
                )
            }

            // Row 4: Payment Info (if financials visible)
            if (showFinancials) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = FleetTokens.Spacing.XS),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Trip Price, Paid & Due Amount with equal spacing
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PaymentInfoItem(
                            label = stringResource(Res.string.customer_trip_price_label),
                            value = trip.tripPriceDisplay,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        PaymentInfoItem(
                            label = stringResource(Res.string.customer_trip_paid_label),
                            value = trip.paidAmountDisplay,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        PaymentInfoItem(
                            label = stringResource(Res.string.customer_trip_due_label),
                            value = formatCurrency(dueAmount),
                            color = if (hasPending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Right: Status Badge
                    when {
                        isPaid -> {
                            PaymentStatusBadge(
                                text = stringResource(Res.string.customer_trip_paid_badge),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        hasPending -> {
                            PaymentStatusBadge(
                                text = stringResource(Res.string.customer_trip_due_label),
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        else -> {
                            PaymentStatusBadge(
                                text = stringResource(Res.string.payment_status_pending),
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DateLabel(label: String, date: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun PaymentInfoItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
internal fun PaymentStatusBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(FleetTokens.Radius.M))
            .background(containerColor)
            .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
internal fun TripStateChip(label: String, state: String?) {
    val (bgColor, textColor) = when (state?.lowercase()) {
        "planned" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "in_progress" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "completed" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "cancelled" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(FleetTokens.Radius.L))
            .background(bgColor)
            .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

