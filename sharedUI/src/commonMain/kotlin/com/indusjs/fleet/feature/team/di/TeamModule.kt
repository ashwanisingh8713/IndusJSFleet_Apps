package com.indusjs.fleet.feature.team.di

import com.indusjs.fleet.core.network.HttpClientProvider
import com.indusjs.fleet.feature.team.data.datasource.TeamRemoteDataSource
import com.indusjs.fleet.feature.team.data.datasource.TeamRemoteDataSourceImpl
import com.indusjs.fleet.feature.team.data.repository.TeamRepositoryImpl
import com.indusjs.fleet.feature.team.domain.repository.TeamRepository
import com.indusjs.fleet.feature.user.di.UserModule
import io.ktor.client.HttpClient

/**
 * Dependency provider for Team Module.
 */
object TeamModule {

    private var teamRemoteDataSource: TeamRemoteDataSource? = null
    private var teamRepository: TeamRepository? = null

    /**
     * Get or create TeamRemoteDataSource instance.
     */
    fun provideTeamRemoteDataSource(): TeamRemoteDataSource {
        return teamRemoteDataSource ?: TeamRemoteDataSourceImpl(
            httpClient = UserModule.provideHttpClient()
        ).also { teamRemoteDataSource = it }
    }

    /**
     * Get or create TeamRepository instance.
     */
    fun provideTeamRepository(): TeamRepository {
        return teamRepository ?: TeamRepositoryImpl(
            remoteDataSource = provideTeamRemoteDataSource(),
            userLocalDataSource = UserModule.provideUserLocalDataSource()
        ).also { teamRepository = it }
    }

    /**
     * Clear all cached instances.
     */
    fun reset() {
        teamRemoteDataSource = null
        teamRepository = null
    }
}

