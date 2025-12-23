package com.indusjs.fleet.feature.team.domain.entity

/**
 * Team member role enum.
 */
enum class TeamMemberRole {
    MANAGER,
    SUPERVISOR
}

/**
 * Team member domain entity.
 */
data class TeamMember(
    val id: String,
    val email: String,
    val mobile: String,
    val firstName: String,
    val lastName: String,
    val role: TeamMemberRole,
    val ownerId: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    val fullName: String get() = "$firstName $lastName"

    val roleDisplayName: String get() = when (role) {
        TeamMemberRole.MANAGER -> "Manager"
        TeamMemberRole.SUPERVISOR -> "Supervisor"
    }

    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}"
}

