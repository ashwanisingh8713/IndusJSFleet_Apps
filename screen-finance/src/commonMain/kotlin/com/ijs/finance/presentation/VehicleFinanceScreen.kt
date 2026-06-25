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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.datetimeutils.FleetEpoch
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FinanceColors
import com.indusjs.uicomponents.components.FinanceFilterChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
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
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
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
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact stays 1-up; Medium/Expanded show two cards per row. On Expanded
        // the grid is capped to a readable width and centered instead of stretching.
        val columns = if (breakpoint.isAtLeastMedium) 2 else 1
        val contentWidthModifier = if (breakpoint.isExpanded) {
            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxWidth()
        }

        LazyColumn(
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
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
                FleetSearchField(
                    query = state.searchQuery,
                    onQueryChange = onSearchChange,
                    placeholder = stringResource(Res.string.finance_search_vehicles_placeholder)
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
            } else if (columns == 1) {
                items(state.filteredVehicles) { item ->
                    VehicleFinanceCard(
                        item = item,
                        notAvailableLabel = notAvailableLabel,
                        onClick = { onVehicleClick(item.vehicle.id.toIntOrNull() ?: 0) },
                        onRecordEmiClick = { onRecordEmiClick(item.vehicle.id.toIntOrNull() ?: 0) },
                        onAddPurchaseClick = onAddPurchaseClick
                    )
                }
            } else {
                val rows = state.filteredVehicles.chunked(columns)
                items(rows) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                    ) {
                        row.forEach { item ->
                            VehicleFinanceCard(
                                item = item,
                                notAvailableLabel = notAvailableLabel,
                                onClick = { onVehicleClick(item.vehicle.id.toIntOrNull() ?: 0) },
                                onRecordEmiClick = { onRecordEmiClick(item.vehicle.id.toIntOrNull() ?: 0) },
                                onAddPurchaseClick = onAddPurchaseClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Keep the last odd card aligned to a single column width.
                        if (row.size < columns) {
                            repeat(columns - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL + FleetTokens.Spacing.XXL)) }
        }
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
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Monthly EMI Badge
            if (monthlyEmiTotal > 0) {
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = WarningOrange.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S),
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
                            .width(FleetTokens.Height.Divider)
                            .height(FleetTokens.Height.ButtonSmall)
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
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
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
            .clip(RoundedCornerShape(FleetTokens.Radius.M))
            .background(
                if (isOverdue) CriticalRed.copy(alpha = 0.1f)
                else WarningOrange.copy(alpha = 0.1f)
            )
            .clickable(onClick = onRecordClick)
            .padding(FleetTokens.Spacing.M),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isOverdue) "🔴" else "⚠️",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
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
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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
    onAddPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Only make the card clickable if it's not a pending (not recorded) item
    val isClickable = item.status != FinanceStatus.PENDING

    FleetSectionCard(
        modifier = modifier,
        onClick = if (isClickable) onClick else null
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
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
        shape = RoundedCornerShape(FleetTokens.Radius.XL),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS),
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
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)) {
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
        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.finance_percent_complete, "${purchase.loanProgressPercent.toInt()}%"),
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
                    .height(FleetTokens.Height.ProgressBar)
                    .clip(RoundedCornerShape(FleetTokens.Radius.S)),
                color = LoanBlue,
                trackColor = LoanBlue.copy(alpha = 0.15f)
            )
        }

        // Outstanding and Next EMI with action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FleetTokens.Radius.L))
                .background(WarningOrange.copy(alpha = 0.08f))
                .padding(FleetTokens.Spacing.M),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS)) {
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

            Box {
                FleetButton(
                    text = stringResource(Res.string.finance_record_emi),
                    onClick = onRecordEmiClick,
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.SMALL
                )
            }
        }
    }
}

@Composable
private fun CashCardContent(purchase: VehiclePurchase, notAvailableLabel: String) {
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FleetTokens.Radius.L))
                .background(CashGreen.copy(alpha = 0.08f))
                .padding(FleetTokens.Spacing.M),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS)) {
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
                shape = RoundedCornerShape(FleetTokens.Radius.XXL),
                color = CashGreen.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
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
    Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(FleetTokens.Radius.L))
                .background(NoInfoGray.copy(alpha = 0.08f))
                .padding(FleetTokens.Spacing.M),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS)) {
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

            Box {
                FleetButton(
                    text = stringResource(Res.string.finance_add_info_short),
                    onClick = onAddClick,
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.SMALL
                )
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
