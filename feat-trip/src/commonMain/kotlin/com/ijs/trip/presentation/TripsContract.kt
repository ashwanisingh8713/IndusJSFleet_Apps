package com.ijs.trip.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.ijs.trip.domain.entity.Trip
import com.ijs.trip.domain.entity.TripStatus

/**
 * MVI Contract for the Trips List screen.
 */
object TripsContract {

    /**
     * UI State for the Trips screen.
     * Note: Cost summary is now embedded in Trip entity (trip.totalCost)
     * so we don't need separate tripCostSummaries map.
     */
    data class State(
        val isLoading: Boolean = false,
        val trips: List<Trip> = emptyList(),
        val filteredTrips: List<Trip> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
        val selectedStatusFilter: TripStatus? = null,
        val isRefreshing: Boolean = false
    ) : UiState

    /**
     * User intents for the Trips screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadTrips : Intent
        data object RefreshTrips : Intent
        data class SearchTrips(val query: String) : Intent
        data class FilterByStatus(val status: TripStatus?) : Intent
        data class SelectTrip(val tripId: String) : Intent
        data class CancelTrip(val tripId: String) : Intent
        data object CreateTrip : Intent
        data object ClearFilters : Intent
    }

    /**
     * Side effects for the Trips screen.
     */
    sealed interface Effect : UiEffect {
        data class NavigateToTripDetail(val tripId: String) : Effect
        data object NavigateToCreateTrip : Effect
        data class ShowSnackbar(val message: String) : Effect
    }
}
