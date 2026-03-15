# ijs-network-lib — AGENTS.md

## Purpose
HTTP networking infrastructure, authentication management, and user data layer for IndusJS Fleet.

## What's Inside

| Package | Content |
|---------|---------|
| `core.network` | `ApiConfig`, `HttpClientProvider`, `ApiErrorHandler`, `NetworkError` |
| `core.auth` | `AuthenticationManager` (session event bus), `AuthTokenHelper` (require-auth-or-redirect) |
| `data.datasource.user` | `UserLocalDataSource` (Settings-based token/role storage), `UserRemoteDataSource` (login/signup/profile API) |
| `data.model.user` | `UserDto`, `ApiResponse<T>`, `LoginRequest`, `SignUpRequest`, etc. |
| `data.mapper.user` | `UserMapper` — DTO ↔ Domain conversion |
| `data.repository.user` | `UserRepositoryImpl` |
| `domain.repository.user` | `UserRepository` interface |

## Dependencies
- `ijs-core-lib` (api) — transitively exposes `ijs-error-lib`, `ijs-dispatcher-lib`, `ijs-datetime-utils`
- Ktor Client (core, content-negotiation, serialization, logging)
- multiplatform-settings
- Metro (@Inject)

## Platform Source Sets
- `androidMain` — `ktor-client-okhttp`
- `iosMain` — `ktor-client-darwin`

