@file:OptIn(ExperimentalSerializationApi::class)

package com.indusjs.fleet.data.model.user

import com.indusjs.fleet.data.model.Dto
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

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

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
) : Dto

@Serializable
data class ForgotPasswordRequest(
    val identifier: String
) : Dto

@Serializable
data class ResetPasswordRequest(
    val identifier: String,
    @SerialName("reset_token")
    val resetToken: String,
    @SerialName("new_password")
    val newPassword: String
) : Dto

@Serializable
data class UpdateProfileRequest(
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    val email: String? = null,
    val mobile: String? = null
) : Dto

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password")
    val currentPassword: String,
    @SerialName("new_password")
    val newPassword: String,
    @SerialName("confirm_password")
    val confirmPassword: String
) : Dto

@JsonIgnoreUnknownKeys
@Serializable
data class UserDto(
    val id: Int = 0,
    val email: String = "",
    val mobile: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    val role: String = "",
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("created_by_id")
    val createdById: Int? = null,
    @SerialName("tenant_id")
    val tenantId: String = "",
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String? = null
) : Dto

@JsonIgnoreUnknownKeys
@Serializable
data class AuthResponseDto(
    val user: UserDto,
    // token is absent in the IsResend signup case (existing unverified account)
    val token: String? = null
) : Dto

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

@JsonIgnoreUnknownKeys
@Serializable
data class UserProfileDto(
    val id: Int = 0,
    val email: String = "",
    val mobile: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    val role: String = "",
    @SerialName("owner_id")
    val ownerId: Int? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("owner_stats")
    val ownerStats: OwnerStatsDto? = null,
    @SerialName("owner_info")
    val ownerInfo: OwnerInfoDto? = null
) : Dto

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

@JsonIgnoreUnknownKeys
@Serializable
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null
)

@JsonIgnoreUnknownKeys
@Serializable
data class AuthApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AuthResponseDto? = null
)

/**
 * SignUp API response — token is NEVER present in v1 (IAM requires verification first).
 * The data contains user info but no token.
 */
data class SignUpApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val user: UserDto? = null,
    val isResend: Boolean = false
)

@JsonIgnoreUnknownKeys
@Serializable
data class ProfileApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UserProfileDto? = null
)

@JsonIgnoreUnknownKeys
@Serializable
data class UserApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: UserDto? = null
)

@JsonIgnoreUnknownKeys
@Serializable
data class SimpleApiResponse(
    val success: Boolean = false,
    val message: String? = null
)

// ── Verification & OTP DTOs ──────────────────────────────────────────────

@Serializable
data class VerifyEmailOtpRequest(
    val email: String,
    val otp: String
) : Dto

@Serializable
data class VerifyMobileRequest(
    val token: String
) : Dto

@Serializable
data class SendLoginOtpRequest(
    val mobile: String
) : Dto

@Serializable
data class VerifyLoginOtpRequest(
    val mobile: String,
    val otp: String
) : Dto

@JsonIgnoreUnknownKeys
@Serializable
data class VerifyMobileResponseDto(
    val token: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: Int = 0
)

