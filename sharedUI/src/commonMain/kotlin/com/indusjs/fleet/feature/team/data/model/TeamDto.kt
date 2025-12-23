@file:OptIn(ExperimentalSerializationApi::class)

package com.indusjs.fleet.feature.team.data.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

/**
 * Request to create a team member (Manager or Supervisor).
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
    val role: String // "manager" or "supervisor"
)

/**
 * Request to update a team member.
 */
@Serializable
data class UpdateTeamMemberRequest(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
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

