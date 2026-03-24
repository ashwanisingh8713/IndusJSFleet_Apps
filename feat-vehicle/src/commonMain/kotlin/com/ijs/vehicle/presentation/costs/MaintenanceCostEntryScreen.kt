package com.ijs.vehicle.presentation.costs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.CostTypeGroup
import com.indusjs.uicomponents.components.CostTypeSelection
import com.indusjs.uicomponents.components.CostTypeTwoLevelSelector
import com.indusjs.uicomponents.components.FleetDropdownField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.fleet.data.model.costs.MaintenanceCostDto
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

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

    // Pre-select vehicle if initialVehicleId is provided
    LaunchedEffect(initialVehicleId) {
        if (!initialVehicleId.isNullOrBlank()) {
            viewModel.sendIntent(MaintenanceCostEntryContract.Intent.PreSelectVehicleById(initialVehicleId))
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is MaintenanceCostEntryContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MaintenanceCostEntryContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MaintenanceCostEntryContract.Effect.NavigateBack -> onNavigateBack()
                is MaintenanceCostEntryContract.Effect.CostsSaved -> {
                    snackbarHostState.showSnackbar("${effect.count} maintenance cost(s) saved successfully")
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
                title = { Text("Add Maintenance Costs") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.NavigateBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
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
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = "Refresh Cost Types",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
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
                            contentDescription = "History",
                            tint = if (state.selectedVehicle != null)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(24.dp)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Vehicle Selection
                item {
                    FleetSectionCard(title = "🚛 Select Vehicle") {
                        FleetDropdownField(
                            label = "Vehicle *",
                            value = state.selectedVehicle?.let { "${it.registrationNumber} - ${it.make} ${it.model}" } ?: "",
                            placeholder = "Select a vehicle",
                            isExpanded = state.showVehicleDropdown,
                            error = state.vehicleError,
                            onToggle = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.ToggleVehicleDropdown) },
                            onDismiss = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.ToggleVehicleDropdown) }
                        ) {
                            state.vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = { Text("${vehicle.registrationNumber} - ${vehicle.make} ${vehicle.model}") },
                                    onClick = {
                                        viewModel.sendIntent(MaintenanceCostEntryContract.Intent.SelectVehicle(vehicle))
                                    }
                                )
                            }
                        }
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
                            text = "🔧 Maintenance Costs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.AddCostRow) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = "Add",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Row")
                        }
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

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Summary",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$validCount valid entr${if (validCount == 1) "y" else "ies"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Amount",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${totalAmount.toLong()}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = { viewModel.sendIntent(MaintenanceCostEntryContract.Intent.SaveCosts) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = state.canSave,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("💾 Save Maintenance Costs", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                        shape = RoundedCornerShape(4.dp),
                        color = if (row.isValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outlineVariant
                    ) {
                        Text(
                            text = "#$rowNumber",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (row.isValid)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = row.costTypeDisplayLabel.ifBlank { "New Entry" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (row.amount.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
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
                    IconButton(onClick = onToggleExpanded, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painter = painterResource(
                                if (row.isExpanded) Res.drawable.ic_chevron_right
                                else Res.drawable.ic_chevron_right
                            ),
                            contentDescription = if (row.isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Delete button
                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
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
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⛽ Fuel Details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Fuel Type - read-only, auto-populated from selected Cost Type
                                OutlinedTextField(
                                    value = row.costTypeLabel.ifBlank { "Select a fuel type above" },
                                    onValueChange = {},
                                    label = { Text("Fuel Type") },
                                    modifier = Modifier.fillMaxWidth(),
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                // Fuel Quantity & Rate in a row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = row.fuelQuantity,
                                        onValueChange = onUpdateFuelQuantity,
                                        label = { Text("Quantity (L)") },
                                        placeholder = { Text("Liters") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )

                                    OutlinedTextField(
                                        value = row.fuelRate,
                                        onValueChange = onUpdateFuelRate,
                                        label = { Text("Rate (₹/L)") },
                                        placeholder = { Text("Per liter") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                // Km per Liter
                                OutlinedTextField(
                                    value = row.kmPerLiter,
                                    onValueChange = onUpdateKmPerLiter,
                                    label = { Text("Mileage (Km/L)") },
                                    placeholder = { Text("Km per liter") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                // Calculated total
                                val quantity = row.fuelQuantity.toDoubleOrNull() ?: 0.0
                                val rate = row.fuelRate.toDoubleOrNull() ?: 0.0
                                if (quantity > 0 && rate > 0) {
                                    val calculatedTotal = quantity * rate
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Calculated Total:", fontWeight = FontWeight.Medium)
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
                        label = "Date & Time *",
                        isError = row.dateError != null,
                        errorMessage = row.dateError,
                        minDate = minDate,
                        maxDate = maxDate
                    )

                    // Amount
                    OutlinedTextField(
                        value = row.amount,
                        onValueChange = onUpdateAmount,
                        label = { Text("Amount (₹) *") },
                        placeholder = { Text("Enter amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = row.amountError != null,
                        supportingText = row.amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) }
                    )

                    // Description
                    OutlinedTextField(
                        value = row.description,
                        onValueChange = onUpdateDescription,
                        label = { Text("Description") },
                        placeholder = { Text("Describe the maintenance work") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    // Optional fields section
                    Text(
                        text = "Vendor Details (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = row.vendorName,
                            onValueChange = onUpdateVendorName,
                            label = { Text("Vendor") },
                            placeholder = { Text("Vendor name") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = row.invoiceNo,
                            onValueChange = onUpdateInvoiceNo,
                            label = { Text("Invoice #") },
                            placeholder = { Text("Invoice no.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = row.notes,
                        onValueChange = onUpdateNotes,
                        label = { Text("Notes") },
                        placeholder = { Text("Additional notes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
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
        title = {
            Column {
                Text("🔧 Maintenance History")
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
                        "No maintenance records for this vehicle yet.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(costs) { cost ->
                            MaintenanceHistoryItem(cost)
                        }
                        item {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total:", fontWeight = FontWeight.Bold)
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
                Text("Close")
            }
        }
    )
}

@Composable
private fun MaintenanceHistoryItem(cost: MaintenanceCostDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cost.costType.replace("_", " ").replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${cost.date} ${cost.time ?: ""}".trim(),
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
                        text = "Vendor: ${cost.vendorName}",
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
