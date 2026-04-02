package com.ijs.trip.presentation.cost

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
import com.indusjs.uicomponents.theme.FleetColors
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.ijs.trip.domain.entity.Trip
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trip Cost Entry Screen with multi-row cost entries.
 *
 * @param initialTripId Optional trip ID to pre-select when navigating from Trip Detail
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripCostEntryScreen(
    viewModel: TripCostEntryViewModel,
    initialTripId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var costsSavedCount by remember { mutableStateOf<Int?>(null) }
    val tripCostBatchSavedTemplate = stringResource(Res.string.trip_cost_batch_saved)

    // Pre-select trip if initialTripId is provided
    LaunchedEffect(initialTripId) {
        if (!initialTripId.isNullOrBlank()) {
            viewModel.sendIntent(TripCostEntryContract.Intent.PreSelectTripById(initialTripId))
        }
    }

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
                    costsSavedCount = effect.count
                }
            }
        }
    }

    LaunchedEffect(costsSavedCount, tripCostBatchSavedTemplate) {
        val count = costsSavedCount ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            tripCostBatchSavedTemplate.replace("%1\$d", count.toString())
        )
        costsSavedCount = null
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
                title = { Text(stringResource(Res.string.trip_cost_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.NavigateBack) }) {
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
                        onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.RefreshCostTypes) },
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
                                contentDescription = stringResource(Res.string.trip_cost_cd_refresh_types),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // History button
                    IconButton(
                        onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.ShowHistory) },
                        enabled = state.selectedTrip != null
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_history),
                            contentDescription = stringResource(Res.string.trip_cost_cd_history),
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
                            text = stringResource(Res.string.trip_cost_section_entries),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.AddCostRow) }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = stringResource(Res.string.cd_add),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(Res.string.trip_cost_add_new))
                        }
                    }
                }

                // Section 4: Cost Entry Rows (reversed so new items appear on top)
                itemsIndexed(
                    items = state.costEntries.reversed(),
                    key = { _, item -> item.id }
                ) { index, entry ->
                    val actualIndex = state.costEntries.size - index
                    // Calculate date constraints for trip cost
                    val tripStartDate = state.selectedTrip?.plannedStart?.let {
                        com.indusjs.datetimeutils.FleetDateTime.getMinDateForTripCost(it)
                    } ?: state.selectedTrip?.scheduledDate
                    val tripEndDate = state.selectedTrip?.let { trip ->
                        val isCompleted = trip.status == com.ijs.trip.domain.entity.TripStatus.COMPLETED
                        com.indusjs.datetimeutils.FleetDateTime.getMaxDateForTripCost(trip.plannedEnd, isCompleted)
                    } ?: com.indusjs.datetimeutils.FleetDateTime.getTomorrowDate()

                    CostEntryRowCard(
                        index = actualIndex,
                        entry = entry,
                        costTypeGroups = state.costTypeGroups,
                        fuelTypeOptions = state.fuelTypeOptions,
                        canDelete = state.costEntries.size > 1,
                        tripStartDate = tripStartDate,  // Cost date must be >= Trip start date
                        tripEndDate = tripEndDate,  // Cost date must be <= Trip end date or tomorrow
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
                                stringResource(Res.string.trip_cost_save_trips),
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
    val unknownRoute = stringResource(Res.string.payment_unknown)
    val notAvailable = stringResource(Res.string.vehicle_route_na)
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
                text = stringResource(Res.string.trip_cost_section_select_trip),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            ExposedDropdownMenuBox(
                expanded = state.showTripDropdown,
                onExpandedChange = { onToggleDropdown() }
            ) {
                OutlinedTextField(
                    value = state.selectedTrip?.let {
                        val start = it.startLocation?.address ?: unknownRoute
                        val end = it.endLocation?.address ?: unknownRoute
                        "$start → $end"
                    } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(Res.string.trip_cost_label_trip)) },
                    placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_trip)) },
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
                                        "${trip.startLocation?.address ?: unknownRoute} → ${trip.endLocation?.address ?: unknownRoute}",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        stringResource(
                                            Res.string.trip_cost_vehicle_line,
                                            trip.vehicleNumber ?: notAvailable,
                                            trip.scheduledStartTime.orEmpty()
                                        ),
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
    val notAvailable = stringResource(Res.string.vehicle_route_na)
    val unknownRoute = stringResource(Res.string.payment_unknown)
    val statusColor = when (trip.status.name.lowercase()) {
        "completed" -> FleetStatusColors.FleetOnRoute
        "in_progress", "ongoing" -> FleetStatusColors.FleetPlanned
        "scheduled", "planned" -> FleetStatusColors.FleetMaintenance
        "cancelled" -> FleetColors.tripCancelled
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
                    text = stringResource(Res.string.trip_cost_section_details),
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
                        text = stringResource(Res.string.trip_cost_label_vehicle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.vehicleNumber ?: notAvailable,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Trip Date
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.trip_cost_label_trip_date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = trip.scheduledStartTime?.take(10) ?: trip.createdAt?.take(10) ?: notAvailable,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Row 2: Route (Start → End)
            Column {
                Text(
                    text = stringResource(Res.string.trip_cost_label_route),
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
                        color = FleetStatusColors.FleetOnRoute.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "📍",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.startLocation?.address ?: unknownRoute,
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
                        color = FleetColors.tripCancelled.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🏁",
                            modifier = Modifier.padding(4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = trip.endLocation?.address ?: unknownRoute,
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
    tripStartDate: String? = null,  // Cost date must be >= Trip start date
    tripEndDate: String? = null,  // Cost date must be <= Trip end date or tomorrow
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
    val selectTypeLabel = stringResource(Res.string.trip_cost_select_type)
    val noDateLabel = stringResource(Res.string.trip_cost_no_date)
    val fuelTypePlaceholder = stringResource(Res.string.trip_cost_placeholder_fuel_type)
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
                            text = if (entry.costTypeLabel.isBlank()) selectTypeLabel else entry.costTypeLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (entry.amount.isNotBlank()) {
                            Text(
                                text = "₹${entry.amount} | ${if (entry.date.isBlank()) noDateLabel else entry.date}",
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
                                contentDescription = stringResource(Res.string.delete),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_chevron_right),
                        contentDescription = if (entry.isExpanded) {
                            stringResource(Res.string.trip_cost_cd_collapse)
                        } else {
                            stringResource(Res.string.trip_cost_cd_expand)
                        },
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
                            label = { Text(stringResource(Res.string.trip_cost_label_cost_type_name)) },
                            placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_custom_cost)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Date & Time picker (unified) with trip date constraints
                    FleetDateTimePicker(
                        date = entry.date,
                        time = entry.time,
                        onDateTimeChange = { newDate, newTime ->
                            onDateChange(newDate)
                            onTimeChange(newTime)
                        },
                        label = stringResource(Res.string.trip_cost_label_date_time),
                        isError = entry.dateError != null,
                        errorMessage = entry.dateError,
                        minDate = tripStartDate,  // Cost date must be >= Trip start date
                        maxDate = tripEndDate  // Cost date must be <= Trip end date or tomorrow
                    )

                    // Amount
                    OutlinedTextField(
                        value = entry.amount,
                        onValueChange = onAmountChange,
                        label = { Text(stringResource(Res.string.trip_cost_label_amount)) },
                        placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_amount)) },
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
                            text = stringResource(Res.string.trip_cost_section_fuel),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        // Fuel Type - read-only, auto-populated from selected Cost Type
                        OutlinedTextField(
                            value = if (entry.costTypeLabel.isBlank()) fuelTypePlaceholder else entry.costTypeLabel,
                            onValueChange = {},
                            label = { Text(stringResource(Res.string.trip_cost_label_fuel_type)) },
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
                                label = { Text(stringResource(Res.string.trip_cost_label_fuel_quantity)) },
                                placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_fuel_quantity)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )

                            OutlinedTextField(
                                value = entry.fuelRate,
                                onValueChange = onFuelRateChange,
                                label = { Text(stringResource(Res.string.trip_cost_label_fuel_rate)) },
                                placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_fuel_rate)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }

                        // Km per Liter
                        OutlinedTextField(
                            value = entry.kmPerLiter,
                            onValueChange = onKmPerLiterChange,
                            label = { Text(stringResource(Res.string.trip_cost_label_mileage)) },
                            placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_mileage)) },
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
                                    Text(
                                        stringResource(Res.string.trip_cost_calculated_total),
                                        fontWeight = FontWeight.Medium
                                    )
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
                        label = { Text(stringResource(Res.string.trip_cost_label_notes)) },
                        placeholder = { Text(stringResource(Res.string.trip_cost_placeholder_notes)) },
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
                Text(stringResource(Res.string.trip_cost_history_title))
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
                        stringResource(Res.string.trip_cost_no_costs_yet),
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
                                Text(
                                    stringResource(Res.string.trip_cost_total_label),
                                    fontWeight = FontWeight.Bold
                                )
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
                val costNotes = cost.notes
                if (!costNotes.isNullOrBlank()) {
                    Text(
                        text = costNotes,
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


