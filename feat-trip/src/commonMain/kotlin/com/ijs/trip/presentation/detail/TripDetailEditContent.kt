package com.ijs.trip.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.data.datasource.location.PlacePrediction

/**
 * Edit mode content for the trip detail screen.
 * Delegates to sub-composables for each section.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun EditModeContent(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Loading indicator for vehicles/drivers
        if (state.isLoadingVehiclesDrivers) {
            EditLoadingCard()
        }

        // Vehicle & Driver Selection
        EditVehicleDriverSection(state = state, viewModel = viewModel)

        // Route Section with Google Places
        EditRouteSection(state = state, viewModel = viewModel)

        // Schedule Section
        EditScheduleSection(state = state, viewModel = viewModel)

        // Cargo & Customer Section
        EditCargoCustomerSection(state = state, viewModel = viewModel)

        // Notes Section
        EditNotesSection(state = state, viewModel = viewModel)
    }
}

@Composable
private fun EditLoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading vehicles and drivers...", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/**
 * Vehicle and driver selection dropdowns for edit mode.
 */
@Composable
private fun EditVehicleDriverSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EditSectionHeader(icon = "🚛", title = "Vehicle & Driver")

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Dropdown
            EditDropdownField(
                label = "Select Vehicle",
                selectedText = state.selectedVehicle?.registrationNumber ?: "Choose a vehicle",
                isExpanded = state.showVehicleDropdown,
                onToggle = { viewModel.sendIntent(TripDetailContract.Intent.ToggleVehicleDropdown) },
                error = state.vehicleError,
                icon = "🚚"
            )

            if (state.showVehicleDropdown && state.vehicles.isNotEmpty()) {
                SelectionDropdownList(
                    items = state.vehicles,
                    getLabel = { it.registrationNumber },
                    getIsOccupied = { it.isOccupied },
                    getOccupiedInfo = { vehicle ->
                        vehicle.tripAssignment?.let { a -> "${a.plannedStart ?: ""} - ${a.plannedEnd ?: ""}" }
                            ?: "Currently assigned"
                    },
                    onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectVehicle(it)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Dropdown
            EditDropdownField(
                label = "Select Driver",
                selectedText = state.selectedDriver?.let { "${it.firstName} ${it.lastName}" } ?: "Choose a driver",
                isExpanded = state.showDriverDropdown,
                onToggle = { viewModel.sendIntent(TripDetailContract.Intent.ToggleDriverDropdown) },
                error = state.driverError,
                icon = "👤"
            )

            if (state.showDriverDropdown && state.drivers.isNotEmpty()) {
                SelectionDropdownList(
                    items = state.drivers,
                    getLabel = { "${it.firstName} ${it.lastName}" },
                    getIsOccupied = { it.isOccupied },
                    getOccupiedInfo = { driver ->
                        driver.tripAssignment?.let { a -> "${a.plannedStart ?: ""} - ${a.plannedEnd ?: ""}" }
                            ?: "Currently assigned"
                    },
                    onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectDriver(it)) }
                )
            }
        }
    }
}

/**
 * Generic dropdown selection list for vehicles/drivers.
 */
@Composable
private fun <T> SelectionDropdownList(
    items: List<T>,
    getLabel: (T) -> String,
    getIsOccupied: (T) -> Boolean,
    getOccupiedInfo: (T) -> String,
    onSelect: (T) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
            items.forEach { item ->
                val isOccupied = getIsOccupied(item)
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(enabled = !isOccupied) {
                        onSelect(item)
                    },
                    color = if (isOccupied) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = getLabel(item),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (isOccupied) {
                                Text(
                                    text = "Occupied: ${getOccupiedInfo(item)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        if (isOccupied) {
                            Text("🔒", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

/**
 * Route editing section with Google Places search.
 */
@Composable
private fun EditRouteSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EditSectionHeader(icon = "📍", title = "Route")

            Spacer(modifier = Modifier.height(16.dp))

            // Start Location with Search
            OutlinedTextField(
                value = state.startLocationAddress,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.SearchStartLocation(it)) },
                label = { Text("Start Location *") },
                leadingIcon = { Text("🟢", modifier = Modifier.padding(start = 8.dp)) },
                trailingIcon = {
                    if (state.isSearchingStartLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                isError = state.startLocationError != null,
                supportingText = state.startLocationError?.let { { Text(it) } },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Start Location Predictions
            LocationPredictionsDropdown(
                show = state.showStartLocationDropdown,
                predictions = state.startLocationPredictions,
                onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectStartLocationPrediction(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // End Location with Search
            OutlinedTextField(
                value = state.endLocationAddress,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.SearchEndLocation(it)) },
                label = { Text("End Location *") },
                leadingIcon = { Text("🔴", modifier = Modifier.padding(start = 8.dp)) },
                trailingIcon = {
                    if (state.isSearchingEndLocation) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                isError = state.endLocationError != null,
                supportingText = state.endLocationError?.let { { Text(it) } },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // End Location Predictions
            LocationPredictionsDropdown(
                show = state.showEndLocationDropdown,
                predictions = state.endLocationPredictions,
                onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectEndLocationPrediction(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Distance & Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.estimatedDistance,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEstimatedDistance(it)) },
                    label = { Text("Distance (km)") },
                    leadingIcon = { Text("🛣️", modifier = Modifier.padding(start = 8.dp)) },
                    trailingIcon = {
                        if (state.isCalculatingDistance) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.estimatedDuration.ifBlank { "—" },
                    onValueChange = { },
                    label = { Text("Est. Duration") },
                    leadingIcon = { Text("⏱️", modifier = Modifier.padding(start = 8.dp)) },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

/**
 * Location predictions dropdown for Google Places autocomplete.
 */
@Composable
private fun LocationPredictionsDropdown(
    show: Boolean,
    predictions: List<PlacePrediction>,
    onSelect: (PlacePrediction) -> Unit
) {
    if (show && predictions.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.heightIn(max = 200.dp)) {
                predictions.forEach { prediction ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(prediction) },
                        color = Color.Transparent
                    ) {
                        Text(
                            text = prediction.description,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Schedule editing section with departure/arrival date-time pickers.
 */
@Composable
private fun EditScheduleSection(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EditSectionHeader(icon = "📅", title = "Schedule (Optional)")

            Spacer(modifier = Modifier.height(16.dp))

            FleetDateTimePicker(
                date = state.departureDate,
                time = state.departureTime,
                onDateTimeChange = { newDate, newTime ->
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureDate(newDate))
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureTime(newTime))
                },
                label = "Departure",
                mode = PickerMode.DATE_TIME,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FleetDateTimePicker(
                date = state.arrivalDate,
                time = state.arrivalTime,
                onDateTimeChange = { newDate, newTime ->
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalDate(newDate))
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalTime(newTime))
                },
                label = "Arrival",
                mode = PickerMode.DATE_TIME,
                minDate = state.departureDate.ifBlank { null },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Reusable section header used across edit cards.
 */
@Composable
private fun EditSectionHeader(icon: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

