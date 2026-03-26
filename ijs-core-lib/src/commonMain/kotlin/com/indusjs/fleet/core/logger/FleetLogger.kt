package com.indusjs.fleet.core.logger

/**
 * Public DI-injectable logger interface for IndusJsFleet.
 *
 * A **single instance** is created at app startup and shared across all modules.
 * Each call site passes its own [tag] string to identify the source module/class.
 *
 * All `screen-*` modules access this interface transitively via
 * `ijs-network-lib → ijs-core-lib`. The concrete implementation
 * ([KermitFleetLogger]) lives in `ijs-logger-lib` and is wired
 * in `sharedUI/di/DefaultViewModelProvider`.
 *
 * Usage:
 * ```kotlin
 * // In a ViewModel or data-layer class:
 * logger.i(TAG, "Loaded ${vehicles.size} vehicles")
 * logger.e(TAG, "API error", throwable)
 * ```
 */
interface FleetLogger {
    fun v(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

