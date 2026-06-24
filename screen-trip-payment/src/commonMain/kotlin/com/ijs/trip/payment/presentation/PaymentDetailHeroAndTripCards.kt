package com.ijs.trip.payment.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.payment.domain.entity.PaymentStatus
import com.ijs.trip.payment.domain.entity.TripPayment
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Hero section showing payment amount, status, receipt, mode, date, and type.
 */
@Composable
internal fun HeroSection(
    payment: TripPayment,
    paymentStateLabels: Map<String, String> = emptyMap()
) {
    FleetSectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null,
        contentPadding = FleetTokens.Spacing.L
    ) {
            // Top row: Amount + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.amountDisplay,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (payment.hasTds || payment.hasDiscount) {
                        Text(
                            text = stringResource(Res.string.payment_net_prefix, payment.netAmountDisplay),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                DetailPaymentStatusBadge(status = payment.paymentStatus, paymentStateLabels = paymentStateLabels)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle row: Receipt + Date + Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Receipt number
                payment.receiptNumber?.let { receipt ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = receipt,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Payment Mode with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = payment.modeIcon,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = payment.paymentMode.localizedDisplayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Date
                payment.paymentDate?.let { date ->
                    Text(
                        text = formatDisplayDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            // Payment type
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${payment.paymentType.icon} ${payment.paymentType.localizedDisplayName()} " + stringResource(Res.string.payment_type_suffix_proper),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
    }
}

/**
 * Trip information card showing vehicle, driver, route, and trip price.
 */
@Composable
internal fun TripInfoCard(payment: TripPayment) {
    val tripInfo = payment.tripInfo ?: return
    val na = stringResource(Res.string.label_not_applicable)

    FleetTitledSectionCard(
        title = stringResource(Res.string.payment_section_trip_information)
    ) {
            // Vehicle & Driver in single row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tripInfo.vehicleRegistration?.let { vehicle ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.payment_label_vehicle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = vehicle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                tripInfo.driverName?.let { driver ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = stringResource(Res.string.payment_label_driver),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = driver,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Route - compact format
            if (tripInfo.startLocation != null || tripInfo.endLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tripInfo.startLocation?.take(20) ?: na,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = " → ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = tripInfo.endLocation?.take(20) ?: na,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Trip state (from backend trip_state, now carried on the payment detail response)
            tripInfo.tripState?.let { tripState ->
                if (tripState.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactDetailRow(
                        label = stringResource(Res.string.payment_filter_status),
                        value = tripState
                    )
                }
            }

            // Scheduled / start date & time (from backend trip_scheduled_date + trip_start_time)
            tripInfo.startDateTimeDisplay.takeIf { it != na }?.let { startDisplay ->
                CompactDetailRow(
                    label = stringResource(Res.string.date_range_start_date),
                    value = startDisplay
                )
            }

            // Delivery date & time (from backend trip_delivery_date + trip_delivery_time)
            tripInfo.endDateTimeDisplay.takeIf { it != na }?.let { endDisplay ->
                CompactDetailRow(
                    label = stringResource(Res.string.date_range_end_date),
                    value = endDisplay
                )
            }

            // Trip price
            tripInfo.tripPrice?.let { price ->
                if (price > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.payment_add_trip_price),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tripInfo.tripPriceDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
    }
}

/**
 * Payment details card showing amount breakdown, type, mode, transaction info.
 */
@Composable
internal fun PaymentDetailsCard(payment: TripPayment) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.payment_section_payment_details)
    ) {
            CompactDetailRow(label = stringResource(Res.string.label_amount), value = payment.amountDisplay)

            if (payment.hasTds) {
                CompactDetailRow(label = stringResource(Res.string.payment_label_tds_deducted), value = payment.tdsDisplay)
            }

            if (payment.hasDiscount) {
                CompactDetailRow(label = stringResource(Res.string.payment_label_discount), value = payment.discountDisplay)
            }

            CompactDetailRow(
                label = stringResource(Res.string.payment_label_net_amount),
                value = payment.netAmountDisplay,
                isHighlighted = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            CompactDetailRow(
                label = stringResource(Res.string.payment_detail_type),
                value = "${payment.paymentType.icon} ${payment.paymentType.localizedDisplayName()}"
            )
            CompactDetailRow(
                label = stringResource(Res.string.payment_detail_mode),
                value = "${payment.modeIcon} ${payment.paymentMode.localizedDisplayName()}"
            )

            payment.transactionId?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_detail_transaction_id), value = it)
            }

            payment.bankName?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_detail_bank), value = it)
            }
    }
}

/**
 * Status badge for payment detail screen.
 */
@Composable
internal fun DetailPaymentStatusBadge(
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
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = paymentStateLabels[status.apiValue] ?: status.localizedDisplayName(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

