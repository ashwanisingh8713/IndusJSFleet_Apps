package com.indusjs.error.exception

/**
 * Exception thrown for authentication and authorization errors.
 *
 * @param message Human-readable error message
 * @param type The type of auth error (authentication vs authorization)
 * @param cause The underlying exception
 */
class AuthException(
    message: String,
    val type: Type = Type.UNAUTHENTICATED,
    cause: Throwable? = null
) : IjsException(message, cause) {

    override val errorCode: String = "AUTH_${type.name}"

    override val isRecoverable: Boolean = when (type) {
        Type.UNAUTHENTICATED -> true // Can re-login
        Type.TOKEN_EXPIRED -> true // Can refresh token
        Type.UNAUTHORIZED -> false // Permission issue
        Type.INVALID_CREDENTIALS -> true // Can retry with correct credentials
    }

    /**
     * Whether user needs to re-authenticate
     */
    val requiresReAuthentication: Boolean
        get() = type in listOf(Type.UNAUTHENTICATED, Type.TOKEN_EXPIRED, Type.INVALID_CREDENTIALS)

    /**
     * Types of authentication/authorization errors
     */
    enum class Type {
        /** User is not logged in (401) */
        UNAUTHENTICATED,
        /** Token has expired (401) */
        TOKEN_EXPIRED,
        /** User doesn't have permission (403) */
        UNAUTHORIZED,
        /** Invalid username/password */
        INVALID_CREDENTIALS
    }

    companion object {
        /**
         * Create an unauthenticated error
         */
        fun unauthenticated(
            message: String = "Please log in to continue.",
            cause: Throwable? = null
        ) = AuthException(message, Type.UNAUTHENTICATED, cause)

        /**
         * Create a token expired error
         */
        fun tokenExpired(
            message: String = "Your session has expired. Please log in again.",
            cause: Throwable? = null
        ) = AuthException(message, Type.TOKEN_EXPIRED, cause)

        /**
         * Create an unauthorized error (403)
         */
        fun unauthorized(
            message: String = "You don't have permission to perform this action.",
            cause: Throwable? = null
        ) = AuthException(message, Type.UNAUTHORIZED, cause)

        /**
         * Create an invalid credentials error
         */
        fun invalidCredentials(
            message: String = "Invalid email or password.",
            cause: Throwable? = null
        ) = AuthException(message, Type.INVALID_CREDENTIALS, cause)
    }
}

