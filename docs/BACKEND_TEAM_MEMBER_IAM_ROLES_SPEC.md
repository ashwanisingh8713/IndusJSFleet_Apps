# Backend specification: Team member IAM roles & permissions

This document describes **Fleet API (`IndusJSFleet_GoLang_Backend`) and IAM integration** changes expected by the **IndusJSFleet mobile app** (Kotlin Multiplatform). The app currently adapts to the **legacy** contract (see "App compatibility" below); once the backend implements this spec, the app can send IAM role names directly and rely on the new list-roles endpoint.

---

## CRITICAL: Token issue after tenant creation (403 on team APIs)

### Problem

After a new owner creates a tenant (organization), the very next API call to `POST /api/v1/team/members` returns **403 "insufficient permissions to create team members"**.

### Root cause analysis

1. **IAM `CreateTenant` handler** (in `tenant_handler.go`) calls `IssueTokensForUser` to return a fresh JWT with the `tid` (tenant_id) claim and the owner role.
2. If `IssueTokensForUser` **fails** (returns error), the handler **silently** skips adding `access_token` / `refresh_token` to the response. The client keeps the **old pre-tenant JWT** which has **no** `tid` claim and **no** tenant-scoped permissions.
3. Fleet's `enrichPermissions` in `ExternalIAMClient` calls IAM `GET /api/v1/me/permissions` with the user's token. If this call **fails** (timeout, IAM unreachable, wrong token context), it **silently returns** with **empty** `Permissions` — and `RequireIAMPermission("users:create", …)` denies the request.

### Required backend fixes

1. **IAM `CreateTenant` handler**: If `IssueTokensForUser` fails, **return an error** (or at minimum, log the failure prominently). An empty `access_token` in a 201 response is misleading and leaves the client in an unrecoverable state.

2. **Fleet `enrichPermissions`**: When the permission fetch fails, **log a warning** instead of silently swallowing the error. Consider returning a specific error to the middleware so it can return a more descriptive error (e.g., "permission check unavailable, try again") rather than a flat 403.

3. **Verify `IssueTokensForUser` succeeds reliably**: After the transaction commits the tenant + owner role, the `GetByID` call inside `IssueTokensForUser` must see the updated `user.TenantID`. Ensure there is no read-replica lag or transaction isolation issue causing a stale read.

### App-side mitigations (already implemented)

- The app now decodes the JWT payload locally (`JwtHelper.kt`) and checks for the `tid` claim.
- After tenant creation: if `access_token` is blank, a session-expired event is emitted so the user re-logs in with a fresh token.
- Before team member creation: if the stored JWT has no `tid`, the user is prompted to re-login.
- On 403 "insufficient permissions": the error message asks the user to re-login.

---

## Goals

1. **Create team member** with an IAM tenant role — **`owner`**, **`admin`**, or **`user`** — aligned with tenant onboarding in IndusJS-IAM, not only Fleet-local labels `general_manager` / `manager` / `supervisor`.
2. Expose **GET assignable roles** from IAM so the client does not hard-code role metadata.
3. After user creation in IAM, **sync direct user permissions** from the role's permission list (IAM "manage permissions" behaviour).

---

## Current behaviour (legacy — before this spec)

- `POST /api/v1/team/members` accepts `role` as `general_manager` | `manager` | `supervisor` (Fleet DB).
- The use case calls IAM `CreateUser` with **`Roles: []string{"user"}`** regardless of the requested Fleet role, so IAM and Fleet can diverge.
- There is **no** Fleet route to list IAM tenant roles for the Add Member screen.

---

## 1. New endpoint: list assignable IAM roles

**Method / path:** `GET /api/v1/team/members/roles`

**Auth:** Bearer JWT (same as other team routes).

**Suggested permission:** Reuse `users:create` (same as creating a member) or `users:read` if product prefers read-only for the picker.

**Response (200):** Standard Fleet success envelope:

```json
{
  "success": true,
  "message": "Assignable roles retrieved",
  "data": {
    "roles": [
      {
        "id": "<uuid>",
        "name": "owner",
        "description": "Organization Owner - Full Access"
      },
      {
        "id": "<uuid>",
        "name": "admin",
        "description": "Administrator - Manage users and content"
      },
      {
        "id": "<uuid>",
        "name": "user",
        "description": "Standard team member"
      }
    ]
  }
}
```

**Implementation notes:**

- Proxy IAM `GET /api/v1/admin/roles` with the **owner's bearer token** (same pattern as other IAM-proxied calls).
- Return all three tenant roles: `owner`, `admin`, `user` in a **stable order**.
- The app uses `excludeElevated` logic to hide `owner` when the current user shouldn't be able to assign it (e.g., an admin creating a member).

---

## 2. Change create-team-member contract: IAM role names

**Path:** `POST /api/v1/team/members`

**Request body — `role` field:**

- **New:** `role` must be one of **`owner`**, **`admin`**, **`user`** (IAM tenant role names), matching IAM `CreateUserRequest.roles` and tenant role definitions.
- **Migration:** Deprecate `general_manager` / `manager` / `supervisor` on this endpoint once clients are updated, or accept both during a transition window.

**Fleet DB mapping (recommended):**

| IAM Role | Fleet DB Role     |
|----------|-------------------|
| `owner`  | `general_manager` |
| `admin`  | `manager`         |
| `user`   | `supervisor`      |

Keep local `user.role` as today for existing permission helpers **or** migrate Fleet domain to IAM names — product decision.

---

## 3. IAM `CreateUser` must use the requested role

In `CreateTeamMember` use case, build IAM request as:

```go
iamReq := iamDomain.CreateUserRequest{
    Email:            req.Email,
    Mobile:           req.Mobile,
    Password:         req.Password,
    FirstName:        req.FirstName,
    LastName:         req.LastName,
    Roles:            []string{req.Role}, // "owner", "admin", or "user"
    SendVerification: false,
}
```

Remove the hard-coded `[]string{"user"}`.

---

## 4. Sync direct permissions after create (manage permissions)

After successful IAM user creation, sync permissions from the role definition:

1. Call **`ListTenantRoles`** (or equivalent) to load the role definition including **`permissions`** with permission IDs.
2. For each permission on the selected role, call IAM  
   `POST /api/v1/admin/users/{iamUserId}/permissions`  
   with body `{ "permission_id": "<uuid>", "effect": "allow" }`.
3. Treat individual failures as non-fatal if duplicates or inherited perms already apply (log + continue).

This mirrors IAM RBAC "manage permissions" for the new user.

---

## 5. IAM client (Fleet) additions

Extend the Fleet IAM HTTP client / `UserService` port with:

- `ListTenantRoles(ctx, token) ([]TenantRoleInfo, error)` — `GET /api/v1/admin/roles`, parse `data.roles`.
- `AssignUserDirectPermission(ctx, token, userID, permissionID, effect string) error` — `POST /api/v1/admin/users/{id}/permissions`.

Types should include role `name`, `description`, and nested `permissions[].id` for the sync step.

---

## 6. Related: change-role endpoint

`PATCH /api/v1/team/members/:id/change-role` currently forces IAM to `"user"` in some implementations. It should map **Fleet role** ↔ **IAM role** consistently:

| Fleet Role        | IAM Role |
|-------------------|----------|
| `general_manager` | `owner`  |
| `manager`         | `admin`  |
| `supervisor`      | `user`   |

---

## 7. Tests & route registry

- Register `GET /v1/team/members/roles` **before** `GET /v1/team/members/:id` so `roles` is not captured as an id.
- Update integration tests: create member body uses `role: "owner"` / `"admin"` / `"user"` once the API is live.
- Update Postman / OpenAPI docs for team module.

---

## App compatibility (IndusJSFleet_Apps — current state)

Until the backend ships the above:

- The app calls `GET /api/v1/team/members/roles`; if the route is missing or returns non-success, it **falls back** to a static `owner` / `admin` / `user` picker.
- On create, the app maps **`owner` → `general_manager`**, **`admin` → `manager`**, **`user` → `supervisor`** in `TeamRepositoryImpl` so `POST /team/members` matches the **current** Fleet validation.
- The app verifies the JWT has a `tid` claim before calling any team API. If missing, it prompts re-login.

After the backend implements this document, the app can remove the mapping and send IAM role names directly (coordinate with mobile release).

---

## References (read-only for backend team)

- IndusJS-IAM: tenant onboarding creates `owner`, `admin`, `user` roles; `POST /api/v1/users` (admin) accepts `roles: ["admin"]` etc.
- IAM `IssueTokensForUser` in `auth_usecase.go` — must succeed after tenant creation for the client to receive the new JWT.
- IAM `enrichPermissions` in Fleet's `client.go` — silent failure causes 403; consider error propagation.
- IAM RBAC: `POST /api/v1/admin/users/:id/permissions` for direct permission assignment.
