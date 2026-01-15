package com.indusjs.fleet.presentation.trips.create

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
import com.indusjs.fleet.core.ui.FleetMobileField
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Create Trip Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    viewModel: CreateTripViewModel,
    onNavigateBack: () -> Unit = {},
    onTripCreated: (String) -> Unit = {}
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
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Create Trip")
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
                            text = "${state.formCompletionPercentage}% complete",
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
                            contentDescription = "Back",
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
                            Text("Cancel")
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
                            Text(if (state.isSaving) "Creating..." else "Create Trip")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoadingData) {
            LoadingContent(message = "Loading vehicles and drivers...")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vehicle & Driver Selection
                item {
                    SectionCard(title = "🚚 Vehicle & Driver") {
                        VehicleDriverSelectionSection(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }

                // Route Section
                item {
                    SectionCard(title = "📍 Route") {
                        RouteSection(state = state, viewModel = viewModel)
                    }
                }

                // Schedule Section
                item {
                    SectionCard(title = "📅 Schedule") {
                        ScheduleSection(state = state, viewModel = viewModel)
                    }
                }

                // Cargo Section
                item {
                    SectionCard(title = "📦 Cargo Details") {
                        CargoSection(state = state, viewModel = viewModel)
                    }
                }

                // Priority Section (above Customer Details)
                item {
                    SectionCard(title = "🎯 Priority") {
                        PrioritySection(state = state, viewModel = viewModel)
                    }
                }

                // Customer Section
                item {
                    SectionCard(title = "👤 Customer Details") {
                        CustomerSection(state = state, viewModel = viewModel)
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
                    SectionCard(title = "📝 Notes") {
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateNotes(it)) },
                            label = { Text("Notes") },
                            placeholder = { Text("Add any additional notes...") },
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
                        Text("Creating trip...")
                    }
                }
            }
        }
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
                label = { Text("Select Vehicle *") },
                placeholder = { Text("Choose a vehicle") },
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
                onDismissRequest = { viewModel.sendIntent(CreateTripContract.Intent.ToggleVehicleDropdown) }
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
                                                    text = "OCCUPIED",
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
                                    if (isOccupied && vehicle.tripAssignment != null) {
                                        Text(
                                            text = "🕐 ${vehicle.tripAssignment.plannedStart ?: "N/A"} - ${vehicle.tripAssignment.plannedEnd ?: "N/A"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
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
                label = { Text("Select Driver *") },
                placeholder = { Text("Choose a driver") },
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
                onDismissRequest = { viewModel.sendIntent(CreateTripContract.Intent.ToggleDriverDropdown) }
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
                                                    text = "OCCUPIED",
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
                                    if (isOccupied && driver.tripAssignment != null) {
                                        Text(
                                            text = "🕐 ${driver.tripAssignment.plannedStart ?: "N/A"} - ${driver.tripAssignment.plannedEnd ?: "N/A"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
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
            label = "Start Location *",
            placeholder = "Search for a location...",
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
                label = { Text("Estimated Distance (km)") },
                placeholder = { Text("Auto-calculated") },
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
                                text = "🔄 Calculating road distance...",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        state.estimatedDistance.isNotBlank() && state.estimatedDuration.isNotBlank() -> {
                            Text(
                                text = "✓ Road distance | Est. travel time: ${state.estimatedDuration}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        state.estimatedDistance.isNotBlank() -> {
                            Text(
                                text = "✓ Distance calculated",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Text(
                                text = "Select both locations to auto-calculate",
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
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
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
            label = "Departure Date & Time *",
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
            label = "Expected Arrival Date & Time",
            minDate = state.departureDate.ifBlank { FleetDateTime.today() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CargoSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    var showCargoTypeDropdown by remember { mutableStateOf(false) }
    var showWeightUnitDropdown by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Cargo Type Dropdown with error support
        ExposedDropdownMenuBox(
            expanded = showCargoTypeDropdown,
            onExpandedChange = { showCargoTypeDropdown = it }
        ) {
            OutlinedTextField(
                value = if (state.cargoType.isNotBlank()) state.cargoType.replaceFirstChar { it.uppercaseChar() } else "",
                onValueChange = {},
                label = { Text("Cargo Type *") },
                placeholder = { Text("Select cargo type") },
                readOnly = true,
                isError = state.cargoTypeError != null,
                supportingText = state.cargoTypeError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCargoTypeDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showCargoTypeDropdown,
                onDismissRequest = { showCargoTypeDropdown = false }
            ) {
                state.cargoTypeOptions.forEach { cargo ->
                    DropdownMenuItem(
                        text = { Text(cargo.replaceFirstChar { it.uppercaseChar() }) },
                        onClick = {
                            viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoType(cargo))
                            showCargoTypeDropdown = false
                        }
                    )
                }
            }
        }

        // Cargo Description - single line
        OutlinedTextField(
            value = state.cargoDescription,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoDescription(it)) },
            label = { Text("Cargo Description") },
            placeholder = { Text("e.g., Office furniture") },
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
                label = { Text("Cargo Weight *") },
                placeholder = { Text("e.g., 500") },
                isError = state.cargoWeightError != null,
                supportingText = state.cargoWeightError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            // Weight Unit Dropdown - NOW REQUIRED
            ExposedDropdownMenuBox(
                expanded = showWeightUnitDropdown,
                onExpandedChange = { showWeightUnitDropdown = it },
                modifier = Modifier.weight(0.6f)
            ) {
                OutlinedTextField(
                    value = state.weightUnit,
                    onValueChange = {},
                    label = { Text("Unit *") },
                    placeholder = { Text("Select") },
                    readOnly = true,
                    isError = state.weightUnitError != null,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showWeightUnitDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showWeightUnitDropdown,
                    onDismissRequest = { showWeightUnitDropdown = false }
                ) {
                    state.weightUnitOptions.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                viewModel.sendIntent(CreateTripContract.Intent.UpdateWeightUnit(unit))
                                showWeightUnitDropdown = false
                            }
                        )
                    }
                }
            }
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

@Composable
private fun CustomerSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Customer Name - NOW REQUIRED
        OutlinedTextField(
            value = state.customerName,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCustomerName(it)) },
            label = { Text("Customer Name *") },
            placeholder = { Text("e.g., ABC Corporation") },
            singleLine = true,
            isError = state.customerNameError != null,
            supportingText = state.customerNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )

        // Customer Contact - NOW REQUIRED
        FleetMobileField(
            rawValue = state.customerContact,
            onRawValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCustomerContact(it)) },
            label = "Customer Contact *",
            placeholder = "Enter 10-digit mobile",
            isError = state.customerContactError != null,
            errorMessage = state.customerContactError
        )
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
                        text = "Trip Pricing (Expected) *",
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
                        text = "Cost + Profit (Can be edited later)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info banner - compact
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
                        text = "Pricing helps track Profit & Loss in financial reports",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = { Text("Total Amount (₹) *") },
                placeholder = { Text("e.g., 25000") },
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
        }
    }
}
