package com.ijs.trip.presentation

import androidx.compose.runtime.Composable
import com.ijs.trip.presentation.cost.TripCostEntryScreen
import com.ijs.trip.presentation.cost.TripCostEntryViewModel
import com.ijs.trip.presentation.create.CreateTripScreen
import com.ijs.trip.presentation.create.CreateTripViewModel
import com.ijs.trip.presentation.detail.TripDetailScreen
import com.ijs.trip.presentation.detail.TripDetailViewModel

/**
 * Facade for the Trip feature module.
 */
object TripFeatureFacade {

    @Composable
    fun TripsListEntry(
        viewModel: TripsViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToDetail: (String) -> Unit,
        onNavigateToCreate: () -> Unit
    ) {
        TripsScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToCreate = onNavigateToCreate
        )
    }

    @Composable
    fun CreateTripEntry(
        viewModel: CreateTripViewModel,
        onNavigateBack: () -> Unit,
        onTripCreated: (String) -> Unit,
        onNavigateToAddCustomer: () -> Unit
    ) {
        CreateTripScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onTripCreated = onTripCreated,
            onNavigateToAddCustomer = onNavigateToAddCustomer
        )
    }

    @Composable
    fun TripDetailEntry(
        viewModel: TripDetailViewModel,
        tripId: String,
        onNavigateBack: () -> Unit,
        onNavigateToAddTripCost: (tripId: String, vehicleId: String) -> Unit,
        onNavigateToAddPayment: (tripId: String, vehicleId: String) -> Unit,
        onNavigateToAddCustomer: () -> Unit
    ) {
        TripDetailScreen(
            viewModel = viewModel,
            tripId = tripId,
            onNavigateBack = onNavigateBack,
            onNavigateToAddTripCost = onNavigateToAddTripCost,
            onNavigateToAddPayment = onNavigateToAddPayment,
            onNavigateToAddCustomer = onNavigateToAddCustomer
        )
    }

    @Composable
    fun TripCostEntryEntry(
        viewModel: TripCostEntryViewModel,
        initialTripId: String? = null,
        onNavigateBack: () -> Unit
    ) {
        TripCostEntryScreen(
            viewModel = viewModel,
            initialTripId = initialTripId,
            onNavigateBack = onNavigateBack
        )
    }
}

