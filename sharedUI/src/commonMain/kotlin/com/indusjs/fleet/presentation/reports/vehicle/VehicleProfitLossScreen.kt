package com.indusjs.fleet.presentation.reports.vehicle

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetDateFieldCompact
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.core.util.formatPercentage
import com.indusjs.fleet.domain.entity.reports.VehicleProfitLoss
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.Effect
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.Intent
import com.indusjs.fleet.presentation.reports.vehicle.VehiclePLContract.State
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Vehicle Profit/Loss Screen - Enhanced UI with searchable dropdown for scalability
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfitLossScreen(
    viewModel: VehiclePLViewModel,
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
                            text = "Vehicle P&L Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Profit & Loss Analysis",
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
                    if (!state.isLoading) {
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
            VehiclePLContent(
                state = state,
                viewModel = viewModel
            )

            // Loading overlay
            if (state.isLoading) {
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
                            Text("Generating Report...", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehiclePLContent(
    state: State,
    viewModel: VehiclePLViewModel
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

        // Searchable Vehicle Selection Card
        item {
            SearchableVehicleCard(
                vehicles = state.filteredVehicles,
                allVehiclesCount = state.vehicles.size,
                selectedVehicle = state.selectedVehicle,
                searchQuery = state.vehicleSearchQuery,
                showDropdown = state.showVehicleDropdown,
                isLoading = state.isLoadingVehicles,
                onSearchChange = { viewModel.sendIntent(Intent.UpdateVehicleSearch(it)) },
                onVehicleSelected = { viewModel.sendIntent(Intent.SelectVehicle(it)) },
                onToggleDropdown = { viewModel.sendIntent(Intent.ToggleVehicleDropdown) },
                onClearSearch = { viewModel.sendIntent(Intent.ClearVehicleSearch) }
            )
        }

        // Period Selection with Date Range
        item {
            PeriodAndDateRangeCard(
                selectedPeriod = state.period,
                useCustomDateRange = state.useCustomDateRange,
                startDate = state.startDate,
                endDate = state.endDate,
                onPeriodChange = { viewModel.sendIntent(Intent.UpdatePeriod(it)) },
                onStartDateChange = { viewModel.sendIntent(Intent.UpdateStartDate(it)) },
                onEndDateChange = { viewModel.sendIntent(Intent.UpdateEndDate(it)) }
            )
        }

        // Generate Report Button
        item {
            Button(
                onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = state.selectedVehicleId != null && !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📊", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Generate Report",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // Error message
        if (state.error != null && state.result == null) {
            item {
                ErrorCard(error = state.error)
            }
        }

        // Results
        state.result?.let { result ->
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📈 Report Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                VehiclePLResultCard(result)
            }
        }

        if (state.multiResults.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📈 Report Results (${state.multiResults.size} Vehicles)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(state.multiResults) { result ->
                VehiclePLResultCard(result)
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
                    Text("🚛", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Vehicle Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Search and select a vehicle to view profit/loss",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchableVehicleCard(
    vehicles: List<Vehicle>,
    allVehiclesCount: Int,
    selectedVehicle: Vehicle?,
    searchQuery: String,
    showDropdown: Boolean,
    isLoading: Boolean,
    onSearchChange: (String) -> Unit,
    onVehicleSelected: (String) -> Unit,
    onToggleDropdown: () -> Unit,
    onClearSearch: () -> Unit
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
                Text("🚚", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Vehicle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "$allVehiclesCount vehicles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected vehicle display or search field
            if (selectedVehicle != null && searchQuery.isBlank()) {
                // Show selected vehicle
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleDropdown() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedVehicle.registrationNumber,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${selectedVehicle.make ?: ""} ${selectedVehicle.model ?: ""}".trim().ifEmpty { "Vehicle" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text("✏️", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                // Search input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    label = { Text("Search by registration, make, model...") },
                    placeholder = { Text("Type to search...") },
                    leadingIcon = { Text("🔍", modifier = Modifier.padding(start = 8.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = onClearSearch) {
                                Text("✖️")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Dropdown with filtered results
            if (showDropdown && vehicles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 250.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        vehicles.take(50).forEachIndexed { index, vehicle ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onVehicleSelected(vehicle.id) },
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = vehicle.registrationNumber,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}".trim().ifEmpty { "Vehicle" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            if (index < vehicles.size - 1 && index < 49) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                        if (vehicles.size > 50) {
                            Text(
                                text = "... and ${vehicles.size - 50} more. Refine your search.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            } else if (showDropdown && searchQuery.isNotBlank() && vehicles.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No vehicles found matching \"$searchQuery\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeriodAndDateRangeCard(
    selectedPeriod: String,
    useCustomDateRange: Boolean,
    startDate: String,
    endDate: String,
    onPeriodChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    val periods = listOf(
        "today" to "Today",
        "weekly" to "Week",
        "monthly" to "Month",
        "yearly" to "Year",
        "custom" to "Custom"
    )

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
                    text = "Select Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periods.forEach { (value, label) ->
                    val isSelected = selectedPeriod == value
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPeriodChange(value) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Custom date range inputs
            if (useCustomDateRange) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "📆 Custom Date Range",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FleetDateFieldCompact(
                        rawValue = startDate,
                        onRawValueChange = onStartDateChange,
                        label = "From Date",
                        modifier = Modifier.weight(1f)
                    )
                    FleetDateFieldCompact(
                        rawValue = endDate,
                        onRawValueChange = onEndDateChange,
                        label = "To Date",
                        modifier = Modifier.weight(1f)
                    )
                }
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
private fun VehiclePLResultCard(result: VehicleProfitLoss) {
    val isProfit = result.isProfitable
    val profitColor = if (isProfit) Color(0xFF10B981) else Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🚛", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = result.vehicleNumber ?: "Vehicle #${result.vehicleId}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        result.period?.let {
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
                    shape = RoundedCornerShape(20.dp),
                    color = profitColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isProfit) "✅ Profit" else "⚠️ Loss",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = profitColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main KPIs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                KPIColumn(
                    label = "Revenue",
                    value = formatCurrency(result.totalRevenue),
                    color = Color(0xFF10B981),
                    icon = "📈"
                )
                KPIColumn(
                    label = "Expenses",
                    value = formatCurrency(result.totalExpenses),
                    color = Color(0xFFF59E0B),
                    icon = "💸"
                )
                KPIColumn(
                    label = "Net Profit",
                    value = formatCurrency(result.netProfit),
                    color = profitColor,
                    icon = if (isProfit) "💰" else "📉"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = "🚀",
                    label = "Total Trips",
                    value = result.totalTrips.toString()
                )
                StatItem(
                    icon = "✅",
                    label = "Completed",
                    value = result.completedTrips.toString()
                )
                StatItem(
                    icon = "📊",
                    label = "Margin",
                    value = formatPercentage(result.profitMargin)
                )
            }

            // Cost Breakdown (if available)
            if (result.costBreakdown.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "💰 Cost Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                result.costBreakdown.forEach { cost ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cost.costType.replaceFirstChar { it.uppercaseChar() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatCurrency(cost.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KPIColumn(
    label: String,
    value: String,
    color: Color,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
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
private fun StatItem(
    icon: String,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.labelLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
