package com.ijs.reports.presentation.cost

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.ijs.reports.domain.entity.CostTypeAnalysis
import com.ijs.reports.presentation.cost.CostAnalysisContract.COST_TYPES
import com.ijs.reports.presentation.cost.CostAnalysisContract.Effect
import com.ijs.reports.presentation.cost.CostAnalysisContract.Intent
import com.ijs.reports.presentation.cost.CostAnalysisContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    val snackbarHostState = remember { SnackbarHostState() }
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
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.reports_cost_analysis)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
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
                    Text(stringResource(Res.string.reports_analyze_costs))
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
                        text = stringResource(Res.string.reports_cost_breakdown),
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
                    FleetInlineErrorBanner(message = error.resolve())
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
    val dateVisualTransformation = remember { DateVisualTransformation() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FleetInputField(
            value = startDate,
            onValueChange = { onStartDateChange(filterDigitsOnly(it, 8)) },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.reports_label_from),
            placeholder = "DD-MM-YYYY",
            visualTransformation = dateVisualTransformation,
            modifier = Modifier.weight(1f)
        )
        FleetInputField(
            value = endDate,
            onValueChange = { onEndDateChange(filterDigitsOnly(it, 8)) },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.reports_label_to),
            placeholder = "DD-MM-YYYY",
            visualTransformation = dateVisualTransformation,
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
                text = stringResource(Res.string.reports_select_cost_types_count, selectedTypes.size),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onSelectAll) {
                    Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onClearAll) {
                    Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelSmall)
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
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        border = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.reports_cost_total_expenses_caps),
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
                text = stringResource(Res.string.reports_entries_count, totalCount),
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

    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FleetSectionHeader(
                    title = analysis.displayName,
                    emoji = icon,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
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
                    text = stringResource(Res.string.reports_entries_count, analysis.totalCount),
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
                    text = stringResource(Res.string.reports_cost_avg_per_entry, formatCurrency(analysis.averagePerEntry)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


