package com.ijs.team.domain.repository

import com.ijs.team.domain.entity.AssignableTeamRole
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import com.indusjs.fleet.domain.repository.Repository

/**
 * Repository interface for team management operations.
 */
interface TeamRepository : Repository {

    /**
     * IAM tenant roles the current user may assign when adding a member (e.g. admin, user).
     */
    suspend fun getAssignableTeamRoles(): Result<List<AssignableTeamRole>>

    /**
     * Create a new team member with an IAM tenant [iamRole] (e.g. "admin", "user").
     */
    suspend fun createTeamMember(
        email: String,
        mobile: String,
        password: String,
        firstName: String,
        lastName: String,
        iamRole: String
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
     * **Owner can update:** first_name, last_name, email, mobile, role, is_active.
     * **Admin can update:** user members, subject to backend IAM permissions.
     */
    suspend fun updateTeamMember(
        id: String,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        mobile: String? = null,
        role: TeamMemberRole? = null,
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

    // ==================== Local Storage Methods ====================

    /**
     * Get team members from local cache.
     */
    suspend fun getTeamMembersFromCache(): Result<List<TeamMember>>

    /**
     * Get caretakers (supervisors + managers) from local cache.
     */
    suspend fun getCaretakersFromCache(): Result<List<TeamMember>>

    /**
     * Refresh team members from API and update local cache.
     */
    suspend fun refreshTeamMembers(): Result<List<TeamMember>>
}

