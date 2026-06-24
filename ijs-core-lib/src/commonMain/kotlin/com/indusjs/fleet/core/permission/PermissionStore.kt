package com.indusjs.fleet.core.permission

/**
 * In-memory holder for the current user's permission set.
 *
 * This is the single source of truth the UI reads through [PermissionChecker].
 * It is populated from the backend (`GET /api/v1/me/permissions`) after login /
 * session restore, with a fall-back to the last cached permission set.
 *
 * Threading note: this is used purely for UX gating (the backend is the real
 * authority), so a plain mutable reference is acceptable here. Reads/writes are
 * cheap reference swaps; we keep the backing set immutable so a reader never
 * observes a partially-mutated collection.
 */
object PermissionStore {

    // Plain reference; the backing set is always immutable so readers never see a
    // partially-mutated collection. Sufficient for UX gating (backend is authority).
    private var permissions: Set<String> = emptySet()

    /** Replace the current permission set. */
    fun update(perms: Set<String>) {
        permissions = perms.toSet()
    }

    /** Clear all permissions (e.g. on logout / session expiry). */
    fun clear() {
        permissions = emptySet()
    }

    /** Snapshot of the current permission set. */
    fun current(): Set<String> = permissions
}
