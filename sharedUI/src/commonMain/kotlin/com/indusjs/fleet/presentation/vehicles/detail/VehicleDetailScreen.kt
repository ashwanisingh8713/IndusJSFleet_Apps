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
                            tint = MaterialTheme.colorScheme.onPrimary,
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
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle icon
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = getVehicleTypeEmoji(vehicle.type),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = vehicle.registrationNumber,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VehicleInfoSection(vehicle: Vehicle) {
    SectionCard(title = "🚗 Vehicle Information") {
        InfoRow(label = "Registration", value = vehicle.registrationNumber)
        InfoRow(label = "Make", value = vehicle.make)
        InfoRow(label = "Model", value = vehicle.model)
        InfoRow(label = "Year", value = vehicle.year.toString())
        InfoRow(label = "Type", value = getVehicleTypeLabel(vehicle.type))
    }
}

@Composable
private fun SpecificationsSection(vehicle: Vehicle) {
    SectionCard(title = "⚙️ Specifications") {
        InfoRow(label = "Fuel Type", value = vehicle.fuelType.replaceFirstChar { it.uppercaseChar() })
        InfoRow(label = "Color", value = vehicle.color.replaceFirstChar { it.uppercaseChar() })
        InfoRow(label = "Capacity", value = "${vehicle.capacity} seats")
        InfoRow(label = "Mileage", value = "${vehicle.mileage} km")
        InfoRow(label = "Fuel Level", value = "${vehicle.fuelLevel}%")
    }
}

@Composable
private fun StatusSection(vehicle: Vehicle) {
    SectionCard(title = "ℹ️ Additional Info") {
        InfoRow(label = "Vehicle ID", value = "#${vehicle.id}")
        InfoRow(label = "Status", value = getStatusLabel(vehicle.status))
        vehicle.assignedDriverName?.let {
            InfoRow(label = "Assigned Driver", value = it)
        }
        vehicle.lastServiceDate?.let {
            InfoRow(label = "Last Service", value = formatDate(it))
        }
        vehicle.nextServiceDate?.let {
            InfoRow(label = "Next Service", value = formatDate(it))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
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

// Helper functions
private fun getVehicleTypeEmoji(type: VehicleType): String = when (type) {
    VehicleType.TRUCK -> "🚛"
    VehicleType.VAN -> "🚐"
    VehicleType.CAR -> "🚗"
    VehicleType.BUS -> "🚌"
    VehicleType.MOTORCYCLE -> "🏍️"
    VehicleType.TRAILER -> "🚚"
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

