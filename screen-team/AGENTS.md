# AGENTS.md — screen-team

## Purpose

Full-stack **Team Management** feature module. Allows fleet owners and general managers to manage team members (General Managers, Managers, Supervisors). Includes member listing, detail view, and creation with role-based permissions.

**Package:** `com.ijs.team`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/team/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   ├── TeamLocalDataSource.kt         # Local cache interface for team data
│   │   └── TeamRemoteDataSource.kt        # API: list, detail, create, update, delete team members
│   ├── mapper/
│   │   └── TeamMapper.kt                  # TeamDto ↔ TeamMember entity
│   ├── model/
│   │   └── TeamDto.kt                     # @Serializable DTOs
│   └── repository/
│       └── TeamRepositoryImpl.kt          # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   └── TeamMember.kt                  # TeamMember domain entity (name, role, permissions, status)
│   └── repository/
│       └── TeamRepository.kt              # Repository interface
└── presentation/
    ├── TeamFeatureFacade.kt               # DI entry point
    ├── TeamTypeConverters.kt              # Navigation 3 route argument helpers
    ├── create/
    │   ├── CreateTeamMemberContract.kt
    │   ├── CreateTeamMemberScreen.kt      # Add team member form (name, email, role, permissions)
    │   └── CreateTeamMemberViewModel.kt
    ├── detail/
    │   ├── TeamMemberDetailContract.kt
    │   ├── TeamMemberDetailScreen.kt      # Member detail with role + permissions display
    │   └── TeamMemberDetailViewModel.kt
    └── list/
        ├── TeamListContract.kt
        ├── TeamListScreen.kt              # Team member list with search
        └── TeamListViewModel.kt
```

---

## User Roles (Hierarchical)

| Role | Can Manage | Financial Access |
|------|-----------|-----------------|
| **Owner** | All roles | Full |
| **General Manager** | Manager, Supervisor | Full |
| **Manager** | — | No (no trip_price, no P&L) |
| **Supervisor** | — | No (view-only, can update trip status) |

Role hierarchy is enforced in the API. The create screen filters available roles based on the current user's role.

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client |

This is a **leaf module** — no dependency on other `screen-*` modules.

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| TeamListScreen | `TeamList` | All team members with search |
| TeamMemberDetailScreen | `TeamMemberDetail(id)` | Member info + role + permissions |
| CreateTeamMemberScreen | `CreateTeamMember` | Add GM/Manager/Supervisor |

---

## Key Patterns

- **Role-based creation** — `CreateTeamMemberScreen` filters available roles based on the logged-in user's role (Owner sees all, GM sees Manager+Supervisor).
- **Type converters** — `TeamTypeConverters` handles `TeamMember` serialization for Navigation 3 route arguments.
- **Local data source** — `TeamLocalDataSource` interface defined here; implementation in `sharedUI` with Room.
