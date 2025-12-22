package com.indusjs.fleet.feature.vehicles.presentation

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.feature.vehicles.domain.entity.Location
import com.indusjs.fleet.feature.vehicles.domain.entity.Vehicle
import com.indusjs.fleet.feature.vehicles.domain.entity.VehicleStatus
import com.indusjs.fleet.feature.vehicles.domain.entity.VehicleType
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesContract.Effect
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesContract.Intent
import com.indusjs.fleet.feature.vehicles.presentation.VehiclesContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Vehicles screen implementing MVI pattern.
 */
@Inject
class VehiclesViewModel(
    private val dispatcherProvider: DispatcherProvider
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadVehicles)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadVehicles -> loadVehicles()
            is Intent.RefreshVehicles -> refreshVehicles()
            is Intent.SearchVehicles -> searchVehicles(intent.query)
            is Intent.FilterByStatus -> filterByStatus(intent.status)
            is Intent.SelectVehicle -> sendEffect(Effect.NavigateToVehicleDetail(intent.vehicleId))
            is Intent.DeleteVehicle -> deleteVehicle(intent.vehicleId)
            is Intent.AddVehicle -> sendEffect(Effect.NavigateToAddVehicle)
            is Intent.ClearFilters -> clearFilters()
        }
    }

    private suspend fun loadVehicles() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                // TODO: Replace with actual repository call
                delay(500)

                val mockVehicles = listOf(
                    Vehicle(
                        id = "1",
                        registrationNumber = "TRK-001",
                        make = "Volvo",
                        model = "FH16",
                        year = 2022,
                        type = VehicleType.TRUCK,
                        status = VehicleStatus.ACTIVE,
                        fuelLevel = 75,
                        mileage = 45000.0,
                        lastLocation = Location(28.6139, 77.2090, "Delhi, India"),
                        assignedDriverId = "D1",
                        assignedDriverName = "John Smith"
                    ),
                    Vehicle(
                        id = "2",
                        registrationNumber = "TRK-002",
                        make = "Mercedes",
                        model = "Actros",
                        year = 2021,
                        type = VehicleType.TRUCK,
                        status = VehicleStatus.ACTIVE,
                        fuelLevel = 45,
                        mileage = 62000.0,
                        lastLocation = Location(19.0760, 72.8777, "Mumbai, India"),
                        assignedDriverId = "D2",
                        assignedDriverName = "Mike Johnson"
                    ),
                    Vehicle(
                        id = "3",
                        registrationNumber = "VAN-001",
                        make = "Ford",
                        model = "Transit",
                        year = 2023,
                        type = VehicleType.VAN,
                        status = VehicleStatus.IN_MAINTENANCE,
                        fuelLevel = 60,
                        mileage = 15000.0,
                        lastLocation = Location(13.0827, 80.2707, "Chennai, India")
                    ),
                    Vehicle(
                        id = "4",
                        registrationNumber = "TRK-003",
                        make = "Scania",
                        model = "R450",
                        year = 2020,
                        type = VehicleType.TRUCK,
                        status = VehicleStatus.INACTIVE,
                        fuelLevel = 20,
                        mileage = 98000.0,
                        lastLocation = Location(22.5726, 88.3639, "Kolkata, India")
                    ),
                    Vehicle(
                        id = "5",
                        registrationNumber = "BUS-001",
                        make = "Tata",
                        model = "Starbus",
                        year = 2022,
                        type = VehicleType.BUS,
                        status = VehicleStatus.ACTIVE,
                        fuelLevel = 90,
                        mileage = 35000.0,
                        lastLocation = Location(12.9716, 77.5946, "Bangalore, India"),
                        assignedDriverId = "D3",
                        assignedDriverName = "David Wilson"
                    )
                )

                updateState {
                    copy(
                        isLoading = false,
                        vehicles = mockVehicles,
                        filteredVehicles = mockVehicles
                    )
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load vehicles") }
                sendEffect(Effect.ShowSnackbar("Failed to load vehicles"))
            }
        }
    }

    private suspend fun refreshVehicles() {
        updateState { copy(isRefreshing = true) }
        loadVehicles()
        updateState { copy(isRefreshing = false) }
    }

    private fun searchVehicles(query: String) {
        updateState { copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByStatus(status: VehicleStatus?) {
        updateState { copy(selectedStatusFilter = status) }
        applyFilters()
    }

    private fun clearFilters() {
        updateState {
            copy(
                searchQuery = "",
                selectedStatusFilter = null,
                filteredVehicles = vehicles
            )
        }
    }

    private fun applyFilters() {
        val currentState = currentState
        val filtered = currentState.vehicles.filter { vehicle ->
            val matchesSearch = currentState.searchQuery.isEmpty() ||
                    vehicle.registrationNumber.contains(currentState.searchQuery, ignoreCase = true) ||
                    vehicle.make.contains(currentState.searchQuery, ignoreCase = true) ||
                    vehicle.model.contains(currentState.searchQuery, ignoreCase = true)

            val matchesStatus = currentState.selectedStatusFilter == null ||
                    vehicle.status == currentState.selectedStatusFilter

            matchesSearch && matchesStatus
        }
        updateState { copy(filteredVehicles = filtered) }
    }

    private fun deleteVehicle(vehicleId: String) {
        updateState {
            val updatedVehicles = vehicles.filter { it.id != vehicleId }
            copy(
                vehicles = updatedVehicles,
                filteredVehicles = updatedVehicles
            )
        }
        applyFilters()
        sendEffect(Effect.ShowSnackbar("Vehicle deleted successfully"))
    }
}

