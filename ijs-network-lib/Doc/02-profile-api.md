# 02. Profile API

> **Package:** `com.indusjs.fleet.data.datasource.user`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/profile` | Get current user's profile |
| `PUT` | `/profile` | Update current user's profile |
| `POST` | `/profile/change-password` | Change password |

---

## Data Source Methods

Part of `UserRemoteDataSource`:

```kotlin
suspend fun getProfile(token: String): ApiResponse<UserProfileDto>
suspend fun updateProfile(token: String, request: UpdateProfileRequest): ApiResponse<UserDto>
suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResponse<Unit>
```

---

## Request DTOs

### `UpdateProfileRequest`

| Field | JSON Key | Type | Required | Notes |
|-------|----------|------|----------|-------|
| `firstName` | `first_name` | `String?` | ❌ | Only send fields to update |
| `lastName` | `last_name` | `String?` | ❌ | |
| `email` | `email` | `String?` | ❌ | |
| `mobile` | `mobile` | `String?` | ❌ | |

---

## Response DTOs

### `UserProfileDto`

Extended profile with owner stats and info.

| Field | JSON Key | Type | Description |
|-------|----------|------|-------------|
| `id` | `id` | `Int` | User ID |
| `email` | `email` | `String` | Email address |
| `mobile` | `mobile` | `String` | Mobile number |
| `firstName` | `first_name` | `String` | First name |
| `lastName` | `last_name` | `String` | Last name |
| `role` | `role` | `String` | User role (`owner`, `general_manager`, `manager`, `supervisor`) |
| `ownerId` | `owner_id` | `Int?` | Owner ID (for team members) |
| `isActive` | `is_active` | `Boolean` | Account active status |
| `createdAt` | `created_at` | `String` | Account creation timestamp |
| `updatedAt` | `updated_at` | `String?` | Last update timestamp |
| `ownerStats` | `owner_stats` | `OwnerStatsDto?` | Stats for owner role |
| `ownerInfo` | `owner_info` | `OwnerInfoDto?` | Owner info for team members |

### `OwnerStatsDto`

Available when user role is `owner`:

| Field | JSON Key | Type |
|-------|----------|------|
| `totalManagers` | `total_managers` | `Int` |
| `totalSupervisors` | `total_supervisors` | `Int` |
| `totalTeamMembers` | `total_team_members` | `Int` |
| `totalVehicles` | `total_vehicles` | `Int` |
| `activeVehicles` | `active_vehicles` | `Int` |
| `totalTrips` | `total_trips` | `Int` |
| `activeTrips` | `active_trips` | `Int` |
| `completedTrips` | `completed_trips` | `Int` |

### `OwnerInfoDto`

Available for team members (non-owner roles):

| Field | JSON Key | Type |
|-------|----------|------|
| `ownerId` | `owner_id` | `Int` |
| `ownerName` | `owner_name` | `String` |
| `ownerEmail` | `owner_email` | `String` |

---

## Mapper

### `UserMapper`

Converts between DTO and domain entity:

| Method | Description |
|--------|-------------|
| `toDomain(UserProfileDto)` | Converts profile DTO to `User` entity |
| `toDomain(UserDto)` | Converts user DTO to `User` entity |

---

## Repository

### `UserRepository` (Interface)

```kotlin
interface UserRepository {
    suspend fun login(identifier: String, password: String): Flow<Result<User>>
    suspend fun signUp(request: SignUpRequest): Flow<Result<User>>
    suspend fun getProfile(): Flow<Result<User>>
    suspend fun updateProfile(request: UpdateProfileRequest): Flow<Result<User>>
    suspend fun changePassword(request: ChangePasswordRequest): Flow<Result<Unit>>
    suspend fun forgotPassword(identifier: String): Flow<Result<Unit>>
    suspend fun resetPassword(request: ResetPasswordRequest): Flow<Result<Unit>>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun getUserRole(): String?
}
```

**Implementation:** `UserRepositoryImpl` in `data/repository/user/`

---

## Source Files

| File | Path |
|------|------|
| UserRemoteDataSource | `data/datasource/user/UserRemoteDataSource.kt` |
| DTOs | `data/model/user/UserDto.kt` |
| Mapper | `data/mapper/user/UserMapper.kt` |
| Repository Interface | `domain/repository/user/UserRepository.kt` |
| Repository Impl | `data/repository/user/UserRepositoryImpl.kt` |

