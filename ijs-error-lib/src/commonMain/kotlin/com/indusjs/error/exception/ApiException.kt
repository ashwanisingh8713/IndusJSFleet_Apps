package com.indusjs.error.exception

import com.indusjs.error.code.HttpErrorCode

/**
 * Exception thrown when API call fails.
 *
 * @param message Human-readable error message
 * @param httpCode HTTP status code (e.g., 400, 500)
 * @param errorBody Raw error body from the API response
 * @param cause The underlying exception
 */
class ApiException(
    message: String,
    val httpCode: Int? = null,
    val errorBody: String? = null,
    cause: Throwable? = null
) : IjsException(message, cause) {

    override val errorCode: String = "API_${httpCode ?: "UNKNOWN"}"

    override val isRecoverable: Boolean = when (httpCode) {
        in 400..499 -> httpCode != 429 // Client errors except rate limit
        in 500..599 -> true // Server errors are typically recoverable
        else -> true
    }

    /**
     * Get the HTTP error code enum for this exception
     */
    val httpErrorCode: HttpErrorCode?
        get() = httpCode?.let { HttpErrorCode.fromCode(it) }

    /**
     * Check if this is a client error (4xx)
     */
    val isClientError: Boolean
        get() = httpCode != null && httpCode in 400..499

    /**
     * Check if this is a server error (5xx)
     */
    val isServerError: Boolean
        get() = httpCode != null && httpCode in 500..599

    companion object {
        /**
         * Create a bad request error (400)
         */
        fun badRequest(
            message: String = "Invalid request. Please check your input.",
            errorBody: String? = null,
            cause: Throwable? = null
        ) = ApiException(message, 400, errorBody, cause)

        /**
         * Create a not found error (404)
         */
        fun notFound(
            message: String = "Resource not found.",
            errorBody: String? = null,
            cause: Throwable? = null
        ) = ApiException(message, 404, errorBody, cause)

        /**
         * Create a server error (500)
         */
        fun serverError(
            message: String = "Server error. Please try again later.",
            errorBody: String? = null,
            cause: Throwable? = null
        ) = ApiException(message, 500, errorBody, cause)

        /**
         * Create a rate limit error (429)
         */
        fun rateLimited(
            message: String = "Too many requests. Please wait and try again.",
            errorBody: String? = null,
            cause: Throwable? = null
        ) = ApiException(message, 429, errorBody, cause)

        /**
         * Create from HTTP status code
         */
        fun fromCode(
            code: Int,
            message: String? = null,
            errorBody: String? = null,
            cause: Throwable? = null
        ): ApiException {
            val httpError = HttpErrorCode.fromCode(code)
            val finalMessage = message ?: httpError?.message ?: "Request failed with status $code"
            return ApiException(finalMessage, code, errorBody, cause)
        }
    }
}

