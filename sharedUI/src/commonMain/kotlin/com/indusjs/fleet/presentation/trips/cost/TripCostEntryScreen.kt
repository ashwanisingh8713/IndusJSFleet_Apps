package com.indusjs.fleet.presentation.trips.cost

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.CostTypeGroup
import com.indusjs.fleet.core.ui.CostTypeSelection
import com.indusjs.fleet.core.ui.CostTypeTwoLevelSelector
import com.indusjs.fleet.core.ui.DateInputField
import com.indusjs.fleet.core.ui.TimeInputField
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.domain.entity.trip.Trip
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Trip Cost Entry Screen with multi-row cost entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCostEntryScreen(
    viewModel: TripCostEntryViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TripCostEntryContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TripCostEntryContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TripCostEntryContract.Effect.NavigateBack -> onNavigateBack()
                is TripCostEntryContract.Effect.CostsSaved -> {
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
            tripInfo = state.tripInfo,
            onDismiss = { viewModel.sendIntent(TripCostEntryContract.Intent.HideHistory) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Trip Cost") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.NavigateBack) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // History button
                    IconButton(
                        onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.ShowHistory) },
                        enabled = state.selectedTrip != null
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_history),
                            contentDescription = "History",
                            tint = if (state.selectedTrip != null)
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
                // Section 1: Trip Selection
                item {
                    TripSelectionCard(
                        state = state,
                        onToggleDropdown = { viewModel.sendIntent(TripCostEntryContract.Intent.ToggleTripDropdown) },
                        onSelectTrip = { viewModel.sendIntent(TripCostEntryContract.Intent.SelectTrip(it)) }
                    )
                }

                // Section 2: Trip Details (when trip selected)
                if (state.selectedTrip != null) {
                    item {
                        TripDetailsCard(trip = state.selectedTrip!!)
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
                            onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.AddCostRow) }
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
                    CostEntryRowCard(
                        index = actualIndex,
                        entry = entry,
                        costTypeGroups = state.costTypeGroups,
                        fuelTypeOptions = state.fuelTypeOptions,
                        canDelete = state.costEntries.size > 1,
                        onToggleExpanded = { viewModel.sendIntent(TripCostEntryContract.Intent.ToggleRowExpanded(entry.id)) },
                        onDelete = { viewModel.sendIntent(TripCostEntryContract.Intent.RemoveCostRow(entry.id)) },
                        onSelectCostType = { selection ->
                            viewModel.sendIntent(TripCostEntryContract.Intent.SelectCostType(entry.id, selection))
                        },
                        onCategoryChanged = { groupId, groupName ->
                            viewModel.sendIntent(TripCostEntryContract.Intent.UpdateSelectedCategory(entry.id, groupId, groupName))
                        },
                        onDateChange = { viewModel.sendIntent(TripCostEntryContract.Intent.UpdateDate(entry.id, it)) },
                        onTimeChange = { viewModel.sendIntent(TripCostEntryContract.Intent.UpdateTime(entry.id, it)) },
                        onAmountChange = { viewModel.sendIntent(TripCostEntryContract.Intent.UpdateAmount(entry.id, it)) },
                        onNotesChange = { viewModel.sendIntent(TripCostEntryContract.Intent.UpdateNotes(entry.id, it)) },
                        onCustomCostTypeChange = {
                            viewModel.sendIntent(TripCostEntryContract.Intent.UpdateCustomCostTypeName(entry.id, it))
                        },
                        onSelectFuelType = { viewModel.sendIntent(TripCostEntryContract.Intent.SelectFuelType(entry.id, it)) },
                        onToggleFuelTypeDropdown = {
                            viewModel.sendIntent(TripCostEntryContract.Intent.ToggleFuelTypeDropdown(entry.id))
                        },
                        onFuelQuantityChange = {
                            viewModel.sendIntent(TripCostEntryContract.Intent.UpdateFuelQuantity(entry.id, it))
                        },
                        onFuelRateChange = { viewModel.sendIntent(TripCostEntryContract.Intent.UpdateFuelRate(entry.id, it)) },
                        onKmPerLiterChange = { viewModel.sendIntent(TripCostEntryContract.Intent.UpdateKmPerLiter(entry.id, it)) }
                    )
                }

                // Submit Button
                item {
                    val validCount = state.costEntries.count { it.isValid }
                    Button(
                        onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.SaveCosts) },
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
                                "💾 Save Trip Cost(s)",
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
private fun TripSelectionCard(
    state: TripCostEntryContract.State,
    onToggleDropdown: () -> Unit,
    onSelectTrip: (Trip) -> Unit
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
                text = "🚛 Select Trip",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = state.showTripDropdown,
                onExpandedChange = { onToggleDropdown() }
            ) {
                OutlinedTextField(
                    value = state.selectedTrip?.let {
                        val start = it.startLocation?.address ?: "Unknown"
                        val end = it.endLocation?.address ?: "Unknown"
                        "$start → $end"
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Trip *") },
                    placeholder = { Text("Select a trip") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    readOnly = true,
                    isError = state.tripError != null,
                    supportingText = state.tripError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTripDropdown)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = state.showTripDropdown,
                    onDismissRequest = onToggleDropdown
                ) {
                    state.trips.forEach { trip ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        "${trip.startLocation?.address ?: "Unknown"} → ${trip.endLocation?.address ?: "Unknown"}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Vehicle: ${trip.vehicleNumber ?: "N/A"} | ${trip.scheduledStartTime ?: ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { onSelectTrip(trip) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripDetailsCard(
    trip: Trip
) {
    val statusColor = when (trip.status.name.lowercase()) {
        "completed" -> Color(0xFF4CAF50)
        "in_progress", "ongoing" -> Color(0xFF2196F3)
        "scheduled", "planned" -> Color(0xFFFF9800)
        "cancelled" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusText = trip.status.name.replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Trip Details title with Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 Trip Details",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Row 1: Vehicle Number and Trip Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vehicle Number
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Vehicle",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.vehicleNumber ?: "N/A",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Trip Date
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Trip Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.scheduledStartTime?.take(10) ?: trip.createdAt?.take(10) ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Row 2: Route (Start → End)
            Column {
                Text(
                    text = "Route",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Route with icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start location
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "📍",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.startLocation?.address ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Arrow
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "↓",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // End location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF44336).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🏁",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.endLocation?.address ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CostEntryRowCard(
    index: Int,
    entry: CostEntryRow,
    costTypeGroups: List<CostTypeGroup>,
    fuelTypeOptions: List<Pair<String, String>>,
    canDelete: Boolean,
    onToggleExpanded: () -> Unit,
    onDelete: () -> Unit,
    onSelectCostType: (CostTypeSelection) -> Unit,
    onCategoryChanged: (groupId: String, groupName: String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCustomCostTypeChange: (String) -> Unit,
    onSelectFuelType: (String) -> Unit,
    onToggleFuelTypeDropdown: () -> Unit,
    onFuelQuantityChange: (String) -> Unit,
    onFuelRateChange: (String) -> Unit,
    onKmPerLiterChange: (String) -> Unit
) {
    val borderColor = if (entry.isValid) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Header row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (entry.isValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "#$index",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (entry.isValid)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = entry.costTypeLabel.ifBlank { "Select Type" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (entry.amount.isNotBlank()) {
                            Text(
                                text = "₹${entry.amount} | ${entry.date.ifBlank { "No Date" }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row {
                    if (canDelete) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_delete),
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = if (entry.isExpanded) "Collapse" else "Expand",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = if (entry.isExpanded) 90f else 0f
                            }
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = entry.isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider()

                    // Cost Type two-level selector (category dropdown + chip items)
                    CostTypeTwoLevelSelector(
                        groups = costTypeGroups,
                        selectedCostType = entry.costType,
                        onCostTypeSelected = { selection ->
                            onSelectCostType(selection)
                        },
                        onCategoryChanged = { groupId, groupName ->
                            onCategoryChanged(groupId, groupName)
                        },
                        isError = entry.costTypeError != null,
                        errorMessage = entry.costTypeError
                    )

                    // Custom cost type name (if "Other" selected)
                    if (entry.isOtherCostType) {
                        OutlinedTextField(
                            value = entry.customCostTypeName,
                            onValueChange = onCustomCostTypeChange,
                            label = { Text("Cost Type Name *") },
                            placeholder = { Text("Enter custom cost type") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Date & Time row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DateInputField(
                            value = entry.date,
                            onValueChange = onDateChange,
                            label = "Date *",
                            error = entry.dateError,
                            modifier = Modifier.weight(1f)
                        )

                        TimeInputField(
                            value = entry.time,
                            onValueChange = onTimeChange,
                            label = "Time",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Amount
                    OutlinedTextField(
                        value = entry.amount,
                        onValueChange = onAmountChange,
                        label = { Text("Amount (₹) *") },
                        placeholder = { Text("Enter amount") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = entry.amountError != null,
                        supportingText = entry.amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) }
                    )


                    // Fuel specific fields
                    if (entry.isFuelCostType) {
                        HorizontalDivider()
                        Text(
                            text = "⛽ Fuel Details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        // Fuel Type - read-only, auto-populated from selected Cost Type
                        OutlinedTextField(
                            value = entry.costTypeLabel.ifBlank { "Select a fuel type above" },
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

                        // Fuel Quantity & Rate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = entry.fuelQuantity,
                                onValueChange = onFuelQuantityChange,
                                label = { Text("Quantity (L)") },
                                placeholder = { Text("Liters") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            OutlinedTextField(
                                value = entry.fuelRate,
                                onValueChange = onFuelRateChange,
                                label = { Text("Rate (₹/L)") },
                                placeholder = { Text("Per liter") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        // Km per Liter
                        OutlinedTextField(
                            value = entry.kmPerLiter,
                            onValueChange = onKmPerLiterChange,
                            label = { Text("Mileage (Km/L)") },
                            placeholder = { Text("Km per liter") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        // Calculated total
                        val quantity = entry.fuelQuantity.toDoubleOrNull() ?: 0.0
                        val rate = entry.fuelRate.toDoubleOrNull() ?: 0.0
                        if (quantity > 0 && rate > 0) {
                            val calculatedTotal = quantity * rate
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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

                    // Notes (at bottom)
                    OutlinedTextField(
                        value = entry.notes,
                        onValueChange = onNotesChange,
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("Enter notes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun CostHistoryDialog(
    costs: List<TripCostDto>,
    isLoading: Boolean,
    tripInfo: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("📜 Cost History")
                if (tripInfo.isNotEmpty()) {
                    Text(
                        text = tripInfo,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        "No costs recorded for this trip yet.",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(costs) { cost ->
                            CostHistoryItem(cost)
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
private fun CostHistoryItem(cost: TripCostDto) {
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
                if (!cost.notes.isNullOrBlank()) {
                    Text(
                        text = cost.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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



