package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.indusjs.uicomponents.components.CaretakerInfoCard
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.entity.VehicleType
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource

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
                Text("Delete Vehicle")
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
internal fun CurrentLocationSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = "Current Location",
        icon = "📍"
    ) {
        val location = vehicle.lastLocation
        if (location != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lat: ${location.latitude}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lng: ${location.longitude}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Last updated: Recently",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = { /* TODO: Open in maps */ }) {
                    Text("📍 View Map")
                }
            }
        } else {
            Text(
                text = "Location not available",
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
        ?: "Driver #${vehicle.assignedDriverId}"

    val driverMobile = vehicle.assignedDriver?.mobile

    EnhancedSectionCard(
        title = "Assigned Driver",
        icon = "👤"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", style = MaterialTheme.typography.titleMedium)
                }
            }
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
                        icon = "📱"
                    )
                }
            }
            FilledTonalButton(
                onClick = { /* TODO: View driver */ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("View", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
internal fun StatusChip(status: VehicleStatus) {
    val colorScheme = VehicleStatus.getColorScheme(status)
    val baseColor = com.indusjs.uicomponents.components.stateColorSchemeToColor(colorScheme)
    val containerColor = baseColor.copy(alpha = 0.15f)
    val contentColor = baseColor
    val icon = VehicleStatus.getIcon(status)
    val label = VehicleStatus.getDisplayLabel(status)
    val text = "$icon $label"

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
        title = "Vehicle Information",
        icon = "🚗"
    ) {
        EnhancedInfoRow(icon = "🔢", label = "Registration", value = vehicle.registrationNumber)
        EnhancedInfoRow(icon = "🏭", label = "Make", value = vehicle.make)
        EnhancedInfoRow(icon = "📦", label = "Model", value = vehicle.model)
        EnhancedInfoRow(icon = "📅", label = "Year", value = vehicle.year.toString())
        EnhancedInfoRow(icon = "🚙", label = "Type", value = getVehicleTypeLabel(vehicle.type), isLast = true)
    }
}

@Composable
internal fun SpecificationsSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = "Specifications",
        icon = "⚙️"
    ) {
        EnhancedInfoRow(icon = "⛽", label = "Fuel Type", value = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() })
        EnhancedInfoRow(icon = "🎨", label = "Color", value = vehicle.color.replaceFirstChar { it.uppercaseChar() })
        EnhancedInfoRow(icon = "👥", label = "Capacity", value = "${vehicle.capacity} seats")
        EnhancedInfoRow(icon = "📏", label = "Mileage", value = "${vehicle.mileage.toInt()} km")
        EnhancedInfoRow(icon = "🔋", label = "Fuel Level", value = "${vehicle.fuelLevel}%", isLast = true)
    }
}

@Composable
internal fun StatusSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = "Additional Info",
        icon = "ℹ️"
    ) {
        EnhancedInfoRow(icon = "🆔", label = "Vehicle ID", value = "#${vehicle.id}")
        EnhancedInfoRow(icon = "📊", label = "Status", value = getStatusLabel(vehicle.status))

        // Show assigned driver info
        val driverName = vehicle.assignedDriver?.fullName() ?: vehicle.assignedDriverName
        if (!driverName.isNullOrBlank() && driverName != "N/A") {
            EnhancedInfoRow(icon = "👤", label = "Assigned Driver", value = driverName)
        }

        vehicle.lastServiceDate?.let {
            EnhancedInfoRow(icon = "🔧", label = "Last Service", value = formatDate(it))
        }
        vehicle.nextServiceDate?.let {
            EnhancedInfoRow(icon = "📆", label = "Next Service", value = formatDate(it), isLast = true)
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
        title = "Current Trip",
        icon = "🚀"
    ) {
        EnhancedInfoRow(icon = "🎫", label = "Trip ID", value = "#${tripAssignment.tripId}")
        EnhancedInfoRow(icon = "📍", label = "Status", value = tripAssignment.tripState.replaceFirstChar { it.uppercaseChar() })
        tripAssignment.startLocation?.let {
            EnhancedInfoRow(icon = "🏁", label = "From", value = it)
        }
        tripAssignment.endLocation?.let {
            EnhancedInfoRow(icon = "🎯", label = "To", value = it)
        }
        tripAssignment.customerName?.let {
            EnhancedInfoRow(icon = "👤", label = "Customer", value = it, isLast = true)
        }
    }
}

@Composable
internal fun EnhancedSectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
internal fun EnhancedInfoRow(
    icon: String,
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
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
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

