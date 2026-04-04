package com.ijs.trip.payment.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.payment_no_results),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.payment_no_results_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
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
    @Suppress("UNUSED_PARAMETER") error: String,
    onRetry: () -> Unit,
    onAddPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_cost),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.payment_unable_to_load),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.payment_unable_to_load_message),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text(stringResource(Res.string.retry))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAddPayment) {
                Text(stringResource(Res.string.payment_add_new))
            }
        }
    }
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

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Trip info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Trip ID badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = stringResource(Res.string.payment_trip_id, group.tripId),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Vehicle badge
                            group.tripInfo?.vehicleRegistration?.let { vehicle ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = vehicle,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            // Payment count badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = stringResource(Res.string.payment_count_label, group.paymentCount, if (group.paymentCount > 1) "s" else ""),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

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

                    // Right side: Total amount + expand icon
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${formatGroupAmount(group.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
                        )

                        // Arrow indicator using text
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
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
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Payment type + date
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Payment Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getPaymentTypeColor(payment.paymentType).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${payment.paymentType.icon} ${payment.typeDisplay}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = getPaymentTypeColor(payment.paymentType)
                        )
                    }

                    // Payment mode
                    Text(
                        text = payment.modeIcon,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Payment date
                payment.paymentDate?.let { date ->
                    Text(
                        text = formatPaymentDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Amount + status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = payment.amountDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.isReceived) com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
                    else if (payment.isPending) com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending
                    else MaterialTheme.colorScheme.onSurface
                )
                PaymentStatusBadge(status = payment.paymentStatus, paymentStateLabels = paymentStateLabels)
            }
        }
    }
}

/**
 * Format large amounts for group display.
 */

internal fun formatGroupAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> {
            val cr = amount / 10000000
            "${formatDecimal(cr, 2)} Cr"
        }
        amount >= 100000 -> {
            val lakh = amount / 100000
            "${formatDecimal(lakh, 2)} L"
        }
        amount >= 1000 -> {
            val k = amount / 1000
            "${formatDecimal(k, 1)} K"
        }
        else -> formatDecimal(amount, 0)
    }
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
    val str = rounded.toString()
    val parts = str.split(".")
    return if (parts.size == 1) {
        str + "." + "0".repeat(decimals)
    } else {
        val decPart = parts[1].take(decimals).padEnd(decimals, '0')
        "${parts[0]}.$decPart"
    }
}

