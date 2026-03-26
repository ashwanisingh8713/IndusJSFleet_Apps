package com.ijs.customer.presentation.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.ijs.customer.domain.entity.CustomerPaymentSummary
import com.ijs.customer.domain.entity.PaymentByMode
import com.ijs.customer.domain.entity.PaymentMode
import org.jetbrains.compose.resources.painterResource
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * Payment mode filter chips row with search toggle.
 */
@Composable
internal fun PaymentModeFilterChips(
    selectedMode: PaymentMode?,
    onModeSelected: (PaymentMode?) -> Unit,
    isSearchActive: Boolean = false,
    onSearchToggle: () -> Unit = {},
    resultCount: Int = 0
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
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

        // Search toggle
        IconButton(onClick = onSearchToggle) {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = "Search",
                modifier = Modifier.size(20.dp),
                tint = if (isSearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Payment summary card showing total received and breakdown by mode.
 */
@Composable
internal fun PaymentSummaryCard(
    summary: CustomerPaymentSummary,
    onExportPdf: () -> Unit,
    isExporting: Boolean
) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.totalAmountDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onExportPdf,
                        enabled = !isExporting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("📄", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
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

