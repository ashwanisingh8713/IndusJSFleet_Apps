package com.ijs.reports.presentation.trip

import com.indusjs.error.result.Result
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.core.util.convertToEpochMillis
import com.indusjs.uicomponents.components.UiText
import com.ijs.reports.data.model.MultiTripPLRequest
import com.ijs.reports.domain.usecase.GetMultiTripPLUseCase
import com.ijs.trip.domain.repository.TripRepository
import com.ijs.reports.presentation.trip.TripPLContract.Effect
import com.ijs.reports.presentation.trip.TripPLContract.Intent
import com.ijs.reports.presentation.trip.TripPLContract.State
import dev.zacsweers.metro.Inject
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_generate
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_failed_load_trips
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_no_pl_data
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_no_valid_trip_ids
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_no_trips_found
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_select_both_dates
import indusjsfleet.ijs_ui_components_lib.generated.resources.report_select_one_trip
import kotlinx.coroutines.flow.collectLatest

/**
 * ViewModel for Trip P&L Screen
 * Trip-centric: Select date range -> Load trips -> Select trips -> Generate P&L
 */
@Inject
class TripPLViewModel(
    private val getMultiTripPLUseCase: GetMultiTripPLUseCase,
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
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_select_both_dates)))
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
                        sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_no_trips_found)))
                    }
                }
                is Result.Error -> {
                    updateState { copy(isLoadingTrips = false, tripsLoaded = false) }
                    sendEffect(Effect.ShowSnackbar(result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_load_trips)))
                }
                is Result.Loading -> { /* Already handled */ }
            }
        }
    }

    private suspend fun generateReport() {
        val currentState = state.value

        if (currentState.selectedTripIds.isEmpty()) {
            sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_select_one_trip)))
            return
        }

        updateState { copy(isLoading = true, error = null) }

        val tripIds = currentState.selectedTripIds.mapNotNull { it.toIntOrNull() }
        if (tripIds.isEmpty()) {
            updateState { copy(isLoading = false, error = UiText.StringRes(Res.string.report_no_valid_trip_ids)) }
            return
        }

        // Picker state holds DD-MM-YYYY; convert to UTC epoch millis at the request boundary.
        val request = MultiTripPLRequest(
            tripIds = tripIds,
            startDate = convertToEpochMillis(currentState.startDate),
            endDate = convertToEpochMillis(currentState.endDate)
        )

        when (val result = getMultiTripPLUseCase(request)) {
            is Result.Success -> {
                updateState { copy(isLoading = false, results = result.data) }
                if (result.data.isEmpty()) {
                    sendEffect(Effect.ShowSnackbar(UiText.StringRes(Res.string.report_no_pl_data)))
                }
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.message?.let { UiText.Raw(it) } ?: UiText.StringRes(Res.string.report_failed_generate)) }
            }
            is Result.Loading -> { /* Already handled */ }
        }
    }
}

