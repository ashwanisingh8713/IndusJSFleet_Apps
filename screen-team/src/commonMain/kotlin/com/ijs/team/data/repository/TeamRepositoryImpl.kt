package com.ijs.team.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.team.TAG_TEAM_REPO
import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.ijs.team.data.datasource.TeamLocalDataSource
import com.ijs.team.data.datasource.TeamRemoteDataSource
import com.indusjs.fleet.data.datasource.user.UserLocalDataSource
import com.ijs.team.data.mapper.TeamMapper.toDomain
import com.ijs.team.data.model.CreateTeamMemberRequest
import com.ijs.team.data.model.ResetPasswordRequest
import com.ijs.team.data.model.TeamMemberDto
import com.ijs.team.data.model.UpdateTeamMemberRequest
import com.ijs.team.domain.entity.AssignableTeamRole
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import com.ijs.team.domain.repository.TeamRepository
import dev.zacsweers.metro.Inject

/**
 * Implementation of TeamRepository.
 * Handles team member CRUD operations via remote data source with local caching.
 */
@Inject
class TeamRepositoryImpl(
    private val remoteDataSource: TeamRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val localDataSource: TeamLocalDataSource,
    private val logger: FleetLogger
) : TeamRepository {

    override suspend fun getAssignableTeamRoles(): Result<List<AssignableTeamRole>> = runCatching {
        val token = requireAuthToken()
        val response = remoteDataSource.getAssignableTeamRoles(token)
        if (!response.success || response.data == null) {
            // Fleet route may not exist yet — UI falls back to default admin/user choices.
            return@runCatching emptyList()
        }
        response.data.roles.map { dto ->
            AssignableTeamRole(
                id = dto.id,
                name = dto.name,
                description = dto.description
            )
        }
    }

    override suspend fun createTeamMember(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String,
        iamRole: String
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
                role = mapIamSelectionToCurrentFleetApiRole(iamRole)
            )
        )

        val teamMember = response.data?.toDomain()
            ?: throw ApiException(response.message ?: "Failed to create team member")

        // Save to local cache
        response.data?.let { dto ->
            localDataSource.saveTeamMember(dto)
            logger.d(TAG_TEAM_REPO, "Saved new team member to cache: ${teamMember.fullName}")
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
                logger.d(TAG_TEAM_REPO, "Saved ${dtos.size} team members to cache")
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
            logger.d(TAG_TEAM_REPO, "Updated team member in cache: ${teamMember.fullName}")
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
            logger.d(TAG_TEAM_REPO, "Updated team member status in cache: ${teamMember.fullName}")
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
        logger.d(TAG_TEAM_REPO, "Deleted team member from cache: $id")
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
                logger.d(TAG_TEAM_REPO, "Refreshed ${dtos.size} team members in cache")
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

    /**
     * Current Fleet API expects `general_manager` | `manager` | `supervisor`.
     * The Add Member UI uses IAM-style `admin` | `user` until the backend implements
     * `IndusJSFleet_Apps/Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md`.
     */
    private fun mapIamSelectionToCurrentFleetApiRole(iamRole: String): String =
        when (iamRole.lowercase()) {
            "admin" -> "manager"
            "user" -> "supervisor"
            else -> iamRole
        }
}

