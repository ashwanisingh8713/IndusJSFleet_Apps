package com.indusjs.fleet.androidApp

import android.app.Application
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.common.CommonUtils.isEmulator
import com.indusjs.fleet.androidApp.BuildConfig
import com.indusjs.fleet.core.network.ApiConfig
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
        configureApiBaseUrl()
        initializeLogger()
        initializeCrashlytics()
    }

    /**
     * Pick the API host for the current device, so ONE build runs on both the emulator and a real
     * phone. An Android emulator must reach the dev backend via 10.0.2.2 (its host-loopback alias);
     * a physical device can't use that, so it keeps ApiConfig's LAN-IP default. Runs before any
     * network call (BASE_URL is read per request).
     */
    private fun configureApiBaseUrl() {
        if (isEmulator()) {
            ApiConfig.BASE_URL = ApiConfig.ANDROID_EMULATOR_BASE_URL
            Log.d(TAG, "Android emulator detected → API base = ${ApiConfig.BASE_URL}")
        } else {
            Log.d(TAG, "Physical device → API base = ${ApiConfig.BASE_URL}")
        }
    }

    /** Best-effort Android emulator detection across common AVD / Genymotion / cloud images. */
    private fun isEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.MANUFACTURER.contains("Genymotion") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu") ||
            Build.HARDWARE.contains("vbox") ||
            (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
            Build.PRODUCT.contains("sdk") ||
            Build.PRODUCT.contains("emulator") ||
            Build.PRODUCT.contains("simulator")
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

