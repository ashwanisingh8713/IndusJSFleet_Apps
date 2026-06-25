package com.ijs.vehicle.presentation.costs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.CostTypeGroup
import com.indusjs.uicomponents.components.CostTypeSelection
import com.indusjs.uicomponents.components.CostTypeTwoLevelSelector
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetFormSection
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private fun applySnackbarFormat(template: String, vararg args: Any): String {
    var result = template
    args.forEachIndexed { index, arg ->
        val n = index + 1
        result = result.replace("%${n}\$s", arg.toString()).replace("%${n}\$d", arg.toString())
    }
    return result
}

/**
 * Maintenance Cost Entry Screen with multi-row support.
 *
 * @param initialVehicleId Optional vehicle ID to pre-select when navigating from Vehicle Detail
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceCostEntryScreen(
    viewModel: MaintenanceCostEntryViewModel,
    initialVehicleId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val maintCostsSavedFmt = stringResource(Res.string.vehicle_maint_costs_saved)
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Pre-select vehicle if initialVehicleId is provided
    LaunchedEffect(initialVehicleId) {
        if (!initialVehicleId.isNullOrBlank()) {
            viewModel.sendIntent(MaintenanceCostEntryContract.Intent.PreSelectVehicleById(initialVehicleId))
        }
    }

    // Handle side effects
    LaunchedEffect(maintCostsSavedFmt) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MaintenanceCostEntryContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is MaintenanceCostEntryContract.Effect.ShowError -> {
                    pendingSnackbar = effect.message
                }
                is MaintenanceCostEntryContract.Effect.NavigateBack -> onNavigateBack()
                is MaintenanceCostEntryContract.Effect.CostsSaved -> {
                    snackbarHostState.showSnackbar(applySnackbarFormat(maintCostsSavedFmt, effect.count))
                }
            }
        }
    }

    // History dialog
    if (state.showHistoryDialog) {
        MaintenanceHistoryDialog(
            costs = state.costHistory,
            isLoading = state.isLoadingHistory,
            vehicleInfo = state.selectedVehicle?.registrationNumber ?: "",
            onDismiss = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.HideHistory) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.vehicle_maint_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.NavigateBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                actions = {
                    // Refresh cost types button
                    IconButton(
                        onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.RefreshCostTypes) },
                        enabled = !state.isRefreshingCostTypes
                    ) {
                        if (state.isRefreshingCostTypes) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(FleetTokens.IconSize.M),
                                strokeWidth = FleetTokens.Height.ProgressStroke
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.cd_refresh_cost_types),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
                            )
                        }
                    }
                    // History button
                    IconButton(
                        onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.ShowHistory) },
                        enabled = state.selectedVehicle != null
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_history),
                            contentDescription = stringResource(Res.string.cd_history),
                            tint = if (state.selectedVehicle != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoadingData) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val bp = rememberFleetBreakpoint()
                // Forms read best centered with a capped width on Medium/Expanded.
                val contentWidthModifier = if (bp == FleetBreakpoint.Compact) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier.widthIn(max = FleetTokens.Width.MaxContent)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(contentWidthModifier)
                        .align(Alignment.TopCenter)
                        .padding(FleetTokens.Spacing.L),
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                ) {
                // Section 1: Vehicle Selection
                item {
                    FleetFormSection(title = stringResource(Res.string.maint_section_select_vehicle)) {
                        FleetDropdown(
                            label = stringResource(Res.string.maint_label_vehicle),
                            options = state.vehicles.map {
                                DropdownOption(
                                    id = it.id,
                                    label = "${it.registrationNumber} - ${it.make} ${it.model}"
                                )
                            },
                            selectedOptionId = state.selectedVehicle?.id,
                            onOptionSelected = { vehicleId ->
                                state.vehicles.find { it.id == vehicleId }?.let { vehicle ->
                                    viewModel.sendIntent(MaintenanceCostEntryContract.Intent.SelectVehicle(vehicle))
                                }
                            },
                            placeholder = stringResource(Res.string.maint_placeholder_vehicle),
                            isError = state.vehicleError != null,
                            errorMessage = state.vehicleError?.resolve()
                        )
                    }
                }

                // Section 2: Cost Entries (Multi-row)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.vehicle_maint_costs_section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        FleetButton(
                            text = stringResource(Res.string.maint_btn_add_row),
                            onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.AddCostRow) },
                            variant = ButtonVariant.SECONDARY,
                            size = ButtonSize.SMALL,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_add),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            }
                        )
                    }
                }

                // Cost Entry Rows
                itemsIndexed(state.costEntries, key = { _, row -> row.id }) { index, row ->
                    MaintenanceCostRowCard(
                        row = row,
                        rowNumber = index + 1,
                        costTypeGroups = state.costTypeGroups,
                        canDelete = state.costEntries.size > 1,
                        minDate = com.indusjs.datetimeutils.FleetDateTime.getMinDateForMaintenance(
                            state.selectedVehicle?.createdAt
                        ),
                        maxDate = com.indusjs.datetimeutils.FleetDateTime.getTomorrowDate(),
                        onToggleExpanded = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.ToggleRowExpanded(row.id)) },
                        onDelete = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.RemoveCostRow(row.id)) },
                        onSelectCostType = { selection ->
                            viewModel.sendIntent(MaintenanceCostEntryContract.Intent.SelectCostType(row.id, selection))
                        },
                        onCategoryChanged = { groupId, groupName ->
                            viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateSelectedCategory(row.id, groupId, groupName))
                        },
                        onUpdateDate = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateDate(row.id, it)) },
                        onUpdateTime = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateTime(row.id, it)) },
                        onUpdateAmount = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateAmount(row.id, it)) },
                        onUpdateDescription = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateDescription(row.id, it)) },
                        onUpdateNotes = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateNotes(row.id, it)) },
                        onUpdateVendorName = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateVendorName(row.id, it)) },
                        onUpdateInvoiceNo = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateInvoiceNo(row.id, it)) },
                        onUpdateFuelQuantity = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateFuelQuantity(row.id, it)) },
                        onUpdateFuelRate = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateFuelRate(row.id, it)) },
                        onUpdateKmPerLiter = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.UpdateKmPerLiter(row.id, it)) }
                    )
                }

                // Summary
                if (state.costEntries.any { it.isValid }) {
                    item {
                        val validCount = state.costEntries.count { it.isValid }
                        val totalAmount = state.costEntries
                            .filter { it.isValid }
                            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }

                        FleetSectionCard(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            border = null,
                            elevation = FleetTokens.Elevation.None,
                            contentPadding = FleetTokens.Spacing.L
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(Res.string.vehicle_maint_summary),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = if (validCount == 1) {
                                            stringResource(Res.string.vehicle_maint_valid_entry_one)
                                        } else {
                                            stringResource(Res.string.vehicle_maint_valid_entries_count, validCount)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = stringResource(Res.string.vehicle_maint_total_amount),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                    Text(
                                        text = "₹${totalAmount.toLong()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    FleetButton(
                        text = stringResource(Res.string.vehicle_maint_save_button),
                        onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.SaveCosts) },
                        variant = ButtonVariant.PRIMARY,
                        size = ButtonSize.LARGE,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.canSave,
                        isLoading = state.isSaving
                    )
                }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                    }
                }
            }
        }
    }
}

/**
 * Individual maintenance cost entry row card.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceCostRowCard(
    row: MaintenanceCostRow,
    rowNumber: Int,
    costTypeGroups: List<CostTypeGroup>,
    canDelete: Boolean,
    minDate: String? = null,  // Vehicle creation date
    maxDate: String? = null,  // Tomorrow's date
    onToggleExpanded: () -> Unit,
    onDelete: () -> Unit,
    onSelectCostType: (CostTypeSelection) -> Unit,
    onCategoryChanged: (groupId: String, groupName: String) -> Unit,
    onUpdateDate: (String) -> Unit,
    onUpdateTime: (String) -> Unit,
    onUpdateAmount: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onUpdateVendorName: (String) -> Unit,
    onUpdateInvoiceNo: (String) -> Unit,
    onUpdateFuelQuantity: (String) -> Unit,
    onUpdateFuelRate: (String) -> Unit,
    onUpdateKmPerLiter: (String) -> Unit
) {
    FleetSectionCard(
        elevation = FleetTokens.Elevation.Raised,
        contentPadding = FleetTokens.Spacing.M
    ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Row number badge
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.S),
                        color = if (row.isValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Text(
                            text = "#$rowNumber",
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (row.isValid)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    Text(
                        text = row.costTypeDisplayLabel.ifBlank { stringResource(Res.string.vehicle_maint_new_entry) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (row.amount.isNotBlank()) {
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                        Text(
                            text = "₹${row.amount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row {
                    // Expand/Collapse button
                    IconButton(onClick = onToggleExpanded, modifier = Modifier.size(FleetTokens.IconSize.L)) {
                        Icon(
                            painter = painterResource(
                                if (row.isExpanded) Res.drawable.ic_chevron_right
                                else Res.drawable.ic_chevron_right
                            ),
                            contentDescription = if (row.isExpanded) {
                                stringResource(Res.string.cd_collapse)
                            } else {
                                stringResource(Res.string.cd_expand)
                            },
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    }
                    // Delete button
                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(FleetTokens.IconSize.L)) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = stringResource(Res.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(FleetTokens.IconSize.M)
                            )
                        }
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = row.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = FleetTokens.Spacing.M),
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    // Cost Type two-level selector (category dropdown + chip items)
                    CostTypeTwoLevelSelector(
                        groups = costTypeGroups,
                        selectedCostType = row.costType,
                        onCostTypeSelected = { selection ->
                            onSelectCostType(selection)
                        },
                        onCategoryChanged = { groupId, groupName ->
                            onCategoryChanged(groupId, groupName)
                        },
                        isError = row.costTypeError != null,
                        errorMessage = row.costTypeError
                    )

                    // Fuel Details Section - shown immediately when Fuel & Energy category is selected
                    AnimatedVisibility(
                        visible = row.isFuelCostType,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        FleetSectionCard(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            border = null,
                            elevation = FleetTokens.Elevation.None,
                            contentPadding = FleetTokens.Spacing.M
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                            ) {
                                Text(
                                    text = stringResource(Res.string.trip_cost_section_fuel),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Fuel Type - read-only, auto-populated from selected Cost Type
                                FleetInputField(
                                    value = row.costTypeLabel.ifBlank { stringResource(Res.string.trip_cost_placeholder_fuel_type) },
                                    onValueChange = {},
                                    fieldType = FieldType.DEFAULT,
                                    label = stringResource(Res.string.trip_cost_label_fuel_type),
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false
                                )

                                // Fuel Quantity & Rate in a row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                                ) {
                                    FleetInputField(
                                        value = row.fuelQuantity,
                                        onValueChange = onUpdateFuelQuantity,
                                        fieldType = FieldType.DECIMAL,
                                        label = stringResource(Res.string.trip_cost_label_fuel_quantity),
                                        placeholder = stringResource(Res.string.trip_cost_placeholder_fuel_quantity),
                                        modifier = Modifier.weight(1f)
                                    )

                                    FleetInputField(
                                        value = row.fuelRate,
                                        onValueChange = onUpdateFuelRate,
                                        fieldType = FieldType.DECIMAL,
                                        label = stringResource(Res.string.trip_cost_label_fuel_rate),
                                        placeholder = stringResource(Res.string.trip_cost_placeholder_fuel_rate),
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                // Km per Liter
                                FleetInputField(
                                    value = row.kmPerLiter,
                                    onValueChange = onUpdateKmPerLiter,
                                    fieldType = FieldType.DECIMAL,
                                    label = stringResource(Res.string.trip_cost_label_mileage),
                                    placeholder = stringResource(Res.string.trip_cost_placeholder_mileage),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Calculated total
                                val quantity = row.fuelQuantity.toDoubleOrNull() ?: 0.0
                                val rate = row.fuelRate.toDoubleOrNull() ?: 0.0
                                if (quantity > 0 && rate > 0) {
                                    val calculatedTotal = quantity * rate
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(FleetTokens.Radius.M),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(FleetTokens.Spacing.M),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(Res.string.trip_cost_calculated_total), fontWeight = FontWeight.Medium)
                                            Text(
                                                "₹${((calculatedTotal * 100).toLong() / 100.0)}",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Date & Time picker (unified) with date constraints
                    FleetDateTimePicker(
                        date = row.date,
                        time = row.time,
                        onDateTimeChange = { newDate, newTime ->
                            onUpdateDate(newDate)
                            onUpdateTime(newTime)
                        },
                        label = stringResource(Res.string.trip_cost_label_date_time),
                        isError = row.dateError != null,
                        errorMessage = row.dateError,
                        minDate = minDate,
                        maxDate = maxDate
                    )

                    // Amount — validated via ValidationUtils.validateAmount; inline error
                    // surfaces the VM's amountError (format) or the local positive-amount rule.
                    // Either error feeds row.isValid -> state.canSave, gating the submit button.
                    val amountValidationError = ValidationUtils.validateAmount(row.amount).errorMessage
                    val amountErrorText: String? = row.amountError?.resolve()
                        ?: amountValidationError?.takeIf { row.amount.isNotBlank() }
                    FleetInputField(
                        value = row.amount,
                        onValueChange = onUpdateAmount,
                        fieldType = FieldType.DECIMAL,
                        label = stringResource(Res.string.trip_cost_label_amount),
                        placeholder = stringResource(Res.string.trip_cost_placeholder_amount),
                        modifier = Modifier.fillMaxWidth(),
                        isError = amountErrorText != null,
                        errorMessage = amountErrorText,
                        leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) }
                    )

                    // Description
                    FleetInputField(
                        value = row.description,
                        onValueChange = onUpdateDescription,
                        fieldType = FieldType.NOTES,
                        label = stringResource(Res.string.label_description),
                        placeholder = stringResource(Res.string.vehicle_maint_describe_work),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Optional fields section
                    Text(
                        text = stringResource(Res.string.vehicle_maint_vendor_section),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        FleetInputField(
                            value = row.vendorName,
                            onValueChange = onUpdateVendorName,
                            fieldType = FieldType.DEFAULT,
                            label = stringResource(Res.string.vehicle_maint_vendor),
                            placeholder = stringResource(Res.string.vehicle_maint_vendor_name_placeholder),
                            modifier = Modifier.weight(1f)
                        )

                        FleetInputField(
                            value = row.invoiceNo,
                            onValueChange = onUpdateInvoiceNo,
                            fieldType = FieldType.DEFAULT,
                            label = stringResource(Res.string.vehicle_maint_invoice),
                            placeholder = stringResource(Res.string.vehicle_maint_invoice_placeholder),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    FleetInputField(
                        value = row.notes,
                        onValueChange = onUpdateNotes,
                        fieldType = FieldType.NOTES,
                        label = stringResource(Res.string.label_notes),
                        placeholder = stringResource(Res.string.vehicle_maint_notes_placeholder),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
    }
}


@Composable
private fun MaintenanceHistoryDialog(
    costs: List<MaintenanceCostDto>,
    isLoading: Boolean,
    vehicleInfo: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(FleetTokens.Radius.XL),
        title = {
            Column {
                Text(stringResource(Res.string.vehicle_maint_history_title))
                if (vehicleInfo.isNotEmpty()) {
                    Text(
                        text = vehicleInfo,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (costs.isEmpty()) {
                    Text(
                        stringResource(Res.string.vehicle_maint_no_records),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                    ) {
                        items(costs) { cost ->
                            MaintenanceHistoryItem(cost)
                        }
                        item {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = FleetTokens.Spacing.S),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(Res.string.vehicle_maint_total_label), fontWeight = FontWeight.Bold)
                                Text(
                                    "₹${costs.sumOf { it.amount }.toLong()}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.close))
            }
        }
    )
}

@Composable
private fun MaintenanceHistoryItem(cost: MaintenanceCostDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FleetTokens.Radius.M),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(FleetTokens.Spacing.M),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cost.costType.replace("_", " ").replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${cost.date?.takeIf { it > 0L }?.let { com.indusjs.fleet.core.util.formatDateToHumanReadable(it) } ?: ""} ${cost.time ?: ""}".trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val costDescription = cost.description
                if (!costDescription.isNullOrBlank()) {
                    Text(
                        text = costDescription,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!cost.vendorName.isNullOrBlank()) {
                    Text(
                        text = stringResource(Res.string.vehicle_maint_vendor_prefix, cost.vendorName.orEmpty()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "₹${cost.amount.toLong()}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
