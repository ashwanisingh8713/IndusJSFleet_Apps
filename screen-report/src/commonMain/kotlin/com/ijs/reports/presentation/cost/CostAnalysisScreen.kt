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
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.DateVisualTransformation
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.convertToEpochMillis
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
        // Inline date validation (ValidationUtils): only flag once the user has typed
        // a full DD-MM-YYYY value, plus an end-before-start range check.
        val startDateError = if (state.startDate.length == 8 || state.startDate.contains('-')) {
            ValidationUtils.getDateError(formatDateInput(state.startDate))
        } else null
        val rangeError = run {
            if (startDateError != null) return@run null
            if (state.startDate.length != 8 && !state.startDate.contains('-')) return@run null
            if (state.endDate.length != 8 && !state.endDate.contains('-')) return@run null
            if (ValidationUtils.getDateError(formatDateInput(state.endDate)) != null) return@run null
            val startMillis = convertToEpochMillis(state.startDate)
            val endMillis = convertToEpochMillis(state.endDate)
            if (startMillis != null && endMillis != null && endMillis < startMillis) {
                stringResource(Res.string.reports_cost_end_before_start)
            } else null
        }
        val endDateError = when {
            rangeError != null -> rangeError
            state.endDate.length == 8 || state.endDate.contains('-') ->
                ValidationUtils.getDateError(formatDateInput(state.endDate))
            else -> null
        }
        val datesComplete = startDateError == null && endDateError == null &&
            state.startDate.isNotBlank() && state.endDate.isNotBlank()
        val canGenerate = state.selectedCostTypes.isNotEmpty() && datesComplete

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val breakpoint = rememberFleetBreakpoint()
            val contentModifier = if (breakpoint == FleetBreakpoint.Expanded) {
                Modifier
                    .widthIn(max = FleetTokens.Width.MaxContent)
                    .align(Alignment.TopCenter)
            } else {
                Modifier
            }

            LazyColumn(
                modifier = contentModifier
                    .fillMaxSize()
                    .padding(FleetTokens.Spacing.L),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
            ) {
                // Date Range Section
                item {
                    DateRangeSection(
                        startDate = state.startDate,
                        endDate = state.endDate,
                        startDateError = startDateError,
                        endDateError = endDateError,
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
                    FleetButton(
                        text = stringResource(Res.string.reports_analyze_costs),
                        onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canGenerate,
                        isLoading = state.isLoading
                    )
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
                } else if (state.hasGenerated && !state.isLoading && state.error == null) {
                    // Empty State - report ran but returned no rows
                    item {
                        EmptyContent(
                            iconRes = Res.drawable.ic_cost,
                            title = stringResource(Res.string.reports_cost_empty_title),
                            message = stringResource(Res.string.reports_cost_empty_message),
                            fillMaxSize = false
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
}

/** Normalises raw digit input ("25122024") to DD-MM-YYYY for ValidationUtils. */
private fun formatDateInput(raw: String): String =
    if (raw.contains('-')) raw else ValidationUtils.formatDateFromDigits(raw)

@Composable
private fun DateRangeSection(
    startDate: String,
    endDate: String,
    startDateError: String?,
    endDateError: String?,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    val dateVisualTransformation = remember { DateVisualTransformation() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
    ) {
        FleetInputField(
            value = startDate,
            onValueChange = { onStartDateChange(filterDigitsOnly(it, 8)) },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.reports_label_from),
            placeholder = "DD-MM-YYYY",
            visualTransformation = dateVisualTransformation,
            isError = startDateError != null,
            errorMessage = startDateError,
            modifier = Modifier.weight(1f)
        )
        FleetInputField(
            value = endDate,
            onValueChange = { onEndDateChange(filterDigitsOnly(it, 8)) },
            fieldType = FieldType.NUMBER,
            label = stringResource(Res.string.reports_label_to),
            placeholder = "DD-MM-YYYY",
            visualTransformation = dateVisualTransformation,
            isError = endDateError != null,
            errorMessage = endDateError,
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
            Row(horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                TextButton(onClick = onSelectAll) {
                    Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onClearAll) {
                    Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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

    val icon = costTypeIcon(costType)

    val backgroundColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isSelected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier
            .heightIn(min = FleetTokens.Height.MinTouchTarget)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(FleetTokens.Radius.XXL),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = FleetTokens.Spacing.M,
                vertical = FleetTokens.Spacing.S
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(FleetTokens.IconSize.S),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                Icon(
                    painter = painterResource(Res.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.S),
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
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
            Text(
                text = formatCurrency(totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(Res.string.reports_entries_count, totalCount),
                style = MaterialTheme.typography.bodyMedium,
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

    val icon = costTypeIcon(analysis.costType)

    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FleetSectionHeader(
                    title = analysis.displayName,
                    iconRes = icon,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                Text(
                    text = formatCurrency(analysis.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FleetTokens.Height.ProgressBar)
                    .clip(RoundedCornerShape(FleetTokens.Radius.S))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(FleetTokens.Radius.S))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.reports_entries_count, analysis.totalCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatPercentage(percentage),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (analysis.averagePerEntry > 0) {
                Text(
                    text = stringResource(Res.string.reports_cost_avg_per_entry, formatCurrency(analysis.averagePerEntry)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Maps a cost-type code to its vector icon, mirroring the app-wide cost-type icon convention. */
private fun costTypeIcon(costType: String): org.jetbrains.compose.resources.DrawableResource =
    when (costType.lowercase()) {
        "fuel" -> Res.drawable.ic_fuel
        "toll" -> Res.drawable.ic_trip
        "driver_allowance" -> Res.drawable.ic_profile
        "parking" -> Res.drawable.ic_map
        "loading_charges" -> Res.drawable.ic_package
        "unloading_charges" -> Res.drawable.ic_package
        "maintenance" -> Res.drawable.ic_settings
        "chalan" -> Res.drawable.ic_warning
        else -> Res.drawable.ic_cost
    }


