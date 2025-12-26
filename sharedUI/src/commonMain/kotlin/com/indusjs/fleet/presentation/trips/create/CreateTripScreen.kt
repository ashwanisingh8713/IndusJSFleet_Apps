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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("Create Trip") },
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

        // Start Location Coordinates (auto-filled or manual)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.startLat,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateStartLat(it)) },
                label = { Text("Latitude") },
                placeholder = { Text("Auto-filled") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            OutlinedTextField(
                value = state.startLng,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateStartLng(it)) },
                label = { Text("Longitude") },
                placeholder = { Text("Auto-filled") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        // End Location Coordinates (auto-filled or manual)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.endLat,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEndLat(it)) },
                label = { Text("Latitude") },
                placeholder = { Text("Auto-filled") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            OutlinedTextField(
                value = state.endLng,
                onValueChange = { viewModel.sendIntent(CreateTripContract.Intent.UpdateEndLng(it)) },
                label = { Text("Longitude") },
                placeholder = { Text("Auto-filled") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

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
            singleLine = false,
            maxLines = 2,
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

/**
 * Visual transformation for date input (DD-MM-YYYY format).
 * Displays delimiters visually while keeping raw digits as actual value.
 */
private class DateVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = text.text.take(8) // Max 8 digits: DDMMYYYY
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1 || i == 3) {
                out.append("-")
            }
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    offset <= 8 -> offset + 2
                    else -> 10
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    offset <= 10 -> offset - 2
                    else -> 8
                }
            }
        }

        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(out.toString()),
            offsetMapping
        )
    }
}

/**
 * Visual transformation for time input (HH:MM format).
 * Displays colon visually while keeping raw digits as actual value.
 */
private class TimeVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = text.text.take(4) // Max 4 digits: HHMM
        val out = StringBuilder()

        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1) {
                out.append(":")
            }
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 4 -> offset + 1
                    else -> 5
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                return when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else -> 4
                }
            }
        }

        return androidx.compose.ui.text.input.TransformedText(
            androidx.compose.ui.text.AnnotatedString(out.toString()),
            offsetMapping
        )
    }
}

/**
 * Filters input to only allow digits for date/time fields.
 */
private fun filterDigitsOnly(input: String, maxLength: Int): String {
    return input.filter { it.isDigit() }.take(maxLength)
}

@Composable
private fun ScheduleSection(
    state: CreateTripContract.State,
    viewModel: CreateTripViewModel
) {
    // Remember visual transformations
    val dateVisualTransformation = remember { DateVisualTransformation() }
    val timeVisualTransformation = remember { TimeVisualTransformation() }

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
                onValueChange = { input ->
                    val filtered = filterDigitsOnly(input, 8)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateDepartureDate(filtered))
                },
                label = { Text("Date") },
                placeholder = { Text("DD-MM-YYYY") },
                isError = state.departureDateError != null,
                supportingText = state.departureDateError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = dateVisualTransformation,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.departureTime,
                onValueChange = { input ->
                    val filtered = filterDigitsOnly(input, 4)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateDepartureTime(filtered))
                },
                label = { Text("Time (24hr)") },
                placeholder = { Text("HH:MM") },
                isError = state.departureTimeError != null,
                supportingText = state.departureTimeError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = timeVisualTransformation,
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
                onValueChange = { input ->
                    val filtered = filterDigitsOnly(input, 8)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateArrivalDate(filtered))
                },
                label = { Text("Date") },
                placeholder = { Text("DD-MM-YYYY") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = dateVisualTransformation,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.arrivalTime,
                onValueChange = { input ->
                    val filtered = filterDigitsOnly(input, 4)
                    viewModel.sendIntent(CreateTripContract.Intent.UpdateArrivalTime(filtered))
                },
                label = { Text("Time (24hr)") },
                placeholder = { Text("HH:MM") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = timeVisualTransformation,
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

