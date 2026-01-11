package com.indusjs.fleet.presentation.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.DateRangePickerDialog
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

// Color constants for consistent theming
private val ProfitGreen = Color(0xFF10B981)
private val ProfitGreenLight = Color(0xFF34D399)
private val LossRed = Color(0xFFEF4444)
private val LossRedLight = Color(0xFFF87171)
private val WarningAmber = Color(0xFFF59E0B)
private val InfoBlue = Color(0xFF3B82F6)

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
    val isProfit = summary.isProfitable

    // Format dates to DD-MM-YYYY
    val formattedStartDate = summary.startDate?.let { formatDateToDDMMYYYY(it) } ?: ""
    val formattedEndDate = summary.endDate?.let { formatDateToDDMMYYYY(it) } ?: "Current"

    // Animated progress for profit margin
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedMargin by animateFloatAsState(
        targetValue = if (animationPlayed) (summary.profitMarginPercentage.toFloat() / 100f).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "margin"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
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
                Text(
                    text = "Financial Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Period info with formatted dates
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = "$formattedStartDate - $formattedEndDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Main Net Result Card - Using neutral/positive language
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status - Use positive language
                Text(
                    text = "Net Result",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Main Amount with indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isProfit) "+" else "-",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = formatCurrency(kotlin.math.abs(summary.grossProfit)),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.error
                    )
                }

                // Margin with progress
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedMargin)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text(
                        text = "${summary.profitMarginPercentage.toInt()}% margin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Revenue & Expenses Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Revenue Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ProfitGreen.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Revenue",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(summary.totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                }
            }

            // Expenses Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarningAmber.copy(alpha = 0.08f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(summary.totalExpenses),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                }
            }
        }

        // Trips Summary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.completedTrips}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InfoBlue
                    )
                    Text(
                        text = "Trips",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.profitableTrips}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                    Text(
                        text = "Profitable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.totalVehicles}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Vehicles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialMetricItem(
    icon: String,
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.labelMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}


@Composable
private fun ProfitMarginCard(summary: PLSummary) {
    // Enhanced profit margin card with circular gauge visual
    val isProfit = summary.isProfitable
    val margin = summary.profitMarginPercentage.toFloat().coerceIn(0f, 100f)

    var animationPlayed by remember { mutableStateOf(false) }
    val animatedMargin by animateFloatAsState(
        targetValue = if (animationPlayed) margin / 100f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "marginGauge"
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "📊", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Profit Margin",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = when {
                        margin >= 30 -> "Excellent margin - Keep it up!"
                        margin >= 15 -> "Good margin - Room for improvement"
                        margin >= 0 -> "Low margin - Optimize costs"
                        else -> "Negative margin - Immediate action needed"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Horizontal progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedMargin)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    when {
                                        margin >= 30 -> listOf(ProfitGreen, ProfitGreenLight)
                                        margin >= 15 -> listOf(WarningAmber, Color(0xFFFBBF24))
                                        else -> listOf(LossRed, LossRedLight)
                                    }
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Percentage display
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            margin >= 30 -> ProfitGreen.copy(alpha = 0.15f)
                            margin >= 15 -> WarningAmber.copy(alpha = 0.15f)
                            else -> LossRed.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${margin.toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            margin >= 30 -> ProfitGreen
                            margin >= 15 -> WarningAmber
                            else -> LossRed
                        }
                    )
                    Text(
                        text = if (isProfit) "↑" else "↓",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isProfit) ProfitGreen else LossRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownCard(breakdown: List<PieChartData>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "💰", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Total expenses badge
                val totalExpenses = breakdown.sumOf { it.value }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarningAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Pie Chart
            PieChart(
                data = breakdown,
                chartSize = 160.dp,
                strokeWidth = 24.dp,
                showLegend = false
            )

            // Top 3 expense categories
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                breakdown.sortedByDescending { it.value }.take(if (expanded) breakdown.size else 3).forEach { item ->
                    ExpenseBreakdownItem(
                        label = item.label,
                        amount = item.value,
                        color = item.color,
                        total = breakdown.sumOf { it.value }
                    )
                }
            }

            // Show more/less toggle
            if (breakdown.size > 3) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (expanded) "Show Less ▲" else "Show All ${breakdown.size} Categories ▼",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownItem(
    label: String,
    amount: Double,
    color: Color,
    total: Double
) {
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }

            Text(
                text = "${percentage}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(36.dp)
            )

            Text(
                text = formatCurrency(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
private fun FleetSummaryCard(summary: PLSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🚛", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Fleet Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Health indicator
                val healthPercentage = if (summary.totalVehicles > 0)
                    (summary.profitableVehicles * 100 / summary.totalVehicles) else 0
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        healthPercentage >= 70 -> ProfitGreen.copy(alpha = 0.2f)
                        healthPercentage >= 40 -> WarningAmber.copy(alpha = 0.2f)
                        else -> LossRed.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "${healthPercentage}% Healthy",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            healthPercentage >= 70 -> ProfitGreen
                            healthPercentage >= 40 -> WarningAmber
                            else -> LossRed
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FleetStatItem(
                    value = "${summary.totalVehicles}",
                    label = "Total",
                    color = MaterialTheme.colorScheme.primary,
                    icon = "🏢"
                )
                FleetStatItem(
                    value = "${summary.activeVehicles}",
                    label = "Active",
                    color = InfoBlue,
                    icon = "✅"
                )
                FleetStatItem(
                    value = "${summary.profitableVehicles}",
                    label = "Profitable",
                    color = ProfitGreen,
                    icon = "📈"
                )
                FleetStatItem(
                    value = "${summary.lossMakingVehicles}",
                    label = "Loss",
                    color = LossRed,
                    icon = "📉"
                )
            }

            // Visual progress bar
            if (summary.totalVehicles > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Profitable",
                            style = MaterialTheme.typography.labelSmall,
                            color = ProfitGreen
                        )
                        Text(
                            text = "Loss Making",
                            style = MaterialTheme.typography.labelSmall,
                            color = LossRed
                        )
                    }
                    val profitableRatio = summary.profitableVehicles.toFloat() / summary.totalVehicles
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(profitableRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(listOf(ProfitGreen, ProfitGreenLight))
                                )
                        )
                        Box(
                            modifier = Modifier
                                .weight((1f - profitableRatio).coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(listOf(LossRedLight, LossRed))
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetStatItem(
    value: String,
    label: String,
    color: Color,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.labelSmall)
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
private fun TripSummaryCard(summary: PLSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🛣️", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Trip Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Success rate badge
                val successRate = if (summary.completedTrips > 0)
                    (summary.profitableTrips * 100 / summary.completedTrips) else 0
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        successRate >= 70 -> ProfitGreen.copy(alpha = 0.2f)
                        successRate >= 40 -> WarningAmber.copy(alpha = 0.2f)
                        else -> LossRed.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "${successRate}% Success",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            successRate >= 70 -> ProfitGreen
                            successRate >= 40 -> WarningAmber
                            else -> LossRed
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FleetStatItem(
                    value = "${summary.totalTrips}",
                    label = "Total",
                    color = MaterialTheme.colorScheme.primary,
                    icon = "📋"
                )
                FleetStatItem(
                    value = "${summary.completedTrips}",
                    label = "Completed",
                    color = InfoBlue,
                    icon = "✅"
                )
                FleetStatItem(
                    value = "${summary.profitableTrips}",
                    label = "Profitable",
                    color = ProfitGreen,
                    icon = "💰"
                )
                FleetStatItem(
                    value = "${summary.lossMakingTrips}",
                    label = "Loss",
                    color = LossRed,
                    icon = "📉"
                )
            }
        }
    }
}


@Composable
private fun TopPerformerCard(title: String, registrationNumber: String, profit: Double) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        ProfitGreen.copy(alpha = 0.15f),
                        ProfitGreenLight.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Trophy with golden background
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA500))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🏆", style = MaterialTheme.typography.titleMedium)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = registrationNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Profit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+${formatCurrency(profit)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )
            }
        }
    }
}


/**
 * Quick Insights Section - Shows top performer, worst performer, and key metrics
 */
@Composable
private fun QuickInsightsSection(summary: PLSummary) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
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

        // Top Performer & Needs Review Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Performer Card
            ImprovedInsightCard(
                modifier = Modifier.weight(1f),
                icon = "🏆",
                title = "Top Performer",
                value = summary.topPerformingVehicle?.registrationNumber ?: "N/A",
                subtitle = if (summary.topPerformingVehicle != null)
                    "+${formatCurrency(summary.topPerformingVehicle.profit)}" else "No data",
                cardType = InsightCardType.POSITIVE
            )

            // Needs Review Card - Use neutral language
            val worstVehicle = summary.lossMakingVehiclesList.firstOrNull()
            ImprovedInsightCard(
                modifier = Modifier.weight(1f),
                icon = if (worstVehicle != null) "📋" else "✅",
                title = if (worstVehicle != null) "Needs Review" else "All Clear",
                value = worstVehicle?.registrationNumber ?: "Great!",
                subtitle = if (worstVehicle != null)
                    "${formatCurrency(worstVehicle.loss)} expenses" else "No issues",
                cardType = if (worstVehicle != null) InsightCardType.NEUTRAL else InsightCardType.POSITIVE
            )
        }

        // Key Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Vehicles Metric
            MetricCard(
                modifier = Modifier.weight(1f),
                value = "${summary.profitableVehicles}/${summary.totalVehicles}",
                label = "Profitable Vehicles",
                progress = if (summary.totalVehicles > 0)
                    summary.profitableVehicles.toFloat() / summary.totalVehicles else 0f
            )

            // Trips Metric
            MetricCard(
                modifier = Modifier.weight(1f),
                value = "${summary.profitableTrips}/${summary.completedTrips}",
                label = "Profitable Trips",
                progress = if (summary.completedTrips > 0)
                    summary.profitableTrips.toFloat() / summary.completedTrips else 0f
            )

            // Margin Metric
            MetricCard(
                modifier = Modifier.weight(1f),
                value = "${summary.profitMarginPercentage.toInt()}%",
                label = "Margin",
                progress = (summary.profitMarginPercentage / 100f).toFloat().coerceIn(0f, 1f)
            )
        }
    }
}

private enum class InsightCardType {
    POSITIVE, NEUTRAL, NEGATIVE
}

@Composable
private fun ImprovedInsightCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    value: String,
    subtitle: String,
    cardType: InsightCardType
) {
    val backgroundColor = when (cardType) {
        InsightCardType.POSITIVE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        InsightCardType.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainerHigh
        InsightCardType.NEGATIVE -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }

    val subtitleColor = when (cardType) {
        InsightCardType.POSITIVE -> ProfitGreen
        InsightCardType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        InsightCardType.NEGATIVE -> MaterialTheme.colorScheme.error
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = icon, style = MaterialTheme.typography.titleSmall)
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = subtitleColor
            )
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    progress: Float
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Loss Alerts Section - Shows critical alerts for loss-making items with severity levels
 */
@Composable
private fun LossAlertsSection(
    lossMakingVehicles: List<com.indusjs.fleet.domain.entity.reports.VehiclePerformer>,
    lossMakingTripsCount: Int,
    onViewDetails: () -> Unit
) {
    val totalAlerts = lossMakingVehicles.size + (if (lossMakingTripsCount > 0) 1 else 0)
    val totalLoss = lossMakingVehicles.sumOf { it.loss }

    var expanded by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LossRed.copy(alpha = 0.08f))
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pulsing alert icon container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LossRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🚨", style = MaterialTheme.typography.titleMedium)
                    }
                    Column {
                        Text(
                            text = "Loss Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                        Text(
                            text = "$totalAlerts item(s) • Total: ${formatCurrency(totalLoss)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Expand/Collapse indicator
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (expanded) "▲" else "▼",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Vehicle Loss Alerts with severity
                    lossMakingVehicles.take(3).forEachIndexed { index, vehicle ->
                        val severity = when {
                            vehicle.loss > 50000 -> AlertSeverity.CRITICAL
                            vehicle.loss > 10000 -> AlertSeverity.WARNING
                            else -> AlertSeverity.INFO
                        }
                        EnhancedLossAlertItem(
                            icon = "🚛",
                            title = vehicle.registrationNumber ?: "Unknown Vehicle",
                            amount = vehicle.loss,
                            severity = severity,
                            rank = index + 1
                        )
                    }

                    // Trip Loss Alert
                    if (lossMakingTripsCount > 0) {
                        EnhancedLossAlertItem(
                            icon = "🛣️",
                            title = "$lossMakingTripsCount trip(s) making loss",
                            amount = 0.0,
                            severity = AlertSeverity.WARNING,
                            showAmount = false
                        )
                    }

                    // View All Button
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LossRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "View Details & Take Action",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private enum class AlertSeverity {
    CRITICAL, WARNING, INFO
}

@Composable
private fun EnhancedLossAlertItem(
    icon: String,
    title: String,
    amount: Double,
    severity: AlertSeverity,
    rank: Int = 0,
    showAmount: Boolean = true
) {
    val severityColor = when (severity) {
        AlertSeverity.CRITICAL -> LossRed
        AlertSeverity.WARNING -> WarningAmber
        AlertSeverity.INFO -> InfoBlue
    }

    val severityLabel = when (severity) {
        AlertSeverity.CRITICAL -> "CRITICAL"
        AlertSeverity.WARNING -> "WARNING"
        AlertSeverity.INFO -> "INFO"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank badge (if applicable)
                if (rank > 0) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(severityColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                    }
                }

                Text(text = icon, style = MaterialTheme.typography.titleSmall)

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Severity label
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = severityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = severityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (showAmount && amount > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "-${formatCurrency(amount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LossRed
                    )
                }
            }
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
            EnhancedReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "🚛",
                title = "Vehicle P&L",
                description = "Profit & Loss by vehicle",
                accentColor = InfoBlue,
                onClick = onVehiclePLClick
            )
            EnhancedReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "🛣️",
                title = "Trip P&L",
                description = "Profit & Loss by trip",
                accentColor = ProfitGreen,
                onClick = onTripPLClick
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EnhancedReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "💰",
                title = "Cost Analysis",
                description = "Breakdown by cost type",
                accentColor = WarningAmber,
                onClick = onCostAnalysisClick
            )
            EnhancedReportTypeCard(
                modifier = Modifier.weight(1f),
                icon = "📊",
                title = "Consolidated",
                description = "Combined fleet report",
                accentColor = Color(0xFF8B5CF6),
                onClick = onConsolidatedClick
            )
        }
    }
}

@Composable
private fun EnhancedReportTypeCard(
    modifier: Modifier = Modifier,
    icon: String,
    title: String,
    description: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Accent bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, style = MaterialTheme.typography.headlineSmall)
                }

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
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Arrow indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Report",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = " →",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


private fun formatCostType(costType: String): String {
    return costType
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

/**
 * Convert date from YYYY-MM-DD or other formats to DD-MM-YYYY
 */
private fun formatDateToDDMMYYYY(date: String): String {
    return try {
        // Handle YYYY-MM-DD format
        if (date.contains("-") && date.length >= 10) {
            val parts = date.split("-")
            if (parts.size >= 3 && parts[0].length == 4) {
                "${parts[2].take(2)}-${parts[1]}-${parts[0]}"
            } else {
                date
            }
        } else {
            date
        }
    } catch (e: Exception) {
        date
    }
}

