# Backend specification: Team member IAM roles & permissions

This document describes **Fleet API (`IndusJSFleet_GoLang_Backend`) and IAM integration** changes expected by the **IndusJSFleet mobile app** (Kotlin Multiplatform). The app currently adapts to the **legacy** contract (see “App compatibility” below); once the backend implements this spec, the app can send IAM role names directly and rely on the new list-roles endpoint.

---

## Goals

1. **Create team member** with an IAM tenant role **`admin`** or **`user`** (aligned with tenant onboarding in IndusJS-IAM), not only Fleet-local labels `manager` / `supervisor`.
2. Expose **GET assignable roles** from IAM so the client does not hard-code role metadata.
3. After user creation in IAM, optionally **sync direct user permissions** from the role’s permission list (IAM “manage permissions” behaviour), if product policy requires explicit overrides in addition to role assignment.

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

**Response (200):** Standard Fleet success envelope, e.g.:

```json
{
  "success": true,
  "message": "Assignable roles retrieved",
  "data": {
    "roles": [
      {
        "id": "<uuid>",
        "name": "admin",
        "description": "Administrator - Manage users and content"
      },
      {
        "id": "<uuid>",
        "name": "user",
        "description": "..."
      }
    ]
  }
}
```

**Implementation notes:**

- Proxy IAM `GET /api/v1/admin/roles` with the **owner’s bearer token** (same pattern as other IAM-proxied calls).
- Filter to roles assignable to new members (e.g. only `admin` and `user`, exclude `owner`), and return a **stable order** (e.g. `admin` then `user`).

---

## 2. Change create-team-member contract: IAM role names

**Path:** `POST /api/v1/team/members`

**Request body — `role` field:**

- **New:** `role` must be one of **`admin`**, **`user`** (IAM tenant role names), matching IAM `CreateUserRequest.roles` and tenant role definitions.
- **Migration:** Deprecate `general_manager` / `manager` / `supervisor` on this endpoint once clients are updated, or accept both during a transition window (not required for KMP app if app only sends `admin`/`user` after backend ships).

**Fleet DB mapping (recommended):**

- Keep local `user.role` as today (`manager` / `supervisor` / `general_manager`) for existing permission helpers **or** migrate Fleet domain to IAM names — product decision. Minimum: store a consistent mapping, e.g. `admin` → `manager`, `user` → `supervisor` for backward compatibility with existing middleware.

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
    Roles:            []string{req.Role}, // "admin" or "user"
    SendVerification: false,
}
```

Remove the hard-coded `[]string{"user"}`.

---

## 4. Optional: sync direct permissions after create

If business requires calling IAM **assign permission** for each permission on the role (in addition to role assignment on create):

1. After successful IAM user create, call **`ListTenantRoles`** (or equivalent) to load the role definition including **`permissions`** with permission IDs.
2. For each permission on the selected role, call IAM  
   `POST /api/v1/admin/users/{iamUserId}/permissions`  
   with body `{ "permission_id": "<uuid>", "effect": "allow" }`.
3. Treat individual failures as non-fatal if duplicates or inherited perms already apply (log + continue).

This mirrors IAM RBAC “manage permissions” for the new user.

---

## 5. IAM client (Fleet) additions

Extend the Fleet IAM HTTP client / `UserService` port with:

- `ListTenantRoles(ctx, token) ([]TenantRoleInfo, error)` — `GET /api/v1/admin/roles`, parse `data.roles`.
- `AssignUserDirectPermission(ctx, token, userID, permissionID, effect string) error` — `POST /api/v1/admin/users/{id}/permissions`.

Types should include role `name`, `description`, and nested `permissions[].id` for the sync step.

---

## 6. Related: change-role endpoint

`PATCH /api/v1/team/members/:id/change-role` currently forces IAM to `"user"` in some implementations. It should map **Fleet role** ↔ **IAM role** consistently (e.g. manager → `admin`, supervisor → `user`) when updating IAM.

---

## 7. Tests & route registry

- Register `GET /v1/team/members/roles` **before** `GET /v1/team/members/:id` so `roles` is not captured as an id.
- Update integration tests: create member body uses `role: "admin"` / `"user"` once the API is live.
- Update Postman / OpenAPI docs for team module.

---

## App compatibility (IndusJSFleet_Apps — no backend deploy yet)

Until the backend ships the above:

- The app calls `GET /api/v1/team/members/roles`; if the route is missing or returns non-success, it **falls back** to a static `admin` / `user` picker.
- On create, the app maps **`admin` → `manager`**, **`user` → `supervisor`** in `TeamRepositoryImpl` so `POST /team/members` matches the **current** Fleet validation.

After the backend implements this document, the app can remove that mapping and send `admin`/`user` in the JSON body directly (coordinate with mobile release).

---

## References (read-only for backend team)

- IndusJS-IAM: tenant onboarding creates tenant roles including `admin` and `user`; `POST /api/v1/users` (admin) accepts `roles: ["admin"]` etc.
- IAM RBAC: `POST /api/v1/admin/users/:id/permissions` for direct permission assignment.
