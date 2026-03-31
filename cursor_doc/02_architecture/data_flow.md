# Data Flow & Request Lifecycle

## Complete Request Lifecycle

```
┌──────────────┐    sendIntent()    ┌──────────────┐
│              │ ──────────────────→ │              │
│    Screen    │                    │  ViewModel   │
│  (Compose)   │ ←────────────────  │  (MVI)       │
│              │   state / effects  │              │
└──────────────┘                    └──────┬───────┘
                                           │ calls
                                    ┌──────┴───────┐
                                    │   Use Case   │
                                    │  (optional)  │
                                    └──────┬───────┘
                                           │ calls
                                    ┌──────┴───────┐
                                    │  Repository  │
                                    │   (impl)     │
                                    └──────┬───────┘
                                           │
                          ┌────────────────┼────────────────┐
                          │                │                │
                   ┌──────┴──────┐  ┌──────┴──────┐  ┌─────┴─────┐
                   │   Remote    │  │   Local     │  │  Mapper   │
                   │ DataSource  │  │ DataSource  │  │           │
                   └──────┬──────┘  └──────┬──────┘  └───────────┘
                          │                │
                   ┌──────┴──────┐  ┌──────┴──────┐
                   │  Ktor HTTP  │  │ Room / KV   │
                   │   Client    │  │  Settings   │
                   └─────────────┘  └─────────────┘
```

## Repository Pattern (Typical Implementation)

```kotlin
override fun getItems(): Flow<Result<List<Item>>> = flow {
    emit(Result.Loading)

    val token = userLocalDataSource.getAuthToken()
        ?: run {
            emit(Result.Error(AuthException.unauthenticated(), "Not authenticated"))
            return@flow
        }

    try {
        val response = remoteDataSource.getItems(token)
        if (response.success && response.data != null) {
            val items = mapper.mapToDomainList(response.data)
            emit(Result.Success(items))
        } else {
            emit(Result.Error(ApiException(response.message), response.message))
        }
    } catch (e: Exception) {
        emit(Result.Error(e, ApiErrorHandler.getNetworkErrorMessage(e)))
    }
}
```

## Authentication Flow

```
                                    ┌─────────────────┐
                                    │ UserLocal        │
                                    │ DataSource       │
                                    │ (Settings KV)    │
                     getAuthToken() │                  │
                    ───────────────→│ auth_token       │
                                    │ user_role        │
                                    │ user_id          │
                                    └─────────────────┘

On Login Success:
  UserRepositoryImpl → saves token, role, id to UserLocalDataSource

On Every API Call:
  Repository → gets token from UserLocalDataSource → passes to DataSource → adds Bearer header

On 401 Response:
  HttpClientProvider → AuthenticationManager.emitSessionExpired()
    → Clears session (via registered callback)
    → App.kt collects auth event → navigates to Login

On Explicit Logout:
  ProfileViewModel → AuthenticationManager.emitLoggedOut()
    → Same clearance + navigation flow
```

## Error Handling Flow

```
API Call Failure
  → DataSource throws exception
    → Repository catches → wraps in Result.Error(exception, friendlyMessage)
      → ViewModel collects → updateState { copy(error = result.errorMessage) }
        → Screen renders ErrorContent with:
            - ErrorHandler.getErrorInfo(error, FleetErrorContext.VEHICLES)
            - Shows icon, title, message, retry button

Exception Classification (ErrorClassifier):
  NetworkException → NETWORK_CONNECTION / NETWORK_TIMEOUT
  ApiException(401) → AUTHENTICATION
  ApiException(403) → AUTHORIZATION
  ApiException(404) → NOT_FOUND
  ApiException(429) → RATE_LIMITED
  ApiException(4xx) → VALIDATION
  ApiException(5xx) → SERVER_ERROR
  ValidationException → VALIDATION
  Other → classifyFromMessage(error string)
```

## API Response Wrapper

All API responses follow this structure:

```kotlin
@Serializable
data class ApiResponse<T>(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: T? = null
)
```

## Date Format Conversion

```
UI Layer (DD-MM-YYYY, HH:MM)
  ↓ Conversion before API call
API Layer (varies by endpoint):
  - Trip scheduling: ISO 8601 (2026-03-14T15:30:00Z)
  - Cost entries: DD-MM-YYYY + HH:MM (sent as-is)
  - Document expiry: DD-MM-YYYY (sent as-is)

Conversion helpers:
  FleetDateTime.toIso8601(date, time)     → for trips
  TimeUtils.convertFormattedToIsoDateTime → for bulk costs
```

## Caching Strategy

| Data | Cache Location | Strategy |
|------|---------------|----------|
| Auth token + role | `multiplatform-settings` (KV) | Persist until logout/401 |
| Dashboard stats | Room (`DashboardCacheEntity`) | Cache → show → refresh from API |
| Cost types | Room (`CostTypesEntity`) | Load once at startup, refresh if stale |
| Customers | Room (`CustomerEntity`) | Local cache for selection sheets |
| Team members | Room (`TeamMemberEntity`) | Local cache |
| Onboarding flag | `multiplatform-settings` | Once per install |
