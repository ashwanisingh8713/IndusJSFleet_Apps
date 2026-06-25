package com.ijs.trip.payment.presentation


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.handler.PaymentsListPdfHandler
import com.indusjs.pdfreport.model.PaymentsListPdfData
import com.indusjs.pdfreport.model.PaymentListItem
import com.indusjs.uicomponents.components.DeleteConfirmationDialog
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.trip.payment.domain.entity.*
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Data class for grouped payments by Trip.
 */
internal data class TripPaymentGroup(
    val tripId: String,
    val tripInfo: TripPaymentTripInfo?,
    val payments: List<TripPayment>,
    val totalAmount: Double,
    val paymentCount: Int,
    val customerName: String?
)

/**
 * Payments List Screen.
 * Shows all trip payments with filters and summary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: PaymentsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddPayment: (String?) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val notApplicableLabel = stringResource(Res.string.label_not_applicable)
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // PDF Export state
    var pdfExportData by remember { mutableStateOf<PaymentsListPdfData?>(null) }
    var isExportingPdf by remember { mutableStateOf(false) }

    // Track expanded trips - default all collapsed
    var expandedTrips by remember { mutableStateOf(setOf<String>()) }

    // View mode toggle - grouped vs flat
    var isGroupedView by remember { mutableStateOf(true) }

    // Dropdown menu state
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Group payments by tripId
    val groupedPayments = remember(state.payments) {
        state.payments.groupBy { it.tripId }
            .map { (tripId, payments) ->
                TripPaymentGroup(
                    tripId = tripId,
                    tripInfo = payments.firstOrNull()?.tripInfo,
                    payments = payments.sortedByDescending { it.paymentDate },
                    totalAmount = payments.sumOf { it.amount },
                    paymentCount = payments.size,
                    customerName = payments.firstOrNull()?.customerName
                        ?: payments.firstOrNull()?.customerCompany
                )
            }
            .sortedByDescending { it.payments.firstOrNull()?.paymentDate }
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is PaymentsContract.Effect.NavigateToDetail -> onNavigateToDetail(effect.paymentId)
                is PaymentsContract.Effect.NavigateToAddPayment -> onNavigateToAddPayment(null)
                is PaymentsContract.Effect.NavigateToAddPaymentForTrip -> onNavigateToAddPayment(effect.tripId)
                is PaymentsContract.Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is PaymentsContract.Effect.ShowError -> pendingSnackbar = effect.message
                is PaymentsContract.Effect.PaymentDeleted -> { /* Handled in list update */ }
                is PaymentsContract.Effect.PdfExportStarted -> {
                    isExportingPdf = true
                }
                is PaymentsContract.Effect.PdfExportCompleted -> {
                    isExportingPdf = false
                }
            }
        }
    }

    // PDF Export Handler
    PaymentsListPdfHandler(
        pdfData = pdfExportData,
        onExportComplete = {
            isExportingPdf = false
            pdfExportData = null
            // No snackbar needed - dialog already shows success
        },
        onExportError = { error ->
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar(error)
            }
        }
    )

    // Load more when reaching end of list
    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= totalItems - 3 && state.hasMore && !state.isLoadingMore
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) {
                viewModel.sendIntent(PaymentsContract.Intent.LoadMore)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.payments_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    // View toggle button - switch between grouped and flat view
                    IconButton(onClick = { isGroupedView = !isGroupedView }) {
                        // Use menu icon for grouped, dashboard for flat
                        Icon(
                            painter = painterResource(
                                if (isGroupedView) Res.drawable.ic_menu else Res.drawable.ic_dashboard
                            ),
                            contentDescription = if (isGroupedView) {
                                stringResource(Res.string.payment_cd_switch_flat)
                            } else {
                                stringResource(Res.string.payment_cd_switch_grouped)
                            },
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    }

                    // Options menu
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert),
                                contentDescription = stringResource(Res.string.payment_more_options),
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            // Export PDF
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export_pdf)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_download),
                                        contentDescription = null,
                                        modifier = Modifier.size(FleetTokens.IconSize.M)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    val dateRange = if (state.filter.startDate != null || state.filter.endDate != null) {
                                        "${state.filter.startDate ?: ""} - ${state.filter.endDate ?: ""}"
                                    } else null
                                    val filterInfo = listOfNotNull(
                                        state.selectedCustomerName,
                                        state.filter.paymentType?.displayName,
                                        state.filter.paymentStatus?.let { status ->
                                            state.paymentStateLabels[status.apiValue] ?: status.displayName
                                        }
                                    ).joinToString(", ").ifEmpty { null }

                                    pdfExportData = PaymentsListPdfData(
                                        payments = state.payments.map { payment ->
                                            PaymentListItem(
                                                paymentId = payment.id.toIntOrNull() ?: 0,
                                                tripId = payment.tripId.toIntOrNull() ?: 0,
                                                vehicleNumber = payment.tripInfo?.vehicleRegistration ?: notApplicableLabel,
                                                customerName = payment.customerName ?: notApplicableLabel,
                                                amount = payment.amount,
                                                paymentType = payment.typeDisplay,
                                                paymentMode = payment.modeDisplay,
                                                paymentDate = com.indusjs.fleet.core.util.formatDateToHumanReadable(payment.paymentDate),
                                                paymentStatus = state.paymentStateLabels[payment.paymentStatus.apiValue] ?: payment.paymentStatus.displayName,
                                                receiptNumber = payment.receiptNumber,
                                                startLocation = payment.tripInfo?.startLocation,
                                                endLocation = payment.tripInfo?.endLocation
                                            )
                                        },
                                        totalPayments = state.payments.size,
                                        totalReceived = state.payments.filter { it.paymentStatus == PaymentStatus.RECEIVED }.sumOf { it.amount },
                                        totalPending = state.pendingSummary?.totalPending ?: 0.0,
                                        dateRange = dateRange,
                                        filterInfo = filterInfo,
                                        generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
                                    )
                                },
                                enabled = state.payments.isNotEmpty() && !isExportingPdf
                            )

                            // Filter
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                                    ) {
                                        Text(stringResource(Res.string.payment_menu_filter))
                                        if (state.hasFilters) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(FleetTokens.Spacing.S)
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_filter),
                                        contentDescription = null,
                                        modifier = Modifier.size(FleetTokens.IconSize.M)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.sendIntent(PaymentsContract.Intent.ShowFilterSheet)
                                }
                            )

                            // Refresh
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.refresh)) },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_refresh),
                                        contentDescription = null,
                                        modifier = Modifier.size(FleetTokens.IconSize.M)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.sendIntent(PaymentsContract.Intent.Refresh)
                                }
                            )

                            // Expand All / Collapse All (only in grouped view)
                            if (isGroupedView && groupedPayments.isNotEmpty()) {
                                HorizontalDivider()
                                val allExpanded = expandedTrips.size == groupedPayments.size
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (allExpanded) {
                                                stringResource(Res.string.payment_collapse_all)
                                            } else {
                                                stringResource(Res.string.payment_expand_all)
                                            }
                                        )
                                    },
                                    leadingIcon = {
                                        Text(
                                            text = if (allExpanded) "▲" else "▼",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        expandedTrips = if (allExpanded) {
                                            emptySet()
                                        } else {
                                            groupedPayments.map { it.tripId }.toSet()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // Only show FAB when there's content (not empty and not loading)
            if (!state.isLoading && state.payments.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToAddPayment) }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.payments_add)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(PaymentsContract.Intent.Refresh) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
            when {
                state.isLoading && state.payments.isEmpty() -> {
                    LoadingContent()
                }
                state.isEmpty && state.error == null -> {
                    // No payments - differentiate between filtered empty and actual empty
                    if (state.hasFilters) {
                        // Filtered results are empty - show Clear Filters instead of Add Payment
                        EmptyFilteredContent(
                            onClearFilters = { viewModel.sendIntent(PaymentsContract.Intent.ResetFilter) }
                        )
                    } else {
                        // No payments exist - show empty state with Add Payment
                        EmptyContent(
                            title = stringResource(Res.string.payments_empty_title),
                            message = stringResource(Res.string.payments_empty_message),
                            actionLabel = stringResource(Res.string.payments_add),
                            onAction = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToAddPayment) }
                        )
                    }
                }
                state.error != null && state.payments.isEmpty() -> {
                    // Actual error occurred - show error content
                    PaymentsErrorContent(
                        error = state.error?.resolve() ?: stringResource(Res.string.finance_error_generic),
                        onRetry = { viewModel.sendIntent(PaymentsContract.Intent.LoadPayments) },
                        onAddPayment = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToAddPayment) }
                    )
                }
                else -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val breakpoint = rememberFleetBreakpoint()
                        // Compact stays 1-up; Medium/Expanded show two cards per row. On
                        // Expanded the list is capped to a readable width and centered.
                        val columns = if (breakpoint.isAtLeastMedium) 2 else 1
                        val contentWidthModifier = if (breakpoint.isExpanded) {
                            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
                        } else {
                            Modifier.fillMaxWidth()
                        }

                        LazyColumn(
                            state = listState,
                            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
                            contentPadding = PaddingValues(
                                start = FleetTokens.Spacing.L,
                                end = FleetTokens.Spacing.L,
                                top = FleetTokens.Spacing.L,
                                // Extra bottom padding to prevent FAB overlap.
                                bottom = FleetTokens.Spacing.XXXL + FleetTokens.Spacing.XXL
                            ),
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                        ) {
                            // Summary Card with trip count for grouped view
                            item {
                                PaymentSummaryCard(
                                    totalReceived = state.totalReceived,
                                    totalPending = state.totalPending,
                                    thisMonth = state.thisMonth,
                                    paymentCount = state.payments.size,
                                    tripCount = if (isGroupedView) groupedPayments.size else 0,
                                    isGroupedView = isGroupedView
                                )
                            }

                            // Filter indicator
                            if (state.hasFilters) {
                                item {
                                    FilterChipRow(
                                        filter = state.filter,
                                        onClear = { viewModel.sendIntent(PaymentsContract.Intent.ResetFilter) },
                                        customerName = state.selectedCustomerName,
                                        paymentStateLabels = state.paymentStateLabels
                                    )
                                }
                            }

                            // Payment items - grouped or flat view
                            if (isGroupedView) {
                                // Grouped by Trip with collapsible sections
                                if (columns == 1) {
                                    items(
                                        items = groupedPayments,
                                        key = { it.tripId }
                                    ) { group ->
                                        CollapsibleTripGroupCard(
                                            group = group,
                                            isExpanded = expandedTrips.contains(group.tripId),
                                            onToggleExpand = {
                                                expandedTrips = if (expandedTrips.contains(group.tripId)) {
                                                    expandedTrips - group.tripId
                                                } else {
                                                    expandedTrips + group.tripId
                                                }
                                            },
                                            onPaymentClick = { paymentId ->
                                                viewModel.sendIntent(PaymentsContract.Intent.NavigateToPaymentDetail(paymentId))
                                            },
                                            paymentStateLabels = state.paymentStateLabels
                                        )
                                    }
                                } else {
                                    val rows = groupedPayments.chunked(columns)
                                    items(rows, key = { row -> row.first().tripId }) { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                                        ) {
                                            row.forEach { group ->
                                                CollapsibleTripGroupCard(
                                                    group = group,
                                                    isExpanded = expandedTrips.contains(group.tripId),
                                                    onToggleExpand = {
                                                        expandedTrips = if (expandedTrips.contains(group.tripId)) {
                                                            expandedTrips - group.tripId
                                                        } else {
                                                            expandedTrips + group.tripId
                                                        }
                                                    },
                                                    onPaymentClick = { paymentId ->
                                                        viewModel.sendIntent(PaymentsContract.Intent.NavigateToPaymentDetail(paymentId))
                                                    },
                                                    paymentStateLabels = state.paymentStateLabels,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (row.size < columns) {
                                                repeat(columns - row.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Flat list view
                                if (columns == 1) {
                                    items(
                                        items = state.payments,
                                        key = { it.id }
                                    ) { payment ->
                                        PaymentCard(
                                            payment = payment,
                                            onClick = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToPaymentDetail(payment.id)) },
                                            onLongClick = { viewModel.sendIntent(PaymentsContract.Intent.ShowDeleteConfirmation(payment)) },
                                            paymentStateLabels = state.paymentStateLabels
                                        )
                                    }
                                } else {
                                    val rows = state.payments.chunked(columns)
                                    items(rows, key = { row -> row.first().id }) { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                                        ) {
                                            row.forEach { payment ->
                                                PaymentCard(
                                                    payment = payment,
                                                    onClick = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToPaymentDetail(payment.id)) },
                                                    onLongClick = { viewModel.sendIntent(PaymentsContract.Intent.ShowDeleteConfirmation(payment)) },
                                                    paymentStateLabels = state.paymentStateLabels,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            if (row.size < columns) {
                                                repeat(columns - row.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Loading more indicator
                            if (state.isLoadingMore) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(FleetTokens.Spacing.L),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.Default))
                                    }
                                }
                            }
                        }

                        // Pull to refresh indicator
                        if (state.isRefreshing) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // Filter Bottom Sheet
    if (state.showFilterSheet) {
        PaymentFilterBottomSheet(
            filter = state.tempFilter,
            customers = state.customers,
            onDismiss = { viewModel.sendIntent(PaymentsContract.Intent.HideFilterSheet) },
            onUpdateCustomer = { viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterCustomer(it)) },
            onUpdateType = { viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterType(it)) },
            onUpdateStatus = { viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterStatus(it)) },
            onUpdateDateRange = { start: String?, end: String? ->
                viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterDateRange(start, end))
            },
            onApply = { viewModel.sendIntent(PaymentsContract.Intent.ApplyFilter) },
            onReset = { viewModel.sendIntent(PaymentsContract.Intent.ResetFilter) },
            paymentStateLabels = state.paymentStateLabels
        )
    }

    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = state.showDeleteConfirmation,
        entityName = stringResource(Res.string.payment_entity_singular),
        entityDetail = state.paymentToDelete?.amountDisplay,
        isLoading = state.isDeleting,
        onConfirmDelete = { viewModel.sendIntent(PaymentsContract.Intent.ConfirmDelete) },
        onDismiss = { viewModel.sendIntent(PaymentsContract.Intent.HideDeleteConfirmation) }
    )
}

