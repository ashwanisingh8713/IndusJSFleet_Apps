package com.indusjs.fleet.domain.entity.team

/**
 * Team member role enum.
 */
enum class TeamMemberRole {
    MANAGER,
    SUPERVISOR;

    /**
     * Convert role to API string representation.
     */
    fun toApiString(): String = when (this) {
        MANAGER -> "manager"
        SUPERVISOR -> "supervisor"
    }

    companion object {
        /**
         * Parse role from API string, defaulting to MANAGER if unknown.
         */
        fun fromApiString(value: String): TeamMemberRole = when (value.lowercase()) {
            "manager" -> MANAGER
            "supervisor" -> SUPERVISOR
            else -> MANAGER
        }
    }
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

