package com.ijs.finance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FinanceColors
import com.indusjs.uicomponents.components.FinanceFilterChip
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.fleet.core.util.formatCurrency
import com.ijs.finance.domain.entity.*
import com.ijs.finance.presentation.VehicleFinanceContract.Effect
import com.ijs.finance.presentation.VehicleFinanceContract.Intent
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Use FinanceColors from core.ui
private val LoanBlue = FinanceColors.LoanBlue
private val CashGreen = FinanceColors.CashGreen
private val NoInfoGray = FinanceColors.NeutralGray
private val WarningOrange = FinanceColors.WarningOrange
private val CriticalRed = FinanceColors.CriticalRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleFinanceScreen(
    viewModel: VehicleFinanceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToAddPurchase: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is Effect.NavigateToDetail -> onNavigateToDetail(effect.vehicleId)
                is Effect.NavigateToAddPurchase -> onNavigateToAddPurchase()
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.PurchaseSaved -> { /* Handled in ViewModel */ }
                is Effect.PaymentRecorded -> { /* Handled in ViewModel */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.finance_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.refresh)
                        )
                    }
                    IconButton(onClick = { viewModel.sendIntent(Intent.NavigateToAddPurchase) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = stringResource(Res.string.add)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.vehiclesWithoutPurchase.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.sendIntent(Intent.NavigateToAddPurchase) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.finance_add_purchase))
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(Intent.Refresh) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.vehicles.isEmpty() -> LoadingContent()
                state.error != null && state.vehicles.isEmpty() -> ErrorContent(
                    error = state.error?.resolve() ?: stringResource(Res.string.finance_error_generic),
                    onRetry = { viewModel.sendIntent(Intent.LoadData) }
                )
                else -> VehicleFinanceContent(
                    state = state,
                    onFilterSelect = { viewModel.sendIntent(Intent.SetFilter(it)) },
                    onSearchChange = { viewModel.sendIntent(Intent.UpdateSearch(it)) },
                    onVehicleClick = { viewModel.sendIntent(Intent.NavigateToDetail(it)) },
                    onAddPurchaseClick = { viewModel.sendIntent(Intent.NavigateToAddPurchase) },
                    onRecordEmiClick = { vehicleId ->
                        viewModel.sendIntent(Intent.SelectVehicle(vehicleId))
                        viewModel.sendIntent(Intent.ShowRecordPaymentSheet)
                    }
                )
            }
        }
    }
}

@Composable
private fun VehicleFinanceContent(
    state: VehicleFinanceContract.State,
    onFilterSelect: (FinanceFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onVehicleClick: (Int) -> Unit,
    onAddPurchaseClick: () -> Unit,
    onRecordEmiClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val notAvailableLabel = stringResource(Res.string.not_applicable_short)
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card
        item {
            FinanceSummaryCard(
                totalVehicles = state.totalVehicles,
                financedVehicles = state.financedVehicles,
                cashVehicles = state.cashVehicles,
                pendingVehicles = state.pendingVehicles,
                monthlyEmiTotal = state.monthlyEmiTotal,
                totalPaid = state.totalPaid,
                totalOutstanding = state.totalOutstanding
            )
        }

        // Alerts Section
        if (state.hasAlerts) {
            item {
                EmiAlertsCard(
                    upcomingAlerts = state.upcomingAlerts,
                    overdueAlerts = state.overdueAlerts,
                    onRecordClick = onRecordEmiClick
                )
            }
        }

        // Filter Chips with counts
        item {
            FilterChipsRow(
                selectedFilter = state.selectedFilter,
                financedCount = state.financedVehicles,
                cashCount = state.cashVehicles,
                pendingCount = state.pendingVehicles,
                onFilterSelect = onFilterSelect
            )
        }

        // Search
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(stringResource(Res.string.finance_search_vehicles_placeholder)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_close),
                                contentDescription = stringResource(Res.string.action_clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoanBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Vehicle List
        if (state.filteredVehicles.isEmpty()) {
            item {
                EmptyContent(
                    title = stringResource(Res.string.finance_empty_no_vehicles),
                    actionLabel = if (state.selectedFilter == FinanceFilter.PENDING) {
                        stringResource(Res.string.finance_add_purchase)
                    } else null,
                    onAction = if (state.selectedFilter == FinanceFilter.PENDING) onAddPurchaseClick else null,
                    fillMaxSize = false
                )
            }
        } else {
            items(state.filteredVehicles) { item ->
                VehicleFinanceCard(
                    item = item,
                    notAvailableLabel = notAvailableLabel,
                    onClick = { onVehicleClick(item.vehicle.id.toIntOrNull() ?: 0) },
                    onRecordEmiClick = { onRecordEmiClick(item.vehicle.id.toIntOrNull() ?: 0) },
                    onAddPurchaseClick = onAddPurchaseClick
                )
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun FinanceSummaryCard(
    totalVehicles: Int,
    financedVehicles: Int,
    cashVehicles: Int,
    pendingVehicles: Int,
    monthlyEmiTotal: Double,
    totalPaid: Double,
    totalOutstanding: Double
) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.finance_fleet_finance),
        subtitle = stringResource(Res.string.finance_vehicles_in_fleet, totalVehicles)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monthly EMI Badge
            if (monthlyEmiTotal > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarningOrange.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatCurrency(monthlyEmiTotal),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = WarningOrange
                        )
                        Text(
                            text = stringResource(Res.string.finance_monthly_emi_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = WarningOrange.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Stats Row - Compact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CompactStatItem(
                    value = financedVehicles.toString(),
                    label = stringResource(Res.string.finance_stat_loans),
                    color = LoanBlue
                )
                CompactStatItem(
                    value = cashVehicles.toString(),
                    label = stringResource(Res.string.finance_stat_cash),
                    color = CashGreen
                )
                CompactStatItem(
                    value = pendingVehicles.toString(),
                    label = stringResource(Res.string.finance_stat_pending),
                    color = NoInfoGray
                )
            }

            // Financial Summary
            if (totalOutstanding > 0 || totalPaid > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatCurrency(totalPaid),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CashGreen
                        )
                        Text(
                            text = stringResource(Res.string.finance_total_paid_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatCurrency(totalOutstanding),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CriticalRed
                        )
                        Text(
                            text = stringResource(Res.string.finance_outstanding_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactStatItem(
    value: String,
    label: String,
    color: Color
) {
    FleetMetricTile(
        value = value,
        label = label,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true
    )
}


@Composable
private fun EmiAlertsCard(
    upcomingAlerts: List<EmiAlert>,
    overdueAlerts: List<EmiAlert>,
    onRecordClick: (Int) -> Unit
) {
    FleetSectionCard(
        containerColor = if (overdueAlerts.isNotEmpty())
            CriticalRed.copy(alpha = 0.1f)
        else
            WarningOrange.copy(alpha = 0.1f),
        border = null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.finance_emi_alerts_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Overdue alerts
            overdueAlerts.forEach { alert ->
                EmiAlertItem(
                    alert = alert,
                    isOverdue = true,
                    onRecordClick = { onRecordClick(alert.vehicleId) }
                )
            }

            // Upcoming alerts
            upcomingAlerts.take(3).forEach { alert ->
                EmiAlertItem(
                    alert = alert,
                    isOverdue = false,
                    onRecordClick = { onRecordClick(alert.vehicleId) }
                )
            }
        }
    }
}

@Composable
private fun EmiAlertItem(
    alert: EmiAlert,
    isOverdue: Boolean,
    onRecordClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isOverdue) CriticalRed.copy(alpha = 0.1f)
                else WarningOrange.copy(alpha = 0.1f)
            )
            .clickable(onClick = onRecordClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isOverdue) "🔴" else "⚠️",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.vehicle?.registrationNumber ?: stringResource(Res.string.finance_vehicle_fallback),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isOverdue) {
                    stringResource(Res.string.finance_overdue_by_days, alert.daysOverdue)
                } else {
                    stringResource(Res.string.finance_due_in_days, alert.daysUntilDue)
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverdue) CriticalRed else WarningOrange
            )
        }
        Text(
            text = formatCurrency(alert.amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (isOverdue) CriticalRed else WarningOrange
        )
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: FinanceFilter,
    financedCount: Int,
    cashCount: Int,
    pendingCount: Int,
    onFilterSelect: (FinanceFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FinanceFilterChip(
                label = stringResource(Res.string.finance_filter_all),
                count = financedCount + cashCount + pendingCount,
                selected = selectedFilter == FinanceFilter.ALL,
                color = MaterialTheme.colorScheme.primary,
                onClick = { onFilterSelect(FinanceFilter.ALL) }
            )
        }
        item {
            FinanceFilterChip(
                label = stringResource(Res.string.finance_filter_financed),
                count = financedCount,
                selected = selectedFilter == FinanceFilter.LOAN,
                color = LoanBlue,
                onClick = { onFilterSelect(FinanceFilter.LOAN) }
            )
        }
        item {
            FinanceFilterChip(
                label = stringResource(Res.string.finance_stat_cash),
                count = cashCount,
                selected = selectedFilter == FinanceFilter.CASH,
                color = CashGreen,
                onClick = { onFilterSelect(FinanceFilter.CASH) }
            )
        }
        item {
            FinanceFilterChip(
                label = stringResource(Res.string.finance_filter_not_recorded),
                count = pendingCount,
                selected = selectedFilter == FinanceFilter.PENDING,
                color = NoInfoGray,
                onClick = { onFilterSelect(FinanceFilter.PENDING) }
            )
        }
    }
}

@Composable
private fun VehicleFinanceCard(
    item: VehicleFinanceItem,
    notAvailableLabel: String,
    onClick: () -> Unit,
    onRecordEmiClick: () -> Unit,
    onAddPurchaseClick: () -> Unit
) {
    // Only make the card clickable if it's not a pending (not recorded) item
    val isClickable = item.status != FinanceStatus.PENDING

    FleetSectionCard(
        onClick = if (isClickable) onClick else null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with vehicle info and status - without icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.vehicle.registrationNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.vehicle.make} ${item.vehicle.model} (${item.vehicle.year})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status chip
                StatusChip(status = item.status)
            }

            // Content based on status
            when (item.status) {
                FinanceStatus.LOAN ->                 LoanCardContent(
                    purchase = item.purchase!!,
                    onRecordEmiClick = onRecordEmiClick,
                    notAvailableLabel = notAvailableLabel
                )
                FinanceStatus.CASH -> CashCardContent(
                    purchase = item.purchase!!,
                    notAvailableLabel = notAvailableLabel
                )
                FinanceStatus.PENDING -> NoInfoCardContent(onAddClick = onAddPurchaseClick)
            }
        }
    }
}

@Composable
private fun StatusChip(status: FinanceStatus) {
    val color = when (status) {
        FinanceStatus.LOAN -> LoanBlue
        FinanceStatus.CASH -> CashGreen
        FinanceStatus.PENDING -> NoInfoGray
    }
    val label = when (status) {
        FinanceStatus.LOAN -> stringResource(Res.string.finance_status_financed)
        FinanceStatus.CASH -> stringResource(Res.string.finance_stat_cash)
        FinanceStatus.PENDING -> stringResource(Res.string.finance_status_not_recorded)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LoanCardContent(
    purchase: VehiclePurchase,
    onRecordEmiClick: () -> Unit,
    notAvailableLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Divider
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Key metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.finance_lender),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = purchase.financierName ?: notAvailableLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.finance_monthly_emi_badge),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(purchase.emiAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LoanBlue
                )
            }
        }

        // Progress section
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.finance_percent_complete, purchase.loanProgressPercent.toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = LoanBlue,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        Res.string.finance_emis_paid_progress,
                        purchase.emisPaid,
                        purchase.tenureMonths
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            LinearProgressIndicator(
                progress = { purchase.loanProgressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = LoanBlue,
                trackColor = LoanBlue.copy(alpha = 0.15f)
            )
        }

        // Outstanding and Next EMI with action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WarningOrange.copy(alpha = 0.08f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(
                        Res.string.finance_next_emi_line,
                        getNextEmiDueDate(purchase, notAvailableLabel)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(
                        Res.string.finance_outstanding_line,
                        formatCurrency(purchase.outstandingBalance)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = CriticalRed
                )
            }

            FilledTonalButton(
                onClick = onRecordEmiClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = LoanBlue.copy(alpha = 0.15f),
                    contentColor = LoanBlue
                )
            ) {
                Text(stringResource(Res.string.finance_record_emi), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CashCardContent(purchase: VehiclePurchase, notAvailableLabel: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CashGreen.copy(alpha = 0.08f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.finance_purchase_price),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(purchase.purchasePrice),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CashGreen
                )
                Text(
                    text = stringResource(Res.string.finance_purchased_on, formatDueDateDisplay(purchase.purchaseDate)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CashGreen.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("✓", style = MaterialTheme.typography.labelMedium, color = CashGreen)
                    Text(
                        text = stringResource(Res.string.finance_fully_paid),
                        style = MaterialTheme.typography.labelMedium,
                        color = CashGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun NoInfoCardContent(onAddClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NoInfoGray.copy(alpha = 0.08f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(Res.string.finance_no_purchase_card),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(Res.string.finance_add_details_track),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = onAddClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(stringResource(Res.string.finance_add_info_short), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Get the next EMI due date for display.
 * Calculates from loanStartDate + emisPaid months to ensure accuracy.
 */
private fun getNextEmiDueDate(purchase: VehiclePurchase, notAvailableLabel: String): String {
    // Calculate from loanStartDate (epoch-ms) + emisPaid months
    val loanStartDate = purchase.loanStartDate
    if (loanStartDate != null && loanStartDate > 0L) {
        val startValue = FleetEpoch.toValue(loanStartDate)
        if (startValue != null) {
            val startDateStr = FleetDateTime.formatDate(startValue)
            val nextDueDateStr = FleetDateTime.addMonths(startDateStr, purchase.emisPaid)
            if (nextDueDateStr != null) {
                return FleetDateTime.formatAnyToDisplayDate(nextDueDateStr)
            }
        }
    }

    // Fallback to API-provided nextEmiDueDate (epoch-ms)
    val nextDue = purchase.nextEmiDueDate
    if (nextDue != null && nextDue > 0L) {
        return formatDueDateDisplay(nextDue)
    }

    return notAvailableLabel
}

/**
 * Format an epoch-ms timestamp for display ("DD-MMM-YYYY"). Treats null/0 as unset.
 */
private fun formatDueDateDisplay(timestampMillis: Long?): String =
    if (timestampMillis == null || timestampMillis <= 0L) ""
    else formatDateToHumanReadable(timestampMillis)
