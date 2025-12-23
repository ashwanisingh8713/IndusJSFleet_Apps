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
    }

    override suspend fun saveAuthToken(token: String) {
        settings.putString(KEY_AUTH_TOKEN, token)
    }

    override suspend fun getAuthToken(): String? {
        return settings.getStringOrNull(KEY_AUTH_TOKEN)
    }

    override suspend fun clearSession() {
        settings.remove(KEY_AUTH_TOKEN)
    }

    override suspend fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
}

