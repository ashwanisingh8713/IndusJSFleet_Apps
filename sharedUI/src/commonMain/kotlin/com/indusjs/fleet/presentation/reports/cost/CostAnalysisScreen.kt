package com.indusjs.fleet.presentation.reports.cost

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetDateField
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.indusjs.fleet.domain.entity.reports.CostTypeAnalysis
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.COST_TYPES
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.Effect
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.Intent
import com.indusjs.fleet.presentation.reports.cost.CostAnalysisContract.State
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Cost Analysis Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostAnalysisScreen(
    viewModel: CostAnalysisViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = SnackbarHostState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cost Analysis") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Section
            item {
                DateRangeSection(
                    startDate = state.startDate,
                    endDate = state.endDate,
                    onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                    onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) }
                )
            }

            // Cost Type Selection
            item {
                CostTypeSelectionSection(
                    selectedTypes = state.selectedCostTypes,
                    onToggle = { viewModel.sendIntent(Intent.ToggleCostType(it)) },
                    onSelectAll = { viewModel.sendIntent(Intent.SelectAllCostTypes) },
                    onClearAll = { viewModel.sendIntent(Intent.ClearCostTypes) }
                )
            }

            // Generate Report Button
            item {
                Button(
                    onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.selectedCostTypes.isNotEmpty() && !state.isLoading,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Analyze Costs")
                }
            }

            // Results
            if (state.hasResults) {
                // Summary Card
                item {
                    TotalCostCard(
                        totalAmount = state.totalAmount,
                        totalCount = state.totalCount
                    )
                }

                // Cost Breakdown Header
                item {
                    Text(
                        text = "Cost Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Cost Type Cards
                items(state.results) { analysis ->
                    CostTypeCard(
                        analysis = analysis,
                        totalAmount = state.totalAmount
                    )
                }
            }

            // Error State
            state.error?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateRangeSection(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FleetDateField(
            rawValue = startDate,
            onRawValueChange = onStartDateChange,
            label = "From",
            modifier = Modifier.weight(1f)
        )
        FleetDateField(
            rawValue = endDate,
            onRawValueChange = onEndDateChange,
            label = "To",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CostTypeSelectionSection(
    selectedTypes: Set<String>,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select Cost Types (${selectedTypes.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSelectAll) {
                    Text("All", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onClearAll) {
                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(COST_TYPES) { (costType, displayName) ->
                CostTypeChip(
                    costType = costType,
                    displayName = displayName,
                    isSelected = selectedTypes.contains(costType),
                    onClick = { onToggle(costType) }
                )
            }
        }
    }
}

@Composable
private fun CostTypeChip(
    costType: String,
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    val icon = when (costType) {
        "fuel" -> "⛽"
        "toll" -> "🛣️"
        "driver_allowance" -> "👤"
        "parking" -> "🅿️"
        "loading_charges" -> "📦"
        "unloading_charges" -> "📤"
        "maintenance" -> "🔧"
        "insurance" -> "🛡️"
        "permit" -> "📄"
        "chalan" -> "📋"
        else -> "💰"
    }

    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
private fun TotalCostCard(
    totalAmount: Double,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Expenses",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatCurrency(totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "$totalCount entries",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CostTypeCard(
    analysis: CostTypeAnalysis,
    totalAmount: Double
) {
    val percentage = if (totalAmount > 0) (analysis.totalAmount / totalAmount) * 100 else 0.0
    val progressFraction = (percentage / 100).toFloat().coerceIn(0f, 1f)

    val icon = when (analysis.costType) {
        "fuel" -> "⛽"
        "toll" -> "🛣️"
        "driver_allowance" -> "👤"
        "parking" -> "🅿️"
        "loading_charges" -> "📦"
        "unloading_charges" -> "📤"
        "maintenance" -> "🔧"
        "insurance" -> "🛡️"
        "permit" -> "📄"
        "chalan" -> "📋"
        else -> "💰"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = analysis.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = formatCurrency(analysis.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${analysis.totalCount} entries",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatPercentage(percentage),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (analysis.averagePerEntry > 0) {
                Text(
                    text = "Avg: ${formatCurrency(analysis.averagePerEntry)}/entry",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


