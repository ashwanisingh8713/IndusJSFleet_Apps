package com.indusjs.fleet.presentation.reports.trip

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.FleetCard
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.core.util.formatCurrency
import com.indusjs.fleet.domain.entity.reports.TripProfitLoss
import com.indusjs.fleet.domain.entity.vehicle.Vehicle
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.Effect
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.Intent
import com.indusjs.fleet.presentation.reports.trip.TripPLContract.State
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Trip Profit/Loss Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripProfitLossScreen(
    viewModel: TripPLViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.ShowSnackbar -> { /* Handle snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip P&L") },
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
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingContent()
            state.error != null -> ErrorContent(
                error = state.error!!,
                onRetry = { viewModel.sendIntent(Intent.Refresh) }
            )
            else -> TripPLContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun TripPLContent(
    state: State,
    viewModel: TripPLViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vehicle Selection
        item {
            VehicleSelectionCard(
                vehicles = state.vehicles,
                selectedVehicleId = state.selectedVehicleId,
                onVehicleSelected = { viewModel.sendIntent(Intent.SelectVehicle(it)) }
            )
        }

        // Generate Report Button
        item {
            Button(
                onClick = { viewModel.sendIntent(Intent.GenerateReport) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedVehicleId != null && !state.isLoading
            ) {
                Text("Generate Trip Report")
            }
        }

        // Results
        if (state.results.isNotEmpty()) {
            items(state.results) { result ->
                TripPLResultCard(result)
            }
        } else if (!state.isLoading && state.selectedVehicleId != null) {
            item {
                Text(
                    text = "No trip data available. Generate a report to see P&L.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun VehicleSelectionCard(
    vehicles: List<Vehicle>,
    selectedVehicleId: String?,
    onVehicleSelected: (String) -> Unit
) {
    FleetCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Vehicle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            vehicles.forEach { vehicle ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = vehicle.id == selectedVehicleId,
                        onClick = { onVehicleSelected(vehicle.id) }
                    )
                    Text(
                        text = vehicle.registrationNumber,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TripPLResultCard(result: TripProfitLoss) {
    FleetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trip #${result.tripId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                result.state?.let { state ->
                    AssistChip(
                        onClick = { },
                        label = { Text(state.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            result.scheduledDate?.let { date ->
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Selling", style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatCurrency(result.sellingValue),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expenses", style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatCurrency(result.totalExpenses),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Net Profit", style = MaterialTheme.typography.bodySmall)
                    Text(
                        formatCurrency(result.netProfit),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (result.isProfitable) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                }
            }

            if (result.startLocation != null && result.endLocation != null) {
                HorizontalDivider()
                Text(
                    text = "${result.startLocation} → ${result.endLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

