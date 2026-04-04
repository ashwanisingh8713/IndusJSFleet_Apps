package com.ijs.reports.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.uicomponents.components.FleetDateRangePickerDialog
import com.indusjs.uicomponents.components.LoadingContent
import kotlinx.coroutines.flow.collectLatest
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
    var pendingExportPath by remember { mutableStateOf<String?>(null) }

    pendingExportPath?.let { path ->
        val msg = stringResource(Res.string.reports_exported_path, path)
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(msg)
            pendingExportPath = null
        }
    }

    // State dates are YYYY-MM-DD (API format), picker uses DD-MM-YYYY
    var pickerStartDdMm by remember { mutableStateOf("") }
    var pickerEndDdMm by remember { mutableStateOf("") }

    LaunchedEffect(state.showDateRangePicker) {
        if (state.showDateRangePicker) {
            // Convert YYYY-MM-DD state dates to DD-MM-YYYY for picker
            pickerStartDdMm = convertToPickerFormat(state.startDate)
            pickerEndDdMm = convertToPickerFormat(state.endDate)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
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
                is Effect.ShowExportSuccess -> pendingExportPath = effect.filePath
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
            // Picker returns DD-MM-YYYY; ViewModel will convert to YYYY-MM-DD for API
            if (startDdMm.isNotBlank() && endDdMm.isNotBlank()) {
                viewModel.sendIntent(Intent.SetCustomDateRange(startDdMm, endDdMm))
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
                        FilledTonalButton(
                            onClick = { viewModel.sendIntent(Intent.ExportToPdf) },
                            enabled = !state.isExporting,
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isExporting) {
                                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_download),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(Res.string.export_format_pdf),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.width(4.dp))
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
                item { SectionLabel(stringResource(Res.string.reports_section_expense_breakdown)) }
                item { ExpenseBreakdownSection(summary.expenseBreakdown) }
            }
            if (summary.topPerformingVehicle != null || summary.lossMakingVehiclesList.isNotEmpty()) {
                item { SectionLabel(stringResource(Res.string.reports_section_vehicle_insights)) }
                item { VehicleInsightsSection(summary.topPerformingVehicle, summary.lossMakingVehiclesList, onVehiclePLClick) }
            }
            item { DocumentCostsNotice() }
        }
        item { SectionLabel(stringResource(Res.string.reports_section_detailed_reports)) }
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
                PeriodChip(Modifier.weight(1f), period.localizedLabel(), selectedPeriod == period) { onPeriodSelect(period) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            secondRow.forEach { period ->
                PeriodChip(Modifier.weight(1f), period.localizedLabel(), selectedPeriod == period) { onPeriodSelect(period) }
            }
            repeat(4 - secondRow.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun PeriodChip(modifier: Modifier, label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (selected) 4.dp else 0.dp,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Box(
            Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
        }
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
                Text(stringResource(Res.string.reports_error_loading), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f))
            }
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.retry), color = MaterialTheme.colorScheme.onErrorContainer) }
        }
    }
}

/** Formats period label from state dates (YYYY-MM-DD format). */
private fun formatPeriodLabel(startDate: String, endDate: String): String {
    if (startDate.isBlank() || endDate.isBlank()) return ""
    return try {
        val start = formatDateDisplay(startDate); val end = formatDateDisplay(endDate)
        if (start == end) start else "$start - $end"
    } catch (_: Exception) { "$startDate - $endDate" }
}

/** Converts a date string (either DD-MM-YYYY or YYYY-MM-DD) to human-readable (e.g., "04 Apr 2026"). */
private fun formatDateDisplay(date: String): String = try {
    if (date.contains("-") && date.length >= 10) {
        val parts = date.split("-")
        if (parts.size >= 3) {
            val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            if (parts[0].length == 4) {
                // YYYY-MM-DD format (API/state format)
                val monthIdx = parts[1].toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
                "${parts[2].take(2)} ${months[monthIdx]} ${parts[0]}"
            } else if (parts[0].length == 2 && parts[2].length == 4) {
                // DD-MM-YYYY format (picker/legacy)
                val monthIdx = parts[1].toIntOrNull()?.minus(1)?.coerceIn(0, 11) ?: 0
                "${parts[0]} ${months[monthIdx]} ${parts[2]}"
            } else date
        } else date
    } else date
} catch (_: Exception) { date }

/**
 * Converts YYYY-MM-DD state date to DD-MM-YYYY for the picker.
 * Returns FleetDateTime.today() if blank.
 */
private fun convertToPickerFormat(date: String): String {
    if (date.isBlank()) return FleetDateTime.today()
    val parts = date.split("-")
    if (parts.size != 3) return date
    // YYYY-MM-DD: parts[0]=year(4), parts[1]=month(2), parts[2]=day(2)
    return if (parts[0].length == 4) {
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else date // Already DD-MM-YYYY
}

