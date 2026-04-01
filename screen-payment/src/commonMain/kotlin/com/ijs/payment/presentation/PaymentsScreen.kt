package com.ijs.payment.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.pdfreport.handler.PaymentsListPdfHandler
import com.indusjs.pdfreport.model.PaymentsListPdfData
import com.indusjs.pdfreport.model.PaymentListItem
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.payment.domain.entity.*
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
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
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentsContract.Effect.NavigateToDetail -> onNavigateToDetail(effect.paymentId)
                is PaymentsContract.Effect.NavigateToAddPayment -> onNavigateToAddPayment(null)
                is PaymentsContract.Effect.NavigateToAddPaymentForTrip -> onNavigateToAddPayment(effect.tripId)
                is PaymentsContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is PaymentsContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
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
                            contentDescription = if (isGroupedView) "Switch to Flat View" else "Switch to Grouped View",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Options menu
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert),
                                contentDescription = "More options",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            // Export PDF
                            DropdownMenuItem(
                                text = { Text("Export PDF") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_download),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    val dateRange = if (state.filter.startDate != null || state.filter.endDate != null) {
                                        "${state.filter.startDate ?: ""} - ${state.filter.endDate ?: ""}"
                                    } else null
                                    val filterInfo = listOfNotNull(
                                        state.filter.paymentType?.displayName,
                                        state.filter.paymentMode?.displayName,
                                        state.filter.paymentStatus?.displayName
                                    ).joinToString(", ").ifEmpty { null }

                                    pdfExportData = PaymentsListPdfData(
                                        payments = state.payments.map { payment ->
                                            PaymentListItem(
                                                paymentId = payment.id.toIntOrNull() ?: 0,
                                                tripId = payment.tripId.toIntOrNull() ?: 0,
                                                vehicleNumber = payment.tripInfo?.vehicleRegistration ?: "N/A",
                                                customerName = payment.customerName ?: "N/A",
                                                amount = payment.amount,
                                                paymentType = payment.typeDisplay,
                                                paymentMode = payment.modeDisplay,
                                                paymentDate = payment.paymentDate?.take(10) ?: "",
                                                paymentStatus = payment.paymentStatus.displayName,
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
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Filter")
                                        if (state.hasFilters) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_filter),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    viewModel.sendIntent(PaymentsContract.Intent.ShowFilterSheet)
                                }
                            )

                            // Refresh
                            DropdownMenuItem(
                                text = { Text("Refresh") },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_refresh),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
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
                                    text = { Text(if (allExpanded) "Collapse All" else "Expand All") },
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
                        contentDescription = "Add Payment"
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
                            title = "No Payments Yet",
                            message = "Record your first payment to get started",
                            actionLabel = "Add Payment",
                            onAction = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToAddPayment) }
                        )
                    }
                }
                state.error != null && state.payments.isEmpty() -> {
                    // Actual error occurred - show error content
                    PaymentsErrorContent(
                        error = state.error ?: "Something went wrong",
                        onRetry = { viewModel.sendIntent(PaymentsContract.Intent.LoadPayments) },
                        onAddPayment = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToAddPayment) }
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 88.dp  // Extra padding to prevent FAB overlap
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    onClear = { viewModel.sendIntent(PaymentsContract.Intent.ResetFilter) }
                                )
                            }
                        }

                        // Payment items - grouped or flat view
                        if (isGroupedView) {
                            // Grouped by Trip with collapsible sections
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
                                    }
                                )
                            }
                        } else {
                            // Flat list view
                            items(
                                items = state.payments,
                                key = { it.id }
                            ) { payment ->
                                PaymentCard(
                                    payment = payment,
                                    onClick = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToPaymentDetail(payment.id)) },
                                    onLongClick = { viewModel.sendIntent(PaymentsContract.Intent.ShowDeleteConfirmation(payment)) }
                                )
                            }
                        }

                        // Loading more indicator
                        if (state.isLoadingMore) {
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

    // Filter Bottom Sheet
    if (state.showFilterSheet) {
        PaymentFilterBottomSheet(
            filter = state.tempFilter,
            onDismiss = { viewModel.sendIntent(PaymentsContract.Intent.HideFilterSheet) },
            onUpdateType = { viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterType(it)) },
            onUpdateMode = { viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterMode(it)) },
            onUpdateStatus = { viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterStatus(it)) },
            onUpdateDateRange = { start: String?, end: String? ->
                viewModel.sendIntent(PaymentsContract.Intent.UpdateTempFilterDateRange(start, end))
            },
            onApply = { viewModel.sendIntent(PaymentsContract.Intent.ApplyFilter) },
            onReset = { viewModel.sendIntent(PaymentsContract.Intent.ResetFilter) }
        )
    }

    // Delete Confirmation Dialog
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(PaymentsContract.Intent.HideDeleteConfirmation) },
            title = { Text("Delete Payment") },
            text = {
                Text("Are you sure you want to delete this payment of ${state.paymentToDelete?.amountDisplay}?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(PaymentsContract.Intent.ConfirmDelete) },
                    enabled = !state.isDeleting
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(PaymentsContract.Intent.HideDeleteConfirmation) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

