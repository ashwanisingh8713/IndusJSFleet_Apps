# Fleet Apps — Project Context

> Plain-language guide to the client apps. For the whole-system picture see
> [`../PROJECT_CONTEXT.md`](../PROJECT_CONTEXT.md).

## What this is

This is the **app** that fleet owners and their teams actually use. It is built
with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, which means
**one shared codebase** produces apps for several platforms:

- **Android** phone/tablet app
- **iOS** iPhone/iPad app
- **Web** (runs in a browser — both JS and WasmJS builds)

There is also a separate, standalone Android app called **`locationTracker`**
that runs in the background to send the vehicle's GPS location.

The apps talk to the **fleet backend** ([`../IndusJSFleet_GoLang_Backend`](../IndusJSFleet_GoLang_Backend))
over HTTPS, and log in through the **IAM** service.

## Technology

- **Kotlin** **2.3.0**, **Compose Multiplatform** **1.10.x**, Material 3.
- **Ktor client 3.3.3** for talking to the backend.
- **Metro** for dependency injection (not Koin).
- **Navigation 3** with type-safe routes.
- **Room** for local storage/caching, **multiplatform-settings** for small
  settings, **Kermit** for logging.
- **Razorpay** Android SDK for subscription payments.
- Build: Gradle, Android Gradle Plugin 8.12.3, JDK 17, `minSdk 23`,
  `compileSdk`/`targetSdk 36`.

> Note: Currently this project is under development, so do not support for backward compatibility, so it is always a fresh.

## How the modules fit together

The project is a set of Gradle modules in three groups. Dependencies flow:
**app shells → `sharedUI` → `screen-*` features → `ijs-network-lib` →
`ijs-core-lib`**.

### Group A — app shells (thin entry points)
`androidApp`, `iosApp`, `webApp`. These are small; they start the app and hand
off to `sharedUI`.

`sharedUI` is the orchestrator that ties everything together: navigation,
dependency-injection wiring, theming and the root `App`. **No screens or
business logic live in `sharedUI`** — it just assembles the feature modules.

### Group B — `ijs-*` shared libraries (the foundation)
- `ijs-core-lib` — the **MVI** base classes, shared models, constants,
  validation, and client-side permission helpers.
- `ijs-network-lib` — the Ktor HTTP client, the `ApiConfig` (all endpoint URLs),
  and auth handling (token, 401 logout, tenant-id from the JWT). **This is the
  source of truth for how the app talks to the backend.**
- `ijs-error-lib` — the `Result` type and error handling.
- `ijs-dispatcher-lib` — coroutine dispatcher abstraction (never use
  `Dispatchers.IO` directly).
- `ijs-ui-components-lib` — reusable `Fleet*` Compose components, theme, icons,
  and shared strings (English + Hindi).
- `ijs-datetime-utils` / `ijs-datetime-picker` — date/time helpers and picker.
- `ijs-pdf-report` — turns reports into PDFs on each platform.
- `ijs-logger-lib` — the Kermit-based logger.

### Group C — `screen-*` feature modules (the actual features)
Each one is a self-contained feature: `screen-vehicle`, `screen-driver`,
`screen-trip`, `screen-trip-payment`, `screen-payment` (customer payments +
subscription billing), `screen-customer`, `screen-team`, `screen-report`,
`screen-finance`, `screen-user` (auth/profile), `screen-onboarding`,
`screen-dashboard`, `screen-alerts`, `screen-map` (live tracking).

## Architecture pattern

Each feature module follows **Clean Architecture + MVI**:

- `domain/` — entities, repository interfaces, use cases.
- `data/` — DTOs, mappers, data sources, repository implementations.
- `presentation/` — a `Contract` (State / Intent / Effect), a `ViewModel`
  (extends `MviViewModel`), the Compose `Screen`, and a `Facade` that is the
  module's only public entry point.

Key rules: state is immutable and updated with `updateState { copy(...) }`;
one-time events are `Effect`s, but **navigation uses lambda callbacks, not
effects**; feature modules never import the navigation routes directly.

## How the app talks to the backend

- **Transport:** Ktor client, configured in `ijs-network-lib`; base URL must
  include the version (`.../api/v1`).
- **Auth:** every request sends `Authorization: Bearer <JWT>`. The token comes
  from IAM and must contain a tenant id (`tid`). A **401** means the session
  expired → go to Login. A **403** means "not allowed" → show a message, do
  **not** log out.
- **Permissions:** the UI hides things the user can't do (using
  `PermissionUtils`), but the **backend is the real authority**.
- **Dates:** different fields expect different formats (ISO 8601 for trips,
  `DD-MM-YYYY` for costs/expiry, `YYYY-MM` for driver-cost month). Getting this
  wrong is a common bug.
- **Live location:** handled outside REST — the driver's `locationTracker`
  publishes GPS over MQTT and `screen-map` shows it.

## Build / run (quick reference)

```bash
# Android debug APK
./gradlew :androidApp:assembleDebug

# Web dev server (JS)
./gradlew :webApp:jsBrowserDevelopmentRun

# Driver GPS tracker APK
./gradlew :locationTracker:assembleDebug

# Fast check while developing a single feature
./gradlew :screen-vehicle:compileCommonMainKotlinMetadata
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode (run `./gradlew iosPodInstall` once
after cloning). You need a `local.properties` with `sdk.dir` and a
`GOOGLE_PLACES_API_KEY` (never commit secrets).

## Conventions to keep in mind

- Max ~500 lines per class file; no backward-compat shims (early dev).
- Never set `compileSdk` / `minSdk` / `jvmTarget` per module — the convention
  files under `gradle/` do that.
- Every DTO field needs `@SerialName` and a default value (so a missing field
  doesn't blank the screen).
- Reuse `Fleet*` components and `MaterialTheme.colorScheme.*` (no hardcoded
  colours); every screen handles loading / error (with retry) / empty states.
- All user-facing strings exist in English and Hindi.

See also: [`REQUIREMENTS.md`](REQUIREMENTS.md), [`CLAUDE.md`](CLAUDE.md) and
[`AGENTS.md`](AGENTS.md) in this folder.
