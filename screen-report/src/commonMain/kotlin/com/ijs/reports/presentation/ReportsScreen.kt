package com.ijs.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.uicomponents.components.FleetDateRangePickerDialog
import com.indusjs.uicomponents.components.LoadingContent
import kotlinx.datetime.LocalDate
import com.ijs.reports.presentation.ReportsContract.Effect
import com.ijs.reports.presentation.ReportsContract.Intent
import com.ijs.reports.presentation.ReportsContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    val snackbarHostState = remember { SnackbarHostState() }

    var pickerStartDdMm by remember { mutableStateOf("") }
    var pickerEndDdMm by remember { mutableStateOf("") }

    LaunchedEffect(state.showDateRangePicker) {
        if (state.showDateRangePicker) {
            pickerStartDdMm = apiDateToDdMmForPicker(state.startDate).ifBlank { FleetDateTime.today() }
            pickerEndDdMm = apiDateToDdMmForPicker(state.endDate).ifBlank { FleetDateTime.today() }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is Effect.NavigateToVehiclePL -> onNavigateToVehiclePL()
                is Effect.NavigateToTripPL -> onNavigateToTripPL()
                is Effect.NavigateToCostAnalysis -> onNavigateToCostAnalysis()
                is Effect.NavigateToConsolidatedPL -> onNavigateToConsolidatedPL()
                is Effect.NavigateToMaintenanceCostReport -> onNavigateToMaintenanceCostReport()
                is Effect.NavigateToTripCostReport -> onNavigateToTripCostReport()
                is Effect.NavigateToDriverCostReport -> onNavigateToDriverCostReport()
                is Effect.NavigateToCombinedReport -> onNavigateToCombinedReport()
                is Effect.ShowExportSuccess -> snackbarHostState.showSnackbar("Report exported: ${effect.filePath}")
                is Effect.ShowExportError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    FleetDateRangePickerDialog(
        isVisible = state.showDateRangePicker,
        startDate = pickerStartDdMm,
        endDate = pickerEndDdMm,
        onStartDateChange = { pickerStartDdMm = it },
        onEndDateChange = { pickerEndDdMm = it },
        onApply = { startDdMm, endDdMm ->
            val startIso = ddMmToIsoApi(startDdMm)
            val endIso = ddMmToIsoApi(endDdMm)
            if (startIso != null && endIso != null) {
                viewModel.sendIntent(Intent.SetCustomDateRange(startIso, endIso))
            }
        },
        onDismiss = { viewModel.sendIntent(Intent.HideDateRangePicker) }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.reports_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        state.summary?.let {
                            Text(text = formatPeriodLabel(state.startDate, state.endDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(Res.drawable.ic_arrow_back), contentDescription = stringResource(Res.string.back))
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
                        Icon(painter = painterResource(Res.drawable.ic_refresh), contentDescription = stringResource(Res.string.refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && !state.hasSummary -> LoadingContent()
            state.error != null && !state.hasSummary -> com.indusjs.uicomponents.components.ErrorContent(
                error = state.error!!, onRetry = { viewModel.sendIntent(Intent.Refresh) }
            )
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
private fun ReportsDashboardContent(
    state: State, onPeriodSelect: (ReportPeriod) -> Unit, onVehiclePLClick: () -> Unit, onTripPLClick: () -> Unit,
    onConsolidatedClick: () -> Unit, onCombinedReportClick: () -> Unit, onMaintenanceCostClick: () -> Unit,
    onTripCostClick: () -> Unit, onDriverCostClick: () -> Unit, onCostAnalysisClick: () -> Unit,
    onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PeriodFilterGrid(state.selectedPeriod, onPeriodSelect) }
        if (state.isLoading && state.hasSummary) item { LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))) }
        state.error?.let { item { ErrorBanner(it, onRetry) } }
        state.summary?.let { summary ->
            item { FinancialHeroCard(summary) }
            item { FleetSnapshotCard(summary) }
            if (summary.expenseBreakdown.isNotEmpty()) {
                item { SectionLabel("Expense Breakdown") }
                item { ExpenseBreakdownSection(summary.expenseBreakdown) }
            }
            if (summary.topPerformingVehicle != null || summary.lossMakingVehiclesList.isNotEmpty()) {
                item { SectionLabel("Vehicle Insights") }
                item { VehicleInsightsSection(summary.topPerformingVehicle, summary.lossMakingVehiclesList, onVehiclePLClick) }
            }
            item { DocumentCostsNotice() }
        }
        item { SectionLabel("Detailed Reports") }
        item { DetailedReportsSection(onVehiclePLClick, onTripPLClick, onConsolidatedClick, onCombinedReportClick, onMaintenanceCostClick, onTripCostClick, onDriverCostClick, onCostAnalysisClick) }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun PeriodFilterGrid(selectedPeriod: ReportPeriod, onPeriodSelect: (ReportPeriod) -> Unit) {
    val periods = ReportPeriod.entries
    val firstRow = periods.take(4)
    val secondRow = periods.drop(4)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            firstRow.forEach { period ->
                PeriodChip(Modifier.weight(1f), period.label, selectedPeriod == period) { onPeriodSelect(period) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            secondRow.forEach { period ->
                PeriodChip(Modifier.weight(1f), period.label, selectedPeriod == period) { onPeriodSelect(period) }
            }
            repeat(4 - secondRow.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun PeriodChip(modifier: Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier.clip(RoundedCornerShape(8.dp)).background(bgColor).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = textColor, maxLines = 1)
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
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

private fun formatPeriodLabel(startDate: String, endDate: String): String {
    if (startDate.isBlank() || endDate.isBlank()) return ""
    return try {
        val start = formatDateDisplay(startDate); val end = formatDateDisplay(endDate)
        if (start == end) start else "$start - $end"
    } catch (_: Exception) { "$startDate - $endDate" }
}

/** Hub state uses YYYY-MM-DD for API; picker uses DD-MM-YYYY. */
private fun apiDateToDdMmForPicker(api: String): String {
    if (api.isBlank()) return ""
    if (api.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        val ld = LocalDate.parse(api)
        return FleetDateTime.formatDateParts(ld.dayOfMonth, ld.monthNumber, ld.year)
    }
    return api
}

private fun ddMmToIsoApi(ddMm: String): String? {
    val v = FleetDateTime.parseDate(ddMm) ?: return null
    return v.toLocalDate().toString()
}

private fun formatDateDisplay(date: String): String = try {
    if (date.contains("-") && date.length >= 10) {
        val parts = date.split("-")
        if (parts.size >= 3 && parts[0].length == 4) {
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthIdx = parts[1].toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
            "${parts[2].take(2)} ${months[monthIdx]} ${parts[0]}"
        } else date
    } else date
} catch (_: Exception) { date }
