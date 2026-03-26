package com.indusjs.fleet.androidApp

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.indusjs.fleet.androidApp.BuildConfig
import com.indusjs.logger.IjsLogger
import com.indusjs.logger.PlatformContext

/**
 * Application class for IndusJS Fleet Android app.
 * Initializes IjsLogger file logging and Firebase Crashlytics for crash reporting.
 */
class FleetApplication : Application() {

    companion object {
        private const val TAG = "FleetApplication"
    }

    override fun onCreate() {
        super.onCreate()
        initializeLogger()
        initializeCrashlytics()
    }

    private fun initializeLogger() {
        try {
            IjsLogger.init(PlatformContext(this))
            Log.d(TAG, "IjsLogger initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize IjsLogger", e)
        }
    }

    private fun initializeCrashlytics() {
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Configure Crashlytics
            val crashlytics = FirebaseCrashlytics.getInstance()

            // Enable/disable crash collection based on build type
            // In production, you might want to check user consent before enabling
            crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

            // Set custom keys for better crash context
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")

            Log.d(TAG, "Crashlytics initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Crashlytics", e)
        }
    }

    /**
     * Set user identifier for crash reports.
     * Call this after user logs in.
     */
    fun setUserId(userId: String) {
        try {
            FirebaseCrashlytics.getInstance().setUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user ID for Crashlytics", e)
        }
    }

    /**
     * Log a custom message to Crashlytics.
     * Useful for tracking user flow before a crash.
     */
    fun logMessage(message: String) {
        try {
            FirebaseCrashlytics.getInstance().log(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log message to Crashlytics", e)
        }
    }

    /**
     * Record a non-fatal exception to Crashlytics.
     */
    fun recordException(throwable: Throwable) {
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record exception to Crashlytics", e)
        }
    }
}

