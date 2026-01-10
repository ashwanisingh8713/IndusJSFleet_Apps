package com.indusjs.fleet.domain.entity.team

/**
 * Team member role enum.
 * Hierarchy: General Manager > Manager > Supervisor
 */
enum class TeamMemberRole {
    GENERAL_MANAGER,
    MANAGER,
    SUPERVISOR;

    /**
     * Convert role to API string representation.
     */
    fun toApiString(): String = when (this) {
        GENERAL_MANAGER -> "general_manager"
        MANAGER -> "manager"
        SUPERVISOR -> "supervisor"
    }

    companion object {
        /**
         * Parse role from API string, defaulting to SUPERVISOR if unknown.
         */
        fun fromApiString(value: String): TeamMemberRole = when (value.lowercase().replace("_", "").replace(" ", "")) {
            "generalmanager", "gm" -> GENERAL_MANAGER
            "manager" -> MANAGER
            "supervisor" -> SUPERVISOR
            else -> SUPERVISOR
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
        TeamMemberRole.GENERAL_MANAGER -> "General Manager"
        TeamMemberRole.MANAGER -> "Manager"
        TeamMemberRole.SUPERVISOR -> "Supervisor"
    }

    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}"
}

