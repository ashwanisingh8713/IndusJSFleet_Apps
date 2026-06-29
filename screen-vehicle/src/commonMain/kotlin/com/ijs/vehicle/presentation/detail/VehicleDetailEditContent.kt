package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.DropdownOption
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetDisplayField
import com.indusjs.uicomponents.components.FleetDropdown
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
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
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)) {
        // ==================== Vehicle Information Section ====================
        FleetTitledSectionCard(
            title = stringResource(Res.string.vehicle_edit_info),
            iconRes = Res.drawable.ic_truck,
            accent = MaterialTheme.colorScheme.primary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                // Registration (read-only display, not an input)
                FleetDisplayField(
                    label = stringResource(Res.string.vehicles_registration),
                    value = state.registrationNumber
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetInputField(
                        value = state.make,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateMake(it)) },
                        fieldType = FieldType.DEFAULT,
                        label = stringResource(Res.string.vehicle_label_make),
                        placeholder = stringResource(Res.string.vehicle_edit_placeholder_make),
                        isError = state.makeError != null,
                        errorMessage = state.makeError?.resolve(),
                        modifier = Modifier.weight(1f)
                    )

                    FleetInputField(
                        value = state.model,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateModel(it)) },
                        fieldType = FieldType.DEFAULT,
                        label = stringResource(Res.string.vehicle_label_model),
                        placeholder = stringResource(Res.string.vehicle_edit_placeholder_model),
                        isError = state.modelError != null,
                        errorMessage = state.modelError?.resolve(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetInputField(
                        value = state.year,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateYear(it)) },
                        fieldType = FieldType.NUMBER,
                        label = stringResource(Res.string.vehicle_label_year),
                        placeholder = stringResource(Res.string.vehicle_edit_placeholder_year),
                        isError = state.yearError != null,
                        errorMessage = state.yearError?.resolve(),
                        modifier = Modifier.weight(1f)
                    )

                    FleetInputField(
                        value = state.mileage,
                        onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateMileage(it)) },
                        fieldType = FieldType.DECIMAL,
                        label = stringResource(Res.string.vehicle_edit_mileage),
                        placeholder = stringResource(Res.string.vehicle_edit_mileage_placeholder),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Vehicle Type + Fuel Type — config-driven dual dropdowns (mirrors the Register
                // screen). Fuel options are filtered to the selected vehicle type.
                val isHindi = Locale.current.language == "hi"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    FleetDropdown(
                        label = stringResource(Res.string.vehicle_edit_vehicle_type),
                        options = VehicleType.entries.map { type ->
                            DropdownOption(id = type, label = state.vehicleTypeLabelFor(type, isHindi))
                        },
                        selectedOptionId = state.vehicleType,
                        onOptionSelected = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateVehicleType(it)) },
                        modifier = Modifier.weight(1f)
                    )

                    FleetDropdown(
                        label = stringResource(Res.string.vehicle_edit_fuel_type),
                        options = state.fuelTypeOptions.map { fuel ->
                            DropdownOption(id = fuel, label = state.fuelLabelFor(fuel, isHindi))
                        },
                        selectedOptionId = state.fuelType.takeIf { it.isNotBlank() },
                        onOptionSelected = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateFuelType(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Color
                FleetInputField(
                    value = state.color,
                    onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateColor(it)) },
                    fieldType = FieldType.DEFAULT,
                    label = stringResource(Res.string.vehicle_edit_color),
                    placeholder = stringResource(Res.string.vehicle_edit_color_placeholder),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ==================== Driver Assignment Section ====================
        FleetTitledSectionCard(
            title = stringResource(Res.string.vehicle_edit_assigned_driver),
            iconRes = Res.drawable.ic_profile,
            accent = MaterialTheme.colorScheme.primary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
                // Driver Dropdown
                Column {
                    Surface(
                        onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ToggleDriverDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = FleetTokens.Height.MinTouchTarget),
                        shape = RoundedCornerShape(FleetTokens.Radius.L),
                        border = BorderStroke(FleetTokens.Height.Divider, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(FleetTokens.Spacing.L),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (state.isLoadingDrivers) {
                                CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.M), strokeWidth = FleetTokens.Height.ProgressStroke)
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
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
                            modifier = Modifier.fillMaxWidth().padding(top = FleetTokens.Spacing.XS),
                            shape = RoundedCornerShape(FleetTokens.Radius.L),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            elevation = CardDefaults.cardElevation(defaultElevation = FleetTokens.Elevation.Dropdown)
                        ) {
                            Column(modifier = Modifier.heightIn(max = FleetTokens.Width.DropdownMaxHeight).verticalScroll(rememberScrollState())) {
                                // Option to remove driver
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.sendIntent(VehicleDetailContract.Intent.SelectDriver(null))
                                    },
                                    color = Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(FleetTokens.Spacing.M),
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
                                            modifier = Modifier.padding(FleetTokens.Spacing.M),
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
                                                Icon(
                                                    painter = painterResource(Res.drawable.ic_check),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(FleetTokens.IconSize.S),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
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

