package com.indusjs.fleet.presentation.finance

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.EmptyContent
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.finance.*
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Effect
import com.indusjs.fleet.presentation.finance.VehicleFinanceContract.Intent
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

// Colors
private val LoanBlue = Color(0xFF3B82F6)
private val CashGreen = Color(0xFF10B981)
private val NoInfoGray = Color(0xFF9CA3AF)
private val WarningOrange = Color(0xFFF59E0B)
private val CriticalRed = Color(0xFFEF4444)

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

        // Filter Chips
        item {
            FilterChipsRow(
                selectedFilter = state.selectedFilter,
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
                    Text("🔍", style = MaterialTheme.typography.bodyMedium)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📊 Fleet Finance Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Top row - Vehicle counts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = totalVehicles.toString(),
                    label = "Total\nVehicles",
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    value = financedVehicles.toString(),
                    label = "Active\nLoans",
                    color = LoanBlue
                )
                SummaryItem(
                    value = formatCurrency(monthlyEmiTotal),
                    label = "Monthly\nEMI",
                    color = WarningOrange
                )
            }

            HorizontalDivider()

            // Bottom row - Financial summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    value = formatCurrency(totalPaid),
                    label = "Total\nPaid",
                    color = CashGreen
                )
                SummaryItem(
                    value = formatCurrency(totalOutstanding),
                    label = "Outstanding\nBalance",
                    color = CriticalRed
                )
                SummaryItem(
                    value = cashVehicles.toString(),
                    label = "Cash\nPurchase",
                    color = CashGreen
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
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
    onFilterSelect: (FinanceFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FinanceFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelect(filter) },
                label = {
                    Text(filter.label)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (filter) {
                        FinanceFilter.LOAN -> LoanBlue.copy(alpha = 0.2f)
                        FinanceFilter.CASH -> CashGreen.copy(alpha = 0.2f)
                        FinanceFilter.PENDING -> NoInfoGray.copy(alpha = 0.2f)
                    }
                )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚛", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.vehicle.registrationNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.vehicle.make} ${item.vehicle.model}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status chip
                StatusChip(status = item.status)
            }

            HorizontalDivider()

            // Content based on status
            when (item.status) {
                FinanceStatus.LOAN -> LoanContent(
                    purchase = item.purchase!!,
                    onRecordEmiClick = onRecordEmiClick
                )
                FinanceStatus.CASH -> CashContent(purchase = item.purchase!!)
                FinanceStatus.PENDING -> NoInfoContent(onAddClick = onAddPurchaseClick)
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
private fun LoanContent(
    purchase: VehiclePurchase,
    onRecordEmiClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Lender and EMI info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Lender: ${purchase.financierName ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "EMI: ${formatCurrency(purchase.emiAmount)}/month",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = LoanBlue
            )
        }

        // Progress bar
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${purchase.emisPaid}/${purchase.tenureMonths} EMIs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { purchase.loanProgressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = LoanBlue,
                trackColor = LoanBlue.copy(alpha = 0.2f)
            )
        }

        // Next EMI and Outstanding
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Next EMI: ${purchase.nextEmiDueDate ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Outstanding: ${formatCurrency(purchase.outstandingBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CriticalRed
                )
            }

            FilledTonalButton(
                onClick = onRecordEmiClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Record EMI", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun CashContent(purchase: VehiclePurchase) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Purchase: ${formatCurrency(purchase.purchasePrice)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Date: ${purchase.purchaseDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✅", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Fully Paid",
                style = MaterialTheme.typography.bodyMedium,
                color = CashGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NoInfoContent(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚪", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "No purchase information added",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TextButton(onClick = onAddClick) {
            Text("Add Info →")
        }
    }
}
