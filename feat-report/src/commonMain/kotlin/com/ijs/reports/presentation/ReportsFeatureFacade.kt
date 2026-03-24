package com.ijs.reports.presentation

import androidx.compose.runtime.Composable
import com.ijs.reports.presentation.consolidated.ConsolidatedPLScreen
import com.ijs.reports.presentation.consolidated.ConsolidatedPLViewModel
import com.ijs.reports.presentation.cost.CostAnalysisScreen
import com.ijs.reports.presentation.cost.CostAnalysisViewModel
import com.ijs.reports.presentation.trip.TripPLViewModel
import com.ijs.reports.presentation.trip.TripProfitLossScreen
import com.ijs.reports.presentation.vehicle.VehiclePLViewModel
import com.ijs.reports.presentation.vehicle.VehicleProfitLossScreen

/**
 * Facade for the Reports feature module.
 */
object ReportsFeatureFacade {

    @Composable
    fun ReportsHubEntry(
        viewModel: ReportsViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToVehiclePL: () -> Unit,
        onNavigateToTripPL: () -> Unit,
        onNavigateToCostAnalysis: () -> Unit,
        onNavigateToConsolidatedPL: () -> Unit
    ) {
        ReportsScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToVehiclePL = onNavigateToVehiclePL,
            onNavigateToTripPL = onNavigateToTripPL,
            onNavigateToCostAnalysis = onNavigateToCostAnalysis,
            onNavigateToConsolidatedPL = onNavigateToConsolidatedPL
        )
    }

    @Composable
    fun VehicleProfitLossEntry(
        viewModel: VehiclePLViewModel,
        onNavigateBack: () -> Unit
    ) {
        VehicleProfitLossScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }

    @Composable
    fun TripProfitLossEntry(
        viewModel: TripPLViewModel,
        onNavigateBack: () -> Unit
    ) {
        TripProfitLossScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }

    @Composable
    fun CostAnalysisEntry(
        viewModel: CostAnalysisViewModel,
        onNavigateBack: () -> Unit
    ) {
        CostAnalysisScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }

    @Composable
    fun ConsolidatedPLEntry(
        viewModel: ConsolidatedPLViewModel,
        onNavigateBack: () -> Unit
    ) {
        ConsolidatedPLScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

