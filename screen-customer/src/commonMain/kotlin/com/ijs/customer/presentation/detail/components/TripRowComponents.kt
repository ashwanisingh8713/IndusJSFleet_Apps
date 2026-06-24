package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatDateToHumanReadable
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

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Row 1: Trip ID, State, Vehicle Reg
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
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

            Spacer(modifier = Modifier.height(4.dp))

            // Row 2: Route
            Text(
                text = trip.routeDisplay,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Start & End Dates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
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
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
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
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

