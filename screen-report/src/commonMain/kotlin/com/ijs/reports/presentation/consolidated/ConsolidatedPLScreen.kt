package com.ijs.reports.presentation.consolidated

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Button(
                    onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.startDate.isNotBlank() && state.endDate.isNotBlank() && !state.isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(Res.string.reports_generate_report))
                }
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
        }
    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range
            Text(
                text = "📅 " + stringResource(Res.string.reports_filter_date_range_required),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val dateVisualTransformation = remember { DateVisualTransformation() }
                FleetInputField(
                    value = startDate,
                    onValueChange = { onStartDateChange(filterDigitsOnly(it, 8)) },
                    fieldType = FieldType.NUMBER,
                    label = stringResource(Res.string.reports_label_from),
                    placeholder = "DD-MM-YYYY",
                    visualTransformation = dateVisualTransformation,
                    modifier = Modifier.weight(1f)
                )
                FleetInputField(
                    value = endDate,
                    onValueChange = { onEndDateChange(filterDigitsOnly(it, 8)) },
                    fieldType = FieldType.NUMBER,
                    label = stringResource(Res.string.reports_label_to),
                    placeholder = "DD-MM-YYYY",
                    visualTransformation = dateVisualTransformation,
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
                    Text(
                        text = "🚚 " + stringResource(
                            Res.string.reports_filter_vehicles_count,
                            selectedVehicleIds.size,
                            vehicles.size
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onSelectAllVehicles, contentPadding = PaddingValues(4.dp)) {
                            Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = onClearVehicles, contentPadding = PaddingValues(4.dp)) {
                            Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                if (isLoadingVehicles) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(vehicles) { vehicle ->
                            FilterChip(
                                selected = selectedVehicleIds.contains(vehicle.id),
                                onClick = { onToggleVehicle(vehicle.id) },
                                label = { Text(vehicle.registrationNumber, style = MaterialTheme.typography.labelSmall) }
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
                    Text(
                        text = "💰 " + stringResource(
                            Res.string.reports_filter_cost_types_count,
                            selectedCostTypes.size
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onSelectAllCostTypes, contentPadding = PaddingValues(4.dp)) {
                            Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = onClearCostTypes, contentPadding = PaddingValues(4.dp)) {
                            Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(COST_TYPES) { costType ->
                        val displayName = ConsolidatedPLContract.costIdLabel(costType)
                        FilterChip(
                            selected = selectedCostTypes.contains(costType),
                            onClick = { onToggleCostType(costType) },
                            label = { Text(displayName, style = MaterialTheme.typography.labelSmall) }
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
                Text(
                    text = "📊 " + stringResource(Res.string.reports_filter_group_by),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

