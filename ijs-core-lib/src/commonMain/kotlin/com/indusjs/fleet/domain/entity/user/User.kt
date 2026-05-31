package com.indusjs.fleet.domain.entity.user

import com.indusjs.fleet.domain.entity.Entity

/**
 * User roles in the Fleet Management system.
 * Hierarchy: Owner > General Manager > Manager > Supervisor
 */
enum class UserRole {
    OWNER,
    GENERAL_MANAGER,
    MANAGER,
    SUPERVISOR;

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.lowercase().replace("_", "").replace(" ", "")) {
                "owner" -> OWNER
                "generalmanager", "gm" -> GENERAL_MANAGER
                "manager" -> MANAGER
                "supervisor" -> SUPERVISOR
                else -> SUPERVISOR // Default to lowest privilege
            }
        }

        fun toApiString(role: UserRole): String {
            return when (role) {
                OWNER -> "owner"
                GENERAL_MANAGER -> "general_manager"
                MANAGER -> "manager"
                SUPERVISOR -> "supervisor"
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
    val ownerId: String? = null, // null for owners, owner's id for managers/supervisors
    val isActive: Boolean = true,
    val createdAt: String,
    val updatedAt: String
) : Entity {
    val fullName: String
        get() = "$firstName $lastName"

    val isOwner: Boolean
        get() = role == UserRole.OWNER

    val isGeneralManager: Boolean
        get() = role == UserRole.GENERAL_MANAGER

    val isManager: Boolean
        get() = role == UserRole.MANAGER

    val isSupervisor: Boolean
        get() = role == UserRole.SUPERVISOR

    /** Owner or General Manager - has financial access */
    val hasFinancialAccess: Boolean
        get() = role == UserRole.OWNER || role == UserRole.GENERAL_MANAGER

    /** Can edit trips in any state */
    val canEditTripInAnyState: Boolean
        get() = role == UserRole.OWNER || role == UserRole.GENERAL_MANAGER

    /** Can manage team members */
    val canManageTeam: Boolean
        get() = role == UserRole.OWNER || role == UserRole.GENERAL_MANAGER

    /** Can assign caretakers to vehicles/drivers */
    val canAssignCaretaker: Boolean
        get() = role == UserRole.OWNER || role == UserRole.GENERAL_MANAGER

    /** Can view trip_price field */
    val canViewTripPrice: Boolean
        get() = role == UserRole.OWNER || role == UserRole.GENERAL_MANAGER

    /** Can delete costs */
    val canDeleteCosts: Boolean
        get() = role != UserRole.SUPERVISOR
}

/**
 * Domain entity for user profile with optional organization stats.
 */
data class UserProfile(
    val user: User,
    val organizationStats: OrganizationStats? = null, // Only for owners
    val ownerInfo: OwnerInfo? = null // Only for managers/supervisors
) : Entity

/**
 * Organization statistics for owners.
 * Mirrors the `owner_stats` block returned by `GET /profile` for owners.
 */
data class OrganizationStats(
    val totalManagers: Int = 0,
    val totalSupervisors: Int = 0,
    val totalTeamMembers: Int = 0,
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

