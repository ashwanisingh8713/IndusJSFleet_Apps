package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.data.model.costs.CostBreakdownItemDto
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlin.math.abs
import kotlin.math.round
import org.jetbrains.compose.resources.stringResource

// ============================================
// Cost Breakdown UI Components
// ============================================

/**
 * A card displaying a single cost breakdown item.
 * Shows cost label, amount, count, and percentage.
 */
@Composable
fun CostBreakdownItemCard(
    item: CostBreakdownItemDto,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    showPercentage: Boolean = true,
    showCount: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showCount && item.count > 0) {
                    Text(
                        text = "${item.count} entries",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$currencySymbol${formatAmount(item.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (showPercentage && item.percentage > 0) {
                    Text(
                        text = "${formatDecimal(item.percentage, 1)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * A section displaying multiple cost breakdown items grouped by category.
 */
@Composable
fun CostBreakdownSection(
    title: String,
    items: List<CostBreakdownItemDto>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    showTotal: Boolean = true,
    emptyMessage: String = ""
) {
    val resolvedEmptyMessage = emptyMessage.ifBlank { stringResource(Res.string.no_costs_recorded) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (showTotal && items.isNotEmpty()) {
                    val total = items.sumOf { it.amount }
                    Text(
                        text = "$currencySymbol${formatAmount(total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resolvedEmptyMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { item ->
                        CostBreakdownItemCard(item = item, currencySymbol = currencySymbol)
                    }
                }
            }
        }
    }
}

/**
 * A simple cost breakdown row for compact displays.
 */
@Composable
fun CostBreakdownRow(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    percentage: Double? = null,
    labelColor: Color = MaterialTheme.colorScheme.onSurface,
    amountColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = labelColor
            )
            percentage?.let {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "${formatDecimal(it, 1)}%",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text(
            text = "$currencySymbol${formatAmount(amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = amountColor
        )
    }
}

// ============================================
// Financial Summary Components
// ============================================

/**
 * A KPI card for displaying financial metrics.
 */
@Composable
fun FinancialKpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.let {
                    Text(text = it, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * A summary card showing revenue, expenses, and profit/loss.
 */
@Composable
fun FinancialSummaryCard(
    totalRevenue: Double,
    totalExpenses: Double,
    netProfit: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    profitMargin: Double? = null,
    title: String = ""
) {
    val resolvedTitle = title.ifBlank { stringResource(Res.string.financial_summary) }
    val isProfitable = netProfit >= 0
    val profitColor = if (isProfitable) {
        Color(0xFF2E7D32) // Green
    } else {
        Color(0xFFC62828) // Red
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = resolvedTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Revenue
            CostBreakdownRow(
                label = "💰 Total Revenue",
                amount = totalRevenue,
                currencySymbol = currencySymbol,
                amountColor = Color(0xFF2E7D32)
            )

            // Expenses
            CostBreakdownRow(
                label = "📉 Total Expenses",
                amount = totalExpenses,
                currencySymbol = currencySymbol,
                amountColor = Color(0xFFC62828)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Net Profit/Loss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isProfitable) "📈 Net Profit" else "📉 Net Loss",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    profitMargin?.let {
                        Text(
                            text = stringResource(Res.string.margin_percent, formatDecimal(it, 1)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "$currencySymbol${formatAmount(kotlin.math.abs(netProfit))}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = profitColor
                )
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isProfitable) {
                    Color(0xFF2E7D32).copy(alpha = 0.1f)
                } else {
                    Color(0xFFC62828).copy(alpha = 0.1f)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (isProfitable) "✅ Profitable" else "⚠️ Loss",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = profitColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * A compact profit/loss indicator badge.
 */
@Composable
fun ProfitLossBadge(
    amount: Double,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    showIcon: Boolean = true
) {
    val isProfitable = amount >= 0
    val backgroundColor = if (isProfitable) {
        Color(0xFF2E7D32).copy(alpha = 0.1f)
    } else {
        Color(0xFFC62828).copy(alpha = 0.1f)
    }
    val textColor = if (isProfitable) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (showIcon) {
                Text(
                    text = if (isProfitable) "📈" else "📉",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = "${if (!isProfitable) "-" else "+"}$currencySymbol${formatAmount(kotlin.math.abs(amount))}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

// ============================================
// Helper Functions
// ============================================

/**
 * Formats an amount with thousand separators.
 * Kotlin Multiplatform compatible.
 */
private fun formatAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> {
            // Show in Crores for amounts >= 1 Crore
            val value = amount / 10000000
            "${formatDecimal(value, 2)} Cr"
        }
        amount >= 100000 -> {
            // Show in Lakhs for amounts >= 1 Lakh
            val value = amount / 100000
            "${formatDecimal(value, 2)} L"
        }
        amount >= 1000 -> {
            // Show in Thousands for amounts >= 1000
            val value = amount / 1000
            "${formatDecimal(value, 2)} K"
        }
        else -> formatDecimal(amount, 2)
    }
}

/**
 * Formats a decimal value to a specific number of decimal places.
 * Kotlin Multiplatform compatible - doesn't use String.format.
 */
private fun formatDecimal(value: Double, decimalPlaces: Int): String {
    // Calculate factor manually for KMP compatibility
    var factor = 1.0
    repeat(decimalPlaces) { factor *= 10.0 }

    val rounded = round(value * factor) / factor
    val str = rounded.toString()

    // Ensure proper decimal places
    val dotIndex = str.indexOf('.')
    return if (dotIndex == -1) {
        // No decimal point, add one
        "$str.${"0".repeat(decimalPlaces)}"
    } else {
        val currentDecimals = str.length - dotIndex - 1
        if (currentDecimals < decimalPlaces) {
            str + "0".repeat(decimalPlaces - currentDecimals)
        } else if (currentDecimals > decimalPlaces) {
            str.substring(0, dotIndex + decimalPlaces + 1)
        } else {
            str
        }
    }
}
