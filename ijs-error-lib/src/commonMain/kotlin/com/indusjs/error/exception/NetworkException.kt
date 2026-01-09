package com.indusjs.error.exception

/**
 * Exception thrown when network connectivity issues occur.
 *
 * @param message Human-readable error message
 * @param type The specific type of network error
 * @param cause The underlying exception that caused this error
 */
class NetworkException(
    message: String = "Network unavailable. Please check your connection.",
    val type: Type = Type.CONNECTION,
    cause: Throwable? = null
) : IjsException(message, cause) {

    override val errorCode: String = "NETWORK_${type.name}"

    override val isRecoverable: Boolean = true

    /**
     * Types of network errors
     */
    enum class Type {
        /** No internet connection */
        CONNECTION,
        /** Request timed out */
        TIMEOUT,
        /** DNS resolution failed */
        DNS,
        /** SSL/TLS handshake failed */
        SSL,
        /** Connection was reset */
        RESET,
        /** Unknown network error */
        UNKNOWN
    }

    companion object {
        /**
         * Create a connection error
         */
        fun connection(
            message: String = "No internet connection. Please check your network.",
            cause: Throwable? = null
        ) = NetworkException(message, Type.CONNECTION, cause)

        /**
         * Create a timeout error
         */
        fun timeout(
            message: String = "Request timed out. Please try again.",
            cause: Throwable? = null
        ) = NetworkException(message, Type.TIMEOUT, cause)

        /**
         * Create a DNS error
         */
        fun dns(
            message: String = "Unable to reach server. Please check your connection.",
            cause: Throwable? = null
        ) = NetworkException(message, Type.DNS, cause)

        /**
         * Create an SSL error
         */
        fun ssl(
            message: String = "Secure connection failed. Please try again.",
            cause: Throwable? = null
        ) = NetworkException(message, Type.SSL, cause)
    }
}

