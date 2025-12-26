package com.indusjs.fleet.locationtracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tracker_settings")

/**
 * Repository for managing tracker configuration using DataStore.
 */
class TrackerPreferencesRepository(private val context: Context) {

    private object Keys {
        val REGISTRATION_NUMBER = stringPreferencesKey("registration_number")
        val DRIVER_ID = longPreferencesKey("driver_id")
        val TRIP_ID = longPreferencesKey("trip_id")
        val MQTT_BROKER_URL = stringPreferencesKey("mqtt_broker_url")
        val MQTT_CLIENT_ID = stringPreferencesKey("mqtt_client_id")
        val MQTT_USERNAME = stringPreferencesKey("mqtt_username")
        val MQTT_PASSWORD = stringPreferencesKey("mqtt_password")
        val UPDATE_INTERVAL_MS = longPreferencesKey("update_interval_ms")
        val TRACKING_ENABLED = booleanPreferencesKey("tracking_enabled")
    }

    /**
     * Flow of the current tracker configuration.
     */
    val configFlow: Flow<TrackerConfig> = context.dataStore.data.map { preferences ->
        TrackerConfig(
            registrationNumber = preferences[Keys.REGISTRATION_NUMBER] ?: "",
            driverId = preferences[Keys.DRIVER_ID],
            tripId = preferences[Keys.TRIP_ID],
            mqttBrokerUrl = preferences[Keys.MQTT_BROKER_URL] ?: "tcp://10.0.2.2:1883",
            mqttClientId = preferences[Keys.MQTT_CLIENT_ID] ?: "",
            mqttUsername = preferences[Keys.MQTT_USERNAME] ?: "",
            mqttPassword = preferences[Keys.MQTT_PASSWORD] ?: "",
            updateIntervalMs = preferences[Keys.UPDATE_INTERVAL_MS] ?: 2000,
            isTrackingEnabled = preferences[Keys.TRACKING_ENABLED] ?: false
        )
    }

    /**
     * Save the tracker configuration.
     */
    suspend fun saveConfig(config: TrackerConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.REGISTRATION_NUMBER] = config.registrationNumber
            config.driverId?.let { preferences[Keys.DRIVER_ID] = it }
            config.tripId?.let { preferences[Keys.TRIP_ID] = it }
            preferences[Keys.MQTT_BROKER_URL] = config.mqttBrokerUrl
            preferences[Keys.MQTT_CLIENT_ID] = config.mqttClientId
            preferences[Keys.MQTT_USERNAME] = config.mqttUsername
            preferences[Keys.MQTT_PASSWORD] = config.mqttPassword
            preferences[Keys.UPDATE_INTERVAL_MS] = config.updateIntervalMs
            preferences[Keys.TRACKING_ENABLED] = config.isTrackingEnabled
        }
    }

    /**
     * Update tracking enabled state.
     */
    suspend fun setTrackingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TRACKING_ENABLED] = enabled
        }
    }

    /**
     * Update active trip.
     */
    suspend fun setActiveTrip(tripId: Long?) {
        context.dataStore.edit { preferences ->
            if (tripId != null) {
                preferences[Keys.TRIP_ID] = tripId
            } else {
                preferences.remove(Keys.TRIP_ID)
            }
        }
    }

    /**
     * Clear driver ID (when driver logs out).
     */
    suspend fun clearDriver() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.DRIVER_ID)
            preferences.remove(Keys.TRIP_ID)
        }
    }

    /**
     * Clear all configuration.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

