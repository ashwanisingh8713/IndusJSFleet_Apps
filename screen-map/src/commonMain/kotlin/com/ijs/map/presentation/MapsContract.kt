package com.ijs.map.presentation

import com.indusjs.fleet.core.mvi.UiEffect
import com.indusjs.fleet.core.mvi.UiIntent
import com.indusjs.fleet.core.mvi.UiState
import com.indusjs.fleet.domain.entity.maps.Geofence
import com.indusjs.fleet.domain.entity.maps.MapVehicle

/**
 * MVI Contract for the Maps/Live Tracking screen.
 */
object MapsContract {

    /**
     * Live-location WebSocket connection status, surfaced in the UI so the user
     * knows whether markers are updating in real time or are last-known seeds.
     */
    enum class LiveConnectionStatus {
        /** Not attempting a connection (live tracking toggled off, or no session). */
        IDLE,

        /** Handshake in progress. */
        CONNECTING,

        /** Socket open; markers update from the live feed. */
        CONNECTED,

        /** Connect failed or socket dropped; seeded markers still shown. */
        DISCONNECTED
    }

    /**
     * UI State for the Maps screen.
     */
    data class State(
        val isLoading: Boolean = false,
        val vehicles: List<MapVehicle> = emptyList(),
        val geofences: List<Geofence> = emptyList(),
        val selectedVehicle: MapVehicle? = null,
        val error: String? = null,
        val isLiveTrackingEnabled: Boolean = true,
        val showGeofences: Boolean = true,
        val mapZoom: Float = 5f,
        val centerLatitude: Double = 20.5937,
        val centerLongitude: Double = 78.9629,
        val liveConnectionStatus: LiveConnectionStatus = LiveConnectionStatus.IDLE
    ) : UiState

    /**
     * User intents for the Maps screen.
     */
    sealed interface Intent : UiIntent {
        data object LoadMapData : Intent
        data object RefreshVehicleLocations : Intent
        data class SelectVehicle(val vehicleId: String) : Intent
        data object ClearSelection : Intent
        data object ToggleLiveTracking : Intent
        data object ToggleGeofences : Intent
        data class UpdateMapCenter(val latitude: Double, val longitude: Double) : Intent
        data class UpdateMapZoom(val zoom: Float) : Intent
        data class NavigateToVehicleDetail(val vehicleId: String) : Intent
    }

    /**
     * Side effects for the Maps screen.
     */
    sealed interface Effect : UiEffect {
        data class ShowSnackbar(val message: String) : Effect
        data class ZoomToVehicle(val latitude: Double, val longitude: Double) : Effect
        data class NavigateToVehicleDetail(val vehicleId: String) : Effect
    }
}

