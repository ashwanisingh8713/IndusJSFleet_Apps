package com.indusjs.error.code

/**
 * Centralized error message provider.
 * Maps error types and codes to human-readable messages.
 */
object ErrorMessages {

    // ==================== Network Error Messages ====================

    object Network {
        const val NO_CONNECTION = "No internet connection. Please check your network settings."
        const val CONNECTION_LOST = "Connection lost. Please check your internet and try again."
        const val TIMEOUT = "Request timed out. Please try again."
        const val DNS_FAILURE = "Unable to reach the server. Please check your connection."
        const val SSL_ERROR = "Secure connection failed. Please try again later."
        const val UNKNOWN = "Network error occurred. Please try again."

        fun forType(type: String): String {
            return when (type.uppercase()) {
                "CONNECTION" -> NO_CONNECTION
                "TIMEOUT" -> TIMEOUT
                "DNS" -> DNS_FAILURE
                "SSL" -> SSL_ERROR
                else -> UNKNOWN
            }
        }
    }

    // ==================== Auth Error Messages ====================

    object Auth {
        const val UNAUTHENTICATED = "Please log in to continue."
        const val SESSION_EXPIRED = "Your session has expired. Please log in again."
        const val UNAUTHORIZED = "You don't have permission to perform this action."
        const val INVALID_CREDENTIALS = "Invalid email or password. Please try again."
        const val ACCOUNT_LOCKED = "Your account has been locked. Please contact support."
        const val ACCOUNT_DISABLED = "Your account has been disabled. Please contact support."
    }

    // ==================== Validation Error Messages ====================

    object Validation {
        const val REQUIRED = "This field is required"
        const val INVALID_EMAIL = "Please enter a valid email address"
        const val INVALID_PHONE = "Please enter a valid phone number"
        const val INVALID_DATE = "Please enter a valid date (DD-MM-YYYY)"
        const val INVALID_TIME = "Please enter a valid time (HH:MM)"
        const val TOO_SHORT = "This field is too short"
        const val TOO_LONG = "This field is too long"
        const val INVALID_FORMAT = "Invalid format"

        fun required(fieldName: String): String = "$fieldName is required"
        fun invalidFormat(fieldName: String): String = "$fieldName format is invalid"
        fun minLength(fieldName: String, min: Int): String = "$fieldName must be at least $min characters"
        fun maxLength(fieldName: String, max: Int): String = "$fieldName must be at most $max characters"
        fun range(fieldName: String, min: Number, max: Number): String = "$fieldName must be between $min and $max"
    }

    // ==================== API Error Messages ====================

    object Api {
        const val BAD_REQUEST = "Invalid request. Please check your input."
        const val NOT_FOUND = "The requested item was not found."
        const val SERVER_ERROR = "Server error. Please try again later."
        const val SERVICE_UNAVAILABLE = "Service is temporarily unavailable. Please try again later."
        const val RATE_LIMITED = "Too many requests. Please wait and try again."
        const val CONFLICT = "This operation conflicts with the current state."
        const val UNKNOWN = "An unexpected error occurred. Please try again."

        /**
         * Get message for HTTP status code
         */
        fun forStatusCode(code: Int): String {
            return HttpErrorCode.getMessage(code)
        }
    }

    // ==================== Generic Error Messages ====================

    object Generic {
        const val UNKNOWN = "Something went wrong. Please try again."
        const val RETRY = "An error occurred. Please try again."
        const val CONTACT_SUPPORT = "An error occurred. Please contact support if the problem persists."
    }

    // ==================== Action Labels ====================

    object Actions {
        const val RETRY = "Retry"
        const val TRY_AGAIN = "Try Again"
        const val GO_BACK = "Go Back"
        const val SIGN_IN = "Sign In"
        const val REFRESH = "Refresh"
        const val DISMISS = "Dismiss"
        const val CONTACT_SUPPORT = "Contact Support"
        const val FIX_AND_RETRY = "Fix & Retry"
    }
}

