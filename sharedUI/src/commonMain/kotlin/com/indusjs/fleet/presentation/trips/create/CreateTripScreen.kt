package com.indusjs.fleet.presentation.trips.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import kotlinx.coroutines.flow.collectLatest

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
                title = { Text("Create Trip") },
                navigationIcon = {
                    TextButton(
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.NavigateBack) }
                    ) {
                        Text("← Back", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
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
                    SectionCard(title = "🚗 Vehicle & Driver") {
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

                // Cargo & Customer Section
                item {
                    SectionCard(title = "📦 Cargo & Customer") {
                        CargoSection(state = state, viewModel = viewModel)
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
                state.vehicles.forEach { vehicle ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = vehicle.registrationNumber,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${vehicle.make} ${vehicle.model} (${vehicle.year})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.SelectVehicle(vehicle)) }
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
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.SelectDriver(driver)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Start Location
        OutlinedTextField(
            value = state.startLocation,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateStartLocation(it)) },
            label = { Text("Start Location *") },
            placeholder = { Text("e.g., 123 Main St, New Delhi") },
            leadingIcon = { Text("🟢", modifier = Modifier.padding(start = 12.dp)) },
            isError = state.startLocationError != null,
            supportingText = state.startLocationError?.let { { Text(it) } },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.startLat,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateStartLat(it)) },
                label = { Text("Latitude") },
                placeholder = { Text("e.g., 28.6139") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.startLng,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateStartLng(it)) },
                label = { Text("Longitude") },
                placeholder = { Text("e.g., 77.2090") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // End Location
        OutlinedTextField(
            value = state.endLocation,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEndLocation(it)) },
            label = { Text("End Location *") },
            placeholder = { Text("e.g., 456 Taj Mahal Road, Agra") },
            leadingIcon = { Text("🔴", modifier = Modifier.padding(start = 12.dp)) },
            isError = state.endLocationError != null,
            supportingText = state.endLocationError?.let { { Text(it) } },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.endLat,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEndLat(it)) },
                label = { Text("Latitude") },
                placeholder = { Text("e.g., 27.1767") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.endLng,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEndLng(it)) },
                label = { Text("Longitude") },
                placeholder = { Text("e.g., 78.0081") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = state.estimatedDistance,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEstimatedDistance(it)) },
            label = { Text("Estimated Distance (km)") },
            placeholder = { Text("e.g., 233.5") },
            leadingIcon = { Text("🛣️", modifier = Modifier.padding(start = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Formats date input with auto-delimiter for Indian format (DD-MM-YYYY).
 * Adds "-" automatically after day and month.
 */
private fun formatDateInput(input: String): String {
    // Remove all non-digit characters
    val digitsOnly = input.filter { it.isDigit() }

    return buildString {
        digitsOnly.forEachIndexed { index, char ->
            if (index == 2 || index == 4) {
                append("-")
            }
            if (index < 8) { // Max 8 digits: DDMMYYYY
                append(char)
            }
        }
    }
}

/**
 * Formats time input with auto-delimiter (HH:MM).
 * Adds ":" automatically after hours.
 */
private fun formatTimeInput(input: String): String {
    // Remove all non-digit characters
    val digitsOnly = input.filter { it.isDigit() }

    return buildString {
        digitsOnly.forEachIndexed { index, char ->
            if (index == 2) {
                append(":")
            }
            if (index < 4) { // Max 4 digits: HHMM
                append(char)
            }
        }
    }
}

@Composable
private fun ScheduleSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Departure Section
        Text(
            text = "🚀 Departure (Required)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.departureDate,
                onValueChange = {
                    val formatted = formatDateInput(it)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateDepartureDate(formatted))
                },
                label = { Text("Date") },
                placeholder = { Text("DD-MM-YYYY") },
                isError = state.departureDateError != null,
                supportingText = state.departureDateError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.departureTime,
                onValueChange = {
                    val formatted = formatTimeInput(it)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateDepartureTime(formatted))
                },
                label = { Text("Time (24hr)") },
                placeholder = { Text("HH:MM") },
                isError = state.departureTimeError != null,
                supportingText = state.departureTimeError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        // Arrival Section
        Text(
            text = "🏁 Expected Arrival (Optional)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.arrivalDate,
                onValueChange = {
                    val formatted = formatDateInput(it)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateArrivalDate(formatted))
                },
                label = { Text("Date") },
                placeholder = { Text("DD-MM-YYYY") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.arrivalTime,
                onValueChange = {
                    val formatted = formatTimeInput(it)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateArrivalTime(formatted))
                },
                label = { Text("Time (24hr)") },
                placeholder = { Text("HH:MM") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CargoSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Cargo Type selector
        Column {
            Text(
                text = "Cargo Type *",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.cargoTypeOptions.forEach { cargo ->
                    FilterChip(
                        selected = state.cargoType.equals(cargo, ignoreCase = true),
                        onClick = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoType(cargo)) },
                        label = { Text(cargo.replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.cargoDescription,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoDescription(it)) },
            label = { Text("Cargo Description") },
            placeholder = { Text("e.g., Office furniture and equipment") },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.cargoWeight,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCargoWeight(it)) },
            label = { Text("Cargo Weight (kg)") },
            placeholder = { Text("e.g., 500") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        OutlinedTextField(
            value = state.customerName,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCustomerName(it)) },
            label = { Text("Customer Name") },
            placeholder = { Text("e.g., ABC Corporation") },
            leadingIcon = { Text("👤", modifier = Modifier.padding(start = 12.dp)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.customerContact,
            onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateCustomerContact(it)) },
            label = { Text("Customer Contact") },
            placeholder = { Text("e.g., 9876543210") },
            leadingIcon = { Text("📱", modifier = Modifier.padding(start = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Priority selector
        Column {
            Text(
                text = "Priority",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    }
}

