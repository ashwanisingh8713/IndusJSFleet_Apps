package com.ijs.reports.presentation.vehicle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.VehiclePLSortOption
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun MultiVehicleSummaryCard(
    totalVehicles: Int,
    profitableCount: Int,
    lossMakingCount: Int,
    totalRevenue: Double,
    totalExpenses: Double,
    totalNetProfit: Double
) {
    val isOverallProfit = totalNetProfit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverallProfit)
                com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.08f)
            else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fleet Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isOverallProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.2f)
                    else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isOverallProfit) "NET PROFIT" else "NET LOSS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverallProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚛", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "$totalVehicles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "$profitableCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                    )
                    Text(
                        text = "Profitable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "$lossMakingCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                    Text(
                        text = "Loss Making",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isOverallProfit) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(totalNetProfit)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverallProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                }
            }
        }
    }
}

// ============================================================================
// Sorting Filter Section
// ============================================================================


@Composable
fun SortingFilterSection(
    sortOption: VehiclePLSortOption,
    plStatusFilter: PLStatusFilter,
    resultCount: Int,
    onSortChange: (VehiclePLSortOption) -> Unit,
    onFilterChange: (PLStatusFilter) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$resultCount vehicle(s)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = sortOption.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PLStatusFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = plStatusFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    }
}

// ============================================================================
// Vehicle PL Result Card
// ============================================================================


@Composable
internal fun VehiclePLResultCard(result: VehicleProfitLoss) {
    val isProfit = result.netProfit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🚛", style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            text = result.vehicleNumber ?: "Vehicle #${result.vehicleId}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${result.totalTrips} trips",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.15f)
                    else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = formatCurrency(kotlin.math.abs(result.netProfit)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalRevenue),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalExpenses),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPercentage(result.profitMargin),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                }
            }
        }
    }
}

