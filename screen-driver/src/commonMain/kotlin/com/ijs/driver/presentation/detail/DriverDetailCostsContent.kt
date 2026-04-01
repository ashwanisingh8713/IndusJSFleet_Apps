package com.ijs.driver.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.pdfreport.handler.DriverCostsPdfHandler
import com.indusjs.pdfreport.model.DriverCostsPdfData
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.indusjs.datetimepicker.FleetDatePicker
import com.indusjs.datetimeutils.FleetDateTime
import com.indusjs.fleet.core.util.formatCostAmount

@Composable
internal fun DriverCostsTabContent(
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
                            text = state.costsError ?: "An error occurred",
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.sendIntent(DriverDetailContract.Intent.NavigateToAddDriverCost) }
                        ) {
                            Text("Add Driver Cost")
                        }
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
                    // Enhanced Hero Card
                    item {
                        DriverCostsHeroCard(
                            earnings = state.costsTotalAmount,
                            deductions = state.costsDeductionsAmount,
                            netAmount = state.costsNetAmount,
                            entryCount = state.costs.size,
                            categoryCount = state.costCategoryCount,
                            onExportPdf = { viewModel.sendIntent(DriverDetailContract.Intent.ExportCostsToPdf) }
                        )
                    }

                    // Filter and Expand/Collapse Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cost Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Expand/Collapse All
                                if (state.costsByGroup.size > 1) {
                                    val allExpanded = state.expandedGroups.size == state.costsByGroup.size
                                    TextButton(
                                        onClick = {
                                            if (allExpanded) {
                                                viewModel.sendIntent(DriverDetailContract.Intent.CollapseAllCostGroups)
                                            } else {
                                                viewModel.sendIntent(DriverDetailContract.Intent.ExpandAllCostGroups)
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (allExpanded) "Collapse All" else "Expand All",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.sendIntent(DriverDetailContract.Intent.ShowCostsFilterSheet) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("🔍 Filter")
                                }
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

                    // Cost Groups (Collapsible Sections)
                    val groupOrder = listOf("DC-G-001", "DC-G-002", "DC-G-003", "DC-G-004")
                    val groupNames = mapOf(
                        "DC-G-001" to "Salary & Wages",
                        "DC-G-002" to "Incentives & Bonuses",
                        "DC-G-003" to "Deductions",
                        "DC-G-004" to "Other"
                    )
                    val groupIcons = mapOf(
                        "DC-G-001" to "💼",
                        "DC-G-002" to "🏆",
                        "DC-G-003" to "📉",
                        "DC-G-004" to "📋"
                    )

                    groupOrder.forEach { groupId ->
                        val groupCosts = state.costsByGroup[groupId]
                        if (groupCosts != null && groupCosts.isNotEmpty()) {
                            item(key = "group_$groupId") {
                                DriverCostGroupSection(
                                    groupId = groupId,
                                    groupName = groupNames[groupId] ?: "Other",
                                    groupIcon = groupIcons[groupId] ?: "📋",
                                    costs = groupCosts,
                                    totalAmount = state.groupTotals[groupId] ?: 0.0,
                                    isExpanded = state.expandedGroups.contains(groupId),
                                    isDeductionGroup = groupId == "DC-G-003",
                                    onToggle = { viewModel.sendIntent(DriverDetailContract.Intent.ToggleCostGroup(groupId)) },
                                    onTripClick = { tripId ->
                                        viewModel.sendIntent(DriverDetailContract.Intent.NavigateToTripDetail(tripId))
                                    }
                                )
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

        // FAB for adding driver cost (only show when not empty state)
        if (state.costs.isNotEmpty()) {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(DriverDetailContract.Intent.NavigateToAddDriverCost) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = "Add Driver Cost",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Compact summary card for driver costs - consistent with project theme.
 * Shows net amount, earnings, and deductions in a clean, compact layout.
 */
@Composable
internal fun DriverCostsHeroCard(
    earnings: Double,
    deductions: Double,
    netAmount: Double,
    entryCount: Int,
    categoryCount: Int,
    onExportPdf: () -> Unit
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Title and Export Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Cost Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Entry count badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        val entryText = if (entryCount == 1) "entry" else "entries"
                        Text(
                            text = "$entryCount $entryText",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                // Export button
                TextButton(
                    onClick = onExportPdf,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "📄 Export",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Summary Row - Earnings, Deductions, Net Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Earnings Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Earnings",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${formatCostAmount(earnings)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Deductions Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Deductions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "- ₹${formatCostAmount(deductions)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Net Amount Card (highlighted)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Net",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "₹${formatCostAmount(netAmount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Collapsible cost group section.
 */
@Composable
internal fun ActiveFiltersRow(
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
                Icon(
                    painter = painterResource(Res.drawable.ic_filter),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
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
 * Costs filter bottom sheet using FleetDatePicker for date selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CostsFilterSheet(
    startDate: String,
    endDate: String,
    month: String,
    onApply: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var localStartDate by remember { mutableStateOf(startDate) }
    var localEndDate by remember { mutableStateOf(endDate) }
    var localMonth by remember { mutableStateOf(month) }

    // Get today's date for maxDate constraint using FleetDateTime
    val today = remember { FleetDateTime.today() }

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

            // Date Range using FleetDatePicker from ijs-datetime-picker
            FleetDatePicker(
                date = localStartDate,
                onDateChange = {
                    localStartDate = it
                    // Clear month if date range is used
                    if (it.isNotBlank()) localMonth = ""
                },
                label = "Start Date",
                maxDate = today  // Can't select future dates for cost filtering
            )

            FleetDatePicker(
                date = localEndDate,
                onDateChange = {
                    localEndDate = it
                    if (it.isNotBlank()) localMonth = ""
                },
                label = "End Date",
                minDate = localStartDate.takeIf { it.isNotBlank() },  // End date must be after start date
                maxDate = today  // Can't select future dates
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



