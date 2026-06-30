package com.ijs.trip.payment.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.payment.domain.entity.PaymentStatus
import com.ijs.trip.payment.domain.entity.PaymentType
import com.ijs.trip.payment.domain.entity.TripPayment
import com.ijs.trip.payment.domain.entity.TripPaymentFilter
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FilterChipRow(
    filter: TripPaymentFilter,
    onClear: () -> Unit,
    customerName: String? = null,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_filter),
            contentDescription = null,
            modifier = Modifier.size(FleetTokens.IconSize.S),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(Res.string.payment_filters_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = FleetTokens.Spacing.XS)
        )

        // Customer chip (resolved display name passed in from state)
        if (filter.customerId != null && !customerName.isNullOrBlank()) {
            FleetFilterChip(
                selected = true,
                onClick = { },
                label = customerName
            )
        }

        filter.paymentType?.let { type ->
            FleetFilterChip(
                selected = true,
                onClick = { },
                label = type.localizedDisplayName()
            )
        }

        filter.paymentStatus?.let { status ->
            FleetFilterChip(
                selected = true,
                onClick = { },
                label = paymentStateLabels[status.apiValue] ?: status.localizedDisplayName()
            )
        }

        // Show date range if applied
        if (filter.startDate != null || filter.endDate != null) {
            FleetFilterChip(
                selected = true,
                onClick = { },
                label = buildString {
                    filter.startDate?.let { append(it) }
                    append(" - ")
                    filter.endDate?.let { append(it) }
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
    FleetSectionCard(
        modifier = modifier.fillMaxWidth(),
        elevation = FleetTokens.Elevation.Raised
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
                    shape = RoundedCornerShape(FleetTokens.Radius.L),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(Res.string.payment_count, paymentCount),
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

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

            Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

            // Pending Card
            SummaryItemCard(
                value = totalPending,
                label = stringResource(Res.string.payment_filter_pending),
                valueColor = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending,
                backgroundColor = com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending.copy(alpha = 0.1f),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

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
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = stringResource(Res.string.payment_grouped_by_trip, tripCount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = FleetTokens.Spacing.S)
            )
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
            .clip(RoundedCornerShape(FleetTokens.Radius.M))
            .background(backgroundColor)
            .padding(vertical = FleetTokens.Spacing.M, horizontal = FleetTokens.Spacing.S)
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
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
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
    paymentStateLabels: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    FleetSectionCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick?.invoke() }
            ),
        elevation = FleetTokens.Elevation.Raised,
        contentPadding = FleetTokens.Spacing.M
    ) {
        // Row 1: Trip ID + Payment Type | Amount + Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
            ) {
                // Trip ID badge
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = stringResource(Res.string.payment_trip_id, payment.tripId),
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                // Payment Type badge
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = getPaymentTypeColor(payment.paymentType).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${payment.paymentType.icon} ${payment.paymentType.localizedDisplayName()}",
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS),
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
                PaymentStatusBadge(status = payment.paymentStatus, paymentStateLabels = paymentStateLabels)
            }
        }

        // Trip Info Section - grouped in a subtle background
        payment.tripInfo?.let { tripInfo ->
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(FleetTokens.Radius.M))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(FleetTokens.Spacing.S),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
            ) {
                // Vehicle + Route
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tripInfo.vehicleRegistration?.let { vehicle ->
                        Surface(
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = vehicle,
                                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.XS, vertical = FleetTokens.Spacing.XXS),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
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

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        // Row: Customer + Payment Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer name - prioritize customerName, fallback to customerCompany
            val noCustomerLabel = stringResource(Res.string.payment_no_customer)
            val resolvedCustomerName = payment.customerName?.takeIf { it.isNotBlank() }
                ?: payment.customerCompany?.takeIf { it.isNotBlank() }
            val customerDisplayName = resolvedCustomerName ?: noCustomerLabel
            Text(
                text = resolvedCustomerName?.let { "👤 $it" } ?: customerDisplayName,
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
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = stringResource(Res.string.payment_paid_date, formatPaymentDate(date)),
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
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
    paymentStateLabels: Map<String, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PaymentStatus.RECEIVED -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceivedBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentReceived
        PaymentStatus.PENDING -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPendingBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending
        PaymentStatus.CANCELLED -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentCancelledBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentCancelled
        PaymentStatus.PARTIAL -> com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPendingBg to com.indusjs.uicomponents.theme.FleetStatusColors.PaymentPending
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(FleetTokens.Radius.M),
        color = backgroundColor
    ) {
        Text(
            text = paymentStateLabels[status.apiValue] ?: status.localizedDisplayName(),
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}


/**
 * Format UTC epoch-millis to display format: "DD-MMM-YYYY hh:mm AM/PM".
 */
internal fun formatPaymentDate(timestampMillis: Long): String {
    return com.indusjs.fleet.core.util.formatDateTimeForDisplay(timestampMillis)
}

