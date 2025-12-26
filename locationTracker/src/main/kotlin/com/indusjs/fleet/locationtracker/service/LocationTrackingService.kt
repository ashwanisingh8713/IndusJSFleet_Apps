package com.indusjs.fleet.locationtracker.service

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import co.touchlab.kermit.Logger
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.indusjs.fleet.locationtracker.R
import com.indusjs.fleet.locationtracker.data.LocationMessage
import com.indusjs.fleet.locationtracker.data.TrackerConfig
import com.indusjs.fleet.locationtracker.data.TrackerPreferencesRepository
import com.indusjs.fleet.locationtracker.mqtt.MqttClientManager
import com.indusjs.fleet.locationtracker.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground service for continuous location tracking and MQTT publishing.
 *
 * This service runs in the foreground with a persistent notification,
 * tracks the device location at configured intervals, and publishes
 * location updates to the MQTT broker.
 */
class LocationTrackingService : Service() {

    private val log = Logger.withTag("LocationTrackingService")
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var preferencesRepository: TrackerPreferencesRepository

    // WakeLock to keep CPU running
    private var wakeLock: PowerManager.WakeLock? = null

    // Handler for periodic self-check
    private val handler = Handler(Looper.getMainLooper())
    private var watchdogRunnable: Runnable? = null

    private var currentConfig: TrackerConfig? = null
    private var isTracking = false
    private var lastLocationTime = 0L

    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "location_tracking_channel"

        const val ACTION_START = "com.indusjs.fleet.locationtracker.ACTION_START"
        const val ACTION_STOP = "com.indusjs.fleet.locationtracker.ACTION_STOP"
        const val ACTION_RESTART = "com.indusjs.fleet.locationtracker.ACTION_RESTART"

        private const val RESTART_ALARM_REQUEST_CODE = 1002
        private const val WATCHDOG_INTERVAL_MS = 60_000L // Check every 1 minute
        private const val LOCATION_TIMEOUT_MS = 120_000L // Restart if no location for 2 minutes

        fun startService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        /**
         * Schedule a restart of the service using AlarmManager.
         * This ensures the service restarts even if it's killed by the system.
         */
        fun scheduleRestart(context: Context, delayMs: Long = 5000L) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_RESTART
            }
            val pendingIntent = PendingIntent.getService(
                context,
                RESTART_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val triggerTime = SystemClock.elapsedRealtime() + delayMs

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }

        /**
         * Cancel any scheduled restart.
         */
        fun cancelScheduledRestart(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = ACTION_RESTART
            }
            val pendingIntent = PendingIntent.getService(
                context,
                RESTART_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            if (pendingIntent != null) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        log.i { "Service onCreate" }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        preferencesRepository = TrackerPreferencesRepository(this)

        // Acquire partial wake lock to keep CPU running
        acquireWakeLock()

        createNotificationChannel()
        setupLocationCallback()

        // Start watchdog to ensure service stays alive
        startWatchdog()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        log.i { "onStartCommand: ${intent?.action}" }

        when (intent?.action) {
            ACTION_START, ACTION_RESTART, null -> {
                // null intent means service restarted by system
                serviceScope.launch {
                    startTracking()
                }
            }
            ACTION_STOP -> {
                cancelScheduledRestart(this)
                stopTracking()
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // START_STICKY ensures the service is restarted if killed
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        log.i { "onTaskRemoved - scheduling restart" }
        // Schedule restart when app is swiped away
        if (isTracking) {
            scheduleRestart(this)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        log.i { "Service onDestroy" }

        // Stop watchdog
        stopWatchdog()

        // Release wake lock
        releaseWakeLock()

        // If tracking was enabled, schedule a restart
        if (isTracking) {
            log.i { "Service destroyed while tracking - scheduling restart" }
            scheduleRestart(this)
        }

        stopTracking()
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Acquire a partial wake lock to keep the CPU running.
     */
    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(PowerManager::class.java)
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "FleetTracker::LocationTrackingWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(24 * 60 * 60 * 1000L) // 24 hours max
            }
            log.i { "WakeLock acquired" }
        }
    }

    /**
     * Release the wake lock.
     */
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                log.i { "WakeLock released" }
            }
        }
        wakeLock = null
    }

    /**
     * Start a watchdog that periodically checks if location updates are being received.
     * If no location updates for a while, restart the location tracking.
     */
    private fun startWatchdog() {
        watchdogRunnable = object : Runnable {
            override fun run() {
                val now = System.currentTimeMillis()

                if (isTracking && lastLocationTime > 0) {
                    val timeSinceLastLocation = now - lastLocationTime

                    if (timeSinceLastLocation > LOCATION_TIMEOUT_MS) {
                        log.w { "No location update for ${timeSinceLastLocation / 1000}s - restarting location updates" }
                        restartLocationUpdates()
                    }
                }

                // Also check MQTT connection
                if (isTracking && !MqttClientManager.isClientConnected()) {
                    log.w { "MQTT disconnected - reconnecting" }
                    currentConfig?.let { MqttClientManager.connect(it) }
                }

                // Re-acquire wake lock if needed
                if (isTracking && wakeLock?.isHeld != true) {
                    log.w { "WakeLock lost - reacquiring" }
                    acquireWakeLock()
                }

                // Schedule next check
                handler.postDelayed(this, WATCHDOG_INTERVAL_MS)
            }
        }

        handler.postDelayed(watchdogRunnable!!, WATCHDOG_INTERVAL_MS)
        log.i { "Watchdog started" }
    }

    /**
     * Stop the watchdog.
     */
    private fun stopWatchdog() {
        watchdogRunnable?.let {
            handler.removeCallbacks(it)
        }
        watchdogRunnable = null
        log.i { "Watchdog stopped" }
    }

    /**
     * Restart location updates if they seem to have stopped.
     */
    private fun restartLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            log.e { "Error removing location updates: ${e.message}" }
        }

        startLocationUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, LocationTrackingService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    log.d { "Location update: ${location.latitude}, ${location.longitude}" }

                    // Update last location time for watchdog
                    lastLocationTime = System.currentTimeMillis()

                    currentConfig?.let { config ->
                        val locationMessage = LocationMessage(
                            registrationNumber = config.registrationNumber,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            speed = if (location.hasSpeed()) (location.speed * 3.6) else 0.0, // Convert m/s to km/h
                            timestamp = System.currentTimeMillis(),
                            heading = if (location.hasBearing()) location.bearing.toDouble() else null,
                            altitude = if (location.hasAltitude()) location.altitude else null,
                            accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
                            tripId = config.tripId,
                            driverId = config.driverId,
                            batteryLevel = getBatteryLevel(),
                            provider = location.provider
                        )

                        MqttClientManager.publishLocation(locationMessage)
                    }
                }
            }
        }
    }

    private suspend fun startTracking() {
        if (isTracking) {
            log.w { "Already tracking" }
            return
        }

        // Load configuration
        currentConfig = preferencesRepository.configFlow.first()

        if (currentConfig?.isConfigured != true) {
            log.e { "Configuration not complete" }
            stopSelf()
            return
        }

        // Start foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                createNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }

        // Connect to MQTT broker
        currentConfig?.let { config ->
            MqttClientManager.connect(config)
        }

        // Start location updates
        startLocationUpdates()

        isTracking = true
        log.i { "Tracking started" }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            log.e { "Location permission not granted" }
            return
        }

        val intervalMs = currentConfig?.updateIntervalMs ?: 2000L

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMaxUpdateDelayMillis(intervalMs * 2)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            log.i { "Location updates started with interval: ${intervalMs}ms" }
        } catch (e: Exception) {
            log.e { "Error starting location updates: ${e.message}" }
        }
    }

    private fun stopTracking() {
        if (!isTracking) {
            return
        }

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            log.e { "Error removing location updates: ${e.message}" }
        }

        MqttClientManager.disconnect()

        isTracking = false
        log.i { "Tracking stopped" }
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            -1
        }
    }
}

