package com.indusjs.fleet.feature.trips.presentation

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.feature.trips.domain.entity.Trip
import com.indusjs.fleet.feature.trips.domain.entity.TripLocation
import com.indusjs.fleet.feature.trips.domain.entity.TripStatus
import com.indusjs.fleet.feature.trips.presentation.TripsContract.Effect
import com.indusjs.fleet.feature.trips.presentation.TripsContract.Intent
import com.indusjs.fleet.feature.trips.presentation.TripsContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Trips screen implementing MVI pattern.
 */
@Inject
class TripsViewModel(
    private val dispatcherProvider: DispatcherProvider
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
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // Using a simple timestamp for mock data
                val currentTime = 1734700000000L // Dec 20, 2024 approximate
                delay(500)

                val mockTrips = listOf(
                    Trip(
                        id = "T1",
                        tripNumber = "TRIP-001",
                        vehicleId = "V1",
                        vehicleNumber = "TRK-001",
                        driverId = "D1",
                        driverName = "John Smith",
                        status = TripStatus.IN_PROGRESS,
                        startLocation = TripLocation(28.6139, 77.2090, "Delhi, India"),
                        endLocation = TripLocation(19.0760, 72.8777, "Mumbai, India"),
                        currentLocation = TripLocation(23.2599, 77.4126, "Bhopal, India"),
                        distance = 1400.0,
                        estimatedDuration = 20 * 60 * 60 * 1000L,
                        scheduledStartTime = currentTime - 8 * 60 * 60 * 1000L,
                        actualStartTime = currentTime - 8 * 60 * 60 * 1000L,
                        cargo = "Electronics"
                    ),
                    Trip(
                        id = "T2",
                        tripNumber = "TRIP-002",
                        vehicleId = "V2",
                        vehicleNumber = "TRK-002",
                        driverId = "D2",
                        driverName = "Mike Johnson",
                        status = TripStatus.IN_PROGRESS,
                        startLocation = TripLocation(19.0760, 72.8777, "Mumbai, India"),
                        endLocation = TripLocation(12.9716, 77.5946, "Bangalore, India"),
                        currentLocation = TripLocation(15.3173, 75.7139, "Hubli, India"),
                        distance = 980.0,
                        estimatedDuration = 14 * 60 * 60 * 1000L,
                        scheduledStartTime = currentTime - 6 * 60 * 60 * 1000L,
                        actualStartTime = currentTime - 6 * 60 * 60 * 1000L,
                        cargo = "Furniture"
                    ),
                    Trip(
                        id = "T3",
                        tripNumber = "TRIP-003",
                        vehicleId = "V3",
                        vehicleNumber = "VAN-001",
                        driverId = "D3",
                        driverName = "David Wilson",
                        status = TripStatus.SCHEDULED,
                        startLocation = TripLocation(12.9716, 77.5946, "Bangalore, India"),
                        endLocation = TripLocation(13.0827, 80.2707, "Chennai, India"),
                        distance = 350.0,
                        estimatedDuration = 6 * 60 * 60 * 1000L,
                        scheduledStartTime = currentTime + 2 * 60 * 60 * 1000L,
                        cargo = "Medical Supplies"
                    ),
                    Trip(
                        id = "T4",
                        tripNumber = "TRIP-004",
                        vehicleId = "V1",
                        vehicleNumber = "TRK-001",
                        driverId = "D1",
                        driverName = "John Smith",
                        status = TripStatus.COMPLETED,
                        startLocation = TripLocation(28.6139, 77.2090, "Delhi, India"),
                        endLocation = TripLocation(26.9124, 75.7873, "Jaipur, India"),
                        distance = 280.0,
                        estimatedDuration = 5 * 60 * 60 * 1000L,
                        actualDuration = 5 * 60 * 60 * 1000L + 30 * 60 * 1000L,
                        scheduledStartTime = currentTime - 24 * 60 * 60 * 1000L,
                        actualStartTime = currentTime - 24 * 60 * 60 * 1000L,
                        actualEndTime = currentTime - 18 * 60 * 60 * 1000L,
                        cargo = "Textiles"
                    ),
                    Trip(
                        id = "T5",
                        tripNumber = "TRIP-005",
                        vehicleId = "V4",
                        vehicleNumber = "TRK-003",
                        driverId = "D4",
                        driverName = "Sarah Brown",
                        status = TripStatus.CANCELLED,
                        startLocation = TripLocation(17.3850, 78.4867, "Hyderabad, India"),
                        endLocation = TripLocation(21.1702, 72.8311, "Surat, India"),
                        distance = 900.0,
                        estimatedDuration = 13 * 60 * 60 * 1000L,
                        scheduledStartTime = currentTime - 12 * 60 * 60 * 1000L,
                        notes = "Cancelled due to vehicle maintenance"
                    ),
                    Trip(
                        id = "T6",
                        tripNumber = "TRIP-006",
                        vehicleId = "V5",
                        vehicleNumber = "BUS-001",
                        driverId = "D5",
                        driverName = "Robert Taylor",
                        status = TripStatus.DELAYED,
                        startLocation = TripLocation(22.5726, 88.3639, "Kolkata, India"),
                        endLocation = TripLocation(25.5941, 85.1376, "Patna, India"),
                        distance = 530.0,
                        estimatedDuration = 8 * 60 * 60 * 1000L,
                        scheduledStartTime = currentTime - 2 * 60 * 60 * 1000L,
                        notes = "Delayed due to traffic"
                    )
                )

                updateState {
                    copy(
                        isLoading = false,
                        trips = mockTrips,
                        filteredTrips = mockTrips
                    )
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load trips") }
                sendEffect(Effect.ShowSnackbar("Failed to load trips"))
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

    private fun cancelTrip(tripId: String) {
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

    private fun clearFilters() {
        updateState { copy(searchQuery = "", selectedStatusFilter = null) }
        applyFilters()
    }
}
