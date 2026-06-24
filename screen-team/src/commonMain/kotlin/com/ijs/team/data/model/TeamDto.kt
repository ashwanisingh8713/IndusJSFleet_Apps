@file:OptIn(ExperimentalSerializationApi::class)

package com.ijs.team.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * Request to create a team member.
 * Current Fleet API accepts IAM-aligned role names such as `admin` and `user`.
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

@JsonIgnoreUnknownKeys
@Serializable
data class AssignableRolesDataDto(
    val roles: List<String> = emptyList(),
    val count: Int = 0
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
    val role: String? = null,  // IAM role name: "admin" or "user"
    @SerialName("is_active")
    val isActive: Boolean? = null,
    val password: String? = null
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
    val id: Int = 0,
    // Backend marks email/mobile as omitempty — they are absent from JSON when blank
    // (e.g. a member created with only a mobile, or only an email). Defaults prevent
    // a MissingFieldException that would blank the screen.
    val email: String = "",
    val mobile: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    val role: String = "",
    // Backend owner_id is omitempty *uint — absent for an owner's own record (nil OwnerID).
    @SerialName("owner_id")
    val ownerId: Int = 0,
    @SerialName("is_active")
    val isActive: Boolean = true,
    // Backend-computed: role is non-empty and not "owner". Source of truth for caretaker pickers.
    @SerialName("is_caretaker_eligible")
    val isCaretakerEligible: Boolean = false,
    // UTC epoch-millis (JSON number). 0 = unset.
    @SerialName("created_at")
    val createdAt: Long = 0L,
    @SerialName("updated_at")
    val updatedAt: Long? = null
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

