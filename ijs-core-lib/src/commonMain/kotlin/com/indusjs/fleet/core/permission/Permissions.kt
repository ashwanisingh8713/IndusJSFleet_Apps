package com.indusjs.fleet.core.permission

/**
 * Permission string catalog — mirrors the backend permission catalog verbatim.
 *
 * These are the ONLY source of truth for permission identifiers on the client.
 * UI gating must reference these constants (via [PermissionChecker]) and NEVER
 * hardcode role names. The backend remains the security authority; these checks
 * are for UX only.
 */
object Permissions {

    // ── Financials ──────────────────────────────────────────────────────────
    const val FINANCIALS_READ = "financials:read"

    // ── Vehicles ────────────────────────────────────────────────────────────
    const val VEHICLES_CREATE = "vehicles:create"
    const val VEHICLES_UPDATE = "vehicles:update"
    const val VEHICLES_DELETE = "vehicles:delete"

    // ── Drivers ─────────────────────────────────────────────────────────────
    const val DRIVERS_CREATE = "drivers:create"
    const val DRIVERS_UPDATE = "drivers:update"
    const val DRIVERS_DELETE = "drivers:delete"

    // ── Trips ───────────────────────────────────────────────────────────────
    const val TRIPS_CREATE = "trips:create"
    const val TRIPS_UPDATE = "trips:update"
    const val TRIPS_DELETE = "trips:delete"

    // ── Costs ───────────────────────────────────────────────────────────────
    const val COSTS_UPDATE = "costs:update"
    const val COSTS_DELETE = "costs:delete"

    // ── Maintenance ─────────────────────────────────────────────────────────
    const val MAINTENANCE_UPDATE = "maintenance:update"
    const val MAINTENANCE_DELETE = "maintenance:delete"

    // ── Caretakers ──────────────────────────────────────────────────────────
    const val CARETAKERS_ASSIGN = "caretakers:assign"

    // ── Documents ───────────────────────────────────────────────────────────
    const val DOCUMENTS_UPDATE = "documents:update"
    const val DOCUMENTS_DELETE = "documents:delete"

    // ── Users / Team ────────────────────────────────────────────────────────
    const val USERS_READ = "users:read"
    const val USERS_CREATE = "users:create"
    const val USERS_INVITE = "users:invite"
    const val USERS_UPDATE = "users:update"
    const val USERS_DELETE = "users:delete"
    const val USERS_TOGGLE_ACTIVE = "users:toggle_active"
    const val USERS_CHANGE_ROLE = "users:change_role"
    const val USERS_RESET_PASSWORD = "users:reset_password"

    // ── Dashboard ───────────────────────────────────────────────────────────
    const val DASHBOARD_OWNER_VIEW = "dashboard:owner_view"
    const val DASHBOARD_MANAGER_VIEW = "dashboard:manager_view"
    const val DASHBOARD_SUPERVISOR_VIEW = "dashboard:supervisor_view"

    // ── Roles & effective permission sets ─────────────────────────────────────

    /** Backend Tier-1 role name that maps to full [OWNER] access. */
    const val ROLE_OWNER = "owner"

    /**
     * Full fleet-management authority for a tenant OWNER.
     *
     * Owners are authorized server-side via a role bypass (the backend's
     * `IsOwner()`), so their true authority is "everything". IAM currently grants
     * the owner ROLE but not this permission SET, which would otherwise leave a
     * freshly-onboarded owner locked out of every fleet screen. We expand an
     * owner's effective UI permissions to this catalog in ONE place (see
     * [effectivePermissions]) so call sites stay permission-based and never check
     * role names directly. Harmless / a no-op once IAM grants the complete set.
     *
     * Only [DASHBOARD_OWNER_VIEW] is included — the manager/supervisor view
     * selectors are role-specific and would render the wrong dashboard sections.
     */
    val OWNER: Set<String> = setOf(
        FINANCIALS_READ,
        VEHICLES_CREATE, VEHICLES_UPDATE, VEHICLES_DELETE,
        DRIVERS_CREATE, DRIVERS_UPDATE, DRIVERS_DELETE,
        TRIPS_CREATE, TRIPS_UPDATE, TRIPS_DELETE,
        COSTS_UPDATE, COSTS_DELETE,
        MAINTENANCE_UPDATE, MAINTENANCE_DELETE,
        CARETAKERS_ASSIGN,
        DOCUMENTS_UPDATE, DOCUMENTS_DELETE,
        USERS_READ, USERS_CREATE, USERS_INVITE, USERS_UPDATE, USERS_DELETE,
        USERS_TOGGLE_ACTIVE, USERS_CHANGE_ROLE, USERS_RESET_PASSWORD,
        DASHBOARD_OWNER_VIEW
    )

    /**
     * The effective UI permission set: the backend-granted [base] permissions,
     * expanded to the full [OWNER] set when [roles] marks the user as an owner.
     * Centralizes owner detection so the UI keeps gating purely on permission
     * strings (never on role names).
     */
    fun effectivePermissions(base: Set<String>, roles: Collection<String>): Set<String> =
        if (roles.any { it.equals(ROLE_OWNER, ignoreCase = true) }) base + OWNER else base
}
