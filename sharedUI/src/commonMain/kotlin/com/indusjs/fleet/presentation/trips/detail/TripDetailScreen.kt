package com.indusjs.fleet.presentation.trips.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.pdf.PdfExportHandler
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetDateFieldCompact
import com.indusjs.fleet.core.ui.FleetMobileField
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.FleetTimeFieldCompact
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatCostAmount
import com.indusjs.fleet.core.util.formatCostTime
import com.indusjs.fleet.core.util.formatDateTimeForDisplay
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.fleet.data.model.costs.TripCostDto
import com.indusjs.fleet.data.model.costs.TripCostTypes
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<TripDetailContract.TripCostsPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

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
                is TripDetailContract.Effect.ExportPdf -> {
                    // Trigger PDF export by setting the data and showing loader
                    isExportingPdf = true
                    pdfExportData = effect.pdfData
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

    // PDF Export Handler
    PdfExportHandler(
        pdfData = pdfExportData,
        onExportComplete = {
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar("PDF exported successfully!")
            }
        },
        onExportError = { error: String ->
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    )

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
                    screenContext = FleetErrorContext.TRIP_DETAIL,
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
                                    isLoading = state.isLoadingCosts,
                                    onExportPdf = { viewModel.sendIntent(TripDetailContract.Intent.ExportCostsToPdf) }
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

        // PDF Export overlay
        if (isExportingPdf) {
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
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "📄 Generating PDF...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Please wait while we prepare your report",
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
        // Departure Section
        val hasDeparture = trip.plannedStart != null || trip.scheduledDate != null || trip.startTime != null
        if (hasDeparture) {
            Text(
                text = "🚀 Departure",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Show formatted departure date/time
            val departureDateTime = formatScheduleDateTime(
                isoDateTime = trip.plannedStart,
                date = trip.scheduledDate,
                time = trip.startTime
            )
            if (departureDateTime.isNotBlank()) {
                EnhancedInfoRow(icon = "📅", label = "Date & Time", value = departureDateTime)
            }
        }

        // Arrival Section
        val hasArrival = trip.plannedEnd != null || trip.deliveryDate != null || trip.deliveryTime != null
        if (hasArrival) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "🏁 Expected Arrival",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Show formatted arrival date/time
            val arrivalDateTime = formatScheduleDateTime(
                isoDateTime = trip.plannedEnd,
                date = trip.deliveryDate,
                time = trip.deliveryTime
            )
            if (arrivalDateTime.isNotBlank()) {
                EnhancedInfoRow(icon = "📅", label = "Date & Time", value = arrivalDateTime)
            }
        }

        // Actual times (for in_progress/completed trips)
        val hasActualTimes = trip.actualStartTime != null || trip.actualEndTime != null
        if (hasActualTimes) {
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "⏱️ Actual Times",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            trip.actualStartTime?.let {
                EnhancedInfoRow(icon = "▶️", label = "Started", value = formatIsoDateTime(it))
            }
            trip.actualEndTime?.let {
                EnhancedInfoRow(icon = "⏹️", label = "Ended", value = formatIsoDateTime(it))
            }
        }

        // Duration
        if (trip.displayInfo.durationValue != "NA") {
            Spacer(modifier = Modifier.height(8.dp))
            EnhancedInfoRow(
                icon = "⏱️",
                label = trip.displayInfo.durationLabel,
                value = trip.displayInfo.durationValue,
                isLast = true
            )
        }
    }
}

/**
 * Formats schedule date/time from ISO or separate date/time fields.
 */
private fun formatScheduleDateTime(
    isoDateTime: String?,
    date: String?,
    time: String?
): String {
    // Try to use ISO format first
    if (!isoDateTime.isNullOrBlank()) {
        return formatIsoDateTime(isoDateTime)
    }

    // Fallback to separate date/time fields
    val parts = mutableListOf<String>()
    if (!date.isNullOrBlank()) {
        parts.add(date)
    }
    if (!time.isNullOrBlank()) {
        // Format time if it's in HHMM format
        val formattedTime = if (time.length == 4 && time.all { it.isDigit() }) {
            "${time.substring(0, 2)}:${time.substring(2, 4)}"
        } else {
            time
        }
        parts.add(formattedTime)
    }

    return parts.joinToString(" at ")
}

/**
 * Formats ISO 8601 datetime string to human-readable format.
 */
private fun formatIsoDateTime(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return ""

    return try {
        // Parse ISO 8601: 2026-01-04T11:11:00Z
        val parts = isoDateTime.replace("Z", "").split("T")
        if (parts.size == 2) {
            val datePart = parts[0] // 2026-01-04
            val timePart = parts[1].substring(0, 5) // 11:11

            // Convert date to DD-MM-YYYY
            val dateComponents = datePart.split("-")
            if (dateComponents.size == 3) {
                val formattedDate = "${dateComponents[2]}-${dateComponents[1]}-${dateComponents[0]}"
                "$formattedDate at $timePart"
            } else {
                "$datePart at $timePart"
            }
        } else {
            isoDateTime
        }
    } catch (e: Exception) {
        isoDateTime
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
            EnhancedInfoRow(icon = "📅", label = "Created on", value = com.indusjs.fleet.core.util.formatDateToHumanReadable(it, shortMonth = true), isLast = true)
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
        // Loading indicator for vehicles/drivers
        if (state.isLoadingVehiclesDrivers) {
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

        // Vehicle & Driver Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🚛", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vehicle & Driver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                            state.vehicles.forEach { vehicle ->
                                val isOccupied = vehicle.isOccupied
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = !isOccupied) {
                                        viewModel.sendIntent(TripDetailContract.Intent.SelectVehicle(vehicle))
                                    },
                                    color = if (isOccupied) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = vehicle.registrationNumber,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (isOccupied) {
                                                val occupiedInfo = vehicle.tripAssignment?.let { assignment ->
                                                    "${assignment.plannedStart ?: ""} - ${assignment.plannedEnd ?: ""}"
                                                } ?: "Currently assigned"
                                                Text(
                                                    text = "Occupied: $occupiedInfo",
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                            state.drivers.forEach { driver ->
                                val isOccupied = driver.isOccupied
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable(enabled = !isOccupied) {
                                        viewModel.sendIntent(TripDetailContract.Intent.SelectDriver(driver))
                                    },
                                    color = if (isOccupied) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else Color.Transparent
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
                                            if (isOccupied) {
                                                val occupiedInfo = driver.tripAssignment?.let { assignment ->
                                                    "${assignment.plannedStart ?: ""} - ${assignment.plannedEnd ?: ""}"
                                                } ?: "Currently assigned"
                                                Text(
                                                    text = "Occupied: $occupiedInfo",
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
            }
        }

        // Route Section Card with Google Places Search
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontWeight = FontWeight.SemiBold
                    )
                }

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

                // Start Location Predictions Dropdown
                if (state.showStartLocationDropdown && state.startLocationPredictions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.heightIn(max = 200.dp)) {
                            state.startLocationPredictions.forEach { prediction ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.sendIntent(TripDetailContract.Intent.SelectStartLocationPrediction(prediction))
                                    },
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

                // End Location Predictions Dropdown
                if (state.showEndLocationDropdown && state.endLocationPredictions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.heightIn(max = 200.dp)) {
                            state.endLocationPredictions.forEach { prediction ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        viewModel.sendIntent(TripDetailContract.Intent.SelectEndLocationPrediction(prediction))
                                    },
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

                Spacer(modifier = Modifier.height(16.dp))

                // Estimated Distance & Duration Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Estimated Distance
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

                    // Estimated Duration (read-only, auto-calculated)
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

        // Schedule Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("📅", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Schedule (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Departure Date & Time
                Text("Departure", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FleetDateFieldCompact(
                        rawValue = state.departureDate,
                        onRawValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureDate(it)) },
                        label = "Date",
                        modifier = Modifier.weight(1f)
                    )
                    FleetTimeFieldCompact(
                        rawValue = state.departureTime,
                        onRawValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureTime(it)) },
                        label = "Time (24hr)",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Arrival Date & Time
                Text("Arrival", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FleetDateFieldCompact(
                        rawValue = state.arrivalDate,
                        onRawValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalDate(it)) },
                        label = "Date",
                        modifier = Modifier.weight(1f)
                    )
                    FleetTimeFieldCompact(
                        rawValue = state.arrivalTime,
                        onRawValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalTime(it)) },
                        label = "Time (24hr)",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Cargo & Customer Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cargo Type selector
                Text("Cargo Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
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
                    value = state.cargoWeight,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoWeight(it)) },
                    label = { Text("Cargo Weight (kg)") },
                    leadingIcon = { Text("⚖️", modifier = Modifier.padding(start = 8.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.customerName,
                    onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCustomerName(it)) },
                    label = { Text("Customer Name") },
                    leadingIcon = { Text("👤", modifier = Modifier.padding(start = 8.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FleetMobileField(
                    rawValue = state.customerContact,
                    onRawValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCustomerContact(it)) },
                    label = "Customer Contact",
                    placeholder = "Enter 10-digit mobile",
                    leadingEmoji = "📞"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Priority selector
                Text("Priority", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        fontWeight = FontWeight.SemiBold
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
private fun EditDropdownField(
    label: String,
    selectedText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    error: String?,
    icon: String
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() },
            shape = RoundedCornerShape(12.dp),
            color = if (error != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(if (isExpanded) "▲" else "▼", style = MaterialTheme.typography.labelMedium)
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Change Status", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                TripStatus.entries.forEach { status ->
                    val isSelected = status == currentStatus
                    val statusIcon = when (status) {
                        TripStatus.PLANNED -> "📋"
                        TripStatus.IN_PROGRESS -> "🚀"
                        TripStatus.COMPLETED -> "✅"
                        TripStatus.CANCELLED -> "❌"
                    }
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.clickable { onStatusSelected(status) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(statusIcon, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = TripStatus.toApiString(status).replaceFirstChar { it.uppercaseChar() },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
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

/**
 * Trip Costs Section - Enhanced UI displaying costs grouped by type with PDF export.
 * Only shown when there are costs (hasCosts == true).
 */
@Composable
private fun TripCostsSection(
    costs: List<TripCostDto>,
    totalCost: Double,
    costsByType: Map<String, List<TripCostDto>>,
    isLoading: Boolean,
    onExportPdf: (() -> Unit)? = null
) {
    var expandedTypes by remember { mutableStateOf(setOf<String>()) }

    EnhancedSectionCard(
        title = "Trip Costs",
        icon = "💰"
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        } else {
            // Total Cost Header Card - Prominent display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${formatCostAmount(totalCost)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${costs.size} ${if (costs.size == 1) "transaction" else "transactions"} • ${costsByType.size} ${if (costsByType.size == 1) "category" else "categories"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Export to PDF Button
            if (onExportPdf != null && costs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onExportPdf,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    Text("📄", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export to PDF", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cost Categories Header
            Text(
                text = "Cost Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Cost breakdown by type with progress bars
            costsByType.forEach { (costType, typeCosts) ->
                val isExpanded = expandedTypes.contains(costType)
                val typeTotal = typeCosts.sumOf { it.amount }
                val displayName = getCostTypeDisplayName(costType)
                val typeIcon = getCostTypeIcon(costType)
                val percentage = if (totalCost > 0) (typeTotal / totalCost * 100).toInt() else 0
                val costColor = getCostTypeColor(costType)

                Surface(
                    onClick = {
                        expandedTypes = if (isExpanded) {
                            expandedTypes - costType
                        } else {
                            expandedTypes + costType
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Icon with colored background
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = costColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(text = typeIcon, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${typeCosts.size} ${if (typeCosts.size == 1) "entry" else "entries"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = " • $percentage%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = costColor
                                        )
                                    }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${formatCostAmount(typeTotal)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = if (isExpanded) "▲" else "▼",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Progress bar showing percentage (when collapsed)
                        if (!isExpanded) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .padding(bottom = 12.dp)
                                    .height(6.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(3.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (percentage / 100f).coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .background(
                                            costColor,
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                // Expanded cost details
                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    ) {
                        typeCosts.forEachIndexed { index, cost ->
                            EnhancedCostDetailItem(cost = cost, costColor = costColor)
                            if (index < typeCosts.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Get display name for cost type.
 */
private fun getCostTypeDisplayName(costType: String): String {
    return when (costType.lowercase()) {
        "fuel" -> "Fuel"
        "toll" -> "Toll"
        "driver_allowance" -> "Driver Allowance"
        "parking" -> "Parking"
        "loading_charges" -> "Loading Charges"
        "unloading_charges" -> "Unloading Charges"
        "chalan" -> "Chalan"
        "permit" -> "Permit"
        "insurance" -> "Insurance"
        "other" -> "Other"
        else -> costType.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
    }
}

/**
 * Get icon for cost type.
 */
private fun getCostTypeIcon(costType: String): String {
    return when (costType.lowercase()) {
        "fuel" -> "⛽"
        "toll" -> "🛣️"
        "driver_allowance" -> "👤"
        "parking" -> "🅿️"
        "loading_charges" -> "📦"
        "unloading_charges" -> "📤"
        "chalan" -> "📄"
        "permit" -> "🎫"
        "insurance" -> "🛡️"
        else -> "💵"
    }
}

/**
 * Get color for cost type for visual differentiation.
 */
@Composable
private fun getCostTypeColor(costType: String): Color {
    return when (costType.lowercase()) {
        "fuel" -> Color(0xFF4CAF50) // Green
        "toll" -> Color(0xFF2196F3) // Blue
        "driver_allowance" -> Color(0xFFFF9800) // Orange
        "parking" -> Color(0xFF9C27B0) // Purple
        "loading_charges" -> Color(0xFF795548) // Brown
        "unloading_charges" -> Color(0xFF607D8B) // Blue Grey
        "chalan" -> Color(0xFFE91E63) // Pink
        "permit" -> Color(0xFF00BCD4) // Cyan
        "insurance" -> Color(0xFF3F51B5) // Indigo
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Enhanced Cost Detail Item with improved UI.
 * Shows: Title (cost type), Date & Time, Amount - always visible
 * On expand: Shows description/notes only
 *
 * Layout:
 * ┌─────────────────────────────────────────────────┐
 * │ 📅 10-01-2026 • 🕐 2:30 PM  ▼     │ ₹500.00 │
 * └─────────────────────────────────────────────────┘
 * ↓ (tap to expand if has notes)
 * ┌─────────────────────────────────────────────────
 * │ 📝 Description: Toll payment at highway        │
 * │ ⛽ 50 L @ ₹100.00/L (only for fuel)           │
 * └─────────────────────────────────────────────────┘
 */
@Composable
private fun EnhancedCostDetailItem(cost: TripCostDto, costColor: Color) {
    var isExpanded by remember { mutableStateOf(false) }
    val hasFuelDetails = cost.costType == "fuel" && cost.fuelQuantity != null
    val hasNotes = !cost.notes.isNullOrBlank()
    val hasExpandableContent = hasNotes || hasFuelDetails

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hasExpandableContent) {
                    Modifier.clickable { isExpanded = !isExpanded }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            // Main row - Date & Time on left, Amount on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - colored indicator + Date & Time inline
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Small colored indicator bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(32.dp)
                            .background(costColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))

                    // Date & Time inline with separator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Date - Human readable
                        Text(text = "📅", style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDateToHumanReadable(cost.date, shortMonth = true),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Time (if available)
                        cost.time?.takeIf { it.isNotBlank() }?.let { time ->
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(text = "🕐", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = formatCostTime(time),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Expand indicator (only if has expandable content)
                    if (hasExpandableContent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Amount Badge - always visible
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = costColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "₹${formatCostAmount(cost.amount)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = costColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Expanded Details Section - Description only
            if (isExpanded && hasExpandableContent) {
                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 0.5.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes/Description - compact inline display
                cost.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Fuel details (only for fuel type)
                if (hasFuelDetails) {
                    if (hasNotes) Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⛽",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Fuel Filled",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${cost.fuelQuantity} Liters",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                            cost.fuelRate?.let { rate ->
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Rate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "₹${formatCostAmount(rate)}/L",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    // Fuel efficiency if available
                    cost.kmPerLiter?.let { efficiency ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                            ) {
                                val efficiencyFormatted = ((efficiency * 10).toLong() / 10.0).toString()
                                Text(
                                    text = "🚗 Efficiency: $efficiencyFormatted km/L",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Keep old CostDetailItem for backward compatibility but mark as deprecated
@Composable
private fun CostDetailItem(cost: TripCostDto) {
    EnhancedCostDetailItem(cost = cost, costColor = MaterialTheme.colorScheme.primary)
}
