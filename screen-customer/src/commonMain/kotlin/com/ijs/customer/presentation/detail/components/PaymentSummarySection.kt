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
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.domain.entity.CustomerPaymentSummary
import com.ijs.customer.domain.entity.PaymentByMode
import com.ijs.customer.domain.entity.PaymentMode
import com.ijs.customer.presentation.localizedDisplayName
import com.ijs.customer.presentation.localizedModeDisplay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
            .padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            // All filter
            item {
                FleetFilterChip(
                    selected = selectedMode == null,
                    onClick = { onModeSelected(null) },
                    label = stringResource(Res.string.filter_all)
                )
            }

            // Mode filters
            items(PaymentMode.entries) { mode ->
                FleetFilterChip(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) },
                    label = mode.localizedDisplayName(),
                    leadingIcon = Res.drawable.ic_cost
                )
            }
        }

        // Search toggle
        IconButton(onClick = onSearchToggle) {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = stringResource(Res.string.customer_cd_search),
                modifier = Modifier.size(FleetTokens.IconSize.M),
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
    FleetSectionCard(
        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null,
        elevation = FleetTokens.Elevation.None
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            // Total Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.customer_total_received_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = summary.totalAmountDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(
                        onClick = onExportPdf,
                        enabled = !isExporting,
                        modifier = Modifier.size(FleetTokens.Height.StepCircle)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(FleetTokens.IconSize.S),
                                strokeWidth = FleetTokens.Height.ProgressStroke,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_download),
                                contentDescription = stringResource(Res.string.cd_export_pdf),
                                modifier = Modifier.size(FleetTokens.IconSize.M),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            if (summary.byMode.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                // Payment Mode Breakdown
                Text(
                    text = stringResource(Res.string.customer_payment_breakdown),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )

                Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
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
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_cost),
                contentDescription = null,
                modifier = Modifier.size(FleetTokens.IconSize.S),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = data.localizedModeDisplay(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            Text(
                text = data.amountDisplay,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(FleetTokens.Radius.M))
                    .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))
                    .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XXS)
            ) {
                Text(
                    text = data.percentageDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }
    }
}

