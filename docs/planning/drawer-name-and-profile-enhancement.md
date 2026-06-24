# Plan — Drawer user-name fix + My Profile enhancement

## 1. Problem statements
1. **Hamburger drawer header** shows the greeting "Welcome back," but **no user name** (renders `—`).
2. **My Profile screen** "is not looking good" — perceived as just a large name banner; needs a cleaner, modern layout that also degrades gracefully when data is sparse.

## 2. Root-cause analysis (all aspects)

### 2a. Drawer name (confirmed in code)
- `NavigationDrawerContent.ProfileDrawerHeader` already renders the name: `userName.ifBlank { "—" }`. So the UI is wired correctly.
- `DashboardScreen` passes `userName = state.userName.ifEmpty { "User" }`.
- `DashboardViewModel` sets `state.userName = data.userInfo.fullName`.
- `DashboardUserInfo.fullName` = `"$firstName $lastName"` — **no `.trim()`**. When the dashboard API returns empty first/last name, this yields `" "` (a single space).
- `" "` is **non-empty**, so `state.userName.ifEmpty { "User" }` does NOT substitute the default; the whitespace string flows to the drawer, whose `ifBlank { "—" }` then shows `—`. ⇒ **root cause = un-trimmed whitespace name + reliance on the dashboard API's user fields.**
- **Authoritative name already exists locally:** `UserRepositoryImpl` calls `UserLocalDataSource.saveUserName(fullName)` at login, profile-fetch, and profile-update; `getUserName()` reads it. The dashboard/drawer don't use this single source of truth.

### 2b. Profile screen
- `ProfileScreen` is already card-based and reuses `EnhancedProfileCard` (header, contact-info, org-stats, owner-info, actions). It is **not** literally only a banner — BUT:
  - The header is a large, centered `primaryContainer` banner (100dp avatar) that visually dominates.
  - When `organizationStats`/`ownerInfo` are null and contact fields are blank (`—`), the rest looks empty, so the screen reads as "just a banner."
  - Shared data gap: if `/profile` returns sparse user fields, name falls back to email and details show `—`.

### 2c. Likely shared cause
Both symptoms point to **user/profile fields arriving empty** from the dashboard/profile APIs. The client can be made robust (prefer locally-saved name; render gracefully); if the backend genuinely returns empty names, that is a backend data gap → BUG to Session A.

## 3. Approach & alternatives

### 3a. Drawer name → authoritative local name + trim (chosen)
- Add `.trim()` to `DashboardUserInfo.fullName` (correctness for every consumer).
- Prefer the **locally-saved name** (`getUserName()`) as the display name, falling back to the dashboard `userInfo` name, then to `"User"`. Inject a lightweight name source into `DashboardViewModel` (reuse `UserLocalDataSource`, a foundation lib — not a cross-feature violation).
- Alternatives: (a) trim-only — insufficient if API names are empty; (b) fetch `/profile` in dashboard — heavier/duplicate network. Chosen reuses already-saved data (single source of truth), works offline.

### 3b. Profile header → cleaner, graceful, component-reuse (chosen)
- Replace the oversized centered banner with a compact, modern header card: avatar (reuse initials logic) + name + email + inline `RoleBadge`/`AccountStatusBadge` (kept). Less dominant, better hierarchy.
- Name fallback: `user.fullName` → saved name → email.
- Keep the detail/stats/actions cards; they already handle `—`/null gracefully.
- Strings EN+HI; `MaterialTheme.colorScheme.*` only.

## 4. Affected modules / contracts
- `ijs-network-lib`: `DashboardStats.kt` (`fullName.trim()`).
- `screen-dashboard`: `DashboardViewModel` (prefer saved name) + DI wiring (`DefaultViewModelProvider.dashboardViewModel`).
- `screen-user`: `ProfileScreen` (header redesign + name fallback); possibly `ProfileViewModel`.
- **No API contract change.** If `/profile` or `/dashboard` return empty user names → backend data gap, route BUG to A.

## 5. Design patterns / best practices
- Single source of truth for display name (local cache populated by the repo).
- MVI preserved; no logic in composables; reuse `Fleet*` / existing `Enhanced*` components; no hardcoded colours; EN+HI.

## 6. Edge cases
- Name unknown (fresh signup pre-profile) → fallback `"User"` / initials `?`.
- Long names → `maxLines = 1` + ellipsis.
- Sparse profile (null stats/owner) → sections hidden; header still looks good.
- Offline → saved name still shows.

## 7. Verification
- `:androidApp:assembleDebug`; confirm drawer shows the real name; profile header looks good with both full and sparse data.
- If still blank → curl `/api/v1/profile` + `/api/v1/dashboard`; empty user fields ⇒ BUG to A.

## 8. Future enhancements
- Extract a shared `FleetUserAvatar` / `FleetProfileHeader` into `ijs-ui-components-lib` reused by both the drawer and profile.
- Profile photo upload.
- A `GetDisplayNameUseCase` exposing the authoritative name app-wide.
