package com.ijs.reports.presentation.trip

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.ijs.reports.data.model.MultiTripPLRequest
import com.ijs.reports.domain.repository.ReportsRepository
import com.ijs.trip.domain.repository.TripRepository
import com.ijs.reports.presentation.trip.TripPLContract.Effect
import com.ijs.reports.presentation.trip.TripPLContract.Intent
import com.ijs.reports.presentation.trip.TripPLContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for Trip P&L Screen
 * Trip-centric: Select date range -> Load trips -> Select trips -> Generate P&L
 */
@Inject
class TripPLViewModel(
    private val reportsRepository: ReportsRepository,
    private val tripRepository: TripRepository
) : MviViewModel<State, Intent, Effect>(State()) {

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdateStartDate -> updateState { copy(startDate = intent.date) }
            is Intent.UpdateEndDate -> updateState { copy(endDate = intent.date) }
            is Intent.LoadTrips -> loadTrips()
            is Intent.ToggleTrip -> toggleTrip(intent.tripId)
            is Intent.SelectAllTrips -> updateState {
                copy(selectedTripIds = filteredTrips.map { it.id }.toSet())
            }
            is Intent.ClearSelection -> updateState { copy(selectedTripIds = emptySet()) }
            is Intent.UpdateTripSearch -> updateState { copy(tripSearchQuery = intent.query) }
            is Intent.GenerateReport -> generateReport()
            is Intent.Refresh -> {
                if (state.value.tripsLoaded) {
                    loadTrips()
                }
            }
            // Sorting and filtering
            is Intent.UpdateSortOption -> updateState { copy(sortOption = intent.option) }
            is Intent.UpdatePLStatusFilter -> updateState { copy(plStatusFilter = intent.filter) }
        }
    }

    private fun toggleTrip(tripId: String) {
        updateState {
            val newSelection = if (selectedTripIds.contains(tripId)) {
                selectedTripIds - tripId
            } else {
                selectedTripIds + tripId
            }
            copy(selectedTripIds = newSelection)
        }
    }

    private suspend fun loadTrips() {
        val currentState = state.value

        if (currentState.startDate.isBlank() || currentState.endDate.isBlank()) {
            sendEffect(Effect.ShowSnackbar("Please select both start and end dates"))
            return
        }

        updateState { copy(isLoadingTrips = true, error = null, trips = emptyList(), selectedTripIds = emptySet(), tripsLoaded = false) }

        tripRepository.getTrips().collectLatest { result ->
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val allTrips = result.data as? List<com.ijs.trip.domain.entity.Trip> ?: emptyList()

                    // For now, include all trips
                    // TODO: Add server-side date filtering when API supports it
                    updateState {
                        copy(
                            isLoadingTrips = false,
                            trips = allTrips,
                            tripsLoaded = true
                        )
                    }

                    if (allTrips.isEmpty()) {
                        sendEffect(Effect.ShowSnackbar("No trips found"))
                    }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingTrips = false, tripsLoaded = false) }
                    sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to load trips"))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun generateReport() {
        val currentState = state.value

        if (currentState.selectedTripIds.isEmpty()) {
            sendEffect(Effect.ShowSnackbar("Please select at least one trip"))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val tripIds = currentState.selectedTripIds.mapNotNull { it.toIntOrNull() }
        if (tripIds.isEmpty()) {
            updateState { copy(isLoading = false, error = "No valid trip IDs selected") }
            return
        }

        val request = MultiTripPLRequest(
            tripIds = tripIds,
            startDate = currentState.startDate.takeIf { it.isNotBlank() },
            endDate = currentState.endDate.takeIf { it.isNotBlank() }
        )

        when (val result = reportsRepository.getMultiTripPL(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, results = result.data) }
                if (result.data.isEmpty()) {
                    sendEffect(Effect.ShowSnackbar("No P&L data available for selected trips"))
                }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message ?: "Failed to generate report") }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}

