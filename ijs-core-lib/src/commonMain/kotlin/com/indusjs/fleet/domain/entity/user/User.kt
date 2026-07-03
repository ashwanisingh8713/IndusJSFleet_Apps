package com.indusjs.fleet.domain.entity.user

import com.indusjs.fleet.domain.entity.Entity

/**
 * User roles in the Fleet Management system (IAM tenant roles).
 * Hierarchy: Owner > Admin > User. Fine-grained access is permission-based
 * (/me/permissions); the role is presentation/fallback only.
 */
enum class UserRole {
    OWNER,
    ADMIN,
    USER;

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.lowercase().replace("_", "").replace(" ", "")) {
                "owner" -> OWNER
                "admin" -> ADMIN
                "user" -> USER
                else -> USER // Default to lowest privilege
            }
        }

        fun toApiString(role: UserRole): String {
            return when (role) {
                OWNER -> "owner"
                ADMIN -> "admin"
                USER -> "user"
            }
        }
    }
}

/**
 * Domain entity representing a User.
 */
data class User(
    val id: String,
    val email: String,
    val mobile: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val ownerId: String? = null, // null for owners, owner's id for admins/users
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String,
    // Tenant/business display name (enriched by backend from IAM). Null/blank when unavailable —
    // the Home header (f5) falls back to a default title. Same tenant for owner + team members.
    val businessName: String? = null
) : Entity {
    val fullName: String
        get() = "$firstName $lastName"

    // Access gating is permission-based (PermissionChecker on /me/permissions),
    // not role-based — the role here is for display/fallback only.
    val isOwner: Boolean
        get() = role == UserRole.OWNER
}

/**
 * Domain entity for user profile with optional organization stats.
 */
data class UserProfile(
    val user: User,
    val organizationStats: OrganizationStats? = null, // Only for owners
    val ownerInfo: OwnerInfo? = null // Only for admins/users
) : Entity

/**
 * Organization statistics for owners.
 * Mirrors the `owner_stats` block returned by `GET /profile` for owners.
 * admins + users partition totalTeamMembers (owner excluded everywhere).
 */
data class OrganizationStats(
    val totalTeamMembers: Int = 0,
    val admins: Int = 0,
    val users: Int = 0,
    val totalVehicles: Int = 0,
    val activeVehicles: Int = 0,
    val totalTrips: Int = 0,
    val activeTrips: Int = 0,
    val completedTrips: Int = 0
)

/**
 * Owner information for team members.
 */
data class OwnerInfo(
    val ownerId: String,
    val ownerName: String,
    val ownerEmail: String
)

/**
 * Authentication result containing user and token.
 */
data class AuthResult(
    val user: User,
    val token: String
)

/**
 * SignUp result — v1 backend does NOT issue tokens at signup.
 * Email/mobile verification is required before login is possible.
 */
data class SignUpResult(
    val message: String,
    val isResend: Boolean
)

