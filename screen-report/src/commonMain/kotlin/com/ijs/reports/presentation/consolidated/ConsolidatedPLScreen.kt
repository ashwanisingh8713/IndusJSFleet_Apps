package com.ijs.reports.presentation.consolidated

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.ijs.reports.domain.entity.ConsolidatedPL
import com.ijs.reports.domain.entity.PeriodBreakdown
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.COST_TYPES
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.Effect
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.GROUP_BY_OPTIONS
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.Intent
import com.ijs.reports.presentation.consolidated.ConsolidatedPLContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Consolidated P&L Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsolidatedPLScreen(
    viewModel: ConsolidatedPLViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consolidated Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
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
                    Text("Generate Report")
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
                            text = "${state.groupBy.replaceFirstChar { it.uppercaseChar() }}ly Breakdown",
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
                            text = "Vehicle Performance",
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range
            Text(
                text = "📅 Date Range *",
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
                    label = "From",
                    placeholder = "DD-MM-YYYY",
                    visualTransformation = dateVisualTransformation,
                    modifier = Modifier.weight(1f)
                )
                FleetInputField(
                    value = endDate,
                    onValueChange = { onEndDateChange(filterDigitsOnly(it, 8)) },
                    fieldType = FieldType.NUMBER,
                    label = "To",
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
                        text = "🚚 Vehicles (${selectedVehicleIds.size}/${vehicles.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onSelectAllVehicles, contentPadding = PaddingValues(4.dp)) {
                            Text("All", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = onClearVehicles, contentPadding = PaddingValues(4.dp)) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall)
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
                                label = { Text(vehicle.registrationNumber, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (selectedVehicleIds.contains(vehicle.id)) {
                                    { Icon(painterResource(Res.drawable.ic_check), null, Modifier.size(14.dp)) }
                                } else null
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
                        text = "💰 Cost Types (${selectedCostTypes.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = onSelectAllCostTypes, contentPadding = PaddingValues(4.dp)) {
                            Text("All", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = onClearCostTypes, contentPadding = PaddingValues(4.dp)) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(COST_TYPES) { costType ->
                        val displayName = costType.replace("_", " ").split(" ").joinToString(" ") {
                            it.replaceFirstChar { c -> c.uppercaseChar() }
                        }
                        FilterChip(
                            selected = selectedCostTypes.contains(costType),
                            onClick = { onToggleCostType(costType) },
                            label = { Text(displayName, style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = if (selectedCostTypes.contains(costType)) {
                                { Icon(painterResource(Res.drawable.ic_check), null, Modifier.size(14.dp)) }
                            } else null
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
                    text = "📊 Group By",
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

@Composable
private fun ConsolidatedSummaryCard(report: ConsolidatedPL) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (report.isProfitable)
                com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
            else
                com.indusjs.uicomponents.theme.FleetStatusColors.LossRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "CONSOLIDATED SUMMARY",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn(label = "Total Revenue", value = formatCurrency(report.totalRevenue))
                SummaryColumn(label = "Total Expenses", value = formatCurrency(report.totalExpenses), alignment = Alignment.End)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NET ${if (report.isProfitable) "PROFIT" else "LOSS"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (report.isProfitable) "▲" else "▼"} ${formatCurrency(kotlin.math.abs(report.netProfit))}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (report.isProfitable) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                    )
                    if (report.profitMargin > 0) {
                        Text(
                            text = "Margin: ${formatPercentage(report.profitMargin)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn(value = "${report.totalVehicles}", label = "Vehicles")
                StatColumn(value = "${report.totalTrips}", label = "Total Trips")
                StatColumn(value = "${report.completedTrips}", label = "Completed")
            }
        }
    }
}

@Composable
private fun SummaryColumn(label: String, value: String, alignment: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PeriodBreakdownCard(period: PeriodBreakdown) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = period.label ?: period.period,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${period.tripCount} trips",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (period.isProfitable) "▲" else "▼"} ${formatCurrency(kotlin.math.abs(period.profit))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (period.isProfitable) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                )
                Text(
                    text = "Rev: ${formatCurrency(period.revenue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VehicleSummaryCard(
    vehicleNumber: String,
    tripCount: Int,
    revenue: Double,
    expenses: Double,
    profit: Double,
    isProfitable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = vehicleNumber,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$tripCount trips",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${if (isProfitable) "▲" else "▼"} ${formatCurrency(kotlin.math.abs(profit))}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isProfitable) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen else com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Revenue: ${formatCurrency(revenue)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Expenses: ${formatCurrency(expenses)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


