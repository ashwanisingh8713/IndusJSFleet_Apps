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
    /** Persist the (rotating) refresh token issued alongside the access token. */
    suspend fun saveRefreshToken(refreshToken: String)
    /** The last persisted refresh token, or null for older sessions that predate refresh support. */
    suspend fun getRefreshToken(): String?
    /**
     * Persist the absolute access-token expiry as UTC epoch-millis, derived from the
     * server's per-response `expires_in` (seconds). 0/absent = unknown.
     */
    suspend fun saveTokenExpiresAt(epochMillis: Long)
    suspend fun getTokenExpiresAt(): Long
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
    suspend fun saveUserPermissions(perms: Set<String>)
    suspend fun getUserPermissions(): Set<String>
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
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_TOKEN_EXPIRES_AT = "token_expires_at"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TENANT_ID = "tenant_id"
        private const val KEY_TEAM_SETUP_COMPLETED = "team_setup_completed"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PERMISSIONS = "user_permissions"
        private const val PERMISSIONS_DELIMITER = ","
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

    override suspend fun saveRefreshToken(refreshToken: String) {
        logger.d(TAG_USER_LOCAL_DS, "Saving refresh token: ${refreshToken.take(12)}...")
        settings.putString(KEY_REFRESH_TOKEN, refreshToken)
    }

    override suspend fun getRefreshToken(): String? {
        val token = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        logger.d(TAG_USER_LOCAL_DS, "Getting refresh token: ${token?.take(12) ?: "null"}")
        return token
    }

    override suspend fun saveTokenExpiresAt(epochMillis: Long) {
        logger.d(TAG_USER_LOCAL_DS, "Saving token expiry (epochMillis): $epochMillis")
        settings.putLong(KEY_TOKEN_EXPIRES_AT, epochMillis)
    }

    override suspend fun getTokenExpiresAt(): Long {
        return settings.getLong(KEY_TOKEN_EXPIRES_AT, 0L)
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

    override suspend fun saveUserPermissions(perms: Set<String>) {
        // Permission strings never contain "," so a comma-delimited join is safe.
        val joined = perms.filter { it.isNotBlank() }.joinToString(PERMISSIONS_DELIMITER)
        logger.d(TAG_USER_LOCAL_DS, "Saving ${perms.size} user permissions")
        settings.putString(KEY_USER_PERMISSIONS, joined)
    }

    override suspend fun getUserPermissions(): Set<String> {
        val joined = settings.getStringOrNull(KEY_USER_PERMISSIONS).orEmpty()
        if (joined.isBlank()) return emptySet()
        return joined.split(PERMISSIONS_DELIMITER)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()
    }

    override suspend fun clearSession() {
        logger.d(TAG_USER_LOCAL_DS, "Clearing session - removing all auth data")
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_TOKEN_EXPIRES_AT)
        settings.remove(KEY_USER_ROLE)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_TENANT_ID)
        settings.remove(KEY_TEAM_SETUP_COMPLETED)
        settings.remove(KEY_USER_NAME)
        settings.remove(KEY_USER_PERMISSIONS)
    }

    override suspend fun isLoggedIn(): Boolean {
        val token = getAuthToken()
        val result = token != null && token.isNotBlank()
        logger.d(TAG_USER_LOCAL_DS, "isLoggedIn check: $result (token exists: ${token != null}, token not blank: ${token?.isNotBlank()})")
        return result
    }
}

