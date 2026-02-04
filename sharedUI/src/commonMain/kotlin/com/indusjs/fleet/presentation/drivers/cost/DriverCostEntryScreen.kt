package com.indusjs.fleet.presentation.drivers.cost

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.fleet.core.ui.CostTypeGroup
import com.indusjs.fleet.core.ui.CostTypeSelection
import com.indusjs.fleet.core.ui.CostTypeTwoLevelSelector
import com.indusjs.fleet.data.model.driver.DriverCostDto
import com.indusjs.fleet.domain.entity.driver.Driver
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Driver Cost Entry Screen with multi-row cost entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverCostEntryScreen(
    viewModel: DriverCostEntryViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverCostEntryContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DriverCostEntryContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DriverCostEntryContract.Effect.NavigateBack -> onNavigateBack()
                is DriverCostEntryContract.Effect.CostsSaved -> {
                    snackbarHostState.showSnackbar("${effect.count} cost(s) saved successfully")
                }
            }
        }
    }

    // History dialog
    if (state.showHistoryDialog) {
        CostHistoryDialog(
            costs = state.costHistory,
            isLoading = state.isLoadingHistory,
            driverInfo = state.driverInfo,
            onDismiss = { viewModel.sendIntent(DriverCostEntryContract.Intent.HideHistory) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Driver Cost") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.NavigateBack) }) {
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
                        onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.RefreshCostTypes) },
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
                        onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.ShowHistory) },
                        enabled = state.selectedDriver != null
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_history),
                            contentDescription = "History",
                            tint = if (state.selectedDriver != null)
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
                // Section 1: Driver Selection
                item {
                    DriverSelectionCard(
                        state = state,
                        onToggleDropdown = { viewModel.sendIntent(DriverCostEntryContract.Intent.ToggleDriverDropdown) },
                        onSelectDriver = { viewModel.sendIntent(DriverCostEntryContract.Intent.SelectDriver(it)) }
                    )
                }

                // Section 2: Driver Details (when driver selected)
                if (state.selectedDriver != null) {
                    item {
                        DriverDetailsCard(driver = state.selectedDriver!!)
                    }
                }

                // Section 3: Cost Entries Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💰 Cost Entries",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.AddCostRow) }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = "Add",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add New Cost")
                        }
                    }
                }

                // Section 4: Cost Entry Rows (reversed so new items appear on top)
                itemsIndexed(
                    items = state.costEntries.reversed(),
                    key = { _, item -> item.id }
                ) { index, entry ->
                    val actualIndex = state.costEntries.size - index
                    // Calculate date constraints for driver cost
                    val minDate = state.selectedDriver?.joiningDate?.let { joiningTimestamp ->
                        com.indusjs.datetimeutils.FleetDateTime.timestampToDateString(joiningTimestamp)
                    } ?: "01-01-2000"
                    val maxDate = com.indusjs.datetimeutils.FleetDateTime.getTomorrowDate()

                    DriverCostEntryRowCard(
                        index = actualIndex,
                        entry = entry,
                        costTypeGroups = state.costTypeGroups,
                        canDelete = state.costEntries.size > 1,
                        minDate = minDate,  // Cost date must be >= Driver joining date
                        maxDate = maxDate,  // Cost date must be <= Tomorrow
                        onToggleExpanded = { viewModel.sendIntent(DriverCostEntryContract.Intent.ToggleRowExpanded(entry.id)) },
                        onDelete = { viewModel.sendIntent(DriverCostEntryContract.Intent.RemoveCostRow(entry.id)) },
                        onSelectCostType = { selection ->
                            viewModel.sendIntent(DriverCostEntryContract.Intent.SelectCostType(entry.id, selection))
                        },
                        onCategoryChanged = { groupId, groupName ->
                            viewModel.sendIntent(DriverCostEntryContract.Intent.UpdateSelectedCategory(entry.id, groupId, groupName))
                        },
                        onDateChange = { viewModel.sendIntent(DriverCostEntryContract.Intent.UpdateDate(entry.id, it)) },
                        onTimeChange = { viewModel.sendIntent(DriverCostEntryContract.Intent.UpdateTime(entry.id, it)) },
                        onAmountChange = { viewModel.sendIntent(DriverCostEntryContract.Intent.UpdateAmount(entry.id, it)) },
                        onNotesChange = { viewModel.sendIntent(DriverCostEntryContract.Intent.UpdateNotes(entry.id, it)) },
                        onCustomCostTypeChange = {
                            viewModel.sendIntent(DriverCostEntryContract.Intent.UpdateCustomCostTypeName(entry.id, it))
                        }
                    )
                }

                // Submit Button
                item {
                    val validCount = state.costEntries.count { it.isValid }
                    Button(
                        onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.SaveCosts) },
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
                            Text(
                                "💾 Save Driver Cost(s)",
                                fontWeight = FontWeight.Bold
                            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverSelectionCard(
    state: DriverCostEntryContract.State,
    onToggleDropdown: () -> Unit,
    onSelectDriver: (Driver) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "👨‍✈️ Select Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = state.showDriverDropdown,
                onExpandedChange = { onToggleDropdown() }
            ) {
                OutlinedTextField(
                    value = state.selectedDriver?.let {
                        "${it.firstName} ${it.lastName} - ${it.mobile}"
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Driver *") },
                    placeholder = { Text("Select a driver") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    readOnly = true,
                    isError = state.driverError != null,
                    supportingText = state.driverError?.let { { Text(it) } },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDriverDropdown) }
                )

                ExposedDropdownMenu(
                    expanded = state.showDriverDropdown,
                    onDismissRequest = { onToggleDropdown() }
                ) {
                    state.drivers.forEach { driver ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${driver.firstName} ${driver.lastName}",
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = driver.mobile,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { onSelectDriver(driver) },
                            leadingIcon = {
                                Text("👤", style = MaterialTheme.typography.bodyMedium)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DriverDetailsCard(driver: Driver) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${driver.firstName.take(1)}${driver.lastName.take(1)}".uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${driver.firstName} ${driver.lastName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = driver.mobile,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (driver.licenseNumber.isNotBlank()) {
                    Text(
                        text = "License: ${driver.licenseNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (driver.isActive)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = if (driver.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (driver.isActive)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverCostEntryRowCard(
    index: Int,
    entry: DriverCostEntryRow,
    costTypeGroups: List<CostTypeGroup>,
    canDelete: Boolean,
    minDate: String? = null,  // Driver joining date
    maxDate: String? = null,  // Tomorrow's date
    onToggleExpanded: () -> Unit,
    onDelete: () -> Unit,
    onSelectCostType: (CostTypeSelection) -> Unit,
    onCategoryChanged: (String, String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCustomCostTypeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header Row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Entry number badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "#$index",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Cost type label or placeholder
                    Text(
                        text = if (entry.costTypeLabel.isNotBlank()) entry.costTypeLabel else "New Cost Entry",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Deduction badge
                    if (entry.isDeductionType) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "Deduction",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Amount preview
                    if (entry.amount.isNotBlank()) {
                        Text(
                            text = "₹${entry.amount}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.isDeductionType)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    // Expand/collapse icon
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = if (entry.isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = if (entry.isExpanded) 90f else 0f
                            },
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Delete button
                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Expandable Content
            AnimatedVisibility(
                visible = entry.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Cost Type Selector using Two-Level Selector
                    CostTypeTwoLevelSelector(
                        groups = costTypeGroups,
                        selectedCostType = entry.costType,
                        onCostTypeSelected = onSelectCostType,
                        onCategoryChanged = onCategoryChanged,
                        isError = entry.costTypeError != null,
                        errorMessage = entry.costTypeError
                    )

                    // Custom cost type name (for "Other" type)
                    if (entry.isOtherCostType) {
                        OutlinedTextField(
                            value = entry.customCostTypeName,
                            onValueChange = onCustomCostTypeChange,
                            label = { Text("Custom Cost Name") },
                            placeholder = { Text("Enter custom cost name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Date and Time using FleetDateTimePicker with date constraints
                    FleetDateTimePicker(
                        date = entry.date,
                        time = entry.time,
                        onDateTimeChange = { newDate, newTime ->
                            onDateChange(newDate)
                            onTimeChange(newTime)
                        },
                        label = "Date & Time *",
                        isError = entry.dateError != null,
                        errorMessage = entry.dateError,
                        minDate = minDate,  // Cost date must be >= Driver joining date
                        maxDate = maxDate   // Cost date must be <= Tomorrow
                    )

                    // Amount
                    OutlinedTextField(
                        value = entry.amount,
                        onValueChange = { value ->
                            // Allow only numeric input with decimal
                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                onAmountChange(value)
                            }
                        },
                        label = { Text("Amount (₹) *") },
                        placeholder = { Text("Enter amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = entry.amountError != null,
                        supportingText = entry.amountError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text(
                                text = if (entry.isDeductionType) "-₹" else "₹",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (entry.isDeductionType)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    )

                    // Notes (optional)
                    OutlinedTextField(
                        value = entry.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("Add any notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        }
    }
}

/**
 * Cost History Dialog for Driver Costs
 */
@Composable
private fun CostHistoryDialog(
    costs: List<DriverCostDto>,
    isLoading: Boolean,
    driverInfo: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Cost History")
                if (driverInfo.isNotBlank()) {
                    Text(
                        text = driverInfo,
                        style = MaterialTheme.typography.bodySmall,
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
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    costs.isEmpty() -> {
                        Text(
                            text = "No cost history found",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(costs.size) { index ->
                                val cost = costs[index]
                                CostHistoryItem(cost = cost)
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
private fun CostHistoryItem(cost: DriverCostDto) {
    val isDeduction = cost.isDeductionCost

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isDeduction)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cost.costLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (isDeduction) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Deduction",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = cost.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!cost.notes.isNullOrBlank()) {
                    Text(
                        text = cost.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = if (isDeduction) "-₹${cost.amount}" else "₹${cost.amount}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDeduction)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}

