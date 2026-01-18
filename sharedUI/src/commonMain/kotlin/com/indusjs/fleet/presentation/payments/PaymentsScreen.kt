package com.indusjs.fleet.presentation.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.pdf.PaymentsListPdfExportHandler
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.payment.*
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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
    PaymentsListPdfExportHandler(
        pdfData = pdfExportData,
        onExportComplete = {
            isExportingPdf = false
            pdfExportData = null
            scope.launch {
                snackbarHostState.showSnackbar("PDF exported successfully!")
            }
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
                title = { Text("Payments") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // PDF Export button - only show when there's data
                    if (state.payments.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                // Generate PDF data from current state
                                val nowValue = FleetDateTime.now()
                                pdfExportData = PaymentsListPdfData(
                                    generatedDate = FleetDateTime.today(),
                                    generatedTime = FleetDateTime.currentTime(),
                                    fromDate = state.filter.startDate,
                                    toDate = state.filter.endDate,
                                    paymentType = state.filter.paymentType?.displayName,
                                    paymentMode = state.filter.paymentMode?.displayName,
                                    paymentStatus = state.filter.paymentStatus?.displayName,
                                    totalReceived = state.payments.filter { it.paymentStatus == PaymentStatus.RECEIVED }.sumOf { it.amount },
                                    totalPending = state.pendingSummary?.totalPending ?: 0.0,
                                    thisMonthTotal = state.summary?.thisMonthTotal ?: state.payments.filter { it.paymentStatus == PaymentStatus.RECEIVED }.sumOf { it.amount },
                                    totalPaymentsCount = state.payments.size,
                                    payments = state.payments.map { payment ->
                                        PaymentPdfItem(
                                            id = payment.id,
                                            paymentDate = payment.paymentDate?.take(10) ?: "",
                                            amount = payment.amount,
                                            paymentType = payment.typeDisplay,
                                            paymentMode = payment.modeDisplay,
                                            paymentStatus = payment.paymentStatus.displayName,
                                            vehicleNumber = payment.tripInfo?.vehicleRegistration,
                                            route = payment.tripInfo?.routeDisplay,
                                            customerName = payment.customerName,
                                            receiptNumber = payment.receiptNumber,
                                            notes = payment.notes
                                        )
                                    }
                                )
                            },
                            enabled = !isExportingPdf
                        ) {
                            if (isExportingPdf) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_download),
                                    contentDescription = "Export PDF"
                                )
                            }
                        }
                    }
                    // Filter button
                    IconButton(onClick = { viewModel.sendIntent(PaymentsContract.Intent.ShowFilterSheet) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_filter),
                            contentDescription = "Filter"
                        )
                    }
                    IconButton(onClick = { viewModel.sendIntent(PaymentsContract.Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Refresh"
                        )
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
                    // No payments exist - show empty state
                    EmptyContent(
                        title = "No Payments Yet",
                        message = if (state.hasFilters) "No payments match your filters" else "Record your first payment to get started",
                        actionLabel = "Add Payment",
                        onAction = { viewModel.sendIntent(PaymentsContract.Intent.NavigateToAddPayment) }
                    )
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
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary Card
                        item {
                            PaymentSummaryCard(
                                totalReceived = state.totalReceived,
                                totalPending = state.totalPending,
                                thisMonth = state.thisMonth
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

                        // Payment items
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
 * Enhanced UI with gradient background and better visual design.
 */
@Composable
private fun PaymentSummaryCard(
    totalReceived: String,
    totalPending: String,
    thisMonth: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "💳 Payment Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Received Card
                SummaryItemCard(
                    emoji = "💰",
                    value = totalReceived,
                    label = "Received",
                    valueColor = Color(0xFF2E7D32),
                    backgroundColor = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Pending Card
                SummaryItemCard(
                    emoji = "⏳",
                    value = totalPending,
                    label = "Pending",
                    valueColor = Color(0xFFE65100),
                    backgroundColor = Color(0xFFE65100).copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // This Month Card
                SummaryItemCard(
                    emoji = "📅",
                    value = thisMonth,
                    label = "This Month",
                    valueColor = MaterialTheme.colorScheme.primary,
                    backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual summary item with background.
 */
@Composable
private fun SummaryItemCard(
    emoji: String,
    value: String,
    label: String,
    valueColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Mode icon, Amount, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mode icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(getModeColor(payment.paymentMode).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = payment.modeIcon,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Column {
                        Text(
                            text = payment.modeDisplay,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = payment.typeDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = payment.amountDisplay,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (payment.isReceived) {
                            Color(0xFF2E7D32)
                        } else if (payment.isPending) {
                            Color(0xFFE65100)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    PaymentStatusBadge(status = payment.paymentStatus)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Trip info row - Vehicle and Route
            payment.tripInfo?.let { tripInfo ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vehicle registration
                    tripInfo.vehicleRegistration?.let { vehicle ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "🚛 $vehicle",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Route
                    Text(
                        text = tripInfo.routeDisplay,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Customer and date row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    payment.customerName?.let { customer ->
                        Text(
                            text = customer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    payment.receiptNumber?.let { receipt ->
                        Text(
                            text = "Receipt: $receipt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                payment.paymentDate?.let { date ->
                    Text(
                        text = formatPaymentDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
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

@Composable
private fun getModeColor(mode: PaymentMode): Color {
    return when (mode) {
        PaymentMode.CASH -> Color(0xFF4CAF50)
        PaymentMode.UPI -> Color(0xFF9C27B0)
        PaymentMode.BANK_TRANSFER -> Color(0xFF2196F3)
        PaymentMode.CARD -> Color(0xFFFF9800)
        PaymentMode.CREDIT -> Color(0xFF607D8B)
    }
}

private fun formatPaymentDate(isoDate: String): String {
    return try {
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) {
            "${parts[2]}-${parts[1]}-${parts[0].takeLast(2)}"
        } else {
            isoDate.take(10)
        }
    } catch (_: Exception) {
        isoDate.take(10)
    }
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
