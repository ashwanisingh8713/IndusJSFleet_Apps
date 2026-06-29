package com.ijs.trip.presentation.cost

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.CostTypeGroup
import com.indusjs.uicomponents.components.CostTypeSelection
import com.indusjs.uicomponents.components.CostTypeTwoLevelSelector
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.FleetTopAppBar
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetColors
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
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
            FleetTopAppBar(
                title = stringResource(Res.string.trip_cost_screen_title),
                onNavigateBack = { viewModel.sendIntent(TripCostEntryContract.Intent.NavigateBack) },
                actions = {
                    // Refresh cost types button
                    IconButton(
                        onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.RefreshCostTypes) },
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
                                contentDescription = stringResource(Res.string.trip_cost_cd_refresh_types),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                }
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
                // Center the form with a content cap on wide screens.
                val contentModifier = if (bp == FleetBreakpoint.Expanded) {
                    Modifier
                        .widthIn(max = FleetTokens.Width.MaxContent)
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                } else {
                    Modifier.fillMaxWidth()
                }

                LazyColumn(
                    modifier = contentModifier
                        .fillMaxHeight()
                        .padding(FleetTokens.Spacing.L),
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
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
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
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
                        // Schedule fields are UTC epoch-millis; convert to DD-MM-YYYY
                        // picker strings for the cost-date min/max constraints.
                        val tripStartMs = state.selectedTrip?.plannedStart?.takeIf { it > 0L }
                            ?: state.selectedTrip?.scheduledDate?.takeIf { it > 0L }
                        val tripStartDate = tripStartMs?.let {
                            com.indusjs.datetimeutils.FleetDateTime.getMinDateForTripCost(
                                com.indusjs.datetimeutils.FleetDateTime.timestampToDateString(it)
                            )
                        }
                        val tripEndDate = state.selectedTrip?.let { trip ->
                            val isCompleted = trip.status == com.ijs.trip.domain.entity.TripStatus.COMPLETED
                            val endDateStr = trip.plannedEnd?.takeIf { it > 0L }?.let { ms ->
                                com.indusjs.datetimeutils.FleetDateTime.timestampToDateString(ms)
                            }
                            com.indusjs.datetimeutils.FleetDateTime.getMaxDateForTripCost(endDateStr, isCompleted)
                        } ?: com.indusjs.datetimeutils.FleetDateTime.getTomorrowDate()

                        CostEntryRowCard(
                            index = actualIndex,
                            entry = entry,
                            // Fuel & Energy cost types filtered to the trip's vehicle fuel (+ EV/AdBlue).
                            costTypeGroups = state.costTypeGroupsForVehicle(),
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

                    // Submit Button (gated on state.canSave; preserves bulk-create flow)
                    item {
                        FleetButton(
                            text = stringResource(Res.string.trip_cost_save_trips),
                            onClick = { viewModel.sendIntent(TripCostEntryContract.Intent.SaveCosts) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripSelectionCard(
    state: TripCostEntryContract.State,
    onToggleDropdown: () -> Unit,
    onSelectTrip: (Trip) -> Unit
) {
    val unknownRoute = stringResource(Res.string.payment_unknown)
    val notAvailable = stringResource(Res.string.vehicle_route_na)
    FleetTitledSectionCard(title = stringResource(Res.string.trip_cost_section_select_trip)) {
        // Rich two-line trip options (route + vehicle/date) require the Material 3
        // ExposedDropdownMenuBox; FleetDropdown only renders single-label options.
        ExposedDropdownMenuBox(
            expanded = state.showTripDropdown,
            onExpandedChange = { onToggleDropdown() }
        ) {
            FleetInputField(
                value = state.selectedTrip?.let {
                    val start = it.startLocation?.address ?: unknownRoute
                    val end = it.endLocation?.address ?: unknownRoute
                    "$start → $end"
                } ?: "",
                onValueChange = {},
                label = stringResource(Res.string.trip_cost_label_trip),
                placeholder = stringResource(Res.string.trip_cost_placeholder_trip),
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                readOnly = true,
                isError = state.tripError != null,
                errorMessage = state.tripError,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showTripDropdown)
                }
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    stringResource(
                                        Res.string.trip_cost_vehicle_line,
                                        trip.vehicleNumber ?: notAvailable,
                                        com.indusjs.fleet.core.util.formatDateToHumanReadable(
                                            trip.scheduledStartTime?.takeIf { it > 0L }
                                        )
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
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

    FleetSectionCard(
        elevation = FleetTokens.Elevation.Raised
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
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
                FleetStatusBadge(status = statusText, color = statusColor)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Row 1: Vehicle Number and Trip Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vehicle Number
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.trip_cost_label_vehicle),
                        style = MaterialTheme.typography.labelMedium,
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
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = com.indusjs.fleet.core.util.formatDateToHumanReadable(
                            trip.scheduledStartTime?.takeIf { it > 0L }
                                ?: trip.createdAt?.takeIf { it > 0L }
                        ).ifBlank { notAvailable },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Row 2: Route (Start → End)
            Column {
                Text(
                    text = stringResource(Res.string.trip_cost_label_route),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                // Start location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(FleetTokens.Radius.S),
                        color = FleetStatusColors.FleetOnRoute.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "📍",
                            modifier = Modifier.padding(FleetTokens.Spacing.XS),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
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
                        .padding(start = FleetTokens.Spacing.M, top = FleetTokens.Spacing.XXS, bottom = FleetTokens.Spacing.XXS),
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
                        shape = RoundedCornerShape(FleetTokens.Radius.S),
                        color = FleetColors.tripCancelled.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "🏁",
                            modifier = Modifier.padding(FleetTokens.Spacing.XS),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
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

    // Card manages its own zero outer padding so the clickable header and the
    // divider can run full-bleed; inner sections add their own FleetTokens padding.
    FleetSectionCard(contentPadding = 0.dp) {
        // Header row (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded() }
                .padding(FleetTokens.Spacing.M),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.S),
                    color = if (entry.isValid)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "#$index",
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (entry.isValid)
                            MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                Column {
                    Text(
                        text = if (entry.costTypeLabel.isBlank()) selectTypeLabel else entry.costTypeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (entry.amount.isNotBlank()) {
                        Text(
                            text = "₹${entry.amount} | ${if (entry.date.isBlank()) noDateLabel else entry.date}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(FleetTokens.IconSize.Default)
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
                modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M).padding(bottom = FleetTokens.Spacing.M),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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
                    FleetInputField(
                        value = entry.customCostTypeName,
                        onValueChange = onCustomCostTypeChange,
                        fieldType = FieldType.DEFAULT,
                        label = stringResource(Res.string.trip_cost_label_cost_type_name),
                        placeholder = stringResource(Res.string.trip_cost_placeholder_custom_cost),
                        modifier = Modifier.fillMaxWidth()
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
                FleetInputField(
                    value = entry.amount,
                    onValueChange = onAmountChange,
                    fieldType = FieldType.DECIMAL,
                    label = stringResource(Res.string.trip_cost_label_amount),
                    placeholder = stringResource(Res.string.trip_cost_placeholder_amount),
                    modifier = Modifier.fillMaxWidth(),
                    isError = entry.amountError != null,
                    errorMessage = entry.amountError,
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) }
                )

                // Fuel specific fields
                if (entry.isFuelCostType) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = stringResource(Res.string.trip_cost_section_fuel),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    // Fuel Type - read-only, auto-populated from selected Cost Type
                    FleetInputField(
                        value = if (entry.costTypeLabel.isBlank()) fuelTypePlaceholder else entry.costTypeLabel,
                        onValueChange = {},
                        fieldType = FieldType.DEFAULT,
                        label = stringResource(Res.string.trip_cost_label_fuel_type),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false
                    )

                    // Fuel Quantity & Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        FleetInputField(
                            value = entry.fuelQuantity,
                            onValueChange = onFuelQuantityChange,
                            fieldType = FieldType.DECIMAL,
                            label = stringResource(Res.string.trip_cost_label_fuel_quantity),
                            placeholder = stringResource(Res.string.trip_cost_placeholder_fuel_quantity),
                            modifier = Modifier.weight(1f)
                        )

                        FleetInputField(
                            value = entry.fuelRate,
                            onValueChange = onFuelRateChange,
                            fieldType = FieldType.DECIMAL,
                            label = stringResource(Res.string.trip_cost_label_fuel_rate),
                            placeholder = stringResource(Res.string.trip_cost_placeholder_fuel_rate),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Km per Liter
                    FleetInputField(
                        value = entry.kmPerLiter,
                        onValueChange = onKmPerLiterChange,
                        fieldType = FieldType.DECIMAL,
                        label = stringResource(Res.string.trip_cost_label_mileage),
                        placeholder = stringResource(Res.string.trip_cost_placeholder_mileage),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Calculated total
                    val quantity = entry.fuelQuantity.toDoubleOrNull() ?: 0.0
                    val rate = entry.fuelRate.toDoubleOrNull() ?: 0.0
                    if (quantity > 0 && rate > 0) {
                        val calculatedTotal = quantity * rate
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(FleetTokens.Radius.M),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(FleetTokens.Spacing.M),
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
                FleetInputField(
                    value = entry.notes,
                    onValueChange = onNotesChange,
                    fieldType = FieldType.NOTES,
                    label = stringResource(Res.string.trip_cost_label_notes),
                    placeholder = stringResource(Res.string.trip_cost_placeholder_notes),
                    modifier = Modifier.fillMaxWidth()
                )
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
        shape = RoundedCornerShape(FleetTokens.Radius.XL),
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
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    costs.isEmpty() -> {
                        Text(
                            stringResource(Res.string.trip_cost_no_costs_yet),
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                        ) {
                            items(costs) { cost ->
                                CostHistoryItem(cost)
                            }
                            item {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = FleetTokens.Spacing.S),
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
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${cost.date?.takeIf { it > 0L }?.let { com.indusjs.fleet.core.util.formatDateToHumanReadable(it) } ?: ""} ${cost.time ?: ""}".trim(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val costNotes = cost.notes
                if (!costNotes.isNullOrBlank()) {
                    Text(
                        text = costNotes,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = "₹${cost.amount.toLong()}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
