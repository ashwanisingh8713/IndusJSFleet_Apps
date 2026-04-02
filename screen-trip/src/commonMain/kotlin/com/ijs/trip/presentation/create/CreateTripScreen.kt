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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.customer.CustomerDetailsSection
import com.indusjs.uicomponents.customer.CustomerSelectionBottomSheet
import com.ijs.customer.presentation.toSelectableCustomer
import com.ijs.customer.presentation.toSelectableCustomerList
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.ijs.customer.domain.entity.Customer
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
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
                            text = stringResource(
                                Res.string.trip_create_percent_complete,
                                state.formCompletionPercentage
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
                                text = "⚠️ $errorCount field(s) need attention",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
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
                        OutlinedButton(
                            onClick = { viewModel.sendIntent(CreateTripContract.Intent.NavigateBack) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.cancel))
                        }

                        Button(
                            onClick = { viewModel.sendIntent(CreateTripContract.Intent.CreateTrip) },
                            modifier = Modifier.weight(1f),
                            enabled = state.canSave
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                if (state.isSaving) stringResource(Res.string.action_creating)
                                else stringResource(Res.string.trip_create_title)
                            )
                        }
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
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Schedule Section - First (when is the trip?)
                item {
                    SectionCard(title = stringResource(Res.string.trip_create_section_schedule)) {
                        ScheduleSection(state = state, viewModel = viewModel)
                    }
                }

                // Vehicle & Driver Selection
                item {
                    SectionCard(title = stringResource(Res.string.trip_create_section_vehicle_driver)) {
                        VehicleDriverSelectionSection(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }

                // Route Section
                item {
                    SectionCard(title = stringResource(Res.string.trip_create_section_route)) {
                        RouteSection(state = state, viewModel = viewModel)
                    }
                }

                // Cargo Section
                item {
                    SectionCard(title = stringResource(Res.string.trip_create_section_cargo)) {
                        CargoSection(state = state, viewModel = viewModel)
                    }
                }

                // Priority Section (above Customer Details)
                item {
                    SectionCard(title = stringResource(Res.string.trip_create_section_priority)) {
                        PrioritySection(state = state, viewModel = viewModel)
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
                        onSelectClick = { viewModel.sendIntent(CreateTripContract.Intent.ToggleCustomerBottomSheet) },
                        onClearClick = { viewModel.sendIntent(CreateTripContract.Intent.ClearCustomerSelection) },
                        onRefreshClick = { viewModel.sendIntent(CreateTripContract.Intent.RefreshCustomers) },
                        onAddNewClick = { viewModel.sendIntent(CreateTripContract.Intent.NavigateToAddCustomer) }
                    )
                }

                // Pricing Section - Only visible to Owner and General Manager
                if (state.canViewTripPrice) {
                    item {
                        PricingSection(state = state, viewModel = viewModel)
                    }
                }

                // Notes Section
                item {
                    SectionCard(title = stringResource(Res.string.trip_create_section_notes)) {
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateNotes(it)) },
                            label = { Text(stringResource(Res.string.trip_create_notes_label)) },
                            placeholder = { Text(stringResource(Res.string.trip_create_notes_placeholder)) },
                            singleLine = false,
                            maxLines = 4,
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

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDriverSelectionSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                        Text(
                                            text = "🕐 ${assignment.plannedStart ?: stringResource(Res.string.vehicle_route_na)} - ${assignment.plannedEnd ?: stringResource(Res.string.vehicle_route_na)}",
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
                                        Text(
                                            text = "🕐 ${assignment.plannedStart ?: stringResource(Res.string.vehicle_route_na)} - ${assignment.plannedEnd ?: stringResource(Res.string.vehicle_route_na)}",
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
            label = "End Location *",
            placeholder = "Search for a location...",
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
                leadingIcon = { Text("🛣️", modifier = Modifier.padding(start = 12.dp)) },
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
                        Text("📍", style = MaterialTheme.typography.bodyMedium)
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FleetDropdown(
            label = stringResource(Res.string.trip_create_cargo_type_required),
            options = state.cargoTypeOptions.map { cargo ->
                DropdownOption(
                    id = cargo,
                    label = cargo.replaceFirstChar { it.uppercaseChar() }
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
        OutlinedTextField(
            value = state.cargoDescription,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoDescription(it)) },
            label = { Text(stringResource(Res.string.trip_create_cargo_desc_label)) },
            placeholder = { Text(stringResource(Res.string.trip_create_cargo_desc_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Cargo Weight with Unit dropdown and error support
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Weight input - accepts only decimal numbers - NOW REQUIRED
            OutlinedTextField(
                value = state.cargoWeight,
                onValueChange = { newValue ->
                    // Only allow digits and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoWeight(newValue))
                    }
                },
                label = { Text(stringResource(Res.string.trip_create_cargo_weight_required)) },
                placeholder = { Text(stringResource(Res.string.trip_create_cargo_weight_placeholder)) },
                isError = state.cargoWeightError != null,
                supportingText = state.cargoWeightError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            FleetDropdown(
                label = stringResource(Res.string.trip_create_unit_required),
                options = state.weightUnitOptions.map { DropdownOption(id = it, label = it) },
                selectedOptionId = state.weightUnit.takeIf { it.isNotBlank() },
                onOptionSelected = { viewModel.sendIntent(CreateTripContract.Intent.UpdateWeightUnit(it)) },
                modifier = Modifier.weight(0.6f),
                placeholder = stringResource(Res.string.trip_create_select_placeholder),
                isError = state.weightUnitError != null,
                errorMessage = state.weightUnitError
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrioritySection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.priorityOptions.forEach { priority ->
            FilterChip(
                selected = state.priority.equals(priority, ignoreCase = true),
                onClick = { viewModel.sendIntent(CreateTripContract.Intent.UpdatePriority(priority)) },
                label = { Text(priority.replaceFirstChar { it.uppercaseChar() }) }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Compact header with title and info in single row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💰",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.trip_create_pricing_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = stringResource(Res.string.trip_create_pricing_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price input - compact with inline error
            OutlinedTextField(
                value = state.tripPrice,
                onValueChange = { newValue ->
                    // Only allow digits and decimal point
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        viewModel.sendIntent(CreateTripContract.Intent.UpdateTripPrice(newValue))
                    }
                },
                label = { Text(stringResource(Res.string.trip_create_total_amount_inr)) },
                placeholder = { Text(stringResource(Res.string.trip_edit_placeholder_trip_price)) },
                singleLine = true,
                isError = state.tripPriceError != null,
                supportingText = if (state.tripPriceError != null) {
                    { Text(state.tripPriceError!!, color = MaterialTheme.colorScheme.error) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Info banner - below Total Amount
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.trip_create_pricing_banner),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
