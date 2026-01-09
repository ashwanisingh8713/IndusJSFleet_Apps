package com.indusjs.error.code

/**
 * HTTP Error Codes with human-readable messages.
 * Provides standard HTTP status code handling.
 */
enum class HttpErrorCode(
    val code: Int,
    val message: String,
    val isRetryable: Boolean = false
) {
    // 4xx Client Errors
    BAD_REQUEST(400, "Invalid request. Please check your input.", false),
    UNAUTHORIZED(401, "Please log in to continue.", true),
    PAYMENT_REQUIRED(402, "Payment required.", false),
    FORBIDDEN(403, "You don't have permission to access this resource.", false),
    NOT_FOUND(404, "The requested resource was not found.", false),
    METHOD_NOT_ALLOWED(405, "This operation is not allowed.", false),
    NOT_ACCEPTABLE(406, "The requested format is not supported.", false),
    REQUEST_TIMEOUT(408, "Request timed out. Please try again.", true),
    CONFLICT(409, "This operation conflicts with the current state.", false),
    GONE(410, "This resource is no longer available.", false),
    UNPROCESSABLE_ENTITY(422, "The request contains invalid data.", false),
    TOO_MANY_REQUESTS(429, "Too many requests. Please wait and try again.", true),

    // 5xx Server Errors
    INTERNAL_SERVER_ERROR(500, "Server error. Please try again later.", true),
    NOT_IMPLEMENTED(501, "This feature is not yet available.", false),
    BAD_GATEWAY(502, "Server temporarily unavailable. Please try again.", true),
    SERVICE_UNAVAILABLE(503, "Service is temporarily unavailable. Please try again later.", true),
    GATEWAY_TIMEOUT(504, "Server took too long to respond. Please try again.", true);

    companion object {
        /**
         * Get HttpErrorCode from status code
         */
        fun fromCode(code: Int): HttpErrorCode? {
            return entries.find { it.code == code }
        }

        /**
         * Get human-readable message for a status code
         */
        fun getMessage(code: Int): String {
            return fromCode(code)?.message ?: getDefaultMessage(code)
        }

        /**
         * Check if a status code is retryable
         */
        fun isRetryable(code: Int): Boolean {
            return fromCode(code)?.isRetryable ?: (code >= 500)
        }

        /**
         * Get default message for unknown status codes
         */
        private fun getDefaultMessage(code: Int): String {
            return when {
                code in 400..499 -> "Request failed. Please try again."
                code in 500..599 -> "Server error. Please try again later."
                else -> "An unexpected error occurred."
            }
        }

        /**
         * Check if status code indicates client error
         */
        fun isClientError(code: Int): Boolean = code in 400..499

        /**
         * Check if status code indicates server error
         */
        fun isServerError(code: Int): Boolean = code in 500..599

        /**
         * Check if status code indicates success
         */
        fun isSuccess(code: Int): Boolean = code in 200..299
    }
}

