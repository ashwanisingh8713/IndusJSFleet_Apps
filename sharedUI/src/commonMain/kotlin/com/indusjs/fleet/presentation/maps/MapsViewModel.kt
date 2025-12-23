package com.indusjs.fleet.presentation.maps

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.maps.Geofence
import com.indusjs.fleet.domain.entity.maps.GeofenceType
import com.indusjs.fleet.domain.entity.maps.MapLocation
import com.indusjs.fleet.domain.entity.maps.MapVehicle
import com.indusjs.fleet.domain.entity.maps.MapVehicleStatus
import com.indusjs.fleet.presentation.maps.MapsContract.Effect
import com.indusjs.fleet.presentation.maps.MapsContract.Intent
import com.indusjs.fleet.presentation.maps.MapsContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * ViewModel for the Maps/Live Tracking screen implementing MVI pattern.
 */
@Inject
class MapsViewModel(
    private val dispatcherProvider: DispatcherProvider
) : MviViewModel<State, Intent, Effect>(State()) {

    init {
        sendIntent(Intent.LoadMapData)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadMapData -> loadMapData()
            is Intent.RefreshVehicleLocations -> refreshLocations()
            is Intent.SelectVehicle -> selectVehicle(intent.vehicleId)
            is Intent.ClearSelection -> clearSelection()
            is Intent.ToggleLiveTracking -> toggleLiveTracking()
            is Intent.ToggleGeofences -> toggleGeofences()
            is Intent.UpdateMapCenter -> updateMapCenter(intent.latitude, intent.longitude)
            is Intent.UpdateMapZoom -> updateMapZoom(intent.zoom)
            is Intent.NavigateToVehicleDetail -> navigateToVehicleDetail(intent.vehicleId)
        }
    }

    private suspend fun loadMapData() {
        updateState { copy(isLoading = true, error = null) }

        withContext(dispatcherProvider.io) {
            try {
                delay(500)

                // Using a simple timestamp for mock data
                val currentTime = 1734700000000L // Dec 20, 2024 approximate
                val mockVehicles = listOf(
                    MapVehicle(
                        id = "V1",
                        vehicleNumber = "TRK-001",
                        driverName = "John Smith",
                        location = MapLocation(28.6139, 77.2090, "Delhi, India"),
                        status = MapVehicleStatus.MOVING,
                        speed = 45.0,
                        heading = 90.0,
                        lastUpdated = currentTime
                    ),
                    MapVehicle(
                        id = "V2",
                        vehicleNumber = "TRK-002",
                        driverName = "Mike Johnson",
                        location = MapLocation(19.0760, 72.8777, "Mumbai, India"),
                        status = MapVehicleStatus.MOVING,
                        speed = 32.0,
                        heading = 180.0,
                        lastUpdated = currentTime
                    ),
                    MapVehicle(
                        id = "V3",
                        vehicleNumber = "VAN-001",
                        driverName = null,
                        location = MapLocation(12.9716, 77.5946, "Bangalore, India"),
                        status = MapVehicleStatus.STOPPED,
                        speed = 0.0,
                        heading = 0.0,
                        lastUpdated = currentTime
                    ),
                    MapVehicle(
                        id = "V4",
                        vehicleNumber = "TRK-003",
                        driverName = null,
                        location = MapLocation(17.3850, 78.4867, "Hyderabad, India"),
                        status = MapVehicleStatus.IDLE,
                        speed = 0.0,
                        heading = 45.0,
                        lastUpdated = currentTime
                    ),
                    MapVehicle(
                        id = "V5",
                        vehicleNumber = "BUS-001",
                        driverName = "David Wilson",
                        location = MapLocation(22.5726, 88.3639, "Kolkata, India"),
                        status = MapVehicleStatus.MOVING,
                        speed = 28.0,
                        heading = 270.0,
                        lastUpdated = currentTime
                    )
                )

                val mockGeofences = listOf(
                    Geofence(
                        id = "G1",
                        name = "Main Depot",
                        type = GeofenceType.DEPOT,
                        center = MapLocation(28.6129, 77.2295, "Main Depot, Delhi"),
                        radius = 500.0,
                        isActive = true
                    ),
                    Geofence(
                        id = "G2",
                        name = "Mumbai Depot",
                        type = GeofenceType.DEPOT,
                        center = MapLocation(19.0750, 72.8800, "Mumbai Depot"),
                        radius = 400.0,
                        isActive = true
                    ),
                    Geofence(
                        id = "G3",
                        name = "Customer Site A",
                        type = GeofenceType.CUSTOMER,
                        center = MapLocation(12.9700, 77.6000, "Bangalore Customer"),
                        radius = 200.0,
                        isActive = true
                    )
                )

                updateState {
                    copy(
                        isLoading = false,
                        vehicles = mockVehicles,
                        geofences = mockGeofences
                    )
                }
            } catch (e: Exception) {
                updateState { copy(isLoading = false, error = e.message ?: "Failed to load map data") }
                sendEffect(Effect.ShowSnackbar("Failed to load map data"))
            }
        }
    }

    private suspend fun refreshLocations() {
        withContext(dispatcherProvider.io) {
            // Using a simple timestamp for mock data
            val currentTime = 1734700000000L // Dec 20, 2024 approximate
            val updatedVehicles = currentState.vehicles.map { vehicle ->
                if (vehicle.status == MapVehicleStatus.MOVING) {
                    vehicle.copy(
                        location = vehicle.location.copy(
                            latitude = vehicle.location.latitude + (Random.nextDouble() - 0.5) * 0.001,
                            longitude = vehicle.location.longitude + (Random.nextDouble() - 0.5) * 0.001
                        ),
                        lastUpdated = currentTime
                    )
                } else vehicle
            }
            updateState { copy(vehicles = updatedVehicles) }
        }
    }

    private fun selectVehicle(vehicleId: String) {
        val vehicle = currentState.vehicles.find { it.id == vehicleId }
        updateState { copy(selectedVehicle = vehicle) }
        vehicle?.let {
            sendEffect(Effect.ZoomToVehicle(it.location.latitude, it.location.longitude))
        }
    }

    private fun clearSelection() {
        updateState { copy(selectedVehicle = null) }
    }

    private fun toggleGeofences() {
        updateState { copy(showGeofences = !showGeofences) }
    }

    private fun toggleLiveTracking() {
        updateState { copy(isLiveTrackingEnabled = !isLiveTrackingEnabled) }
    }

    private fun updateMapCenter(latitude: Double, longitude: Double) {
        updateState { copy(centerLatitude = latitude, centerLongitude = longitude) }
    }

    private fun updateMapZoom(zoom: Float) {
        updateState { copy(mapZoom = zoom) }
    }

    private fun navigateToVehicleDetail(vehicleId: String) {
        sendEffect(Effect.NavigateToVehicleDetail(vehicleId))
    }
}
