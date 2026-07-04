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
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.VehiclePLSortOption
import com.ijs.reports.presentation.localizedLabel
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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

    FleetSectionCard(
        // §H addendum-3: neutral surface — the Net Profit/Loss badge + loss-red numeral carry the sign.
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.reports_fleet_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = if (isOverallProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.2f)
                    else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isOverallProfit) stringResource(Res.string.reports_net_profit_badge) else stringResource(Res.string.reports_net_loss_badge),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverallProfit) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_truck),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$totalVehicles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(Res.string.reports_total),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check_circle),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M),
                        tint = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                    )
                    Text(
                        text = "$profitableCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                    )
                    Text(
                        text = stringResource(Res.string.reports_profitable),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_warning),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.M),
                        tint = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                    Text(
                        text = "$lossMakingCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                    Text(
                        text = stringResource(Res.string.reports_loss_making),
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
                        text = stringResource(Res.string.reports_total_revenue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.reports_total_expenses),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isOverallProfit) stringResource(Res.string.reports_net_profit_label) else stringResource(Res.string.reports_net_loss_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(totalNetProfit)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        // §H: profit numeral stays neutral; only a genuine loss may go LossRed.
                        color = if (isOverallProfit) MaterialTheme.colorScheme.onSurface else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
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
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        border = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.reports_vehicle_count, resultCount),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = sortOption.localizedLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
            ) {
                PLStatusFilter.entries.forEach { filter ->
                    FleetFilterChip(
                        selected = plStatusFilter == filter,
                        onClick = { onFilterChange(filter) },
                        label = filter.localizedLabel()
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
    val vehicleFallback = stringResource(Res.string.reports_vehicle_id_fallback, result.vehicleId)

    FleetSectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_truck),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.Default),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = result.vehicleNumber ?: vehicleFallback,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.reports_trips_count, result.totalTrips),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    // §H addendum-3: neutral pill; the numeral inside stays neutral (or loss-red on a loss).
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = formatCurrency(kotlin.math.abs(result.netProfit)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        // §H: profit numeral stays neutral; only a genuine loss may go LossRed.
                        color = if (isProfit) MaterialTheme.colorScheme.onSurface else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed,
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.reports_revenue),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalRevenue),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.reports_expenses),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(result.totalExpenses),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.reports_margin),
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
