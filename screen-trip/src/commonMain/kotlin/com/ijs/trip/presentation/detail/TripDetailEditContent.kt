package com.ijs.trip.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.core.util.formatDateTimeForDisplay
import com.indusjs.fleet.data.datasource.location.PlacePrediction
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Formats a trip-assignment window (UTC epoch-millis start/end) for the
 * "occupied" hint. Treats null/0 as unset.
 */
private fun formatAssignmentWindow(startMs: Long?, endMs: Long?): String {
    val start = startMs?.takeIf { it > 0L }?.let { formatDateTimeForDisplay(it) } ?: ""
    val end = endMs?.takeIf { it > 0L }?.let { formatDateTimeForDisplay(it) } ?: ""
    return "$start - $end"
}

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
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)) {
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
    FleetSectionCard {
        Box(
            modifier = Modifier.fillMaxWidth().padding(FleetTokens.Spacing.L),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.L))
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                Text(stringResource(Res.string.trip_detail_loading_vehicles_drivers), style = MaterialTheme.typography.bodyMedium)
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
    FleetTitledSectionCard(title = stringResource(Res.string.trip_edit_section_vehicle_driver), emoji = "🚛") {
            // Vehicle Dropdown
            EditDropdownField(
                label = stringResource(Res.string.trip_edit_label_vehicle),
                selectedText = state.selectedVehicle?.registrationNumber ?: stringResource(Res.string.trip_edit_placeholder_vehicle),
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
                        vehicle.tripAssignment?.let { a -> formatAssignmentWindow(a.plannedStart, a.plannedEnd) }
                            ?: stringResource(Res.string.trip_edit_currently_assigned)
                    },
                    onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectVehicle(it)) }
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            // Driver Dropdown
            EditDropdownField(
                label = stringResource(Res.string.trip_edit_label_driver),
                selectedText = state.selectedDriver?.let { "${it.firstName} ${it.lastName}" } ?: stringResource(Res.string.trip_edit_placeholder_driver),
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
                        driver.tripAssignment?.let { a -> formatAssignmentWindow(a.plannedStart, a.plannedEnd) }
                            ?: stringResource(Res.string.trip_edit_currently_assigned)
                    },
                    onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectDriver(it)) }
                )
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
    getOccupiedInfo: @Composable (T) -> String,
    onSelect: (T) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FleetTokens.Radius.L),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = FleetTokens.Elevation.Dropdown)
    ) {
        Column(modifier = Modifier.heightIn(max = FleetTokens.Width.DropdownMaxHeight).verticalScroll(rememberScrollState())) {
            items.forEach { item ->
                val isOccupied = getIsOccupied(item)
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable(enabled = !isOccupied) {
                        onSelect(item)
                    },
                    color = if (isOccupied) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(FleetTokens.Spacing.M),
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
                                    text = stringResource(Res.string.trip_edit_occupied_window, getOccupiedInfo(item)),
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
    FleetTitledSectionCard(title = stringResource(Res.string.trip_edit_section_route), emoji = "📍") {
            // Start Location with Search
            FleetInputField(
                value = state.startLocationAddress,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.SearchStartLocation(it)) },
                fieldType = FieldType.ADDRESS,
                label = stringResource(Res.string.trip_edit_start_location),
                leadingIcon = { Text("🟢", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
                trailingIcon = {
                    if (state.isSearchingStartLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(FleetTokens.IconSize.M),
                            strokeWidth = FleetTokens.Height.ProgressStroke
                        )
                    }
                },
                isError = state.startLocationError != null,
                errorMessage = state.startLocationError,
                modifier = Modifier.fillMaxWidth()
            )

            // Start Location Predictions
            LocationPredictionsDropdown(
                show = state.showStartLocationDropdown,
                predictions = state.startLocationPredictions,
                onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectStartLocationPrediction(it)) }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            // End Location with Search
            FleetInputField(
                value = state.endLocationAddress,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.SearchEndLocation(it)) },
                fieldType = FieldType.ADDRESS,
                label = stringResource(Res.string.trip_edit_end_location),
                leadingIcon = { Text("🔴", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
                trailingIcon = {
                    if (state.isSearchingEndLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(FleetTokens.IconSize.M),
                            strokeWidth = FleetTokens.Height.ProgressStroke
                        )
                    }
                },
                isError = state.endLocationError != null,
                errorMessage = state.endLocationError,
                modifier = Modifier.fillMaxWidth()
            )

            // End Location Predictions
            LocationPredictionsDropdown(
                show = state.showEndLocationDropdown,
                predictions = state.endLocationPredictions,
                onSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectEndLocationPrediction(it)) }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            // Distance & Duration Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                FleetInputField(
                    value = state.estimatedDistance,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEstimatedDistance(it)) },
                    fieldType = FieldType.DECIMAL,
                    label = stringResource(Res.string.trip_edit_distance),
                    leadingIcon = { Text("🛣️", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
                    trailingIcon = {
                        if (state.isCalculatingDistance) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(FleetTokens.IconSize.M),
                                strokeWidth = FleetTokens.Height.ProgressStroke
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                FleetInputField(
                    value = state.estimatedDuration.ifBlank { "—" },
                    onValueChange = { },
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.trip_edit_est_duration),
                    leadingIcon = { Text("⏱️", modifier = Modifier.padding(start = FleetTokens.Spacing.S)) },
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
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
            shape = RoundedCornerShape(FleetTokens.Radius.M),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.heightIn(max = FleetTokens.Width.DropdownMaxHeight)) {
                predictions.forEach { prediction ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(prediction) },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = prediction.description,
                            modifier = Modifier.padding(FleetTokens.Spacing.M),
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
    FleetTitledSectionCard(title = stringResource(Res.string.trip_edit_section_schedule), emoji = "📅") {
            FleetDateTimePicker(
                date = state.departureDate,
                time = state.departureTime,
                onDateTimeChange = { newDate, newTime ->
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureDate(newDate))
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureTime(newTime))
                },
                label = stringResource(Res.string.trip_edit_departure),
                mode = PickerMode.DATE_TIME,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            FleetDateTimePicker(
                date = state.arrivalDate,
                time = state.arrivalTime,
                onDateTimeChange = { newDate, newTime ->
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalDate(newDate))
                    viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalTime(newTime))
                },
                label = stringResource(Res.string.trip_edit_arrival),
                mode = PickerMode.DATE_TIME,
                minDate = state.departureDate.ifBlank { null },
                modifier = Modifier.fillMaxWidth()
            )
    }
}


