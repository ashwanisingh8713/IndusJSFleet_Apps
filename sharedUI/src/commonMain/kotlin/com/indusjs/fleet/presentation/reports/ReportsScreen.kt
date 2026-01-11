package com.indusjs.fleet.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.DateRangePickerDialog
import com.indusjs.fleet.core.ui.FleetCard
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.ui.PieChart
import com.indusjs.fleet.core.ui.PieChartColors
import com.indusjs.fleet.core.ui.PieChartData
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.reports.PLSummary
import com.indusjs.fleet.presentation.reports.ReportsContract.Effect
import com.indusjs.fleet.presentation.reports.ReportsContract.Intent
import com.indusjs.fleet.presentation.reports.ReportsContract.State
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Reports Hub Screen - Entry point for all P&L reports
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVehiclePL: () -> Unit,
    onNavigateToTripPL: () -> Unit,
    onNavigateToCostAnalysis: () -> Unit,
    onNavigateToConsolidatedPL: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> { /* Handle snackbar */ }
                is Effect.NavigateToVehiclePL -> onNavigateToVehiclePL()
                is Effect.NavigateToTripPL -> onNavigateToTripPL()
                is Effect.NavigateToCostAnalysis -> onNavigateToCostAnalysis()
                is Effect.NavigateToConsolidatedPL -> onNavigateToConsolidatedPL()
                is Effect.ShowExportSuccess -> { /* Handled in dialog */ }
                is Effect.ShowExportError -> { /* Handle error */ }
            }
        }
    }

    // Date Range Picker Dialog
    DateRangePickerDialog(
        isVisible = state.showDateRangePicker,
        startDate = state.startDate,
        endDate = state.endDate,
        onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
        onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) },
        onApply = { start, end -> viewModel.sendIntent(Intent.SetCustomDateRange(start, end)) },
        onDismiss = { viewModel.sendIntent(Intent.HideDateRangePicker) }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.hasSummary) {
                FloatingActionButton(
                    onClick = { viewModel.sendIntent(Intent.ExportToPdf) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (state.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading && !state.hasSummary -> LoadingContent()
            else -> ReportsContent(
                state = state,
                onPeriodSelect = { viewModel.sendIntent(Intent.SelectPeriod(it)) },
                onVehiclePLClick = { viewModel.sendIntent(Intent.NavigateToVehiclePL) },
                onTripPLClick = { viewModel.sendIntent(Intent.NavigateToTripPL) },
                onCostAnalysisClick = { viewModel.sendIntent(Intent.NavigateToCostAnalysis) },
                onConsolidatedClick = { viewModel.sendIntent(Intent.NavigateToConsolidatedPL) },
                onRetry = { viewModel.sendIntent(Intent.Refresh) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ReportsContent(
    state: State,
    onPeriodSelect: (ReportPeriod) -> Unit,
    onVehiclePLClick: () -> Unit,
    onTripPLClick: () -> Unit,
    onCostAnalysisClick: () -> Unit,
    onConsolidatedClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Period Filter Chips
        item {
            PeriodFilterSection(
                selectedPeriod = state.selectedPeriod,
                onPeriodSelect = onPeriodSelect
            )
        }

        // Error Banner (if summary failed to load)
        state.error?.let { error ->
            item {
                ErrorBanner(error = error, onRetry = onRetry)
            }
        }

        // Loading indicator for refresh
        if (state.isLoading && state.hasSummary) {
            item {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // P&L Summary Cards
        state.summary?.let { summary ->
            item {
                PLSummaryCards(summary = summary)
            }

            // Quick Insights Section
            item {
                QuickInsightsSection(summary = summary)
            }

            // Profit Margin Gauge
            item {
                ProfitMarginCard(summary = summary)
            }

            // Loss Alerts Section (if any loss-making vehicles)
            if (summary.lossMakingVehiclesList.isNotEmpty() || summary.lossMakingTrips > 0) {
                item {
                    LossAlertsSection(
                        lossMakingVehicles = summary.lossMakingVehiclesList,
                        lossMakingTripsCount = summary.lossMakingTrips,
                        onViewDetails = onVehiclePLClick
                    )
                }
            }

            // Expense Breakdown Pie Chart
            if (state.hasExpenseData) {
                item {
                    ExpenseBreakdownCard(
                        breakdown = state.expenseBreakdown.map { item ->
                            PieChartData(
                                label = formatCostType(item.costType),
                                value = item.amount,
                                color = PieChartColors.getCostTypeColor(item.costType)
                            )
                        }
                    )
                }
            }

            // Fleet Summary
            item {
                FleetSummaryCard(summary = summary)
            }

            // Trip Summary
            item {
                TripSummaryCard(summary = summary)
            }

            // Top Performers
            summary.topPerformingVehicle?.let { vehicle ->
                item {
                    TopPerformerCard(
                        title = "Top Performing Vehicle",
                        registrationNumber = vehicle.registrationNumber ?: "N/A",
                        profit = vehicle.profit
                    )
                }
            }
        }

        // Report Types Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detailed Reports",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            ReportTypesGrid(
                onVehiclePLClick = onVehiclePLClick,
                onTripPLClick = onTripPLClick,
                onCostAnalysisClick = onCostAnalysisClick,
                onConsolidatedClick = onConsolidatedClick
            )
        }
    }
}

@Composable
private fun PeriodFilterSection(
    selectedPeriod: ReportPeriod,
    onPeriodSelect: (ReportPeriod) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(ReportPeriod.entries.toList()) { period ->
            val isSelected = selectedPeriod == period
            FilterChip(
                selected = isSelected,
                onClick = { onPeriodSelect(period) },
                label = {
                    Text(
                        text = period.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

@Composable
private fun ErrorBanner(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
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
                    text = "Something went wrong",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = onRetry) {
                Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun PLSummaryCards(summary: PLSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Revenue Card
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            icon = "📈",
            label = "Revenue",
            value = formatCurrency(summary.totalRevenue),
            backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f),
            valueColor = Color(0xFF10B981)
        )

        // Expenses Card
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            icon = "💸",
            label = "Expenses",
            value = formatCurrency(summary.totalExpenses),
            backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
            valueColor = Color(0xFFF59E0B)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Profit/Loss Card (full width)
    val isProfit = summary.isProfitable
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProfit) Color(0xFF10B981).copy(alpha = 0.1f)
                            else Color(0xFFEF4444).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isProfit) "✅" else "⚠️",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isProfit) "Net Profit" else "Net Loss",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(summary.grossProfit)),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }
            }

            // Margin badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isProfit) Color(0xFF10B981).copy(alpha = 0.2f)
                        else Color(0xFFEF4444).copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${summary.profitMarginPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    backgroundColor: Color,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun ProfitMarginCard(summary: PLSummary) {
    FleetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profit Margin",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress bar
                Box(modifier = Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { (summary.profitMarginPercentage.toFloat() / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = if (summary.isProfitable) Color(0xFF10B981) else Color(0xFFEF4444),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${summary.profitMarginPercentage.toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.isProfitable) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownCard(breakdown: List<PieChartData>) {
    FleetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Expense Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            PieChart(
                data = breakdown,
                chartSize = 180.dp,
                strokeWidth = 28.dp,
                showLegend = true
            )
        }
    }
}

@Composable
private fun FleetSummaryCard(summary: PLSummary) {
    FleetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fleet Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(label = "Total", value = "${summary.totalVehicles}", color = MaterialTheme.colorScheme.primary)
                StatColumn(label = "Active", value = "${summary.activeVehicles}", color = Color(0xFF10B981))
                StatColumn(label = "Profitable", value = "${summary.profitableVehicles}", color = Color(0xFF10B981))
                StatColumn(label = "Loss", value = "${summary.lossMakingVehicles}", color = Color(0xFFEF4444))
            }

            // Visual bar
            if (summary.totalVehicles > 0) {
                val profitableRatio = summary.profitableVehicles.toFloat() / summary.totalVehicles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(profitableRatio.coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFF10B981))
                    )
                    Box(
                        modifier = Modifier
                            .weight((1f - profitableRatio).coerceAtLeast(0.01f))
                            .fillMaxHeight()
                            .background(Color(0xFFEF4444))
                    )
                }
            }
        }
    }
}

@Composable
private fun TripSummaryCard(summary: PLSummary) {
    FleetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Trip Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(label = "Total", value = "${summary.totalTrips}", color = MaterialTheme.colorScheme.primary)
                StatColumn(label = "Completed", value = "${summary.completedTrips}", color = Color(0xFF10B981))
                StatColumn(label = "Profitable", value = "${summary.profitableTrips}", color = Color(0xFF10B981))
                StatColumn(label = "Loss", value = "${summary.lossMakingTrips}", color = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TopPerformerCard(title: String, registrationNumber: String, profit: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🏆", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = registrationNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Profit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(profit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun LossMakingVehiclesCard(vehicles: List<com.indusjs.fleet.domain.entity.reports.VehiclePerformer>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚠️", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Loss Making Vehicles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFEF4444)
                )
            }

            vehicles.take(3).forEach { vehicle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = vehicle.registrationNumber ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "-${formatCurrency(vehicle.loss)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

/**
 * Quick Insights Section - Shows top performer, worst performer, and highest cost category
 */
@Composable
private fun QuickInsightsSection(summary: PLSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💡", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Quick Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Performer
                InsightCard(
                    modifier = Modifier.weight(1f),
                    icon = "🏆",
                    title = "Top Performer",
                    value = summary.topPerformingVehicle?.registrationNumber ?: "N/A",
                    subtitle = if (summary.topPerformingVehicle != null)
                        "+${formatCurrency(summary.topPerformingVehicle.profit)}" else "No data",
                    subtitleColor = Color(0xFF10B981),
                    backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )

                // Needs Attention
                val worstVehicle = summary.lossMakingVehiclesList.firstOrNull()
                InsightCard(
                    modifier = Modifier.weight(1f),
                    icon = "⚠️",
                    title = "Needs Attention",
                    value = worstVehicle?.registrationNumber ?: "All Good!",
                    subtitle = if (worstVehicle != null)
                        "-${formatCurrency(worstVehicle.loss)}" else "No losses",
                    subtitleColor = if (worstVehicle != null) Color(0xFFEF4444) else Color(0xFF10B981),
                    backgroundColor = if (worstVehicle != null)
                        Color(0xFFEF4444).copy(alpha = 0.1f) else Color(0xFF10B981).copy(alpha = 0.1f)
                )
            }

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    icon = "🚛",
                    value = "${summary.profitableVehicles}/${summary.totalVehicles}",
                    label = "Profitable Vehicles"
                )
                QuickStatItem(
                    icon = "🛣️",
                    value = "${summary.profitableTrips}/${summary.completedTrips}",
                    label = "Profitable Trips"
                )
                QuickStatItem(
                    icon = "📈",
                    value = "${summary.profitMarginPercentage.toInt()}%",
                    label = "Margin"
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color,
    backgroundColor: Color
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
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
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

/**
 * Loss Alerts Section - Shows critical alerts for loss-making items
 */
@Composable
private fun LossAlertsSection(
    lossMakingVehicles: List<com.indusjs.fleet.domain.entity.reports.VehiclePerformer>,
    lossMakingTripsCount: Int,
    onViewDetails: () -> Unit
) {
    val totalAlerts = lossMakingVehicles.size + (if (lossMakingTripsCount > 0) 1 else 0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEF4444).copy(alpha = 0.08f)
        )
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🚨", style = MaterialTheme.typography.titleMedium)
                    Column {
                        Text(
                            text = "Loss Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = "$totalAlerts item(s) need attention",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Vehicle Loss Alerts
            lossMakingVehicles.take(3).forEach { vehicle ->
                LossAlertItem(
                    icon = "🚛",
                    title = vehicle.registrationNumber ?: "Unknown Vehicle",
                    amount = vehicle.loss,
                    reason = "Vehicle making loss"
                )
            }

            // Trip Loss Alert
            if (lossMakingTripsCount > 0) {
                LossAlertItem(
                    icon = "🛣️",
                    title = "$lossMakingTripsCount trip(s)",
                    amount = 0.0, // We don't have individual trip loss amount in summary
                    reason = "Making loss",
                    showAmount = false
                )
            }

            // View All Button
            TextButton(
                onClick = onViewDetails,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "View All Details →",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LossAlertItem(
    icon: String,
    title: String,
    amount: Double,
    reason: String,
    showAmount: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodyLarge)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showAmount && amount > 0) {
            Text(
                text = "-${formatCurrency(amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
private fun ReportTypesGrid(
    onVehiclePLClick: () -> Unit,
    onTripPLClick: () -> Unit,
    onCostAnalysisClick: () -> Unit,
    onConsolidatedClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "🚛",
                title = "Vehicle P&L",
                description = "By vehicle",
                onClick = onVehiclePLClick
            )
            ReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "🛣️",
                title = "Trip P&L",
                description = "By trip",
                onClick = onTripPLClick
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "💰",
                title = "Cost Analysis",
                description = "By type",
                onClick = onCostAnalysisClick
            )
            ReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "📊",
                title = "Consolidated",
                description = "Combined",
                onClick = onConsolidatedClick
            )
        }
    }
}

@Composable
private fun ReportTypeCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCostType(costType: String): String {
    return costType
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
