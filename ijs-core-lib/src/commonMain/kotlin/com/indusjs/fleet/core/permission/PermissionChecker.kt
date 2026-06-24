package com.indusjs.fleet.core.permission

/**
 * Permission-based UI gate.
 *
 * Implementations decide visibility/enablement purely from the user's ACTUAL
 * permission set (fetched from the backend) — NEVER from hardcoded role names.
 * The backend remains the security authority; these checks are for UX only.
 *
 * The semantic helpers below are thin, permission-only wrappers so call sites
 * read intent ("canCreateTrip()") without knowing the underlying permission
 * string.
 */
interface PermissionChecker {

    /** True if the user has the given permission. */
    fun has(permission: String): Boolean

    /** True if the user has at least one of the given permissions. */
    fun hasAny(vararg p: String): Boolean

    /** True if the user has all of the given permissions. */
    fun hasAll(vararg p: String): Boolean

    // ── Financials ──────────────────────────────────────────────────────────
    fun canViewFinancials(): Boolean = has(Permissions.FINANCIALS_READ)
    fun canViewTripPrice(): Boolean = has(Permissions.FINANCIALS_READ)

    // ── Trips ───────────────────────────────────────────────────────────────
    fun canCreateTrip(): Boolean = has(Permissions.TRIPS_CREATE)
    fun canEditTrip(): Boolean = has(Permissions.TRIPS_UPDATE)
    fun canDeleteTrip(): Boolean = has(Permissions.TRIPS_DELETE)

    // ── Costs ───────────────────────────────────────────────────────────────
    fun canEditCosts(): Boolean = has(Permissions.COSTS_UPDATE)
    fun canDeleteCosts(): Boolean = has(Permissions.COSTS_DELETE)

    // ── Vehicles ────────────────────────────────────────────────────────────
    fun canCreateVehicle(): Boolean = has(Permissions.VEHICLES_CREATE)
    fun canEditVehicle(): Boolean = has(Permissions.VEHICLES_UPDATE)
    fun canDisableVehicle(): Boolean = has(Permissions.VEHICLES_DELETE)

    // ── Drivers ─────────────────────────────────────────────────────────────
    fun canCreateDriver(): Boolean = has(Permissions.DRIVERS_CREATE)
    fun canEditDriver(): Boolean = has(Permissions.DRIVERS_UPDATE)
    fun canDisableDriver(): Boolean = has(Permissions.DRIVERS_DELETE)

    // ── Caretakers ──────────────────────────────────────────────────────────
    fun canAssignCaretaker(): Boolean = has(Permissions.CARETAKERS_ASSIGN)

    // ── Team / Users ────────────────────────────────────────────────────────
    fun canManageTeam(): Boolean =
        hasAny(Permissions.USERS_CREATE, Permissions.USERS_UPDATE, Permissions.USERS_INVITE)
    fun canCreateTeamMember(): Boolean = has(Permissions.USERS_CREATE)
    fun canInviteTeamMember(): Boolean = has(Permissions.USERS_INVITE)
    fun canChangeRole(): Boolean = has(Permissions.USERS_CHANGE_ROLE)
    fun canToggleTeamMemberStatus(): Boolean = has(Permissions.USERS_TOGGLE_ACTIVE)
    fun canResetPassword(): Boolean = has(Permissions.USERS_RESET_PASSWORD)
    fun canDeleteTeamMember(): Boolean = has(Permissions.USERS_DELETE)
}

/**
 * Default [PermissionChecker] backed by [PermissionStore].
 *
 * Reads the store on every call so permission updates after login / session
 * restore are reflected immediately without re-creating the checker.
 */
class DefaultPermissionChecker(
    private val store: PermissionStore
) : PermissionChecker {

    override fun has(permission: String): Boolean =
        store.current().contains(permission)

    override fun hasAny(vararg p: String): Boolean {
        val current = store.current()
        return p.any { current.contains(it) }
    }

    override fun hasAll(vararg p: String): Boolean {
        val current = store.current()
        return p.all { current.contains(it) }
    }
}
