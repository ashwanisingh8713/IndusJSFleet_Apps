package com.indusjs.fleet.core.util

import com.indusjs.fleet.core.constants.StatusConstants
import com.indusjs.fleet.domain.entity.user.UserRole

/**
 * Utility functions for role-based permission checks.
 *
 * Role Hierarchy: Owner > General Manager > Manager > Supervisor
 */
object PermissionUtils {

    /**
     * Check if user can view trip_price field.
     * Only Owner and General Manager can see trip pricing.
     */
    fun canViewTripPrice(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole == UserRole.OWNER || userRole == UserRole.GENERAL_MANAGER
    }

    /**
     * Check if user can view financial data (P&L, cost overview, pending payments).
     * Only Owner and General Manager have financial access.
     */
    fun canViewFinancials(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole == UserRole.OWNER || userRole == UserRole.GENERAL_MANAGER
    }

    /**
     * Check if user can edit trip in any state (planned, in_progress, completed).
     * Owner and General Manager can edit trips in any state.
     * Manager can only edit planned trips.
     * Supervisor cannot edit trips.
     *
     * @param role User role string
     * @param tripStatus Trip status string (e.g., "planned", "on_route")
     */
    fun canEditTrip(role: String, tripStatus: String): Boolean {
        val userRole = parseRole(role)
        return when (userRole) {
            UserRole.OWNER, UserRole.GENERAL_MANAGER -> true
            UserRole.MANAGER -> tripStatus.lowercase() == StatusConstants.TripState.PLANNED
            UserRole.SUPERVISOR -> false
        }
    }

    /**
     * Check if user can edit trips in any state.
     */
    fun canEditTripInAnyState(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole == UserRole.OWNER || userRole == UserRole.GENERAL_MANAGER
    }

    /**
     * Check if user can delete costs.
     * Supervisor cannot delete costs.
     */
    fun canDeleteCosts(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole != UserRole.SUPERVISOR
    }

    /**
     * Check if user can manage team members (create, edit, disable).
     */
    fun canManageTeam(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole == UserRole.OWNER || userRole == UserRole.GENERAL_MANAGER
    }

    /**
     * Check if user can assign caretakers to vehicles/drivers.
     */
    fun canAssignCaretaker(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole == UserRole.OWNER || userRole == UserRole.GENERAL_MANAGER
    }

    /**
     * Check if user can create a team member with the target role.
     */
    fun canCreateTeamMember(currentRole: String, targetRole: String): Boolean {
        val current = parseRole(currentRole)
        val target = parseRole(targetRole)

        return when (current) {
            UserRole.OWNER -> true // Owner can create any role
            UserRole.GENERAL_MANAGER -> target == UserRole.MANAGER || target == UserRole.SUPERVISOR
            else -> false // Manager and Supervisor cannot create team members
        }
    }

    /**
     * Get list of roles that the current user can create.
     */
    fun getCreatableRoles(currentRole: String): List<UserRole> {
        val userRole = parseRole(currentRole)
        return when (userRole) {
            UserRole.OWNER -> listOf(UserRole.GENERAL_MANAGER, UserRole.MANAGER, UserRole.SUPERVISOR)
            UserRole.GENERAL_MANAGER -> listOf(UserRole.MANAGER, UserRole.SUPERVISOR)
            else -> emptyList()
        }
    }

    /**
     * Check if user can change roles.
     * Owner can change any role.
     * General Manager can only change Manager ↔ Supervisor.
     */
    fun canChangeRole(currentUserRole: String, targetMemberRole: String, newRole: String): Boolean {
        val current = parseRole(currentUserRole)
        val targetCurrent = parseRole(targetMemberRole)
        val targetNew = parseRole(newRole)

        return when (current) {
            UserRole.OWNER -> true // Owner can change any role
            UserRole.GENERAL_MANAGER -> {
                // GM can only change between Manager and Supervisor
                (targetCurrent == UserRole.MANAGER || targetCurrent == UserRole.SUPERVISOR) &&
                (targetNew == UserRole.MANAGER || targetNew == UserRole.SUPERVISOR)
            }
            else -> false
        }
    }

    /**
     * Check if user can disable/enable a team member.
     */
    fun canToggleTeamMemberStatus(currentUserRole: String, targetMemberRole: String): Boolean {
        val current = parseRole(currentUserRole)
        val target = parseRole(targetMemberRole)

        return when (current) {
            UserRole.OWNER -> true // Owner can disable anyone
            UserRole.GENERAL_MANAGER -> target == UserRole.MANAGER || target == UserRole.SUPERVISOR
            else -> false
        }
    }

    /**
     * Check if user can create vehicles.
     * Supervisor cannot create vehicles.
     */
    fun canCreateVehicle(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole != UserRole.SUPERVISOR
    }

    /**
     * Check if user can create drivers.
     * Supervisor cannot create drivers.
     */
    fun canCreateDriver(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole != UserRole.SUPERVISOR
    }

    /**
     * Check if user can create trips.
     * Supervisor cannot create trips.
     */
    fun canCreateTrip(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole != UserRole.SUPERVISOR
    }

    /**
     * Check if user can edit vehicles/drivers.
     * Supervisor cannot edit.
     */
    fun canEditVehicleOrDriver(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole != UserRole.SUPERVISOR
    }

    /**
     * Check if user can disable vehicles/drivers.
     * Only Owner and General Manager.
     */
    fun canDisableVehicleOrDriver(role: String): Boolean {
        val userRole = parseRole(role)
        return userRole == UserRole.OWNER || userRole == UserRole.GENERAL_MANAGER
    }

    /**
     * Parse role string to UserRole enum.
     */
    private fun parseRole(role: String): UserRole {
        return UserRole.fromString(role)
    }

    /**
     * Get display name for a role.
     */
    fun getRoleDisplayName(role: String): String {
        return when (parseRole(role)) {
            UserRole.OWNER -> "Owner"
            UserRole.GENERAL_MANAGER -> "General Manager"
            UserRole.MANAGER -> "Manager"
            UserRole.SUPERVISOR -> "Supervisor"
        }
    }

    /**
     * Get display name for UserRole enum.
     */
    fun getRoleDisplayName(role: UserRole): String {
        return when (role) {
            UserRole.OWNER -> "Owner"
            UserRole.GENERAL_MANAGER -> "General Manager"
            UserRole.MANAGER -> "Manager"
            UserRole.SUPERVISOR -> "Supervisor"
        }
    }
}

