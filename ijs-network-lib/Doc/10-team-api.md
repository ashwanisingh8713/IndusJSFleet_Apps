# 10. Team Management API

> **Package:** `com.indusjs.fleet.data.datasource.team`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

| Method | Endpoint | Description | Who Can Call |
|--------|----------|-------------|-------------|
| `POST` | `/team/members` | Create team member | Owner, GM |
| `GET` | `/team/members` | List team members | Owner, GM |
| `GET` | `/team/members/{id}` | Get team member | Owner, GM |
| `PUT` | `/team/members/{id}` | Update team member | Owner, GM |
| `PATCH` | `/team/members/{id}/toggle-active` | Toggle active status | Owner, GM |
| `POST` | `/team/members/{id}/reset-password` | Reset member's password | Owner, GM |
| `DELETE` | `/team/members/{id}` | Delete team member | Owner, GM |

---

## Data Source Interface

### `TeamRemoteDataSource`

```kotlin
interface TeamRemoteDataSource {
    suspend fun createTeamMember(token: String, request: CreateTeamMemberRequest): TeamMemberApiResponse
    suspend fun getTeamMembers(token: String, role: String? = null): TeamMemberListApiResponse
    suspend fun getTeamMember(token: String, id: String): TeamMemberApiResponse
    suspend fun updateTeamMember(token: String, id: String, request: UpdateTeamMemberRequest): TeamMemberApiResponse
    suspend fun toggleTeamMemberActive(token: String, id: String): TeamMemberApiResponse
    suspend fun resetTeamMemberPassword(token: String, id: String, request: ResetPasswordRequest): TeamSimpleApiResponse
    suspend fun deleteTeamMember(token: String, id: String): TeamSimpleApiResponse
}
```

**Implementation:** `TeamRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

### `TeamLocalDataSource`

Caches team member list locally.

---

## Query Parameters (List)

| Parameter | Type | Description |
|-----------|------|-------------|
| `role` | `String?` | Filter by role: `general_manager`, `manager`, `supervisor` |

---

## Key Request DTOs

### `CreateTeamMemberRequest`

| Field | JSON Key | Type | Required | Notes |
|-------|----------|------|----------|-------|
| `email` | `email` | `String` | ✅ | |
| `mobile` | `mobile` | `String` | ✅ | |
| `firstName` | `first_name` | `String` | ✅ | |
| `lastName` | `last_name` | `String` | ✅ | |
| `password` | `password` | `String` | ✅ | |
| `role` | `role` | `String` | ✅ | See Role Hierarchy |

### `UpdateTeamMemberRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `firstName` | `first_name` | `String?` | ❌ |
| `lastName` | `last_name` | `String?` | ❌ |
| `email` | `email` | `String?` | ❌ |
| `mobile` | `mobile` | `String?` | ❌ |
| `role` | `role` | `String?` | ❌ |

### `ResetPasswordRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `newPassword` | `new_password` | `String` | ✅ |

---

## Role Hierarchy

```
Owner (Full Access)
  └── General Manager (can create Manager, Supervisor)
        └── Manager (operational access, no financials)
              └── Supervisor (view-only, can update trip status)
```

| Role | Code | Created By |
|------|------|------------|
| General Manager | `general_manager` | Owner |
| Manager | `manager` | Owner, GM |
| Supervisor | `supervisor` | Owner, GM |

---

## Source Files

| File | Path |
|------|------|
| TeamRemoteDataSource | `data/datasource/team/TeamRemoteDataSource.kt` |
| TeamLocalDataSource | `data/datasource/team/TeamLocalDataSource.kt` |
| DTOs | `data/model/team/TeamDto.kt` |

