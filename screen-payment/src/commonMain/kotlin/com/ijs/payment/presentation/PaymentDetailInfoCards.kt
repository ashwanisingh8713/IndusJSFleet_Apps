package com.ijs.payment.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.util.rememberPhoneDialer
import com.ijs.payment.domain.entity.TripPayment

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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Customer",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            payment.customerName?.let {
                CompactDetailRow(label = "Name", value = it)
            }

            payment.customerCompany?.let {
                // Only show if different from name
                if (it != payment.customerName) {
                    CompactDetailRow(label = "Company", value = it)
                }
            }

            // Contact with calling functionality
            payment.customerContact?.let { contact ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
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
                        text = "Contact",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = contact,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (phoneDialer != null) {
                            Text(
                                text = "📞",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            payment.customerGst?.let {
                CompactDetailRow(label = "GST", value = it)
            }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Financial Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                payment.financialYear?.let {
                    Text(
                        text = "FY: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                payment.financialMonth?.let {
                    Text(
                        text = "Month: $it",
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Additional Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            payment.notes?.let {
                CompactDetailRow(label = "Notes", value = it)
            }

            payment.receivedBy?.let {
                CompactDetailRow(label = "Received By", value = it)
            }

            payment.receivedAtLocation?.let {
                CompactDetailRow(label = "Location", value = it)
            }

            payment.createdByName?.let {
                CompactDetailRow(label = "Created By", value = it)
            }

            payment.createdAt?.let {
                CompactDetailRow(label = "Created", value = formatDisplayDate(it))
            }
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
            .padding(vertical = 3.dp),
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
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Format ISO date to display format: "DD-MMM-YYYY hh:mm AM/PM"
 */
internal fun formatDisplayDate(isoDate: String): String {
    return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDate)
}

