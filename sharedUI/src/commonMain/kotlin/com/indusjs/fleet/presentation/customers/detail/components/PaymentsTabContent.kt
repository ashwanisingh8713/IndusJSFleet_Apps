package com.indusjs.fleet.presentation.customers.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.customer.*
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.Intent
import com.indusjs.fleet.presentation.customers.detail.CustomerDetailContract.State

/**
 * Received Payments Tab Content.
 * Shows all payments received from customer with mode filter.
 * Only visible to Owner and General Manager.
 */
@Composable
fun PaymentsTabContent(
    state: State,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Filter Chips
        PaymentModeFilterChips(
            selectedMode = state.paymentModeFilter,
            onModeSelected = { onIntent(Intent.SetPaymentModeFilter(it)) }
        )

        // Payment Summary Card
        state.paymentSummary?.let { summary ->
            PaymentSummaryCard(summary)
        }

        when {
            state.isLoadingPayments && state.receivedPayments.isEmpty() -> {
                LoadingContent()
            }
            state.paymentsError != null && state.receivedPayments.isEmpty() -> {
                ErrorContent(
                    error = state.paymentsError,
                    onRetry = { onIntent(Intent.RefreshPayments) }
                )
            }
            state.receivedPayments.isEmpty() -> {
                EmptyContent(
                    title = "No payments recorded",
                    message = "No payments received from this customer yet",
                    icon = "💳"
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.receivedPayments, key = { it.id }) { payment ->
                        PaymentItem(payment = payment)
                    }

                    if (state.hasMorePayments) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.isLoadingPayments) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    TextButton(onClick = { onIntent(Intent.LoadMorePayments) }) {
                                        Text("Load More")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentModeFilterChips(
    selectedMode: PaymentMode?,
    onModeSelected: (PaymentMode?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All filter
        item {
            FilterChip(
                selected = selectedMode == null,
                onClick = { onModeSelected(null) },
                label = { Text("All") },
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Mode filters
        items(PaymentMode.entries) { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text("${mode.icon} ${mode.displayName}") },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
private fun PaymentSummaryCard(summary: CustomerPaymentSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💰 Total Received",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = summary.totalAmountDisplay,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (summary.byMode.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Payment Mode Breakdown
                Text(
                    text = "Payment Breakdown",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.byMode.forEach { modeData ->
                        PaymentModeRow(modeData)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentModeRow(data: PaymentByMode) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(data.modeIcon, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = data.modeDisplay,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = data.amountDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = data.percentageDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PaymentItem(
    payment: CustomerPayment,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mode Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = payment.modeIcon,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Column {
                    Text(
                        text = "${payment.modeDisplay} Payment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = payment.tripDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        payment.date?.let { date ->
                            Text(
                                text = "• $date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    payment.referenceNumber?.takeIf { it.isNotBlank() }?.let { ref ->
                        Text(
                            text = "Ref: $ref",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(
                text = payment.amountDisplay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
