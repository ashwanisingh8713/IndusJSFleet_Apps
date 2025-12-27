package com.indusjs.fleet.core.error

/**
 * Utility for handling errors and converting them to user-friendly messages.
 * Use this throughout the app for consistent error messaging.
 */
object ErrorHandler {

    /**
     * Screen context for providing screen-specific error messages.
     */
    enum class ScreenContext {
        DASHBOARD,
        VEHICLES,
        VEHICLE_DETAIL,
        DRIVERS,
        DRIVER_DETAIL,
        TRIPS,
        TRIP_DETAIL,
        TEAM,
        PROFILE,
        LOGIN,
        SIGNUP,
        MAPS,
        DOCUMENTS,
        GENERIC
    }

    /**
     * Error type classification.
     */
    enum class ErrorType {
        NETWORK_CONNECTION,
        NETWORK_TIMEOUT,
        SERVER_ERROR,
        AUTHENTICATION,
        AUTHORIZATION,
        NOT_FOUND,
        VALIDATION,
        UNKNOWN
    }

    /**
     * User-friendly error info with icon and message.
     */
    data class ErrorInfo(
        val icon: String,
        val title: String,
        val message: String,
        val actionLabel: String = "Retry"
    )

    /**
     * Classify error type from error message or exception.
     */
    fun classifyError(error: String?): ErrorType {
        val errorLower = error?.lowercase() ?: ""

        return when {
            // Network connection errors
            errorLower.contains("failed to connect") ||
            errorLower.contains("unable to resolve host") ||
            errorLower.contains("no address associated") ||
            errorLower.contains("network is unreachable") ||
            errorLower.contains("connection refused") ||
            errorLower.contains("no internet") ||
            errorLower.contains("offline") ||
            errorLower.contains("network unavailable") ||
            errorLower.contains("econnrefused") ||
            errorLower.contains("unknownhostexception") -> ErrorType.NETWORK_CONNECTION

            // Timeout errors
            errorLower.contains("timeout") ||
            errorLower.contains("timed out") ||
            errorLower.contains("sockettimeoutexception") -> ErrorType.NETWORK_TIMEOUT

            // Server errors (5xx)
            errorLower.contains("500") ||
            errorLower.contains("502") ||
            errorLower.contains("503") ||
            errorLower.contains("504") ||
            errorLower.contains("server error") ||
            errorLower.contains("internal server") -> ErrorType.SERVER_ERROR

            // Authentication errors
            errorLower.contains("401") ||
            errorLower.contains("unauthorized") ||
            errorLower.contains("not authenticated") ||
            errorLower.contains("invalid token") ||
            errorLower.contains("token expired") ||
            errorLower.contains("login required") -> ErrorType.AUTHENTICATION

            // Authorization errors
            errorLower.contains("403") ||
            errorLower.contains("forbidden") ||
            errorLower.contains("access denied") ||
            errorLower.contains("permission denied") -> ErrorType.AUTHORIZATION

            // Not found errors
            errorLower.contains("404") ||
            errorLower.contains("not found") -> ErrorType.NOT_FOUND

            // Validation errors
            errorLower.contains("validation") ||
            errorLower.contains("invalid") ||
            errorLower.contains("required field") -> ErrorType.VALIDATION

            else -> ErrorType.UNKNOWN
        }
    }

    /**
     * Get user-friendly error info based on error type and screen context.
     */
    fun getErrorInfo(error: String?, context: ScreenContext = ScreenContext.GENERIC): ErrorInfo {
        val errorType = classifyError(error)

        return when (errorType) {
            ErrorType.NETWORK_CONNECTION -> getNetworkConnectionError(context)
            ErrorType.NETWORK_TIMEOUT -> getTimeoutError(context)
            ErrorType.SERVER_ERROR -> getServerError(context)
            ErrorType.AUTHENTICATION -> getAuthenticationError()
            ErrorType.AUTHORIZATION -> getAuthorizationError(context)
            ErrorType.NOT_FOUND -> getNotFoundError(context)
            ErrorType.VALIDATION -> ErrorInfo(
                icon = "⚠️",
                title = "Invalid Input",
                message = error ?: "Please check your input and try again.",
                actionLabel = "Fix & Retry"
            )
            ErrorType.UNKNOWN -> ErrorInfo(
                icon = "❌",
                title = "Something Went Wrong",
                message = error ?: "An unexpected error occurred. Please try again.",
                actionLabel = "Retry"
            )
        }
    }

    /**
     * Get user-friendly error message string (simplified version).
     */
    fun getUserFriendlyMessage(error: String?, context: ScreenContext = ScreenContext.GENERIC): String {
        return getErrorInfo(error, context).message
    }

    private fun getNetworkConnectionError(context: ScreenContext): ErrorInfo {
        val contextMessage = when (context) {
            ScreenContext.DASHBOARD -> "Unable to load your dashboard"
            ScreenContext.VEHICLES -> "Unable to load vehicles"
            ScreenContext.VEHICLE_DETAIL -> "Unable to load vehicle details"
            ScreenContext.DRIVERS -> "Unable to load drivers"
            ScreenContext.DRIVER_DETAIL -> "Unable to load driver details"
            ScreenContext.TRIPS -> "Unable to load trips"
            ScreenContext.TRIP_DETAIL -> "Unable to load trip details"
            ScreenContext.TEAM -> "Unable to load team members"
            ScreenContext.PROFILE -> "Unable to load your profile"
            ScreenContext.LOGIN -> "Unable to sign in"
            ScreenContext.SIGNUP -> "Unable to create account"
            ScreenContext.MAPS -> "Unable to load map data"
            ScreenContext.DOCUMENTS -> "Unable to load documents"
            ScreenContext.GENERIC -> "Unable to connect"
        }

        return ErrorInfo(
            icon = "📡",
            title = "No Internet Connection",
            message = "$contextMessage. Please check your internet connection and try again.",
            actionLabel = "Retry"
        )
    }

    private fun getTimeoutError(context: ScreenContext): ErrorInfo {
        val contextMessage = when (context) {
            ScreenContext.DASHBOARD -> "Dashboard is taking too long to load"
            ScreenContext.VEHICLES -> "Loading vehicles is taking too long"
            ScreenContext.VEHICLE_DETAIL -> "Loading vehicle details is taking too long"
            ScreenContext.DRIVERS -> "Loading drivers is taking too long"
            ScreenContext.DRIVER_DETAIL -> "Loading driver details is taking too long"
            ScreenContext.TRIPS -> "Loading trips is taking too long"
            ScreenContext.TRIP_DETAIL -> "Loading trip details is taking too long"
            ScreenContext.TEAM -> "Loading team members is taking too long"
            ScreenContext.PROFILE -> "Loading your profile is taking too long"
            ScreenContext.LOGIN -> "Sign in is taking too long"
            ScreenContext.SIGNUP -> "Account creation is taking too long"
            ScreenContext.MAPS -> "Loading map is taking too long"
            ScreenContext.DOCUMENTS -> "Loading documents is taking too long"
            ScreenContext.GENERIC -> "Request is taking too long"
        }

        return ErrorInfo(
            icon = "⏱️",
            title = "Connection Timeout",
            message = "$contextMessage. Please check your connection and try again.",
            actionLabel = "Retry"
        )
    }

    private fun getServerError(context: ScreenContext): ErrorInfo {
        return ErrorInfo(
            icon = "🔧",
            title = "Server Issue",
            message = "Our servers are experiencing issues. Please try again in a few moments.",
            actionLabel = "Retry"
        )
    }

    private fun getAuthenticationError(): ErrorInfo {
        return ErrorInfo(
            icon = "🔐",
            title = "Session Expired",
            message = "Your session has expired. Please sign in again to continue.",
            actionLabel = "Sign In"
        )
    }

    private fun getAuthorizationError(context: ScreenContext): ErrorInfo {
        val contextMessage = when (context) {
            ScreenContext.TEAM -> "manage team members"
            ScreenContext.VEHICLES -> "access this vehicle"
            ScreenContext.DRIVERS -> "access this driver"
            ScreenContext.TRIPS -> "access this trip"
            ScreenContext.DOCUMENTS -> "access these documents"
            else -> "perform this action"
        }

        return ErrorInfo(
            icon = "🚫",
            title = "Access Denied",
            message = "You don't have permission to $contextMessage. Please contact your administrator.",
            actionLabel = "Go Back"
        )
    }

    private fun getNotFoundError(context: ScreenContext): ErrorInfo {
        val contextMessage = when (context) {
            ScreenContext.VEHICLE_DETAIL -> "Vehicle not found"
            ScreenContext.DRIVER_DETAIL -> "Driver not found"
            ScreenContext.TRIP_DETAIL -> "Trip not found"
            ScreenContext.PROFILE -> "Profile not found"
            ScreenContext.DOCUMENTS -> "Document not found"
            else -> "Resource not found"
        }

        return ErrorInfo(
            icon = "🔍",
            title = contextMessage,
            message = "The item you're looking for may have been deleted or doesn't exist.",
            actionLabel = "Go Back"
        )
    }
}

/**
 * Extension function to get user-friendly message from any error string.
 */
fun String?.toUserFriendlyError(context: ErrorHandler.ScreenContext = ErrorHandler.ScreenContext.GENERIC): String {
    return ErrorHandler.getUserFriendlyMessage(this, context)
}

/**
 * Extension function to get full error info from any error string.
 */
fun String?.toErrorInfo(context: ErrorHandler.ScreenContext = ErrorHandler.ScreenContext.GENERIC): ErrorHandler.ErrorInfo {
    return ErrorHandler.getErrorInfo(this, context)
}

