package com.indusjs.fleet.core.auth

import co.touchlab.kermit.Logger
import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import dev.zacsweers.metro.Inject

/**
 * Presentation-layer authentication facade for the Fleet app.
 *
 * Wraps [UserLocalDataSource] (from ijs-network-lib) to provide
 * convenient auth token access for ViewModels and DI graphs.
 *
 * Also registers the session clear callback with [AuthenticationManager]
 * so that 401 responses and session expiry events clear the local session.
 *
 * Usage:
 * ```kotlin
 * // In AppInitializer or App.kt:
 * authManager.registerSessionClearCallback()
 *
 * // In ViewModels (if needed):
 * val isLoggedIn = authManager.isLoggedIn()
 * val role = authManager.getUserRole()
 * ```
 */
@Inject
class AuthManager(
    private val userLocalDataSource: UserLocalDataSource
) {
    private val log = Logger.withTag("AuthManager")

    /**
     * Get the current auth token, or null if not logged in.
     */
    suspend fun getToken(): String? = userLocalDataSource.getAuthToken()

    /**
     * Get the current user's role (owner, manager, supervisor, driver).
     */
    suspend fun getUserRole(): String? = userLocalDataSource.getUserRole()

    /**
     * Get the current user's ID.
     */
    suspend fun getUserId(): String? = userLocalDataSource.getUserId()

    /**
     * Check if the user is currently logged in.
     */
    suspend fun isLoggedIn(): Boolean = userLocalDataSource.isLoggedIn()

    /**
     * Clear the local session (auth token, user role, user ID).
     */
    suspend fun clearSession() {
        log.d { "Clearing session via AuthManager" }
        userLocalDataSource.clearSession()
    }

    /**
     * Register the session clear callback with [AuthenticationManager].
     * This ensures that when a 401 or session expiry event occurs,
     * the local session is automatically cleared before navigating to login.
     *
     * Call this once at app startup (e.g., in AppInitializer or App.kt).
     */
    fun registerSessionClearCallback() {
        log.d { "Registering session clear callback with AuthenticationManager" }
        AuthenticationManager.registerSessionClearCallback {
            clearSession()
        }
    }
}

