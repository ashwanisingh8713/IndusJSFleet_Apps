package com.indusjs.fleet.feature.user.di

import com.indusjs.fleet.core.network.HttpClientProvider
import com.indusjs.fleet.feature.user.data.datasource.UserLocalDataSource
import com.indusjs.fleet.feature.user.data.datasource.UserLocalDataSourceImpl
import com.indusjs.fleet.feature.user.data.datasource.UserRemoteDataSource
import com.indusjs.fleet.feature.user.data.datasource.UserRemoteDataSourceImpl
import com.indusjs.fleet.feature.user.data.repository.UserRepositoryImpl
import com.indusjs.fleet.feature.user.domain.repository.UserRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient

/**
 * Dependency provider for User Module.
 * In production, use Metro DI framework for proper injection.
 */
object UserModule {

    private var httpClient: HttpClient? = null
    private var userRemoteDataSource: UserRemoteDataSource? = null
    private var userLocalDataSource: UserLocalDataSource? = null
    private var userRepository: UserRepository? = null

    /**
     * Get or create HttpClient instance.
     */
    fun provideHttpClient(): HttpClient {
        return httpClient ?: HttpClientProvider.create().also { httpClient = it }
    }

    /**
     * Get or create UserRemoteDataSource instance.
     */
    fun provideUserRemoteDataSource(): UserRemoteDataSource {
        return userRemoteDataSource ?: UserRemoteDataSourceImpl(
            httpClient = provideHttpClient()
        ).also { userRemoteDataSource = it }
    }

    /**
     * Get or create UserLocalDataSource instance.
     */
    fun provideUserLocalDataSource(): UserLocalDataSource {
        return userLocalDataSource ?: UserLocalDataSourceImpl(
            settings = Settings()
        ).also { userLocalDataSource = it }
    }

    /**
     * Get or create UserRepository instance.
     */
    fun provideUserRepository(): UserRepository {
        return userRepository ?: UserRepositoryImpl(
            remoteDataSource = provideUserRemoteDataSource(),
            localDataSource = provideUserLocalDataSource()
        ).also { userRepository = it }
    }

    /**
     * Clear all cached instances (useful for logout/testing).
     */
    fun reset() {
        httpClient?.close()
        httpClient = null
        userRemoteDataSource = null
        userLocalDataSource = null
        userRepository = null
    }
}

