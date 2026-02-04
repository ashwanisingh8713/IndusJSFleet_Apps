package com.indusjs.fleet.presentation.vehicles.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.ui.DateInputField
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.ui.ClickablePhoneRow
import com.indusjs.fleet.core.ui.caretaker.CaretakerInfoCard
import com.indusjs.fleet.core.ui.caretaker.CaretakerSectionCard
import com.indusjs.fleet.core.ui.history.HistoryTabContent
import com.indusjs.fleet.core.ui.state.StateChangeDialog
import com.indusjs.fleet.core.ui.state.getVehicleStateOptions
import com.indusjs.fleet.domain.entity.vehicle.DocumentTypeDetail
import com.indusjs.fleet.domain.entity.vehicle.RouteInfo
import com.indusjs.fleet.domain.entity.vehicle.RouteStop
import com.indusjs.fleet.domain.entity.vehicle.TripsSummary
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.domain.entity.vehicle.VehicleDocumentsData
import com.indusjs.fleet.domain.entity.vehicle.VehicleStatus
import com.indusjs.fleet.domain.entity.vehicle.VehicleTripItem
import com.indusjs.fleet.domain.entity.vehicle.VehicleType
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Tab definitions for Vehicle Detail Screen
 */
private enum class VehicleDetailTab(val title: String, val icon: String) {
    OVERVIEW("Overview", "📊"),
    TRIPS("Trips", "🚀"),
    COSTS("Costs", "💰"),
    ROUTE("Route & Stops", "📍"),
    DOCUMENTS("Documents", "📄"),
    HISTORY("History", "📋")
}

/**
 * Vehicle Detail Screen composable with Edit functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleDetailScreen(
    viewModel: VehicleDetailViewModel,
    vehicleId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToMaintenanceCost: (vehicleId: String) -> Unit = {},
    onRequestFilePicker: ((documentType: String, callback: (fileName: String, fileBytes: ByteArray, mimeType: String) -> Unit) -> Unit)? = null,
    onOpenDocumentPreview: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onDownloadDocument: ((documentName: String, fileUrl: String) -> Unit)? = null,
    onSaveDocument: ((documentName: String, fileBytes: ByteArray, mimeType: String) -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

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
                    isDownloading = false
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
                is VehicleDetailContract.Effect.DocumentUploaded -> {
                    // Document uploaded successfully - already handled in ViewModel
                }
                is VehicleDetailContract.Effect.OpenFilePicker -> {
                    // Trigger file picker
                    val documentType = state.selectedDocumentType
                    val documentTypeName = state.selectedDocumentTypeName
                    if (documentType != null && documentTypeName != null) {
                        onRequestFilePicker?.invoke(documentType) { fileName, fileBytes, mimeType ->
                            viewModel.sendIntent(
                                VehicleDetailContract.Intent.UploadDocument(
                                    documentType = documentType,
                                    documentName = documentTypeName,
                                    fileBytes = fileBytes,
                                    fileName = fileName,
                                    mimeType = mimeType,
                                    documentNumber = null,
                                    expiryDate = null
                                )
                            )
                        }
                    }
                }
                is VehicleDetailContract.Effect.OpenDocumentPreview -> {
                    if (onOpenDocumentPreview != null) {
                        onOpenDocumentPreview(effect.documentName, effect.fileUrl)
                    } else {
                        snackbarHostState.showSnackbar("Preview: ${effect.documentName}\nURL: ${effect.fileUrl}")
                    }
                }
                is VehicleDetailContract.Effect.DownloadDocumentFile -> {
                    if (onDownloadDocument != null) {
                        onDownloadDocument(effect.documentName, effect.fileUrl)
                    } else {
                        snackbarHostState.showSnackbar("Download: ${effect.documentName}\nURL: ${effect.fileUrl}")
                    }
                }
                is VehicleDetailContract.Effect.DocumentDownloading -> {
                    isDownloading = true
                    snackbarHostState.showSnackbar("Downloading document...")
                }
                is VehicleDetailContract.Effect.DocumentDownloaded -> {
                    isDownloading = false
                    if (onSaveDocument != null) {
                        onSaveDocument(effect.documentName, effect.fileBytes, effect.mimeType)
                        snackbarHostState.showSnackbar("Downloaded ${effect.documentName}")
                    } else {
                        snackbarHostState.showSnackbar("Document downloaded: ${effect.documentName} (${effect.fileBytes.size} bytes)")
                    }
                }
                is VehicleDetailContract.Effect.CostDeleted -> {
                    // Cost deleted - list is refreshed in ViewModel
                }
                is VehicleDetailContract.Effect.StateUpdated -> {
                    // State updated - handled by ViewModel
                }
                is VehicleDetailContract.Effect.NavigateToMaintenanceCost -> {
                    onNavigateToMaintenanceCost(effect.vehicleId)
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

    // State Change Dialog
    if (state.showStateChangeDialog && state.vehicle != null) {
        StateChangeDialog(
            title = "Change Vehicle Status",
            currentStateLabel = VehicleStatus.getDisplayLabel(state.vehicle!!.status),
            stateOptions = getVehicleStateOptions(state.vehicle!!.status),
            onStateSelected = { newState ->
                viewModel.sendIntent(VehicleDetailContract.Intent.UpdateVehicleState(newState))
            },
            onDismiss = {
                viewModel.sendIntent(VehicleDetailContract.Intent.HideStateChangeDialog)
            },
            isLoading = state.isUpdatingState
        )
    }

    Scaffold(
        topBar = {
            val vehicle = state.vehicle
            TopAppBar(
                title = {
                    if (state.isEditMode) {
                        Text("Edit Vehicle")
                    } else {
                        Column {
                            Text(
                                text = vehicle?.registrationNumber ?: "Vehicle Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (vehicle != null) {
                                Text(
                                    text = "${vehicle.make} ${vehicle.model}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
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
                    if (!state.isEditMode && vehicle != null) {
                        // Status Badge - clickable to change state
                        Box(
                            modifier = Modifier.clickable {
                                viewModel.sendIntent(VehicleDetailContract.Intent.ShowStateChangeDialog)
                            }
                        ) {
                            StatusChip(status = vehicle.status)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Edit Button
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
                    screenContext = FleetErrorContext.VEHICLE_DETAIL,
                    onRetry = { viewModel.sendIntent(VehicleDetailContract.Intent.Refresh) }
                )
            }
            state.vehicle != null -> {
                if (state.isEditMode) {
                    // Edit Mode - Show edit form without tabs
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { EditModeContent(state, viewModel) }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                } else {
                    // View Mode with Tabs
                    VehicleDetailTabbedContent(
                        vehicle = state.vehicle!!,
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding)
                    )
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

        // Uploading document overlay
        if (state.isUploading) {
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
                        Text("Uploading ${state.selectedDocumentTypeName ?: "document"}...")
                    }
                }
            }
        }
    }
}

/**
 * Tabbed content for Vehicle Detail screen
 */
@Composable
private fun VehicleDetailTabbedContent(
    vehicle: Vehicle,
    viewModel: VehicleDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tabs = VehicleDetailTab.entries
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val scope = rememberCoroutineScope()

    // Notify viewModel of tab changes for lazy loading
    LaunchedEffect(pagerState.currentPage) {
        viewModel.sendIntent(VehicleDetailContract.Intent.SelectTab(pagerState.currentPage))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp,
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tab.icon,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tab Content with Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (tabs[page]) {
                VehicleDetailTab.OVERVIEW -> OverviewTabContent(vehicle = vehicle, state = state, viewModel = viewModel)
                VehicleDetailTab.TRIPS -> TripsTabContent(
                    tripsList = state.tripsList,
                    tripsSummary = state.tripsSummary,
                    isLoading = state.isLoadingTrips,
                    error = state.tripsError,
                    hasMore = state.hasMoreTrips,
                    onLoadMore = { viewModel.sendIntent(VehicleDetailContract.Intent.LoadMoreTrips) },
                    onRefresh = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshTrips) }
                )
                VehicleDetailTab.COSTS -> CostsTabContent(
                    state = state,
                    viewModel = viewModel
                )
                VehicleDetailTab.ROUTE -> RouteTabContent(
                    routeInfo = state.routeInfo,
                    isLoading = state.isLoadingRoute,
                    error = state.routeError,
                    onRefresh = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshRoute) }
                )
                VehicleDetailTab.DOCUMENTS -> DocumentsTabContent(
                    documentsData = state.documentsData,
                    isLoading = state.isLoadingDocuments,
                    error = state.documentsError,
                    onRefresh = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshDocuments) },
                    onUploadClick = { documentType, documentTypeName ->
                        viewModel.sendIntent(VehicleDetailContract.Intent.ShowUploadDialog(documentType, documentTypeName))
                    },
                    onPreviewClick = { documentId, documentName, fileUrl ->
                        viewModel.sendIntent(VehicleDetailContract.Intent.PreviewDocument(documentId, documentName, fileUrl))
                    },
                    onDownloadClick = { documentId, documentName, fileUrl ->
                        viewModel.sendIntent(VehicleDetailContract.Intent.DownloadDocument(documentId, documentName, fileUrl))
                    },
                    onReplaceClick = { documentType, documentTypeName ->
                        viewModel.sendIntent(VehicleDetailContract.Intent.ReplaceDocument(documentType, documentTypeName))
                    }
                )
                VehicleDetailTab.HISTORY -> HistoryTabContent(
                    items = state.historyItems,
                    isLoading = state.isLoadingHistory,
                    error = state.historyError,
                    hasMore = state.hasMoreHistory,
                    onLoadMore = { viewModel.sendIntent(VehicleDetailContract.Intent.LoadMoreHistory) },
                    onRetry = { viewModel.sendIntent(VehicleDetailContract.Intent.LoadHistory) }
                )
            }
        }
    }
}

/**
 * Overview Tab - Shows vehicle info, stats, current location, assigned driver
 */
@Composable
private fun OverviewTabContent(
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
                caretaker = state.selectedCaretaker,
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


/**
 * Current Location Section
 */
@Composable
private fun CurrentLocationSection(vehicle: Vehicle) {
    EnhancedSectionCard(
        title = "Current Location",
        icon = "📍"
    ) {
        if (vehicle.lastLocation != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lat: ${vehicle.lastLocation.latitude}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lng: ${vehicle.lastLocation.longitude}",
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

/**
 * Assigned Driver Section - only shown when a driver is assigned
 */
@Composable
private fun AssignedDriverSection(vehicle: Vehicle) {
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

/**
 * Trips Tab - Shows trip history for this vehicle
 */
@Composable
private fun TripsTabContent(
    tripsList: List<VehicleTripItem>,
    tripsSummary: TripsSummary,
    isLoading: Boolean,
    error: String?,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit
) {
    when {
        isLoading && tripsList.isEmpty() -> {
            LoadingContent(message = "Loading trips...")
        }
        error != null && tripsList.isEmpty() -> {
            ErrorContent(
                error = error,
                screenContext = FleetErrorContext.TRIPS,
                onRetry = onRefresh
            )
        }
        tripsList.isEmpty() -> {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🚀", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No trips found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "This vehicle hasn't completed any trips yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Trip Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TripStatItem(
                                count = tripsSummary.total.toString(),
                                label = "Total",
                                color = MaterialTheme.colorScheme.primary
                            )
                            TripStatItem(
                                count = tripsSummary.inProgress.toString(),
                                label = "Active",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            TripStatItem(
                                count = tripsSummary.completed.toString(),
                                label = "Completed",
                                color = MaterialTheme.colorScheme.secondary
                            )
                            TripStatItem(
                                count = tripsSummary.planned.toString(),
                                label = "Planned",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Trip List Header
                item {
                    Text(
                        text = "Trips History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Trip Items
                items(tripsList.size) { index ->
                    TripItemCard(trip = tripsList[index])

                    // Load more when reaching end
                    if (index == tripsList.size - 1 && hasMore && !isLoading) {
                        LaunchedEffect(Unit) {
                            onLoadMore()
                        }
                    }
                }

                // Loading more indicator
                if (isLoading && tripsList.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun TripStatItem(count: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TripItemCard(trip: VehicleTripItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trip.tripNumber ?: "Trip #${trip.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TripStatusChip(status = trip.stateLabel.ifEmpty { trip.state })
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📍", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trip.origin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " → ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = trip.destination,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.scheduledDate?.let { formatIsoDateToDisplay(it) } ?: trip.duration ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                trip.distance?.let { distance ->
                    Text(
                        text = "${distance.toInt()} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Driver info if available
            trip.driverName?.let { driverName ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👤 $driverName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TripStatusChip(status: String) {
    val (color, bgColor) = when (status) {
        "Completed" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        "In Progress" -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        "Planned" -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Route & Stops Tab - Shows active route with stops
 */
@Composable
private fun RouteTabContent(
    routeInfo: RouteInfo?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit
) {
    when {
        isLoading && routeInfo == null -> {
            LoadingContent(message = "Loading route...")
        }
        error != null && routeInfo == null -> {
            ErrorContent(
                error = error,
                screenContext = FleetErrorContext.VEHICLE_DETAIL,
                onRetry = onRefresh
            )
        }
        else -> {
            val hasActiveTrip = routeInfo?.hasActiveTrip ?: false
            val stops = routeInfo?.stops ?: emptyList()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasActiveTrip && routeInfo != null) {
                    // Active Trip Info
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🚀 Active Trip",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = routeInfo.tripNumber ?: "Trip #${routeInfo.tripId}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Route origin -> destination
                                if (routeInfo.origin != null || routeInfo.destination != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📍", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${routeInfo.origin ?: "Origin"} → ${routeInfo.destination ?: "Destination"}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                // Driver info
                                routeInfo.driverName?.let { driverName ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "👤 $driverName",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress Bar
                                val progress = routeInfo.progress
                                if (progress != null) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Progress",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${progress.percentage}%",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress.percentage / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.tertiary,
                                            trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "ETA",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = progress.eta ?: "N/A",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "Distance Left",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${progress.distanceRemaining.toInt()} km",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stops Header
                    if (stops.isNotEmpty()) {
                        item {
                            Text(
                                text = "Route Stops",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Stops List with Timeline
                        items(stops.size) { index ->
                            StopItemWithTimeline(
                                stop = stops[index],
                                isFirst = index == 0,
                                isLast = index == stops.size - 1
                            )
                        }
                    }
                } else {
                    // No Active Trip
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛣️", style = MaterialTheme.typography.displaySmall)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Active Trip",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "This vehicle is not currently on a trip",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StopItemWithTimeline(
    stop: RouteStop,
    isFirst: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                        .background(
                            if (stop.status == "Completed") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Circle indicator
            val circleColor = when (stop.status) {
                "Completed" -> MaterialTheme.colorScheme.primary
                "Current" -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(circleColor, CircleShape)
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (stop.status == "Completed") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Stop Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (stop.status == "Current")
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stop.type.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    StopStatusBadge(status = stop.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stop.location,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                stop.address?.let { address ->
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stop.scheduledTime ?: stop.actualTime ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StopStatusBadge(status: String) {
    val (text, color) = when (status) {
        "Completed" -> "✓ Done" to MaterialTheme.colorScheme.primary
        "Current" -> "● Now" to MaterialTheme.colorScheme.tertiary
        else -> "○ Pending" to MaterialTheme.colorScheme.outline
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.Medium
    )
}

/**
 * Documents Tab - Shows all vehicle documents
 */
@Composable
private fun DocumentsTabContent(
    documentsData: VehicleDocumentsData?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onUploadClick: (documentType: String, documentTypeName: String) -> Unit,
    onPreviewClick: (documentId: String, documentName: String, fileUrl: String?) -> Unit = { _, _, _ -> },
    onDownloadClick: (documentId: String, documentName: String, fileUrl: String?) -> Unit = { _, _, _ -> },
    onReplaceClick: (documentType: String, documentTypeName: String) -> Unit = { _, _ -> }
) {
    when {
        isLoading && documentsData == null -> {
            LoadingContent(message = "Loading documents...")
        }
        error != null && documentsData == null -> {
            ErrorContent(
                error = error,
                screenContext = FleetErrorContext.DOCUMENTS,
                onRetry = onRefresh
            )
        }
        else -> {
            val summary = documentsData?.summary
            val documentTypes = documentsData?.documentTypes ?: emptyList()
            val alertDocs = documentsData?.alertDocs ?: emptyList()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Documents Summary
                if (summary != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DocumentStatItem(
                                    count = summary.uploaded.toString(),
                                    label = "Uploaded",
                                    icon = "📄"
                                )
                                DocumentStatItem(
                                    count = summary.notUploaded.toString(),
                                    label = "Missing",
                                    icon = "⚠️"
                                )
                                DocumentStatItem(
                                    count = summary.expiringSoon.toString(),
                                    label = "Expiring",
                                    icon = "⏰"
                                )
                            }
                        }
                    }
                }

                // Alert Documents
                if (alertDocs.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "⚠️ Attention Required",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                alertDocs.forEach { alert ->
                                    Text(
                                        text = "• ${alert.typeName} - ${alert.message}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // All Documents Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All Documents",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Document List
                if (documentTypes.isNotEmpty()) {
                    items(documentTypes.size) { index ->
                        val doc = documentTypes[index]
                        DocumentTypeCard(
                            doc = doc,
                            onUploadClick = { onUploadClick(doc.type, doc.typeName) },
                            onPreviewClick = {
                                val documentId = doc.document?.id ?: ""
                                val fileUrl = doc.document?.fileUrl
                                onPreviewClick(documentId, doc.typeName, fileUrl)
                            },
                            onDownloadClick = {
                                val documentId = doc.document?.id ?: ""
                                val fileUrl = doc.document?.fileUrl
                                onDownloadClick(documentId, doc.typeName, fileUrl)
                            },
                            onReplaceClick = { onReplaceClick(doc.type, doc.typeName) }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "No documents found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DocumentStatItem(count: String, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DocumentTypeCard(
    doc: DocumentTypeDetail,
    onUploadClick: () -> Unit,
    onPreviewClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onReplaceClick: () -> Unit = {}
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showUploadConfirmDialog by remember { mutableStateOf(false) }

    val isExpiringSoon = doc.document?.daysRemaining != null && doc.document.daysRemaining <= 30
    val isExpired = doc.document?.daysRemaining != null && doc.document.daysRemaining <= 0

    // Upload confirmation dialog
    if (showUploadConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUploadConfirmDialog = false },
            icon = { Text("📤", style = MaterialTheme.typography.headlineMedium) },
            title = { Text("Upload ${doc.typeName}") },
            text = {
                Column {
                    Text("You are about to upload a document for:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = doc.typeName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (doc.isRequired) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This is a required document.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Please ensure the document is clear and readable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showUploadConfirmDialog = false
                    onUploadClick()
                }) {
                    Text("Select File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (doc.isUploaded) {
        // ==================== UPLOADED DOCUMENT CARD ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with gradient accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            when {
                                isExpired -> MaterialTheme.colorScheme.error
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                )

                // Main content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Document icon with check badge
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isExpired -> MaterialTheme.colorScheme.errorContainer
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            tonalElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = when {
                                        isExpired -> MaterialTheme.colorScheme.error
                                        isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }

                        // Status indicator
                        Surface(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp),
                            shape = CircleShape,
                            color = when {
                                isExpired -> MaterialTheme.colorScheme.error
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = when {
                                        isExpired -> "!"
                                        isExpiringSoon -> "⏰"
                                        else -> "✓"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Document details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.typeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status row with icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isExpired -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    isExpiringSoon -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = when {
                                        isExpired -> "⚠️ Expired"
                                        isExpiringSoon -> "⏰ Expires in ${doc.document?.daysRemaining} days"
                                        doc.document?.expiryDate != null -> "✓ Valid till ${formatIsoDateToDisplay(doc.document.expiryDate)}"
                                        else -> "✓ ${doc.document?.statusLabel ?: "Uploaded"}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        isExpired -> MaterialTheme.colorScheme.error
                                        isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Document number if available
                        if (!doc.document?.documentNumber.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔢",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Doc #: ${doc.document?.documentNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Upload date if available
                        doc.document?.uploadedAt?.let { uploadedAt ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📅",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Uploaded: $uploadedAt",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // More options button
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert),
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("👁️", modifier = Modifier.padding(end = 8.dp))
                                        Text("Preview")
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onPreviewClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("⬇️", modifier = Modifier.padding(end = 8.dp))
                                        Text("Download")
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onDownloadClick()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🔄", modifier = Modifier.padding(end = 8.dp))
                                        Text("Replace")
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onReplaceClick()
                                }
                            )
                        }
                    }
                }

                // Action buttons row
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(
                        onClick = onPreviewClick,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledTonalButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).rotate(270f),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Download",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    FilledTonalButton(
                        onClick = onReplaceClick,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Replace",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    } else {
        // ==================== NOT UPLOADED DOCUMENT CARD ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "📁",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Document info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.typeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Not uploaded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Upload button - streamlined without icon
                Button(
                    onClick = { showUploadConfirmDialog = true },
                    modifier = Modifier
                        .height(38.dp)
                        .widthIn(min = 80.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Upload",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusChip(status: VehicleStatus) {
    val colorScheme = VehicleStatus.getColorScheme(status)
    val baseColor = when (colorScheme) {
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> MaterialTheme.colorScheme.secondary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> MaterialTheme.colorScheme.tertiary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
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
        // ==================== Vehicle Information Section ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚗", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Vehicle Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Registration (read-only)
                OutlinedTextField(
                    value = state.registrationNumber,
                    onValueChange = { },
                    label = { Text("Registration Number") },
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
                        label = { Text("Make *") },
                        placeholder = { Text("e.g., Toyota") },
                        isError = state.makeError != null,
                        supportingText = state.makeError?.let { { Text(it) } },
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
                        label = { Text("Model *") },
                        placeholder = { Text("e.g., Fortuner") },
                        isError = state.modelError != null,
                        supportingText = state.modelError?.let { { Text(it) } },
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
                        label = { Text("Year *") },
                        placeholder = { Text("e.g., 2024") },
                        isError = state.yearError != null,
                        supportingText = state.yearError?.let { { Text(it) } },
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
                        label = { Text("Mileage (km)") },
                        placeholder = { Text("e.g., 50000") },
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
            }
        }

        // ==================== Specifications Section ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚙️", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

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
                        label = { Text("Capacity") },
                        placeholder = { Text("e.g., 7") },
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
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👤", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assigned Driver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary                    )
                }

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
                                Text("Loading drivers...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(
                                    text = state.selectedDriver?.let { "${it.firstName} ${it.lastName}" }
                                        ?: "No driver assigned",
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
                                            text = "No driver (Unassign)",
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
            selectedCaretaker = state.selectedCaretaker,
            caretakers = state.caretakers,
            onCaretakerSelected = { viewModel.sendIntent(VehicleDetailContract.Intent.SelectCaretaker(it)) },
            onRefresh = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshCaretakers) },
            isLoading = state.isLoadingCaretakers
        )
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

private fun getStatusLabel(status: VehicleStatus): String =
    VehicleStatus.getDisplayLabel(status)

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
        "$dayStr-$monthStr-$years" // DD-MM-YYYY format
    } catch (_: Exception) {
        "N/A"
    }
}

/**
 * Converts ISO date string (YYYY-MM-DD) to display format (DD-MM-YYYY).
 */
private fun formatIsoDateToDisplay(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "N/A"
    return try {
        val datePart = isoDate.split("T").firstOrNull() ?: isoDate
        val parts = datePart.split("-")
        if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0]}" // DD-MM-YYYY
        } else {
            datePart
        }
    } catch (_: Exception) {
        isoDate
    }
}

// ==================== Costs Tab ====================

/**
 * Costs Tab Content - Shows Trip Costs and Maintenance Costs with overlay filter sheet.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CostsTabContent(
    state: VehicleDetailContract.State,
    viewModel: VehicleDetailViewModel,
    modifier: Modifier = Modifier
) {
    // Temp filter values for bottom sheet
    var tempStartDate by remember { mutableStateOf(state.costsStartDate) }
    var tempEndDate by remember { mutableStateOf(state.costsEndDate) }
    var tempSelectedFilters by remember { mutableStateOf(state.selectedCostTypeFilters) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Sync temp values when sheet opens
    LaunchedEffect(state.showCostsFilterSheet) {
        if (state.showCostsFilterSheet) {
            tempStartDate = state.costsStartDate
            tempEndDate = state.costsEndDate
            tempSelectedFilters = state.selectedCostTypeFilters
            // Load cost types from local database when filter sheet opens
            viewModel.sendIntent(VehicleDetailContract.Intent.LoadCostTypes)
        }
    }

    // Load costs on first composition
    LaunchedEffect(Unit) {
        if (state.tripCosts.isEmpty() && state.maintenanceCosts.isEmpty() && !state.isLoadingCosts) {
            viewModel.sendIntent(VehicleDetailContract.Intent.LoadCosts)
        }
    }

    // Filter Bottom Sheet
    if (state.showCostsFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.sendIntent(VehicleDetailContract.Intent.HideCostsFilterSheet) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        tempStartDate = ""
                        tempEndDate = ""
                        tempSelectedFilters = emptySet()
                    }) {
                        Text("Clear All")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Date Range Section
                Text("Date Range", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateInputField(
                        value = tempStartDate,
                        onValueChange = { tempStartDate = it },
                        label = "From",
                        modifier = Modifier.weight(1f)
                    )
                    DateInputField(
                        value = tempEndDate,
                        onValueChange = { tempEndDate = it },
                        label = "To",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Cost Types Section (Multi-select)
                Text("Cost Types", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Select one or more", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(10.dp))

                // Loading indicator for cost types
                if (state.isLoadingCostTypes) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    // Trip Cost Types - from local database
                    if (state.tripCostTypeGroups.isNotEmpty()) {
                        Text("Trip Costs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))

                        state.tripCostTypeGroups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        },
                                        label = { Text(item.label) },
                                        leadingIcon = if (tempSelectedFilters.contains(item.id)) {
                                            { Text("✓", style = MaterialTheme.typography.labelSmall) }
                                        } else null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        // Fallback to static cost types from TripCostTypes object
                        Text("Trip Costs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        com.indusjs.fleet.data.model.costs.TripCostTypes.groups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        },
                                        label = { Text(item.label) },
                                        leadingIcon = if (tempSelectedFilters.contains(item.id)) {
                                            { Text("✓", style = MaterialTheme.typography.labelSmall) }
                                        } else null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Maintenance Cost Types - from local database
                    if (state.maintenanceCostTypeGroups.isNotEmpty()) {
                        Text("Maintenance Costs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))

                        state.maintenanceCostTypeGroups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        },
                                        label = { Text(item.label) },
                                        leadingIcon = if (tempSelectedFilters.contains(item.id)) {
                                            { Text("✓", style = MaterialTheme.typography.labelSmall) }
                                        } else null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    } else {
                        // Fallback to static cost types from MaintenanceCostTypes object
                        Text("Maintenance Costs", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.height(6.dp))
                        com.indusjs.fleet.data.model.costs.MaintenanceCostTypes.groups.forEach { group ->
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                group.items.forEach { item ->
                                    FilterChip(
                                        selected = tempSelectedFilters.contains(item.id),
                                        onClick = {
                                            tempSelectedFilters = if (tempSelectedFilters.contains(item.id)) {
                                                tempSelectedFilters - item.id
                                            } else {
                                                tempSelectedFilters + item.id
                                            }
                                        },
                                        label = { Text(item.label) },
                                        leadingIcon = if (tempSelectedFilters.contains(item.id)) {
                                            { Text("✓", style = MaterialTheme.typography.labelSmall) }
                                        } else null
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.HideCostsFilterSheet) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            viewModel.sendIntent(
                                VehicleDetailContract.Intent.ApplyCostFilters(tempStartDate, tempEndDate, tempSelectedFilters)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }

    // Combine and group costs by date
    val allCosts = (state.tripCosts.map { CostDisplayItem.fromTripCost(it) } +
            state.maintenanceCosts.map { CostDisplayItem.fromMaintenanceCost(it) })
        .sortedByDescending { it.date }
    val groupedByDate = allCosts.groupBy { it.dateLabel }
    val activeFilterCount = state.selectedCostTypeFilters.size +
        (if (state.costsStartDate.isNotBlank() || state.costsEndDate.isNotBlank()) 1 else 0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Compact Summary Header with Filter Button and Add Cost Button
        item(key = "header") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "₹${formatAmount(state.costsTotalAmount)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row {
                            Text("Trip: ₹${formatAmount(state.tripCostsTotalAmount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(" • ", style = MaterialTheme.typography.labelSmall)
                            Text("Maint: ₹${formatAmount(state.maintenanceCostsTotalAmount)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add Cost Button
                        FilledTonalButton(
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.NavigateToAddMaintenanceCost) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("+ Add Cost", style = MaterialTheme.typography.labelMedium)
                        }

                        // Filter Button with Badge
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge { Text("$activeFilterCount") }
                                }
                            }
                        ) {
                            FilledTonalIconButton(
                                onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ShowCostsFilterSheet) }
                            ) {
                                Text("🔍", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }

        // Active Filters Chips (dismissable)
        if (activeFilterCount > 0) {
            item(key = "active_filters") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.selectedCostTypeFilters.forEach { filter ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ToggleCostTypeFilter(filter)) },
                            label = { Text(getCostTypeLabel(filter), style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = { Text("✕", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                    if (state.costsStartDate.isNotBlank() || state.costsEndDate.isNotBlank()) {
                        InputChip(
                            selected = true,
                            onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.UpdateCostsDateRange("", "")) },
                            label = {
                                Text(
                                    "${state.costsStartDate.ifBlank { "..." }} → ${state.costsEndDate.ifBlank { "..." }}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            trailingIcon = { Text("✕", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // Loading
        if (state.isLoadingCosts && allCosts.isEmpty()) {
            item(key = "loading") {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }

        // Error
        state.costsError?.let { error ->
            item(key = "error") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshCosts) }) { Text("Retry") }
                    }
                }
            }
        }

        // Empty State
        if (allCosts.isEmpty() && !state.isLoadingCosts && state.costsError == null) {
            item(key = "empty") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💰", style = MaterialTheme.typography.displaySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No costs recorded", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Trip and maintenance costs appear here", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.RefreshCosts) }) { Text("🔄 Refresh") }
                    }
                }
            }
        }

        // Grouped Costs by Date
        groupedByDate.forEach { (dateLabel, costs) ->
            item(key = "date_$dateLabel") {
                val dateTotal = costs.sumOf { it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text("📅", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(dateLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(" (${costs.size})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("₹${formatAmount(dateTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                }
            }
            costs.forEach { cost ->
                item(key = "cost_${cost.category}_${cost.id}") {
                    CompactCostCard(cost = cost, onDelete = { viewModel.sendIntent(VehicleDetailContract.Intent.DeleteCost(cost.id, cost.category)) })
                }
            }
        }

        // Load More
        if (state.hasMoreCosts) {
            item(key = "load_more") {
                Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (state.isLoadingCosts) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    else TextButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.LoadMoreCosts) }) { Text("Load More") }
                }
            }
        }
    }

    // Delete Dialog
    if (state.showDeleteCostDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(VehicleDetailContract.Intent.DismissDeleteCostDialog) },
            title = { Text("Delete Cost") },
            text = { Text("Delete this cost entry?") },
            confirmButton = { TextButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.ConfirmDeleteCost) }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { viewModel.sendIntent(VehicleDetailContract.Intent.DismissDeleteCostDialog) }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CompactCostCard(cost: CostDisplayItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (cost.category == "trip") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            ) {
                Text(cost.icon, modifier = Modifier.padding(6.dp), style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cost.typeLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = if (cost.category == "trip") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            if (cost.category == "trip") "Trip" else "Maint",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (cost.category == "trip") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
                cost.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${formatAmount(cost.amount)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Text("🗑️", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private data class CostDisplayItem(
    val id: String,
    val category: String,
    val costType: String,
    val amount: Double,
    val date: String,
    val dateLabel: String,
    val description: String?,
    val vendorName: String?,
    val icon: String,
    val typeLabel: String
) {
    companion object {
        fun fromTripCost(dto: com.indusjs.fleet.data.model.costs.TripCostDto): CostDisplayItem {
            return CostDisplayItem(
                id = dto.id.toString(),
                category = "trip",
                costType = dto.effectiveCostType,
                amount = dto.amount,
                date = dto.date,
                dateLabel = formatCostDateLabel(dto.date),
                description = dto.notes,
                vendorName = null,
                icon = getCostTypeIcon(dto.effectiveCostType),
                typeLabel = dto.displayLabel
            )
        }

        fun fromMaintenanceCost(dto: com.indusjs.fleet.data.model.costs.MaintenanceCostDto): CostDisplayItem {
            return CostDisplayItem(
                id = dto.id.toString(),
                category = "maintenance",
                costType = dto.effectiveCostType,
                amount = dto.amount,
                date = dto.date,
                dateLabel = formatCostDateLabel(dto.date),
                description = dto.description,
                vendorName = dto.vendorName,
                icon = getCostTypeIcon(dto.effectiveCostType),
                typeLabel = dto.displayLabel
            )
        }

        private fun formatCostDateLabel(date: String?): String {
            if (date.isNullOrBlank()) return "Unknown"
            return try {
                val datePart = date.split("T").firstOrNull() ?: date
                if (datePart.contains("-") && datePart.length >= 10) {
                    val parts = datePart.split("-")
                    if (parts.size == 3) {
                        if (parts[0].length == 4) {
                            "${parts[2]}-${parts[1]}-${parts[0]}"
                        } else {
                            datePart
                        }
                    } else datePart
                } else datePart
            } catch (e: Exception) {
                date ?: "Unknown"
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        val intAmount = amount.toLong()
        val formatted = StringBuilder()
        val str = intAmount.toString()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            if (count > 0 && count % 3 == 0) {
                formatted.insert(0, ',')
            }
            formatted.insert(0, str[i])
            count++
        }
        formatted.toString()
    } else {
        val intPart = amount.toLong()
        val decPart = ((amount - intPart) * 100).toInt()
        "$intPart.${decPart.toString().padStart(2, '0')}"
    }
}

private fun getCostTypeIcon(type: String): String = when (type.lowercase()) {
    "fuel" -> "⛽"
    "toll" -> "🛣️"
    "tyre" -> "🛞"
    "battery" -> "🔋"
    "oil_change" -> "🛢️"
    "brake", "brake_service" -> "🛑"
    "engine", "engine_repair" -> "🔧"
    "driver_allowance" -> "👤"
    "loading" -> "📦"
    "unloading" -> "📤"
    "parking" -> "🅿️"
    "cleaning" -> "🧹"
    "servicing" -> "🔩"
    "electrical" -> "⚡"
    "body_work" -> "🚗"
    "rto" -> "📋"
    "police" -> "🚔"
    "food" -> "🍽️"
    "halt" -> "⏸️"
    "commission" -> "💵"
    "weighing" -> "⚖️"
    "detention" -> "⏰"
    else -> "💰"
}

private fun getCostTypeLabel(type: String): String = when (type.lowercase()) {
    "all" -> "All"
    // Trip Cost Types
    "fuel" -> "Fuel"
    "toll" -> "Toll"
    "driver_allowance" -> "Driver Allowance"
    "parking" -> "Parking"
    "loading_charges" -> "Loading"
    "unloading_charges" -> "Unloading"
    "insurance" -> "Insurance"
    "permit" -> "Permit"
    "registration_renewal" -> "Registration"
    "fitness_check" -> "Fitness Check"
    "emission_test" -> "Emission Test"
    "state_permit" -> "State Permit"
    "national_permit" -> "National Permit"
    "chalan" -> "Chalan/Fine"
    // Maintenance Cost Types
    "tyre" -> "Tyre"
    "battery" -> "Battery"
    "oil_change" -> "Oil Change"
    "brake_service" -> "Brake"
    "engine_repair" -> "Engine"
    "clutch_repair" -> "Clutch"
    "suspension" -> "Suspension"
    "electrical" -> "Electrical"
    "body_work" -> "Body Work"
    "cleaning" -> "Cleaning"
    "servicing" -> "Servicing"
    // Legacy/Other
    "brake" -> "Brake"
    "engine" -> "Engine"
    "loading" -> "Loading"
    "unloading" -> "Unloading"
    "rto" -> "RTO"
    "police" -> "Police"
    "food" -> "Food"
    "halt" -> "Halt"
    "commission" -> "Commission"
    "weighing" -> "Weighing"
    "detention" -> "Detention"
    "other", "miscellaneous" -> "Other"
    else -> type.replace("_", " ").replaceFirstChar { it.uppercaseChar() }
}


