package com.ijs.team.domain.entity

/**
 * Team member role enum — IAM-aligned tenant roles: owner, admin, user.
 */
enum class TeamMemberRole {
    OWNER,
    ADMIN,
    USER;

    /**
     * Convert role to API string representation.
     */
    fun toApiString(): String = when (this) {
        OWNER -> "owner"
        ADMIN -> "admin"
        USER -> "user"
    }

    companion object {
        /**
         * Parse role from API string, defaulting to USER (lowest privilege) if unknown.
         */
        fun fromApiString(value: String): TeamMemberRole = when (value.lowercase().replace("_", "").replace(" ", "")) {
            "owner" -> OWNER
            "admin" -> ADMIN
            "user" -> USER
            else -> USER
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

