package com.indusjs.fleet.presentation.drivers.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetDateField
import com.indusjs.fleet.core.ui.FleetEmailField
import com.indusjs.fleet.core.ui.FleetMobileField
import com.indusjs.fleet.core.ui.FleetStatusBadge
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.ui.caretaker.CaretakerInfoCard
import com.indusjs.fleet.core.ui.caretaker.CaretakerSectionCard
import com.indusjs.fleet.core.ui.convertDdMmYyyyToIso
import com.indusjs.fleet.core.ui.convertIsoToDdMmYyyyRaw
import com.indusjs.fleet.core.ui.history.HistoryTabContent
import com.indusjs.fleet.core.ui.state.StateChangeDialog
import com.indusjs.fleet.core.ui.state.getDriverStateOptions
import com.indusjs.fleet.core.util.formatCostAmount
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.domain.entity.driver.LicenseType
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Driver Detail Screen composable with Edit functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDetailScreen(
    viewModel: DriverDetailViewModel,
    driverId: String,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showStatusDialog by remember { mutableStateOf(false) }

    // Load driver on first composition
    LaunchedEffect(driverId) {
        viewModel.sendIntent(DriverDetailContract.Intent.LoadDriver(driverId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is DriverDetailContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DriverDetailContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
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
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Driver") },
            text = { Text("Are you sure you want to delete ${state.driver?.fullName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.sendIntent(DriverDetailContract.Intent.ConfirmDelete)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Status change dialog - use new StateChangeDialog
    if (showStatusDialog && state.driver != null) {
        StateChangeDialog(
            title = "Change Driver Status",
            currentStateLabel = DriverStatus.getDisplayLabel(state.driver!!.status),
            stateOptions = getDriverStateOptions(state.driver!!.status),
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
                title = { Text(if (state.isEditMode) "Edit Driver" else "Driver Details") },
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
                            contentDescription = if (state.isEditMode) "Cancel" else "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (!state.isEditMode && state.driver != null) {
                        IconButton(onClick = { viewModel.sendIntent(DriverDetailContract.Intent.EnterEditMode) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
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
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.ExitEditMode) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.SaveChanges) },
                            modifier = Modifier.weight(1f),
                            enabled = state.canSave
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (state.isSaving) "Saving..." else "Save Changes")
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(message = "Loading driver details...")
            }
            state.error != null && state.driver == null -> {
                ErrorContent(
                    error = state.error!!,
                    screenContext = FleetErrorContext.DRIVER_DETAIL,
                    onRetry = { viewModel.sendIntent(DriverDetailContract.Intent.Refresh) }
                )
            }
            state.driver != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (state.isEditMode) {
                        // Edit Mode - Full screen form
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item { EditModeContent(state, viewModel) }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    } else {
                        // View Mode with Tabs
                        DriverDetailTabs(
                            state = state,
                            viewModel = viewModel
                        )
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
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Saving...")
                    }
                }
            }
        }
    }
}

/**
 * Tab definitions for Driver Detail Screen
 */
private enum class DriverDetailTab(val title: String, val icon: String) {
    OVERVIEW("Overview", "📋"),
    COSTS("Costs", "💰"),
    HISTORY("History", "📜")
}

/**
 * Tabbed layout for Driver Detail
 */
@Composable
private fun DriverDetailTabs(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    val tabs = DriverDetailTab.entries
    var selectedTab by remember { mutableStateOf(0) }

    // Load history when switching to history tab
    LaunchedEffect(selectedTab) {
        viewModel.sendIntent(DriverDetailContract.Intent.SelectTab(selectedTab))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(tab.icon)
                            Text(
                                text = tab.title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }

        // Tab Content
        when (tabs[selectedTab]) {
            DriverDetailTab.OVERVIEW -> DriverOverviewContent(
                state = state,
                viewModel = viewModel
            )
            DriverDetailTab.COSTS -> DriverCostsTabContent(
                state = state,
                viewModel = viewModel
            )
            DriverDetailTab.HISTORY -> HistoryTabContent(
                items = state.historyItems,
                isLoading = state.isLoadingHistory,
                error = state.historyError,
                hasMore = state.hasMoreHistory,
                onLoadMore = { viewModel.sendIntent(DriverDetailContract.Intent.LoadMoreHistory) },
                onRetry = { viewModel.sendIntent(DriverDetailContract.Intent.LoadHistory) }
            )
        }
    }
}

/**
 * Overview tab content with driver info
 */
@Composable
private fun DriverOverviewContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { DriverHeader(driver = state.driver!!) }

        item { ContactSection(driver = state.driver!!) }

        item { LicenseSection(driver = state.driver!!) }

        item { PersonalSection(driver = state.driver!!) }

        item { MetadataSection(driver = state.driver!!) }

        // Caretaker Assignment Section
        item {
            CaretakerInfoCard(
                caretaker = state.selectedCaretaker,
                onChangeCaretaker = if (state.isEditMode) {
                    { viewModel.sendIntent(DriverDetailContract.Intent.LoadCaretakers) }
                } else null
            )
        }

        // Delete button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { viewModel.sendIntent(DriverDetailContract.Intent.DeleteDriver) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("🗑️ Delete Driver")
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

/**
 * Costs tab content displaying driver earnings, deductions, and costs.
 */
@Composable
private fun DriverCostsTabContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            // Initial loading
            state.isLoadingCosts && state.costs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading costs...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Error state
            state.costsError != null && state.costs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            text = state.costsError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(onClick = { viewModel.sendIntent(DriverDetailContract.Intent.LoadCosts) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            // Empty state
            state.costs.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "💰",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "No costs recorded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Driver cost entries will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content with costs
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Summary Card
                    item {
                        DriverCostsSummaryCard(
                            earnings = state.costsTotalAmount,
                            deductions = state.costsDeductionsAmount,
                            netAmount = state.costsNetAmount,
                            costCount = state.costs.size
                        )
                    }

                    // Filter Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cost Entries",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            OutlinedButton(
                                onClick = { viewModel.sendIntent(DriverDetailContract.Intent.ShowCostsFilterSheet) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("🔍 Filter")
                            }
                        }
                    }

                    // Active Filters Display
                    if (state.costsStartDate.isNotBlank() || state.costsEndDate.isNotBlank() || state.costsMonth.isNotBlank()) {
                        item {
                            ActiveFiltersRow(
                                startDate = state.costsStartDate,
                                endDate = state.costsEndDate,
                                month = state.costsMonth,
                                onClear = { viewModel.sendIntent(DriverDetailContract.Intent.ClearCostFilters) }
                            )
                        }
                    }

                    // Cost Items
                    items(state.costs.size) { index ->
                        val cost = state.costs[index]
                        DriverCostItem(cost = cost)

                        // Load more when reaching the end
                        if (index == state.costs.size - 1 && state.hasMoreCosts && !state.isLoadingCosts) {
                            LaunchedEffect(Unit) {
                                viewModel.sendIntent(DriverDetailContract.Intent.LoadMoreCosts)
                            }
                        }
                    }

                    // Loading more indicator
                    if (state.isLoadingCosts && state.costs.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                    }

                    // Bottom spacing
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // Filter Bottom Sheet
        if (state.showCostsFilterSheet) {
            CostsFilterSheet(
                startDate = state.costsStartDate,
                endDate = state.costsEndDate,
                month = state.costsMonth,
                onApply = { start, end, month ->
                    viewModel.sendIntent(DriverDetailContract.Intent.ApplyCostFilters(start, end, month))
                },
                onDismiss = { viewModel.sendIntent(DriverDetailContract.Intent.HideCostsFilterSheet) }
            )
        }
    }
}

/**
 * Summary card showing earnings, deductions, and net amount.
 */
@Composable
private fun DriverCostsSummaryCard(
    earnings: Double,
    deductions: Double,
    netAmount: Double,
    costCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Amount Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Net Amount",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "₹${formatCostAmount(netAmount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (netAmount >= 0) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "$costCount entries",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            // Earnings and Deductions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "💰 Earnings",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "₹${formatCostAmount(earnings)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📉 Deductions",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "- ₹${formatCostAmount(deductions)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Single driver cost item row.
 */
@Composable
private fun DriverCostItem(cost: com.indusjs.fleet.data.model.driver.DriverCostDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Cost Label
                Text(
                    text = cost.displayLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Date and Description
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📅 ${cost.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!cost.description.isNullOrBlank()) {
                        Text(
                            text = " • ${cost.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                // Month if available
                cost.month?.let { month ->
                    Text(
                        text = "📆 $month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                val isDeduction = cost.isDeductionCost
                Text(
                    text = "${if (isDeduction) "- " else "+ "}₹${formatCostAmount(cost.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDeduction) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isDeduction) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isDeduction) "Deduction" else "Earning",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDeduction) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Active filters display row.
 */
@Composable
private fun ActiveFiltersRow(
    startDate: String,
    endDate: String,
    month: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "🔍",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = when {
                        month.isNotBlank() -> "Month: $month"
                        startDate.isNotBlank() && endDate.isNotBlank() -> "$startDate to $endDate"
                        startDate.isNotBlank() -> "From: $startDate"
                        endDate.isNotBlank() -> "To: $endDate"
                        else -> "Filtered"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            TextButton(onClick = onClear) {
                Text("Clear", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

/**
 * Costs filter bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CostsFilterSheet(
    startDate: String,
    endDate: String,
    month: String,
    onApply: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var localStartDate by remember { mutableStateOf(startDate) }
    var localEndDate by remember { mutableStateOf(endDate) }
    var localMonth by remember { mutableStateOf(month) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Filter Costs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Month Filter
            OutlinedTextField(
                value = localMonth,
                onValueChange = {
                    localMonth = it
                    // Clear date range if month is set
                    if (it.isNotBlank()) {
                        localStartDate = ""
                        localEndDate = ""
                    }
                },
                label = { Text("Month (YYYY-MM)") },
                placeholder = { Text("e.g., 2026-01") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "— OR —",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Date Range
            FleetDateField(
                rawValue = localStartDate,
                onRawValueChange = {
                    localStartDate = it
                    // Clear month if date range is used
                    if (it.isNotBlank()) localMonth = ""
                },
                label = "Start Date",
                placeholder = "DD-MM-YYYY"
            )

            FleetDateField(
                rawValue = localEndDate,
                onRawValueChange = {
                    localEndDate = it
                    if (it.isNotBlank()) localMonth = ""
                },
                label = "End Date",
                placeholder = "DD-MM-YYYY"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        localStartDate = ""
                        localEndDate = ""
                        localMonth = ""
                        onApply("", "", "")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }

                Button(
                    onClick = { onApply(localStartDate, localEndDate, localMonth) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


@Composable
private fun DriverHeader(
    driver: Driver
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${driver.firstName.firstOrNull() ?: ""}${driver.lastName.firstOrNull() ?: ""}".uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name
            Text(
                text = driver.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mobile
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📱",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = driver.mobile,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge
            FleetStatusBadge(
                status = getStatusDisplayName(driver.status),
                color = getStatusColor(driver.status)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickStatItem(
                    icon = "⭐",
                    value = "${driver.rating}",
                    label = "Rating"
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🛣️",
                    value = "${driver.totalTrips}",
                    label = "Trips"
                )

                VerticalDivider(
                    modifier = Modifier.height(40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                QuickStatItem(
                    icon = "🪪",
                    value = driver.licenseType.name,
                    label = "License"
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    icon: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactSection(driver: Driver) {
    SectionCard(title = "📞 Contact Information") {
        InfoRow(label = "Mobile", value = driver.mobile)
        if (driver.email.isNotBlank()) {
            InfoRow(label = "Email", value = driver.email)
        }
        driver.emergencyContact?.let { InfoRow(label = "Emergency Contact", value = it) }
        driver.address?.let { InfoRow(label = "Address", value = it) }
    }
}

@Composable
private fun LicenseSection(driver: Driver) {
    SectionCard(title = "🪪 License Details") {
        InfoRow(label = "License Number", value = driver.licenseNumber)
        InfoRow(label = "License Type", value = getLicenseTypeLabel(driver.licenseType))
        if (driver.licenseExpiry > 0) {
            InfoRow(label = "Expiry Date", value = formatDate(driver.licenseExpiry))
        }
    }
}

@Composable
private fun PersonalSection(driver: Driver) {
    val hasPersonalInfo = driver.dateOfBirth != null || driver.bloodGroup != null || driver.joiningDate != null

    if (hasPersonalInfo) {
        SectionCard(title = "👤 Personal Details") {
            driver.dateOfBirth?.let { InfoRow(label = "Date of Birth", value = formatDate(it)) }
            driver.bloodGroup?.let { InfoRow(label = "Blood Group", value = it) }
            driver.joiningDate?.let { InfoRow(label = "Joining Date", value = formatDate(it)) }
        }
    }
}

@Composable
private fun MetadataSection(driver: Driver) {
    SectionCard(title = "ℹ️ Additional Info") {
        InfoRow(label = "Driver ID", value = "#${driver.id}")
        driver.owner?.let { owner ->
            val ownerName = "${owner.firstName ?: ""} ${owner.lastName ?: ""}".trim()
            if (ownerName.isNotBlank()) {
                InfoRow(label = "Added by", value = ownerName)
            }
        }
        driver.createdAt?.let { InfoRow(label = "Created on", value = formatDate(it)) }
        driver.updatedAt?.let { InfoRow(label = "Last Updated", value = formatDate(it)) }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditModeContent(
    state: DriverDetailContract.State,
    viewModel: DriverDetailViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Basic Info Section
        EditSectionHeader(icon = "👤", title = "Basic Information")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateFirstName(it)) },
                label = { Text("First Name *") },
                isError = state.firstNameError != null,
                supportingText = state.firstNameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = state.lastName,
                onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLastName(it)) },
                label = { Text("Last Name *") },
                isError = state.lastNameError != null,
                supportingText = state.lastNameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        FleetMobileField(
            rawValue = state.mobile,
            onRawValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateMobile(it)) },
            label = "Mobile Number *",
            placeholder = "Enter 10-digit mobile",
            isError = state.mobileError != null,
            errorMessage = state.mobileError
        )

        FleetEmailField(
            value = state.email,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmail(it)) },
            label = "Email",
            isError = state.emailError != null,
            errorMessage = state.emailError
        )

        HorizontalDivider()

        // License Section
        EditSectionHeader(icon = "🪪", title = "License Details")

        // License number (read-only)
        OutlinedTextField(
            value = state.licenseNumber,
            onValueChange = { },
            label = { Text("License Number") },
            leadingIcon = { Text("🪪", modifier = Modifier.padding(start = 12.dp)) },
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // License Type
        Column {
            Text(
                text = "License Type",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LicenseType.entries.forEach { type ->
                    FilterChip(
                        selected = state.licenseType == type,
                        onClick = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseType(type)) },
                        label = { Text(getLicenseTypeLabel(type)) }
                    )
                }
            }
        }

        FleetDateField(
            rawValue = convertIsoToDdMmYyyyRaw(state.licenseExpiry),
            onRawValueChange = {
                val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateLicenseExpiry(isoFormatted))
            },
            label = "License Expiry Date",
            leadingEmoji = "📅"
        )

        HorizontalDivider()

        // Personal Details Section
        EditSectionHeader(icon = "📋", title = "Personal Details")

        FleetDateField(
            rawValue = convertIsoToDdMmYyyyRaw(state.dateOfBirth),
            onRawValueChange = {
                val isoFormatted = if (it.length == 8) convertDdMmYyyyToIso(it) else it
                viewModel.sendIntent(DriverDetailContract.Intent.UpdateDateOfBirth(isoFormatted))
            },
            label = "Date of Birth",
            leadingEmoji = "🎂"
        )

        // Blood Group
        Column {
            Text(
                text = "Blood Group",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.bloodGroupOptions.forEach { group ->
                    FilterChip(
                        selected = state.bloodGroup == group,
                        onClick = {
                            viewModel.sendIntent(DriverDetailContract.Intent.UpdateBloodGroup(
                                if (state.bloodGroup == group) "" else group
                            ))
                        },
                        label = { Text(group) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.address,
            onValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateAddress(it)) },
            label = { Text("Address") },
            leadingIcon = { Text("🏠", modifier = Modifier.padding(start = 12.dp)) },
            singleLine = false,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        FleetMobileField(
            rawValue = state.emergencyContact,
            onRawValueChange = { viewModel.sendIntent(DriverDetailContract.Intent.UpdateEmergencyContact(it)) },
            label = "Emergency Contact",
            placeholder = "Enter 10-digit mobile",
            leadingEmoji = "🆘"
        )

        HorizontalDivider()

        // Caretaker Assignment Section
        CaretakerSectionCard(
            selectedCaretaker = state.selectedCaretaker,
            caretakers = state.caretakers,
            onCaretakerSelected = { viewModel.sendIntent(DriverDetailContract.Intent.SelectCaretaker(it)) },
            onRefresh = { viewModel.sendIntent(DriverDetailContract.Intent.RefreshCaretakers) },
            isLoading = state.isLoadingCaretakers
        )
    }
}

@Composable
private fun StatusChangeDialog(
    currentStatus: DriverStatus,
    onStatusSelected: (DriverStatus) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Status") },
        text = {
            Column {
                DriverStatus.entries.forEach { status ->
                    Surface(
                        onClick = { onStatusSelected(status) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (status == currentStatus)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getStatusDisplayName(status),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (status == currentStatus) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    if (status != DriverStatus.entries.last()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditSectionHeader(
    icon: String,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Helper functions
@Composable
private fun getStatusColor(status: DriverStatus): androidx.compose.ui.graphics.Color {
    val colorScheme = DriverStatus.getColorScheme(status)
    return when (colorScheme) {
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> MaterialTheme.colorScheme.secondary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> MaterialTheme.colorScheme.tertiary
        com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusDisplayName(status: DriverStatus): String =
    DriverStatus.getDisplayLabel(status)

private fun getLicenseTypeLabel(type: LicenseType): String = when (type) {
    LicenseType.LMV -> "LMV"
    LicenseType.HMV -> "HMV"
    LicenseType.MCWG -> "MCWG"
    LicenseType.MCWOG -> "MCWOG"
}

private fun formatDate(timestamp: Long): String {
    if (timestamp <= 0) return "N/A"
    return try {
        val days = timestamp / (24 * 60 * 60 * 1000)
        val years = (days / 365.25).toInt() + 1970
        val remainingDays = (days % 365.25).toInt()
        val months = (remainingDays / 30) + 1
        val dayOfMonth = (remainingDays % 30) + 1
        val monthStr = months.coerceIn(1, 12).toString().padStart(2, '0')
        val dayStr = dayOfMonth.coerceIn(1, 28).toString().padStart(2, '0')
        "$dayStr-$monthStr-$years" // DD-MM-YYYY format
    } catch (_: Exception) {
        "N/A"
    }
}

