# ijs-core-lib — AGENTS.md

## Purpose
Shared foundation module for IndusJS Fleet. Contains **base contracts, MVI pattern, status constants, utilities, and user domain entities** used across all feature modules.

## What's Inside

| Package | Content |
|---------|---------|
| `core.mvi` | `UiState`, `UiIntent`, `UiEffect`, `MviViewModel`, `HandleEffects`, `collectState` |
| `core.constants` | `StatusConstants` — Vehicle/Driver/Trip state machines, transitions, color schemes |
| `core.error` | `FleetErrorContext` — Screen-specific error messages |
| `core.util` | `FormatUtils`, `ValidationUtils`, `CostTypeUtils`, `TimeUtils`, `PhoneCallUtil`, `PermissionUtils` |
| `domain.entity` | `Entity` marker interface |
| `domain.entity.user` | `UserRole`, `User`, `UserProfile`, `AuthResult` |
| `domain.repository` | `Repository` marker interface |
| `domain.usecase` | `UseCase`, `UseCaseWithParams`, `SuspendUseCase`, `SuspendUseCaseWithParams` |
| `data.datasource` | `LocalDataSource`, `RemoteDataSource` marker interfaces |
| `data.model` | `Dto`, `DbEntity` marker interfaces |
| `data.mapper` | `Mapper<D,E>` interface + list extensions |

## Dependencies
- `ijs-error-lib` (api) — `Result<T>`, exception hierarchy
- `ijs-dispatcher-lib` (api) — `DispatcherProvider`
- `ijs-datetime-utils` (api) — `FleetDateTime`
- Compose Runtime, UI, Material3, Lifecycle ViewModel/Runtime

## Platform Source Sets
- `androidMain` — `TimeUtils.android.kt`, `PhoneCallUtil.android.kt`
- `iosMain` — `TimeUtils.ios.kt`, `PhoneCallUtil.ios.kt`
- `jsMain` — `TimeUtils.js.kt`, `PhoneCallUtil.js.kt`
- `wasmJsMain` — `TimeUtils.wasmJs.kt`, `PhoneCallUtil.wasmJs.kt`

