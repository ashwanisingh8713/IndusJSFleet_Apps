package com.indusjs.fleet.di

import com.indusjs.fleet.core.dispatcher.DispatcherProvider
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSource
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSourceImpl
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.repository.team.TeamRepositoryImpl
import com.indusjs.fleet.domain.repository.team.TeamRepository
import com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel
import com.indusjs.fleet.presentation.team.list.TeamListViewModel
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient

/**
 * Scope marker for the Team feature.
 */
abstract class TeamFeatureScope private constructor()

/**
 * Dependency graph for the Team feature.
 * Contains all dependencies needed for team-related operations.
 *
 * Usage:
 * ```kotlin
 * val teamGraph = TeamFeatureGraph.Factory::class.create(
 *     httpClient = AppDependencies.httpClient,
 *     dispatcherProvider = AppDependencies.dispatcherProvider,
 *     userLocalDataSource = userGraph.userLocalDataSource
 * )
 * ```
 */
@SingleIn(TeamFeatureScope::class)
@DependencyGraph
abstract class TeamFeatureGraph {

    @Binds
    abstract fun bindTeamRemoteDataSource(impl: TeamRemoteDataSourceImpl): TeamRemoteDataSource

    @Binds
    abstract fun bindTeamRepository(impl: TeamRepositoryImpl): TeamRepository

    abstract val teamRepository: TeamRepository
    abstract val teamListViewModel: TeamListViewModel
    abstract val createTeamMemberViewModel: CreateTeamMemberViewModel
    abstract val dispatcherProvider: DispatcherProvider

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides httpClient: HttpClient,
            @Provides dispatcherProvider: DispatcherProvider,
            @Provides userLocalDataSource: UserLocalDataSource
        ): TeamFeatureGraph
    }
}

