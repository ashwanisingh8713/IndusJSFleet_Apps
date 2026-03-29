# screen-team

## Overview

**Package:** `com.ijs.team`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Team member management — CRUD for General Managers, Managers, and Supervisors. Role-based hierarchy (Owner > GM > Manager > Supervisor) determines who can create/view which team members. Supports local caching for offline access.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `TeamFeatureFacade`, `TeamTypeConverters`, List (Contract/VM/Screen), Detail (Contract/VM/Screen), Create (Contract/VM/Screen) |
| **Domain** | `TeamMember`, `TeamMemberRole` entities; `TeamRepository` interface |
| **Data** | `TeamRemoteDataSource`, `TeamLocalDataSource`, `TeamRepositoryImpl`, `TeamMapper`, `TeamDto` |

**Local caching:** `TeamLocalDataSource` caches team member data for offline access and faster subsequent loads.

---

## Dependencies

```
screen-team → ijs-network-lib → ijs-core-lib
```

No cross-feature module dependencies. `screen-team` is a dependency **of** other modules (`screen-driver`, `screen-vehicle`) for caretaker selection, but itself depends on nothing feature-specific.

---

## Screens

### 1. TeamListScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.TeamList` |
| ViewModel | `TeamListViewModel` |
| Contract | `TeamListContract` |

**Features:**
- Team member cards with name, role badge, email, mobile, active status
- Role filter chips (All, General Manager, Manager, Supervisor)
- Search by name/email
- Member count summary
- FAB to create new team member (visible based on current user's role)
- Pull-to-refresh

---

### 2. TeamMemberDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.TeamMemberDetail(memberId)` |
| ViewModel | `TeamMemberDetailViewModel` |
| Contract | `TeamMemberDetailContract` |

**Features:**
- Member info: name, email, mobile, role, active status
- Role badge with color coding (GM=purple, Manager=blue, Supervisor=green)
- Created/updated timestamps
- Toggle active/inactive status
- Edit member info
- Delete member (with confirmation)

---

### 3. CreateTeamMemberScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.CreateTeamMember` |
| ViewModel | `CreateTeamMemberViewModel` |
| Contract | `CreateTeamMemberContract` |

**Features:**
- Form fields: First Name*, Last Name*, Email*, Mobile*, Password*, Role*
- Role selection: General Manager, Manager, Supervisor
  - `excludeGeneralManager` flag (set when current user is GM — can only create Manager/Supervisor)
- Field validation: email format, mobile 10 digits, password strength
- On success → navigates back to team list

---

## Facade

```kotlin
object TeamFeatureFacade {
    fun TeamListEntry(viewModel, onNavigateBack, onNavigateToCreateMember, onNavigateToMemberDetail)
    fun CreateTeamMemberEntry(viewModel, excludeGeneralManager, onNavigateBack)
    fun TeamMemberDetailEntry(viewModel, memberId, onNavigateBack)
}
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `TeamMember` | Full member data with computed `fullName`, `roleDisplayName`, `initials` |
| `TeamMemberRole` | Enum: GENERAL_MANAGER, MANAGER, SUPERVISOR with `toApiString()`/`fromApiString()` |

---

## Role Hierarchy & Permissions

| Current User | Can Create | Can View |
|-------------|------------|----------|
| Owner | GM, Manager, Supervisor | All |
| GM | Manager, Supervisor | Manager, Supervisor |
| Manager | — | Supervisor (limited) |
| Supervisor | — | — |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/team` | GET | List team members |
| `/team` | POST | Create team member |
| `/team/{id}` | GET | Get member details |
| `/team/{id}` | PUT | Update member |
| `/team/{id}` | DELETE | Delete member |
| `/team/{id}/toggle-active` | PATCH | Toggle active status |

---

## Inter-Module Communication

- `screen-team` is **depended upon** by `screen-driver` and `screen-vehicle` for `TeamMemberDto` (used in caretaker assignment dropdowns).
- `TeamLocalDataSource` provides cached team member lists for fast dropdown population in other modules.

