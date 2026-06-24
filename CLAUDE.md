# CLAUDE.md — Fleet Apps (Kotlin Multiplatform)

Guide for an AI agent working in the client apps. Read
[`project_context.md`](project_context.md) first, and the monorepo guide at
[`../CLAUDE.md`](../CLAUDE.md). Per-module `AGENTS.md` files give finer detail
inside each module.

## Mental model

One Kotlin Multiplatform codebase → Android, iOS, Web apps. The apps are a
**client**: business truth lives in the fleet backend, and identity lives in
IAM. Keep features thin and well-structured; don't reinvent backend logic on the
client.

## Module layout

- **App shells** (`androidApp`, `iosApp`, `webApp`) — thin entry points.
- **`sharedUI`** — orchestrator: navigation, DI wiring, theme, root `App`.
  **Never put screens or business logic here.**
- **`ijs-*` libraries** — foundation (core/MVI, network, errors, dispatchers, UI
  components, datetime, pdf, logger). `ijs-network-lib` is the source of truth
  for backend wiring.
- **`screen-*` modules** — the features. Each is Clean Architecture + MVI with
  `domain/`, `data/`, `presentation/`, exposing a single `Facade`.

## Hard rules (don't break these)

- **MVI discipline:** ViewModels extend `MviViewModel<State, Intent, Effect>`;
  state is immutable, updated only via `updateState { copy(...) }`; never
  `mutableStateOf` in a ViewModel. Effects are one-time events; **navigation is
  done through lambda callbacks, not effects**.
- **Facade isolation:** a feature's `*FeatureFacade` is its only public API;
  feature modules never import `FleetRoute`. Cross-feature data goes through
  adapter interfaces.
- **Network contract:** base URL must include `/api/v1`; send
  `Authorization: Bearer <JWT>`; the token must carry a tenant id (`tid`). **401
  → re-login; 403 → show "not allowed", stay logged in.** Don't change this in
  `ijs-network-lib`'s auth handling without care.
- **DTOs:** every field needs `@SerialName` **and** a default value, or a missing
  field will blank the screen. Repositories propagate failures as `Result.Error`,
  never as empty lists.
- **Dates:** use the right format per field — ISO 8601 (trips), `DD-MM-YYYY`
  (costs/expiry), `YYYY-MM` (driver-cost month). Use `FleetDateTime` helpers.
- **Dispatchers:** use `DispatcherProvider`, never `Dispatchers.IO` directly.
- **Build config:** never set `compileSdk` / `minSdk` / `jvmTarget` per module —
  the `gradle/` convention files own that.
- **UI:** reuse `Fleet*` components and `MaterialTheme.colorScheme.*` (no
  hardcoded colours); handle loading / error (retry) / empty in every screen;
  strings in English **and** Hindi.
- **File size:** keep classes under ~500 lines; no backward-compat shims.

## Permissions

The UI gates actions with `PermissionUtils` (client-side, for UX only). The
**backend is the authority** — don't treat client-side checks as security.

## Build / run / fast checks

```bash
# Fastest feedback while editing a feature (compile just that module's common code)
./gradlew :screen-vehicle:compileCommonMainKotlinMetadata

./gradlew :androidApp:assembleDebug                 # Android APK
./gradlew :webApp:jsBrowserDevelopmentRun           # Web (JS) dev server
./gradlew :locationTracker:assembleDebug            # Vehicle GPS tracker APK
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode (run `./gradlew iosPodInstall` once
after cloning). Needs `local.properties` with `sdk.dir` and
`GOOGLE_PLACES_API_KEY`.

## Where to look

- Backend wiring & endpoints: `ijs-network-lib/.../core/network/ApiConfig.kt`
- Auth (401/tenant id): `ijs-network-lib/.../core/auth/`
- MVI base + permissions: `ijs-core-lib/.../core/`
- Navigation & DI: `sharedUI/.../navigation/` and `sharedUI/.../di/`
- Gradle conventions & versions: `gradle/` and `gradle/libs.versions.toml`
- Each module's own `AGENTS.md` for module-specific detail.
