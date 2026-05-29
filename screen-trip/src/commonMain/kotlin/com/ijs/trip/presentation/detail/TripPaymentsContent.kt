package com.ijs.trip.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.trip.payment.domain.entity.PaymentStatus
import com.ijs.trip.payment.domain.entity.TripPayment
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Trip Payments Section — displays payment history and summary for a trip.
 * Only visible to Owner and General Manager roles.
 *
 * @param payments List of all trip payments
 * @param totalPaid Total amount received
 * @param pendingAmount Remaining amount pending
 * @param tripPrice Expected trip price
 * @param isLoading Whether payments are being loaded
 * @param onAddPayment Callback to add a new payment
 * @param onPaymentClick Callback when a payment item is clicked
 */
@Composable
internal fun TripPaymentsSection(
    payments: List<TripPayment>,
    totalPaid: Double,
    pendingAmount: Double,
    tripPrice: Double?,
    isLoading: Boolean,
    onAddPayment: () -> Unit,
    onPaymentClick: (String) -> Unit,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    EnhancedSectionCard(
        title = stringResource(Res.string.payments_title),
        icon = "💳"
    ) {
        when {
            isLoading -> {
                PaymentsLoadingContent()
            }
            payments.isEmpty() -> {
                PaymentsEmptyContent(onAddPayment = onAddPayment)
            }
            else -> {
                // Payment Summary Header
                PaymentSummaryHeader(
                    tripPrice = tripPrice,
                    totalPaid = totalPaid,
                    pendingAmount = pendingAmount,
                    paymentCount = payments.size
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Add Payment button
                OutlinedButton(
                    onClick = onAddPayment,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(stringResource(Res.string.trip_payment_record_plus), fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment History Header
                Text(
                    text = stringResource(Res.string.trip_payment_history),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Payment items
                payments.forEachIndexed { index, payment ->
                    PaymentListItem(
                        payment = payment,
                        onClick = { onPaymentClick(payment.id) },
                        paymentStateLabels = paymentStateLabels
                    )
                    if (index < payments.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Payment summary header showing trip price, total paid, and pending amount.
 */
@Composable
private fun PaymentSummaryHeader(
    tripPrice: Double?,
    totalPaid: Double,
    pendingAmount: Double,
    paymentCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Trip Price row
            if (tripPrice != null && tripPrice > 0) {
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
                    Text(
                        text = formatCurrency(tripPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Total Paid row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✅", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.trip_payment_received_count, paymentCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatCurrency(totalPaid),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Pending row
            if (pendingAmount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⏳", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.payment_status_pending),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatCurrency(pendingAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Progress bar
            if (tripPrice != null && tripPrice > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val progress = (totalPaid / tripPrice).toFloat().coerceIn(0f, 1f)
                Column {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = if (progress >= 1f) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {}
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            Res.string.trip_payment_percent_collected,
                            (progress * 100).toInt()
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Individual payment list item.
 */
@Composable
private fun PaymentListItem(
    payment: TripPayment,
    onClick: () -> Unit,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: mode icon + details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Payment mode icon
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = payment.modeIcon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Payment type + mode
                    Text(
                        text = "${payment.typeDisplay} • ${payment.modeDisplay}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Received by person name
                    println("TRIP_PAYMENTS_CONTENT - id: ${payment.id}, receivedBy: '${payment.receivedBy}', createdByName: '${payment.createdByName}'")
                    val receiverName = payment.receivedBy?.takeIf { it.isNotBlank() && it != "Unknown" && it.lowercase() != "null" }
                        ?: payment.createdByName?.takeIf { it.isNotBlank() && it != "Unknown" && it.lowercase() != "null" }
                        ?: "Staff"
                    Text(
                        text = "Received by: $receiverName",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Date & Time on two lines
                    payment.paymentDate?.let { date ->
                        val formattedDate = try {
                            com.indusjs.datetimeutils.FleetDateTime.formatIsoToDisplayDateTime12Hour(date)
                        } catch (_: Exception) {
                            date
                        }
                        val parts = formattedDate.split(" ", limit = 2)
                        val dateString = parts.firstOrNull() ?: ""
                        val timeString = parts.getOrNull(1) ?: ""

                        if (dateString.isNotBlank()) {
                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (timeString.isNotBlank()) {
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Location info
                    if (!payment.receivedAtLocation.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📍 ${stringResource(Res.string.payment_label_location)}: ${payment.receivedAtLocation}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Notes info
                    if (!payment.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📝 ${stringResource(Res.string.payment_label_notes)}: ${payment.notes}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: amount + status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = payment.amountDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                PaymentStatusBadge(status = payment.paymentStatus, paymentStateLabels = paymentStateLabels)
            }
        }
    }
}

/**
 * Status badge for payment.
 */
@Composable
private fun PaymentStatusBadge(
    status: PaymentStatus,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    val (bgColor, textColor) = when (status) {
        PaymentStatus.RECEIVED -> Pair(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary
        )
        PaymentStatus.PENDING -> Pair(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.tertiary
        )
        PaymentStatus.CANCELLED -> Pair(
            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.error
        )
        PaymentStatus.PARTIAL -> Pair(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.secondary
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = "${status.icon} ${paymentStateLabels[status.apiValue] ?: status.displayName}",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

/**
 * Empty state for payments.
 */
@Composable
private fun PaymentsEmptyContent(onAddPayment: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "💳", style = MaterialTheme.typography.displaySmall)
        Text(
            text = stringResource(Res.string.trip_payments_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(Res.string.trip_payments_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAddPayment,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(Res.string.trip_payment_record_plus))
        }
    }
}

/**
 * Loading state for payments.
 */
@Composable
private fun PaymentsLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}
