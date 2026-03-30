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
private data class TripPaymentGroup(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterChipRow(
    filter: TripPaymentFilter,
    onClear: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "🔍 Filters:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 4.dp)
        )

        filter.paymentType?.let { type ->
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }

        filter.paymentMode?.let { mode ->
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(mode.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }

        filter.paymentStatus?.let { status ->
            FilterChip(
                selected = true,
                onClick = { },
                label = { Text(status.displayName, style = MaterialTheme.typography.labelSmall) }
            )
        }

        // Show date range if applied
        if (filter.startDate != null || filter.endDate != null) {
            FilterChip(
                selected = true,
                onClick = { },
                label = {
                    Text(
                        text = buildString {
                            append("📅 ")
                            filter.startDate?.let { append(it) }
                            append(" - ")
                            filter.endDate?.let { append(it) }
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(onClick = onClear) {
            Text("Clear", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ==================== Inline Components ====================

/**
 * Summary card showing payment totals at the top of the list.
 * Clean UI with white background in day mode.
 */
@Composable
private fun PaymentSummaryCard(
    totalReceived: String,
    totalPending: String,
    thisMonth: String,
    paymentCount: Int = 0,
    tripCount: Int = 0,
    isGroupedView: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with payment count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (paymentCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$paymentCount payments",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Received Card
                SummaryItemCard(
                    value = totalReceived,
                    label = "Received",
                    valueColor = Color(0xFF2E7D32),
                    backgroundColor = Color(0xFF2E7D32).copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Pending Card
                SummaryItemCard(
                    value = totalPending,
                    label = "Pending",
                    valueColor = Color(0xFFE65100),
                    backgroundColor = Color(0xFFE65100).copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // This Month Card
                SummaryItemCard(
                    value = thisMonth,
                    label = "This Month",
                    valueColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
            }

            // Trip count section - only in grouped view
            if (isGroupedView) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grouped by Trip: $tripCount trips",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

/**
 * Individual summary item with background (no icon).
 */
@Composable
private fun SummaryItemCard(
    value: String,
    label: String,
    valueColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Card displaying a single payment item in the list.
 * Enhanced compact layout with Trip info grouped visually.
 */
@Composable
private fun PaymentCard(
    payment: TripPayment,
    onClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Trip ID + Payment Type | Amount + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Trip ID badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Trip #${payment.tripId}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    // Payment Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getPaymentTypeColor(payment.paymentType).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${payment.paymentType.icon} ${payment.typeDisplay}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = getPaymentTypeColor(payment.paymentType)
                        )
                    }
                }

                // Amount + Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = payment.amountDisplay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (payment.isReceived) Color(0xFF2E7D32)
                        else if (payment.isPending) Color(0xFFE65100)
                        else MaterialTheme.colorScheme.onSurface
                    )
                    PaymentStatusBadge(status = payment.paymentStatus)
                }
            }

            // Trip Info Section - grouped in a subtle background
            payment.tripInfo?.let { tripInfo ->
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Vehicle + Route
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tripInfo.vehicleRegistration?.let { vehicle ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = vehicle,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        // Route with better visibility
                        Text(
                            text = "${tripInfo.startLocation ?: "Unknown"} → ${tripInfo.endLocation ?: "Unknown"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Trip Dates (only show if valid)
                    val hasValidDates = tripInfo.tripStartDate != null || tripInfo.tripEndDate != null
                    if (hasValidDates) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Depart: ${tripInfo.startDateTimeDisplay}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Arrive: ${tripInfo.endDateTimeDisplay}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Row: Customer + Payment Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer name - prioritize customerName, fallback to customerCompany
                val customerDisplayName = payment.customerName?.takeIf { it.isNotBlank() }
                    ?: payment.customerCompany?.takeIf { it.isNotBlank() }
                    ?: "No Customer"
                Text(
                    text = customerDisplayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (customerDisplayName != "No Customer")
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Payment received date with label
                payment.paymentDate?.let { date ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = "Paid: ${formatPaymentDate(date)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Get color for payment type badge.
 */
private fun getPaymentTypeColor(type: PaymentType): Color {
    return when (type) {
        PaymentType.ADVANCE -> Color(0xFF1976D2)  // Blue
        PaymentType.PARTIAL -> Color(0xFFFFA000)  // Orange
        PaymentType.FINAL -> Color(0xFF2E7D32)    // Green
        PaymentType.REFUND -> Color(0xFFD32F2F)   // Red
    }
}

@Composable
private fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PaymentStatus.RECEIVED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        PaymentStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        PaymentStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}


/**
 * Format ISO date to display format: "DD-MMM-YYYY hh:mm AM/PM"
 */
private fun formatPaymentDate(isoDate: String): String {
    return FleetDateTime.formatIsoToDisplayDateTime12Hour(isoDate)
}

/**
 * Filter bottom sheet for payments list.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PaymentFilterBottomSheet(
    filter: TripPaymentFilter,
    onDismiss: () -> Unit,
    onUpdateType: (PaymentType?) -> Unit,
    onUpdateMode: (PaymentMode?) -> Unit,
    onUpdateStatus: (PaymentStatus?) -> Unit,
    onUpdateDateRange: (String?, String?) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Local state for date fields
    var startDate by remember(filter.startDate) { mutableStateOf(filter.startDate ?: "") }
    var endDate by remember(filter.endDate) { mutableStateOf(filter.endDate ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Payments",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    startDate = ""
                    endDate = ""
                    onReset()
                }) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Type Section
            Text(
                text = "Payment Type",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.paymentType == null,
                    onClick = { onUpdateType(null) },
                    label = { Text("All", style = MaterialTheme.typography.bodySmall) }
                )
                PaymentType.entries.forEach { type ->
                    FilterChip(
                        selected = filter.paymentType == type,
                        onClick = { onUpdateType(type) },
                        label = { Text(type.displayName, style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Payment Mode Section
            Text(
                text = "Payment Mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.paymentMode == null,
                    onClick = { onUpdateMode(null) },
                    label = { Text("All", style = MaterialTheme.typography.bodySmall) }
                )
                PaymentMode.entries.forEach { mode ->
                    FilterChip(
                        selected = filter.paymentMode == mode,
                        onClick = { onUpdateMode(mode) },
                        label = { Text("${mode.icon} ${mode.displayName}", style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Payment Status Section
            Text(
                text = "Status",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter.paymentStatus == null,
                    onClick = { onUpdateStatus(null) },
                    label = { Text("All", style = MaterialTheme.typography.bodySmall) }
                )
                PaymentStatus.entries.forEach { status ->
                    FilterChip(
                        selected = filter.paymentStatus == status,
                        onClick = { onUpdateStatus(status) },
                        label = { Text("${status.icon} ${status.displayName}", style = MaterialTheme.typography.bodySmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Date Range Section
            Text(
                text = "📅 Date Range",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetDateTimePicker(
                    date = startDate,
                    time = "",
                    onDateTimeChange = { date, _ ->
                        startDate = date
                        onUpdateDateRange(date.ifEmpty { null }, endDate.ifEmpty { null })
                    },
                    modifier = Modifier.weight(1f),
                    mode = PickerMode.DATE_ONLY,
                    label = "From Date",
                    maxDate = endDate.ifEmpty { null }
                )

                FleetDateTimePicker(
                    date = endDate,
                    time = "",
                    onDateTimeChange = { date, _ ->
                        endDate = date
                        onUpdateDateRange(startDate.ifEmpty { null }, date.ifEmpty { null })
                    },
                    modifier = Modifier.weight(1f),
                    mode = PickerMode.DATE_ONLY,
                    label = "To Date",
                    minDate = startDate.ifEmpty { null }
                )
            }

            // Quick date range options
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Quick Select",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Get today's date using FleetDateTime utility
            val todayFormatted = remember { FleetDateTime.today() }
            val todayValue = remember { FleetDateTime.now() }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // This Month
                AssistChip(
                    onClick = {
                        val startOfMonth = formatLocalDateToDDMMYYYY(
                            todayValue.year, todayValue.month, 1
                        )
                        startDate = startOfMonth
                        endDate = todayFormatted
                        onUpdateDateRange(startOfMonth, todayFormatted)
                    },
                    label = { Text("This Month", style = MaterialTheme.typography.labelSmall) }
                )

                // Last Month
                AssistChip(
                    onClick = {
                        val lastMonthYear = if (todayValue.month == 1) todayValue.year - 1 else todayValue.year
                        val lastMonthNumber = if (todayValue.month == 1) 12 else todayValue.month - 1
                        val startOfLastMonth = formatLocalDateToDDMMYYYY(lastMonthYear, lastMonthNumber, 1)
                        val endOfLastMonth = formatLocalDateToDDMMYYYY(
                            lastMonthYear, lastMonthNumber,
                            getLastDayOfMonth(lastMonthYear, lastMonthNumber)
                        )
                        startDate = startOfLastMonth
                        endDate = endOfLastMonth
                        onUpdateDateRange(startOfLastMonth, endOfLastMonth)
                    },
                    label = { Text("Last Month", style = MaterialTheme.typography.labelSmall) }
                )

                // Last 7 Days
                AssistChip(
                    onClick = {
                        // Use FleetDateTime.addDays with date string format
                        val last7Formatted = FleetDateTime.addDays(todayFormatted, -7) ?: todayFormatted
                        startDate = last7Formatted
                        endDate = todayFormatted
                        onUpdateDateRange(last7Formatted, todayFormatted)
                    },
                    label = { Text("Last 7 Days", style = MaterialTheme.typography.labelSmall) }
                )

                // Last 30 Days
                AssistChip(
                    onClick = {
                        // Use FleetDateTime.addDays with date string format
                        val last30Formatted = FleetDateTime.addDays(todayFormatted, -30) ?: todayFormatted
                        startDate = last30Formatted
                        endDate = todayFormatted
                        onUpdateDateRange(last30Formatted, todayFormatted)
                    },
                    label = { Text("Last 30 Days", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
        }
    }
}

/**
 * Format date to DD-MM-YYYY format.
 */
private fun formatLocalDateToDDMMYYYY(year: Int, month: Int, day: Int): String {
    return "${day.toString().padStart(2, '0')}-${month.toString().padStart(2, '0')}-$year"
}

/**
 * Get the last day of a given month.
 */
private fun getLastDayOfMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> 30
    }
}

/**
 * Empty content shown when filters are applied but no results found.
 * Shows "Clear Filters" instead of "Add Payment".
 */
@Composable
private fun EmptyFilteredContent(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "🔍",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Results Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No payments match your current filters.\nTry adjusting your filters or clear them to see all payments.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onClearFilters) {
                Text("Clear Filters")
            }
        }
    }
}

/**
 * Custom error content for Payments screen with better UX.
 */
@Composable
private fun PaymentsErrorContent(
    @Suppress("UNUSED_PARAMETER") error: String,
    onRetry: () -> Unit,
    onAddPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Text(
                text = "💳",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Unable to Load Payments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We couldn't load the payments. Please check your connection and try again.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAddPayment) {
                Text("Add New Payment")
            }
        }
    }
}


/**
 * Collapsible card showing a trip group with expandable payment list.
 * Animation duration: 300ms for smooth expand/collapse.
 */
@Composable
private fun CollapsibleTripGroupCard(
    group: TripPaymentGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onPaymentClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Trip Header - Always visible, clickable to expand/collapse
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                color = if (isExpanded)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left side: Trip info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Trip ID badge
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Trip #${group.tripId}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Vehicle badge
                            group.tripInfo?.vehicleRegistration?.let { vehicle ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = vehicle,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            // Payment count badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = "${group.paymentCount} payment${if (group.paymentCount > 1) "s" else ""}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Route
                        group.tripInfo?.let { tripInfo ->
                            Text(
                                text = "${tripInfo.startLocation ?: "Unknown"} → ${tripInfo.endLocation ?: "Unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Customer
                        group.customerName?.let { customer ->
                            Text(
                                text = customer,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Right side: Total amount + expand icon
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${formatGroupAmount(group.totalAmount)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )

                        // Arrow indicator using text
                        Text(
                            text = if (isExpanded) "▲" else "▼",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Expanded content - Payment list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    group.payments.forEach { payment ->
                        CompactPaymentItem(
                            payment = payment,
                            onClick = { onPaymentClick(payment.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact payment item shown inside expanded trip group.
 */
@Composable
private fun CompactPaymentItem(
    payment: TripPayment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Payment type + date
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Payment Type badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getPaymentTypeColor(payment.paymentType).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${payment.paymentType.icon} ${payment.typeDisplay}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = getPaymentTypeColor(payment.paymentType)
                        )
                    }

                    // Payment mode
                    Text(
                        text = payment.modeIcon,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Payment date
                payment.paymentDate?.let { date ->
                    Text(
                        text = formatPaymentDate(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Amount + status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = payment.amountDisplay,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (payment.isReceived) Color(0xFF2E7D32)
                    else if (payment.isPending) Color(0xFFE65100)
                    else MaterialTheme.colorScheme.onSurface
                )
                PaymentStatusBadge(status = payment.paymentStatus)
            }
        }
    }
}

/**
 * Format large amounts for group display.
 */
private fun formatGroupAmount(amount: Double): String {
    return when {
        amount >= 10000000 -> {
            val cr = amount / 10000000
            "${formatDecimal(cr, 2)} Cr"
        }
        amount >= 100000 -> {
            val lakh = amount / 100000
            "${formatDecimal(lakh, 2)} L"
        }
        amount >= 1000 -> {
            val k = amount / 1000
            "${formatDecimal(k, 1)} K"
        }
        else -> formatDecimal(amount, 0)
    }
}

/**
 * Format decimal with specified precision (multiplatform compatible).
 */
private fun formatDecimal(value: Double, decimals: Int): String {
    if (decimals == 0) {
        return kotlin.math.round(value).toLong().toString()
    }
    // Manual power calculation for multiplatform
    var factor = 1.0
    repeat(decimals) { factor *= 10.0 }
    val rounded = kotlin.math.round(value * factor) / factor
    val str = rounded.toString()
    val parts = str.split(".")
    return if (parts.size == 1) {
        str + "." + "0".repeat(decimals)
    } else {
        val decPart = parts[1].take(decimals).padEnd(decimals, '0')
        "${parts[0]}.$decPart"
    }
}
