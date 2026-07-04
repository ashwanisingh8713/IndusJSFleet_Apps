package com.ijs.trip.payment.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.payment.domain.entity.PaymentType
import com.ijs.trip.payment.domain.entity.TripPayment
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyFilteredContent(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XXL)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(FleetTokens.IconSize.XL),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            Text(
                text = stringResource(Res.string.payment_no_results),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = stringResource(Res.string.payment_no_results_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
            OutlinedButton(onClick = onClearFilters) {
                Text(stringResource(Res.string.payment_clear_filters))
            }
        }
    }
}

/**
 * Custom error content for Payments screen with better UX.
 */

@Composable
internal fun PaymentsErrorContent(
    error: String,
    onRetry: () -> Unit,
    onAddPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorContent(
        error = error,
        screenContext = FleetErrorContext.GENERIC,
        onRetry = onRetry,
        modifier = modifier,
        secondaryActionLabel = stringResource(Res.string.payment_add_new),
        onSecondaryAction = onAddPayment
    )
}


/**
 * Collapsible card showing a trip group with expandable payment list.
 * Animation duration: 300ms for smooth expand/collapse.
 */

@Composable
internal fun CollapsibleTripGroupCard(
    group: TripPaymentGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPaymentClick: (String) -> Unit,
    paymentStateLabels: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    FleetSectionCard(
        modifier = modifier.fillMaxWidth(),
        elevation = FleetTokens.Elevation.Raised,
        contentPadding = FleetTokens.Spacing.None
    ) {
        // Trip Header - Always visible, clickable to expand/collapse
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand),
            color = if (isExpanded)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FleetTokens.Spacing.M),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Trip info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        // Trip ID badge
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = stringResource(Res.string.payment_trip_id, group.tripId),
                                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Vehicle badge
                        group.tripInfo?.vehicleRegistration?.let { vehicle ->
                            Surface(
                                shape = RoundedCornerShape(FleetTokens.Radius.M),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = vehicle,
                                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XS, vertical = FleetTokens.Spacing.XS),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        // Payment count badge — §H: meta count, neutral chip (no colour accent).
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.L),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = stringResource(Res.string.payment_count_label, group.paymentCount, if (group.paymentCount > 1) "s" else ""),
                                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                    // Route
                    group.tripInfo?.let { tripInfo ->
                        val unknownLabel = stringResource(Res.string.payment_unknown)
                        Text(
                            text = "${tripInfo.startLocation ?: unknownLabel} → ${tripInfo.endLocation ?: unknownLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Customer
                    group.customerName?.let { customer ->
                        Text(
                            text = customer,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Right side: Total amount + expand icon. §H: money numerals are NEUTRAL (onSurface).
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${formatGroupAmount(group.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Arrow indicator using text
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = FleetTokens.Spacing.XS)
                    )
                }
            }
        }

        // Expanded content - Payment list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.S),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                group.payments.forEach { payment ->
                    CompactPaymentItem(
                        payment = payment,
                        onClick = { onPaymentClick(payment.id) },
                        paymentStateLabels = paymentStateLabels
                    )
                }
            }
        }
    }
}

/**
 * Compact payment item shown inside expanded trip group.
 */

@Composable
internal fun CompactPaymentItem(
    payment: TripPayment,
    onClick: () -> Unit,
    paymentStateLabels: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(FleetTokens.Radius.M),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FleetTokens.Spacing.S),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Payment type + date
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    // Payment Type badge — §H/D3: type is a CATEGORY, not a status; one
                    // neutral chip token (secondaryContainer), differentiated by TEXT only.
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.M),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = payment.paymentType.localizedDisplayName(),
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XS, vertical = FleetTokens.Spacing.XXS),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    // Payment mode
                    Text(
                        text = payment.paymentMode.localizedDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                // Payment date
                payment.paymentDate?.let { date ->
                    Text(
                        text = formatPaymentDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Amount + status. §H: neutral money numerals; status only via the badge chip.
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
 * Format large amounts for group display.
 */

// Mirrors PaymentsContract.formatAmount so the grouped cards and the summary cards
// render the SAME value identically (Cr / L abbreviations above a lakh; full Indian-comma
// rupees below it — no "K" abbreviation, which previously made one screen show both
// "₹55,000.00" and "₹55.0 K").
internal fun formatGroupAmount(amount: Double): String {
    return when {
        amount <= 0 -> "0"
        amount >= 10000000 -> "${formatDecimal(amount / 10000000, 2)}Cr"
        amount >= 100000 -> "${formatDecimal(amount / 100000, 2)}L"
        else -> {
            val intPart = amount.toLong()
            val decPart = ((amount - intPart) * 100).toLong()
            // §H quiet money: drop ".00" for whole values; show paise only when nonzero.
            if (decPart == 0L) formatIndianCommas(intPart)
            else "${formatIndianCommas(intPart)}.${decPart.toString().padStart(2, '0')}"
        }
    }
}

/** Indian digit grouping: last 3 digits, then groups of 2 (e.g. 5500000 -> 55,00,000). */
internal fun formatIndianCommas(value: Long): String {
    val s = value.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}

/**
 * Format decimal with specified precision (multiplatform compatible).
 */

internal fun formatDecimal(value: Double, decimals: Int): String {
    if (decimals == 0) {
        return kotlin.math.round(value).toLong().toString()
    }
    // Manual power calculation for multiplatform
    var factor = 1.0
    repeat(decimals) { factor *= 10.0 }
    val rounded = kotlin.math.round(value * factor) / factor
    // §H quiet money: a whole value drops the ".00" tail (1.00L → 1L); fractional keeps its decimals.
    if (rounded == rounded.toLong().toDouble()) {
        return rounded.toLong().toString()
    }
    val str = rounded.toString()
    val parts = str.split(".")
    return if (parts.size == 1) {
        str
    } else {
        val decPart = parts[1].take(decimals).padEnd(decimals, '0')
        "${parts[0]}.$decPart"
    }
}

