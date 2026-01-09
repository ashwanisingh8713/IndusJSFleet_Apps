package com.indusjs.error.handler

import com.indusjs.error.exception.*

/**
 * Utility for classifying errors from messages and exceptions.
 * Use this to determine the type of error for appropriate handling.
 */
object ErrorClassifier {

    /**
     * Classify error type from an error message string.
     */
    fun classifyFromMessage(error: String?): ErrorType {
        val errorLower = error?.lowercase() ?: ""

        return when {
            // Network connection errors
            isNetworkConnectionError(errorLower) -> ErrorType.NETWORK_CONNECTION

            // Timeout errors
            isTimeoutError(errorLower) -> ErrorType.NETWORK_TIMEOUT

            // Server errors (5xx)
            isServerError(errorLower) -> ErrorType.SERVER_ERROR

            // Authentication errors
            isAuthenticationError(errorLower) -> ErrorType.AUTHENTICATION

            // Authorization errors
            isAuthorizationError(errorLower) -> ErrorType.AUTHORIZATION

            // Not found errors
            isNotFoundError(errorLower) -> ErrorType.NOT_FOUND

            // Rate limiting
            isRateLimitError(errorLower) -> ErrorType.RATE_LIMITED

            // Validation errors
            isValidationError(errorLower) -> ErrorType.VALIDATION

            else -> ErrorType.UNKNOWN
        }
    }

    /**
     * Classify error type from a Throwable.
     */
    fun classifyFromException(throwable: Throwable): ErrorType {
        return when (throwable) {
            is NetworkException -> when (throwable.type) {
                NetworkException.Type.CONNECTION,
                NetworkException.Type.DNS,
                NetworkException.Type.RESET -> ErrorType.NETWORK_CONNECTION
                NetworkException.Type.TIMEOUT -> ErrorType.NETWORK_TIMEOUT
                NetworkException.Type.SSL -> ErrorType.NETWORK_CONNECTION
                NetworkException.Type.UNKNOWN -> classifyFromMessage(throwable.message)
            }
            is ApiException -> when {
                throwable.httpCode == 401 -> ErrorType.AUTHENTICATION
                throwable.httpCode == 403 -> ErrorType.AUTHORIZATION
                throwable.httpCode == 404 -> ErrorType.NOT_FOUND
                throwable.httpCode == 429 -> ErrorType.RATE_LIMITED
                throwable.httpCode in 400..499 -> ErrorType.VALIDATION
                throwable.httpCode in 500..599 -> ErrorType.SERVER_ERROR
                else -> classifyFromMessage(throwable.message)
            }
            is AuthException -> when (throwable.type) {
                AuthException.Type.UNAUTHENTICATED,
                AuthException.Type.TOKEN_EXPIRED,
                AuthException.Type.INVALID_CREDENTIALS -> ErrorType.AUTHENTICATION
                AuthException.Type.UNAUTHORIZED -> ErrorType.AUTHORIZATION
            }
            is ValidationException -> ErrorType.VALIDATION
            is IjsException -> classifyFromMessage(throwable.message)
            else -> classifyFromMessage(throwable.message)
        }
    }

    // ==================== Private Classification Helpers ====================

    private fun isNetworkConnectionError(error: String): Boolean {
        val patterns = listOf(
            "failed to connect",
            "unable to resolve host",
            "no address associated",
            "network is unreachable",
            "connection refused",
            "no internet",
            "offline",
            "network unavailable",
            "econnrefused",
            "unknownhostexception",
            "no route to host",
            "connection reset",
            "socket closed"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isTimeoutError(error: String): Boolean {
        val patterns = listOf(
            "timeout",
            "timed out",
            "sockettimeoutexception",
            "connect timed out",
            "read timed out"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isServerError(error: String): Boolean {
        val patterns = listOf(
            "500", "501", "502", "503", "504",
            "server error",
            "internal server",
            "bad gateway",
            "service unavailable",
            "gateway timeout"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isAuthenticationError(error: String): Boolean {
        val patterns = listOf(
            "401",
            "unauthorized",
            "not authenticated",
            "invalid token",
            "token expired",
            "login required",
            "session expired",
            "authentication failed",
            "invalid credentials"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isAuthorizationError(error: String): Boolean {
        val patterns = listOf(
            "403",
            "forbidden",
            "access denied",
            "permission denied",
            "not authorized"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isNotFoundError(error: String): Boolean {
        val patterns = listOf(
            "404",
            "not found",
            "does not exist",
            "no such"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isRateLimitError(error: String): Boolean {
        val patterns = listOf(
            "429",
            "rate limit",
            "too many requests",
            "throttled"
        )
        return patterns.any { error.contains(it) }
    }

    private fun isValidationError(error: String): Boolean {
        val patterns = listOf(
            "validation",
            "invalid input",
            "required field",
            "invalid format",
            "must be",
            "cannot be empty"
        )
        return patterns.any { error.contains(it) }
    }
}

