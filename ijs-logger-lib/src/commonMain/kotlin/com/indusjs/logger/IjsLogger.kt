package com.indusjs.logger

import co.touchlab.kermit.Logger
import co.touchlab.kermit.platformLogWriter
import com.indusjs.fleet.core.logger.FleetLogger
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Public entry point for the ijs-logger module.
 *
 * Usage:
 * ```kotlin
 * // At app launch (once):
 * IjsLogger.init(platformContext)
 *
 * // Anywhere after init:
 * IjsLogger.logger.i("NetworkTag") { "Connected to server")
 * IjsLogger.logger.e("AuthTag", throwable) { "Token expired")
 * ```
 *
 * Internally registers:
 * - Kermit's [platformLogWriter] (Logcat / OSLog / console)
 * - [FileLogWriter] that persists to platform-specific storage
 * - [CrashHandler] for uncaught exceptions
 */
object IjsLogger {

    private var _logger: Logger? = null
    private var fileManager: LogFileManager? = null

    /**
     * The Kermit [Logger] instance. Call [init] before using.
     * @throws IllegalStateException if [init] was not called.
     */
    val logger: Logger
        get() = _logger ?: error("IjsLogger.init() has not been called")

    /**
     * Initialize the logger. Call **once** at app launch.
     *
     * @param context platform-specific context —
     *  Android: `applicationContext`,
     *  iOS/Web: `PlatformContext()` marker.
     */
    fun init(context: PlatformContext) {
        if (_logger != null) return // already initialized — idempotent

        val sessionTimestamp = generateSessionTimestamp()
        val manager = LogFileManager(context, sessionTimestamp)
        fileManager = manager

        val fileWriter = FileLogWriter(manager)

        _logger = Logger.apply {
            setLogWriters(platformLogWriter(), fileWriter)
            setTag("IjsFleet")
        }

        // Install crash handler after logger is ready
        val crashHandler = CrashHandler(manager)
        crashHandler.install()

        _logger?.i(tag = "IjsLogger") { "Logger initialized — session: $sessionTimestamp" }
    }

    /**
     * Creates a single [FleetLogger] instance backed by this [IjsLogger].
     *
     * Call **after** [init]. Typically called once from `DefaultViewModelProvider`
     * and the resulting instance is shared across the entire app.
     *
     * @throws IllegalStateException if [init] was not called.
     */
    fun createFleetLogger(): FleetLogger = KermitFleetLogger()

    /**
     * Generates session timestamp for log filename.
     * Pattern: `ddMMYYYY_HHmm`
     * Example: `24032026_1430`
     */
    private fun generateSessionTimestamp(): String {
        val now = Clock.System.now()
        val local = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val dd = local.dayOfMonth.toString().padStart(2, '0')
        val mm = local.monthNumber.toString().padStart(2, '0')
        val yyyy = local.year.toString()
        val hh = local.hour.toString().padStart(2, '0')
        val min = local.minute.toString().padStart(2, '0')
        return "${dd}${mm}${yyyy}_${hh}${min}"
    }
}

