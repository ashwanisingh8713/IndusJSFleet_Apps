package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetTab
import com.indusjs.uicomponents.components.FleetTabBar
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Get date range string for the selected filter.
 */
private fun getDateRangeForFilter(filter: CostOverviewFilter): String {
    val currentTimeMs = com.indusjs.fleet.core.util.currentTimeMillis()
    val daysSinceEpoch = currentTimeMs / (24 * 60 * 60 * 1000L)
    val year = 1970 + (daysSinceEpoch / 365.25).toInt()
    val dayOfYear = ((daysSinceEpoch % 365.25)).toInt()

    val monthDays = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var month = 1
    var remainingDays = dayOfYear
    for (i in 0 until 12) {
        if (remainingDays < monthDays[i]) {
            month = i + 1
            break
        }
        remainingDays -= monthDays[i]
    }
    val day = (remainingDays + 1).coerceIn(1, 31)

    return when (filter) {
        CostOverviewFilter.TODAY -> {
            val dayStr = if (day < 10) "0$day" else "$day"
            val monthStr = if (month < 10) "0$month" else "$month"
            "$dayStr-$monthStr-$year"
        }
        CostOverviewFilter.WEEKLY -> {
            val startDay = (day - (daysSinceEpoch % 7).toInt()).coerceAtLeast(1)
            val endDay = (startDay + 6).coerceAtMost(31)
            val startDayStr = if (startDay < 10) "0$startDay" else "$startDay"
            val endDayStr = if (endDay < 10) "0$endDay" else "$endDay"
            val monthStr = if (month < 10) "0$month" else "$month"
            "$startDayStr-$monthStr to $endDayStr-$monthStr"
        }
        CostOverviewFilter.MONTHLY -> {
            val monthName = com.indusjs.datetimeutils.FleetDateTime.getMonthName(month)
            "$monthName $year"
        }
    }
}

/**
 * Cost Overview Section - Modern professional design with enhanced visuals.
 */
@Composable
internal fun CostOverviewSection(
    costOverview: CostOverview,
    selectedFilter: CostOverviewFilter,
    isLoading: Boolean,
    onFilterChange: (CostOverviewFilter) -> Unit,
    onAddTripCostClick: () -> Unit,
    onAddVehicleCostClick: () -> Unit,
    vehicleStatus: VehicleStatusSummary,
    tripSummary: TripSummary,
    onAddVehicleClick: () -> Unit,
    onCreateTripClick: () -> Unit
) {
    val hasNoFleet = vehicleStatus.total == 0 && tripSummary.total == 0
    val profitColor = FleetStatusColors.ProfitGreen
    val lossColor = FleetStatusColors.LossRed
    val expenseColor = FleetStatusColors.ExpenseAmber

    val dateRangeText = remember(selectedFilter) { getDateRangeForFilter(selectedFilter) }

    DashboardSectionCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Header (financial-themed accent: green on profit, primary otherwise)
            DashboardSectionHeader(
                title = stringResource(Res.string.dashboard_financial_overview),
                iconRes = Res.drawable.ic_cost,
                accent = if (costOverview.isProfit) profitColor else MaterialTheme.colorScheme.primary
            )

            // Period Filter Tabs with date range (hide if no fleet)
            if (!hasNoFleet) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                ) {
                    // Calm Fintech segmented pill (shared FleetTabBar) — replaces the old filled-indigo pills.
                    val periodTabs = listOf(
                        FleetTab(CostOverviewFilter.TODAY, stringResource(Res.string.dashboard_filter_today)),
                        FleetTab(CostOverviewFilter.WEEKLY, stringResource(Res.string.dashboard_filter_this_week)),
                        FleetTab(CostOverviewFilter.MONTHLY, stringResource(Res.string.dashboard_filter_this_month)),
                    )
                    FleetTabBar(
                        tabs = periodTabs,
                        selectedTabId = selectedFilter,
                        onTabSelected = onFilterChange,
                        modifier = Modifier.fillMaxWidth(),
                        scrollable = false,
                    )

                    // Date range indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_calendar),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Content with fixed minimum height to prevent fluctuation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(FleetTokens.IconSize.L),
                        strokeWidth = FleetTokens.Height.ProgressStroke,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (hasNoFleet) {
                    // Empty State - Getting Started
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(FleetTokens.IconSize.XL)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_trip),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(FleetTokens.IconSize.M)
                            )
                        }
                        Text(
                            text = stringResource(Res.string.dashboard_get_started),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.dashboard_get_started_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Financial Stats Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                    ) {
                        // Show all three figures so the relationship is explicit:
                        // Total Business (money in) − Expenses (money out) = Profit (kept).
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                        ) {
                            MetricTile(
                                modifier = Modifier.weight(1f),
                                label = stringResource(Res.string.reports_revenue),
                                value = formatCurrency(costOverview.totalRevenue),
                                accent = MaterialTheme.colorScheme.primary,
                                valueColor = MaterialTheme.colorScheme.primary
                            )
                            MetricTile(
                                modifier = Modifier.weight(1f),
                                label = stringResource(Res.string.dashboard_label_expenses),
                                value = formatCurrency(costOverview.totalExpenses),
                                accent = expenseColor,
                                valueColor = expenseColor
                            )
                            MetricTile(
                                modifier = Modifier.weight(1f),
                                label = if (costOverview.isProfit) stringResource(Res.string.reports_profit) else stringResource(Res.string.reports_loss),
                                value = formatCurrency(kotlin.math.abs(costOverview.profitLoss)),
                                accent = if (costOverview.isProfit) profitColor else lossColor,
                                valueColor = if (costOverview.isProfit) profitColor else lossColor
                            )
                        }

                        // Trips Count Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(FleetTokens.Radius.L))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.M),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_truck),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                                Text(
                                    text = stringResource(Res.string.dashboard_completed_trips),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = costOverview.completedTrips.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Getting Started Buttons (only for empty state)
            if (!isLoading && hasNoFleet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetButton(
                        text = stringResource(Res.string.action_add_vehicle),
                        onClick = onAddVehicleClick,
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(FleetTokens.IconSize.M)
                            )
                        }
                    )

                    FleetButton(
                        text = stringResource(Res.string.action_create_trip),
                        onClick = onCreateTripClick,
                        variant = ButtonVariant.SECONDARY,
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = null,
                                modifier = Modifier.size(FleetTokens.IconSize.M)
                            )
                        }
                    )
                }
            }
        }
    }
}
