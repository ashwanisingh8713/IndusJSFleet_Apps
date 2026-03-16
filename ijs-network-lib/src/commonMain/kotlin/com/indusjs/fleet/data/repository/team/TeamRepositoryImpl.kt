package com.indusjs.fleet.data.repository.team

import co.touchlab.kermit.Logger
import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.data.datasource.team.TeamLocalDataSource
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.team.TeamMapper.toDomain
import com.indusjs.fleet.data.model.team.CreateTeamMemberRequest
import com.indusjs.fleet.data.model.team.ResetPasswordRequest
import com.indusjs.fleet.data.model.team.TeamMemberDto
import com.indusjs.fleet.data.model.team.UpdateTeamMemberRequest
import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import com.indusjs.fleet.domain.repository.team.TeamRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of TeamRepository.
 * Handles team member CRUD operations via remote data source with local caching.
 */
@Inject
class TeamRepositoryImpl(
    private val remoteDataSource: TeamRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val localDataSource: TeamLocalDataSource
) : TeamRepository {

    private val log = Logger.withTag("TeamRepository")

    override suspend fun createTeamMember(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String,
        role: TeamMemberRole
    ): Result<TeamMember> = runCatching {
        val token = requireAuthToken()

        val response = remoteDataSource.createTeamMember(
            token = token,
            request = CreateTeamMemberRequest(
                email = email,
                mobile = mobile,
                password = password,
                firstName = firstName,
                lastName = lastName,
                role = role.toApiString()
            )
        )

        val teamMember = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to create team member")

        // Save to local cache
        response.data?.let { dto ->
            localDataSource.saveTeamMember(dto)
            log.d { "Saved new team member to cache: ${teamMember.fullName}" }
        }

        teamMember
    }

    override suspend fun getTeamMembers(): Result<List<TeamMember>> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getTeamMembers(token = token)

        if (response.success && response.data != null) {
            val members = response.data.team?.toDomain() ?: emptyList()

            // Save to local cache
            response.data.team?.let { dtos ->
                localDataSource.saveTeamMembers(dtos)
                log.d { "Saved ${dtos.size} team members to cache" }
            }

            members
        } else {
            throw ApiException(response.message ?: "Failed to get team members")
        }
    }

    override suspend fun getTeamMembersByRole(role: TeamMemberRole): Result<List<TeamMember>> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getTeamMembers(
            token = token,
            role = role.toApiString()
        )

        if (response.success && response.data != null) {
            response.data.team?.toDomain() ?: emptyList()
        } else {
            throw ApiException(response.message ?: "Failed to get team members")
        }
    }

    override suspend fun getTeamMember(id: String): Result<TeamMember> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getTeamMember(token = token, id = id)

        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to get team member")
    }

    override suspend fun updateTeamMember(
        id: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        mobile: String?,
        role: TeamMemberRole?,
        isActive: Boolean?
    ): Result<TeamMember> = runCatching {
        val token = requireAuthToken()

        val response = remoteDataSource.updateTeamMember(
            token = token,
            id = id,
            request = UpdateTeamMemberRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                mobile = mobile,
                role = role?.toApiString(),
                isActive = isActive
            )
        )

        val teamMember = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to update team member")

        // Update local cache
        response.data?.let { dto ->
            localDataSource.saveTeamMember(dto)
            log.d { "Updated team member in cache: ${teamMember.fullName}" }
        }

        teamMember
    }

    override suspend fun toggleTeamMemberActive(id: String): Result<TeamMember> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.toggleTeamMemberActive(token = token, id = id)

        val teamMember = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to toggle team member status")

        // Update local cache
        response.data?.let { dto ->
            localDataSource.saveTeamMember(dto)
            log.d { "Updated team member status in cache: ${teamMember.fullName}" }
        }

        teamMember
    }

    override suspend fun resetTeamMemberPassword(id: String, newPassword: String): Result<Unit> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.resetTeamMemberPassword(
            token = token,
            id = id,
            request = ResetPasswordRequest(newPassword = newPassword)
        )

        if (!response.success) {
            throw ApiException(response.message ?: "Failed to reset password")
        }
    }

    override suspend fun deleteTeamMember(id: String): Result<Unit> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.deleteTeamMember(token = token, id = id)

        if (!response.success) {
            throw ApiException(response.message ?: "Failed to delete team member")
        }

        // Remove from local cache
        localDataSource.deleteTeamMember(id.toIntOrNull() ?: 0)
        log.d { "Deleted team member from cache: $id" }
    }

    // ==================== Local Cache Methods ====================

    override suspend fun getTeamMembersFromCache(): Result<List<TeamMember>> = runCatching {
        localDataSource.getTeamMembers().map { it.toDomain() }
    }

    override suspend fun getCaretakersFromCache(): Result<List<TeamMember>> = runCatching {
        localDataSource.getCaretakers().map { it.toDomain() }
    }

    override suspend fun refreshTeamMembers(): Result<List<TeamMember>> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getTeamMembers(token = token)

        if (response.success && response.data != null) {
            val members = response.data.team?.toDomain() ?: emptyList()

            // Save to local cache
            response.data.team?.let { dtos ->
                localDataSource.saveTeamMembers(dtos)
                log.d { "Refreshed ${dtos.size} team members in cache" }
            }

            members
        } else {
            throw ApiException(response.message ?: "Failed to refresh team members")
        }
    }

    /**
     * Retrieves auth token or emits session expired event and throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return AuthTokenHelper.requireAuthTokenOrRedirect {
            userLocalDataSource.getAuthToken()
        }
    }
}

