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
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.fleet.core.util.formatDateTimeForDisplay
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.domain.entity.CustomerPayment
import com.ijs.customer.presentation.localizedModeDisplay
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Enhanced Payment Item with proper date format and compact layout.
 */
@Composable
fun EnhancedPaymentItem(
    payment: CustomerPayment,
    modifier: Modifier = Modifier
) {
    FleetSectionCard(
        modifier = modifier,
        contentPadding = FleetTokens.Spacing.M
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Row 1: Mode, Trip ID, Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                ) {
                    // Mode Icon Badge
                    Box(
                        modifier = Modifier
                            .size(FleetTokens.Height.StepCircle)
                            .clip(RoundedCornerShape(FleetTokens.Radius.M))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_cost),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.M),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = payment.localizedModeDisplay(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = payment.tripDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = payment.amountDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            // Row 2: Date/Time, Payment Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date with proper format: DD-MMM-YYYY hh:mm AM/PM
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.S),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = payment.date?.let { formatDateTimeForDisplay(it) } ?: "-",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Payment Type Badge
                PaymentTypeBadge(type = payment.paymentType.orEmpty().ifBlank { "payment" })
            }

            // Row 3: Receipt/Reference (if available)
            val hasReceipt = !payment.receiptNumber.isNullOrBlank()
            val hasReference = !payment.referenceNumber.isNullOrBlank()

            if (hasReceipt || hasReference) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    payment.receiptNumber?.takeIf { it.isNotBlank() }?.let { receipt ->
                        Text(
                            text = stringResource(Res.string.customer_payment_receipt_line, receipt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    payment.referenceNumber?.takeIf { it.isNotBlank() }?.let { ref ->
                        Text(
                            text = stringResource(Res.string.customer_payment_ref_line, ref),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Badge displaying payment type (advance, partial, final, refund).
 */
@Composable
internal fun PaymentTypeBadge(type: String) {
    val (bgColor, textColor, displayText) = when (type.lowercase()) {
        "advance" -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            stringResource(Res.string.customer_payment_type_advance)
        )
        "partial" -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            stringResource(Res.string.customer_payment_type_partial)
        )
        "final" -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            stringResource(Res.string.customer_payment_type_final)
        )
        "refund" -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            stringResource(Res.string.customer_payment_type_refund)
        )
        "payment" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            stringResource(Res.string.customer_payment_type_generic)
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            type.replaceFirstChar { it.uppercase() }
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(FleetTokens.Radius.S))
            .background(bgColor)
            .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

