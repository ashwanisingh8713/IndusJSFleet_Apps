package com.indusjs.fleet.locationtracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.touchlab.kermit.Logger
import com.indusjs.fleet.locationtracker.data.TrackerPreferencesRepository
import com.indusjs.fleet.locationtracker.service.LocationTrackingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Broadcast receiver to restart location tracking service.
 *
 * This receiver handles various system events that might kill the service
 * and restarts it if tracking was enabled.
 */
class RestartReceiver : BroadcastReceiver() {

    private val log = Logger.withTag("RestartReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        log.i { "RestartReceiver triggered: ${intent.action}" }

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferencesRepository = TrackerPreferencesRepository(context)
                val config = preferencesRepository.configFlow.first()

                if (config.isTrackingEnabled && config.isConfigured) {
                    log.i { "Restarting location tracking service" }
                    LocationTrackingService.startService(context)
                } else {
                    log.i { "Tracking not enabled or not configured, skipping restart" }
                }
            } catch (e: Exception) {
                log.e { "Error restarting service: ${e.message}" }
            } finally {
                pendingResult.finish()
            }
        }
    }
}

