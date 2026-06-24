package com.ijs.driver.presentation.cost

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.uicomponents.components.CostTypeGroup
import com.indusjs.uicomponents.components.CostTypeSelection
import com.indusjs.uicomponents.components.CostTypeTwoLevelSelector
import com.indusjs.uicomponents.components.FleetAvatar
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.fleet.data.model.driver.DriverCostDto
import com.ijs.driver.domain.entity.Driver
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Driver Cost Entry Screen with multi-row cost entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverCostEntryScreen(
    viewModel: DriverCostEntryViewModel,
    initialDriverId: String? = null,
    onNavigateBack: () -> Unit = {},
    onCostsSaved: (driverId: String?, count: Int) -> Unit = { _, _ -> }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingCostSavedEventId by remember { mutableStateOf(0) }
    var pendingCostSavedCount by remember { mutableStateOf(0) }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Set initial driver if provided
    LaunchedEffect(initialDriverId, state.drivers) {
        if (!initialDriverId.isNullOrBlank() && state.drivers.isNotEmpty() && state.selectedDriver == null) {
            viewModel.sendIntent(DriverCostEntryContract.Intent.SetInitialDriver(initialDriverId))
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverCostEntryContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is DriverCostEntryContract.Effect.ShowError -> {
                    pendingSnackbar = effect.message
                }
                is DriverCostEntryContract.Effect.NavigateBack -> onNavigateBack()
                is DriverCostEntryContract.Effect.CostsSaved -> {
                    pendingCostSavedEventId++
                    pendingCostSavedCount = effect.count
                    // Notify parent so the previous screen (DriverDetail) can refresh its costs tab.
                    onCostsSaved(state.selectedDriver?.id?.toString() ?: initialDriverId, effect.count)
                }
            }
        }
    }

    if (pendingCostSavedEventId > 0) {
        val costSavedMsg = stringResource(Res.string.driver_cost_saved_count, pendingCostSavedCount)
        LaunchedEffect(pendingCostSavedEventId) {
            snackbarHostState.showSnackbar(costSavedMsg)
            pendingCostSavedEventId = 0
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
                title = { Text(stringResource(Res.string.drivers_cost)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.NavigateBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
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
                                contentDescription = stringResource(Res.string.driver_cost_refresh_cost_types),
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
                            contentDescription = stringResource(Res.string.cd_history),
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
                            text = stringResource(Res.string.driver_cost_entry_section),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { viewModel.sendIntent(DriverCostEntryContract.Intent.AddCostRow) }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = stringResource(Res.string.cd_add),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.driver_cost_add_new_row))
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
                                stringResource(Res.string.driver_cost_save_button),
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
    FleetTitledSectionCard(
        title = stringResource(Res.string.driver_cost_select_driver_section)
    ) {
        ExposedDropdownMenuBox(
            expanded = state.showDriverDropdown,
            onExpandedChange = { onToggleDropdown() }
        ) {
                OutlinedTextField(
                    value = state.selectedDriver?.let {
                        "${it.firstName} ${it.lastName} - ${it.mobile}"
                    } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(Res.string.driver_cost_label_driver)) },
                    placeholder = { Text(stringResource(Res.string.driver_cost_placeholder_driver)) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    readOnly = true,
                    isError = state.driverError != null,
                    supportingText = state.driverError?.let { { Text(it.resolve()) } },
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

@Composable
private fun DriverDetailsCard(driver: Driver) {
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = null
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FleetAvatar(
                name = "${driver.firstName} ${driver.lastName}",
                background = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            )

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
                        text = stringResource(Res.string.driver_cost_license_line, driver.licenseNumber),
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
                    text = if (driver.isActive) {
                        stringResource(Res.string.driver_cost_active)
                    } else {
                        stringResource(Res.string.driver_cost_inactive)
                    },
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
                        text = if (entry.costTypeLabel.isNotBlank()) {
                            entry.costTypeLabel
                        } else {
                            stringResource(Res.string.driver_cost_new_entry)
                        },
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
                                text = stringResource(Res.string.driver_cost_deduction),
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
                        contentDescription = if (entry.isExpanded) {
                            stringResource(Res.string.driver_cost_collapse)
                        } else {
                            stringResource(Res.string.driver_cost_expand)
                        },
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
                                contentDescription = stringResource(Res.string.delete),
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
                        errorMessage = entry.costTypeError?.resolve()
                    )

                    // Custom cost type name (for "Other" type)
                    if (entry.isOtherCostType) {
                        OutlinedTextField(
                            value = entry.customCostTypeName,
                            onValueChange = onCustomCostTypeChange,
                            label = { Text(stringResource(Res.string.driver_cost_label_custom_name)) },
                            placeholder = { Text(stringResource(Res.string.driver_cost_placeholder_custom_name)) },
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
                        label = stringResource(Res.string.driver_cost_label_date_time),
                        isError = entry.dateError != null,
                        errorMessage = entry.dateError?.resolve(),
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
                        label = { Text(stringResource(Res.string.driver_cost_label_amount)) },
                        placeholder = { Text(stringResource(Res.string.driver_cost_placeholder_amount)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = entry.amountError != null,
                        supportingText = entry.amountError?.let { { Text(it.resolve()) } },
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
                        label = { Text(stringResource(Res.string.driver_cost_label_notes)) },
                        placeholder = { Text(stringResource(Res.string.driver_cost_notes_placeholder)) },
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
                Text(stringResource(Res.string.driver_cost_history_dialog_title))
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
                            text = stringResource(Res.string.driver_cost_history_empty),
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
                Text(stringResource(Res.string.close))
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
                                text = stringResource(Res.string.driver_cost_deduction),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = cost.date?.takeIf { it > 0L }
                        ?.let { com.indusjs.fleet.core.util.formatDateToHumanReadable(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val costNotes = cost.notes
                if (!costNotes.isNullOrBlank()) {
                    Text(
                        text = costNotes,
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

