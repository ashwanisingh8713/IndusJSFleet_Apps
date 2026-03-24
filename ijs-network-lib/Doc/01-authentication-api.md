# 01. Authentication API

> **Package:** `com.indusjs.fleet.data.datasource.user`, `com.indusjs.fleet.core.auth`  
> **Auth Required:** No (except change-password)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/signup` | Register a new owner account | ❌ |
| `POST` | `/auth/login` | Login with email/mobile + password | ❌ |
| `POST` | `/auth/forgot-password` | Send password reset link | ❌ |
| `POST` | `/auth/reset-password` | Reset password with token | ❌ |
| `POST` | `/profile/change-password` | Change password (logged-in user) | ✅ Bearer |

---

## Data Source Interface

### `UserRemoteDataSource`

```kotlin
interface UserRemoteDataSource : RemoteDataSource {
    suspend fun signUp(request: SignUpRequest): ApiResponse<AuthResponseDto>
    suspend fun login(request: LoginRequest): ApiResponse<AuthResponseDto>
    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResponse<Unit>
    suspend fun resetPassword(request: ResetPasswordRequest): ApiResponse<Unit>
    suspend fun changePassword(token: String, request: ChangePasswordRequest): ApiResponse<Unit>
}
```

**Implementation:** `UserRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`

---

## Request DTOs

### `SignUpRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `firstName` | `first_name` | `String` | ✅ |
| `lastName` | `last_name` | `String` | ✅ |
| `email` | `email` | `String` | ✅ |
| `mobile` | `mobile` | `String` | ✅ |
| `password` | `password` | `String` | ✅ |

### `LoginRequest`

| Field | JSON Key | Type | Required | Notes |
|-------|----------|------|----------|-------|
| `identifier` | `identifier` | `String` | ✅ | Email or mobile number |
| `password` | `password` | `String` | ✅ | |

### `ForgotPasswordRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `identifier` | `identifier` | `String` | ✅ |

### `ResetPasswordRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `identifier` | `identifier` | `String` | ✅ |
| `newPassword` | `new_password` | `String` | ✅ |

### `ChangePasswordRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `currentPassword` | `current_password` | `String` | ✅ |
| `newPassword` | `new_password` | `String` | ✅ |
| `confirmPassword` | `confirm_password` | `String` | ✅ |

---

## Response DTOs

### `AuthResponseDto`

| Field | Type | Description |
|-------|------|-------------|
| `user` | `UserDto` | User profile data |
| `token` | `String` | JWT bearer token |

### `UserDto`

| Field | JSON Key | Type |
|-------|----------|------|
| `id` | `id` | `Int` |
| `email` | `email` | `String` |
| `mobile` | `mobile` | `String` |
| `firstName` | `first_name` | `String` |
| `lastName` | `last_name` | `String` |
| `role` | `role` | `String` |
| `ownerId` | `owner_id` | `Int?` |
| `isActive` | `is_active` | `Boolean` |
| `createdAt` | `created_at` | `String` |
| `updatedAt` | `updated_at` | `String?` |

### `ApiResponse<T>` (Generic Wrapper)

| Field | Type | Description |
|-------|------|-------------|
| `success` | `Boolean` | Whether the request succeeded |
| `message` | `String?` | Server message |
| `data` | `T?` | Response payload |

---

## Session Management

### `UserLocalDataSource`

Stores auth session locally via `multiplatform-settings`.

| Method | Returns | Description |
|--------|---------|-------------|
| `saveAuthToken(token)` | `Unit` | Persist JWT token |
| `getAuthToken()` | `String?` | Retrieve JWT token |
| `saveUserRole(role)` | `Unit` | Persist user role |
| `getUserRole()` | `String?` | Retrieve user role |
| `saveUserId(userId)` | `Unit` | Persist user ID |
| `getUserId()` | `String?` | Retrieve user ID |
| `clearSession()` | `Unit` | Remove all auth data |
| `isLoggedIn()` | `Boolean` | Token exists and is non-blank |

**Implementation:** `UserLocalDataSourceImpl` — `@Inject`, depends on `Settings`

### `AuthTokenHelper`

Utility for repositories to require auth token or redirect to login:

```kotlin
val token = AuthTokenHelper.requireAuthTokenOrRedirect {
    userLocalDataSource.getAuthToken()
}
```

- **Null token → `SessionExpired` event + `AuthException`**
- Uses `MESSAGE_NOT_LOGGED_IN` to differentiate from 401 expired
- Race-condition safe via `AuthenticationManager.isHandlingUnauthorized` flag

### `AuthenticationManager`

Singleton event bus for authentication state changes.

| Event | Trigger |
|-------|---------|
| `Unauthorized` | 401 response with `Authorization` header |
| `LoggedOut` | User explicitly calls logout |
| `SessionExpired(message)` | Null token or expired session |

| Method | Description |
|--------|-------------|
| `registerSessionClearCallback(callback)` | Register session cleanup function |
| `emitUnauthorized()` | Emit 401 event (clears session first) |
| `emitSessionExpired(message)` | Emit session expired event |
| `emitLoggedOut()` | Emit logged out event |
| `authEvents: SharedFlow` | Observe auth events |

---

## Source Files

| File | Path |
|------|------|
| UserRemoteDataSource | `data/datasource/user/UserRemoteDataSource.kt` |
| UserLocalDataSource | `data/datasource/user/UserLocalDataSource.kt` |
| AuthenticationManager | `core/auth/AuthenticationManager.kt` |
| AuthTokenHelper | `core/auth/AuthTokenHelper.kt` |
| DTOs | `data/model/user/UserDto.kt` |

