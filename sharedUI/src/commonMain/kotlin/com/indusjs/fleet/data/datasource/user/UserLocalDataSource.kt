package com.indusjs.fleet.data.datasource.user

import co.touchlab.kermit.Logger
import com.indusjs.fleet.data.datasource.LocalDataSource
import dev.zacsweers.metro.Inject
import com.russhwolf.settings.Settings

/**
 * Local data source for user session storage.
 */
interface UserLocalDataSource : LocalDataSource {
    suspend fun saveAuthToken(token: String)
    suspend fun getAuthToken(): String?
    suspend fun saveUserRole(role: String)
    suspend fun getUserRole(): String?
    suspend fun saveUserId(userId: String)
    suspend fun getUserId(): String?
    suspend fun clearSession()
    suspend fun isLoggedIn(): Boolean
}

/**
 * Implementation of UserLocalDataSource using multiplatform-settings.
 */
@Inject
class UserLocalDataSourceImpl(
    private val settings: Settings = Settings()
) : UserLocalDataSource {

    private val log = Logger.withTag("UserLocalDataSource")

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
    }

    override suspend fun saveAuthToken(token: String) {
        log.d { "Saving auth token: ${token.take(20)}..." }
        settings.putString(KEY_AUTH_TOKEN, token)
        // Verify it was saved
        val saved = settings.getStringOrNull(KEY_AUTH_TOKEN)
        log.d { "Verified saved token: ${saved?.take(20)}..." }
    }

    override suspend fun getAuthToken(): String? {
        val token = settings.getStringOrNull(KEY_AUTH_TOKEN)
        log.d { "Getting auth token: ${token?.take(20) ?: "null"}" }
        return token
    }

    override suspend fun saveUserRole(role: String) {
        log.d { "Saving user role: $role" }
        settings.putString(KEY_USER_ROLE, role)
    }

    override suspend fun getUserRole(): String? {
        return settings.getStringOrNull(KEY_USER_ROLE)
    }

    override suspend fun saveUserId(userId: String) {
        log.d { "Saving user id: $userId" }
        settings.putString(KEY_USER_ID, userId)
    }

    override suspend fun getUserId(): String? {
        return settings.getStringOrNull(KEY_USER_ID)
    }

    override suspend fun clearSession() {
        log.d { "Clearing session - removing all auth data" }
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USER_ROLE)
        settings.remove(KEY_USER_ID)
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        val result = token != null && token.isNotBlank()
        log.d { "isLoggedIn check: $result (token exists: ${token != null}, token not blank: ${token?.isNotBlank()})" }
        return result
    }
}

