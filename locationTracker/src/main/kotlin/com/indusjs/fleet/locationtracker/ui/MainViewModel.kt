package com.indusjs.fleet.locationtracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.indusjs.fleet.locationtracker.data.TrackerConfig
import com.indusjs.fleet.locationtracker.data.TrackerPreferencesRepository
import com.indusjs.fleet.locationtracker.data.TrackingStatus
import com.indusjs.fleet.locationtracker.mqtt.MqttClientManager
import com.indusjs.fleet.locationtracker.service.LocationTrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for the main screen of the Location Tracker app.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val log = Logger.withTag("MainViewModel")
    private val preferencesRepository = TrackerPreferencesRepository(application)

    private val _config = MutableStateFlow(TrackerConfig())
    val config: StateFlow<TrackerConfig> = _config.asStateFlow()

    private val _trackingStatus = MutableStateFlow(TrackingStatus())
    val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    private val _showConfigDialog = MutableStateFlow(false)
    val showConfigDialog: StateFlow<Boolean> = _showConfigDialog.asStateFlow()

    // Expose connection state for UI
    val connectionState: StateFlow<String> = MqttClientManager.connectionState

    init {
        // Observe configuration changes
        preferencesRepository.configFlow
            .onEach { config ->
                log.d { "Config updated: $config" }
                _config.value = config
            }
            .launchIn(viewModelScope)

        // Combine MQTT status with tracking status (using singleton MqttClientManager)
        combine(
            MqttClientManager.isConnected,
            MqttClientManager.messagesPublished,
            MqttClientManager.lastError
        ) { isConnected, messagesPublished, lastError ->
            _trackingStatus.value = _trackingStatus.value.copy(
                isMqttConnected = isConnected,
                messagesPublished = messagesPublished,
                lastError = lastError
            )
        }.launchIn(viewModelScope)
    }

    /**
     * Save the tracker configuration.
     */
    fun saveConfig(config: TrackerConfig) {
        viewModelScope.launch {
            log.i { "Saving config: $config" }
            preferencesRepository.saveConfig(config)
            _showConfigDialog.value = false
        }
    }

    /**
     * Start location tracking.
     */
    fun startTracking() {
        viewModelScope.launch {
            val currentConfig = _config.value
            if (!currentConfig.isConfigured) {
                log.e { "Configuration not complete" }
                _trackingStatus.value = _trackingStatus.value.copy(
                    lastError = "Please configure vehicle registration number first"
                )
                return@launch
            }

            // Save tracking enabled state
            preferencesRepository.setTrackingEnabled(true)

            // Start the foreground service
            LocationTrackingService.startService(getApplication())

            _trackingStatus.value = _trackingStatus.value.copy(
                isTracking = true,
                lastError = null
            )

            log.i { "Started tracking" }
        }
    }

    /**
     * Stop location tracking.
     */
    fun stopTracking() {
        viewModelScope.launch {
            // Save tracking disabled state
            preferencesRepository.setTrackingEnabled(false)

            // Stop the foreground service
            LocationTrackingService.stopService(getApplication())

            _trackingStatus.value = _trackingStatus.value.copy(
                isTracking = false
            )

            log.i { "Stopped tracking" }
        }
    }

    /**
     * Show the configuration dialog.
     */
    fun showConfig() {
        _showConfigDialog.value = true
    }

    /**
     * Hide the configuration dialog.
     */
    fun hideConfig() {
        _showConfigDialog.value = false
    }

    /**
     * Update active trip ID.
     */
    fun setActiveTrip(tripId: Long?) {
        viewModelScope.launch {
            preferencesRepository.setActiveTrip(tripId)
        }
    }

    /**
     * Clear all configuration.
     */
    fun clearConfig() {
        viewModelScope.launch {
            stopTracking()
            preferencesRepository.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // MqttClientManager is a singleton, no need to disconnect here
        // It will be managed by the LocationTrackingService
    }
}

