package com.indusjs.fleet.locationtracker

import android.app.Application
import co.touchlab.kermit.Logger

/**
 * Application class for the Location Tracker app.
 */
class LocationTrackerApp : Application() {

    private val log = Logger.withTag("LocationTrackerApp")

    override fun onCreate() {
        super.onCreate()
        log.i { "LocationTrackerApp initialized" }
    }
}

