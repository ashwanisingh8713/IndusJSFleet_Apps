package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.entity.VehicleType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditModeContent(
    state: VehicleDetailContract.State,
    viewModel: VehicleDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // ==================== Vehicle Information Section ====================
        FleetTitledSectionCard(
            title = stringResource(Res.string.vehicle_edit_info),
            emoji = "🚗",
            accent = MaterialTheme.colorScheme.primary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Registration (read-only)
                OutlinedTextField(
                    value = state.registrationNumber,
                    onValueChange = { },
                    label = { Text(stringResource(Res.string.vehicles_registration)) },
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.make,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateMake(it)) },
                        label = { Text(stringResource(Res.string.vehicle_label_make)) },
                        placeholder = { Text(stringResource(Res.string.vehicle_edit_placeholder_make)) },
                        isError = state.makeError != null,
                        supportingText = state.makeError?.let { { Text(it.resolve()) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.model,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateModel(it)) },
                        label = { Text(stringResource(Res.string.vehicle_label_model)) },
                        placeholder = { Text(stringResource(Res.string.vehicle_edit_placeholder_model)) },
                        isError = state.modelError != null,
                        supportingText = state.modelError?.let { { Text(it.resolve()) } },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.year,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateYear(it)) },
                        label = { Text(stringResource(Res.string.vehicle_label_year)) },
                        placeholder = { Text(stringResource(Res.string.vehicle_edit_placeholder_year)) },
                        isError = state.yearError != null,
                        supportingText = state.yearError?.let { { Text(it.resolve()) } },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.mileage,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateMileage(it)) },
                        label = { Text(stringResource(Res.string.vehicle_edit_mileage)) },
                        placeholder = { Text(stringResource(Res.string.vehicle_edit_mileage_placeholder)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Vehicle Type selector
                Column {
                    Text(
                        text = stringResource(Res.string.vehicle_edit_vehicle_type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VehicleType.entries.forEach { type ->
                            FilterChip(
                                selected = state.vehicleType == type,
                                onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateVehicleType(type)) },
                                label = { Text(getVehicleTypeLabel(type)) },
                                leadingIcon = if (state.vehicleType == type) {
                                    { Text("✓") }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // ==================== Specifications Section ====================
        FleetTitledSectionCard(
            title = stringResource(Res.string.vehicle_edit_specs),
            emoji = "⚙️",
            accent = MaterialTheme.colorScheme.primary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Fuel Type selector
                Column {
                    Text(
                        text = stringResource(Res.string.vehicle_edit_fuel_type),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.fuelTypeOptions.forEach { fuel ->
                            FilterChip(
                                selected = state.fuelType.equals(fuel, ignoreCase = true),
                                onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateFuelType(fuel)) },
                                label = { Text(fuel) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = state.color,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateColor(it)) },
                        label = { Text(stringResource(Res.string.vehicle_edit_color)) },
                        placeholder = { Text(stringResource(Res.string.vehicle_edit_color_placeholder)) },
                        leadingIcon = { Text("🎨", modifier = Modifier.padding(start = 8.dp)) },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.capacity,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateCapacity(it)) },
                        label = { Text(stringResource(Res.string.vehicle_edit_capacity)) },
                        placeholder = { Text(stringResource(Res.string.vehicle_edit_capacity_placeholder)) },
                        leadingIcon = { Text("👥", modifier = Modifier.padding(start = 8.dp)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // ==================== Driver Assignment Section ====================
        FleetTitledSectionCard(
            title = stringResource(Res.string.vehicle_edit_assigned_driver),
            emoji = "👤",
            accent = MaterialTheme.colorScheme.primary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Driver Dropdown
                Column {
                    Surface(
                        onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ToggleDriverDropdown) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isLoadingDrivers) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(stringResource(Res.string.vehicle_edit_loading_drivers), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(
                                    text = state.selectedDriver?.let { "${it.firstName} ${it.lastName}" }
                                        ?: stringResource(Res.string.vehicle_edit_no_driver_assigned),
                                    modifier = Modifier.weight(1f),
                                    color = if (state.selectedDriver != null)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(if (state.showDriverDropdown) "▲" else "▼")
                            }
                        }
                    }

                    // Driver Dropdown List
                    if (state.showDriverDropdown && state.drivers.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                                // Option to remove driver
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.sendIntent(VehicleDetailContract.Intent.SelectDriver(null))
                                    },
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.vehicle_edit_no_driver),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Available drivers
                                state.drivers.forEach { driver ->
                                    val isSelected = state.selectedDriver?.id == driver.id
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            viewModel.sendIntent(VehicleDetailContract.Intent.SelectDriver(driver))
                                        },
                                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${driver.firstName} ${driver.lastName}",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                driver.mobile?.let {
                                                    Text(
                                                        text = it,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Text("✓", color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== Caretaker Assignment Section ====================
        CaretakerSectionCard(
            selectedCaretaker = state.selectedCaretaker?.toCaretakerInfo(),
            caretakers = state.caretakers.toCaretakerInfoList(),
            onCaretakerSelected = { caretakerInfo ->
                val dto = state.caretakers.find { it.id.toString() == caretakerInfo?.id }
                viewModel.sendIntent(VehicleDetailContract.Intent.SelectCaretaker(dto))
            },
            onRefresh = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshCaretakers) },
            isLoading = state.isLoadingCaretakers
        )
    }
}


/**
 * Composable to display vehicle type icon.
 */
@Composable
internal fun VehicleTypeIcon(
    type: VehicleType,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val iconRes = when (type) {
        VehicleType.TRUCK -> Res.drawable.ic_truck
        VehicleType.VAN -> Res.drawable.ic_van
        VehicleType.CAR -> Res.drawable.ic_car
        VehicleType.BUS -> Res.drawable.ic_bus
        VehicleType.MOTORCYCLE -> Res.drawable.ic_motorcycle
        VehicleType.TRAILER -> Res.drawable.ic_trailer
    }
    Icon(
        painter = painterResource(iconRes),
        contentDescription = type.name,
        modifier = modifier,
        tint = tint
    )
}

internal fun getVehicleTypeLabel(type: VehicleType): String = when (type) {
    VehicleType.TRUCK -> "Truck"
    VehicleType.VAN -> "Van"
    VehicleType.CAR -> "Car"
    VehicleType.BUS -> "Bus"
    VehicleType.MOTORCYCLE -> "Motorcycle"
    VehicleType.TRAILER -> "Trailer"
}

internal fun getStatusLabel(status: VehicleStatus): String =
    VehicleStatus.getDisplayLabel(status)

/** Epoch millis -> "DD-MMM-YYYY" display (e.g. "04-Jan-2026"). "N/A" when unset. */
internal fun formatDate(timestamp: Long): String {
    if (timestamp <= 0L) return "N/A"
    return com.indusjs.fleet.core.util.formatDateToHumanReadable(timestamp).ifBlank { "N/A" }
}

/**
 * Epoch millis (UTC) -> "DD-MMM-YYYY" display. "N/A" when null/unset.
 * (Formerly parsed ISO strings; the API now sends epoch-millis numbers.)
 */
internal fun formatIsoDateToDisplay(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0L) return "N/A"
    return com.indusjs.fleet.core.util.formatDateToHumanReadable(timestamp).ifBlank { "N/A" }
}

// ==================== Costs Tab ====================

/**
 * Costs Tab Content - Shows Trip Costs and Maintenance Costs with overlay filter sheet.
 */

