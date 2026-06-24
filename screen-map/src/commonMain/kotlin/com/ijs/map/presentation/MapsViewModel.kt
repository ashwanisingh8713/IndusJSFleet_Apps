package com.ijs.map.presentation

import androidx.lifecycle.viewModelScope
import com.indusjs.dispatcher.DispatcherProvider
import com.indusjs.error.result.Result
import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.core.mvi.MviViewModel
import com.indusjs.fleet.domain.entity.maps.MapLocation
import com.indusjs.fleet.domain.entity.maps.MapVehicle
import com.indusjs.fleet.domain.entity.maps.MapVehicleStatus
import com.ijs.map.TAG_MAP_VM
import com.ijs.map.domain.LiveLocationSocket
import com.ijs.map.domain.LocationUpdate
import com.ijs.map.domain.MapVehicleProvider
import com.ijs.map.presentation.MapsContract.Effect
import com.ijs.map.presentation.MapsContract.Intent
import com.ijs.map.presentation.MapsContract.LiveConnectionStatus
import com.ijs.map.presentation.MapsContract.State
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Maps/Live Tracking screen implementing MVI pattern.
 *
 * Data flow:
 *  1. [MapVehicleProvider] seeds the markers with the tenant's real vehicles
 *     (last-known location, or OFFLINE when unknown).
 *  2. [LiveLocationSocket] streams `location_update` frames over a tenant-scoped
 *     WebSocket; each frame updates the matching vehicle marker's position live.
 *
 * The live socket is collected inside [viewModelScope], so it is torn down
 * automatically when the screen leaves and the ViewModel is cleared. Connect
 * failures are handled gracefully: the seeded markers stay on screen and the
 * status flips to [LiveConnectionStatus.DISCONNECTED] — no crash.
 */
@Inject
class MapsViewModel(
    private val dispatcherProvider: DispatcherProvider,
    private val mapVehicleProvider: MapVehicleProvider,
    private val liveLocationSocket: LiveLocationSocket,
    private val logger: FleetLogger
) : MviViewModel<State, Intent, Effect>(State()) {

    /** The single in-flight live-feed collection, if any. */
    private var liveJob: Job? = null

    init {
        sendIntent(Intent.LoadMapData)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadMapData -> loadMapData()
            is Intent.RefreshVehicleLocations -> loadMapData()
            is Intent.SelectVehicle -> selectVehicle(intent.vehicleId)
            is Intent.ClearSelection -> clearSelection()
            is Intent.ToggleLiveTracking -> toggleLiveTracking()
            is Intent.ToggleGeofences -> toggleGeofences()
            is Intent.UpdateMapCenter -> updateMapCenter(intent.latitude, intent.longitude)
            is Intent.UpdateMapZoom -> updateMapZoom(intent.zoom)
            is Intent.NavigateToVehicleDetail -> navigateToVehicleDetail(intent.vehicleId)
        }
    }

    /**
     * Loads the tenant's real vehicles as seed markers, then (re)starts the
     * live feed when live tracking is enabled.
     */
    private suspend fun loadMapData() {
        updateState { copy(isLoading = true, error = null) }

        val result = withContext(dispatcherProvider.io) {
            mapVehicleProvider.getMapVehicles()
        }

        when (result) {
            is Result.Success -> {
                updateState {
                    copy(
                        isLoading = false,
                        vehicles = result.data,
                        geofences = emptyList(),
                        error = null
                    )
                }
                if (currentState.isLiveTrackingEnabled) startLiveFeed()
            }
            is Result.Error -> {
                updateState { copy(isLoading = false, error = result.errorMessage) }
                sendEffect(Effect.ShowSnackbar(result.errorMessage))
            }
            is Result.Loading -> {
                updateState { copy(isLoading = true) }
            }
        }
    }

    /**
     * Opens (or re-opens) the live-location WebSocket and feeds frames into the
     * markers. Any previously running collection is cancelled first so we never
     * stack sockets. The collection lives in [viewModelScope] → cancelled when
     * the ViewModel is cleared on screen leave.
     */
    private fun startLiveFeed() {
        stopLiveFeed()
        liveJob = viewModelScope.launch {
            liveLocationSocket.connect()
                .onStart { updateState { copy(liveConnectionStatus = LiveConnectionStatus.CONNECTING) } }
                .catch { e ->
                    // Connect/handshake failure or dropped socket: keep seeds, show disconnected.
                    logger.w(TAG_MAP_VM, "Live-location socket error", e)
                    updateState { copy(liveConnectionStatus = LiveConnectionStatus.DISCONNECTED) }
                }
                .onCompletion { cause ->
                    // Normal completion (server closed) also lands here; only flag
                    // disconnected if we were not deliberately cancelled.
                    if (cause == null && currentState.liveConnectionStatus != LiveConnectionStatus.IDLE) {
                        updateState { copy(liveConnectionStatus = LiveConnectionStatus.DISCONNECTED) }
                    }
                }
                .collect { update ->
                    // First frame proves the socket is live.
                    if (currentState.liveConnectionStatus != LiveConnectionStatus.CONNECTED) {
                        updateState { copy(liveConnectionStatus = LiveConnectionStatus.CONNECTED) }
                    }
                    applyLocationUpdate(update)
                }
        }
    }

    private fun stopLiveFeed() {
        liveJob?.cancel()
        liveJob = null
    }

    /**
     * Merges a live frame into the matching seeded marker. Matching is by
     * registration number (the stable, tenant-unique key in the frame), falling
     * back to nothing if no seed matches (we do not invent markers for vehicles
     * the tenant's vehicle list did not return).
     */
    private fun applyLocationUpdate(update: LocationUpdate) {
        val vehicles = currentState.vehicles
        val index = vehicles.indexOfFirst { it.vehicleNumber == update.registrationNumber }
        if (index < 0) {
            logger.d(TAG_MAP_VM, "Live frame for unknown vehicle ${update.registrationNumber}; ignoring")
            return
        }

        val existing = vehicles[index]
        val updated = existing.copy(
            location = MapLocation(
                latitude = update.lat,
                longitude = update.lng,
                address = existing.location.address
            ),
            status = if (update.speed > MOVING_SPEED_THRESHOLD) MapVehicleStatus.MOVING else MapVehicleStatus.IDLE,
            speed = update.speed,
            heading = update.heading ?: existing.heading,
            lastUpdated = update.ts
        )

        val newList = vehicles.toMutableList().also { it[index] = updated }
        updateState {
            copy(
                vehicles = newList,
                // Keep the selection in sync so the detail panel reflects live data.
                selectedVehicle = if (selectedVehicle?.id == updated.id) updated else selectedVehicle
            )
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
        val enabled = !currentState.isLiveTrackingEnabled
        updateState { copy(isLiveTrackingEnabled = enabled) }
        if (enabled) {
            startLiveFeed()
        } else {
            stopLiveFeed()
            updateState { copy(liveConnectionStatus = LiveConnectionStatus.IDLE) }
        }
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

    override fun onCleared() {
        // Belt-and-braces: viewModelScope cancellation already closes the socket,
        // but cancel explicitly so the WS coroutine unwinds promptly on leave.
        stopLiveFeed()
        super.onCleared()
    }

    private companion object {
        /** km/h above which a vehicle is considered MOVING rather than IDLE. */
        const val MOVING_SPEED_THRESHOLD = 1.0
    }
}
