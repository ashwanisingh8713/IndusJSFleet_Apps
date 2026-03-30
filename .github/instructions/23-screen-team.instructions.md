# screen-team — IndusJS Fleet

## Purpose

Team management: CRUD for team members (GM, Manager, Supervisor), role-based access.

## Package: `com.ijs.team`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| TeamListScreen | `TeamList` | All team members |
| TeamMemberDetailScreen | `TeamMemberDetail(id)` | Member details + permissions |
| CreateTeamMemberScreen | `CreateTeamMember(excludeGM?)` | Add GM/Manager/Supervisor |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/TeamFeatureFacade.kt` | Facade — 3 entry points |
| `presentation/list/TeamList*.kt` | List Contract/VM/Screen |
| `presentation/detail/TeamMemberDetail*.kt` | Detail Contract/VM/Screen |
| `presentation/create/CreateTeamMember*.kt` | Create Contract/VM/Screen |
| `data/datasource/TeamRemoteDataSourceImpl.kt` | API calls |
| `data/datasource/TeamLocalDataSource.kt` | Local caching |
| `data/repository/TeamRepositoryImpl.kt` | Repository impl |
| `domain/entity/TeamMember.kt` | Domain entity |
| `domain/repository/TeamRepository.kt` | Repository interface |

## User Roles (Hierarchical)

```
owner > general_manager > manager > supervisor > driver
```

| Role | Access |
|------|--------|
| Owner | Full access — financials, team, all CRUD |
| General Manager | Financial access, manage Managers/Supervisors |
| Manager | Operational — no financial data (no trip_price, no P&L) |
| Supervisor | View-only, can update trip status only |

## APIs

- `GET /team` — List all
- `GET /team/{id}` — Detail
- `POST /team` — Create member
- `PUT /team/{id}` — Update

## Module Path

`screen-team/src/commonMain/kotlin/com/ijs/team/`

## Depends On: `ijs-network-lib`

