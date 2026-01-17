package com.indusjs.fleet.presentation.reports.trip

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
import com.indusjs.fleet.domain.entity.reports.TripProfitLoss
import com.indusjs.fleet.domain.entity.trip.Trip
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.Effect
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.Intent
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.State
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
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
                            text = "Trip P&L Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Profit & Loss by Trip",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (!state.isLoading && state.tripsLoaded) {
                        IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = "Refresh",
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
                                text = if (state.isLoadingTrips) "Loading Trips..." else "Generating Report...",
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
                Text("🔍", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.tripsLoaded) "Reload Trips" else "Load Trips",
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
                        text = "Generate Report (${state.selectedCount} trips)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Error message
        if (state.error != null && state.results.isEmpty()) {
            item {
                ErrorCard(error = state.error)
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
                        text = "📈 P&L Results (${state.results.size} trips)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    // Summary
                    val totalProfit = state.results.sumOf { it.netProfit }
                    val profitColor = if (totalProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = profitColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Total: ${formatCurrency(totalProfit)}",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = "Trip Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select date range → Load trips → Generate P&L",
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📅", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Step 1: Select Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetDateTimePicker(
                    date = startDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onStartDateChange(newDate) },
                    label = "From Date",
                    mode = PickerMode.DATE_ONLY,
                    modifier = Modifier.fillMaxWidth()
                )
                FleetDateTimePicker(
                    date = endDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onEndDateChange(newDate) },
                    label = "To Date",
                    mode = PickerMode.DATE_ONLY,
                    minDate = startDate.ifBlank { null },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Step 2: Select Trips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
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
                    placeholder = { Text("Search trips...", style = MaterialTheme.typography.bodySmall) },
                    leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 4.dp)) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onSelectAll) {
                    Text("All", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = onClearSelection) {
                    Text("Clear", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trip list
            if (trips.isEmpty()) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No trips match \"$searchQuery\"" else "No trips found",
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
                            text = "Showing first 100 of ${trips.size} trips. Use search to filter.",
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
                        text = "Trip #${trip.id}",
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
            val stateColor = when (colorScheme) {
                com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.SUCCESS -> Color(0xFF10B981)
                com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.INFO -> Color(0xFFF59E0B)
                com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.WARNING -> Color(0xFF3B82F6)
                com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.ERROR -> Color(0xFFEF4444)
                com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
            }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun TripPLResultCard(result: TripProfitLoss) {
    val isProfit = result.isProfitable
    val profitColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            text = "Trip #${result.tripId}",
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
                    }
                }
                // Profit/Loss Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = profitColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isProfit) "✅ Profit" else "⚠️ Loss",
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
                    label = "Revenue",
                    value = formatCurrency(result.sellingValue),
                    color = Color(0xFF10B981)
                )
                MiniKPI(
                    label = "Expenses",
                    value = formatCurrency(result.totalExpenses),
                    color = Color(0xFFF59E0B)
                )
                MiniKPI(
                    label = "Net Profit",
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
                    text = "💰 Cost Breakdown",
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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
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
