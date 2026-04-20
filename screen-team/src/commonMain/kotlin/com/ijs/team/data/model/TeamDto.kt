@file:OptIn(ExperimentalSerializationApi::class)

package com.ijs.team.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * Request to create a team member.
 * Current Fleet API: [role] must be `general_manager`, `manager`, or `supervisor`.
 * The app maps UI selections `admin`/`user` to `manager`/`supervisor` until the backend
 * ships IAM-aligned roles (see Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md).
 */
@Serializable
data class CreateTeamMemberRequest(
    val email: String,
    val mobile: String,
    val password: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String
)

/**
 * One assignable IAM role in Fleet API envelope: data.roles[].
 */
@JsonIgnoreUnknownKeys
@Serializable
data class AssignableRoleDto(
    val id: String = "",
    val name: String = "",
    val description: String = ""
)

@JsonIgnoreUnknownKeys
@Serializable
data class AssignableRolesDataDto(
    val roles: List<AssignableRoleDto> = emptyList()
)

@JsonIgnoreUnknownKeys
@Serializable
data class AssignableRolesApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AssignableRolesDataDto? = null
)

/**
 * Request to update a team member.
 *
 * **Owner can update:** first_name, last_name, email, mobile, role, is_active, password
 * **Manager can update:** first_name, last_name, email, mobile, is_active, password (Supervisors only)
 * **Manager cannot:** change roles, edit other Managers
 */
@Serializable
data class UpdateTeamMemberRequest(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val role: String? = null,  // Owner only - "manager" or "supervisor"
    @SerialName("is_active")
    val isActive: Boolean? = null,
    val password: String? = null  // Owner and Manager (for Supervisors only)
)

/**
 * Request to reset a team member's password.
 * Owner and Manager can access (Manager for Supervisors only).
 */
@Serializable
data class ResetPasswordRequest(
    @SerialName("new_password")
    val newPassword: String
)

/**
 * Team member DTO from API responses.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class TeamMemberDto(
    val id: Int,
    val email: String,
    val mobile: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    @SerialName("owner_id")
    val ownerId: Int,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * API response for creating/getting a single team member.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class TeamMemberApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: TeamMemberDto? = null
)

/**
 * Wrapper for team list data in API response.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class TeamListDataDto(
    val count: Int = 0,
    val team: List<TeamMemberDto>? = null
)

/**
 * API response for getting list of team members.
 * Structure: { success, message, data: { count, team: [...] } }
 */
@JsonIgnoreUnknownKeys
@Serializable
data class TeamMemberListApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: TeamListDataDto? = null
)

/**
 * Simple API response without data.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class TeamSimpleApiResponse(
    val success: Boolean = false,
    val message: String? = null
)

