package com.ijs.trip.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.util.formatDateTimeForDisplay
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.customer.CustomerDetailsSection
import com.indusjs.uicomponents.customer.CustomerSelectionBottomSheet
import com.ijs.customer.presentation.toSelectableCustomer
import com.ijs.customer.presentation.toSelectableCustomerList
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.ijs.customer.domain.entity.Customer
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Create Trip Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    viewModel: CreateTripViewModel,
    onNavigateBack: () -> Unit = {},
    onTripCreated: (String) -> Unit = {},
    onNavigateToAddCustomer: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load vehicles and drivers on first composition
    LaunchedEffect(Unit) {
        viewModel.sendIntent(CreateTripContract.Intent.LoadVehiclesAndDrivers)
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateTripContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateTripContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateTripContract.Effect.NavigateBack -> onNavigateBack()
                is CreateTripContract.Effect.TripCreated -> onTripCreated(effect.tripId)
                is CreateTripContract.Effect.NavigateToAddCustomer -> onNavigateToAddCustomer()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.trip_create_title))
                        // Form completion progress indicator
                        LinearProgressIndicator(
                            progress = { state.formCompletionPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .padding(top = 4.dp),
                            color = when {
                                state.formCompletionPercentage == 100 -> MaterialTheme.colorScheme.primary
                                state.formCompletionPercentage >= 70 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            // compose-resources does NOT collapse "%%", so pass a pre-formatted
                            // "<n>%" string. The string becomes "%1$s complete" (see needsString).
                            text = stringResource(
                                Res.string.trip_create_percent_complete,
                                "${state.formCompletionPercentage}%"
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.NavigateBack) }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Column {
                    // Show validation summary if there are errors
                    if (!state.isFormValid && state.formCompletionPercentage > 0) {
                        val errorCount = listOfNotNull(
                            state.vehicleError,
                            state.driverError,
                            state.startLocationError,
                            state.endLocationError,
                            state.departureDateError,
                            state.departureTimeError,
                            state.arrivalDateError,
                            state.cargoTypeError,
                            state.cargoWeightError,
                            state.customerContactError
                        ).size

                        if (errorCount > 0) {
                            Text(
                                text = "⚠️ " + stringResource(
                                    Res.string.trip_create_fields_need_attention,
                                    errorCount
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.errorContainer)
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FleetButton(
                            text = stringResource(Res.string.cancel),
                            onClick = { viewModel.sendIntent(CreateTripContract.Intent.NavigateBack) },
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )

                        FleetButton(
                            text = if (state.isSaving) stringResource(Res.string.action_creating)
                            else stringResource(Res.string.trip_create_title),
                            onClick = { viewModel.sendIntent(CreateTripContract.Intent.CreateTrip) },
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier.weight(1f),
                            enabled = state.canSave,
                            isLoading = state.isSaving
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoadingData) {
            LoadingContent(message = stringResource(Res.string.trip_detail_loading_vehicles))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = FleetTokens.Spacing.S,
                    vertical = FleetTokens.Spacing.M
                ),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                // Schedule Section - First (when is the trip?)
                item {
                    SectionCard(
                        title = stringResource(Res.string.trip_create_section_schedule),
                        iconRes = Res.drawable.ic_calendar
                    ) {
                        ScheduleSection(state = state, viewModel = viewModel)
                    }
                }

                // Vehicle & Driver Selection
                item {
                    SectionCard(
                        title = stringResource(Res.string.trip_create_section_vehicle_driver),
                        iconRes = Res.drawable.ic_truck
                    ) {
                        VehicleDriverSelectionSection(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }

                // Route Section
                item {
                    SectionCard(
                        title = stringResource(Res.string.trip_create_section_route),
                        iconRes = Res.drawable.ic_map
                    ) {
                        RouteSection(state = state, viewModel = viewModel)
                    }
                }

                // Cargo & Priority Section (Priority merged in here; the standalone
                // Priority SectionCard + PrioritySection composable were removed).
                item {
                    SectionCard(
                        title = stringResource(Res.string.trip_create_section_cargo),
                        iconRes = Res.drawable.ic_package
                    ) {
                        CargoSection(state = state, viewModel = viewModel)
                    }
                }

                // Customer Section with refresh capability
                item {
                    CustomerDetailsSection(
                        selectedCustomer = state.selectedCustomer?.toSelectableCustomer(),
                        customerName = state.customerName,
                        customerContact = state.customerContact,
                        isRefreshing = state.isRefreshingCustomers,
                        hasCustomers = state.allCustomers.isNotEmpty(),
                        validationError = state.customerNameError,
                        isRequired = true,
                        onSelectClick = { viewModel.sendIntent(CreateTripContract.Intent.ToggleCustomerBottomSheet) },
                        onClearClick = { viewModel.sendIntent(CreateTripContract.Intent.ClearCustomerSelection) },
                        onRefreshClick = { viewModel.sendIntent(CreateTripContract.Intent.RefreshCustomers) },
                        onAddNewClick = { viewModel.sendIntent(CreateTripContract.Intent.NavigateToAddCustomer) }
                    )
                }

                // Delivery / Consignee Section — REQUIRED (distinct from the billing customer above)
                item {
                    SectionCard(
                        title = stringResource(Res.string.trip_create_section_delivery),
                        iconRes = Res.drawable.ic_truck
                    ) {
                        DeliverySection(state = state, viewModel = viewModel)
                    }
                }

                // Pricing Section - Only visible to Owner and General Manager
                if (state.canViewTripPrice) {
                    item {
                        PricingSection(state = state, viewModel = viewModel)
                    }
                }

                // Notes Section
                item {
                    SectionCard(
                        title = stringResource(Res.string.trip_create_section_notes),
                        iconRes = Res.drawable.ic_edit
                    ) {
                        FleetInputField(
                            value = state.notes,
                            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateNotes(it)) },
                            fieldType = FieldType.NOTES,
                            label = stringResource(Res.string.trip_create_notes_label),
                            placeholder = stringResource(Res.string.trip_create_notes_placeholder),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // Saving overlay
        if (state.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(Res.string.trip_create_creating_trip))
                    }
                }
            }
        }
    }

    // Customer Selection Bottom Sheet
    if (state.showCustomerBottomSheet) {
        CustomerSelectionBottomSheet(
            customers = state.allCustomers.toSelectableCustomerList(),
            searchQuery = state.customerSearchQuery,
            isLoading = state.isLoadingCustomers,
            onDismiss = { viewModel.sendIntent(CreateTripContract.Intent.ToggleCustomerBottomSheet) },
            onSearchQueryChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCustomerSearchQuery(it)) },
            onCustomerSelect = { selectable ->
                val customer = state.allCustomers.find { it.id == selectable.id }
                if (customer != null) viewModel.sendIntent(CreateTripContract.Intent.SelectCustomer(customer))
            },
            onAddNewCustomer = { viewModel.sendIntent(CreateTripContract.Intent.NavigateToAddCustomer) }
        )
    }
}

/**
 * Local section card. [iconRes] renders an optional leading vector icon (inside a
 * [FleetAccentIconChip]) before the title — the design-system section-header anchor pattern.
 */
@Composable
private fun SectionCard(
    title: String,
    iconRes: DrawableResource? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetSectionCard(
        border = null,
        elevation = FleetTokens.Elevation.Raised
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (iconRes != null) {
                    FleetAccentIconChip(
                        accent = MaterialTheme.colorScheme.primary,
                        chipSize = 32.dp,
                        iconSize = 18.dp,
                        iconRes = iconRes
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                }
                Text(
                    text = title.stripLeadingEmoji(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

/**
 * Strips a leading emoji + whitespace from a section title. The title strings (e.g. "📅 Schedule")
 * embed an icon; the card renders its own leading icon chip, so we remove that prefix and keep only
 * the words ("Schedule"). Conservative: only trims a leading run of non-letter/non-digit symbols.
 */
private fun String.stripLeadingEmoji(): String {
    val firstWord = indexOfFirst { it.isLetterOrDigit() }
    return if (firstWord > 0) substring(firstWord).trimStart() else this
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDriverSelectionSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
        // Vehicle Dropdown
        ExposedDropdownMenuBox(
            expanded = state.showVehicleDropdown,
            onExpandedChange = { viewModel.sendIntent(CreateTripContract.Intent.ToggleVehicleDropdown) }
        ) {
            OutlinedTextField(
                value = state.selectedVehicle?.let { "${it.registrationNumber} - ${it.make} ${it.model}" } ?: "",
                onValueChange = {},
                label = { Text(stringResource(Res.string.trip_label_select_vehicle)) },
                placeholder = { Text(stringResource(Res.string.trip_edit_placeholder_vehicle)) },
                readOnly = true,
                isError = state.vehicleError != null,
                supportingText = state.vehicleError?.let { { Text(it) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showVehicleDropdown) },
                shape = RoundedCornerShape(FleetTokens.Radius.L),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = state.showVehicleDropdown,
                onDismissRequest = { viewModel.sendIntent(CreateTripContract.Intent.ToggleVehicleDropdown) },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                // Sort vehicles: available first, occupied at bottom
                state.vehicles.sortedBy { it.isOccupied }.forEach { vehicle ->
                    val isOccupied = vehicle.isOccupied
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = vehicle.registrationNumber,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isOccupied) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isOccupied) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.trip_create_vehicle_occupied),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${vehicle.make} ${vehicle.model} (${vehicle.year})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isOccupied) {
                                        val assignment = vehicle.tripAssignment
                                        if (assignment != null) {
                                        val na = stringResource(Res.string.vehicle_route_na)
                                        val startStr = assignment.plannedStart?.takeIf { it > 0L }
                                            ?.let { formatDateTimeForDisplay(it) } ?: na
                                        val endStr = assignment.plannedEnd?.takeIf { it > 0L }
                                            ?.let { formatDateTimeForDisplay(it) } ?: na
                                        Text(
                                            text = "🕐 $startStr - $endStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        }
                                    }
                                }
                            }
                        },
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.SelectVehicle(vehicle)) },
                        enabled = !isOccupied
                    )
                }
            }
        }

        // Driver Dropdown
        ExposedDropdownMenuBox(
            expanded = state.showDriverDropdown,
            onExpandedChange = { viewModel.sendIntent(CreateTripContract.Intent.ToggleDriverDropdown) }
        ) {
            OutlinedTextField(
                value = state.selectedDriver?.let { "${it.firstName} ${it.lastName} - ${it.mobile}" } ?: "",
                onValueChange = {},
                label = { Text(stringResource(Res.string.trip_label_select_driver)) },
                placeholder = { Text(stringResource(Res.string.trip_edit_placeholder_driver)) },
                readOnly = true,
                isError = state.driverError != null,
                supportingText = state.driverError?.let { { Text(it) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showDriverDropdown) },
                shape = RoundedCornerShape(FleetTokens.Radius.L),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = state.showDriverDropdown,
                onDismissRequest = { viewModel.sendIntent(CreateTripContract.Intent.ToggleDriverDropdown) },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                // Sort drivers: available first, occupied at bottom
                state.drivers.sortedBy { it.isOccupied }.forEach { driver ->
                    val isOccupied = driver.isOccupied
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${driver.firstName} ${driver.lastName}",
                                            fontWeight = FontWeight.Medium,
                                            color = if (isOccupied) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isOccupied) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.errorContainer
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.trip_create_vehicle_occupied),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = driver.mobile,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isOccupied) {
                                        val assignment = driver.tripAssignment
                                        if (assignment != null) {
                                        val na = stringResource(Res.string.vehicle_route_na)
                                        val startStr = assignment.plannedStart?.takeIf { it > 0L }
                                            ?.let { formatDateTimeForDisplay(it) } ?: na
                                        val endStr = assignment.plannedEnd?.takeIf { it > 0L }
                                            ?.let { formatDateTimeForDisplay(it) } ?: na
                                        Text(
                                            text = "🕐 $startStr - $endStr",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        }
                                    }
                                }
                            }
                        },
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.SelectDriver(driver)) },
                        enabled = !isOccupied
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
        // Start Location with Autocomplete
        LocationSearchField(
            value = state.startLocation,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.SearchStartLocation(it)) },
            label = stringResource(Res.string.trip_edit_start_location),
            placeholder = stringResource(Res.string.trip_edit_search_location),
            leadingEmoji = "🟢",
            isError = state.startLocationError != null,
            errorText = state.startLocationError,
            isLoading = state.isSearchingStartLocation,
            predictions = state.startLocationPredictions,
            showDropdown = state.showStartLocationDropdown,
            onPredictionSelected = { prediction ->
                viewModel.sendIntent(CreateTripContract.Intent.SelectStartLocationPrediction(prediction))
            },
            onDismissDropdown = {
                viewModel.sendIntent(CreateTripContract.Intent.DismissStartLocationDropdown)
            }
        )


        // End Location with Autocomplete
        LocationSearchField(
            value = state.endLocation,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.SearchEndLocation(it)) },
            label = stringResource(Res.string.trip_edit_end_location),
            placeholder = stringResource(Res.string.trip_edit_search_location),
            leadingEmoji = "🔴",
            isError = state.endLocationError != null,
            errorText = state.endLocationError,
            isLoading = state.isSearchingEndLocation,
            predictions = state.endLocationPredictions,
            showDropdown = state.showEndLocationDropdown,
            onPredictionSelected = { prediction ->
                viewModel.sendIntent(CreateTripContract.Intent.SelectEndLocationPrediction(prediction))
            },
            onDismissDropdown = {
                viewModel.sendIntent(CreateTripContract.Intent.DismissEndLocationDropdown)
            }
        )


        // Estimated Distance (auto-calculated when both locations are selected)
        Column {
            OutlinedTextField(
                value = state.estimatedDistance,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEstimatedDistance(it)) },
                label = { Text(stringResource(Res.string.trip_create_distance_label)) },
                placeholder = { Text(stringResource(Res.string.trip_create_distance_placeholder)) },
                leadingIcon = { Icon(painter = painterResource(Res.drawable.ic_map), contentDescription = null, modifier = Modifier.size(FleetTokens.IconSize.M)) },
                trailingIcon = {
                    if (state.isCalculatingDistance) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(FleetTokens.Radius.L),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    when {
                        state.isCalculatingDistance -> {
                            Text(
                                text = stringResource(Res.string.trip_create_calculating_distance),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        state.estimatedDistance.isNotBlank() && state.estimatedDuration.isNotBlank() -> {
                            Text(
                                text = stringResource(
                                    Res.string.trip_create_distance_travel_time,
                                    state.estimatedDuration
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        state.estimatedDistance.isNotBlank() -> {
                            Text(
                                text = stringResource(Res.string.trip_create_distance_calculated),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(Res.string.trip_create_select_both_locations),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}

/**
 * Location Search Field with Autocomplete Dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingEmoji: String,
    isError: Boolean,
    errorText: String?,
    isLoading: Boolean,
    predictions: List<PlacePrediction>,
    showDropdown: Boolean,
    onPredictionSelected: (PlacePrediction) -> Unit,
    onDismissDropdown: () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = showDropdown && predictions.isNotEmpty(),
        onExpandedChange = { }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            leadingIcon = { Text(leadingEmoji, modifier = Modifier.padding(start = 12.dp)) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            },
            isError = isError,
            supportingText = errorText?.let { { Text(it) } },
            singleLine = true,
            shape = RoundedCornerShape(FleetTokens.Radius.L),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = showDropdown && predictions.isNotEmpty(),
            onDismissRequest = onDismissDropdown,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .heightIn(max = 250.dp)
        ) {
            predictions.forEach { prediction ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = prediction.structuredFormatting?.mainText ?: prediction.description,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            prediction.structuredFormatting?.secondaryText?.let { secondary ->
                                Text(
                                    text = secondary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_map),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    },
                    onClick = { onPredictionSelected(prediction) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
private fun ScheduleSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    // FleetDateTimePicker is a single combined date+time field (it fires the date and time
    // intents together). The departure picker and the optional arrival picker are kept as two
    // full-width combined fields to preserve 100% of the existing picker behaviour.
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
        // Departure Date & Time (Required)
        FleetDateTimePicker(
            date = state.departureDate,
            time = state.departureTime,
            onDateTimeChange = { newDate, newTime ->
                viewModel.sendIntent(CreateTripContract.Intent.UpdateDepartureDate(newDate))
                viewModel.sendIntent(CreateTripContract.Intent.UpdateDepartureTime(newTime))
            },
            label = stringResource(Res.string.trip_create_departure_label),
            isError = state.departureDateError != null || state.departureTimeError != null,
            errorMessage = state.departureDateError ?: state.departureTimeError,
            minDate = FleetDateTime.today()
        )

        // Expected Arrival Date & Time (Optional)
        FleetDateTimePicker(
            date = state.arrivalDate,
            time = state.arrivalTime,
            onDateTimeChange = { newDate, newTime ->
                viewModel.sendIntent(CreateTripContract.Intent.UpdateArrivalDate(newDate))
                viewModel.sendIntent(CreateTripContract.Intent.UpdateArrivalTime(newTime))
            },
            label = stringResource(Res.string.trip_create_arrival_label),
            isError = state.arrivalDateError != null,
            errorMessage = state.arrivalDateError,
            minDate = state.departureDate.ifBlank { FleetDateTime.today() }
        )
    }
}

@Composable
private fun CargoSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    // Show the Hindi label when the app is running in Hindi (same locale that Compose Resources
    // uses to pick values-hi); falls back to English otherwise. Drives both the cargo-type labels
    // and the unit dropdown labels (the unit VALUE/id is unchanged — labels are display only).
    val isHindi = Locale.current.language == "hi"
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
        FleetDropdown(
            label = stringResource(Res.string.trip_create_cargo_type_required),
            // Label comes from the loaded material config (falls back to the capitalized id).
            options = state.cargoTypeOptions.map { cargo ->
                DropdownOption(
                    id = cargo,
                    label = state.cargoLabelFor(cargo, isHindi)
                )
            },
            selectedOptionId = state.cargoType.takeIf { it.isNotBlank() },
            onOptionSelected = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoType(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(Res.string.trip_placeholder_cargo_type),
            isError = state.cargoTypeError != null,
            errorMessage = state.cargoTypeError
        )

        // Cargo Description - single line
        FleetInputField(
            value = state.cargoDescription,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoDescription(it)) },
            fieldType = FieldType.DEFAULT,
            label = stringResource(Res.string.trip_create_cargo_desc_label),
            placeholder = stringResource(Res.string.trip_create_cargo_desc_placeholder),
            modifier = Modifier.fillMaxWidth()
        )

        // Cargo Weight with Unit dropdown and error support.
        // The unit list is config-driven by the selected material: it stays disabled (with a hint)
        // until a cargo type is chosen, and the material's default unit is auto-selected on pick.
        val isCargoTypeSelected = state.cargoType.isNotBlank()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
            verticalAlignment = Alignment.Top
        ) {
            // Weight input - accepts only decimal numbers - NOW REQUIRED
            FleetInputField(
                value = state.cargoWeight,
                onValueChange = { newValue ->
                    // Only allow digits and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoWeight(newValue))
                    }
                },
                fieldType = FieldType.DECIMAL,
                label = stringResource(Res.string.trip_create_cargo_weight_required),
                placeholder = stringResource(Res.string.trip_create_cargo_weight_placeholder),
                isError = state.cargoWeightError != null,
                errorMessage = state.cargoWeightError,
                modifier = Modifier.weight(1f)
            )

            FleetDropdown(
                label = stringResource(Res.string.trip_create_unit_required),
                // Display label is localized via unitLabelFor(value, isHindi); the option id stays
                // the unit VALUE so the value sent to the backend is unchanged.
                options = state.unitOptionsForSelectedCargo.map {
                    DropdownOption(id = it, label = state.unitLabelFor(it, isHindi))
                },
                selectedOptionId = state.weightUnit.takeIf { it.isNotBlank() },
                onOptionSelected = { viewModel.sendIntent(CreateTripContract.Intent.UpdateWeightUnit(it)) },
                modifier = Modifier.weight(0.6f),
                // Until a material is picked the units are unknown, so disable + hint.
                enabled = isCargoTypeSelected,
                placeholder = stringResource(
                    if (isCargoTypeSelected) Res.string.trip_create_select_placeholder
                    else Res.string.trip_create_select_material_first
                ),
                isError = state.weightUnitError != null,
                errorMessage = state.weightUnitError
            )
        }

        // Priority — merged in from the former standalone Priority section. Rendered as a dropdown
        // (was chips). The option id stays the raw priority VALUE; the label preserves the old
        // chip display (capitalized value), so the value sent to the backend is unchanged.
        FleetDropdown(
            label = stringResource(Res.string.trip_edit_label_priority),
            options = state.priorityOptions.map { priority ->
                DropdownOption(
                    id = priority,
                    label = priority.replaceFirstChar { it.uppercaseChar() }
                )
            },
            selectedOptionId = state.priority.takeIf { it.isNotBlank() },
            onOptionSelected = { viewModel.sendIntent(CreateTripContract.Intent.UpdatePriority(it)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(painter = painterResource(Res.drawable.ic_info), contentDescription = null, modifier = Modifier.size(FleetTokens.IconSize.M)) }
        )
    }
}

/**
 * Delivery / Consignee Details section. The consignee (receiver) is REQUIRED and is
 * conceptually distinct from the billing customer, though it is usually pre-filled
 * from the selected customer as editable defaults.
 */
@Composable
private fun DeliverySection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
        // Delivery Address (multi-line, required)
        FleetInputField(
            value = state.deliveryAddress,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateDeliveryAddress(it)) },
            fieldType = FieldType.ADDRESS,
            label = stringResource(Res.string.trip_create_delivery_address_required),
            placeholder = stringResource(Res.string.trip_create_delivery_address_placeholder),
            isError = state.deliveryAddressError != null,
            errorMessage = state.deliveryAddressError,
            modifier = Modifier.fillMaxWidth()
        )

        // Delivery person + contact in a 2-col Row.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
            verticalAlignment = Alignment.Top
        ) {
            // Delivery Person Name (required)
            FleetInputField(
                value = state.deliveryPersonName,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateDeliveryPersonName(it)) },
                fieldType = FieldType.DEFAULT,
                label = stringResource(Res.string.trip_create_delivery_person_required),
                placeholder = stringResource(Res.string.trip_create_delivery_person_placeholder),
                isError = state.deliveryPersonNameError != null,
                errorMessage = state.deliveryPersonNameError,
                modifier = Modifier.weight(1f)
            )

            // Delivery Contact Number (phone keyboard, digit-filtered + capped at 10, required)
            FleetInputField(
                value = state.deliveryContactNumber,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }.take(10)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateDeliveryContactNumber(digits))
                },
                fieldType = FieldType.PHONE,
                label = stringResource(Res.string.trip_create_delivery_contact_required),
                placeholder = stringResource(Res.string.trip_create_delivery_contact_placeholder),
                isError = state.deliveryContactNumberError != null,
                errorMessage = state.deliveryContactNumberError,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Enhanced Pricing Section with compact UI
 */
@Composable
private fun PricingSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.trip_create_pricing_title),
        iconRes = Res.drawable.ic_cost,
        accent = MaterialTheme.colorScheme.primary
    ) {
            // Subtitle pill
            Surface(
                shape = RoundedCornerShape(FleetTokens.Radius.S),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Text(
                    text = stringResource(Res.string.trip_create_pricing_subtitle),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = FleetTokens.Spacing.S,
                        vertical = FleetTokens.Spacing.XXS
                    )
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            // ₹ leading icon shared by the price inputs.
            val rupeeIcon: @Composable () -> Unit = {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = FleetTokens.Spacing.M)
                )
            }

            // Quoted (Total Amount) | Actual (Revenue) in a 2-col Row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
                verticalAlignment = Alignment.Top
            ) {
                // Quoted price (expected_trip_price) - inline error.
                FleetInputField(
                    value = state.tripPrice,
                    onValueChange = { newValue ->
                        // Only allow digits and decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            viewModel.sendIntent(CreateTripContract.Intent.UpdateTripPrice(newValue))
                        }
                    },
                    fieldType = FieldType.DECIMAL,
                    label = stringResource(Res.string.trip_create_total_amount_inr),
                    placeholder = stringResource(Res.string.trip_edit_placeholder_trip_price),
                    isError = state.tripPriceError != null,
                    errorMessage = state.tripPriceError,
                    leadingIcon = rupeeIcon,
                    modifier = Modifier.weight(1f)
                )

                // Actual Price (Revenue) - editable, defaults to the quoted Total Amount.
                // Sent to the backend as selling_value (the actual amount the customer owes).
                Column(modifier = Modifier.weight(1f)) {
                    FleetInputField(
                        value = state.actualPrice,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                viewModel.sendIntent(CreateTripContract.Intent.UpdateActualPrice(newValue))
                            }
                        },
                        fieldType = FieldType.DECIMAL,
                        label = stringResource(Res.string.trip_actual_price_revenue_label),
                        placeholder = state.tripPrice.ifBlank { stringResource(Res.string.trip_edit_placeholder_trip_price) },
                        leadingIcon = rupeeIcon,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // FleetInputField only shows supporting text in the error state, so the
                    // (non-error) revenue hint is rendered explicitly below the field.
                    Text(
                        text = stringResource(Res.string.trip_actual_price_revenue_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = FleetTokens.Spacing.L,
                            top = FleetTokens.Spacing.XS
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

            // Purchase Price (COGS) - editable cost of goods sold for this trip, full-width.
            // Sent to the backend as purchase_price; blank → backend default applies.
            FleetInputField(
                value = state.purchasePrice,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        viewModel.sendIntent(CreateTripContract.Intent.UpdatePurchasePrice(newValue))
                    }
                },
                fieldType = FieldType.DECIMAL,
                label = stringResource(Res.string.trip_purchase_price_cogs_label),
                placeholder = stringResource(Res.string.trip_edit_placeholder_trip_price),
                leadingIcon = rupeeIcon,
                modifier = Modifier.fillMaxWidth()
            )
            // Non-error COGS hint (FleetInputField only shows supporting text on error).
            Text(
                text = stringResource(Res.string.trip_purchase_price_cogs_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = FleetTokens.Spacing.L,
                    top = FleetTokens.Spacing.XS
                )
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

            // Info banner - below Total Amount
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(FleetTokens.Radius.M)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = FleetTokens.Spacing.M,
                        vertical = FleetTokens.Spacing.S
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                    Text(
                        text = stringResource(Res.string.trip_create_pricing_banner),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
    }
}
