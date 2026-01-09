package com.indusjs.fleet.data.repository.team

import com.indusjs.error.exception.ApiException
import com.indusjs.error.exception.AuthException
import com.indusjs.fleet.data.datasource.team.TeamRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.indusjs.fleet.data.mapper.team.TeamMapper.toDomain
import com.indusjs.fleet.data.model.team.CreateTeamMemberRequest
import com.indusjs.fleet.data.model.team.ResetPasswordRequest
import com.indusjs.fleet.data.model.team.UpdateTeamMemberRequest
import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import com.indusjs.fleet.domain.repository.team.TeamRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of TeamRepository.
 * Handles team member CRUD operations via remote data source.
 */
@Inject
class TeamRepositoryImpl(
    private val remoteDataSource: TeamRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource
) : TeamRepository {

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

        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to create team member")
    }

    override suspend fun getTeamMembers(): Result<List<TeamMember>> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getTeamMembers(token = token)

        if (response.success && response.data != null) {
            response.data.team?.toDomain() ?: emptyList()
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

        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to update team member")
    }

    override suspend fun toggleTeamMemberActive(id: String): Result<TeamMember> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.toggleTeamMemberActive(token = token, id = id)

        response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to toggle team member status")
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
    }

    /**
     * Retrieves auth token or throws AuthException.
     */
    private suspend fun requireAuthToken(): String {
        return userLocalDataSource.getAuthToken()
            ?: throw AuthException.unauthenticated()
    }
}

