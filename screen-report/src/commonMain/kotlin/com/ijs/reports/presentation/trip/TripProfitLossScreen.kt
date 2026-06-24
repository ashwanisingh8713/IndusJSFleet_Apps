package com.ijs.reports.presentation.trip

import androidx.compose.animation.animateContentSize
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.domain.entity.TripProfitLoss
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus
import com.ijs.reports.presentation.trip.TripPLContract.Effect
import com.ijs.reports.presentation.trip.TripPLContract.Intent
import com.ijs.reports.presentation.trip.TripPLContract.State
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Trip Profit/Loss Screen - Trip-centric design
 * Flow: Select date range → Load trips → Select trips → Generate P&L
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripProfitLossScreen(
    viewModel: TripPLViewModel,
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
                is Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(Res.string.reports_trip_pl_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(Res.string.reports_trip_pl_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (!state.isLoading && state.tripsLoaded) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TripPLContent(
                state = state,
                viewModel = viewModel
            )

            // Loading overlay
            if (state.isLoading || state.isLoadingTrips) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (state.isLoadingTrips) stringResource(Res.string.reports_loading_trips) else stringResource(Res.string.reports_generating),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripPLContent(
    state: State,
    viewModel: TripPLViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        item {
            HeaderCard()
        }

        // Step 1: Date Range Selection
        item {
            DateRangeCard(
                startDate = state.startDate,
                endDate = state.endDate,
                onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) }
            )
        }

        // Load Trips Button
        item {
            OutlinedButton(
                onClick = { viewModel.sendIntent(Intent.LoadTrips) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = state.canLoadTrips && !state.isLoadingTrips,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.tripsLoaded) stringResource(Res.string.reports_reload_trips) else stringResource(Res.string.reports_load_trips),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Step 2: Trip Selection (shown after loading)
        if (state.tripsLoaded) {
            item {
                TripSelectionCard(
                    trips = state.filteredTrips,
                    allTripsCount = state.trips.size,
                    selectedTripIds = state.selectedTripIds,
                    searchQuery = state.tripSearchQuery,
                    onSearchChange = { viewModel.sendIntent(Intent.UpdateTripSearch(it)) },
                    onToggleTrip = { viewModel.sendIntent(Intent.ToggleTrip(it)) },
                    onSelectAll = { viewModel.sendIntent(Intent.SelectAllTrips) },
                    onClearSelection = { viewModel.sendIntent(Intent.ClearSelection) }
                )
            }

            // Generate Report Button
            item {
                Button(
                    onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = state.canGenerateReport && !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("📊", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(Res.string.reports_generate_pl_count, state.selectedCount),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Error message
        if (state.error != null && state.results.isEmpty()) {
            item {
                ErrorCard(error = state.error.resolve())
            }
        }

        // Results
        if (state.results.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 " + stringResource(Res.string.reports_pl_results_count, state.results.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Summary
                    val totalProfit = state.results.sumOf { it.netProfit }
                    val profitColor = com.indusjs.uicomponents.theme.FleetStatusColors.profitLossColor(totalProfit)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = profitColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = stringResource(Res.string.reports_total_profit, formatCurrency(totalProfit)),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = profitColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            items(state.results) { result ->
                TripPLResultCard(result)
            }
        }

        // Bottom spacing
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun HeaderCard() {
    FleetSectionCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚀", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(Res.string.reports_trip_analysis),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.reports_trip_analysis_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DateRangeCard(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    FleetTitledSectionCard(
        title = stringResource(Res.string.reports_step_select_date_range),
        emoji = "📅"
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FleetDateTimePicker(
                date = startDate,
                time = "",
                onDateTimeChange = { newDate, _ -> onStartDateChange(newDate) },
                label = stringResource(Res.string.reports_label_from_date),
                mode = PickerMode.DATE_ONLY,
                modifier = Modifier.fillMaxWidth()
            )
            FleetDateTimePicker(
                date = endDate,
                time = "",
                onDateTimeChange = { newDate, _ -> onEndDateChange(newDate) },
                label = stringResource(Res.string.reports_label_to_date),
                mode = PickerMode.DATE_ONLY,
                minDate = startDate.ifBlank { null },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TripSelectionCard(
    trips: List<Trip>,
    allTripsCount: Int,
    selectedTripIds: Set<String>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleTrip: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit
) {
    FleetSectionCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                FleetSectionHeader(
                    title = stringResource(Res.string.reports_step_select_trips),
                    emoji = "🚀",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${selectedTripIds.size}/$allTripsCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search and bulk actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text(stringResource(Res.string.reports_search_trips_placeholder), style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onSelectAll) {
                    Text(stringResource(Res.string.reports_action_all), style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onClearSelection) {
                    Text(stringResource(Res.string.reports_action_clear), style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trip list
            if (trips.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) stringResource(Res.string.reports_no_trips_match, searchQuery) else stringResource(Res.string.reports_no_trips_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())
                ) {
                    trips.take(100).forEach { trip ->
                        val isSelected = selectedTripIds.contains(trip.id)
                        TripSelectionItem(
                            trip = trip,
                            isSelected = isSelected,
                            onToggle = { onToggleTrip(trip.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (trips.size > 100) {
                        Text(
                            text = stringResource(Res.string.reports_trips_showing_cap, trips.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripSelectionItem(
    trip: Trip,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onToggle() },
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.reports_trip_number, trip.id),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    trip.vehicleNumber?.let {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${trip.startLocation?.address ?: "N/A"} → ${trip.endLocation?.address ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // State badge
            val statusStr = TripStatus.getDisplayLabel(trip.status)
            val colorScheme = TripStatus.getColorScheme(trip.status)
            val stateColor = com.indusjs.uicomponents.components.stateColorSchemeToColor(colorScheme)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = stateColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = statusStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = stateColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(error: String) {
    FleetInlineErrorBanner(
        title = stringResource(Res.string.reports_error_title),
        message = error
    )
}

@Composable
private fun TripPLResultCard(result: TripProfitLoss) {
    // Badge is driven off the backend P&L status (profit / loss / break_even),
    // not the net-profit sign: break_even renders neutral, not as a loss.
    val profitClass = result.profitClass
    val profitColor = when (profitClass) {
        com.ijs.reports.domain.entity.PLProfitClass.PROFIT ->
            com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
        com.ijs.reports.domain.entity.PLProfitClass.LOSS ->
            com.indusjs.uicomponents.theme.FleetStatusColors.LossRed
        com.ijs.reports.domain.entity.PLProfitClass.BREAK_EVEN ->
            com.indusjs.uicomponents.theme.FleetStatusColors.NeutralGray
    }

    FleetSectionCard(
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🚀", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(Res.string.reports_trip_number, result.tripId),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        result.vehicleNumber?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        result.customerName?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                // Profit/Loss Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = profitColor.copy(alpha = 0.15f)
                ) {
                    val badgeText = when (profitClass) {
                        com.ijs.reports.domain.entity.PLProfitClass.PROFIT ->
                            "✅ " + stringResource(Res.string.reports_badge_profit)
                        com.ijs.reports.domain.entity.PLProfitClass.LOSS ->
                            "⚠️ " + stringResource(Res.string.reports_badge_loss)
                        com.ijs.reports.domain.entity.PLProfitClass.BREAK_EVEN ->
                            "➖ " + stringResource(Res.string.profit_status_break_even)
                    }
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = profitColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MiniKPI(
                    label = stringResource(Res.string.reports_revenue),
                    value = formatCurrency(result.sellingValue),
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.ProfitGreen
                )
                MiniKPI(
                    label = stringResource(Res.string.reports_expenses),
                    value = formatCurrency(result.totalExpenses),
                    color = com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber
                )
                MiniKPI(
                    label = stringResource(Res.string.reports_net_profit_label),
                    value = formatCurrency(result.netProfit),
                    color = profitColor
                )
            }

            // Route info
            if (result.startLocation != null && result.endLocation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${result.startLocation} → ${result.endLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Cost Breakdown (if available)
            if (result.costBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "💰 " + stringResource(Res.string.reports_cost_breakdown),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                result.costBreakdown.forEach { cost ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cost.costType.replaceFirstChar { it.uppercaseChar() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(cost.amount),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniKPI(
    label: String,
    value: String,
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
