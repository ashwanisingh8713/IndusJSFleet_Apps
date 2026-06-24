# AGENTS.md — Fleet Apps (root)

Quick orientation for AI agents. The detailed working rules are in
[`CLAUDE.md`](CLAUDE.md); the plain-language overview is in
[`project_context.md`](project_context.md). Each module also has its own
`AGENTS.md` with finer detail.

## What this is

**IndusJS Fleet** — a fleet-management app for transport/logistics businesses,
built with **Kotlin Multiplatform** for **Android, iOS and Web**. It manages
vehicles, drivers, trips, costs, customer payments, team members, vehicle
finance, reports and real-time GPS tracking. Identity, roles and billing live in
the separate **IndusJS-IAM** service; fleet data lives in the **Go backend**.

## Module groups

- **App shells** — `androidApp`, `iosApp`, `webApp` (thin entry points).
- **`sharedUI`** — navigation, DI wiring, theme, root `App`. No screens or
  business logic here.
- **`ijs-*` libraries** — foundation: `ijs-core-lib` (MVI base, constants,
  `PermissionUtils`), `ijs-network-lib` (Ktor client, `ApiConfig`, auth),
  `ijs-error-lib`, `ijs-dispatcher-lib`, `ijs-ui-components-lib`,
  `ijs-datetime-utils`, `ijs-datetime-picker`, `ijs-pdf-report`,
  `ijs-logger-lib`.
- **`screen-*` features** — `vehicle`, `driver`, `trip`, `trip-payment`,
  `payment` (customer payments + subscription billing), `customer`, `team`,
  `report`, `finance`, `user` (auth), `onboarding`, `dashboard`, `alerts`,
  `map`.
- **`locationTracker`** — standalone Android GPS-publisher app (no `sharedUI`
  dependency).

Dependency direction: `app shells → sharedUI → screen-* → ijs-network-lib →
ijs-core-lib`.

## Architecture

Clean Architecture + **MVI** in every feature, with **Metro** DI and
**Navigation 3**. Each feature splits into `domain/` (entities, repository
interfaces, use cases), `data/` (DTOs, mappers, data sources, repository impls)
and `presentation/` (Contract = State/Intent/Effect, ViewModel, Screen, Facade).
A feature's `*FeatureFacade` is its only public API.

## Entity state machines (quick reference)

- **Vehicle:** `inactive → active → on_route → maintenance → damaged → decommissioned`
- **Trip:** `planned → on_route → completed` (or `cancelled`, `failed`, `delayed`)
- **Driver:** `inactive → active → on_route / on_leave / suspended → terminated`

## API & auth essentials

- Backend wiring lives in `ijs-network-lib/.../core/network/ApiConfig.kt` — this
  is the **source of truth**. The base URL **must include the version**
  (currently `/api/v1`). Older docs mentioning `/api/v2` are stale.
- Send `Authorization: Bearer <JWT>`; the token must contain a tenant id
  (`tid`). **401 → re-login; 403 → "not allowed", stay logged in.**
- **Permission-first**: the UI gates actions via `PermissionUtils`
  (`vehicles:create`, `financials:read`, ...), but the backend is the real
  authority. IAM role bundles: `owner` (100), `admin` (50), `user` (10),
  `driver` (app-scoped).
- **Dates per field**: ISO 8601 for trips, `DD-MM-YYYY` for costs/expiry,
  `YYYY-MM` for driver-cost month. Use `FleetDateTime`.

## Build commands

```bash
./gradlew :androidApp:assembleDebug                # Android APK
./gradlew :webApp:jsBrowserDevelopmentRun          # Web (JS) dev server
./gradlew :webApp:wasmJsBrowserDevelopmentRun      # Web (WASM) dev server
./gradlew :locationTracker:assembleDebug           # driver GPS tracker APK
# Fast single-feature check:
./gradlew :screen-vehicle:compileCommonMainKotlinMetadata
```

## Adding a feature (short checklist)

domain entity → repository interface → use cases → DTOs (`@Serializable` +
`@SerialName`, defaults) → mapper → data source → repository impl → Contract →
ViewModel → Screen → DI graph → wire in `DefaultViewModelProvider` → route in
`FleetRoute.kt` → nav entry in `FleetNavigation.kt`.

See [`CLAUDE.md`](CLAUDE.md) for the full set of hard rules.
