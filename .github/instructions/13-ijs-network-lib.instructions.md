# ijs-network-lib — IndusJS Fleet

## Purpose

Networking & cross-cutting data layer. HTTP client, auth, user data, dashboard data, shared costs,
Google Places. **Every feature module depends on this.**

## Package: `com.indusjs.fleet`

## Key Components

| Component | Path | Purpose |
|-----------|------|---------|
| `HttpClientProvider` | `data/network/HttpClientProvider.kt` | Ktor HttpClient setup |
| `ApiConfig` | `core/network/ApiConfig.kt` | Base URL, all endpoints |
| `ApiErrorHandler` | `core/network/ApiErrorHandler.kt` | Extract errors from responses |
| `NetworkError` | `core/network/NetworkError.kt` | Network error types |
| `AuthenticationManager` | `core/auth/AuthenticationManager.kt` | Auth state, 401 handling |
| `AuthTokenHelper` | `core/auth/AuthTokenHelper.kt` | Token refresh/storage |
| `UserLocalDataSource` | `data/datasource/user/` | Auth tokens, user session (Settings) |
| `UserRemoteDataSource` | `data/datasource/user/` | Login, signup, profile APIs |
| `DashboardRemoteDataSource` | `data/datasource/dashboard/` | Dashboard stats API |
| `CostsRemoteDataSource` | `data/datasource/costs/` | Shared cost operations |
| `GooglePlacesService` | `data/service/GooglePlacesService.kt` | Location autocomplete |
| `NetworkDataGraph` | `di/NetworkDataGraph.kt` | Metro DI graph |

## Auth Flow

```
Login → UserRemoteDataSource.login() → stores token in Settings
  → All subsequent requests: UserLocalDataSource.getAuthToken()
  → 401 response → AuthenticationManager.notifyAuthExpired() → redirect to Login
```

## Module Path

`ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/`

## Depends On: `ijs-core-lib` (via `api()`)
## Depended On By: All `screen-*` modules

## Key Exports (Transitive)

Everything from `ijs-core-lib` → `ijs-error-lib` → `ijs-dispatcher-lib` → `ijs-datetime-utils`
is available to any module that depends on `ijs-network-lib`.

