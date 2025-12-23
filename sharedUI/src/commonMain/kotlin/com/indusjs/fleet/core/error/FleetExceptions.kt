package com.indusjs.fleet.core.error

/**
 * Base exception for Fleet application errors.
 */
sealed class FleetException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when user is not authenticated.
 */
class NotAuthenticatedException(
    message: String = "Not authenticated. Please log in."
) : FleetException(message)

/**
 * Exception thrown when API call fails.
 */
class ApiException(
    message: String,
    val code: Int? = null,
    cause: Throwable? = null
) : FleetException(message, cause)

/**
 * Exception thrown when network is unavailable.
 */
class NetworkException(
    message: String = "Network unavailable. Please check your connection.",
    cause: Throwable? = null
) : FleetException(message, cause)

/**
 * Exception thrown when validation fails.
 */
class ValidationException(
    message: String,
    val field: String? = null
) : FleetException(message)

