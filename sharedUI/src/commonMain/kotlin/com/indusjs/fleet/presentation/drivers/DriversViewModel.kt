package com.indusjs.fleet.presentation.drivers

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.driver.Driver
import com.indusjs.fleet.domain.entity.driver.DriverLocation
import com.indusjs.fleet.domain.entity.driver.DriverStatus
import com.indusjs.fleet.presentation.drivers.DriversContract.Effect
import com.indusjs.fleet.presentation.drivers.DriversContract.Intent
import com.indusjs.fleet.presentation.drivers.DriversContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Drivers screen implementing MVI pattern.
 */
@Inject
class DriversViewModel(
    private val dispatcherProvider: DispatcherProvider
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadDrivers)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadDrivers -> loadDrivers()
            is Intent.RefreshDrivers -> refreshDrivers()
            is Intent.SearchDrivers -> searchDrivers(intent.query)
            is Intent.FilterByStatus -> filterByStatus(intent.status)
            is Intent.SelectDriver -> selectDriver(intent.driverId)
            is Intent.DeleteDriver -> deleteDriver(intent.driverId)
            is Intent.AddDriver -> sendEffect(Effect.NavigateToAddDriver)
            is Intent.ClearFilters -> clearFilters()
        }
    }

    private suspend fun loadDrivers() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                delay(500)

                // Using a simple timestamp for mock data
                val currentTime = 1734700000000L // Dec 20, 2024 approximate
                val mockDrivers = listOf(
                    Driver(
                        id = "D1",
                        firstName = "John",
                        lastName = "Smith",
                        email = "john.smith@fleet.com",
                        phone = "+91 98765 43210",
                        licenseNumber = "DL-123456789",
                        status = DriverStatus.ON_TRIP,
                        rating = 4.8,
                        totalTrips = 245,
                        assignedVehicleId = "1",
                        assignedVehicleNumber = "TRK-001",
                        hireDate = currentTime - 365L * 24 * 60 * 60 * 1000,
                        licenseExpiry = currentTime + 365L * 24 * 60 * 60 * 1000,
                        currentLocation = DriverLocation(28.6139, 77.2090, "Delhi, India")
                    ),
                    Driver(
                        id = "D2",
                        firstName = "Mike",
                        lastName = "Johnson",
                        email = "mike.johnson@fleet.com",
                        phone = "+91 98765 43211",
                        licenseNumber = "DL-987654321",
                        status = DriverStatus.ON_TRIP,
                        rating = 4.5,
                        totalTrips = 189,
                        assignedVehicleId = "2",
                        assignedVehicleNumber = "TRK-002",
                        hireDate = currentTime - 200L * 24 * 60 * 60 * 1000,
                        licenseExpiry = currentTime + 180L * 24 * 60 * 60 * 1000,
                        currentLocation = DriverLocation(19.0760, 72.8777, "Mumbai, India")
                    ),
                    Driver(
                        id = "D3",
                        firstName = "David",
                        lastName = "Wilson",
                        email = "david.wilson@fleet.com",
                        phone = "+91 98765 43212",
                        licenseNumber = "DL-456789123",
                        status = DriverStatus.AVAILABLE,
                        rating = 4.9,
                        totalTrips = 312,
                        assignedVehicleId = "5",
                        assignedVehicleNumber = "BUS-001",
                        hireDate = currentTime - 500L * 24 * 60 * 60 * 1000,
                        licenseExpiry = currentTime + 90L * 24 * 60 * 60 * 1000,
                        currentLocation = DriverLocation(12.9716, 77.5946, "Bangalore, India")
                    ),
                    Driver(
                        id = "D4",
                        firstName = "Sarah",
                        lastName = "Brown",
                        email = "sarah.brown@fleet.com",
                        phone = "+91 98765 43213",
                        licenseNumber = "DL-789123456",
                        status = DriverStatus.OFF_DUTY,
                        rating = 4.7,
                        totalTrips = 156,
                        hireDate = currentTime - 150L * 24 * 60 * 60 * 1000,
                        licenseExpiry = currentTime + 400L * 24 * 60 * 60 * 1000
                    ),
                    Driver(
                        id = "D5",
                        firstName = "Robert",
                        lastName = "Taylor",
                        email = "robert.taylor@fleet.com",
                        phone = "+91 98765 43214",
                        licenseNumber = "DL-321654987",
                        status = DriverStatus.ON_BREAK,
                        rating = 4.3,
                        totalTrips = 98,
                        hireDate = currentTime - 100L * 24 * 60 * 60 * 1000,
                        licenseExpiry = currentTime + 250L * 24 * 60 * 60 * 1000,
                        currentLocation = DriverLocation(17.3850, 78.4867, "Hyderabad, India")
                    )
                )

                updateState {
                    copy(
                        isLoading = false,
                        drivers = mockDrivers,
                        filteredDrivers = mockDrivers
                    )
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load drivers") }
                sendEffect(Effect.ShowSnackbar("Failed to load drivers"))
            }
        }
    }

    private suspend fun refreshDrivers() {
        updateState { copy(isRefreshing = true) }
        loadDrivers()
        updateState { copy(isRefreshing = false) }
    }

    private fun searchDrivers(query: String) {
        updateState { copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterByStatus(status: DriverStatus?) {
        updateState { copy(selectedStatusFilter = status) }
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = currentState.drivers.filter { driver ->
            val matchesSearch = currentState.searchQuery.isEmpty() ||
                    driver.fullName.contains(currentState.searchQuery, ignoreCase = true) ||
                    driver.email.contains(currentState.searchQuery, ignoreCase = true) ||
                    driver.phone.contains(currentState.searchQuery, ignoreCase = true)

            val matchesStatus = currentState.selectedStatusFilter == null ||
                    driver.status == currentState.selectedStatusFilter

            matchesSearch && matchesStatus
        }
        updateState { copy(filteredDrivers = filtered) }
    }

    private fun selectDriver(driverId: String) {
        sendEffect(Effect.NavigateToDriverDetail(driverId))
    }

    private fun deleteDriver(driverId: String) {
        updateState {
            copy(
                drivers = drivers.filter { it.id != driverId },
                filteredDrivers = filteredDrivers.filter { it.id != driverId }
            )
        }
        sendEffect(Effect.ShowSnackbar("Driver deleted"))
    }

    private fun clearFilters() {
        updateState { copy(searchQuery = "", selectedStatusFilter = null) }
        applyFilters()
    }
}
