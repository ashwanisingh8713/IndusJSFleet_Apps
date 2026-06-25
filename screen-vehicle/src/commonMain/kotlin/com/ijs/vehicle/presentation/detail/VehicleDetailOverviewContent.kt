package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.indusjs.uicomponents.components.CaretakerInfoCard
import com.indusjs.uicomponents.components.FleetAvatar
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.entity.VehicleType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OverviewTabContent(
    vehicle: Vehicle,
    state: VehicleDetailContract.State,
    viewModel: VehicleDetailViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // Vehicle Info Section
        item { VehicleInfoSection(vehicle = vehicle) }

        // Specifications Section
        item { SpecificationsSection(vehicle = vehicle) }

        // Current Location Section
        item { CurrentLocationSection(vehicle = vehicle) }

        // Assigned Driver Section - only show if driver is assigned
        if (vehicle.assignedDriverId != null) {
            item { AssignedDriverSection(vehicle = vehicle) }
        }

        // Caretaker Assignment Section
        item {
            CaretakerInfoCard(
                caretaker = state.selectedCaretaker?.toCaretakerInfo(),
                onChangeCaretaker = if (state.isEditMode) {
                    { viewModel.sendIntent(VehicleDetailContract.Intent.LoadCaretakers) }
                } else null
            )
        }

        // Status Section
        item { StatusSection(vehicle = vehicle) }

        // Delete button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.DeleteVehicle) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.vehicle_overview_delete))
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
internal fun CurrentLocationSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_detail_current_location),
        iconRes = Res.drawable.ic_map
    ) {
        val location = vehicle.lastLocation
        if (location != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.vehicle_overview_location_lat, location.latitude.toString()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.vehicle_overview_location_lng, location.longitude.toString()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.vehicle_overview_updated_recently),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = { /* TODO: Open in maps */ }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_map),
                        contentDescription = null,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                    Text(stringResource(Res.string.vehicle_overview_view_map))
                }
            }
        } else {
            Text(
                text = stringResource(Res.string.vehicle_overview_location_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun AssignedDriverSection(vehicle: Vehicle) {
    // Get driver name from assignedDriver object or assignedDriverName or use ID as fallback
    val driverName = vehicle.assignedDriver?.fullName()?.takeIf { it.isNotBlank() && it != "N/A" }
        ?: vehicle.assignedDriverName?.takeIf { it.isNotBlank() }
        ?: stringResource(Res.string.vehicle_overview_driver_fallback, vehicle.assignedDriverId!!)

    val driverMobile = vehicle.assignedDriver?.mobile

    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_detail_assigned_driver),
        iconRes = Res.drawable.ic_profile
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filled primary-container avatar (40dp, titleMedium) — FleetAvatar defaults match the
            // original fill; the person glyph becomes real driver initials.
            FleetAvatar(
                name = driverName,
                size = 40.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driverName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                // Show driver mobile with call icon if available
                driverMobile?.takeIf { it.isNotBlank() }?.let { mobile ->
                    Spacer(modifier = Modifier.height(4.dp))
                    ClickablePhoneRow(
                        phoneNumber = mobile,
                        label = null,
                        icon = null
                    )
                }
            }
            FilledTonalButton(
                onClick = { /* TODO: View driver */ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(stringResource(Res.string.vehicle_overview_view), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun StatusChip(status: VehicleStatus, stateLabels: Map<String, String> = emptyMap()) {
    val colorScheme = VehicleStatus.getColorScheme(status)
    val baseColor = com.indusjs.uicomponents.components.stateColorSchemeToChipColor(colorScheme)
    val containerColor = baseColor.copy(alpha = 0.15f)
    val contentColor = baseColor
    val apiValue = VehicleStatus.toApiString(status)
    val label = stateLabels[apiValue] ?: VehicleStatus.getDisplayLabel(status)
    val text = label

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
internal fun VehicleInfoSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_detail_info),
        iconRes = Res.drawable.ic_truck
    ) {
        EnhancedInfoRow(iconRes = Res.drawable.ic_vehicle, label = stringResource(Res.string.vehicle_overview_registration_short), value = vehicle.registrationNumber)
        EnhancedInfoRow(iconRes = Res.drawable.ic_settings, label = stringResource(Res.string.vehicle_overview_make), value = vehicle.make)
        EnhancedInfoRow(iconRes = Res.drawable.ic_package, label = stringResource(Res.string.vehicle_overview_model), value = vehicle.model)
        EnhancedInfoRow(iconRes = Res.drawable.ic_calendar, label = stringResource(Res.string.vehicle_overview_year), value = vehicle.year.toString())
        EnhancedInfoRow(iconRes = Res.drawable.ic_truck, label = stringResource(Res.string.vehicles_type), value = getVehicleTypeLabel(vehicle.type), isLast = true)
    }
}

@Composable
internal fun SpecificationsSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_detail_specs),
        iconRes = Res.drawable.ic_settings
    ) {
        EnhancedInfoRow(iconRes = Res.drawable.ic_fuel, label = stringResource(Res.string.vehicle_edit_fuel_type), value = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() })
        EnhancedInfoRow(iconRes = Res.drawable.ic_info, label = stringResource(Res.string.vehicle_edit_color), value = vehicle.color.replaceFirstChar { it.uppercaseChar() })
        EnhancedInfoRow(iconRes = Res.drawable.ic_team, label = stringResource(Res.string.vehicle_edit_capacity), value = stringResource(Res.string.vehicle_overview_capacity_seats, vehicle.capacity))
        EnhancedInfoRow(iconRes = Res.drawable.ic_speed, label = stringResource(Res.string.vehicle_list_label_mileage), value = stringResource(Res.string.vehicle_trips_km, vehicle.mileage.toInt()))
        EnhancedInfoRow(iconRes = Res.drawable.ic_fuel, label = stringResource(Res.string.vehicle_overview_fuel_level), value = "${vehicle.fuelLevel}%", isLast = true)
    }
}

@Composable
internal fun StatusSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_detail_additional),
        iconRes = Res.drawable.ic_info
    ) {
        EnhancedInfoRow(iconRes = Res.drawable.ic_profile, label = stringResource(Res.string.vehicle_overview_vehicle_id), value = "#${vehicle.id}")
        EnhancedInfoRow(iconRes = Res.drawable.ic_dashboard, label = stringResource(Res.string.vehicles_status), value = getStatusLabel(vehicle.status))

        // Show assigned driver info
        val driverName = vehicle.assignedDriver?.fullName() ?: vehicle.assignedDriverName
        if (!driverName.isNullOrBlank() && driverName != "N/A") {
            EnhancedInfoRow(iconRes = Res.drawable.ic_profile, label = stringResource(Res.string.vehicle_detail_assigned_driver), value = driverName)
        }

        vehicle.lastServiceDate?.let {
            EnhancedInfoRow(iconRes = Res.drawable.ic_settings, label = stringResource(Res.string.vehicle_overview_last_service), value = formatDate(it))
        }
        vehicle.nextServiceDate?.let {
            EnhancedInfoRow(iconRes = Res.drawable.ic_calendar, label = stringResource(Res.string.vehicle_overview_next_service), value = formatDate(it), isLast = true)
        } ?: run {
            // If no next service date, mark the previous one as last
        }
    }

    // Trip Assignment Card (if occupied)
    val tripAssign = vehicle.tripAssignment
    if (vehicle.isOccupied && tripAssign != null) {
        Spacer(modifier = Modifier.height(12.dp))
        TripAssignmentCard(tripAssignment = tripAssign)
    }
}

@Composable
internal fun TripAssignmentCard(tripAssignment: com.ijs.vehicle.domain.entity.VehicleTripAssignment) {
    EnhancedSectionCard(
        title = stringResource(Res.string.vehicle_detail_current_trip),
        iconRes = Res.drawable.ic_trip
    ) {
        EnhancedInfoRow(iconRes = Res.drawable.ic_trip, label = stringResource(Res.string.vehicle_overview_trip_id), value = "#${tripAssignment.tripId}")
        EnhancedInfoRow(iconRes = Res.drawable.ic_dashboard, label = stringResource(Res.string.vehicles_status), value = tripAssignment.tripState.replaceFirstChar { it.uppercaseChar() })
        tripAssignment.startLocation?.let {
            EnhancedInfoRow(iconRes = Res.drawable.ic_map, label = stringResource(Res.string.vehicle_overview_from), value = it)
        }
        tripAssignment.endLocation?.let {
            EnhancedInfoRow(iconRes = Res.drawable.ic_map, label = stringResource(Res.string.vehicle_overview_to), value = it)
        }
        tripAssignment.customerName?.let {
            EnhancedInfoRow(iconRes = Res.drawable.ic_profile, label = stringResource(Res.string.vehicle_overview_customer), value = it, isLast = true)
        }
    }
}

@Composable
internal fun EnhancedSectionCard(
    title: String,
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetTitledSectionCard(title = title, iconRes = iconRes, content = content)
}

@Composable
internal fun EnhancedInfoRow(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(FleetTokens.IconSize.S),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

