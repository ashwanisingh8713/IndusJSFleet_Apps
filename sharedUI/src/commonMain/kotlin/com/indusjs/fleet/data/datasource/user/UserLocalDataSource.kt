package com.indusjs.fleet.data.datasource.user

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

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
    }

    override suspend fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    override suspend fun getAuthToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    override suspend fun saveUserRole(role: String) {
        settings.putString(KEY_USER_ROLE, role)
    }

    override suspend fun getUserRole(): String? {
        return settings.getStringOrNull(KEY_USER_ROLE)
    }

    override suspend fun saveUserId(userId: String) {
        settings.putString(KEY_USER_ID, userId)
    }

    override suspend fun getUserId(): String? {
        return settings.getStringOrNull(KEY_USER_ID)
    }

    override suspend fun clearSession() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USER_ROLE)
        settings.remove(KEY_USER_ID)
    }

    override suspend fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
}

