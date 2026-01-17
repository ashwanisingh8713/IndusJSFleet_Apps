package com.indusjs.fleet.core.auth

import com.indusjs.error.exception.AuthException

/**
 * Helper object for handling auth token requirements across repositories.
 *
 * When the auth token is null (user not logged in or session cleared),
 * this helper emits a SessionExpired event to trigger navigation to login,
 * and then throws an AuthException.
 *
 * This approach ensures:
 * 1. Race condition prevention: Uses AuthenticationManager's isHandlingUnauthorized flag
 * 2. Different messages for null token vs 401 expired for debugging
 * 3. Pending API calls fail naturally - no special cancellation needed
 */
object AuthTokenHelper {

    /**
     * Message shown when auth token is null (user not logged in).
     */
    const val MESSAGE_NOT_LOGGED_IN = "Please log in to continue."

    /**
     * Message shown when session expired (401 from server).
     * This is already used by HttpClientProvider for 401 responses.
     */
    const val MESSAGE_SESSION_EXPIRED = "Your session has expired. Please log in again."

    /**
     * Gets the auth token or emits a session expired event and throws AuthException.
     *
     * Use this in repository methods that require authentication.
     * The null token case uses a different message ("Please log in to continue")
     * than the 401 expired case ("Your session has expired") to help with debugging.
     *
     * Race conditions are prevented by AuthenticationManager's isHandlingUnauthorized flag,
     * which ensures only one auth event is processed at a time.
     *
     * @param getToken Suspend function to retrieve the auth token
     * @return The auth token if available
     * @throws AuthException if the token is null
     *
     * Example usage:
     * ```kotlin
     * private suspend fun requireAuthToken(): String {
     *     return AuthTokenHelper.requireAuthTokenOrRedirect {
     *         userLocalDataSource.getAuthToken()
     *     }
     * }
     * ```
     */
    suspend fun requireAuthTokenOrRedirect(
        getToken: suspend () -> String?
    ): String {
        val token = getToken()
        if (token == null) {
            // Emit session expired event to trigger navigation to login
            // Uses MESSAGE_NOT_LOGGED_IN to differentiate from 401 expired
            AuthenticationManager.emitSessionExpired(MESSAGE_NOT_LOGGED_IN)
            throw AuthException.unauthenticated(MESSAGE_NOT_LOGGED_IN)
        }
        return token
    }

    /**
     * Checks if the given exception is an authentication exception that requires re-login.
     * Useful for repositories that catch exceptions and want to handle auth errors specially.
     */
    fun isAuthenticationRequired(exception: Throwable): Boolean {
        return exception is AuthException && exception.requiresReAuthentication
    }
}
