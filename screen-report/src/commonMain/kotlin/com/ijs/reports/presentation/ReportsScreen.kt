package com.ijs.reports.presentation

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.uicomponents.components.FleetDateRangePickerDialog
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.pdfreport.handler.FleetProfitLossPdfHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    onNavigateToCustomerPL: () -> Unit = {},
    onNavigateToMaintenanceCostReport: () -> Unit = {},
    onNavigateToTripCostReport: () -> Unit = {},
    onNavigateToDriverCostReport: () -> Unit = {},
    onNavigateToCombinedReport: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val exportScope = rememberCoroutineScope()
    var pendingExportPath by remember { mutableStateOf<String?>(null) }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Generates the real Fleet P&L PDF when an export is triggered (state.pdfExportData != null)
    // and shows the open/share dialog. Replaces the previous fake-success stub.
    FleetProfitLossPdfHandler(
        pdfData = state.pdfExportData,
        onExportComplete = { viewModel.sendIntent(Intent.DismissExportDialog) },
        onExportError = { message ->
            viewModel.sendIntent(Intent.DismissExportDialog)
            exportScope.launch { snackbarHostState.showSnackbar(message) }
        }
    )

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
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is Effect.NavigateToVehiclePL -> onNavigateToVehiclePL()
                is Effect.NavigateToTripPL -> onNavigateToTripPL()
                is Effect.NavigateToCostAnalysis -> onNavigateToCostAnalysis()
                is Effect.NavigateToConsolidatedPL -> onNavigateToConsolidatedPL()
                is Effect.NavigateToCustomerPL -> onNavigateToCustomerPL()
                is Effect.NavigateToMaintenanceCostReport -> onNavigateToMaintenanceCostReport()
                is Effect.NavigateToTripCostReport -> onNavigateToTripCostReport()
                is Effect.NavigateToDriverCostReport -> onNavigateToDriverCostReport()
                is Effect.NavigateToCombinedReport -> onNavigateToCombinedReport()
                is Effect.ShowExportSuccess -> pendingExportPath = effect.filePath
                is Effect.ShowExportError -> pendingSnackbar = effect.message
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
                    Text(
                        stringResource(Res.string.reports_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Period filter sits in the action row (left of the icons), never under the title.
                    PeriodSelector(
                        selectedPeriod = state.selectedPeriod,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        onPeriodSelect = { viewModel.sendIntent(Intent.SelectPeriod(it)) }
                    )
                    Spacer(Modifier.width(FleetTokens.Spacing.XS))
                    // Export = a clean icon-only download action (matches Refresh), not a heavy grey tonal pill.
                    if (state.hasSummary) {
                        IconButton(
                            onClick = { viewModel.sendIntent(Intent.ExportToPdf) },
                            enabled = !state.isExporting
                        ) {
                            if (state.isExporting) {
                                CircularProgressIndicator(
                                    Modifier.size(FleetTokens.IconSize.S),
                                    strokeWidth = FleetTokens.Height.ProgressStroke,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_download),
                                    contentDescription = stringResource(Res.string.export_format_pdf),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading && !state.hasSummary -> LoadingContent()
            state.error != null && !state.hasSummary -> com.indusjs.uicomponents.components.ErrorContent(
                error = state.error!!.resolve(), onRetry = { viewModel.sendIntent(Intent.Refresh) }
            )
            else -> ReportsDashboardContent(
                state = state,
                onVehiclePLClick = { viewModel.sendIntent(Intent.NavigateToVehiclePL) },
                onTripPLClick = { viewModel.sendIntent(Intent.NavigateToTripPL) },
                onConsolidatedClick = { viewModel.sendIntent(Intent.NavigateToConsolidatedPL) },
                onCustomerPLClick = { viewModel.sendIntent(Intent.NavigateToCustomerPL) },
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
    state: State, onVehiclePLClick: () -> Unit, onTripPLClick: () -> Unit,
    onConsolidatedClick: () -> Unit, onCustomerPLClick: () -> Unit, onCombinedReportClick: () -> Unit, onMaintenanceCostClick: () -> Unit,
    onTripCostClick: () -> Unit, onDriverCostClick: () -> Unit, onCostAnalysisClick: () -> Unit,
    onRetry: () -> Unit, modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // On Expanded the dashboard is capped to a readable width and centered
        // instead of stretching edge-to-edge; Compact/Medium fill the width.
        val contentWidthModifier = if (breakpoint.isExpanded) {
            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxWidth()
        }
        LazyColumn(
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            if (state.isLoading && state.hasSummary) item {
                LinearProgressIndicator(Modifier.fillMaxWidth().clip(RoundedCornerShape(FleetTokens.Radius.M)))
            }
            state.error?.let { item { ErrorBanner(it.resolve(), onRetry) } }
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
            item { DetailedReportsSection(onVehiclePLClick, onTripPLClick, onConsolidatedClick, onCustomerPLClick, onCombinedReportClick, onMaintenanceCostClick, onTripCostClick, onDriverCostClick, onCostAnalysisClick) }
            item { Spacer(Modifier.height(FleetTokens.Spacing.XL)) }
        }
    }
}

// Period selector — a compact tappable pill in the top-bar action row (left of the icons, never
// under the title). Opens a menu listing all 8 periods. Every period shows its name + the resolved
// date window in one consistent layout, so the chip looks the same regardless of which is active.
@Composable
private fun PeriodSelector(
    selectedPeriod: ReportPeriod,
    startDate: String,
    endDate: String,
    onPeriodSelect: (ReportPeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "periodChevron")
    // Whole Row is the click target: carry the a11y label here (chevron stays decorative).
    val selectPeriodLabel = stringResource(Res.string.reports_select_period)
    // Total uniformity: every period shows its name + the resolved window (a single date when
    // start == end), all using one consistent name+range layout. Name-only is the pre-load fallback.
    val showRange = startDate.isNotBlank() && endDate.isNotBlank()
    Box {
        Row(
            modifier = Modifier
                .heightIn(min = FleetTokens.Height.MinTouchTarget)
                .clip(RoundedCornerShape(FleetTokens.Radius.Pill))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .clickable(onClickLabel = selectPeriodLabel, role = Role.Button) { expanded = true }
                .padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_calendar),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(FleetTokens.IconSize.S)
            )
            if (showRange) {
                // Name on top, resolved window beneath — identical layout for fiscal spans and custom.
                Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS)) {
                    Text(
                        text = selectedPeriod.localizedLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatPeriodLabel(startDate, endDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = selectedPeriod.localizedLabel(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(FleetTokens.IconSize.S).rotate(chevronRotation)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = FleetTokens.Width.DropdownMaxHeight)
        ) {
            ReportPeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                DropdownMenuItem(
                    text = {
                        Text(
                            text = period.localizedLabel(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        expanded = false
                        onPeriodSelect(period)
                    },
                    trailingIcon = if (isSelected) {
                        {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    FleetSectionHeader(title = title)
}

@Composable
private fun ErrorBanner(error: String, onRetry: () -> Unit) {
    FleetInlineErrorBanner(
        title = stringResource(Res.string.reports_error_loading),
        message = error,
        actionLabel = stringResource(Res.string.retry),
        onAction = onRetry
    )
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
private fun formatDateDisplay(date: String): String =
    if (date.isBlank()) date
    else com.indusjs.fleet.core.util.formatDateToHumanReadable(date).ifBlank { date }

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

