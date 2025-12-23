package com.indusjs.fleet.feature.team.data.repository

import com.indusjs.fleet.feature.team.data.datasource.TeamRemoteDataSource
import com.indusjs.fleet.feature.team.data.mapper.TeamMapper.toDomain
import com.indusjs.fleet.feature.team.data.model.CreateTeamMemberRequest
import com.indusjs.fleet.feature.team.data.model.UpdateTeamMemberRequest
import com.indusjs.fleet.feature.team.domain.entity.TeamMember
import com.indusjs.fleet.feature.team.domain.entity.TeamMemberRole
import com.indusjs.fleet.feature.team.domain.repository.TeamRepository
import com.indusjs.fleet.feature.user.data.datasource.UserLocalDataSource
import dev.zacsweers.metro.Inject

/**
 * Implementation of TeamRepository.
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
        val token = userLocalDataSource.getAuthToken()
            ?: throw Exception("Not authenticated. Please log in.")

        val roleString = when (role) {
            TeamMemberRole.MANAGER -> "manager"
            TeamMemberRole.SUPERVISOR -> "supervisor"
        }

        val response = remoteDataSource.createTeamMember(
            token = token,
            request = CreateTeamMemberRequest(
                email = email,
                mobile = mobile,
                password = password,
                firstName = firstName,
                lastName = lastName,
                role = roleString
            )
        )

        if (response.success && response.data != null) {
            response.data.toDomain()
        } else {
            throw Exception(response.message ?: "Failed to create team member")
        }
    }

    override suspend fun getTeamMembers(): Result<List<TeamMember>> = runCatching {
        val token = userLocalDataSource.getAuthToken()
            ?: throw Exception("Not authenticated. Please log in.")

        val response = remoteDataSource.getTeamMembers(token = token)

        if (response.success && response.data != null) {
            // Access nested team array: data.team
            response.data.team?.toDomain() ?: emptyList()
        } else {
            throw Exception(response.message ?: "Failed to get team members")
        }
    }

    override suspend fun getTeamMembersByRole(role: TeamMemberRole): Result<List<TeamMember>> = runCatching {
        val token = userLocalDataSource.getAuthToken()
            ?: throw Exception("Not authenticated. Please log in.")

        val roleString = when (role) {
            TeamMemberRole.MANAGER -> "manager"
            TeamMemberRole.SUPERVISOR -> "supervisor"
        }

        val response = remoteDataSource.getTeamMembers(token = token, role = roleString)

        if (response.success && response.data != null) {
            // Access nested team array: data.team
            response.data.team?.toDomain() ?: emptyList()
        } else {
            throw Exception(response.message ?: "Failed to get team members")
        }
    }

    override suspend fun getTeamMember(id: String): Result<TeamMember> = runCatching {
        val token = userLocalDataSource.getAuthToken()
            ?: throw Exception("Not authenticated. Please log in.")

        val response = remoteDataSource.getTeamMember(token = token, id = id)

        if (response.success && response.data != null) {
            response.data.toDomain()
        } else {
            throw Exception(response.message ?: "Failed to get team member")
        }
    }

    override suspend fun updateTeamMember(
        id: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        mobile: String?,
        isActive: Boolean?
    ): Result<TeamMember> = runCatching {
        val token = userLocalDataSource.getAuthToken()
            ?: throw Exception("Not authenticated. Please log in.")

        val response = remoteDataSource.updateTeamMember(
            token = token,
            id = id,
            request = UpdateTeamMemberRequest(
                firstName = firstName,
                lastName = lastName,
                email = email,
                mobile = mobile,
                isActive = isActive
            )
        )

        if (response.success && response.data != null) {
            response.data.toDomain()
        } else {
            throw Exception(response.message ?: "Failed to update team member")
        }
    }

    override suspend fun deleteTeamMember(id: String): Result<Unit> = runCatching {
        val token = userLocalDataSource.getAuthToken()
            ?: throw Exception("Not authenticated. Please log in.")

        val response = remoteDataSource.deleteTeamMember(token = token, id = id)

        if (!response.success) {
            throw Exception(response.message ?: "Failed to delete team member")
        }
    }
}

