package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.data.model.dashboard.CostOverviewFilter
import com.indusjs.fleet.domain.entity.dashboard.CostOverview
import com.indusjs.fleet.domain.entity.dashboard.TripSummary
import com.indusjs.fleet.domain.entity.dashboard.VehicleStatusSummary
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
    val profitColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
    val lossColor = com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
    val expenseColor = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber

    val dateRangeText = remember(selectedFilter) { getDateRangeForFilter(selectedFilter) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (costOverview.isProfit)
                                        listOf(com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen, com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreenDark)
                                    else
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.dashboard_financial_overview),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!hasNoFleet) {
                            Text(
                                text = when (selectedFilter) {
                                    CostOverviewFilter.TODAY -> stringResource(Res.string.dashboard_summary_today)
                                    CostOverviewFilter.WEEKLY -> stringResource(Res.string.dashboard_summary_weekly)
                                    CostOverviewFilter.MONTHLY -> stringResource(Res.string.dashboard_summary_monthly)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Period Filter Tabs with date range (hide if no fleet)
            if (!hasNoFleet) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CostOverviewFilter.entries.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val label = when (filter) {
                                CostOverviewFilter.TODAY -> stringResource(Res.string.dashboard_filter_today)
                                CostOverviewFilter.WEEKLY -> stringResource(Res.string.dashboard_filter_this_week)
                                CostOverviewFilter.MONTHLY -> stringResource(Res.string.dashboard_filter_this_month)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { onFilterChange(filter) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Date range indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
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
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (hasNoFleet) {
                    // Empty State - Getting Started
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🚀", style = MaterialTheme.typography.headlineMedium)
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FinancialStatCard(
                                modifier = Modifier.weight(1f),
                                icon = "💸",
                                label = stringResource(Res.string.dashboard_label_expenses),
                                value = formatCurrency(costOverview.totalExpenses),
                                backgroundColor = expenseColor.copy(alpha = 0.1f),
                                valueColor = expenseColor
                            )
                            FinancialStatCard(
                                modifier = Modifier.weight(1f),
                                icon = if (costOverview.isProfit) "📈" else "📉",
                                label = if (costOverview.isProfit) stringResource(Res.string.reports_profit) else stringResource(Res.string.reports_loss),
                                value = formatCurrency(kotlin.math.abs(costOverview.profitLoss)),
                                backgroundColor = if (costOverview.isProfit) profitColor.copy(alpha = 0.1f) else lossColor.copy(alpha = 0.1f),
                                valueColor = if (costOverview.isProfit) profitColor else lossColor
                            )
                        }

                        // Trips Count Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🚛", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(10.dp))
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddVehicleClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(Res.string.action_add_vehicle), fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onCreateTripClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(Res.string.action_create_trip), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Financial stat card with icon, label and value.
 */
@Composable
private fun FinancialStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleSmall)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
