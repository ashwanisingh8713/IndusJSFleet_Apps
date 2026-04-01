package com.ijs.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.PieChart
import com.indusjs.uicomponents.components.PieChartColors
import com.indusjs.uicomponents.components.PieChartData
import com.ijs.reports.domain.entity.ExpenseBreakdownItem
import com.ijs.reports.domain.entity.VehiclePerformer
import kotlin.math.roundToInt

// ═══════════════════════════════════════════════════════════════════
// Expense Breakdown
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun ExpenseBreakdownSection(breakdown: List<ExpenseBreakdownItem>) {
    var expanded by remember { mutableStateOf(false) }
    val totalExpenses = breakdown.sumOf { it.amount }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                "Total: ${formatCurrency(totalExpenses)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            PieChart(
                data = breakdown.map {
                    PieChartData(
                        formatCostType(it.type), it.amount,
                        PieChartColors.getCostTypeColor(it.type)
                    )
                },
                chartSize = 130.dp, strokeWidth = 18.dp, showLegend = false
            )
            Spacer(Modifier.height(12.dp))
            (if (expanded) breakdown else breakdown.take(4))
                .sortedByDescending { it.amount }.forEach { item ->
                    ExpenseItemRow(
                        formatCostType(item.type), item.amount,
                        item.percentage, PieChartColors.getCostTypeColor(item.type),
                        totalExpenses
                    )
                    Spacer(Modifier.height(6.dp))
                }
            if (breakdown.size > 4) {
                TextButton(
                    onClick = { expanded = !expanded },
                    Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (expanded) "Show Less ▲" else "Show All ${breakdown.size} ▼")
                }
            }
        }
    }
}

@Composable
private fun ExpenseItemRow(
    type: String, amount: Double, percentage: Double,
    color: Color, total: Double
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(
                type,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    Modifier.fillMaxWidth((amount / total).toFloat().coerceIn(0f, 1f))
                        .fillMaxHeight().background(color)
                )
            }
            Text(
                "${percentage.roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )
            Text(
                formatCurrency(amount),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Vehicle Insights
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun VehicleInsightsSection(
    topPerformer: VehiclePerformer?,
    lossMakingVehicles: List<VehiclePerformer>,
    onViewDetails: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        topPerformer?.let {
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReportsColors.ProfitGreen.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🏆", fontSize = 22.sp)
                        Column {
                            Text(
                                "Top Performer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                it.registrationNumber ?: "N/A",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        "+${formatCurrency(it.profit)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ReportsColors.ProfitGreen
                    )
                }
            }
        }
        if (lossMakingVehicles.isNotEmpty()) {
            Box(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ReportsColors.LossRed.copy(alpha = 0.06f))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠️", fontSize = 16.sp)
                        Text(
                            "${lossMakingVehicles.size} Vehicle(s) Need Attention",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ReportsColors.LossRed
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    lossMakingVehicles.take(3).forEach { vehicle ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                vehicle.registrationNumber ?: "Unknown",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "-${formatCurrency(vehicle.loss)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ReportsColors.LossRed
                            )
                        }
                    }
                    if (lossMakingVehicles.size > 3) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = onViewDetails,
                            Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("View All ${lossMakingVehicles.size} Vehicles →")
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DocumentCostsNotice() {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ℹ️", fontSize = 14.sp)
            Text(
                "Document costs (Insurance, Permits, PUC, etc.) are not yet included in P&L calculations.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Detailed Reports Grid
// ═══════════════════════════════════════════════════════════════════

@Composable
internal fun DetailedReportsSection(
    onVehiclePLClick: () -> Unit,
    onTripPLClick: () -> Unit,
    onConsolidatedClick: () -> Unit,
    onCombinedReportClick: () -> Unit,
    onMaintenanceCostClick: () -> Unit,
    onTripCostClick: () -> Unit,
    onDriverCostClick: () -> Unit,
    onCostAnalysisClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Profit & Loss",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportCard(Modifier.weight(1f), "🚛", "Vehicle P&L", "By vehicle", ReportsColors.InfoBlue, onVehiclePLClick)
            ReportCard(Modifier.weight(1f), "🛣️", "Trip P&L", "By trip", ReportsColors.ProfitGreen, onTripPLClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportCard(Modifier.weight(1f), "📈", "Fleet P&L", "Consolidated", ReportsColors.Purple, onConsolidatedClick)
            ReportCard(Modifier.weight(1f), "🔀", "Combined", "Vehicle + Trip", ReportsColors.Cyan, onCombinedReportClick)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Cost Analysis",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportCard(Modifier.weight(1f), "🔧", "Maintenance", "By type", ReportsColors.WarningAmber, onMaintenanceCostClick)
            ReportCard(Modifier.weight(1f), "⛽", "Trip Costs", "Fuel, Toll...", ReportsColors.Pink, onTripCostClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ReportCard(Modifier.weight(1f), "👨‍✈️", "Driver Costs", "Salary & more", ReportsColors.Teal, onDriverCostClick)
            ReportCard(Modifier.weight(1f), "💰", "All Costs", "Full breakdown", Color(0xFFF97316), onCostAnalysisClick)
        }
    }
}

@Composable
private fun ReportCard(
    modifier: Modifier, icon: String, title: String,
    description: String, color: Color, onClick: () -> Unit
) {
    Card(
        modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.06f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Text(icon, fontSize = 16.sp) }
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Utility functions
// ═══════════════════════════════════════════════════════════════════

internal fun formatCostType(costType: String) =
    costType.replace("_", " ").split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

