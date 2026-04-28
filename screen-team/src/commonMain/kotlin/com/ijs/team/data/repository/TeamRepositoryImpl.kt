package com.ijs.team.data.repository

import com.indusjs.fleet.core.logger.FleetLogger
import com.ijs.team.TAG_TEAM_REPO
import com.indusjs.error.exception.ApiException
import com.indusjs.fleet.core.auth.AuthenticationManager
import com.indusjs.fleet.core.auth.AuthTokenHelper
import com.indusjs.fleet.core.auth.JwtHelper
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
        val roles = response.data.roles.map { roleName ->
            AssignableTeamRole(
                id = roleName,
                name = roleName,
                description = ""
            )
        }
        roles
    }

    override suspend fun createTeamMember(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String,
        iamRole: String
    ): Result<TeamMember> = runCatching {
        val token = requireAuthTokenWithTenantCheck()

        val response = remoteDataSource.createTeamMember(
            token = token,
            request = CreateTeamMemberRequest(
                email = email,
                mobile = mobile,
                password = password,
                firstName = firstName,
                lastName = lastName,
                role = iamRole
            )
        )

        if (!response.success && response.message?.contains("insufficient permissions", ignoreCase = true) == true) {
            logger.e(TAG_TEAM_REPO, "403 permission error — token may lack tenant context, triggering re-login")
            AuthenticationManager.emitSessionExpired(
                "Your session doesn't have the required permissions. Please log in again."
            )
            throw ApiException("Insufficient permissions. Please log out and log back in to refresh your session.")
        }

        val teamMember = response.data?.toDomain()
            ?: run {
                throw ApiException(response.message ?: "Failed to create team member")
            }

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
     * Like [requireAuthToken] but also verifies the JWT carries a `tid` (tenant_id)
     * claim, which is required for any tenant-scoped API (team, permissions, etc.).
     *
     * If the claim is absent the owner is still using the pre-tenant JWT — we trigger
     * a session refresh so they re-login and receive a proper token.
     */
    private suspend fun requireAuthTokenWithTenantCheck(): String {
        val token = requireAuthToken()
        if (!JwtHelper.hasTenantContext(token)) {
            logger.e(TAG_TEAM_REPO, "JWT missing 'tid' claim — pre-tenant token detected, forcing re-login")
            AuthenticationManager.emitSessionExpired(
                "Your session needs to be refreshed after creating the organization. Please log in again."
            )
            throw ApiException("Session missing organization context. Please log out and log back in.")
        }
        return token
    }

}

