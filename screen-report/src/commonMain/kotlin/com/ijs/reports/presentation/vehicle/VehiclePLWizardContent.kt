package com.ijs.reports.presentation.vehicle

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
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

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = rememberFleetBreakpoint().isExpanded
        val contentModifier = if (isWide) {
            Modifier
                .fillMaxSize()
                .widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxSize()
        }

        Column(
            modifier = contentModifier
                .padding(FleetTokens.Spacing.L)
        ) {
        // Step Indicator - only show when not viewing results
        if (state.result == null) {
            WizardStepIndicator(
                currentStep = currentStep,
                steps = listOf(
                    stringResource(Res.string.reports_wizard_step_vehicle),
                    stringResource(Res.string.reports_wizard_step_period),
                    stringResource(Res.string.reports_wizard_step_view)
                )
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
        }

        when {
            // ========== STEP 3: Results ==========
            state.result != null -> {
                // Result Header with vehicle info and New Report button
                ResultHeaderCard(
                    vehicleNumber = state.result!!.vehicleNumber ?: stringResource(Res.string.reports_vehicle_fallback),
                    vehicleMakeModel = "${state.selectedVehicle?.make ?: ""} ${state.selectedVehicle?.model ?: ""}".trim(),
                    period = state.result!!.period ?: state.period,
                    onNewReport = onShowVehicleSelector
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    item { PLResultKPICard(result = state.result!!) }

                    if (state.result!!.costBreakdown.isNotEmpty()) {
                        item { PLCostBreakdownCard(costs = state.result!!.costBreakdown) }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                        ) {
                            FleetButton(
                                text = stringResource(Res.string.reports_new_report),
                                onClick = onShowVehicleSelector,
                                variant = ButtonVariant.SECONDARY,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_refresh),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(FleetTokens.IconSize.S)
                                    )
                                }
                            )
                            FleetButton(
                                text = stringResource(Res.string.reports_export_pdf),
                                onClick = { /* TODO: Export */ },
                                variant = ButtonVariant.PRIMARY,
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_download),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(FleetTokens.IconSize.S)
                                    )
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.L)) }
                }
            }

            // ========== STEP 2: Period Selection ==========
            state.selectedVehicleId != null -> {
                // Selected Vehicle Card with Change button
                SelectedVehicleDisplayCard(
                    vehicle = state.selectedVehicle!!,
                    onChangeVehicle = onShowVehicleSelector
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                // Period selection title
                Text(
                    text = stringResource(Res.string.vehicle_pl_select_report_period),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

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
                    FleetSectionCard(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        border = null,
                        contentPadding = FleetTokens.Spacing.M
                    ) {
                        Text(
                            text = state.error?.resolve() ?: stringResource(Res.string.reports_error_generic),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                }

                // Generate Report Button (gated on required vehicle + period selection)
                FleetButton(
                    text = stringResource(Res.string.reports_generate_report),
                    onClick = onGenerateReport,
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canGenerateReport
                )
            }

            // ========== STEP 1: Vehicle Selection ==========
            else -> {
                // Welcome Card
                FleetSectionCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    border = null,
                    elevation = FleetTokens.Elevation.None,
                    contentPadding = FleetTokens.Spacing.XL
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_dashboard),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(FleetTokens.IconSize.XL)
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                        Text(
                            text = stringResource(Res.string.reports_vehicle_pl_header),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                        Text(
                            text = stringResource(Res.string.vehicle_pl_analyze_performance),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                        val selectVehicleLabel = stringResource(Res.string.vehicle_pl_select_vehicle)
                        val buttonLabel = if (state.vehicles.isNotEmpty()) {
                            "$selectVehicleLabel (${state.vehicles.size})"
                        } else {
                            selectVehicleLabel
                        }
                        FleetButton(
                            text = buttonLabel,
                            onClick = onShowVehicleSelector,
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_truck),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            }
                        )
                    }
                }

                // Recent Reports
                if (state.recentReports.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
                    Text(
                        text = stringResource(Res.string.reports_recent_reports),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
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
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
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
                    modifier = Modifier.size(FleetTokens.Height.StepCircle),
                    shape = CircleShape,
                    color = when {
                        isCompleted -> FleetStatusColors.ProfitGreen
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_check),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
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

                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

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
                            .height(FleetTokens.Height.Connector)
                            .padding(horizontal = FleetTokens.Spacing.XS)
                            .background(
                                if (isCompleted) FleetStatusColors.ProfitGreen
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
    FleetSectionCard(
        containerColor = FleetStatusColors.ProfitGreen.copy(alpha = 0.1f),
        border = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkmark
            Surface(
                modifier = Modifier.size(FleetTokens.IconSize.XL),
                shape = CircleShape,
                color = FleetStatusColors.ProfitGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            }

            Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))

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
            FleetButton(
                text = stringResource(Res.string.change),
                onClick = onChangeVehicle,
                variant = ButtonVariant.SECONDARY,
                size = ButtonSize.SMALL
            )
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
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        border = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            // Period chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
            ) {
                listOf(
                    "weekly" to stringResource(Res.string.reports_period_chip_week),
                    "monthly" to stringResource(Res.string.reports_period_chip_month),
                    "yearly" to stringResource(Res.string.reports_period_chip_year),
                    "custom" to stringResource(Res.string.reports_period_chip_custom)
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

            // Show selected custom date range label if applicable
            if (useCustomDateRange && startDate.isNotBlank() && endDate.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS),
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_calendar),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                        Text(
                            text = "$startDate — $endDate",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// Result Header Card - Shows vehicle info and New Report action
// ============================================================================


