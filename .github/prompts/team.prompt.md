# Team Management Feature

> Use this prompt when working on team member CRUD or role-based restrictions.

## Access Control

- **Owner:** Full access — create any role, manage all members
- **General Manager:** Can create Manager and Supervisor only (`excludeGeneralManager = true`)
- **Manager/Supervisor:** Cannot access team management

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `TeamListScreen` | `TeamList` | All team members with role filter |
| `TeamMemberDetailScreen` | `TeamMemberDetail(memberId)` | Member info, permissions, toggle active |
| `CreateTeamMemberScreen` | `CreateTeamMember(excludeGeneralManager)` | Add new team member |

## API Endpoints

```
GET    /team-members                → List all team members
GET    /team-members/{id}           → Member details
POST   /team-members                → Create member
PUT    /team-members/{id}           → Update member
DELETE /team-members/{id}           → Delete member
PATCH  /team-members/{id}/toggle-active → Toggle active/inactive
```

## Roles Hierarchy

```
Owner
  └── General Manager
        └── Manager
              └── Supervisor
```

Each role can only create members of **lower** rank. GM creates Manager/Supervisor. Owner creates any.

## Domain Entity: `TeamMember`

```kotlin
data class TeamMember(
    val id: String,
    val name: String,
    val email: String,
    val mobile: String?,
    val role: String,            // "general_manager", "manager", "supervisor"
    val isActive: Boolean,
    val createdAt: String?
)
```

## Offline Caching

Team members are cached locally via `TeamLocalDataSourceImpl` using Room DAO. Used for caretaker assignment in vehicle/driver forms.

## Key Files

| Layer | File |
|-------|------|
| Repository | `domain/repository/team/TeamRepository.kt` |
| DataSource | `data/datasource/team/` (Remote + Local) |
| Repository Impl | `data/repository/team/TeamRepositoryImpl.kt` |
| List | `presentation/team/list/` |
| Detail | `presentation/team/detail/` |
| Create | `presentation/team/create/` |

