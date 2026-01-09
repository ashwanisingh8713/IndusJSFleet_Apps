package com.indusjs.error.exception

/**
 * Base sealed exception for all IndusJS application errors.
 * Provides a consistent error hierarchy for error handling across the app.
 *
 * Usage:
 * ```kotlin
 * try {
 *     // operation
 * } catch (e: IjsException) {
 *     when (e) {
 *         is NetworkException -> handleNetworkError(e)
 *         is ApiException -> handleApiError(e)
 *         is AuthException -> handleAuthError(e)
 *         is ValidationException -> handleValidationError(e)
 *     }
 * }
 * ```
 */
sealed class IjsException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * A short code for the error type (e.g., "NETWORK", "AUTH", "API")
     */
    abstract val errorCode: String

    /**
     * Whether this error is recoverable (user can retry)
     */
    open val isRecoverable: Boolean = true
}

