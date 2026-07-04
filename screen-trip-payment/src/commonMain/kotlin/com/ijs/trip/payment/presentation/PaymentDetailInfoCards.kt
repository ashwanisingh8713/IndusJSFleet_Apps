package com.ijs.trip.payment.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.fleet.core.util.rememberPhoneDialer
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.payment.domain.entity.TripPayment
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Customer details card with contact calling functionality.
 */
@Composable
internal fun CustomerDetailsCard(payment: TripPayment) {
    val hasCustomerInfo = !payment.customerName.isNullOrBlank() ||
            !payment.customerContact.isNullOrBlank() ||
            !payment.customerCompany.isNullOrBlank()

    if (!hasCustomerInfo) return

    // Phone dialer for customer contact
    val phoneDialer = payment.customerContact?.let { rememberPhoneDialer(it) }

    FleetTitledSectionCard(
        title = stringResource(Res.string.payment_section_customer),
        accent = MaterialTheme.colorScheme.primary
    ) {
            payment.customerName?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_detail_name), value = it)
            }

            payment.customerCompany?.let {
                // Only show if different from name
                if (it != payment.customerName) {
                    CompactDetailRow(label = stringResource(Res.string.payment_detail_company), value = it)
                }
            }

            // Contact with calling functionality
            payment.customerContact?.let { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = FleetTokens.Spacing.XS)
                        .then(
                            if (phoneDialer != null) {
                                Modifier.clickable { phoneDialer() }
                            } else {
                                Modifier
                            }
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.payment_section_contact),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
                    ) {
                        Text(
                            text = contact,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (phoneDialer != null) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_phone),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        }
                    }
                }
            }

            payment.customerGst?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_detail_gst), value = it)
            }
    }
}

/**
 * Financial information card showing fiscal year and month.
 */
@Composable
internal fun FinancialInfoCard(payment: TripPayment) {
    val hasFinancialInfo = !payment.financialYear.isNullOrBlank() ||
            !payment.financialMonth.isNullOrBlank()

    if (!hasFinancialInfo) return

    FleetSectionCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.payment_section_financial_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L),
                verticalAlignment = Alignment.CenterVertically
            ) {
                payment.financialYear?.let {
                    Text(
                        text = stringResource(Res.string.payment_label_fy, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                payment.financialMonth?.let {
                    Text(
                        text = stringResource(Res.string.payment_label_month, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Additional information card showing notes, received by, location, and audit info.
 */
@Composable
internal fun AdditionalInfoCard(payment: TripPayment) {
    val hasAdditionalInfo = !payment.notes.isNullOrBlank() ||
            !payment.receivedBy.isNullOrBlank() ||
            !payment.receivedAtLocation.isNullOrBlank() ||
            !payment.createdByName.isNullOrBlank()

    if (!hasAdditionalInfo) return

    FleetTitledSectionCard(
        title = stringResource(Res.string.payment_section_additional_info)
    ) {
            payment.notes?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_label_notes), value = it)
            }

            payment.receivedBy?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_label_received_by), value = it)
            }

            payment.receivedAtLocation?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_label_location), value = it)
            }

            payment.createdByName?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_detail_created_by), value = it)
            }

            payment.createdAt?.let {
                CompactDetailRow(label = stringResource(Res.string.payment_detail_created), value = formatDisplayDate(it))
            }
    }
}

/**
 * Compact label-value row used across payment detail cards.
 */
@Composable
internal fun CompactDetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FleetTokens.Spacing.XS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium,
            // §H: money numeral is neutral; emphasis comes from weight, not colour.
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Format ISO date to display format: "DD-MMM-YYYY hh:mm AM/PM"
 */
internal fun formatDisplayDate(timestampMillis: Long): String {
    return com.indusjs.fleet.core.util.formatDateTimeForDisplay(timestampMillis)
}

