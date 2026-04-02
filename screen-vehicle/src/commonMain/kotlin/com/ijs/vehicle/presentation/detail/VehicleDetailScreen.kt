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
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetTab
import com.indusjs.uicomponents.components.FleetTabBar
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
import org.jetbrains.compose.resources.stringResource

/**
 * Tab definitions for Vehicle Detail Screen
 */
private enum class VehicleDetailTab(val icon: String) {
    OVERVIEW("📊"),
    TRIPS("🚀"),
    COSTS("💰"),
    ROUTE("📍"),
    DOCUMENTS("📄"),
    HISTORY("📋")
}

@Composable
private fun VehicleDetailTab.titleText(): String = when (this) {
    VehicleDetailTab.OVERVIEW -> stringResource(Res.string.vehicles_overview)
    VehicleDetailTab.TRIPS -> stringResource(Res.string.vehicles_trips)
    VehicleDetailTab.COSTS -> stringResource(Res.string.vehicles_costs)
    VehicleDetailTab.ROUTE -> stringResource(Res.string.vehicle_detail_tab_route)
    VehicleDetailTab.DOCUMENTS -> stringResource(Res.string.vehicles_documents)
    VehicleDetailTab.HISTORY -> stringResource(Res.string.vehicle_detail_tab_history)
}

private fun applySnackbarFormat(template: String, vararg args: Any): String {
    var result = template
    args.forEachIndexed { index, arg ->
        val n = index + 1
        result = result.replace("%${n}\$s", arg.toString()).replace("%${n}\$d", arg.toString())
    }
    return result
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

    val snackbarPreviewFmt = stringResource(Res.string.vehicle_snackbar_preview)
    val snackbarDownloadFmt = stringResource(Res.string.vehicle_snackbar_download)
    val snackbarDownloading = stringResource(Res.string.vehicle_snackbar_downloading)
    val snackbarDownloadedFmt = stringResource(Res.string.vehicle_snackbar_downloaded)
    val snackbarDownloadedBytesFmt = stringResource(Res.string.vehicle_snackbar_downloaded_bytes)

    // Load vehicle on first composition
    LaunchedEffect(vehicleId) {
        viewModel.sendIntent(VehicleDetailContract.Intent.LoadVehicle(vehicleId))
    }

    // Handle effects
    LaunchedEffect(
        snackbarPreviewFmt,
        snackbarDownloadFmt,
        snackbarDownloading,
        snackbarDownloadedFmt,
        snackbarDownloadedBytesFmt
    ) {
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
                        snackbarHostState.showSnackbar(
                            applySnackbarFormat(snackbarPreviewFmt, effect.documentName, effect.fileUrl)
                        )
                    }
                }
                is VehicleDetailContract.Effect.DownloadDocumentFile -> {
                    if (onDownloadDocument != null) {
                        onDownloadDocument(effect.documentName, effect.fileUrl)
                    } else {
                        snackbarHostState.showSnackbar(
                            applySnackbarFormat(snackbarDownloadFmt, effect.documentName, effect.fileUrl)
                        )
                    }
                }
                is VehicleDetailContract.Effect.DocumentDownloading -> {
                    isDownloading = true
                    snackbarHostState.showSnackbar(snackbarDownloading)
                }
                is VehicleDetailContract.Effect.DocumentDownloaded -> {
                    isDownloading = false
                    if (onSaveDocument != null) {
                        onSaveDocument(effect.documentName, effect.fileBytes, effect.mimeType)
                        snackbarHostState.showSnackbar(
                            applySnackbarFormat(snackbarDownloadedFmt, effect.documentName)
                        )
                    } else {
                        snackbarHostState.showSnackbar(
                            applySnackbarFormat(
                                snackbarDownloadedBytesFmt,
                                effect.documentName,
                                effect.fileBytes.size
                            )
                        )
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
            title = { Text(stringResource(Res.string.vehicle_detail_delete)) },
            text = {
                Text(
                    stringResource(
                        Res.string.delete_entity_confirmation_message,
                        stringResource(Res.string.vehicle_entity_singular),
                        state.vehicle?.registrationNumber.orEmpty()
                    )
                )
            },
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
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    // State Change Dialog
    if (state.showStateChangeDialog && state.vehicle != null) {
        StateChangeDialog(
            title = stringResource(Res.string.vehicle_detail_change_status),
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
                        Text(stringResource(Res.string.vehicle_detail_edit))
                    } else {
                        Column {
                            Text(
                                text = vehicle?.registrationNumber ?: stringResource(Res.string.vehicles_detail),
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
                            contentDescription = if (state.isEditMode) {
                                stringResource(Res.string.cancel)
                            } else {
                                stringResource(Res.string.back)
                            },
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
                                contentDescription = stringResource(Res.string.edit),
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
                            Text(stringResource(Res.string.cancel))
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
                            Text(
                                if (state.isSaving) {
                                    stringResource(Res.string.action_saving)
                                } else {
                                    stringResource(Res.string.vehicle_detail_save_changes)
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(message = stringResource(Res.string.vehicle_detail_loading))
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
                        Text(stringResource(Res.string.action_saving))
                    }
                }
            }
        }

        // Uploading document overlay
        if (state.isUploading) {
            val uploadingDocName = state.selectedDocumentTypeName ?: stringResource(Res.string.vehicle_detail_document)
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
                        Text(stringResource(Res.string.vehicle_detail_uploading, uploadingDocName))
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
    val fleetTabs = tabs.map { tab ->
        FleetTab(
            id = tab,
            label = "${tab.icon} ${tab.titleText()}"
        )
    }
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

        FleetTabBar(
            tabs = fleetTabs,
            selectedTabId = tabs[pagerState.currentPage],
            onTabSelected = { tab ->
                scope.launch {
                    pagerState.animateScrollToPage(tabs.indexOf(tab))
                }
            }
        )

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
