package com.ijs.team.domain.entity

/**
 * Team member role enum.
 * Fleet API uses IAM-aligned roles: owner, admin, user.
 * Existing enum names are retained for compatibility with older modules.
 */
enum class TeamMemberRole {
    GENERAL_MANAGER,
    MANAGER,
    SUPERVISOR;

    /**
     * Convert role to API string representation.
     */
    fun toApiString(): String = when (this) {
        GENERAL_MANAGER -> "owner"
        MANAGER -> "admin"
        SUPERVISOR -> "user"
    }

    companion object {
        /**
         * Parse role from API string, defaulting to SUPERVISOR if unknown.
         */
        fun fromApiString(value: String): TeamMemberRole = when (value.lowercase().replace("_", "").replace(" ", "")) {
            "owner", "generalmanager", "gm" -> GENERAL_MANAGER
            "admin", "manager" -> MANAGER
            "user", "supervisor" -> SUPERVISOR
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
    // Backend-computed eligibility to be a vehicle/driver caretaker.
    val isCaretakerEligible: Boolean = false,
    // UTC epoch-millis. 0 = unset.
    val createdAt: Long,
    val updatedAt: Long?
) {
    val fullName: String get() = "$firstName $lastName"
}

