package com.indusjs.fleet.data.datasource.user

import com.indusjs.fleet.core.logger.FleetLogger
import com.indusjs.fleet.network.TAG_USER_LOCAL_DS
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
    suspend fun saveTenantId(tenantId: String)
    suspend fun getTenantId(): String?
    suspend fun setTeamSetupCompleted(completed: Boolean)
    suspend fun isTeamSetupCompleted(): Boolean
    suspend fun saveUserName(name: String)
    suspend fun getUserName(): String?
    suspend fun clearSession()
    suspend fun isLoggedIn(): Boolean
}

/**
 * Implementation of UserLocalDataSource using multiplatform-settings.
 *
 * IMPORTANT: The Settings instance MUST be injected from DI to ensure
 * the same instance is used throughout the app. Using Settings() directly
 * can cause token persistence issues across app restarts.
 */
@Inject
class UserLocalDataSourceImpl(
    private val settings: Settings,
    private val logger: FleetLogger
) : UserLocalDataSource {


    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TENANT_ID = "tenant_id"
        private const val KEY_TEAM_SETUP_COMPLETED = "team_setup_completed"
        private const val KEY_USER_NAME = "user_name"
    }

    override suspend fun saveAuthToken(token: String) {
        logger.d(TAG_USER_LOCAL_DS, "Saving auth token: ${token.take(20)}...")
        settings.putString(KEY_AUTH_TOKEN, token)
        // Verify it was saved
        val saved = settings.getStringOrNull(KEY_AUTH_TOKEN)
        logger.d(TAG_USER_LOCAL_DS, "Verified saved token: ${saved?.take(20)}...")
    }

    override suspend fun getAuthToken(): String? {
        val token = settings.getStringOrNull(KEY_AUTH_TOKEN)
        logger.d(TAG_USER_LOCAL_DS, "Getting auth token: ${token?.take(20) ?: "null"}")
        return token
    }

    override suspend fun saveUserRole(role: String) {
        logger.d(TAG_USER_LOCAL_DS, "Saving user role: $role")
        settings.putString(KEY_USER_ROLE, role)
    }

    override suspend fun getUserRole(): String? {
        return settings.getStringOrNull(KEY_USER_ROLE)
    }

    override suspend fun saveUserId(userId: String) {
        logger.d(TAG_USER_LOCAL_DS, "Saving user id: $userId")
        settings.putString(KEY_USER_ID, userId)
    }

    override suspend fun getUserId(): String? {
        return settings.getStringOrNull(KEY_USER_ID)
    }

    override suspend fun saveTenantId(tenantId: String) {
        logger.d(TAG_USER_LOCAL_DS, "Saving tenant id: $tenantId")
        settings.putString(KEY_TENANT_ID, tenantId)
    }

    override suspend fun getTenantId(): String? {
        return settings.getStringOrNull(KEY_TENANT_ID)
    }

    override suspend fun setTeamSetupCompleted(completed: Boolean) {
        logger.d(TAG_USER_LOCAL_DS, "Setting team setup completed: $completed")
        settings.putBoolean(KEY_TEAM_SETUP_COMPLETED, completed)
    }

    override suspend fun isTeamSetupCompleted(): Boolean {
        return settings.getBoolean(KEY_TEAM_SETUP_COMPLETED, false)
    }

    override suspend fun saveUserName(name: String) {
        logger.d(TAG_USER_LOCAL_DS, "Saving user name: $name")
        settings.putString(KEY_USER_NAME, name)
    }

    override suspend fun getUserName(): String? {
        return settings.getStringOrNull(KEY_USER_NAME)
    }

    override suspend fun clearSession() {
        logger.d(TAG_USER_LOCAL_DS, "Clearing session - removing all auth data")
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USER_ROLE)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_TENANT_ID)
        settings.remove(KEY_TEAM_SETUP_COMPLETED)
        settings.remove(KEY_USER_NAME)
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        val result = token != null && token.isNotBlank()
        logger.d(TAG_USER_LOCAL_DS, "isLoggedIn check: $result (token exists: ${token != null}, token not blank: ${token?.isNotBlank()})")
        return result
    }
}

