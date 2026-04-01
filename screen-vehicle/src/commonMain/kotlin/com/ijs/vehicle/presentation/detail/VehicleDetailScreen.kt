package com.ijs.vehicle.presentation.detail

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
import com.indusjs.uicomponents.components.DateInputField
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.indusjs.uicomponents.components.CaretakerInfoCard
import com.indusjs.uicomponents.components.CaretakerSectionCard
import com.indusjs.uicomponents.components.HistoryTabContent
import com.indusjs.uicomponents.components.StateChangeDialog
import com.ijs.vehicle.presentation.getVehicleStateOptions
import com.ijs.team.presentation.toCaretakerInfo
import com.ijs.team.presentation.toCaretakerInfoList
import com.ijs.vehicle.domain.entity.DocumentTypeDetail
import com.ijs.vehicle.domain.entity.RouteInfo
import com.ijs.vehicle.domain.entity.RouteStop
import com.ijs.vehicle.domain.entity.TripsSummary
import com.ijs.vehicle.domain.entity.Vehicle
import com.ijs.vehicle.domain.entity.VehicleDocumentsData
import com.ijs.vehicle.domain.entity.VehicleStatus
import com.ijs.vehicle.domain.entity.VehicleTripItem
import com.ijs.vehicle.domain.entity.VehicleType
import com.indusjs.pdfreport.handler.VehicleMaintenanceCostsPdfHandler
import com.indusjs.pdfreport.model.VehicleMaintenanceCostsPdfData
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
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
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<VehicleMaintenanceCostsPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

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
                is VehicleDetailContract.Effect.ExportMaintenanceCostsPdf -> {
                    isExportingPdf = true
                    pdfExportData = effect.pdfData
                }
            }
        }
    }

    // PDF Export Handler
    VehicleMaintenanceCostsPdfHandler(
        pdfData = pdfExportData,
        onExportComplete = {
            isExportingPdf = false
            pdfExportData = null
        },
        onExportError = { error ->
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    )

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
