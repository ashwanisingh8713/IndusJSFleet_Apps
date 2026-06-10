# Access Control — IAM Permissions & Role Bundles

> **Document status:** Rewritten June 2026 for the **permission-first IAM model**.
> The previous version of this document (legacy 4-role RBAC: Owner / General
> Manager / Manager / Supervisor) is preserved as a **deprecated appendix** at the
> bottom for historical reference.
>
> **Authoritative sources:**
> - PRD §2 — `IndusJS_Fleet_Software_Development_PRD.md` (product truth)
> - `Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md` (backend/IAM integration spec)
> - `Docs/BACKEND_DRIVER_IAM_ROLE_SETUP.md` (driver role provisioning)
> - IAM code: `IndusJS-IAM/internal/domain/rbac/` (`permission.go`, `role.go`)
> - Fleet backend permission constants: `internal/infrastructure/iam/mapper.go`

---

## 1. The Model — Permissions First, Not Titles

IndusJS Fleet does **not** hard-code job titles. Access to every feature, screen,
and API route is decided by **fine-grained IAM permissions**.

```
Permission = resource : action        e.g.  vehicles:create
                                            financials:read
                                            users:change_role
Wildcards supported:  *:read   vehicles:*   *:*
```

A **role** is nothing more than a **named bundle of permissions** with a numeric
hierarchy **level**:

| IAM Role Level | Constant | Used for |
|:--:|----------|----------|
| 100 | `RoleLevelOwner` | Highest authority in the tenant |
| 50 | `RoleLevelAdmin` | Administrative authority |
| 30 | `RoleLevelModerator` | Reserved / custom bundles |
| 10 | `RoleLevelMember` | Baseline member |

**Two separate questions, two separate mechanisms:**

| Question | Decided by |
|----------|-----------|
| *Can this user use this feature?* | **Permissions only** (`RequireIAMPermission` per route) |
| *Can this user manage that user / assign that role?* | **Levels only** (must be strictly above the target) |

> **Consequence:** any user, with the appropriate permissions, can access any
> feature or screen. Granting `financials:read` to an operations user instantly
> unlocks reports/payments for them — no code change, no special-case role.

---

## 2. Standard Role Bundles (Seeded per Tenant)

When an organization (tenant) is created, IAM auto-seeds three bundles
(`tenant_onboarding_usecase.go`); `driver` is provisioned per tenant/app:

| Bundle | Level | Seeded description | Default contents |
|--------|:-----:|--------------------|------------------|
| `owner` | 100 | Organization Owner — Full Access | Every permission, incl. `financials:read` and all `users:*` |
| `admin` | 50 | Administrator — Manage users and content | Operational CRUD (`vehicles:*`, `drivers:*`, `trips:*`, cost permissions); **no** `financials:read`, **no** team management |
| `user` | 10 | Standard User — Basic Access | Read access + trip status updates + own cost entries |
| `driver` | app-scoped | Driver login | GPS publishing (Location Tracker APK); *(future)* cost-upload permissions for the field cost-capture app (PRD FE-07) |

**Custom bundles** (e.g., an "Accountant" = read-only + `financials:read`) are
supported by the IAM data model today; an in-app bundle editor is roadmap
item **FE-14** in the PRD.

---

## 3. Permission Catalog (Fleet Backend)

Defined in `internal/infrastructure/iam/mapper.go`, enforced per route via
`RequireIAMPermission`:

| Domain | Permissions |
|--------|------------|
| Vehicles | `vehicles:create`, `vehicles:update`, `vehicles:delete` |
| Drivers | `drivers:create`, `drivers:update`, `drivers:delete` |
| Trips | `trips:create`, `trips:update`, `trips:delete` |
| Costs | `costs:update`, `costs:delete`, `trip-costs:delete` |
| Maintenance | `maintenance:update`, `maintenance:delete` |
| Documents | `documents:update`, `documents:delete` |
| Financials | `financials:read` — gates trip price, P&L reports, payments, vehicle finance |
| Team/Users | `users:read`, `users:create`, `users:invite`, `users:update`, `users:delete`, `users:toggle_active`, `users:change_role`, `users:reset_password` |
| Dashboard | `dashboard:owner_view`, `dashboard:manager_view`, `dashboard:supervisor_view` |
| Caretakers | `caretakers:assign` |

**The single most important permission is `financials:read`** — it is the
financial-visibility boundary. Everything money-related (trip price, revenue,
profit, payments, reports, vehicle finance) is invisible without it.

---

## 4. Capability Matrix (by Default Bundle)

Each capability is unlocked by a permission; the columns show which **default**
bundles include it. Granting the permission to any bundle moves the ✅.

| Capability | Gating permission | `owner` | `admin` | `user` | `driver` |
|------------|-------------------|:--:|:--:|:--:|:--:|
| See revenue / profit / trip price | `financials:read` | ✅ | ❌ | ❌ | ❌ |
| View P&L reports / export PDF | `financials:read` | ✅ | ❌ | ❌ | ❌ |
| Record customer payments | `financials:read` | ✅ | ❌ | ❌ | ❌ |
| Vehicle finance (loans/EMI) | `financials:read` | ✅ | ❌ | ❌ | ❌ |
| Manage team members | `users:*` family | ✅ (below own level) | ❌ | ❌ | ❌ |
| Add / edit vehicles, drivers, trips | `*:create` / `*:update` | ✅ | ✅ | ❌ | ❌ |
| Delete vehicles / trips | `vehicles:delete`, `trips:delete` | ✅ | ❌ | ❌ | ❌ |
| Edit trip in any state | `trips:update` + level rule | ✅ | planned-only | ❌ | ❌ |
| Record costs | cost permissions | ✅ | ✅ | own entries | 🔮 FE-07 |
| Delete cost entries | `costs:delete`, `trip-costs:delete` | ✅ | ✅ | ❌ | ❌ |
| Update trip status | baseline | ✅ | ✅ | ✅ | 🔮 own trips |
| View fleet data | baseline | ✅ | ✅ | ✅ | 🔮 own data |
| Assign caretakers | `caretakers:assign` | ✅ | ❌ | ❌ | ❌ |
| Billing & subscription | tenant creator only | ✅ (creator) | ❌ | ❌ | ❌ |
| Publish GPS (tracker app) | driver bundle | — | — | — | ✅ |

Screen-level access matrix: see PRD §2.6.

---

## 5. Enforcement Chain

```
App UI         PermissionUtils (ijs-core-lib) hides elements early
   │
   ▼
App → Fleet API   Bearer JWT — MUST contain the `tid` (tenant id) claim
   │              (verified locally via JwtHelper.kt; missing → force re-login)
   ▼
Fleet backend     fetches caller's permissions from IAM (GET /me/permissions)
   │              middleware: RequireIAMPermission("<resource:action>")
   ▼
403 Forbidden     if the permission is absent  →  show friendly message,
                                                  do NOT clear the session
401 Unauthorized  if the session is invalid    →  auto-logout to Login
```

Rules of thumb:
- **Server is the final authority.** Client checks are UX, not security.
- **403 ≠ 401.** Permission-denied keeps the session; expired token ends it.
- A token without `tid` has no tenant permissions — every call 403s. Detect it
  locally and prompt re-login instead of looping.

---

## 6. Team Management Flow (App)

- The **Add Team Member** screen loads assignable bundles live from
  `GET /api/v1/team/members/roles`; if unavailable it falls back to the static
  `owner` / `admin` / `user` list.
- A user may only assign bundles **below their own level** (an `admin` can never
  create another `owner`).
- Admin actions on a member are each gated by their own permission:
  Change Role (`users:change_role`), Enable/Disable (`users:toggle_active`),
  Reset Password (`users:reset_password`).
- Drivers get IAM accounts (role `driver`) when created with a password on the
  Create Driver screen — used by the Location Tracker app. If the backend has not
  seeded the `driver` role for the tenant, creation fails with
  "role not found in tenant" (see `BACKEND_DRIVER_IAM_ROLE_SETUP.md`).

### App-side helpers

- `PermissionUtils` (`ijs-core-lib`) — UI gating mirrors
  (`canViewTripPrice`, `canViewFinancials`, `canEditTrip(role, tripStatus)`,
  `canManageTeam`, `getCreatableRoles`, …).
- `JwtHelper` (`ijs-network-lib/.../core/auth/`) — local `tid` claim validation.
- ⚠️ **Transition note:** the app/DB still carries legacy labels
  (`general_manager` / `manager` / `supervisor`) in `UserRole` and
  `TeamRepositoryImpl` mapping. Removing them is roadmap item **FE-01** —
  new code must use IAM bundle names only.

---

## 7. Best Practices

- **Check the permission, not the role name.** Never write
  `if (role == "manager")` — ask "does this user hold `trips:create`?"
- Hide UI elements the user can't use (don't show disabled buttons).
- Double-check permissions in the ViewModel before API calls; handle 403
  gracefully with a friendly message.
- When designing a new feature, define its permission string(s) first and add
  them to the catalog in §3 and the backend mapper.

---
---

## Appendix — Legacy 4-Role RBAC (DEPRECATED)

> ⚠️ The content below describes the **pre-IAM** role model
> (Owner / General Manager / Manager / Supervisor). It is retained only as a
> historical reference for the *default bundle contents* and old API behavior.
> Do **not** build new functionality against these role names.
>
> Mapping: `owner` ← Owner / General Manager · `admin` ← Manager ·
> `user` ← Supervisor · `driver` ← Driver.

### Legacy Role Hierarchy

```
OWNER  >  GENERAL MANAGER  >  MANAGER  >  SUPERVISOR
```

| Legacy role | API value | Created by | Could create |
|-------------|-----------|------------|--------------|
| Owner | `owner` | Self (Sign Up) | GM, Manager, Supervisor |
| General Manager | `general_manager` | Owner | Manager, Supervisor |
| Manager | `manager` | Owner / GM | Supervisor |
| Supervisor | `supervisor` | Owner / GM / Manager | — |

### Legacy Module-Level Permissions

| Module | Owner | General Manager | Manager | Supervisor |
|--------|:-----:|:---------------:|:-------:|:----------:|
| Dashboard | Full | Full | Limited | Minimal |
| Vehicles | Full CRUD | Full CRUD | Full CRUD | View Only |
| Drivers | Full CRUD | Full CRUD | Full CRUD | View Only |
| Trips | Full CRUD | Full CRUD | CRUD (no price) | View Assigned |
| Costs | Full CRUD | Full CRUD | Create/View | Create Own |
| Payments | Full CRUD | Full CRUD | View Only | View Only |
| Customers | Full CRUD | Full CRUD | Full CRUD | View Only |
| Reports | Full | Full | Limited | None |
| Team | Full CRUD | CRUD (no Owner) | View Only | None |
| Settings | Full | Limited | Profile Only | Profile Only |

### Legacy Feature-Specific Permissions

| Feature | Owner | GM | Manager | Supervisor |
|---------|:-----:|:--:|:-------:|:----------:|
| View Trip Price | ✅ | ✅ | ❌ | ❌ |
| Edit Trip Price | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Any State) | ✅ | ✅ | ❌ | ❌ |
| Delete Costs | ✅ | ✅ | ✅ | ❌ |
| Manage Team | ✅ | ✅ | ❌ | ❌ |
| Assign Caretaker | ✅ | ✅ | ❌ | ❌ |
| View P&L Reports | ✅ | ✅ | ❌ | ❌ |
| Record Payments | ✅ | ✅ | ❌ | ❌ |
| Export PDF Reports | ✅ | ✅ | ✅ | ❌ |
| View Driver Costs | ✅ | ✅ | ❌ | ❌ |

### Legacy Navigation Menu Visibility

| Menu Item | Owner | GM | Manager | Supervisor |
|-----------|:-----:|:--:|:-------:|:----------:|
| Dashboard / Vehicles / Drivers / Trips / Customers | ✅ | ✅ | ✅ | ✅ |
| Payments | ✅ | ✅ | ✅ | ❌ |
| Reports | ✅ | ✅ | ❌ | ❌ |
| Team | ✅ | ✅ | ❌ | ❌ |
| Settings | ✅ | ✅ | ✅ | ✅ |

### Legacy Action Button Visibility

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| Add / Edit Vehicle | ✅ | ✅ | ✅ | ❌ |
| Delete Vehicle | ✅ | ✅ | ❌ | ❌ |
| Add / Edit Driver | ✅ | ✅ | ✅ | ❌ |
| Delete Driver | ✅ | ❌ | ❌ | ❌ |
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| Edit Trip | ✅ | ✅ | Planned Only | ❌ |
| Cancel Trip | ✅ | ✅ | ✅ | ❌ |
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| Record Payment | ✅ | ✅ | ❌ | ❌ |
| Add Team Member | ✅ | ✅ | ❌ | ❌ |

### Legacy User Entity Helpers

| Property | Returned true for |
|----------|-------------------|
| isOwner / isGeneralManager / isManager / isSupervisor | exact role |
| hasFinancialAccess, canEditTripInAnyState, canManageTeam, canAssignCaretaker, canViewTripPrice | Owner, GM |
| canDeleteCosts | Owner, GM, Manager |

### Legacy Rate Limits by Role

| Role | Standard API | Location API | Auth API |
|------|-------------|--------------|----------|
| Owner / GM | 100 req/min | 1000 req/min | 10 req/min |
| Manager | 100 req/min | 500 req/min | 10 req/min |
| Supervisor | 50 req/min | 200 req/min | 10 req/min |

---

## Related Documentation

- PRD §2 — Target Users, Roles & Access Hierarchy (`../../IndusJS_Fleet_Software_Development_PRD.md`)
- [IAM team-member spec](../BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md)
- [Driver IAM role setup](../BACKEND_DRIVER_IAM_ROLE_SETUP.md)
- [Modules Overview](../modules/README.md)
- [Architecture Guide](../architecture/architecture-README.md)

