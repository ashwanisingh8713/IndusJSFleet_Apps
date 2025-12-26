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
 * Broadcast receiver to restart location tracking after device boot.
 *
 * If tracking was enabled before the device was shut down, this receiver
 * will automatically restart the tracking service after boot completes.
 */
class BootReceiver : BroadcastReceiver() {

    private val log = Logger.withTag("BootReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            log.i { "Boot completed, checking if tracking should be resumed" }

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val preferencesRepository = TrackerPreferencesRepository(context)
                    val config = preferencesRepository.configFlow.first()

                    if (config.isTrackingEnabled && config.isConfigured) {
                        log.i { "Resuming tracking after boot" }
                        LocationTrackingService.startService(context)
                    } else {
                        log.i { "Tracking not enabled or not configured, skipping" }
                    }
                } catch (e: Exception) {
                    log.e { "Error checking tracking status: ${e.message}" }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

