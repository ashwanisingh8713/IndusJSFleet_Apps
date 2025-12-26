package com.indusjs.fleet.presentation.vehicles.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Vehicle Detail Screen composable with Edit functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    viewModel: VehicleDetailViewModel,
    vehicleId: String,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load vehicle on first composition
    LaunchedEffect(vehicleId) {
        viewModel.sendIntent(VehicleDetailContract.Intent.LoadVehicle(vehicleId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VehicleDetailContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VehicleDetailContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is VehicleDetailContract.Effect.NavigateBack -> onNavigateBack()
                is VehicleDetailContract.Effect.ShowDeleteConfirmation -> {
                    showDeleteDialog = true
                }
                is VehicleDetailContract.Effect.VehicleDeleted -> {
                    // Already navigating back
                }
                is VehicleDetailContract.Effect.VehicleUpdated -> {
                    // Refresh handled in ViewModel
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Vehicle") },
            text = { Text("Are you sure you want to delete ${state.vehicle?.registrationNumber}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.sendIntent(VehicleDetailContract.Intent.ConfirmDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Vehicle" else "Vehicle Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isEditMode) {
                                viewModel.sendIntent(VehicleDetailContract.Intent.ExitEditMode)
                            } else {
                                viewModel.sendIntent(VehicleDetailContract.Intent.NavigateBack)
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (state.isEditMode) Res.drawable.ic_close else Res.drawable.ic_arrow_back),
                            contentDescription = if (state.isEditMode) "Cancel" else "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (!state.isEditMode && state.vehicle != null) {
                        IconButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.EnterEditMode) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
            if (state.isEditMode) {
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
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ExitEditMode) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.SaveChanges) },
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
                            Text(if (state.isSaving) "Saving..." else "Save Changes")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(message = "Loading vehicle details...")
            }
            state.error != null && state.vehicle == null -> {
                ErrorContent(
                    error = state.error!!,
                    onRetry = { viewModel.sendIntent(VehicleDetailContract.Intent.Refresh) }
                )
            }
            state.vehicle != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isEditMode) {
                        // Edit Mode
                        item { EditModeContent(state, viewModel) }
                    } else {
                        // View Mode
                        item { VehicleHeader(vehicle = state.vehicle!!) }

                        item { VehicleInfoSection(vehicle = state.vehicle!!) }

                        item { SpecificationsSection(vehicle = state.vehicle!!) }

                        item { StatusSection(vehicle = state.vehicle!!) }

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
                                Text("🗑️ Delete Vehicle")
                            }
                        }
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
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
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Saving...")
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleHeader(vehicle: Vehicle) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Vehicle Icon
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    VehicleTypeIcon(
                        type = vehicle.type,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Number - Large and Bold
            Text(
                text = vehicle.registrationNumber,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Make & Model
            Text(
                text = "${vehicle.make} ${vehicle.model} • ${vehicle.year}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge - Centered and Prominent
            StatusChip(status = vehicle.status)

            // Quick Stats Row
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStatItem(
                    icon = "⛽",
                    value = "${vehicle.fuelLevel}%",
                    label = "Fuel"
                )
                QuickStatItem(
                    icon = "📏",
                    value = "${vehicle.mileage.toInt()}",
                    label = "KM"
                )
                QuickStatItem(
                    icon = "👥",
                    value = "${vehicle.capacity}",
                    label = "Seats"
                )
                QuickStatItem(
                    icon = if (vehicle.isOccupied) "🔴" else "🟢",
                    value = if (vehicle.isOccupied) "Busy" else "Free",
                    label = "Status"
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: VehicleStatus) {
    val (containerColor, contentColor, text) = when (status) {
        VehicleStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            "✓ Active"
        )
        VehicleStatus.INACTIVE -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary,
            "○ Inactive"
        )
        VehicleStatus.IN_MAINTENANCE -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary,
            "🔧 Maintenance"
        )
        VehicleStatus.OUT_OF_SERVICE -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            "✗ Out of Service"
        )
    }

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
private fun QuickStatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VehicleInfoSection(vehicle: Vehicle) {
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
private fun SpecificationsSection(vehicle: Vehicle) {
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
private fun StatusSection(vehicle: Vehicle) {
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
    if (vehicle.isOccupied && vehicle.tripAssignment != null) {
        Spacer(modifier = Modifier.height(12.dp))
        TripAssignmentCard(tripAssignment = vehicle.tripAssignment)
    }
}

@Composable
private fun TripAssignmentCard(tripAssignment: com.indusjs.fleet.domain.entity.vehicle.VehicleTripAssignment) {
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
private fun EnhancedSectionCard(
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
private fun EnhancedInfoRow(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditModeContent(
    state: VehicleDetailContract.State,
    viewModel: VehicleDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Registration (read-only)
        Text(
            text = "Vehicle Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = state.registrationNumber,
            onValueChange = { },
            label = { Text("Registration Number") },
            leadingIcon = { Text("🚗", modifier = Modifier.padding(start = 12.dp)) },
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.make,
                onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateMake(it)) },
                label = { Text("Make *") },
                placeholder = { Text("e.g., Toyota") },
                isError = state.makeError != null,
                supportingText = state.makeError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.model,
                onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateModel(it)) },
                label = { Text("Model *") },
                placeholder = { Text("e.g., Fortuner") },
                isError = state.modelError != null,
                supportingText = state.modelError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = state.year,
            onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateYear(it)) },
            label = { Text("Year *") },
            placeholder = { Text("e.g., 2024") },
            leadingIcon = { Text("📅", modifier = Modifier.padding(start = 12.dp)) },
            isError = state.yearError != null,
            supportingText = state.yearError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Vehicle Type selector
        Column {
            Text(
                text = "Vehicle Type",
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

        HorizontalDivider()

        // Specifications
        Text(
            text = "Specifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Fuel Type selector
        Column {
            Text(
                text = "Fuel Type",
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
                label = { Text("Color") },
                placeholder = { Text("e.g., Silver") },
                leadingIcon = { Text("🎨", modifier = Modifier.padding(start = 12.dp)) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.capacity,
                onValueChange = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateCapacity(it)) },
                label = { Text("Capacity") },
                placeholder = { Text("e.g., 7") },
                leadingIcon = { Text("👥", modifier = Modifier.padding(start = 12.dp)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


/**
 * Composable to display vehicle type icon.
 */
@Composable
private fun VehicleTypeIcon(
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

private fun getVehicleTypeLabel(type: VehicleType): String = when (type) {
    VehicleType.TRUCK -> "Truck"
    VehicleType.VAN -> "Van"
    VehicleType.CAR -> "Car"
    VehicleType.BUS -> "Bus"
    VehicleType.MOTORCYCLE -> "Motorcycle"
    VehicleType.TRAILER -> "Trailer"
}

private fun getStatusLabel(status: VehicleStatus): String = when (status) {
    VehicleStatus.ACTIVE -> "Active"
    VehicleStatus.INACTIVE -> "Inactive"
    VehicleStatus.IN_MAINTENANCE -> "In Maintenance"
    VehicleStatus.OUT_OF_SERVICE -> "Out of Service"
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return "N/A"
    return try {
        val days = timestamp / (24 * 60 * 60 * 1000)
        val years = (days / 365.25).toInt() + 1970
        val remainingDays = (days % 365.25).toInt()
        val months = (remainingDays / 30) + 1
        val dayOfMonth = (remainingDays % 30) + 1
        val monthStr = months.coerceIn(1, 12).toString().padStart(2, '0')
        val dayStr = dayOfMonth.coerceIn(1, 28).toString().padStart(2, '0')
        "$dayStr/$monthStr/$years"
    } catch (_: Exception) {
        "N/A"
    }
}

