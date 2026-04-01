package com.ijs.reports.presentation.vehicle

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.reports.domain.entity.VehicleProfitLoss
import com.ijs.reports.presentation.PLStatusFilter
import com.ijs.reports.presentation.RecentReport
import com.ijs.reports.presentation.VehiclePLSortOption
import com.ijs.vehicle.domain.entity.Vehicle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun VehiclePLContent(
    state: VehiclePLContract.State,
    onShowVehicleSelector: () -> Unit,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onGenerateReport: () -> Unit,
    onQuickReport: (RecentReport) -> Unit,
    onSortChange: (VehiclePLSortOption) -> Unit,
    onFilterChange: (PLStatusFilter) -> Unit
) {
    val vehicleFallback = stringResource(Res.string.vehicle_pl_vehicle_fallback)
    // Determine current step
    val currentStep = when {
        state.result != null -> 3
        state.selectedVehicleId != null -> 2
        else -> 1
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Step Indicator - only show when not viewing results
        if (state.result == null) {
            WizardStepIndicator(
                currentStep = currentStep,
                steps = listOf("Select Vehicle", "Select Period", "View Report")
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        when {
            // ========== STEP 3: Results ==========
            state.result != null -> {
                // Result Header with vehicle info and New Report button
                ResultHeaderCard(
                    vehicleNumber = state.result!!.vehicleNumber ?: "Vehicle",
                    vehicleMakeModel = "${state.selectedVehicle?.make ?: ""} ${state.selectedVehicle?.model ?: ""}".trim(),
                    period = state.result!!.period ?: state.period,
                    onNewReport = onShowVehicleSelector
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { PLResultKPICard(result = state.result!!) }

                    if (state.result!!.costBreakdown.isNotEmpty()) {
                        item { PLCostBreakdownCard(costs = state.result!!.costBreakdown) }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onShowVehicleSelector,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("🔄 New Report")
                            }
                            Button(
                                onClick = { /* TODO: Export */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("📄 Export PDF")
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }

            // ========== STEP 2: Period Selection ==========
            state.selectedVehicleId != null -> {
                // Selected Vehicle Card with Change button
                SelectedVehicleDisplayCard(
                    vehicle = state.selectedVehicle!!,
                    onChangeVehicle = onShowVehicleSelector
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Period selection title
                    Text(
                        text = stringResource(Res.string.vehicle_pl_select_report_period),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Period selection card
                PeriodSelectionChipsCard(
                    selectedPeriod = state.period,
                    useCustomDateRange = state.useCustomDateRange,
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onPeriodChange = onPeriodChange,
                    onStartDateChange = onStartDateChange,
                    onEndDateChange = onEndDateChange
                )

                Spacer(modifier = Modifier.weight(1f))

                // Error message
                if (state.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.error ?: "Something went wrong",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Generate Report Button
                Button(
                    onClick = onGenerateReport,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.canGenerateReport,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Generate Report",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ========== STEP 1: Vehicle Selection ==========
            else -> {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = stringResource(Res.string.vehicle_pl_report_label), style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Vehicle Profit & Loss",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(Res.string.vehicle_pl_analyze_performance),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onShowVehicleSelector,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🚛")
                                Text(
                                    text = stringResource(Res.string.vehicle_pl_select_vehicle),
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (state.vehicles.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${state.vehicles.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Recent Reports
                if (state.recentReports.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Recent Reports",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(state.recentReports) { report ->
                            RecentReportCard(report = report, onClick = { onQuickReport(report) })
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f)
                )
            }
        }

        // Multi-Vehicle Results
        if (state.multiResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MultiVehicleSummaryCard(
                        totalVehicles = state.multiResults.size,
                        profitableCount = state.totalProfitableCount,
                        lossMakingCount = state.totalLossMakingCount,
                        totalRevenue = state.totalRevenue,
                        totalExpenses = state.totalExpenses,
                        totalNetProfit = state.totalNetProfit
                    )
                }
                item {
                    SortingFilterSection(
                        sortOption = state.sortOption,
                        plStatusFilter = state.plStatusFilter,
                        resultCount = state.sortedFilteredResults.size,
                        onSortChange = onSortChange,
                        onFilterChange = onFilterChange
                    )
                }
                items(state.sortedFilteredResults, key = { it.vehicleId }) { result ->
                    VehiclePLResultCard(result)
                }
            }
        }
    }
}

// ============================================================================
// Wizard Step Indicator
// ============================================================================


@Composable
internal fun WizardStepIndicator(
    currentStep: Int,
    steps: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Step circle
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = when {
                        isCompleted -> com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "$stepNumber",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary
                                       else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Step label (only show for current on small screens)
                if (isCurrent) {
                    Text(
                        text = step,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                // Connector line (except for last step)
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .padding(horizontal = 4.dp)
                            .background(
                                if (isCompleted) com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }
    }
}

// ============================================================================
// Selected Vehicle Display Card - Clear with Change button
// ============================================================================


@Composable
internal fun SelectedVehicleDisplayCard(
    vehicle: Vehicle,
    onChangeVehicle: () -> Unit
) {
    val vehicleFallback = stringResource(Res.string.vehicle_pl_vehicle_fallback)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Vehicle Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehicle.registrationNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim()
                        .ifEmpty { vehicleFallback },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Change Vehicle Button
            OutlinedButton(
                onClick = onChangeVehicle,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(Res.string.change),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ============================================================================
// Period Selection Card with Chips
// ============================================================================


@Composable
internal fun PeriodSelectionChipsCard(
    selectedPeriod: String,
    useCustomDateRange: Boolean,
    startDate: String,
    endDate: String,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Period chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "weekly" to "Week",
                    "monthly" to "Month",
                    "yearly" to "Year",
                    "custom" to "Custom"
                ).forEach { (key, label) ->
                    val isSelected = selectedPeriod == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPeriodChange(key) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Custom date range using ijs-datetime-picker
            AnimatedVisibility(visible = useCustomDateRange) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FleetDateTimePicker(
                        date = startDate,
                        time = "",
                        onDateTimeChange = { newDate, _ -> onStartDateChange(newDate) },
                        label = "From Date",
                        mode = PickerMode.DATE_ONLY,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FleetDateTimePicker(
                        date = endDate,
                        time = "",
                        onDateTimeChange = { newDate, _ -> onEndDateChange(newDate) },
                        label = "To Date",
                        mode = PickerMode.DATE_ONLY,
                        minDate = startDate.ifBlank { null },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ============================================================================
// Result Header Card - Shows vehicle info and New Report action
// ============================================================================


