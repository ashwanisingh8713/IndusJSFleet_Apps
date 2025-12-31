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
import com.indusjs.fleet.core.error.ErrorHandler
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.data.model.costs.TripCostTypes
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
                            tint = MaterialTheme.colorScheme.primary,
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
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendIntent(TripDetailContract.Intent.ExitEditMode) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = { viewModel.sendIntent(TripDetailContract.Intent.SaveChanges) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            enabled = state.canSave,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (state.isSaving) "Saving..." else "💾 Save Changes",
                                fontWeight = FontWeight.SemiBold
                            )
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
                    screenContext = ErrorHandler.ScreenContext.TRIP_DETAIL,
                    onRetry = { viewModel.sendIntent(TripDetailContract.Intent.Refresh) }
                )
            }
            state.trip != null -> {
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

                        // Trip Costs Section - only show if there are costs
                        if (state.hasCosts) {
                            item {
                                TripCostsSection(
                                    costs = state.costs,
                                    totalCost = state.totalCost,
                                    costsByType = state.costsByType,
                                    isLoading = state.isLoadingCosts
                                )
                            }
                        }

                        // Cancel button for planned trips
                        if (state.trip?.status == TripStatus.PLANNED) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.sendIntent(TripDetailContract.Intent.CancelTrip) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("❌", style = MaterialTheme.typography.bodyLarge)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cancel Trip",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Saving Changes...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please wait",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
            // Large Trip Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_truck),
                        contentDescription = "Trip",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trip Number
            Text(
                text = trip.tripNumber ?: "Trip #${trip.id}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Vehicle & Driver
            Text(
                text = "${trip.vehicleNumber ?: "Vehicle"} • ${trip.driverName ?: "Driver"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status Badge - Clickable for planned trips
            EnhancedStatusBadge(
                status = trip.status,
                onClick = onStatusClick
            )

            // Quick Stats Row
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripQuickStat(
                    value = trip.displayInfo.distanceValue,
                    label = trip.displayInfo.distanceLabel
                )
                TripQuickStat(
                    value = trip.displayInfo.durationValue,
                    label = trip.displayInfo.durationLabel
                )
                TripQuickStat(
                    icon = getPriorityIcon(trip.priority),
                    value = trip.priority?.replaceFirstChar { it.uppercaseChar() } ?: "Normal",
                    label = "Priority"
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatusBadge(
    status: TripStatus,
    onClick: () -> Unit
) {
    val (containerColor, contentColor, icon, text) = when (status) {
        TripStatus.PLANNED -> listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            "📋",
            "Planned"
        )
        TripStatus.IN_PROGRESS -> listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary,
            "🚀",
            "In Progress"
        )
        TripStatus.COMPLETED -> listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary,
            "✅",
            "Completed"
        )
        TripStatus.CANCELLED -> listOf(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            "❌",
            "Cancelled"
        )
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor as androidx.compose.ui.graphics.Color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon as String, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text as String,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor as androidx.compose.ui.graphics.Color
            )
            if (status == TripStatus.PLANNED) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
private fun TripQuickStat(
    icon: String? = null,
    value: String,
    label: String
) {
    val isNA = value == "NA" || value == "N/A"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isNA) FontWeight.Normal else FontWeight.Bold,
            color = if (isNA) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                   else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getPriorityIcon(priority: String?): String {
    return when (priority?.lowercase()) {
        "high" -> "🔴"
        "medium" -> "🟡"
        "low" -> "🟢"
        else -> "⚪"
    }
}

@Composable
private fun RouteSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Route",
        icon = "📍"
    ) {
        // Route Timeline
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Timeline indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                // Start point
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🟢", style = MaterialTheme.typography.labelSmall)
                    }
                }
                // Connecting line
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(1.dp)
                        )
                )
                // End point
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🔴", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Location details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Start Location
                Column {
                    Text(
                        text = "FROM",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trip.startLocation?.address ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // End Location
                Column {
                    Text(
                        text = "TO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trip.endLocation?.address ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Distance info if available
        if (trip.displayInfo.distanceValue != "NA") {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trip.displayInfo.distanceValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = " ${trip.displayInfo.distanceLabel.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Schedule",
        icon = "📅"
    ) {
        trip.scheduledStartTime?.let {
            EnhancedInfoRow(icon = "🗓️", label = "Planned Start", value = it)
        }
        trip.actualStartTime?.let {
            EnhancedInfoRow(icon = "▶️", label = "Actual Start", value = it)
        }
        trip.actualEndTime?.let {
            EnhancedInfoRow(icon = "⏹️", label = "End Time", value = it)
        }
        if (trip.displayInfo.durationValue != "NA") {
            EnhancedInfoRow(
                icon = "⏱️",
                label = trip.displayInfo.durationLabel,
                value = trip.displayInfo.durationValue,
                isLast = true
            )
        }
    }
}

@Composable
private fun CargoSection(trip: Trip) {
    val hasCargo = trip.cargoType != null || trip.cargoDescription != null || trip.customerName != null

    if (hasCargo) {
        EnhancedSectionCard(
            title = "Cargo & Customer",
            icon = "📦"
        ) {
            trip.cargoType?.let {
                EnhancedInfoRow(
                    icon = "📋",
                    label = "Cargo Type",
                    value = it.replaceFirstChar { c -> c.uppercaseChar() }
                )
            }
            trip.cargoDescription?.let {
                EnhancedInfoRow(icon = "📝", label = "Description", value = it)
            }
            trip.customerName?.let {
                EnhancedInfoRow(icon = "👤", label = "Customer", value = it)
            }
            trip.priority?.let {
                val priorityIcon = getPriorityIcon(it)
                EnhancedInfoRow(
                    icon = priorityIcon,
                    label = "Priority",
                    value = it.replaceFirstChar { c -> c.uppercaseChar() },
                    isLast = true
                )
            }
        }
    }
}

@Composable
private fun AdditionalInfoSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Additional Info",
        icon = "ℹ️"
    ) {
        EnhancedInfoRow(icon = "🆔", label = "Trip ID", value = "#${trip.id}")
        trip.notes?.let {
            EnhancedInfoRow(icon = "📝", label = "Notes", value = it)
        }
        trip.createdAt?.let {
            EnhancedInfoRow(icon = "📅", label = "Created on", value = it, isLast = true)
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            modifier = Modifier.weight(0.4f)
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
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
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 0.5.dp
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditModeContent(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Route Section Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📍", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Route",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.startLocationAddress,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateStartLocation(it)) },
                    label = { Text("Start Location *") },
                    leadingIcon = { Text("🟢", modifier = Modifier.padding(start = 8.dp)) },
                    isError = state.startLocationError != null,
                    supportingText = state.startLocationError?.let { { Text(it) } },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.startLng,
                        onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateStartLng(it)) },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.endLocationAddress,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEndLocation(it)) },
                    label = { Text("End Location *") },
                    leadingIcon = { Text("🔴", modifier = Modifier.padding(start = 8.dp)) },
                    isError = state.endLocationError != null,
                    supportingText = state.endLocationError?.let { { Text(it) } },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = state.endLng,
                        onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateEndLng(it)) },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.distance,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateDistance(it)) },
                    label = { Text("Distance (km)") },
                    leadingIcon = { Text("🛣️", modifier = Modifier.padding(start = 8.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Cargo & Customer Section Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📦", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Cargo & Customer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cargo Type selector
                Text(
                    text = "Cargo Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.cargoDescription,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoDescription(it)) },
                    label = { Text("Cargo Description") },
                    leadingIcon = { Text("📝", modifier = Modifier.padding(start = 8.dp)) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.customerName,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCustomerName(it)) },
                    label = { Text("Customer Name") },
                    leadingIcon = { Text("👤", modifier = Modifier.padding(start = 8.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Priority selector
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
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
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(getPriorityIcon(priority))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(priority.replaceFirstChar { it.uppercaseChar() })
                                }
                            }
                        )
                    }
                }
            }
        }

        // Notes Section Card
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📝", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateNotes(it)) },
                    label = { Text("Additional Notes") },
                    placeholder = { Text("Add any additional notes or instructions...") },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
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
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Change Status",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TripStatus.entries.forEach { status ->
                    val isSelected = status == currentStatus
                    val (bgColor, icon) = when (status) {
                        TripStatus.PLANNED -> Pair(MaterialTheme.colorScheme.primary, "📋")
                        TripStatus.IN_PROGRESS -> Pair(MaterialTheme.colorScheme.tertiary, "🚀")
                        TripStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.secondary, "✅")
                        TripStatus.CANCELLED -> Pair(MaterialTheme.colorScheme.error, "❌")
                    }

                    Surface(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected)
                            bgColor.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = getStatusDisplayName(status),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) bgColor else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = bgColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "✓",
                                            color = bgColor,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.SemiBold
                )
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

/**
 * Trip Costs Section - displays costs grouped by type.
 * Only shown when there are costs (hasCosts == true).
 */
@Composable
private fun TripCostsSection(
    costs: List<TripCostDto>,
    totalCost: Double,
    costsByType: Map<String, List<TripCostDto>>,
    isLoading: Boolean
) {
    var expandedTypes by remember { mutableStateOf(setOf<String>()) }

    EnhancedSectionCard(
        title = "Trip Costs",
        icon = "💰"
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        } else {
            // Total Cost Summary
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Cost",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${costs.size} entries",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = "₹${formatCostAmount(totalCost)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cost breakdown by type
            costsByType.forEach { (costType, typeCosts) ->
                val isExpanded = expandedTypes.contains(costType)
                val typeTotal = typeCosts.sumOf { it.amount }
                val displayName = getCostTypeDisplayName(costType)
                val typeIcon = getCostTypeIcon(costType)

                // Type header - clickable to expand/collapse
                Surface(
                    onClick = {
                        expandedTypes = if (isExpanded) {
                            expandedTypes - costType
                        } else {
                            expandedTypes + costType
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = typeIcon,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${typeCosts.size} ${if (typeCosts.size == 1) "entry" else "entries"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${formatCostAmount(typeTotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Expanded cost details
                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        typeCosts.forEachIndexed { index, cost ->
                            CostDetailItem(cost = cost)
                            if (index < typeCosts.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CostDetailItem(cost: TripCostDto) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Date and time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📅",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = cost.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                cost.time?.let { time ->
                    Text(
                        text = " • $time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notes if available
            cost.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }

            // Fuel details if applicable
            if (cost.costType == "fuel" && cost.fuelQuantity != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "⛽ ${cost.fuelQuantity} L",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    cost.fuelRate?.let { rate ->
                        Text(
                            text = " @ ₹$rate/L",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Amount
        Text(
            text = "₹${formatCostAmount(cost.amount)}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatCostAmount(amount: Double): String {
    val intPart = amount.toLong()
    val decPart = ((amount - intPart) * 100).toInt()
    val decStr = if (decPart < 10) "0$decPart" else "$decPart"

    return if (intPart >= 1000) {
        val formattedInt = intPart.toString().reversed().chunked(3).joinToString(",").reversed()
        "$formattedInt.$decStr"
    } else {
        "$intPart.$decStr"
    }
}

private fun getCostTypeDisplayName(costType: String): String {
    return TripCostTypes.types.find { it.first == costType }?.second
        ?: costType.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
}

private fun getCostTypeIcon(costType: String): String {
    return when (costType) {
        "fuel" -> "⛽"
        "toll" -> "🛣️"
        "driver_allowance" -> "👤"
        "loading" -> "📦"
        "unloading" -> "📤"
        "parking" -> "🅿️"
        "rto" -> "📋"
        "police" -> "👮"
        "repair" -> "🔧"
        "food" -> "🍽️"
        "halt" -> "⏸️"
        "commission" -> "💵"
        "weighing" -> "⚖️"
        "detention" -> "⏰"
        "miscellaneous" -> "📝"
        "other" -> "📌"
        else -> "💰"
    }
}
