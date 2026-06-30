package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.indusjs.pdfreport.handler.VehicleMaintenanceCostsPdfHandler
import com.indusjs.pdfreport.model.VehicleMaintenanceCostsPdfData
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetConfirmationDialog
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.components.formatToDdMmYyyy
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CostsTabContent(
    state: VehicleDetailContract.State,
    viewModel: VehicleDetailViewModel,
    modifier: Modifier = Modifier
) {
    // Temp filter values for bottom sheet
    var tempStartDate by remember { mutableStateOf(state.costsStartDate.replace("-", "")) }
    var tempEndDate by remember { mutableStateOf(state.costsEndDate.replace("-", "")) }
    var tempSelectedFilters by remember { mutableStateOf(state.selectedCostTypeFilters) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sync temp values when sheet opens
    LaunchedEffect(state.showCostsFilterSheet) {
        if (state.showCostsFilterSheet) {
            tempStartDate = state.costsStartDate.replace("-", "")
            tempEndDate = state.costsEndDate.replace("-", "")
            tempSelectedFilters = state.selectedCostTypeFilters
            // Load cost types from local database when filter sheet opens
            viewModel.sendIntent(VehicleDetailContract.Intent.LoadCostTypes)
        }
    }

    // Load costs on first composition
    LaunchedEffect(Unit) {
        // Only check maintenance costs since Vehicle Costs tab shows only maintenance costs
        if (state.maintenanceCosts.isEmpty() && !state.isLoadingCosts) {
            viewModel.sendIntent(VehicleDetailContract.Intent.LoadCosts)
        }
    }

    // Filter Bottom Sheet
    if (state.showCostsFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.sendIntent(VehicleDetailContract.Intent.HideCostsFilterSheet) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.vehicle_costs_filters), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        tempStartDate = ""
                        tempEndDate = ""
                        tempSelectedFilters = emptySet()
                    }) {
                        Text(stringResource(Res.string.action_clear_all))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date Range Section
                Text(stringResource(Res.string.vehicle_costs_date_range), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                val dateTransformation = remember { DateVisualTransformation() }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FleetInputField(
                        value = tempStartDate,
                        onValueChange = { tempStartDate = filterDigitsOnly(it, 8) },
                        fieldType = FieldType.NUMBER,
                        label = stringResource(Res.string.vehicle_label_date_from),
                        placeholder = stringResource(Res.string.placeholder_date),
                        visualTransformation = dateTransformation,
                        modifier = Modifier.weight(1f)
                    )
                    FleetInputField(
                        value = tempEndDate,
                        onValueChange = { tempEndDate = filterDigitsOnly(it, 8) },
                        fieldType = FieldType.NUMBER,
                        label = stringResource(Res.string.vehicle_label_date_to),
                        placeholder = stringResource(Res.string.placeholder_date),
                        visualTransformation = dateTransformation,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cost Types Section (Multi-select)
                Text(stringResource(Res.string.vehicle_costs_cost_types), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(stringResource(Res.string.vehicle_costs_select_types), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Loading indicator for cost types
                if (state.isLoadingCostTypes) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    // Trip Cost Types - from local database
                    if (state.tripCostTypeGroups.isNotEmpty()) {
                        Text(stringResource(Res.string.vehicle_costs_trip_costs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))

                        state.tripCostTypeGroups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FleetFilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        label = item.label,
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        // Fallback to static cost types from TripCostTypes object
                        Text(stringResource(Res.string.vehicle_costs_trip_costs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        com.indusjs.fleet.data.model.costs.TripCostTypes.groups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FleetFilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        label = item.label,
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Maintenance Cost Types - from local database
                    if (state.maintenanceCostTypeGroups.isNotEmpty()) {
                        Text(stringResource(Res.string.vehicle_costs_maintenance_costs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))

                        state.maintenanceCostTypeGroups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FleetFilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        label = item.label,
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        // Fallback to static cost types from MaintenanceCostTypes object
                        Text(stringResource(Res.string.vehicle_costs_maintenance_costs), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        com.indusjs.fleet.data.model.costs.MaintenanceCostTypes.groups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FleetFilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        label = item.label,
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.HideCostsFilterSheet) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                    Button(
                        onClick = {
                            viewModel.sendIntent(
                                VehicleDetailContract.Intent.ApplyCostFilters(formatToDdMmYyyy(tempStartDate), formatToDdMmYyyy(tempEndDate), tempSelectedFilters)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Res.string.vehicle_costs_apply_filters))
                    }
                }
            }
        }
    }

    // Show only Maintenance Costs (Vehicle Maintenance Costs per costs-README.md)
    // Trip costs are shown in Trip Detail screen, not here
    val allCosts = state.maintenanceCosts.map { CostDisplayItem.fromMaintenanceCost(it) }
        .sortedByDescending { it.date ?: 0L }
    val groupedByDate = allCosts.groupBy { it.dateLabel }
    val activeFilterCount = state.selectedCostTypeFilters.size +
        (if (state.costsStartDate.isNotBlank() || state.costsEndDate.isNotBlank()) 1 else 0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Summary Header with Filter Button and Add Cost Button
        item(key = "header") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.vehicle_costs_maintenance_costs),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "₹${formatAmount(state.maintenanceCostsTotalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = stringResource(Res.string.vehicle_costs_entries, state.maintenanceCosts.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Export Button
                        if (state.maintenanceCosts.isNotEmpty()) {
                            FilledTonalIconButton(
                                onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ExportMaintenanceCostsPdf) }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_download),
                                    contentDescription = stringResource(Res.string.cd_export_pdf),
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            }
                        }

                        // Add Cost Button
                        FilledTonalButton(
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.NavigateToAddMaintenanceCost) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(stringResource(Res.string.vehicle_costs_add_cost), style = MaterialTheme.typography.labelMedium)
                        }

                        // Filter Button with Badge
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge { Text("$activeFilterCount") }
                                }
                            }
                        ) {
                            FilledTonalIconButton(
                                onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ShowCostsFilterSheet) }
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_filter),
                                    contentDescription = stringResource(Res.string.vehicle_costs_filters),
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Filters Chips (dismissable)
        if (activeFilterCount > 0) {
            item(key = "active_filters") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.selectedCostTypeFilters.forEach { filter ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ToggleCostTypeFilter(filter)) },
                            label = { Text(getCostTypeLabel(filter), style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_close),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            }
                        )
                    }
                    if (state.costsStartDate.isNotBlank() || state.costsEndDate.isNotBlank()) {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateCostsDateRange("", "")) },
                            label = {
                                Text(
                                    "${state.costsStartDate.ifBlank { "..." }} → ${state.costsEndDate.ifBlank { "..." }}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_close),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Loading
        if (state.isLoadingCosts && allCosts.isEmpty()) {
            item(key = "loading") {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }

        // Error — inline errorContainer-tinted banner (in-list), not the full-screen ErrorContent.
        state.costsError?.let { error ->
            item(key = "error") {
                FleetInlineErrorBanner(
                    message = error.resolve(),
                    actionLabel = stringResource(Res.string.retry),
                    onAction = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshCosts) }
                )
            }
        }

        // Empty State - Updated for maintenance costs only
        if (allCosts.isEmpty() && !state.isLoadingCosts && state.costsError == null) {
            item(key = "empty") {
                EmptyContent(
                    iconRes = Res.drawable.ic_settings,
                    title = stringResource(Res.string.vehicle_costs_no_maintenance),
                    message = stringResource(Res.string.vehicle_costs_maint_hint),
                    actionLabel = stringResource(Res.string.vehicle_costs_add_maint),
                    onAction = { viewModel.sendIntent(VehicleDetailContract.Intent.NavigateToAddMaintenanceCost) },
                    fillMaxSize = false
                )
            }
        }

        // Grouped Costs by Date
        groupedByDate.forEach { (dateLabel, costs) ->
            item(key = "date_$dateLabel") {
                val dateTotal = costs.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_calendar),
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp).size(FleetTokens.IconSize.S),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(" (${costs.size})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("₹${formatAmount(dateTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
            }
            costs.forEach { cost ->
                item(key = "cost_${cost.category}_${cost.id}") {
                    CompactCostCard(cost = cost, onDelete = { viewModel.sendIntent(VehicleDetailContract.Intent.DeleteCost(cost.id, cost.category)) })
                }
            }
        }

        // Load More
        if (state.hasMoreCosts) {
            item(key = "load_more") {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (state.isLoadingCosts) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else TextButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.LoadMoreCosts) }) { Text(stringResource(Res.string.vehicle_costs_load_more)) }
                }
            }
        }
    }

    // Delete Dialog
    FleetConfirmationDialog(
        showDialog = state.showDeleteCostDialog,
        title = stringResource(Res.string.vehicle_costs_delete_title),
        message = stringResource(Res.string.vehicle_costs_delete_message),
        confirmText = stringResource(Res.string.delete),
        isDestructive = true,
        onConfirm = { viewModel.sendIntent(VehicleDetailContract.Intent.ConfirmDeleteCost) },
        onDismiss = { viewModel.sendIntent(VehicleDetailContract.Intent.DismissDeleteCostDialog) }
    )
}

