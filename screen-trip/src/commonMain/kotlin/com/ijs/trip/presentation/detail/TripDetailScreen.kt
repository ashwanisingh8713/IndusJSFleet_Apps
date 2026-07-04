package com.ijs.trip.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.pdfreport.handler.TripCostsPdfHandler
import com.indusjs.pdfreport.model.TripCostsPdfData
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetConfirmationDialog
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.StateChangeDialog
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.trip.presentation.getTripStateOptions
import com.ijs.customer.presentation.toSelectableCustomerList
import com.indusjs.uicomponents.customer.CustomerSelectionBottomSheet
import com.ijs.trip.domain.entity.TripStatus
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trip Detail Screen composable with Edit functionality.
 *
 * This is the main orchestrator screen. View-mode sections are in [TripDetailViewContent.kt],
 * edit-mode sections are in [TripDetailEditContent.kt] and [TripDetailEditForms.kt],
 * reusable components are in [TripDetailComponents.kt],
 * and costs display is in [TripCostsContent.kt].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    viewModel: TripDetailViewModel,
    tripId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToAddTripCost: (tripId: String, vehicleId: String) -> Unit = { _, _ -> },
    onNavigateToAddPayment: (tripId: String, vehicleId: String) -> Unit = { _, _ -> },
    onNavigateToPaymentDetail: (paymentId: String) -> Unit = {},
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

    // Refresh trip detail when screen is resumed (e.g., when returning from Add Payment or Edit Payments)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.sendIntent(TripDetailContract.Intent.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load trip on first composition
    LaunchedEffect(tripId) {
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
                is TripDetailContract.Effect.TripCancelled -> { }
                is TripDetailContract.Effect.TripUpdated -> { }
                is TripDetailContract.Effect.ExportPdf -> {
                    isExportingPdf = true
                    pdfExportData = effect.pdfData
                }
                is TripDetailContract.Effect.StateUpdated -> { }
                is TripDetailContract.Effect.NavigateToAddTripCost -> {
                    onNavigateToAddTripCost(effect.tripId, effect.vehicleId)
                }
                is TripDetailContract.Effect.NavigateToAddPayment -> {
                    onNavigateToAddPayment(effect.tripId, effect.vehicleId)
                }
                is TripDetailContract.Effect.NavigateToPaymentDetail -> {
                    onNavigateToPaymentDetail(effect.paymentId)
                }
                is TripDetailContract.Effect.NavigateToAddCustomer -> {
                    onNavigateToAddCustomer()
                }
            }
        }
    }

    // Cancel confirmation dialog
    FleetConfirmationDialog(
        showDialog = showCancelDialog,
        title = stringResource(Res.string.trip_detail_cancel),
        message = stringResource(Res.string.trip_detail_cancel_confirm),
        confirmText = stringResource(Res.string.trip_detail_cancel),
        dismissText = stringResource(Res.string.trip_detail_keep),
        isDestructive = true,
        onConfirm = {
            showCancelDialog = false
            viewModel.sendIntent(TripDetailContract.Intent.ConfirmCancel)
        },
        onDismiss = { showCancelDialog = false }
    )

    // Status change dialog
    if (showStatusDialog && state.trip != null) {
        StateChangeDialog(
            title = stringResource(Res.string.trip_detail_change_status),
            currentStateLabel = state.stateLabels[com.ijs.trip.domain.entity.TripStatus.toApiString(state.trip!!.status)]
                ?: TripStatus.getDisplayLabel(state.trip!!.status),
            stateOptions = getTripStateOptions(state.trip!!.status, state.stateLabels),
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
            TripDetailTopBar(
                isEditMode = state.isEditMode,
                showEditButton = state.showEditButton,
                hasTrip = state.trip != null,
                onNavigateBack = {
                    if (state.isEditMode) {
                        viewModel.sendIntent(TripDetailContract.Intent.ExitEditMode)
                    } else {
                        viewModel.sendIntent(TripDetailContract.Intent.NavigateBack)
                    }
                },
                onEditClick = { viewModel.sendIntent(TripDetailContract.Intent.EnterEditMode) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.isEditMode) {
                EditModeBottomBar(
                    isSaving = state.isSaving,
                    canSave = state.canSave,
                    onCancel = { viewModel.sendIntent(TripDetailContract.Intent.ExitEditMode) },
                    onSave = { viewModel.sendIntent(TripDetailContract.Intent.SaveChanges) }
                )
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(message = stringResource(Res.string.trip_detail_loading_trip))
            }
            state.error != null && state.trip == null -> {
                ErrorContent(
                    error = state.error!!,
                    screenContext = FleetErrorContext.TRIP_DETAIL,
                    onRetry = { viewModel.sendIntent(TripDetailContract.Intent.Refresh) }
                )
            }
            state.trip != null -> {
                TripDetailContent(
                    state = state,
                    viewModel = viewModel,
                    padding = padding,
                    onShowStatusDialog = { showStatusDialog = true }
                )
            }
        }

        // Saving overlay
        if (state.isSaving) {
            ProgressOverlay(
                padding = padding,
                title = stringResource(Res.string.action_saving),
                subtitle = stringResource(Res.string.trip_detail_please_wait)
            )
        }

        // PDF Export overlay
        if (isExportingPdf) {
            ProgressOverlay(
                padding = padding,
                title = stringResource(Res.string.action_generating_pdf),
                subtitle = stringResource(Res.string.trip_detail_pdf_preparing)
            )
        }
    }

    // Customer Selection Bottom Sheet for Edit Mode
    if (state.showCustomerBottomSheet) {
        CustomerSelectionBottomSheet(
            customers = state.customers.toSelectableCustomerList(),
            searchQuery = state.customerSearchQuery,
            isLoading = state.isLoadingCustomers,
            onDismiss = { viewModel.sendIntent(TripDetailContract.Intent.ToggleCustomerBottomSheet) },
            onSearchQueryChange = { viewModel.sendIntent(TripDetailContract.Intent.UpdateCustomerSearchQuery(it)) },
            onCustomerSelect = { selectable ->
                val customer = state.customers.find { it.id == selectable.id }
                if (customer != null) viewModel.sendIntent(TripDetailContract.Intent.SelectCustomer(customer))
            },
            onAddNewCustomer = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddCustomer) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripDetailTopBar(
    isEditMode: Boolean,
    showEditButton: Boolean,
    hasTrip: Boolean,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                if (isEditMode) stringResource(Res.string.trip_detail_edit_title)
                else stringResource(Res.string.trips_detail)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(if (isEditMode) Res.drawable.ic_close else Res.drawable.ic_arrow_back),
                    contentDescription = if (isEditMode) stringResource(Res.string.cancel)
                    else stringResource(Res.string.back),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            }
        },
        actions = {
            if (!isEditMode && hasTrip && showEditButton) {
                IconButton(onClick = onEditClick) {
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
}

@Composable
private fun EditModeBottomBar(
    isSaving: Boolean,
    canSave: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = FleetTokens.Elevation.Dialog,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FleetTokens.Spacing.L),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            FleetButton(
                text = stringResource(Res.string.cancel),
                onClick = onCancel,
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
            )

            FleetButton(
                text = if (isSaving) stringResource(Res.string.action_saving)
                else stringResource(Res.string.team_save_changes),
                onClick = onSave,
                variant = ButtonVariant.PRIMARY,
                enabled = canSave,
                isLoading = isSaving,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TripDetailContent(
    state: TripDetailContract.State,
    viewModel: TripDetailViewModel,
    padding: PaddingValues,
    onShowStatusDialog: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(padding)
    ) {
        val bp = rememberFleetBreakpoint()
        // Detail/forms are centered and width-capped on wider screens.
        val contentWidthModifier = if (bp == FleetBreakpoint.Compact) {
            Modifier.fillMaxSize()
        } else {
            Modifier
                .fillMaxSize()
                .widthIn(max = FleetTokens.Width.MaxContent)
                .align(Alignment.TopCenter)
        }

        LazyColumn(
            modifier = contentWidthModifier,
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
        if (state.isEditMode) {
            item { EditModeContent(state, viewModel) }
        } else {
            item {
                TripHeader(
                    trip = state.trip!!,
                    canViewTripPrice = state.canViewTripPrice,
                    onStatusClick = onShowStatusDialog,
                    stateLabels = state.stateLabels
                )
            }

            item { RouteAndScheduleSection(trip = state.trip!!) }

            if (state.trip!!.actualStartTime != null || state.trip!!.actualEndTime != null) {
                item { ActualTimesSection(trip = state.trip!!) }
            }

            item { CargoSection(trip = state.trip!!, canViewTripPrice = state.canViewTripPrice) }

            item { AdditionalInfoSection(trip = state.trip!!) }

            item {
                TripCostsSection(
                    costs = state.costs,
                    totalCost = state.totalCost,
                    costsByType = state.costsByType,
                    isLoading = state.isLoadingCosts,
                    costsError = state.costsError,
                    onRetryCosts = { viewModel.sendIntent(TripDetailContract.Intent.RetryLoadCosts) },
                    onExportPdf = if (state.hasCosts) {
                        { viewModel.sendIntent(TripDetailContract.Intent.ExportCostsToPdf) }
                    } else null,
                    onAddTripCost = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddTripCost) }
                )
            }

            // Payments section - only for Owner and General Manager
            if (state.canViewTripPrice) {
                item {
                    TripPaymentsSection(
                        payments = state.payments,
                        totalPaid = state.totalPaid,
                        pendingAmount = state.pendingAmount,
                        tripPrice = state.trip?.tripPrice,
                        isLoading = state.isLoadingPayments,
                        onAddPayment = { viewModel.sendIntent(TripDetailContract.Intent.NavigateToAddPayment) },
                        onPaymentClick = { paymentId ->
                            viewModel.sendIntent(TripDetailContract.Intent.NavigateToPaymentDetail(paymentId))
                        },
                        paymentStateLabels = state.paymentStateLabels
                    )
                }
            }

            // Cancel button for planned trips
            if (state.trip?.status == TripStatus.PLANNED) {
                item {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    FleetButton(
                        text = stringResource(Res.string.trip_detail_cancel),
                        onClick = { viewModel.sendIntent(TripDetailContract.Intent.CancelTrip) },
                        variant = ButtonVariant.DESTRUCTIVE,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = null,
                                modifier = Modifier.size(FleetTokens.IconSize.S)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL)) }
        }
    }
}

@Composable
private fun ProgressOverlay(
    padding: PaddingValues,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(FleetTokens.Radius.XXL),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = FleetTokens.Elevation.Dialog)
        ) {
            Column(
                modifier = Modifier.padding(FleetTokens.Spacing.XXL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(FleetTokens.IconSize.XL),
                    strokeWidth = FleetTokens.Spacing.XS
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
