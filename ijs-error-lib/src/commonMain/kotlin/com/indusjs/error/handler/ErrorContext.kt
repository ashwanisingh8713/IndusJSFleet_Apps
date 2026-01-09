package com.indusjs.error.handler

/**
 * Interface for defining error context.
 * Implement this in your app to provide screen-specific error messages.
 *
 * Example implementation for Fleet app:
 * ```kotlin
 * enum class FleetErrorContext : ErrorContext {
 *     VEHICLES, DRIVERS, TRIPS, DASHBOARD;
 *
 *     override val contextName: String get() = name.lowercase()
 * }
 * ```
 */
interface ErrorContext {
    /**
     * Name of the context (e.g., "vehicles", "trips", "dashboard")
     */
    val contextName: String

    /**
     * Get context-specific message prefix for network errors
     */
    fun getNetworkErrorPrefix(): String = "Unable to load $contextName"

    /**
     * Get context-specific message prefix for timeout errors
     */
    fun getTimeoutErrorPrefix(): String = "Loading $contextName is taking too long"

    /**
     * Get context-specific not found message
     */
    fun getNotFoundMessage(): String = "${contextName.replaceFirstChar { it.uppercase() }} not found"

    /**
     * Get context-specific unauthorized message
     */
    fun getUnauthorizedMessage(): String = "access $contextName"
}

/**
 * Default/generic error context when no specific context is provided.
 */
object GenericErrorContext : ErrorContext {
    override val contextName: String = "data"

    override fun getNetworkErrorPrefix(): String = "Unable to connect"

    override fun getTimeoutErrorPrefix(): String = "Request is taking too long"

    override fun getNotFoundMessage(): String = "Resource not found"

    override fun getUnauthorizedMessage(): String = "perform this action"
}

