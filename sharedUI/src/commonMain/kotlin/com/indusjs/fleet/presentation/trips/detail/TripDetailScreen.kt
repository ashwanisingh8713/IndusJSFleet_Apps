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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.pdfreport.handler.TripCostsPdfHandler
import com.indusjs.pdfreport.model.TripCostsPdfData
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.ui.ClickablePhoneRow
import com.indusjs.fleet.core.ui.customer.CustomerDetailsSection
import com.indusjs.fleet.core.ui.customer.CustomerSelectionBottomSheet
import com.indusjs.fleet.core.ui.state.StateChangeDialog
import com.indusjs.fleet.core.ui.state.getTripStateOptions
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.data.model.costs.TripCostDto
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
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTripCost: (tripId: String, vehicleId: String) -> Unit = { _, _ -> },
    onNavigateToAddPayment: (tripId: String, vehicleId: String) -> Unit = { _, _ -> },
    onNavigateToAddCustomer: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<TripCostsPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Load trip on first composition - only if not already loaded
    LaunchedEffect(tripId) {
        // Only load if trip is not already loaded or tripId changed
        if (state.trip == null || state.tripId != tripId) {
            viewModel.sendIntent(TripDetailContract.Intent.LoadTrip(tripId))
        }
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
                is TripDetailContract.Effect.StateUpdated -> {
                    // State updated - handled by ViewModel
                }
                is TripDetailContract.Effect.NavigateToAddTripCost -> {
                    onNavigateToAddTripCost(effect.tripId, effect.vehicleId)
                }
                is TripDetailContract.Effect.NavigateToAddPayment -> {
                    onNavigateToAddPayment(effect.tripId, effect.vehicleId)
                }
                is TripDetailContract.Effect.NavigateToAddCustomer -> {
                    onNavigateToAddCustomer()
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

    // Status change dialog - use new StateChangeDialog
    if (showStatusDialog && state.trip != null) {
        StateChangeDialog(
            title = "Change Trip Status",
            currentStateLabel = TripStatus.getDisplayLabel(state.trip!!.status),
            stateOptions = getTripStateOptions(state.trip!!.status),
            onStateSelected = { newState ->
                showStatusDialog = false
                viewModel.sendIntent(TripDetailContract.Intent.UpdateTripState(newState))
            },
            onDismiss = { showStatusDialog = false },
            isLoading = state.isUpdatingState
        )
    }


    // PDF Export Handler
    TripCostsPdfHandler(
        pdfData = pdfExportData,
        onExportComplete = {
            isExportingPdf = false
            pdfExportData = null
            // No snackbar needed - dialog already shows success
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
                    // Show edit button if user can edit (full or price-only)
                    if (!state.isEditMode && state.trip != null && state.showEditButton) {
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
                                canViewTripPrice = state.canViewTripPrice,
                                onStatusClick = { showStatusDialog = true }
                            )
                        }

                        item { RouteAndScheduleSection(trip = state.trip!!) }

                        // Show actual times section only when there are actual start/end times
                        if (state.trip!!.actualStartTime != null || state.trip!!.actualEndTime != null) {
                            item { ActualTimesSection(trip = state.trip!!) }
                        }

                        item { CargoSection(trip = state.trip!!, canViewTripPrice = state.canViewTripPrice) }

                        item { AdditionalInfoSection(trip = state.trip!!) }

                        // Trip Costs Section - always show (with empty state if no costs)
                        item {
                            TripCostsSection(
                                costs = state.costs,
                                totalCost = state.totalCost,
                                costsByType = state.costsByType,
                                isLoading = state.isLoadingCosts,
                                onExportPdf = if (state.hasCosts) {
                                    { viewModel.sendIntent(TripDetailContract.Intent.ExportCostsToPdf) }
                                } else null,
                                onAddTripCost = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddTripCost) }
                            )
                        }

                        // Record Payment button - only for Owner and General Manager
                        if (state.canViewTripPrice) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddPayment) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("₹", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Record Payment",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
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

    // Customer Selection Bottom Sheet for Edit Mode
    if (state.showCustomerBottomSheet) {
        CustomerSelectionBottomSheet(
            customers = state.customers,
            searchQuery = state.customerSearchQuery,
            isLoading = state.isLoadingCustomers,
            onDismiss = { viewModel.sendIntent(TripDetailContract.Intent.ToggleCustomerBottomSheet) },
            onSearchQueryChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCustomerSearchQuery(it)) },
            onCustomerSelect = { viewModel.sendIntent(TripDetailContract.Intent.SelectCustomer(it)) },
            onAddNewCustomer = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddCustomer) }
        )
    }
}

@Composable
private fun TripHeader(
    trip: Trip,
    canViewTripPrice: Boolean = false,
    onStatusClick: () -> Unit
) {
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
                .padding(16.dp)
        ) {
            // Trip Title (Full Width - Bold)
            Text(
                text = trip.tripNumber ?: "Trip #${trip.id}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EnhancedStatusBadge(
                    status = trip.status,
                    onClick = onStatusClick
                )
            }

            // Row 2: Trip Price (for Owner/GM) - Highlighted
            if (canViewTripPrice) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trip Price",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (trip.tripPrice != null && trip.tripPrice > 0) {
                            formatCurrency(trip.tripPrice)
                        } else {
                            "Not Set"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.tripPrice != null && trip.tripPrice > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Row 3: Vehicle Number
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vehicle",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.vehicleNumber ?: "Not Assigned",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Row 4: Driver Name
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Driver",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.driverName ?: "Not Assigned",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Divider
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row: Distance | Duration | Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Distance
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trip.displayInfo.distanceValue,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trip.displayInfo.distanceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Duration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trip.displayInfo.durationValue,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = trip.displayInfo.durationLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Priority
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trip.priority?.replaceFirstChar { it.uppercaseChar() } ?: "Normal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedStatusBadge(
    status: TripStatus,
    onClick: () -> Unit
) {
    val colorScheme = TripStatus.getColorScheme(status)
    val baseColor = when (colorScheme) {
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.secondary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> MaterialTheme.colorScheme.tertiary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> MaterialTheme.colorScheme.primary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
    val containerColor = baseColor.copy(alpha = 0.15f)
    val icon = TripStatus.getIcon(status)
    val text = TripStatus.getDisplayLabel(status)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = baseColor
            )
            if (status == TripStatus.PLANNED) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = baseColor
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
private fun RouteAndScheduleSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Route & Schedule",
        icon = "📍"
    ) {
        // Departure Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side - Location
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                // Start point indicator
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Departure",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.startLocation?.address ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }

            // Right side - Date & Time
            val departureDateTime = formatScheduleDateTime(
                isoDateTime = trip.plannedStart,
                date = trip.scheduledDate,
                time = trip.startTime
            )
            if (departureDateTime.isNotBlank()) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "🗓️ $departureDateTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connecting dots
        Row(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Arrival Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left side - Location
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                // End point indicator
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "B",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Arrival",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trip.endLocation?.address ?: "Not specified",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2
                    )
                }
            }

            // Right side - Date & Time
            val arrivalDateTime = formatScheduleDateTime(
                isoDateTime = trip.plannedEnd,
                date = trip.deliveryDate,
                time = trip.deliveryTime
            )
            if (arrivalDateTime.isNotBlank()) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "🗓️ $arrivalDateTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Actual Times Section - Shows actual start/end times for in-progress/completed trips.
 * Only rendered when there are actual times recorded.
 */
@Composable
private fun ActualTimesSection(trip: Trip) {
    EnhancedSectionCard(
        title = "Actual Times",
        icon = "⏱️"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Actual Start Time
            trip.actualStartTime?.let { startTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "▶️",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Started",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatIsoDateTime(startTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Separator if both times exist
            if (trip.actualStartTime != null && trip.actualEndTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Actual End Time
            trip.actualEndTime?.let { endTime ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⏹️",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatIsoDateTime(endTime),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Duration if available
            if (trip.displayInfo.durationValue != "NA" && trip.actualStartTime != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ Actual Duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = trip.displayInfo.durationValue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formats schedule date/time from ISO or separate date/time fields.
 * Output format: DD-MMM-YYYY hh:mm AM/PM (e.g., "05-Feb-2026 02:30 PM")
 */
private fun formatScheduleDateTime(
    isoDateTime: String?,
    date: String?,
    time: String?
): String {
    // Try to use ISO format first
    if (!isoDateTime.isNullOrBlank()) {
        return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDateTime)
    }

    // Fallback to separate date/time fields
    if (!date.isNullOrBlank() && !time.isNullOrBlank()) {
        return FleetDateTime.formatAnyToDisplayDateTime12Hour(date, time)
    }

    if (!date.isNullOrBlank()) {
        return FleetDateTime.formatAnyToDisplayDate(date)
    }

    return ""
}

/**
 * Formats ISO 8601 datetime string to human-readable format.
 * Output format: DD-MMM-YYYY hh:mm AM/PM (e.g., "05-Feb-2026 02:30 PM")
 */
private fun formatIsoDateTime(isoDateTime: String): String {
    if (isoDateTime.isBlank()) return ""
    return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDateTime)
}

@Composable
private fun CargoSection(trip: Trip, canViewTripPrice: Boolean = false) {
    val hasCargo = trip.cargoType != null || trip.cargoDescription != null ||
        trip.cargoLoadingWeight != null || trip.customerName != null ||
        (canViewTripPrice && trip.tripPrice != null)

    if (hasCargo) {
        EnhancedSectionCard(
            title = "Cargo & Customer",
            icon = "📦"
        ) {
            // Cargo Type with Weight in same row if both available
            if (trip.cargoType != null || trip.cargoLoadingWeight != null) {
                val cargoTypeValue = trip.cargoType?.replaceFirstChar { c -> c.uppercaseChar() } ?: ""
                val weightValue = trip.cargoLoadingWeight?.let { weight ->
                    val unit = trip.weightUnit ?: "KG"
                    val formattedWeight = if (weight == weight.toLong().toDouble()) {
                        weight.toLong().toString()
                    } else {
                        // Multiplatform-compatible decimal formatting
                        val rounded = (weight * 100).toLong() / 100.0
                        val parts = rounded.toString().split(".")
                        val intPart = parts[0]
                        val decPart = if (parts.size > 1) parts[1].take(2).padEnd(2, '0') else "00"
                        "$intPart.$decPart"
                    }
                    "$formattedWeight $unit"
                }

                // Display cargo type and weight
                if (cargoTypeValue.isNotBlank() && weightValue != null) {
                    EnhancedInfoRow(
                        icon = "📋",
                        label = "Cargo",
                        value = "$cargoTypeValue • $weightValue"
                    )
                } else if (cargoTypeValue.isNotBlank()) {
                    EnhancedInfoRow(
                        icon = "📋",
                        label = "Cargo Type",
                        value = cargoTypeValue
                    )
                } else if (weightValue != null) {
                    EnhancedInfoRow(
                        icon = "⚖️",
                        label = "Weight",
                        value = weightValue
                    )
                }
            }

            trip.cargoDescription?.let {
                EnhancedInfoRow(icon = "📝", label = "Description", value = it)
            }
            trip.customerName?.let {
                EnhancedInfoRow(icon = "👤", label = "Customer", value = it)
            }
            // Customer Contact with call icon
            trip.customerContact?.let { contact ->
                if (contact.isNotBlank()) {
                    ClickablePhoneRow(
                        phoneNumber = contact,
                        label = "Customer Contact",
                        icon = "📞"
                    )
                }
            }
            // Only show trip_price for Owner and General Manager
            if (canViewTripPrice) {
                trip.tripPrice?.let {
                    EnhancedInfoRow(
                        icon = "💰",
                        label = "Trip Price",
                        value = formatCurrency(it)
                    )
                }
            }
            trip.priority?.let {
                EnhancedInfoRow(
                    icon = "",
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
                FleetDateTimePicker(
                    date = state.departureDate,
                    time = state.departureTime,
                    onDateTimeChange = { newDate, newTime ->
                        viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureDate(newDate))
                        viewModel.sendIntent(TripDetailContract.Intent.UpdateDepartureTime(newTime))
                    },
                    label = "Departure",
                    mode = PickerMode.DATE_TIME,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Arrival Date & Time
                FleetDateTimePicker(
                    date = state.arrivalDate,
                    time = state.arrivalTime,
                    onDateTimeChange = { newDate, newTime ->
                        viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalDate(newDate))
                        viewModel.sendIntent(TripDetailContract.Intent.UpdateArrivalTime(newTime))
                    },
                    label = "Arrival",
                    mode = PickerMode.DATE_TIME,
                    minDate = state.departureDate.ifBlank { null },
                    modifier = Modifier.fillMaxWidth()
                )
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

                // Cargo Type Dropdown
                var showCargoTypeDropdown by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = showCargoTypeDropdown,
                    onExpandedChange = { showCargoTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = state.cargoType.takeIf { it.isNotBlank() }?.replaceFirstChar { it.uppercaseChar() } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cargo Type") },
                        placeholder = { Text("Select cargo type") },
                        leadingIcon = { Text("📦", modifier = Modifier.padding(start = 8.dp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCargoTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = showCargoTypeDropdown,
                        onDismissRequest = { showCargoTypeDropdown = false }
                    ) {
                        state.cargoTypeOptions.forEach { cargoType ->
                            DropdownMenuItem(
                                text = { Text(cargoType.replaceFirstChar { it.uppercaseChar() }) },
                                onClick = {
                                    viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoType(cargoType))
                                    showCargoTypeDropdown = false
                                },
                                leadingIcon = {
                                    val icon = when (cargoType.lowercase()) {
                                        "gitti" -> "🪨"
                                        "balu" -> "🏖️"
                                        "bhakshi" -> "🧱"
                                        "enta" -> "🧱"
                                        "hazardous" -> "⚠️"
                                        "valuable" -> "💎"
                                        else -> "📦"
                                    }
                                    Text(icon)
                                }
                            )
                        }
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

                // Cargo Weight with Unit dropdown (similar to CreateTripScreen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Weight input - accepts only decimal numbers
                    OutlinedTextField(
                        value = state.cargoWeight,
                        onValueChange = { newValue ->
                            // Only allow digits and decimal point
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                viewModel.sendIntent(TripDetailContract.Intent.UpdateCargoWeight(newValue))
                            }
                        },
                        label = { Text("Cargo Weight") },
                        placeholder = { Text("e.g., 500") },
                        leadingIcon = { Text("⚖️", modifier = Modifier.padding(start = 8.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Weight Unit Dropdown
                    var showWeightUnitDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = showWeightUnitDropdown,
                        onExpandedChange = { showWeightUnitDropdown = it },
                        modifier = Modifier.weight(0.6f)
                    ) {
                        OutlinedTextField(
                            value = state.weightUnit.ifBlank { "KG" },
                            onValueChange = {},
                            label = { Text("Unit") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showWeightUnitDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = showWeightUnitDropdown,
                            onDismissRequest = { showWeightUnitDropdown = false }
                        ) {
                            state.weightUnitOptions.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        viewModel.sendIntent(TripDetailContract.Intent.UpdateWeightUnit(unit))
                                        showWeightUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer Selection Section - Using shared component
                CustomerDetailsSection(
                    selectedCustomer = state.selectedCustomer,
                    customerName = state.customerName,
                    customerContact = state.customerContact,
                    isRefreshing = state.isRefreshingCustomers,
                    hasCustomers = state.customers.isNotEmpty(),
                    validationError = null,
                    onSelectClick = { viewModel.sendIntent(TripDetailContract.Intent.ToggleCustomerBottomSheet) },
                    onClearClick = { viewModel.sendIntent(TripDetailContract.Intent.ClearCustomerSelection) },
                    onRefreshClick = { viewModel.sendIntent(TripDetailContract.Intent.RefreshCustomers) },
                    onAddNewClick = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddCustomer) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trip Price - Only visible to Owner and General Manager
                if (state.canViewTripPrice) {
                    OutlinedTextField(
                        value = state.tripPrice,
                        onValueChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateTripPrice(it)) },
                        label = { Text("Trip Price (Expected) *") },
                        leadingIcon = { Text("💰", modifier = Modifier.padding(start = 8.dp)) },
                        placeholder = { Text("Enter trip price") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text(
                                text = "Expected Cost + Profit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

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


/**
 * Trip Costs Section - Flat list displaying costs with dialog for details.
 * Uses reusable components from [com.indusjs.fleet.core.ui.costs].
 * Only shown when there are costs (hasCosts == true).
 *
 * @param costs List of all trip costs
 * @param totalCost Sum of all cost amounts
 * @param costsByType Costs grouped by their cost type (for category count)
 * @param isLoading Whether costs are being loaded
 * @param onExportPdf Optional callback for PDF export action
 * @param onAddTripCost Optional callback to add a new trip cost
 */
@Composable
private fun TripCostsSection(
    costs: List<TripCostDto>,
    totalCost: Double,
    costsByType: Map<String, List<TripCostDto>>,
    isLoading: Boolean,
    onExportPdf: (() -> Unit)? = null,
    onAddTripCost: (() -> Unit)? = null
) {
    // State for dialog - which cost to show details for
    var selectedCost by remember { mutableStateOf<TripCostDto?>(null) }

    EnhancedSectionCard(
        title = "Trip Costs",
        icon = "💰"
    ) {
        when {
            isLoading -> {
                TripCostsLoadingContent()
            }
            costs.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💸",
                        style = MaterialTheme.typography.displaySmall
                    )
                    Text(
                        text = "No costs recorded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Trip expenses will appear here once added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    // Add Trip Cost button in empty state
                    if (onAddTripCost != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onAddTripCost,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("+ Add Trip Cost")
                        }
                    }
                }
            }
            else -> {
                // Total Cost Header
                com.indusjs.fleet.core.ui.costs.TotalCostHeader(
                    totalCost = totalCost,
                    transactionCount = costs.size,
                    categoryCount = costsByType.size
                )

                // Action buttons row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Add Trip Cost button
                    if (onAddTripCost != null) {
                        OutlinedButton(
                            onClick = onAddTripCost,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Add Cost", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    // Export to PDF Button
                    if (onExportPdf != null && costs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onExportPdf,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📄 Export PDF", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cost Breakdown Header
                Text(
                    text = "Cost Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Flat list of all costs - each item clickable to show dialog
                costs.forEachIndexed { index, cost ->
                    com.indusjs.fleet.core.ui.costs.CostListItem(
                        cost = cost,
                        totalCost = totalCost,
                        onClick = { selectedCost = cost }
                    )
                    if (index < costs.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Show dialog when a cost is selected
    selectedCost?.let { cost ->
        com.indusjs.fleet.core.ui.costs.CostDetailDialog(
            cost = cost,
            onDismiss = { selectedCost = null }
        )
    }
}

/**
 * Loading state content for Trip Costs section.
 */
@Composable
private fun TripCostsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp))
    }
}
