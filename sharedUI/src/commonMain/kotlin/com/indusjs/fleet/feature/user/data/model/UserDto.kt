@file:OptIn(ExperimentalSerializationApi::class)

package com.indusjs.fleet.feature.user.data.model

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys


/**
 * Sign up request DTO.
 */
@Serializable
data class SignUpRequest(
    val email: String,
    val mobile: String,
    val password: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String
) : Dto

/**
 * Login request DTO.
 */
@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
) : Dto

/**
 * Forgot password request DTO.
 */
@Serializable
data class ForgotPasswordRequest(
    val identifier: String
) : Dto

/**
 * Reset password request DTO.
 */
@Serializable
data class ResetPasswordRequest(
    val identifier: String,
    @SerialName("new_password")
    val newPassword: String
) : Dto

/**
 * Update profile request DTO.
 */
@Serializable
data class UpdateProfileRequest(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val mobile: String? = null
) : Dto

/**
 * Change password request DTO.
 */
@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password")
    val currentPassword: String,
    @SerialName("new_password")
    val newPassword: String,
    @SerialName("confirm_password")
    val confirmPassword: String
) : Dto

/**
 * User DTO from API responses.
 * Note: id is Int from API, we convert to String in domain layer.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val mobile: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
) : Dto

/**
 * Auth response DTO containing user and token.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class AuthResponseDto(
    val user: UserDto,
    val token: String
) : Dto

/**
 * Owner stats DTO for owner profiles - matches actual API response.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class OwnerStatsDto(
    @SerialName("total_managers")
    val totalManagers: Int = 0,
    @SerialName("total_supervisors")
    val totalSupervisors: Int = 0,
    @SerialName("total_team_members")
    val totalTeamMembers: Int = 0,
    @SerialName("total_vehicles")
    val totalVehicles: Int = 0,
    @SerialName("active_vehicles")
    val activeVehicles: Int = 0,
    @SerialName("total_trips")
    val totalTrips: Int = 0,
    @SerialName("active_trips")
    val activeTrips: Int = 0,
    @SerialName("completed_trips")
    val completedTrips: Int = 0
) : Dto

/**
 * User profile DTO - matches actual API response.
 * Profile endpoint returns user fields directly with owner_stats nested.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class UserProfileDto(
    val id: Int,
    val email: String,
    val mobile: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val role: String,
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("owner_stats")
    val ownerStats: OwnerStatsDto? = null,
    @SerialName("owner_info")
    val ownerInfo: OwnerInfoDto? = null
) : Dto

/**
 * Owner info DTO for team member profiles.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class OwnerInfoDto(
    @SerialName("owner_id")
    val ownerId: Int = 0,
    @SerialName("owner_name")
    val ownerName: String = "",
    @SerialName("owner_email")
    val ownerEmail: String = ""
) : Dto

/**
 * Generic API response wrapper.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

/**
 * Specific API response for authentication (login/signup).
 */
@JsonIgnoreUnknownKeys
@Serializable
data class AuthApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AuthResponseDto? = null
)

/**
 * Specific API response for profile.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class ProfileApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UserProfileDto? = null
)

/**
 * Specific API response for user update.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class UserApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UserDto? = null
)

/**
 * Simple API response without data.
 */
@JsonIgnoreUnknownKeys
@Serializable
data class SimpleApiResponse(
    val success: Boolean = false,
    val message: String? = null
)

