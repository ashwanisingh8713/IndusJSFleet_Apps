package com.indusjs.fleet.feature.team.domain.repository

import com.indusjs.fleet.feature.team.domain.entity.TeamMember
import com.indusjs.fleet.feature.team.domain.entity.TeamMemberRole

/**
 * Repository interface for team management operations.
 */
interface TeamRepository {

    /**
     * Create a new team member (Manager or Supervisor).
     */
    suspend fun createTeamMember(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String,
        role: TeamMemberRole
    ): Result<TeamMember>

    /**
     * Get all team members.
     */
    suspend fun getTeamMembers(): Result<List<TeamMember>>

    /**
     * Get team members filtered by role.
     */
    suspend fun getTeamMembersByRole(role: TeamMemberRole): Result<List<TeamMember>>

    /**
     * Get a specific team member by ID.
     */
    suspend fun getTeamMember(id: String): Result<TeamMember>

    /**
     * Update a team member.
     */
    suspend fun updateTeamMember(
        id: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        mobile: String? = null,
        isActive: Boolean? = null
    ): Result<TeamMember>

    /**
     * Delete a team member.
     */
    suspend fun deleteTeamMember(id: String): Result<Unit>
}

