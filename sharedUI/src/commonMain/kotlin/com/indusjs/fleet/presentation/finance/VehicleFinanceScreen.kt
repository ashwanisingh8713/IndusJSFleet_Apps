package com.indusjs.fleet.presentation.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FinanceColors
import com.indusjs.fleet.core.ui.FinanceFilterChip
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.finance.*
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Effect
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Intent
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

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

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
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
                title = { Text("Vehicle Finance") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = { viewModel.sendIntent(Intent.NavigateToAddPurchase) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = "Add"
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
                    Text("Add Purchase Info")
                }
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading && state.vehicles.isEmpty() -> LoadingContent()
            state.error != null && state.vehicles.isEmpty() -> ErrorContent(
                error = state.error ?: "Something went wrong",
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
                },
                modifier = Modifier.padding(paddingValues)
            )
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
                placeholder = { Text("Search vehicles...") },
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
                                contentDescription = "Clear",
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
                    title = "No vehicles found",
                    actionLabel = if (state.selectedFilter == FinanceFilter.PENDING) "Add Purchase Info" else null,
                    onAction = if (state.selectedFilter == FinanceFilter.PENDING) onAddPurchaseClick else null
                )
            }
        } else {
            items(state.filteredVehicles) { item ->
                VehicleFinanceCard(
                    item = item,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fleet Finance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalVehicles vehicles in fleet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                                text = "Monthly EMI",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarningOrange.copy(alpha = 0.8f)
                            )
                        }
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
                    label = "Loans",
                    color = LoanBlue
                )
                CompactStatItem(
                    value = cashVehicles.toString(),
                    label = "Cash",
                    color = CashGreen
                )
                CompactStatItem(
                    value = pendingVehicles.toString(),
                    label = "Pending",
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
                            text = "Total Paid",
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
                            text = "Outstanding",
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun EmiAlertsCard(
    upcomingAlerts: List<EmiAlert>,
    overdueAlerts: List<EmiAlert>,
    onRecordClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (overdueAlerts.isNotEmpty())
                CriticalRed.copy(alpha = 0.1f)
            else
                WarningOrange.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🔔 EMI Alerts",
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
                text = alert.vehicle?.registrationNumber ?: "Vehicle",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (isOverdue)
                    "Overdue by ${alert.daysOverdue} days"
                else
                    "Due in ${alert.daysUntilDue} days",
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
                label = "All",
                count = financedCount + cashCount + pendingCount,
                selected = selectedFilter == FinanceFilter.ALL,
                color = MaterialTheme.colorScheme.primary,
                onClick = { onFilterSelect(FinanceFilter.ALL) }
            )
        }
        item {
            FinanceFilterChip(
                label = "Financed",
                count = financedCount,
                selected = selectedFilter == FinanceFilter.LOAN,
                color = LoanBlue,
                onClick = { onFilterSelect(FinanceFilter.LOAN) }
            )
        }
        item {
            FinanceFilterChip(
                label = "Cash",
                count = cashCount,
                selected = selectedFilter == FinanceFilter.CASH,
                color = CashGreen,
                onClick = { onFilterSelect(FinanceFilter.CASH) }
            )
        }
        item {
            FinanceFilterChip(
                label = "Not Recorded",
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
    onClick: () -> Unit,
    onRecordEmiClick: () -> Unit,
    onAddPurchaseClick: () -> Unit
) {
    // Only make the card clickable if it's not a pending (not recorded) item
    val isClickable = item.status != FinanceStatus.PENDING

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                FinanceStatus.LOAN -> LoanCardContent(
                    purchase = item.purchase!!,
                    onRecordEmiClick = onRecordEmiClick
                )
                FinanceStatus.CASH -> CashCardContent(purchase = item.purchase!!)
                FinanceStatus.PENDING -> NoInfoCardContent(onAddClick = onAddPurchaseClick)
            }
        }
    }
}

@Composable
private fun StatusChip(status: FinanceStatus) {
    val (color, label) = when (status) {
        FinanceStatus.LOAN -> LoanBlue to "Financed"
        FinanceStatus.CASH -> CashGreen to "Cash"
        FinanceStatus.PENDING -> NoInfoGray to "Not Recorded"
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
    onRecordEmiClick: () -> Unit
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
                    text = "Lender",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = purchase.financierName ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Monthly EMI",
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
                    text = "${purchase.loanProgressPercent.toInt()}% Complete",
                    style = MaterialTheme.typography.labelMedium,
                    color = LoanBlue,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${purchase.emisPaid}/${purchase.tenureMonths} EMIs paid",
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
                    text = "Next EMI: ${getNextEmiDueDate(purchase)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Outstanding: ${formatCurrency(purchase.outstandingBalance)}",
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
                Text("Record EMI", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CashCardContent(purchase: VehiclePurchase) {
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
                    text = "Purchase Price",
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
                    text = "Purchased on ${purchase.purchaseDate}",
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
                        text = "Fully Paid",
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
                    text = "No purchase information",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Add details to track finance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledTonalButton(
                onClick = onAddClick,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Add Info", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Get the next EMI due date for display.
 * Calculates from loanStartDate + emisPaid months to ensure accuracy.
 */
private fun getNextEmiDueDate(purchase: VehiclePurchase): String {
    // Calculate from loanStartDate + emisPaid months
    val loanStartDate = purchase.loanStartDate
    if (!loanStartDate.isNullOrBlank()) {
        val startParsed = FleetDateTime.fromIso8601(loanStartDate)
        if (startParsed != null) {
            val startDateStr = FleetDateTime.formatDate(startParsed)
            val nextDueDateStr = FleetDateTime.addMonths(startDateStr, purchase.emisPaid)
            if (nextDueDateStr != null) {
                return formatDueDateDisplay(nextDueDateStr)
            }
        }
    }

    // Fallback to API-provided nextEmiDueDate
    if (!purchase.nextEmiDueDate.isNullOrBlank()) {
        return formatDueDateDisplay(purchase.nextEmiDueDate)
    }

    return "N/A"
}

/**
 * Format date for display.
 * Converts ISO 8601, YYYY-MM-DD, or DD-MM-YYYY format to "07 Apr 2024" format.
 */
private fun formatDueDateDisplay(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "N/A"

    // Try to parse ISO format first (e.g., "2026-01-15T00:00:00Z")
    val parsed = FleetDateTime.fromIso8601(dateString)
    if (parsed != null) {
        val day = parsed.day.toString().padStart(2, '0')
        val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[parsed.month - 1]
        return "$day $monthName ${parsed.year}"
    }

    val parts = dateString.split("-")
    if (parts.size == 3) {
        return try {
            // Check if YYYY-MM-DD format (year first, 4 digits)
            if (parts[0].length == 4) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month - 1]
                "${day.toString().padStart(2, '0')} $monthName $year"
            } else {
                // DD-MM-YYYY format
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                val monthName = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[month - 1]
                "${day.toString().padStart(2, '0')} $monthName $year"
            }
        } catch (e: Exception) {
            dateString
        }
    }

    return dateString
}
