# ijs-network-lib — AGENTS.md

## Purpose
HTTP networking infrastructure, authentication management, user data layer, and external API services for IndusJS Fleet.

## What's Inside

| Package | Content |
|---------|---------|
| `core.network` | `ApiConfig` (all endpoint constants), `HttpClientProvider` (HttpClient + Json factories), `ApiErrorHandler`, `NetworkError` |
| `core.auth` | `AuthenticationManager` (session event bus), `AuthTokenHelper` (require-auth-or-redirect) |
| `di` | `NetworkDataGraph` — centralized DI composition root for all data layer dependencies |
| `data.datasource.user` | `UserLocalDataSource` (Settings-based token/role storage), `UserRemoteDataSource` (login/signup/profile API) |
| `data.datasource.location` | `GooglePlacesService` (Places autocomplete, details, Distance Matrix) + response DTOs |
| `data.model.user` | `UserDto`, `ApiResponse<T>`, `LoginRequest`, `SignUpRequest`, etc. |
| `data.mapper.user` | `UserMapper` — DTO ↔ Domain conversion |
| `data.repository.user` | `UserRepositoryImpl` |
| `domain.repository.user` | `UserRepository` interface |

## NetworkDataGraph (DI)

The `NetworkDataGraph` class centralizes all data layer wiring. It creates and connects all data sources, mappers, and repositories, exposing them as domain repository interfaces.

**External dependencies** (provided via `create()`):
- `HttpClient`, `Json`, `Settings`, `DispatcherProvider` — core infrastructure
- `DashboardLocalDataSource`, `CostsLocalDataSource`, `TeamLocalDataSource`, `CustomerLocalDataSource` — Room-backed local caches (interfaces here, impls in sharedUI)

**Usage:**
```kotlin
val graph = NetworkDataGraph.create(
    httpClient, json, settings, dispatcherProvider,
    dashboardLocalDS, costsLocalDS, teamLocalDS, customerLocalDS
)
val vehicleRepo = graph.vehicleRepository
```

## Dependencies
- `ijs-core-lib` (api) — transitively exposes `ijs-error-lib`, `ijs-dispatcher-lib`, `ijs-datetime-utils`
- Ktor Client (core, content-negotiation, serialization, logging)
- multiplatform-settings
- Metro (@Inject)

## Platform Source Sets
- `androidMain` — `ktor-client-okhttp`
- `iosMain` — `ktor-client-darwin`
- `jsMain` — `ktor-client-js`
- `wasmJsMain` — `ktor-client-js`

