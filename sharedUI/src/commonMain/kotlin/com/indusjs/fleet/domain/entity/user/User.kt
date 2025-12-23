package com.indusjs.fleet.domain.entity.user

import com.indusjs.fleet.domain.entity.Entity

/**
 * User roles in the Fleet Management system.
 */
enum class UserRole {
    OWNER,
    MANAGER,
    SUPERVISOR
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
    val createdAt: String,
    val updatedAt: String
) : Entity {
    val fullName: String
        get() = "$firstName $lastName"

    val isOwner: Boolean
        get() = role == UserRole.OWNER
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
 */
data class OrganizationStats(
    val totalManagers: Int,
    val totalSupervisors: Int,
    val totalDrivers: Int,
    val totalVehicles: Int,
    val activeTrips: Int
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

