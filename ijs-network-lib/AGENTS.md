# ijs-network-lib — AGENTS.md

## Purpose
HTTP networking infrastructure, authentication management, user data layer, cross-cutting shared services (dashboard, costs), and external API services for IndusJS Fleet.

## Architecture Role
`ijs-network-lib` is the **shared data services** module. All feature libs (`ijs-vehicle-lib`, `ijs-trip-lib`, etc.) depend on it via `api(project(":ijs-network-lib"))` to access:
- `ApiConfig` — all API endpoint constants
- `ApiErrorHandler` — standardized error extraction
- `AuthTokenHelper` — auth token requirement with session expiry redirect
- `UserLocalDataSource` — auth token storage (needed by every repository for authenticated API calls)
- `HttpClientProvider` — configured HttpClient factory

## What's Inside (32 files)

| Package | Content |
|---------|---------|
| `core.network` | `ApiConfig` (endpoint constants + base URL), `HttpClientProvider` (HttpClient + Json factories), `ApiErrorHandler` (error message extraction), `NetworkError` (sealed error types) |
| `core.auth` | `AuthenticationManager` (401 event bus, session clear), `AuthTokenHelper` (require-auth-or-redirect helper) |
| `data.datasource.user` | `UserLocalDataSource` (Settings-based token/role storage interface + impl), `UserRemoteDataSource` (login/signup/profile API interface + impl) |
| `data.datasource.dashboard` | `DashboardRemoteDataSource` (interface + impl), `DashboardLocalDataSource` (interface only — impl in sharedUI with Room) |
| `data.datasource.costs` | `CostsRemoteDataSource` (interface + impl), `CostsLocalDataSource` (interface only — impl in sharedUI with Room) |
| `data.datasource.location` | `GooglePlacesService` (Places autocomplete, details, Distance Matrix) |
| `data.model.user` | `UserDto`, `ApiResponse<T>`, `LoginRequest`, `SignUpRequest`, etc. |
| `data.model.dashboard` | `DashboardModels` — all dashboard DTOs |
| `data.mapper.user` | `UserMapper` — DTO ↔ Domain |
| `data.mapper.dashboard` | `DashboardMapper` — DTO ↔ Domain |
| `data.repository.user` | `UserRepositoryImpl` |
| `data.repository.dashboard` | `DashboardRepositoryImpl` (offline-first with cache) |
| `data.repository.costs` | `CostsRepositoryImpl`, `CostTypesRepositoryImpl` |
| `domain.entity.dashboard` | `DashboardStats` + related domain entities |
| `domain.entity.maps` | `MapEntities` (vehicle map status) |
| `domain.repository.user` | `UserRepository` interface |
| `domain.repository.dashboard` | `DashboardRepository` interface |
| `domain.repository.costs` | `CostsRepository`, `CostTypesRepository` interfaces |
| `domain.usecase.dashboard` | Dashboard use cases |
| `domain.usecase.costs` | Cost type use cases |
| `di` | `NetworkDataGraph` — DI composition root |

## NetworkDataGraph (DI)

Manages ONLY shared/infrastructure repositories:

```kotlin
val graph = NetworkDataGraph.create(
    httpClient, json, settings, dispatcherProvider,
    dashboardLocalDataSource, costsLocalDataSource
)
// Exposed:
graph.userRepository          // Auth, login, profile
graph.dashboardRepository     // Dashboard aggregation
graph.costsRepository         // Cost CRUD
graph.costTypesRepository     // Cost type cache
graph.userLocalDataSource     // Token storage
```

Feature-specific repositories (vehicle, trip, driver, etc.) are constructed directly in `sharedUI/DefaultViewModelProvider` from their respective feature libs.

## Dependencies
- `ijs-core-lib` (api) — transitively exposes `ijs-error-lib`, `ijs-dispatcher-lib`, `ijs-datetime-utils`
- Ktor Client (core, content-negotiation, serialization, logging)
- multiplatform-settings (for `UserLocalDataSource`)
- Metro (@Inject)

## Platform Source Sets
- `androidMain` — `ktor-client-okhttp`
- `iosMain` — `ktor-client-darwin`
- `jsMain` / `wasmJsMain` — `ktor-client-js`

## Key Pattern: Auth Token in Repositories
Every repository that calls authenticated APIs follows this pattern:
```kotlin
private suspend fun requireAuthToken(): String {
    return AuthTokenHelper.requireAuthTokenOrRedirect {
        userLocalDataSource.getAuthToken()
    }
}
```
This emits a `SessionExpired` event if the token is null, triggering navigation to login.
