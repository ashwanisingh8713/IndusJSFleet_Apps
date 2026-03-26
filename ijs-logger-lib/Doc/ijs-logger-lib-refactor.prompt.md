# Prompt: Refactor `ijs-logger` → `ijs-logger-lib` with DI + Screen Module Integration

## Context

You are working on **IndusJsFleet** — a Kotlin Multiplatform (KMP) fleet management app targeting
Android, iOS, JS, and WasmJS.

The existing `ijs-logger` module is a self-contained KMP file logger built on **Kermit**
(`co.touchlab.kermit`). It currently exposes a global singleton (`IjsLogger` object) and is
initialized directly at app-launch without DI wiring.

This prompt drives a **four-part refactor**:

1. Rename `ijs-logger` → `ijs-logger-lib`
2. Replace the global singleton with a **Metro DI-injectable interface**
3. Integrate the logger into every `feat-*` module via DI (no direct `IjsLogger` references
   in feature/screen code)
4. Enforce that **no `feat-*` or `sharedUI` screen code ever calls Android `Log.*` directly**

---

## Existing Module — What Must NOT Change

The following internal implementation files are **complete and correct** — do not modify them:

| File | Role |
|------|------|
| `FileLogWriter.kt` | Kermit `LogWriter` — Mutex-guarded, `Dispatchers.Default`, formats entries |
| `LogFileManager.kt` (+ actuals) | `expect class` — write/close per platform |
| `CrashHandler.kt` (+ actuals) | `expect class` — installs uncaught exception handler per platform |
| `PlatformContext.kt` (+ actuals) | `expect class` — platform context marker |
| `ThreadInfo.kt` (+ actuals) | `expect fun` — `currentThreadId()` / `currentThreadName()` |

Platform actuals: `androidMain`, `iosMain`, `jsMain`, `wasmJsMain` — all correct, no changes.

Log entry format is fixed:
```
[yyyy-MM-dd HH:mm:ss.SSS] [LEVEL  ] [ThreadID:id | ThreadName:name] [Tag] Message
```

File naming pattern: `ijs_fleet_ddMMYYYY_HHmm.txt`, 2 MB max with `_1`, `_2` rollover.

---

## Part 1 — Rename Module

Rename the Gradle module from `ijs-logger` → `ijs-logger-lib` in:
- `settings.gradle.kts` — `include(":ijs-logger-lib")`
- All `build.gradle.kts` that depend on it — update `implementation(project(":ijs-logger-lib"))`
- Module folder name on disk: `ijs-logger-lib/`
- Keep package `com.indusjs.logger` unchanged

---

## Part 2 — DI Interface + Wiring

### 2a. Create `FleetLogger` interface (commonMain)

```
ijs-logger-lib/src/commonMain/kotlin/com/indusjs/logger/
└── FleetLogger.kt        ← NEW: public interface
```

```kotlin
package com.indusjs.logger

/**
 * Public DI-injectable logger interface for IndusJsFleet.
 *
 * All feat-* modules depend on this interface, never on [IjsLogger] directly.
 * The tag identifies the calling module (e.g., "VehicleModule", "TripModule").
 *
 * Usage:
 *   fleetLogger.i("Loaded ${vehicles.size} vehicles")
 *   fleetLogger.e("API error", throwable)
 */
interface FleetLogger {
    val tag: String
    fun v(message: String, throwable: Throwable? = null)
    fun d(message: String, throwable: Throwable? = null)
    fun i(message: String, throwable: Throwable? = null)
    fun w(message: String, throwable: Throwable? = null)
    fun e(message: String, throwable: Throwable? = null)
}
```

### 2b. Create `KermitFleetLogger` implementation (commonMain)

```
ijs-logger-lib/src/commonMain/kotlin/com/indusjs/logger/
└── KermitFleetLogger.kt  ← NEW: internal implementation
```

```kotlin
package com.indusjs.logger

import co.touchlab.kermit.Logger

/**
 * [FleetLogger] backed by the Kermit [Logger] instance from [IjsLogger].
 *
 * Created via Metro DI — one instance per [tag].
 * [tag] must match the module-level constant from each feat-* module's LogTags.
 */
internal class KermitFleetLogger(
    override val tag: String
) : FleetLogger {
    private val logger: Logger get() = IjsLogger.logger

    override fun v(message: String, throwable: Throwable?) =
        logger.v(tag = tag, throwable = throwable) { message }

    override fun d(message: String, throwable: Throwable?) =
        logger.d(tag = tag, throwable = throwable) { message }

    override fun i(message: String, throwable: Throwable?) =
        logger.i(tag = tag, throwable = throwable) { message }

    override fun w(message: String, throwable: Throwable?) =
        logger.w(tag = tag, throwable = throwable) { message }

    override fun e(message: String, throwable: Throwable?) =
        logger.e(tag = tag, throwable = throwable) { message }
}
```

### 2c. Factory function (commonMain)

Add to `IjsLogger.kt` — keep existing `init()` and `logger` property unchanged:

```kotlin
/**
 * Creates a [FleetLogger] bound to the given [tag].
 * Call after [init] — typically from a Metro DI @Provides function.
 * @throws IllegalStateException if [init] was not called.
 */
fun forTag(tag: String): FleetLogger = KermitFleetLogger(tag)
```

### 2d. Metro DI provider in `sharedUI` (NEW file)

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/
└── LoggerModule.kt       ← NEW
```

```kotlin
package com.indusjs.fleet.di

import com.indusjs.logger.FleetLogger
import com.indusjs.logger.IjsLogger
import metro.Module
import metro.Provides
import metro.Named

/**
 * Metro DI module that provides [FleetLogger] instances for each feat-* module.
 *
 * Each provision is @Named with the module's LogTag constant so that
 * the correct tag appears in every log entry and log file.
 *
 * Add a new @Provides entry for each new feat-* module.
 */
@Module
object LoggerModule {

    @Provides @Named("VehicleModule")
    fun provideVehicleLogger(): FleetLogger =
        IjsLogger.forTag(VehicleLogTag)

    @Provides @Named("DriverModule")
    fun provideDriverLogger(): FleetLogger =
        IjsLogger.forTag(DriverLogTag)

    @Provides @Named("TripModule")
    fun provideTripLogger(): FleetLogger =
        IjsLogger.forTag(TripLogTag)

    @Provides @Named("CustomerModule")
    fun provideCustomerLogger(): FleetLogger =
        IjsLogger.forTag(CustomerLogTag)

    @Provides @Named("PaymentModule")
    fun providePaymentLogger(): FleetLogger =
        IjsLogger.forTag(PaymentLogTag)

    @Provides @Named("TeamModule")
    fun provideTeamLogger(): FleetLogger =
        IjsLogger.forTag(TeamLogTag)

    @Provides @Named("ReportModule")
    fun provideReportLogger(): FleetLogger =
        IjsLogger.forTag(ReportLogTag)

    @Provides @Named("FinanceModule")
    fun provideFinanceLogger(): FleetLogger =
        IjsLogger.forTag(FinanceLogTag)

    @Provides @Named("DashboardModule")
    fun provideDashboardLogger(): FleetLogger =
        IjsLogger.forTag(DashboardLogTag)

    @Provides @Named("MapModule")
    fun provideMapLogger(): FleetLogger =
        IjsLogger.forTag(MapLogTag)

    @Provides @Named("AlertsModule")
    fun provideAlertsLogger(): FleetLogger =
        IjsLogger.forTag(AlertsLogTag)

    @Provides @Named("UserModule")
    fun provideUserLogger(): FleetLogger =
        IjsLogger.forTag(UserLogTag)

    @Provides @Named("OnboardingModule")
    fun provideOnboardingLogger(): FleetLogger =
        IjsLogger.forTag(OnboardingLogTag)

    @Provides @Named("NetworkLib")
    fun provideNetworkLogger(): FleetLogger =
        IjsLogger.forTag(NetworkLogTag)
}
```

Wire `LoggerModule` into `RootGraph.kt` — add it to the `@Component` includes list.

---

## Part 3 — Log Tags per `feat-*` Module

### 3a. Create `LogTags.kt` in each `feat-*` module (commonMain)

Place in: `feat-<name>/src/commonMain/kotlin/com/indusjs/fleet/<name>/`

One file per module. All values are `const val String`.

| Module | File | Tag constant name | Tag value |
|--------|------|-------------------|-----------|
| `feat-vehicle` | `VehicleLogTag.kt` | `VehicleLogTag` | `"VehicleModule"` |
| `feat-driver` | `DriverLogTag.kt` | `DriverLogTag` | `"DriverModule"` |
| `feat-trip` | `TripLogTag.kt` | `TripLogTag` | `"TripModule"` |
| `feat-customer` | `CustomerLogTag.kt` | `CustomerLogTag` | `"CustomerModule"` |
| `feat-payment` | `PaymentLogTag.kt` | `PaymentLogTag` | `"PaymentModule"` |
| `feat-team` | `TeamLogTag.kt` | `TeamLogTag` | `"TeamModule"` |
| `feat-report` | `ReportLogTag.kt` | `ReportLogTag` | `"ReportModule"` |
| `feat-finance` | `FinanceLogTag.kt` | `FinanceLogTag` | `"FinanceModule"` |
| `feat-dashboard` | `DashboardLogTag.kt` | `DashboardLogTag` | `"DashboardModule"` |
| `feat-map` | `MapLogTag.kt` | `MapLogTag` | `"MapModule"` |
| `feat-alerts` | `AlertsLogTag.kt` | `AlertsLogTag` | `"AlertsModule"` |
| `feat-user` | `UserLogTag.kt` | `UserLogTag` | `"UserModule"` |
| `feat-onboarding` | `OnboardingLogTag.kt` | `OnboardingLogTag` | `"OnboardingModule"` |
| `ijs-network-lib` | `NetworkLogTag.kt` | `NetworkLogTag` | `"NetworkLib"` |

Example (`feat-vehicle`):
```kotlin
package com.indusjs.fleet.vehicle

const val VehicleLogTag = "VehicleModule"
```

### 3b. Inject `FleetLogger` into ViewModels

Each ViewModel that requires logging receives `FleetLogger` via constructor injection.
The `@Named` qualifier must match the module's tag constant.

Example — `VehicleListViewModel`:
```kotlin
class VehicleListViewModel @Inject constructor(
    private val getVehiclesUseCase: GetVehiclesUseCase,
    @Named("VehicleModule") private val logger: FleetLogger
) : MviViewModel<VehicleListState, VehicleListIntent, VehicleListEffect>() {

    override fun handleIntent(intent: VehicleListIntent) {
        when (intent) {
            is VehicleListIntent.LoadVehicles -> loadVehicles()
        }
    }

    private fun loadVehicles() {
        logger.d("LoadVehicles intent received")
        viewModelScope.launch {
            getVehiclesUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> logger.d("Vehicles loading...")
                    is Result.Success -> logger.i("Loaded ${result.data.size} vehicles")
                    is Result.Error   -> logger.e("Failed to load vehicles", result.exception)
                }
            }
        }
    }
}
```

Apply the same pattern to **all ViewModels** across all `feat-*` modules.

---

## Part 4 — Enforcement: No `android.util.Log` in feat-* or sharedUI

### 4a. Lint rule (`androidApp/lint.xml` or root `lint.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <!-- Ban android.util.Log in all feat-* and sharedUI source sets -->
    <issue id="LogNotTimber" severity="error" />
</lint>
```

If `LogNotTimber` is not available (no Timber), add a custom Lint check or use a **detekt rule**.

### 4b. Detekt rule (`detekt-config.yml`)

```yaml
naming:
  active: true

custom:
  ForbidAndroidLog:
    active: true
    reason: >
      Use injected FleetLogger (com.indusjs.logger.FleetLogger) instead of android.util.Log.
      android.util.Log is forbidden in feat-* and sharedUI modules.
    includes:
      - '**/feat-*/**'
      - '**/sharedUI/**'
      - '**/ijs-network-lib/**'
```

### 4c. Grep check in CI (`scripts/check_no_android_log.sh`)

```bash
#!/bin/bash
# Fails CI if android.util.Log is used in feat-* / sharedUI / ijs-network-lib
RESULT=$(grep -rn "android\.util\.Log\." \
  feat-vehicle feat-driver feat-trip feat-customer feat-payment \
  feat-team feat-report feat-finance feat-dashboard feat-map \
  feat-alerts feat-user feat-onboarding sharedUI ijs-network-lib \
  --include="*.kt" 2>/dev/null)

if [ -n "$RESULT" ]; then
  echo "❌ Forbidden android.util.Log usage found:"
  echo "$RESULT"
  exit 1
fi
echo "✅ No android.util.Log usage found."
```

---

## Summary — Files to Create / Modify

### New files in `ijs-logger-lib`
| File | Action |
|------|--------|
| `commonMain/.../FleetLogger.kt` | CREATE — public DI interface |
| `commonMain/.../KermitFleetLogger.kt` | CREATE — internal implementation |
| `IjsLogger.kt` | MODIFY — add `forTag(tag): FleetLogger` factory only |

### New files in `sharedUI`
| File | Action |
|------|--------|
| `di/LoggerModule.kt` | CREATE — Metro @Module with @Named provisions |
| `di/RootGraph.kt` | MODIFY — include `LoggerModule` |

### New files in each `feat-*` module
| File | Action |
|------|--------|
| `<module>/src/commonMain/.../LogTag.kt` | CREATE — `const val` tag string |
| Each ViewModel | MODIFY — add `@Named(...) logger: FleetLogger` constructor param |

### Build files
| File | Action |
|------|--------|
| `settings.gradle.kts` | MODIFY — rename `:ijs-logger` → `:ijs-logger-lib` |
| All `build.gradle.kts` with logger dep | MODIFY — update project reference |

### Enforcement
| File | Action |
|------|--------|
| `lint.xml` | CREATE/MODIFY — ban `android.util.Log` |
| `detekt-config.yml` | MODIFY — add `ForbidAndroidLog` rule |
| `scripts/check_no_android_log.sh` | CREATE — CI grep check |

---

## Constraints

- Do **not** modify any `actual` platform implementations (`androidMain`, `iosMain`, `jsMain`, `wasmJsMain`)
- Do **not** change the log entry format, file naming pattern, or rotation logic
- Do **not** add `ijs-logger-lib` as a direct dependency of any `feat-*` module in `build.gradle.kts` — `feat-*` modules depend only on `FleetLogger` interface; DI wiring lives in `sharedUI`
- `IjsLogger.init()` call sites remain unchanged (app-layer only)
- `KermitFleetLogger` stays `internal` — never expose Kermit types through `FleetLogger`
- `FleetLogger` must be usable in `commonMain` — no platform imports
- `Dispatchers.IO` must **not** be used anywhere in this module (use `Dispatchers.Default`)
