package com.indusjs.fleet.core.util

import com.indusjs.fleet.domain.entity.user.UserRole

/**
 * Role DISPLAY helpers.
 *
 * IMPORTANT: This object is for presentation only (e.g. rendering a human-readable
 * role label). It must NEVER be used for authorization. All UI gating goes through
 * [com.indusjs.fleet.core.permission.PermissionChecker], which reads the user's
 * actual permission set; the backend remains the security authority.
 */
object PermissionUtils {

    /**
     * Get display name for a role string (for presentation only).
     */
    fun getRoleDisplayName(role: String): String = getRoleDisplayName(UserRole.fromString(role))

    /**
     * Get display name for a [UserRole] enum (for presentation only).
     */
    fun getRoleDisplayName(role: UserRole): String {
        return when (role) {
            UserRole.OWNER -> "Owner"
            UserRole.ADMIN -> "Admin"
            UserRole.USER -> "User"
        }
    }
}
