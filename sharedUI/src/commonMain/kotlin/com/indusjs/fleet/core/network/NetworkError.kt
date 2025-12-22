package com.indusjs.fleet.core.network

/**
 * Network error types for standardized error handling.
 */
sealed class NetworkError : Exception() {

    /**
     * No internet connection available.
     */
    data object NoConnection : NetworkError() {
        private fun readResolve(): Any = NoConnection
        override val message: String = "No internet connection"
    }

    /**
     * Request timed out.
     */
    data object Timeout : NetworkError() {
        private fun readResolve(): Any = Timeout
        override val message: String = "Request timed out"
    }

    /**
     * Server returned an error.
     */
    data class ServerError(
        val code: Int,
        override val message: String
    ) : NetworkError()

    /**
     * Unknown error occurred.
     */
    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkError()

    /**
     * Parsing error for response data.
     */
    data class ParseError(
        override val message: String,
        override val cause: Throwable? = null
    ) : NetworkError()
}

