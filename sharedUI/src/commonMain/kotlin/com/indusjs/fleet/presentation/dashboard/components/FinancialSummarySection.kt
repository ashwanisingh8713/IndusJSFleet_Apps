package com.indusjs.fleet.presentation.dashboard.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.data.model.dashboard.FinancialPeriod
import com.indusjs.fleet.domain.entity.dashboard.FinancialSummary

/**
 * Financial Summary Section for Dashboard - Shows key financial KPIs.
 * Only visible to Owner and General Manager.
 */
@Composable
fun FinancialSummarySection(
    financialSummary: FinancialSummary?,
    selectedPeriod: FinancialPeriod,
    isLoading: Boolean,
    error: String?,
    onPeriodChange: (FinancialPeriod) -> Unit,
    onViewReports: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "💰", style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            text = "Financial Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = financialSummary?.periodLabel ?: selectedPeriod.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Period Tabs
            PeriodTabs(
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange
            )

            // Error state
            if (error != null && financialSummary == null) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                // Financial KPIs
                financialSummary?.let { summary ->
                    FinancialKPICards(summary = summary)

                    // Pending Payments Banner (if any)
                    if (summary.pendingPayments > 0) {
                        PendingPaymentsBanner(amount = summary.pendingPayments)
                    }
                }
            }

            // View Reports Button
            TextButton(
                onClick = onViewReports,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("📊 View Detailed Reports")
            }
        }
    }
}

@Composable
private fun PeriodTabs(
    selectedPeriod: FinancialPeriod,
    onPeriodChange: (FinancialPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FinancialPeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodChange(period) },
                label = {
                    Text(
                        text = when (period) {
                            FinancialPeriod.TODAY -> "Today"
                            FinancialPeriod.WEEKLY -> "Week"
                            FinancialPeriod.MONTHLY -> "Month"
                            FinancialPeriod.YEARLY -> "Year"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun FinancialKPICards(summary: FinancialSummary) {
    val isProfit = summary.isProfit

    // Revenue and Expenses Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Revenue Card
        KPICard(
            modifier = Modifier.weight(1f),
            icon = "📈",
            label = "Revenue",
            value = formatCurrency(summary.totalRevenue),
            backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f),
            valueColor = Color(0xFF10B981)
        )

        // Expenses Card
        KPICard(
            modifier = Modifier.weight(1f),
            icon = "💸",
            label = "Expenses",
            value = formatCurrency(summary.totalExpenses),
            backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
            valueColor = Color(0xFFF59E0B)
        )
    }

    // Profit/Loss Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProfit) Color(0xFF10B981).copy(alpha = 0.15f)
                            else Color(0xFFEF4444).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isProfit) "✅" else "⚠️",
                    style = MaterialTheme.typography.titleMedium
                )
                Column {
                    Text(
                        text = if (isProfit) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(summary.netProfit)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            // Profit Margin Progress
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Margin",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { (summary.profitMargin.toFloat() / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(60.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${summary.profitMargin.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }
        }
    }

    // Additional KPIs Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MiniKPI(
            icon = "🚛",
            value = "${summary.completedTrips}",
            label = "Trips"
        )
        MiniKPI(
            icon = "📊",
            value = formatCurrency(summary.avgTripRevenue),
            label = "Avg Revenue"
        )
        MiniKPI(
            icon = "💵",
            value = formatCurrency(summary.avgTripProfit),
            label = "Avg Profit"
        )
    }
}

@Composable
private fun KPICard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = icon, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun MiniKPI(
    icon: String,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PendingPaymentsBanner(amount: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF59E0B).copy(alpha = 0.15f)
        )
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⏳", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Pending Payments",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF59E0B)
            )
        }
    }
}

