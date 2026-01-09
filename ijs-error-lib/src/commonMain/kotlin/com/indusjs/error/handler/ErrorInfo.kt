package com.indusjs.error.handler

/**
 * User-friendly error information for displaying to users.
 *
 * @param icon Emoji or icon identifier for the error
 * @param title Short title for the error
 * @param message Detailed user-friendly message
 * @param actionLabel Label for the action button (e.g., "Retry", "Go Back")
 * @param errorType The classified error type
 * @param isRetryable Whether the user can retry the operation
 */
data class ErrorInfo(
    val icon: String,
    val title: String,
    val message: String,
    val actionLabel: String = "Retry",
    val errorType: ErrorType = ErrorType.UNKNOWN,
    val isRetryable: Boolean = true
) {
    companion object {
        /**
         * Create an error info from error type with default values
         */
        fun fromType(
            type: ErrorType,
            customMessage: String? = null
        ): ErrorInfo {
            return ErrorInfo(
                icon = type.defaultIcon,
                title = type.defaultTitle,
                message = customMessage ?: getDefaultMessage(type),
                actionLabel = getDefaultActionLabel(type),
                errorType = type,
                isRetryable = type.isRetryable
            )
        }

        /**
         * Create a network connection error info
         */
        fun networkConnection(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "📡",
                title = "No Internet Connection",
                message = customMessage ?: "Please check your internet connection and try again.",
                actionLabel = "Retry",
                errorType = ErrorType.NETWORK_CONNECTION,
                isRetryable = true
            )
        }

        /**
         * Create a timeout error info
         */
        fun timeout(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "⏱️",
                title = "Connection Timeout",
                message = customMessage ?: "The request took too long. Please try again.",
                actionLabel = "Retry",
                errorType = ErrorType.NETWORK_TIMEOUT,
                isRetryable = true
            )
        }

        /**
         * Create a server error info
         */
        fun serverError(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "🔧",
                title = "Server Issue",
                message = customMessage ?: "Our servers are experiencing issues. Please try again later.",
                actionLabel = "Retry",
                errorType = ErrorType.SERVER_ERROR,
                isRetryable = true
            )
        }

        /**
         * Create an authentication error info
         */
        fun authentication(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "🔐",
                title = "Session Expired",
                message = customMessage ?: "Your session has expired. Please sign in again.",
                actionLabel = "Sign In",
                errorType = ErrorType.AUTHENTICATION,
                isRetryable = false
            )
        }

        /**
         * Create an authorization error info
         */
        fun authorization(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "🚫",
                title = "Access Denied",
                message = customMessage ?: "You don't have permission to perform this action.",
                actionLabel = "Go Back",
                errorType = ErrorType.AUTHORIZATION,
                isRetryable = false
            )
        }

        /**
         * Create a not found error info
         */
        fun notFound(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "🔍",
                title = "Not Found",
                message = customMessage ?: "The requested resource was not found.",
                actionLabel = "Go Back",
                errorType = ErrorType.NOT_FOUND,
                isRetryable = false
            )
        }

        /**
         * Create a validation error info
         */
        fun validation(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "⚠️",
                title = "Invalid Input",
                message = customMessage ?: "Please check your input and try again.",
                actionLabel = "Fix & Retry",
                errorType = ErrorType.VALIDATION,
                isRetryable = true
            )
        }

        /**
         * Create a generic error info
         */
        fun unknown(customMessage: String? = null): ErrorInfo {
            return ErrorInfo(
                icon = "❌",
                title = "Something Went Wrong",
                message = customMessage ?: "An unexpected error occurred. Please try again.",
                actionLabel = "Retry",
                errorType = ErrorType.UNKNOWN,
                isRetryable = true
            )
        }

        private fun getDefaultMessage(type: ErrorType): String {
            return when (type) {
                ErrorType.NETWORK_CONNECTION -> "Please check your internet connection and try again."
                ErrorType.NETWORK_TIMEOUT -> "The request took too long. Please try again."
                ErrorType.SERVER_ERROR -> "Our servers are experiencing issues. Please try again later."
                ErrorType.AUTHENTICATION -> "Your session has expired. Please sign in again."
                ErrorType.AUTHORIZATION -> "You don't have permission to perform this action."
                ErrorType.NOT_FOUND -> "The requested resource was not found."
                ErrorType.VALIDATION -> "Please check your input and try again."
                ErrorType.RATE_LIMITED -> "Too many requests. Please wait and try again."
                ErrorType.UNKNOWN -> "An unexpected error occurred. Please try again."
            }
        }

        private fun getDefaultActionLabel(type: ErrorType): String {
            return when (type) {
                ErrorType.AUTHENTICATION -> "Sign In"
                ErrorType.AUTHORIZATION, ErrorType.NOT_FOUND -> "Go Back"
                ErrorType.VALIDATION -> "Fix & Retry"
                else -> "Retry"
            }
        }
    }
}

