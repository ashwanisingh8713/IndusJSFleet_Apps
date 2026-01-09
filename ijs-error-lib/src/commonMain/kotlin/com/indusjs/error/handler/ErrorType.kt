package com.indusjs.error.handler

/**
 * Classification of error types.
 * Used to categorize errors for appropriate handling and messaging.
 */
enum class ErrorType {
    /** Network connection errors (no internet, connection refused) */
    NETWORK_CONNECTION,

    /** Request timeout errors */
    NETWORK_TIMEOUT,

    /** Server errors (5xx status codes) */
    SERVER_ERROR,

    /** Authentication errors (401, expired token) */
    AUTHENTICATION,

    /** Authorization errors (403, permission denied) */
    AUTHORIZATION,

    /** Resource not found errors (404) */
    NOT_FOUND,

    /** Input validation errors (400, 422) */
    VALIDATION,

    /** Rate limiting errors (429) */
    RATE_LIMITED,

    /** Unknown or unclassified errors */
    UNKNOWN;

    /**
     * Whether this error type is typically recoverable by retry
     */
    val isRetryable: Boolean
        get() = this in listOf(
            NETWORK_CONNECTION,
            NETWORK_TIMEOUT,
            SERVER_ERROR,
            RATE_LIMITED
        )

    /**
     * Whether this error type requires user action (like re-login)
     */
    val requiresUserAction: Boolean
        get() = this in listOf(
            AUTHENTICATION,
            AUTHORIZATION,
            VALIDATION
        )

    /**
     * Get default icon for this error type
     */
    val defaultIcon: String
        get() = when (this) {
            NETWORK_CONNECTION -> "📡"
            NETWORK_TIMEOUT -> "⏱️"
            SERVER_ERROR -> "🔧"
            AUTHENTICATION -> "🔐"
            AUTHORIZATION -> "🚫"
            NOT_FOUND -> "🔍"
            VALIDATION -> "⚠️"
            RATE_LIMITED -> "⏳"
            UNKNOWN -> "❌"
        }

    /**
     * Get default title for this error type
     */
    val defaultTitle: String
        get() = when (this) {
            NETWORK_CONNECTION -> "No Internet Connection"
            NETWORK_TIMEOUT -> "Connection Timeout"
            SERVER_ERROR -> "Server Issue"
            AUTHENTICATION -> "Session Expired"
            AUTHORIZATION -> "Access Denied"
            NOT_FOUND -> "Not Found"
            VALIDATION -> "Invalid Input"
            RATE_LIMITED -> "Too Many Requests"
            UNKNOWN -> "Something Went Wrong"
        }
}

