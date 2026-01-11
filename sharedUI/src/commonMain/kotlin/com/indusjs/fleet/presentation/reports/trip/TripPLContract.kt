package com.indusjs.fleet.presentation.reports.trip

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.reports.TripProfitLoss
import com.indusjs.fleet.domain.entity.trip.Trip

/**
 * MVI Contract for Trip P&L Screen
 * Trip-centric design: Select date range, then select trips to analyze
 */
object TripPLContract {

    data class State(
        val isLoading: Boolean = false,
        val isLoadingTrips: Boolean = false,
        val error: String? = null,
        // Date range for filtering trips
        val startDate: String = "",
        val endDate: String = "",
        // Trip listing and selection
        val trips: List<Trip> = emptyList(),
        val selectedTripIds: Set<String> = emptySet(),
        val tripSearchQuery: String = "",
        // Results
        val results: List<TripProfitLoss> = emptyList(),
        // UI state
        val tripsLoaded: Boolean = false
    ) : UiState {
        val hasResults: Boolean get() = results.isNotEmpty()

        // Filtered trips based on search query
        val filteredTrips: List<Trip> get() {
            if (tripSearchQuery.isBlank()) return trips
            val query = tripSearchQuery.lowercase()
            return trips.filter { trip ->
                trip.id.lowercase().contains(query) ||
                (trip.startLocation?.address?.lowercase()?.contains(query) == true) ||
                (trip.endLocation?.address?.lowercase()?.contains(query) == true) ||
                (trip.vehicleNumber?.lowercase()?.contains(query) == true) ||
                (trip.customerName?.lowercase()?.contains(query) == true)
            }
        }

        // Computed properties
        val canLoadTrips: Boolean get() = startDate.isNotBlank() && endDate.isNotBlank()
        val canGenerateReport: Boolean get() = selectedTripIds.isNotEmpty()
        val selectedCount: Int get() = selectedTripIds.size
    }

    sealed interface Intent : UiIntent {
        // Date range
        data class UpdateStartDate(val date: String) : Intent
        data class UpdateEndDate(val date: String) : Intent
        // Load trips based on date range
        data object LoadTrips : Intent
        // Trip selection
        data class ToggleTrip(val tripId: String) : Intent
        data object SelectAllTrips : Intent
        data object ClearSelection : Intent
        // Search
        data class UpdateTripSearch(val query: String) : Intent
        // Generate report
        data object GenerateReport : Intent
        data object Refresh : Intent
    }

    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
    }
}

