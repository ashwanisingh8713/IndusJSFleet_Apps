package com.indusjs.fleet.domain.repository.team

import com.indusjs.fleet.domain.entity.team.TeamMember
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for team management operations.
 */
interface TeamRepository : Repository {

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
     *
     * **Owner can update:** first_name, last_name, email, mobile, role, is_active
     * **Manager can update:** first_name, last_name, email, mobile, is_active (Supervisors only)
     */
    suspend fun updateTeamMember(
        id: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        mobile: String? = null,
        role: TeamMemberRole? = null,  // Owner only
        isActive: Boolean? = null
    ): Result<TeamMember>

    /**
     * Toggle team member active status (enable/disable).
     * Owner and Manager can access (Manager for Supervisors only).
     */
    suspend fun toggleTeamMemberActive(id: String): Result<TeamMember>

    /**
     * Reset a team member's password.
     * Owner and Manager can access (Manager for Supervisors only).
     */
    suspend fun resetTeamMemberPassword(id: String, newPassword: String): Result<Unit>

    /**
     * Delete a team member.
     */
    suspend fun deleteTeamMember(id: String): Result<Unit>
}

