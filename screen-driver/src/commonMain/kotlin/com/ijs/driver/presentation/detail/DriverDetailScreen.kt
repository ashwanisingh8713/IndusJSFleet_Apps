package com.ijs.driver.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetConfirmationDialog
import com.indusjs.uicomponents.components.FleetTab
import com.indusjs.uicomponents.components.FleetTabBar
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.HistoryTabContent
import com.indusjs.uicomponents.components.StateChangeDialog
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.driver.presentation.driverStatusLabel
import com.ijs.driver.presentation.driverStatusLabelsByApi
import com.ijs.driver.presentation.getDriverStateOptions
import com.indusjs.pdfreport.handler.DriverCostsPdfHandler
import com.indusjs.pdfreport.model.DriverCostsPdfData
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Driver Detail Screen composable with Edit functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailScreen(
    viewModel: DriverDetailViewModel,
    driverId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToAddDriverCost: (String) -> Unit = {},
    onNavigateToTripDetail: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resourceLabels = driverStatusLabelsByApi()
    // Merge: DB-cached labels take priority over resource strings
    val driverStatusLabels = remember(resourceLabels, state.stateLabels) {
        resourceLabels + state.stateLabels
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<DriverCostsPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Load driver on first composition
    LaunchedEffect(driverId) {
        viewModel.sendIntent(DriverDetailContract.Intent.LoadDriver(driverId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverDetailContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is DriverDetailContract.Effect.ShowError -> {
                    pendingSnackbar = effect.message
                }
                is DriverDetailContract.Effect.NavigateBack -> onNavigateBack()
                is DriverDetailContract.Effect.ShowDeleteConfirmation -> {
                    showDeleteDialog = true
                }
                is DriverDetailContract.Effect.DriverDeleted -> {
                    // Already navigating back
                }
                is DriverDetailContract.Effect.DriverUpdated -> {
                    // Refresh handled in ViewModel
                }
                is DriverDetailContract.Effect.StateUpdated -> {
                    // State updated - handled by ViewModel
                }
                is DriverDetailContract.Effect.NavigateToAddDriverCost -> {
                    onNavigateToAddDriverCost(effect.driverId)
                }
                is DriverDetailContract.Effect.NavigateToTripDetail -> {
                    onNavigateToTripDetail(effect.tripId)
                }
                is DriverDetailContract.Effect.ExportPdf -> {
                    // Trigger PDF export by setting the data
                    isExportingPdf = true
                    pdfExportData = effect.pdfData
                }
            }
        }
    }

    // PDF Export Handler
    DriverCostsPdfHandler(
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

    // Delete confirmation dialog (standardized; named message + destructive action)
    FleetConfirmationDialog(
        showDialog = showDeleteDialog,
        title = stringResource(Res.string.driver_detail_delete),
        message = stringResource(
            Res.string.driver_delete_confirmation_named,
            state.driver?.fullName.orEmpty()
        ),
        confirmText = stringResource(Res.string.delete),
        isDestructive = true,
        onConfirm = {
            showDeleteDialog = false
            viewModel.sendIntent(DriverDetailContract.Intent.ConfirmDelete)
        },
        onDismiss = { showDeleteDialog = false }
    )

    // Status change dialog - use new StateChangeDialog
    if (showStatusDialog && state.driver != null) {
        StateChangeDialog(
            title = stringResource(Res.string.driver_change_status_title),
            currentStateLabel = driverStatusLabel(state.driver!!.status, driverStatusLabels),
            stateOptions = getDriverStateOptions(state.driver!!.status, state.stateLabels).map { opt ->
                // Prefer DB labels (via stateLabels), then localized resource labels, then original
                opt.copy(label = driverStatusLabels[opt.value] ?: opt.label)
            },
            onStateSelected = { newStatus ->
                showStatusDialog = false
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateDriverState(newStatus))
            },
            onDismiss = { showStatusDialog = false },
            isLoading = state.isUpdatingState
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) stringResource(Res.string.driver_detail_edit_title)
                        else stringResource(Res.string.drivers_detail)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.isEditMode) {
                                viewModel.sendIntent(DriverDetailContract.Intent.ExitEditMode)
                            } else {
                                viewModel.sendIntent(DriverDetailContract.Intent.NavigateBack)
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
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                actions = {
                    if (!state.isEditMode && state.driver != null) {
                        IconButton(onClick = { viewModel.sendIntent(DriverDetailContract.Intent.EnterEditMode) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = stringResource(Res.string.edit),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                    shadowElevation = FleetTokens.Elevation.Dialog
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(FleetTokens.Spacing.L),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        FleetButton(
                            text = stringResource(Res.string.cancel),
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.ExitEditMode) },
                            variant = ButtonVariant.SECONDARY,
                            modifier = Modifier.weight(1f)
                        )

                        FleetButton(
                            text = if (state.isSaving) stringResource(Res.string.action_saving)
                            else stringResource(Res.string.driver_save_changes),
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.SaveChanges) },
                            variant = ButtonVariant.PRIMARY,
                            enabled = state.canSave,
                            isLoading = state.isSaving,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(message = stringResource(Res.string.driver_loading_details))
            }
            state.error != null && state.driver == null -> {
                ErrorContent(
                    error = state.error?.resolve() ?: stringResource(Res.string.error_generic),
                    screenContext = FleetErrorContext.DRIVER_DETAIL,
                    onRetry = { viewModel.sendIntent(DriverDetailContract.Intent.Refresh) }
                )
            }
            state.driver != null -> {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val isWide = rememberFleetBreakpoint() != FleetBreakpoint.Compact
                    // On wide screens cap the content width and centre it; on
                    // Compact fill the whole width.
                    val cappedModifier = if (isWide) {
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = FleetTokens.Width.MaxContent)
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    } else {
                        Modifier.fillMaxSize()
                    }
                    if (state.isEditMode) {
                        // Edit Mode - centered, width-capped form on wide screens
                        LazyColumn(
                            modifier = cappedModifier,
                            contentPadding = PaddingValues(FleetTokens.Spacing.L),
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                        ) {
                            item { EditModeContent(state, viewModel) }
                            item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL)) }
                        }
                    } else {
                        // View Mode with Tabs - capped width on wide screens
                        Column(modifier = cappedModifier) {
                            DriverDetailTabs(
                                state = state,
                                viewModel = viewModel,
                                driverStatusLabels = driverStatusLabels,
                                onStatusClick = { showStatusDialog = true }
                            )
                        }
                    }
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
                    shape = RoundedCornerShape(FleetTokens.Radius.XL)
                ) {
                    Column(
                        modifier = Modifier.padding(FleetTokens.Spacing.XL),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                        Text(stringResource(Res.string.action_saving))
                    }
                }
            }
        }

        // PDF Export progress indicator
        if (isExportingPdf) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(FleetTokens.Radius.XL)
                ) {
                    Column(
                        modifier = Modifier.padding(FleetTokens.Spacing.XL),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                        Text(stringResource(Res.string.action_exporting_pdf))
                    }
                }
            }
        }
    }
}

/**
 * Tab definitions for Driver Detail Screen
 */
private enum class DriverDetailTab(val title: StringResource) {
    OVERVIEW(Res.string.driver_tab_overview),
    COSTS(Res.string.driver_tab_costs),
    HISTORY(Res.string.driver_tab_history)
}

/**
 * Tabbed layout for Driver Detail
 */
@Composable
private fun DriverDetailTabs(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel,
    driverStatusLabels: Map<String, String>,
    onStatusClick: () -> Unit = {}
) {
    // Filter out Costs tab if user doesn't have permission
    val tabs = if (state.canViewCosts) {
        DriverDetailTab.entries
    } else {
        DriverDetailTab.entries.filter { it != DriverDetailTab.COSTS }
    }
    var selectedTab by remember { mutableStateOf(DriverDetailTab.OVERVIEW.ordinal) }

    LaunchedEffect(state.canViewCosts) {
        if (!state.canViewCosts && selectedTab == DriverDetailTab.COSTS.ordinal) {
            selectedTab = DriverDetailTab.OVERVIEW.ordinal
        }
    }

    // Load history when switching to history tab (indices: 0 Overview, 1 Costs, 2 History)
    LaunchedEffect(selectedTab) {
        viewModel.sendIntent(DriverDetailContract.Intent.SelectTab(selectedTab))
    }

    val fleetTabs = tabs.map { tab ->
        FleetTab(id = tab.ordinal, label = stringResource(tab.title))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FleetTabBar(
            tabs = fleetTabs,
            selectedTabId = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        val activeTab = DriverDetailTab.entries.find { it.ordinal == selectedTab } ?: DriverDetailTab.OVERVIEW
        when (activeTab) {
            DriverDetailTab.OVERVIEW -> DriverOverviewContent(
                state = state,
                viewModel = viewModel,
                driverStatusLabels = driverStatusLabels,
                onStatusClick = onStatusClick
            )
            DriverDetailTab.COSTS -> DriverCostsTabContent(
                state = state,
                viewModel = viewModel
            )
            DriverDetailTab.HISTORY -> HistoryTabContent(
                items = state.historyItems,
                isLoading = state.isLoadingHistory,
                error = state.historyError?.resolve(),
                hasMore = state.hasMoreHistory,
                onLoadMore = { viewModel.sendIntent(DriverDetailContract.Intent.LoadMoreHistory) },
                onRetry = { viewModel.sendIntent(DriverDetailContract.Intent.LoadHistory) }
            )
        }
    }
}

