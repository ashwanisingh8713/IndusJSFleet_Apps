package com.indusjs.error.handler

import com.indusjs.error.exception.*

/**
 * Main error handler for processing errors and generating user-friendly messages.
 * Supports context-aware error messages through the [ErrorContext] interface.
 *
 * Usage:
 * ```kotlin
 * // With context
 * val errorInfo = ErrorHandler.getErrorInfo(error, MyAppContext.VEHICLES)
 *
 * // Without context (generic messages)
 * val errorInfo = ErrorHandler.getErrorInfo(error)
 *
 * // From exception
 * val errorInfo = ErrorHandler.getErrorInfo(exception, MyAppContext.TRIPS)
 * ```
 */
object ErrorHandler {

    /**
     * Get user-friendly error info from an error message string.
     *
     * @param error The error message string
     * @param context Optional context for screen-specific messages
     * @return ErrorInfo with user-friendly message, icon, and action
     */
    fun getErrorInfo(
        error: String?,
        context: ErrorContext = GenericErrorContext
    ): ErrorInfo {
        val errorType = ErrorClassifier.classifyFromMessage(error)
        return buildErrorInfo(errorType, error, context)
    }

    /**
     * Get user-friendly error info from a Throwable.
     *
     * @param throwable The exception/throwable
     * @param context Optional context for screen-specific messages
     * @return ErrorInfo with user-friendly message, icon, and action
     */
    fun getErrorInfo(
        throwable: Throwable,
        context: ErrorContext = GenericErrorContext
    ): ErrorInfo {
        val errorType = ErrorClassifier.classifyFromException(throwable)
        return buildErrorInfo(errorType, throwable.message, context)
    }

    /**
     * Get simple user-friendly message string.
     *
     * @param error The error message string
     * @param context Optional context for screen-specific messages
     * @return User-friendly message string
     */
    fun getUserFriendlyMessage(
        error: String?,
        context: ErrorContext = GenericErrorContext
    ): String {
        return getErrorInfo(error, context).message
    }

    /**
     * Get simple user-friendly message string from exception.
     *
     * @param throwable The exception
     * @param context Optional context for screen-specific messages
     * @return User-friendly message string
     */
    fun getUserFriendlyMessage(
        throwable: Throwable,
        context: ErrorContext = GenericErrorContext
    ): String {
        return getErrorInfo(throwable, context).message
    }

    /**
     * Classify error type from message.
     */
    fun classifyError(error: String?): ErrorType {
        return ErrorClassifier.classifyFromMessage(error)
    }

    /**
     * Classify error type from exception.
     */
    fun classifyError(throwable: Throwable): ErrorType {
        return ErrorClassifier.classifyFromException(throwable)
    }

    // ==================== Private Helpers ====================

    private fun buildErrorInfo(
        errorType: ErrorType,
        originalMessage: String?,
        context: ErrorContext
    ): ErrorInfo {
        return when (errorType) {
            ErrorType.NETWORK_CONNECTION -> buildNetworkConnectionError(context)
            ErrorType.NETWORK_TIMEOUT -> buildTimeoutError(context)
            ErrorType.SERVER_ERROR -> buildServerError()
            ErrorType.AUTHENTICATION -> buildAuthenticationError()
            ErrorType.AUTHORIZATION -> buildAuthorizationError(context)
            ErrorType.NOT_FOUND -> buildNotFoundError(context)
            ErrorType.RATE_LIMITED -> buildRateLimitError()
            ErrorType.VALIDATION -> buildValidationError(originalMessage)
            ErrorType.UNKNOWN -> buildUnknownError(originalMessage)
        }
    }

    private fun buildNetworkConnectionError(context: ErrorContext): ErrorInfo {
        return ErrorInfo(
            icon = "📡",
            title = "No Internet Connection",
            message = "${context.getNetworkErrorPrefix()}. Please check your internet connection and try again.",
            actionLabel = "Retry",
            errorType = ErrorType.NETWORK_CONNECTION,
            isRetryable = true
        )
    }

    private fun buildTimeoutError(context: ErrorContext): ErrorInfo {
        return ErrorInfo(
            icon = "⏱️",
            title = "Connection Timeout",
            message = "${context.getTimeoutErrorPrefix()}. Please check your connection and try again.",
            actionLabel = "Retry",
            errorType = ErrorType.NETWORK_TIMEOUT,
            isRetryable = true
        )
    }

    private fun buildServerError(): ErrorInfo {
        return ErrorInfo(
            icon = "🔧",
            title = "Server Issue",
            message = "Our servers are experiencing issues. Please try again in a few moments.",
            actionLabel = "Retry",
            errorType = ErrorType.SERVER_ERROR,
            isRetryable = true
        )
    }

    private fun buildAuthenticationError(): ErrorInfo {
        return ErrorInfo(
            icon = "🔐",
            title = "Session Expired",
            message = "Your session has expired. Please sign in again to continue.",
            actionLabel = "Sign In",
            errorType = ErrorType.AUTHENTICATION,
            isRetryable = false
        )
    }

    private fun buildAuthorizationError(context: ErrorContext): ErrorInfo {
        return ErrorInfo(
            icon = "🚫",
            title = "Access Denied",
            message = "You don't have permission to ${context.getUnauthorizedMessage()}. Please contact your administrator.",
            actionLabel = "Go Back",
            errorType = ErrorType.AUTHORIZATION,
            isRetryable = false
        )
    }

    private fun buildNotFoundError(context: ErrorContext): ErrorInfo {
        return ErrorInfo(
            icon = "🔍",
            title = context.getNotFoundMessage(),
            message = "The item you're looking for may have been deleted or doesn't exist.",
            actionLabel = "Go Back",
            errorType = ErrorType.NOT_FOUND,
            isRetryable = false
        )
    }

    private fun buildRateLimitError(): ErrorInfo {
        return ErrorInfo(
            icon = "⏳",
            title = "Too Many Requests",
            message = "You've made too many requests. Please wait a moment and try again.",
            actionLabel = "Retry",
            errorType = ErrorType.RATE_LIMITED,
            isRetryable = true
        )
    }

    private fun buildValidationError(originalMessage: String?): ErrorInfo {
        return ErrorInfo(
            icon = "⚠️",
            title = "Invalid Input",
            message = originalMessage ?: "Please check your input and try again.",
            actionLabel = "Fix & Retry",
            errorType = ErrorType.VALIDATION,
            isRetryable = true
        )
    }

    private fun buildUnknownError(originalMessage: String?): ErrorInfo {
        return ErrorInfo(
            icon = "❌",
            title = "Something Went Wrong",
            message = originalMessage ?: "An unexpected error occurred. Please try again.",
            actionLabel = "Retry",
            errorType = ErrorType.UNKNOWN,
            isRetryable = true
        )
    }
}

// ==================== Extension Functions ====================

/**
 * Extension function to get user-friendly message from any error string.
 */
fun String?.toUserFriendlyError(context: ErrorContext = GenericErrorContext): String {
    return ErrorHandler.getUserFriendlyMessage(this, context)
}

/**
 * Extension function to get full error info from any error string.
 */
fun String?.toErrorInfo(context: ErrorContext = GenericErrorContext): ErrorInfo {
    return ErrorHandler.getErrorInfo(this, context)
}

/**
 * Extension function to get error info from Throwable.
 */
fun Throwable.toErrorInfo(context: ErrorContext = GenericErrorContext): ErrorInfo {
    return ErrorHandler.getErrorInfo(this, context)
}

/**
 * Extension function to get user-friendly message from Throwable.
 */
fun Throwable.toUserFriendlyMessage(context: ErrorContext = GenericErrorContext): String {
    return ErrorHandler.getUserFriendlyMessage(this, context)
}

