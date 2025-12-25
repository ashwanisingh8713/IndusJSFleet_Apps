package com.indusjs.fleet.presentation.trips.detail

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Trip Detail Screen composable with Edit functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    tripId: String,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // Load trip on first composition
    LaunchedEffect(tripId) {
        viewModel.sendIntent(TripDetailContract.Intent.LoadTrip(tripId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TripDetailContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TripDetailContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TripDetailContract.Effect.NavigateBack -> onNavigateBack()
                is TripDetailContract.Effect.ShowCancelConfirmation -> {
                    showCancelDialog = true
                }
                is TripDetailContract.Effect.TripCancelled -> {
                    // Already navigating back
                }
                is TripDetailContract.Effect.TripUpdated -> {
                    // Refresh handled in ViewModel
                }
            }
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Trip") },
            text = { Text("Are you sure you want to cancel this trip? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        viewModel.sendIntent(TripDetailContract.Intent.ConfirmCancel)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Trip")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep")
                }
            }
        )
    }

    // Status change dialog
    if (showStatusDialog) {
        StatusChangeDialog(
            currentStatus = state.trip?.status ?: TripStatus.PLANNED,
            onStatusSelected = { status ->
                showStatusDialog = false
                viewModel.sendIntent(TripDetailContract.Intent.UpdateStatus(status))
            },
            onDismiss = { showStatusDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Trip" else "Trip Details") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isEditMode) {
                                viewModel.sendIntent(TripDetailContract.Intent.ExitEditMode)
                            } else {
                                viewModel.sendIntent(TripDetailContract.Intent.NavigateBack)
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
                    if (!state.isEditMode && state.trip != null && state.trip?.status == TripStatus.PLANNED) {
                        IconButton(onClick = { viewModel.sendIntent(TripDetailContract.Intent.EnterEditMode) }) {
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
                            onClick = { viewModel.sendIntent(TripDetailContract.Intent.ExitEditMode) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { viewModel.sendIntent(TripDetailContract.Intent.SaveChanges) },
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
                LoadingContent(message = "Loading trip details...")
            }
            state.error != null && state.trip == null -> {
                ErrorContent(
                    error = state.error!!,
                    onRetry = { viewModel.sendIntent(TripDetailContract.Intent.Refresh) }
                )
            }
            state.trip != null -> {
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
                        item {
                            TripHeader(
                                trip = state.trip!!,
                                onStatusClick = { showStatusDialog = true }
                            )
                        }

                        item { RouteSection(trip = state.trip!!) }

                        item { ScheduleSection(trip = state.trip!!) }

                        item { CargoSection(trip = state.trip!!) }

                        item { AdditionalInfoSection(trip = state.trip!!) }

                        // Cancel button for planned trips
                        if (state.trip?.status == TripStatus.PLANNED) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { viewModel.sendIntent(TripDetailContract.Intent.CancelTrip) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("❌ Cancel Trip")
                                }
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
private fun TripHeader(
    trip: Trip,
    onStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trip icon
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🚚",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.tripNumber ?: "Trip #${trip.id}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${trip.vehicleNumber ?: "Vehicle"} • ${trip.driverName ?: "Driver"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status badge (clickable for planned trips)
            Surface(
                onClick = onStatusClick,
                shape = RoundedCornerShape(16.dp),
                color = getStatusColor(trip.status).copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getStatusDisplayName(trip.status),
                        style = MaterialTheme.typography.labelMedium,
                        color = getStatusColor(trip.status)
                    )
                    if (trip.status == TripStatus.PLANNED) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("▼", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun RouteSection(trip: Trip) {
    SectionCard(title = "📍 Route") {
        // Start Location
        Row(verticalAlignment = Alignment.Top) {
            Text("🟢", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.startLocation?.address ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // End Location
        Row(verticalAlignment = Alignment.Top) {
            Text("🔴", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.endLocation?.address ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (trip.distance > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(label = "Distance", value = "${trip.distance.toInt()} km")
        }
    }
}

@Composable
private fun ScheduleSection(trip: Trip) {
    SectionCard(title = "📅 Schedule") {
        trip.scheduledStartTime?.let { InfoRow(label = "Planned Start", value = it) }
        trip.actualStartTime?.let { InfoRow(label = "Actual Start", value = it) }
        trip.actualEndTime?.let { InfoRow(label = "End Time", value = it) }
        if (trip.estimatedDuration > 0) {
            InfoRow(label = "Est. Duration", value = formatDuration(trip.estimatedDuration))
        }
    }
}

@Composable
private fun CargoSection(trip: Trip) {
    val hasCargo = trip.cargoType != null || trip.cargoDescription != null || trip.customerName != null

    if (hasCargo) {
        SectionCard(title = "📦 Cargo & Customer") {
            trip.cargoType?.let { InfoRow(label = "Cargo Type", value = it.replaceFirstChar { c -> c.uppercaseChar() }) }
            trip.cargoDescription?.let { InfoRow(label = "Description", value = it) }
            trip.customerName?.let { InfoRow(label = "Customer", value = it) }
            trip.priority?.let { InfoRow(label = "Priority", value = it.replaceFirstChar { c -> c.uppercaseChar() }) }
        }
    }
}

@Composable
private fun AdditionalInfoSection(trip: Trip) {
    SectionCard(title = "ℹ️ Additional Info") {
        InfoRow(label = "Trip ID", value = "#${trip.id}")
        trip.notes?.let { InfoRow(label = "Notes", value = it) }
        trip.createdAt?.let { InfoRow(label = "Created on", value = it) }
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditModeContent(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Route Section
        Text(
            text = "Route",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = state.startLocationAddress,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateStartLocation(it)) },
            label = { Text("Start Location *") },
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
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateStartLat(it)) },
                label = { Text("Latitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.startLng,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateStartLng(it)) },
                label = { Text("Longitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = state.endLocationAddress,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEndLocation(it)) },
            label = { Text("End Location *") },
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
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEndLat(it)) },
                label = { Text("Latitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.endLng,
                onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEndLng(it)) },
                label = { Text("Longitude") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = state.distance,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateDistance(it)) },
            label = { Text("Distance (km)") },
            leadingIcon = { Text("🛣️", modifier = Modifier.padding(start = 12.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        HorizontalDivider()

        // Cargo & Customer Section
        Text(
            text = "Cargo & Customer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Cargo Type selector
        Column {
            Text(
                text = "Cargo Type",
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
                        onClick = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoType(cargo)) },
                        label = { Text(cargo.replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.cargoDescription,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoDescription(it)) },
            label = { Text("Cargo Description") },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.customerName,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCustomerName(it)) },
            label = { Text("Customer Name") },
            leadingIcon = { Text("👤", modifier = Modifier.padding(start = 12.dp)) },
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
                        onClick = { viewModel.sendIntent(TripDetailContract.Intent.UpdatePriority(priority)) },
                        label = { Text(priority.replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }
        }

        HorizontalDivider()

        // Notes Section
        Text(
            text = "Notes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = state.notes,
            onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateNotes(it)) },
            label = { Text("Notes") },
            leadingIcon = { Text("📝", modifier = Modifier.padding(start = 12.dp)) },
            singleLine = false,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatusChangeDialog(
    currentStatus: TripStatus,
    onStatusSelected: (TripStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Status") },
        text = {
            Column {
                TripStatus.entries.forEach { status ->
                    Surface(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (status == currentStatus)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getStatusDisplayName(status),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (status == currentStatus) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    if (status != TripStatus.entries.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper functions
@Composable
private fun getStatusColor(status: TripStatus) = when (status) {
    TripStatus.PLANNED -> MaterialTheme.colorScheme.primary
    TripStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
    TripStatus.COMPLETED -> MaterialTheme.colorScheme.secondary
    TripStatus.CANCELLED -> MaterialTheme.colorScheme.error
}

private fun getStatusDisplayName(status: TripStatus): String = when (status) {
    TripStatus.PLANNED -> "Planned"
    TripStatus.IN_PROGRESS -> "In Progress"
    TripStatus.COMPLETED -> "Completed"
    TripStatus.CANCELLED -> "Cancelled"
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

