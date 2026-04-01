package com.ijs.payment.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import com.indusjs.datetimeutils.FleetDateTime
import com.ijs.payment.domain.entity.PaymentStatus
import com.ijs.payment.domain.entity.PaymentType
import com.ijs.payment.domain.entity.TripPayment
import com.ijs.payment.domain.entity.TripPaymentFilter
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FilterChipRow(
    filter: TripPaymentFilter,
    onClear: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_filter),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(Res.string.payment_filters_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 4.dp)
        )

        filter.paymentType?.let { type ->
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }

        filter.paymentMode?.let { mode ->
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }

        filter.paymentStatus?.let { status ->
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(status.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }

        // Show date range if applied
        if (filter.startDate != null || filter.endDate != null) {
            FilterChip(
                selected = true,
                onClick = { },
                label = {
                    Text(
                        text = buildString {
                            filter.startDate?.let { append(it) }
                            append(" - ")
                            filter.endDate?.let { append(it) }
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onClear) {
            Text(stringResource(Res.string.action_clear), style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ==================== Inline Components ====================

/**
 * Summary card showing payment totals at the top of the list.
 * Clean UI with white background in day mode.
 */

@Composable
internal fun PaymentSummaryCard(
    totalReceived: String,
    totalPending: String,
    thisMonth: String,
    paymentCount: Int = 0,
    tripCount: Int = 0,
    isGroupedView: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with payment count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.payment_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (paymentCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(Res.string.payment_count, paymentCount),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Received Card
                SummaryItemCard(
                    value = totalReceived,
                    label = stringResource(Res.string.payment_filter_received),
                    valueColor = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived,
                    backgroundColor = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Pending Card
                SummaryItemCard(
                    value = totalPending,
                    label = stringResource(Res.string.payment_filter_pending),
                    valueColor = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending,
                    backgroundColor = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // This Month Card
                SummaryItemCard(
                    value = thisMonth,
                    label = stringResource(Res.string.payment_this_month),
                    valueColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
            }

            // Trip count section - only in grouped view
            if (isGroupedView) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.payment_grouped_by_trip, tripCount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Individual summary item with background (no icon).
 */

@Composable
internal fun SummaryItemCard(
    value: String,
    label: String,
    valueColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Card displaying a single payment item in the list.
 * Enhanced compact layout with Trip info grouped visually.
 */

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PaymentCard(
    payment: TripPayment,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Trip ID + Payment Type | Amount + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Trip ID badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = stringResource(Res.string.payment_trip_id, payment.tripId),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    // Payment Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getPaymentTypeColor(payment.paymentType).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${payment.paymentType.icon} ${payment.typeDisplay}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = getPaymentTypeColor(payment.paymentType)
                        )
                    }
                }

                // Amount + Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = payment.amountDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (payment.isReceived) com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
                        else if (payment.isPending) com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending
                        else MaterialTheme.colorScheme.onSurface
                    )
                    PaymentStatusBadge(status = payment.paymentStatus)
                }
            }

            // Trip Info Section - grouped in a subtle background
            payment.tripInfo?.let { tripInfo ->
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Vehicle + Route
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tripInfo.vehicleRegistration?.let { vehicle ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = vehicle,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        // Route with better visibility
                        val unknownLabel = stringResource(Res.string.payment_unknown)
                        Text(
                            text = "${tripInfo.startLocation ?: unknownLabel} → ${tripInfo.endLocation ?: unknownLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Trip Dates (only show if valid)
                    val hasValidDates = tripInfo.tripStartDate != null || tripInfo.tripEndDate != null
                    if (hasValidDates) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(Res.string.payment_depart, tripInfo.startDateTimeDisplay),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(Res.string.payment_arrive, tripInfo.endDateTimeDisplay),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Row: Customer + Payment Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer name - prioritize customerName, fallback to customerCompany
                val noCustomerLabel = stringResource(Res.string.payment_no_customer)
                val customerDisplayName = payment.customerName?.takeIf { it.isNotBlank() }
                    ?: payment.customerCompany?.takeIf { it.isNotBlank() }
                    ?: noCustomerLabel
                Text(
                    text = customerDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (customerDisplayName != noCustomerLabel)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Payment received date with label
                payment.paymentDate?.let { date ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = stringResource(Res.string.payment_paid_date, formatPaymentDate(date)),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get color for payment type badge.
 */

internal fun getPaymentTypeColor(type: PaymentType): Color {
    return when (type) {
        PaymentType.ADVANCE -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentAdvance
        PaymentType.PARTIAL -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPartial
        PaymentType.FINAL -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
        PaymentType.REFUND -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentRefund
    }
}


@Composable
internal fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PaymentStatus.RECEIVED -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceivedBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
        PaymentStatus.PENDING -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPendingBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending
        PaymentStatus.CANCELLED -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentCancelledBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentCancelled
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}


/**
 * Format ISO date to display format: "DD-MMM-YYYY hh:mm AM/PM"
 */

internal fun formatPaymentDate(isoDate: String): String {
    return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDate)
}

