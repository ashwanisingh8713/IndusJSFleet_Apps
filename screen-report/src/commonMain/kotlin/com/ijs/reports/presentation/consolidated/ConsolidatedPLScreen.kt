package com.ijs.reports.presentation.consolidated

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.COST_TYPES
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.Effect
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.GROUP_BY_OPTIONS
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.Intent
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Consolidated P&L Screen.
 * Result cards (ConsolidatedSummaryCard, PeriodBreakdownCard, VehicleSummaryCard)
 * are in ConsolidatedPLResultCards.kt for 500-line compliance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsolidatedPLScreen(
    viewModel: ConsolidatedPLViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
            }
        }
    }

    // Inline date validation: empty is allowed (button gates on blank); a partial/invalid
    // DD-MM-YYYY entry surfaces an inline error and an out-of-order range is flagged on "To".
    val startDateError: String? = if (state.startDate.isNotBlank() && !ValidationUtils.isValidDate(state.startDate)) {
        stringResource(Res.string.reports_date_invalid)
    } else null
    val endRangeInvalid = state.startDate.isNotBlank() && state.endDate.isNotBlank() &&
        ValidationUtils.isValidDate(state.startDate) && ValidationUtils.isValidDate(state.endDate) &&
        !isEndOnOrAfterStart(state.startDate, state.endDate)
    val endDateError: String? = when {
        state.endDate.isNotBlank() && !ValidationUtils.isValidDate(state.endDate) ->
            stringResource(Res.string.reports_date_invalid)
        endRangeInvalid -> stringResource(Res.string.reports_date_range_order_error)
        else -> null
    }
    val datesValid = ValidationUtils.isValidDate(state.startDate) &&
        ValidationUtils.isValidDate(state.endDate) && !endRangeInvalid
    val canGenerate = datesValid && !state.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.reports_consolidated_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isExpanded = rememberFleetBreakpoint().isExpanded
            val contentModifier = if (isExpanded) {
                Modifier
                    .widthIn(max = FleetTokens.Width.MaxContent)
                    .align(Alignment.TopCenter)
            } else {
                Modifier.fillMaxWidth()
            }

            LazyColumn(
                modifier = contentModifier.fillMaxHeight(),
                contentPadding = PaddingValues(FleetTokens.Spacing.L),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
            ) {
                // Filters Section
                item {
                    FiltersCard(
                        vehicles = state.vehicles,
                        selectedVehicleIds = state.selectedVehicleIds,
                        selectedCostTypes = state.selectedCostTypes,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        groupBy = state.groupBy,
                        isLoadingVehicles = state.isLoadingVehicles,
                        startDateError = startDateError,
                        endDateError = endDateError,
                        onToggleVehicle = { viewModel.sendIntent(Intent.ToggleVehicle(it)) },
                        onSelectAllVehicles = { viewModel.sendIntent(Intent.SelectAllVehicles) },
                        onClearVehicles = { viewModel.sendIntent(Intent.ClearVehicles) },
                        onToggleCostType = { viewModel.sendIntent(Intent.ToggleCostType(it)) },
                        onSelectAllCostTypes = { viewModel.sendIntent(Intent.SelectAllCostTypes) },
                        onClearCostTypes = { viewModel.sendIntent(Intent.ClearCostTypes) },
                        onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                        onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) },
                        onGroupByChange = { viewModel.sendIntent(Intent.UpdateGroupBy(it)) }
                    )
                }

                // Generate Button
                item {
                    FleetButton(
                        text = stringResource(Res.string.reports_generate_report),
                        onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                        size = ButtonSize.LARGE,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canGenerate,
                        isLoading = state.isLoading
                    )
                }

                // Results
                state.result?.let { report ->
                    // Summary Card
                    item {
                        ConsolidatedSummaryCard(report = report)
                    }

                    // Period Breakdown
                    if (report.periodBreakdown.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(
                                    Res.string.reports_period_breakdown_header,
                                    state.groupBy.replaceFirstChar { it.uppercaseChar() } + "ly"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(report.periodBreakdown) { period ->
                            PeriodBreakdownCard(period = period)
                        }
                    }

                    // Vehicle Summary
                    if (report.vehicleSummary.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(Res.string.reports_vehicle_performance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(report.vehicleSummary) { vehicle ->
                            VehicleSummaryCard(
                                vehicleNumber = vehicle.vehicleNumber ?: "N/A",
                                tripCount = vehicle.tripCount,
                                revenue = vehicle.revenue,
                                expenses = vehicle.expenses,
                                profit = vehicle.profit,
                                isProfitable = vehicle.isProfitable
                            )
                        }
                    }
                }

                // Error
                state.error?.let { error ->
                    item {
                        FleetInlineErrorBanner(message = error.resolve())
                    }
                }

                // Empty / pre-generate hint
                if (state.result == null && state.error == null && !state.isLoading) {
                    item {
                        EmptyContent(
                            iconRes = Res.drawable.ic_dashboard,
                            title = stringResource(Res.string.reports_consolidated_empty_title),
                            message = stringResource(Res.string.reports_consolidated_empty_hint),
                            fillMaxSize = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compares two valid DD-MM-YYYY date strings; returns true when [endDate] is on or
 * after [startDate]. Assumes both inputs already passed [ValidationUtils.isValidDate].
 */
private fun isEndOnOrAfterStart(startDate: String, endDate: String): Boolean {
    val s = startDate.split("-")
    val e = endDate.split("-")
    if (s.size != 3 || e.size != 3) return true
    val sKey = (s[2].toIntOrNull() ?: 0) * 10000 + (s[1].toIntOrNull() ?: 0) * 100 + (s[0].toIntOrNull() ?: 0)
    val eKey = (e[2].toIntOrNull() ?: 0) * 10000 + (e[1].toIntOrNull() ?: 0) * 100 + (e[0].toIntOrNull() ?: 0)
    return eKey >= sKey
}

@Composable
private fun FiltersCard(
    vehicles: List<Vehicle>,
    selectedVehicleIds: Set<String>,
    selectedCostTypes: Set<String>,
    startDate: String,
    endDate: String,
    groupBy: String,
    isLoadingVehicles: Boolean,
    startDateError: String?,
    endDateError: String?,
    onToggleVehicle: (String) -> Unit,
    onSelectAllVehicles: () -> Unit,
    onClearVehicles: () -> Unit,
    onToggleCostType: (String) -> Unit,
    onSelectAllCostTypes: () -> Unit,
    onClearCostTypes: () -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onGroupByChange: (String) -> Unit
) {
    FleetSectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Date Range
            FilterSectionHeader(
                iconRes = Res.drawable.ic_calendar,
                text = stringResource(Res.string.reports_filter_date_range_required)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                val dateVisualTransformation = remember { DateVisualTransformation() }
                FleetInputField(
                    value = startDate,
                    onValueChange = { onStartDateChange(filterDigitsOnly(it, 8)) },
                    fieldType = FieldType.NUMBER,
                    label = stringResource(Res.string.reports_label_from),
                    placeholder = "DD-MM-YYYY",
                    visualTransformation = dateVisualTransformation,
                    isError = startDateError != null,
                    errorMessage = startDateError,
                    modifier = Modifier.weight(1f)
                )
                FleetInputField(
                    value = endDate,
                    onValueChange = { onEndDateChange(filterDigitsOnly(it, 8)) },
                    fieldType = FieldType.NUMBER,
                    label = stringResource(Res.string.reports_label_to),
                    placeholder = "DD-MM-YYYY",
                    visualTransformation = dateVisualTransformation,
                    isError = endDateError != null,
                    errorMessage = endDateError,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            // Vehicle Selection (Optional)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterSectionHeader(
                        iconRes = Res.drawable.ic_truck,
                        text = stringResource(
                            Res.string.reports_filter_vehicles_count,
                            selectedVehicleIds.size,
                            vehicles.size
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)) {
                        TextButton(onClick = onSelectAllVehicles, contentPadding = PaddingValues(FleetTokens.Spacing.XS)) {
                            Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelMedium)
                        }
                        TextButton(onClick = onClearVehicles, contentPadding = PaddingValues(FleetTokens.Spacing.XS)) {
                            Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (isLoadingVehicles) {
                    CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.M))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                        items(vehicles) { vehicle ->
                            FilterChip(
                                selected = selectedVehicleIds.contains(vehicle.id),
                                onClick = { onToggleVehicle(vehicle.id) },
                                label = { Text(vehicle.registrationNumber, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Cost Types (Optional)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterSectionHeader(
                        iconRes = Res.drawable.ic_cost,
                        text = stringResource(
                            Res.string.reports_filter_cost_types_count,
                            selectedCostTypes.size
                        )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)) {
                        TextButton(onClick = onSelectAllCostTypes, contentPadding = PaddingValues(FleetTokens.Spacing.XS)) {
                            Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelMedium)
                        }
                        TextButton(onClick = onClearCostTypes, contentPadding = PaddingValues(FleetTokens.Spacing.XS)) {
                            Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                    items(COST_TYPES) { costType ->
                        val displayName = ConsolidatedPLContract.costIdLabel(costType)
                        FilterChip(
                            selected = selectedCostTypes.contains(costType),
                            onClick = { onToggleCostType(costType) },
                            label = { Text(displayName, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }

            HorizontalDivider()

            // Group By
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterSectionHeader(
                    iconRes = Res.drawable.ic_dashboard,
                    text = stringResource(Res.string.reports_filter_group_by)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                    GROUP_BY_OPTIONS.forEach { option ->
                        FilterChip(
                            selected = groupBy == option,
                            onClick = { onGroupByChange(option) },
                            label = { Text(option.replaceFirstChar { it.uppercaseChar() }) }
                        )
                    }
                }
            }
        }
    }
}

/** Bold filter-section label preceded by a small leading vector icon. */
@Composable
private fun FilterSectionHeader(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(FleetTokens.IconSize.S)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
