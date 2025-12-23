package com.indusjs.fleet.presentation.trips

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.result.Result
import com.indusjs.fleet.domain.entity.trip.TripStatus
import com.indusjs.fleet.domain.usecase.trip.CancelTripUseCase
import com.indusjs.fleet.domain.usecase.trip.GetTripsUseCase
import com.indusjs.fleet.presentation.trips.TripsContract.Effect
import com.indusjs.fleet.presentation.trips.TripsContract.Intent
import com.indusjs.fleet.presentation.trips.TripsContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Trips screen implementing MVI pattern.
 *
 * Dependencies are injected via Metro DI through the TripsFeatureGraph.
 */
@Inject
class TripsViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val getTripsUseCase: GetTripsUseCase,
    private val cancelTripUseCase: CancelTripUseCase
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadTrips)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadTrips -> loadTrips()
            is Intent.RefreshTrips -> refreshTrips()
            is Intent.SearchTrips -> searchTrips(intent.query)
            is Intent.FilterByStatus -> filterByStatus(intent.status)
            is Intent.SelectTrip -> selectTrip(intent.tripId)
            is Intent.CancelTrip -> cancelTrip(intent.tripId)
            is Intent.CreateTrip -> sendEffect(Effect.NavigateToCreateTrip)
            is Intent.ClearFilters -> clearFilters()
        }
    }

    private suspend fun loadTrips() {
        withContext(dispatcherProvider.io) {
            getTripsUseCase().collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        updateState { copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        updateState {
                            copy(
                                isLoading = false,
                                trips = result.data,
                                filteredTrips = result.data,
                                error = null
                            )
                        }
                        applyFilters()
                    }
                    is Result.Error -> {
                        updateState {
                            copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load trips"
                            )
                        }
                        sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to load trips"))
                    }
                }
            }
        }
    }

    private suspend fun refreshTrips() {
        updateState { copy(isRefreshing = true) }
        loadTrips()
        updateState { copy(isRefreshing = false) }
    }

    private fun searchTrips(query: String) {
        updateState { copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByStatus(status: TripStatus?) {
        updateState { copy(selectedStatusFilter = status) }
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = currentState.trips.filter { trip ->
            val matchesSearch = currentState.searchQuery.isEmpty() ||
                    trip.tripNumber.contains(currentState.searchQuery, ignoreCase = true) ||
                    trip.vehicleNumber.contains(currentState.searchQuery, ignoreCase = true) ||
                    trip.driverName.contains(currentState.searchQuery, ignoreCase = true) ||
                    trip.startLocation.address.contains(currentState.searchQuery, ignoreCase = true) ||
                    trip.endLocation.address.contains(currentState.searchQuery, ignoreCase = true)

            val matchesStatus = currentState.selectedStatusFilter == null ||
                    trip.status == currentState.selectedStatusFilter

            matchesSearch && matchesStatus
        }
        updateState { copy(filteredTrips = filtered) }
    }

    private fun selectTrip(tripId: String) {
        sendEffect(Effect.NavigateToTripDetail(tripId))
    }

    private suspend fun cancelTrip(tripId: String) {
        withContext(dispatcherProvider.io) {
            val result = cancelTripUseCase(tripId)
            when (result) {
                is Result.Success -> {
                    updateState {
                        copy(
                            trips = trips.map { trip ->
                                if (trip.id == tripId) trip.copy(status = TripStatus.CANCELLED) else trip
                            },
                            filteredTrips = filteredTrips.map { trip ->
                                if (trip.id == tripId) trip.copy(status = TripStatus.CANCELLED) else trip
                            }
                        )
                    }
                    sendEffect(Effect.ShowSnackbar("Trip cancelled"))
                }
                is Result.Error -> {
                    sendEffect(Effect.ShowSnackbar(result.message ?: "Failed to cancel trip"))
                }
                is Result.Loading -> { /* Not applicable for suspend function */ }
            }
        }
    }

    private fun clearFilters() {
        updateState { copy(searchQuery = "", selectedStatusFilter = null) }
        applyFilters()
    }
}
