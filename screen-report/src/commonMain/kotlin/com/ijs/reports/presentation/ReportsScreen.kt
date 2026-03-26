package com.ijs.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.DateRangePickerDialog
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.PieChart
import com.indusjs.uicomponents.components.PieChartColors
import com.indusjs.uicomponents.components.PieChartData
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.reports.domain.entity.ExpenseBreakdownItem
import com.ijs.reports.domain.entity.PLSummary
import com.ijs.reports.domain.entity.VehiclePerformer
import com.ijs.reports.presentation.ReportsContract.Effect
import com.ijs.reports.presentation.ReportsContract.Intent
import com.ijs.reports.presentation.ReportsContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs
import kotlin.math.roundToInt

// Color constants
private val ProfitGreen = Color(0xFF10B981)
private val LossRed = Color(0xFFEF4444)
private val WarningAmber = Color(0xFFF59E0B)
private val InfoBlue = Color(0xFF3B82F6)
private val Purple = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF06B6D4)
private val Teal = Color(0xFF14B8A6)
private val Pink = Color(0xFFEC4899)

/**
 * Reports Hub Screen - Entry point for all P&L reports
 * As per report-pl-screen.prompt.md, includes:
 * - Vehicle P&L, Trip P&L, Fleet P&L, Combined Report
 * - Maintenance Cost, Trip Cost, Driver Cost analysis
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVehiclePL: () -> Unit,
    onNavigateToTripPL: () -> Unit,
    onNavigateToCostAnalysis: () -> Unit,
    onNavigateToConsolidatedPL: () -> Unit,
    onNavigateToMaintenanceCostReport: () -> Unit = {},
    onNavigateToTripCostReport: () -> Unit = {},
    onNavigateToDriverCostReport: () -> Unit = {},
    onNavigateToCombinedReport: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> {}
                is Effect.NavigateToVehiclePL -> onNavigateToVehiclePL()
                is Effect.NavigateToTripPL -> onNavigateToTripPL()
                is Effect.NavigateToCostAnalysis -> onNavigateToCostAnalysis()
                is Effect.NavigateToConsolidatedPL -> onNavigateToConsolidatedPL()
                is Effect.NavigateToMaintenanceCostReport -> onNavigateToMaintenanceCostReport()
                is Effect.NavigateToTripCostReport -> onNavigateToTripCostReport()
                is Effect.NavigateToDriverCostReport -> onNavigateToDriverCostReport()
                is Effect.NavigateToCombinedReport -> onNavigateToCombinedReport()
                is Effect.ShowExportSuccess -> {}
                is Effect.ShowExportError -> {}
            }
        }
    }

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
                title = {
                    Column {
                        Text("Reports & P/L", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        state.summary?.let {
                            Text(
                                text = formatPeriodLabel(state.startDate, state.endDate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(Res.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.hasSummary) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.ExportToPdf) }, enabled = !state.isExporting) {
                            if (state.isExporting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(painter = painterResource(Res.drawable.ic_refresh), contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && !state.hasSummary -> LoadingContent()
            state.error != null && !state.hasSummary -> ErrorContent(state.error!!) { viewModel.sendIntent(Intent.Refresh) }
            else -> ReportsDashboardContent(
                state = state,
                onPeriodSelect = { viewModel.sendIntent(Intent.SelectPeriod(it)) },
                onVehiclePLClick = { viewModel.sendIntent(Intent.NavigateToVehiclePL) },
                onTripPLClick = { viewModel.sendIntent(Intent.NavigateToTripPL) },
                onConsolidatedClick = { viewModel.sendIntent(Intent.NavigateToConsolidatedPL) },
                onCombinedReportClick = { viewModel.sendIntent(Intent.NavigateToCombinedReport) },
                onMaintenanceCostClick = { viewModel.sendIntent(Intent.NavigateToMaintenanceCostReport) },
                onTripCostClick = { viewModel.sendIntent(Intent.NavigateToTripCostReport) },
                onDriverCostClick = { viewModel.sendIntent(Intent.NavigateToDriverCostReport) },
                onCostAnalysisClick = { viewModel.sendIntent(Intent.NavigateToCostAnalysis) },
                onRetry = { viewModel.sendIntent(Intent.Refresh) },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ErrorContent(error: String, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("😕", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text("Something went wrong", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun ReportsDashboardContent(
    state: State, onPeriodSelect: (ReportPeriod) -> Unit, onVehiclePLClick: () -> Unit, onTripPLClick: () -> Unit,
    onConsolidatedClick: () -> Unit, onCombinedReportClick: () -> Unit, onMaintenanceCostClick: () -> Unit,
    onTripCostClick: () -> Unit, onDriverCostClick: () -> Unit, onCostAnalysisClick: () -> Unit,
    onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PeriodFilterChips(state.selectedPeriod, onPeriodSelect) }
        if (state.isLoading && state.hasSummary) item { LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))) }
        state.error?.let { item { ErrorBanner(it, onRetry) } }

        state.summary?.let { summary ->
            item { SectionHeader("Financial Overview", "💰") }
            item { FinancialOverviewCard(summary) }
            item { SectionHeader("Key Metrics", "📊") }
            item { KeyMetricsRow(summary) }
            item { SectionHeader("Performance", "🚛") }
            item { PerformanceCards(summary) }
            if (summary.expenseBreakdown.isNotEmpty()) {
                item { SectionHeader("Expense Breakdown", "📈") }
                item { ExpenseBreakdownSection(summary.expenseBreakdown) }
            }
            if (summary.topPerformingVehicle != null || summary.lossMakingVehiclesList.isNotEmpty()) {
                item { SectionHeader("Vehicle Insights", "💡") }
                item { VehicleInsightsSection(summary.topPerformingVehicle, summary.lossMakingVehiclesList, onVehiclePLClick) }
            }
            item { DocumentCostsNotice() }
        }

        item { SectionHeader("Detailed Reports", "📋") }
        item { DetailedReportsSection(onVehiclePLClick, onTripPLClick, onConsolidatedClick, onCombinedReportClick, onMaintenanceCostClick, onTripCostClick, onDriverCostClick, onCostAnalysisClick) }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun PeriodFilterChips(selectedPeriod: ReportPeriod, onPeriodSelect: (ReportPeriod) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportPeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelect(period) },
                label = { Text(period.label, fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

@Composable
private fun ErrorBanner(error: String, onRetry: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Error loading data", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
            }
            TextButton(onClick = onRetry) { Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        Text(icon, fontSize = 18.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FinancialOverviewCard(summary: PLSummary) {
    val isProfit = summary.isProfitable
    val profitStatus = ProfitStatus.fromMargin(summary.profitMarginPercentage)
    val statusColor = Color(profitStatus.colorHex)

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(statusColor.copy(alpha = 0.1f)).padding(16.dp)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(profitStatus.icon, fontSize = 24.sp)
                        Text(if (isProfit) "Net Profit" else "Net Loss", style = MaterialTheme.typography.titleMedium, color = statusColor)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("${if (isProfit) "+" else "-"}${formatCurrency(abs(summary.grossProfit))}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = statusColor)
                    Spacer(Modifier.height(4.dp))
                    Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.2f)) {
                        Text("${profitStatus.label} • ${summary.profitMarginPercentage.roundToInt()}% margin", style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FinancialMetricCard(Modifier.weight(1f), "Total Revenue", summary.totalRevenue, "📥", ProfitGreen)
                FinancialMetricCard(Modifier.weight(1f), "Total Expenses", summary.totalExpenses, "📤", LossRed)
            }
        }
    }
}

@Composable
private fun FinancialMetricCard(modifier: Modifier, label: String, amount: Double, icon: String, color: Color) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.08f)).padding(12.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(icon, fontSize = 14.sp)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(formatCurrency(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun KeyMetricsRow(summary: PLSummary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KeyMetricItem(Modifier.weight(1f), "${summary.completedTrips}", "Trips", "Completed", InfoBlue)
        KeyMetricItem(Modifier.weight(1f), "${summary.activeVehicles}", "Vehicles", "Active", Purple)
        KeyMetricItem(Modifier.weight(1f), "${summary.profitMarginPercentage.roundToInt()}%", "Margin", if (summary.isProfitable) "Profit" else "Loss", if (summary.isProfitable) ProfitGreen else LossRed)
    }
}

@Composable
private fun KeyMetricItem(modifier: Modifier, value: String, label: String, subLabel: String, color: Color) {
    Card(modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PerformanceCards(summary: PLSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PerformanceCard("Fleet", "🚛", listOf(
            Triple("Total", summary.totalVehicles, MaterialTheme.colorScheme.primary),
            Triple("Active", summary.activeVehicles, InfoBlue),
            Triple("Profitable", summary.profitableVehicles, ProfitGreen),
            Triple("Loss", summary.lossMakingVehicles, LossRed)
        ), if (summary.totalVehicles > 0) (summary.profitableVehicles * 100 / summary.totalVehicles) else 0)
        PerformanceCard("Trips", "🛣️", listOf(
            Triple("Total", summary.totalTrips, MaterialTheme.colorScheme.primary),
            Triple("Completed", summary.completedTrips, InfoBlue),
            Triple("Profitable", summary.profitableTrips, ProfitGreen),
            Triple("Loss", summary.lossMakingTrips, LossRed)
        ), if (summary.completedTrips > 0) (summary.profitableTrips * 100 / summary.completedTrips) else 0)
    }
}

@Composable
private fun PerformanceCard(title: String, icon: String, stats: List<Triple<String, Int, Color>>, healthPercentage: Int) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(icon, fontSize = 18.sp)
                    Text("$title Performance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                val healthColor = when { healthPercentage >= 70 -> ProfitGreen; healthPercentage >= 40 -> WarningAmber; else -> LossRed }
                Surface(shape = RoundedCornerShape(12.dp), color = healthColor.copy(alpha = 0.15f)) {
                    Text("${healthPercentage}% Healthy", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = healthColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                stats.forEach { (label, value, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseBreakdownSection(breakdown: List<ExpenseBreakdownItem>) {
    var expanded by remember { mutableStateOf(false) }
    val totalExpenses = breakdown.sumOf { it.amount }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Total: ${formatCurrency(totalExpenses)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            PieChart(data = breakdown.map { PieChartData(formatCostType(it.type), it.amount, PieChartColors.getCostTypeColor(it.type)) }, chartSize = 140.dp, strokeWidth = 20.dp, showLegend = false)
            Spacer(Modifier.height(16.dp))
            (if (expanded) breakdown else breakdown.take(4)).sortedByDescending { it.amount }.forEach { item ->
                ExpenseItemRow(formatCostType(item.type), item.amount, item.percentage, PieChartColors.getCostTypeColor(item.type), totalExpenses)
                Spacer(Modifier.height(8.dp))
            }
            if (breakdown.size > 4) TextButton(onClick = { expanded = !expanded }, Modifier.align(Alignment.CenterHorizontally)) { Text(if (expanded) "Show Less ▲" else "Show All ${breakdown.size} ▼") }
        }
    }
}

@Composable
private fun ExpenseItemRow(type: String, amount: Double, percentage: Double, color: Color, total: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Text(type, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxWidth((amount / total).toFloat().coerceIn(0f, 1f)).fillMaxHeight().background(color))
            }
            Text("${percentage.roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(32.dp))
            Text(formatCurrency(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
        }
    }
}

@Composable
private fun VehicleInsightsSection(topPerformer: VehiclePerformer?, lossMakingVehicles: List<VehiclePerformer>, onViewDetails: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        topPerformer?.let { InsightCard("🏆", "Top Performer", it.registrationNumber ?: "N/A", "+${formatCurrency(it.profit)}", ProfitGreen.copy(alpha = 0.1f), ProfitGreen) }
        if (lossMakingVehicles.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = LossRed.copy(alpha = 0.08f))) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("⚠️", fontSize = 18.sp)
                        Text("${lossMakingVehicles.size} Vehicle(s) Need Attention", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = LossRed)
                    }
                    Spacer(Modifier.height(12.dp))
                    lossMakingVehicles.take(3).forEach { vehicle ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(vehicle.registrationNumber ?: "Unknown", style = MaterialTheme.typography.bodyMedium)
                            Text("-${formatCurrency(vehicle.loss)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = LossRed)
                        }
                    }
                    if (lossMakingVehicles.size > 3) { Spacer(Modifier.height(8.dp)); TextButton(onClick = onViewDetails, Modifier.align(Alignment.CenterHorizontally)) { Text("View All ${lossMakingVehicles.size} Vehicles →") } }
                }
            }
        }
    }
}

@Composable
private fun InsightCard(icon: String, title: String, value: String, subtitle: String, backgroundColor: Color, subtitleColor: Color) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(backgroundColor).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(icon, fontSize = 28.sp)
                Column { Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
            }
            Text(subtitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = subtitleColor)
        }
    }
}

@Composable
private fun DocumentCostsNotice() {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ℹ️", fontSize = 14.sp)
            Text("Document costs (Insurance, Permits, PUC, etc.) are not yet included in P&L calculations.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailedReportsSection(onVehiclePLClick: () -> Unit, onTripPLClick: () -> Unit, onConsolidatedClick: () -> Unit, onCombinedReportClick: () -> Unit, onMaintenanceCostClick: () -> Unit, onTripCostClick: () -> Unit, onDriverCostClick: () -> Unit, onCostAnalysisClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Profit & Loss", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f), "🚛", "Vehicle P&L", "By vehicle", InfoBlue, onVehiclePLClick)
            ReportCard(Modifier.weight(1f), "🛣️", "Trip P&L", "By trip", ProfitGreen, onTripPLClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f), "📈", "Fleet P&L", "Consolidated", Purple, onConsolidatedClick)
            ReportCard(Modifier.weight(1f), "🔀", "Combined", "Vehicle + Trip", Cyan, onCombinedReportClick)
        }
        Spacer(Modifier.height(8.dp))
        Text("Cost Analysis", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f), "🔧", "Maintenance", "By type", WarningAmber, onMaintenanceCostClick)
            ReportCard(Modifier.weight(1f), "⛽", "Trip Costs", "Fuel, Toll...", Pink, onTripCostClick)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportCard(Modifier.weight(1f), "👨‍✈️", "Driver Costs", "Salary & more", Teal, onDriverCostClick)
            ReportCard(Modifier.weight(1f), "💰", "All Costs", "Full breakdown", Color(0xFFF97316), onCostAnalysisClick)
        }
    }
}

@Composable
private fun ReportCard(modifier: Modifier, icon: String, title: String, description: String, color: Color, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Box {
            Box(Modifier.fillMaxWidth().height(3.dp).background(color))
            Column(Modifier.padding(16.dp).padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) { Text(icon, fontSize = 20.sp) }
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }
}

private fun formatCostType(costType: String) = costType.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

private fun formatPeriodLabel(startDate: String, endDate: String): String {
    if (startDate.isBlank() || endDate.isBlank()) return ""
    return try { val start = formatDateDisplay(startDate); val end = formatDateDisplay(endDate); if (start == end) start else "$start - $end" } catch (e: Exception) { "$startDate - $endDate" }
}

private fun formatDateDisplay(date: String): String = try {
    if (date.contains("-") && date.length >= 10) {
        val parts = date.split("-")
        if (parts.size >= 3 && parts[0].length == 4) {
            val day = parts[2].take(2)
            val month = when (parts[1]) { "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"; "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"; "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"; else -> parts[1] }
            "$day $month ${parts[0]}"
        } else date
    } else date
} catch (e: Exception) { date }
