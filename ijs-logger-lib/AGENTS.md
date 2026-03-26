# AGENTS.md — ijs-logger-lib

## Purpose
Self-contained Kotlin Multiplatform file logger built on **Kermit** (`co.touchlab.kermit`). Persists structured log entries to platform-specific storage and installs uncaught exception handlers. Exposes `KermitFleetLogger` — the single implementation of `FleetLogger` (defined in `ijs-core-lib`) — used throughout the app via DI.

**Package:** `com.indusjs.logger`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS (browser), WasmJS (browser)

---

## Source Tree

```
src/
├── commonMain/kotlin/com/indusjs/logger/
│   ├── IjsLogger.kt          # ★ Public API: init() + logger + createFleetLogger()
│   ├── KermitFleetLogger.kt   # ★ FleetLogger implementation (tag-per-call, delegates to Kermit)
│   ├── FileLogWriter.kt      # Kermit LogWriter → file/IndexedDB
│   ├── LogFileManager.kt     # expect: write()/close()
│   ├── CrashHandler.kt       # expect: install()
│   ├── PlatformContext.kt     # expect: platform context marker
│   └── ThreadInfo.kt         # expect: currentThreadId()/currentThreadName()
├── androidMain/               # actual: BufferedWriter → sdcard/app-external
├── iosMain/                   # actual: NSFileHandle → Documents dir
├── jsMain/                    # actual: IndexedDB via dynamic API
└── wasmJsMain/                # actual: IndexedDB via js() interop
```

---

## Public API

```kotlin
object IjsLogger {
    fun init(context: PlatformContext)          // Call once at app launch
    val logger: Logger                          // Kermit Logger (throws if init not called)
    fun createFleetLogger(): FleetLogger        // Factory for the shared FleetLogger instance
}
```

### FleetLogger Integration

The `FleetLogger` interface lives in `ijs-core-lib` (`com.indusjs.fleet.core.logger.FleetLogger`) so all modules can depend on it without depending on Kermit directly. The implementation `KermitFleetLogger` lives here and delegates to `IjsLogger.logger`:

```kotlin
// ijs-core-lib — interface
interface FleetLogger {
    fun v(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

// ijs-logger-lib — implementation
internal class KermitFleetLogger : FleetLogger {
    override fun d(tag: String, message: String, throwable: Throwable?) {
        IjsLogger.logger.d(tag = tag, throwable = throwable) { message }
    }
    // ... etc
}
```

### Usage Pattern (All Modules)

Each module has a `LogTags.kt` with `const val TAG_XXX = "ClassName"` constants. `FleetLogger` is injected via constructor:

```kotlin
@Inject
class SomeRepository(
    private val logger: FleetLogger
) {
    fun doWork() {
        logger.d(TAG_SOME_REPO, "Doing work...")
    }
}
```

A **single** `FleetLogger` instance is created in `DefaultViewModelProvider` via `IjsLogger.createFleetLogger()` and passed to all repositories, data sources, and ViewModels.

---

## Platform Storage

| Platform | Path | Mechanism |
|----------|------|-----------|
| Android (API < 29) | `sdcard/IndusJS/Fleet/Log/` | BufferedWriter + legacy external storage |
| Android (API ≥ 29) | `<app-external>/IndusJS/Fleet/Log/` | BufferedWriter + getExternalFilesDir |
| iOS | `<Documents>/IndusJS/Fleet/Log/` | NSFileHandle append-writes |
| JS | IndexedDB `IndusJS` / `FleetLogs` store | dynamic API + IndexedDB |
| WasmJS | IndexedDB `IndusJS` / `FleetLogs` store | js() interop + IndexedDB |

---

## Log Entry Format

```
[yyyy-MM-dd HH:mm:ss.SSS] [LEVEL  ] [ThreadID:id | ThreadName:name] [Tag] Message
```

## Crash Entry Format

```
[yyyy-MM-dd HH:mm:ss.SSS] [CRASH  ] [ThreadID:id | ThreadName:name] [UncaughtException]
<stack trace>
```

---

## File Naming & Rotation

- Pattern: `ijs_fleet_ddMMYYYY_HHmm.txt`
- Session timestamp captured once at `init()` — same file for entire session
- **2 MB max** per file → rolls over with suffix `_1`, `_2`, …
- Old logs **retained** (no auto-deletion)

---

## Initialization (How It's Wired)

| Platform | Where | Code |
|----------|-------|------|
| Android | `FleetApplication.onCreate()` | `IjsLogger.init(PlatformContext(this))` |
| iOS | `MainViewController()` in sharedUI/iosMain | `IjsLogger.init(PlatformContext())` |
| JS/WasmJS | `App()` via `initPlatformLogger()` | `IjsLogger.init(PlatformContext())` |

The `initPlatformLogger()` expect/actual in `sharedUI/core/logger/` is a no-op on Android/iOS (already initialized earlier) and performs init on JS/WasmJS.

---

## Key Design Decisions

1. **`Dispatchers.Default` not `Dispatchers.IO`** — IO is unavailable on JS/WasmJS. Mutex serializes writes.
2. **Android PlatformContext is a wrapper, not typealias** — `Context` is abstract; can't typealias to a `final expect class`.
3. **iOS CrashHandler uses `staticCFunction` + global** — `NSSetUncaughtExceptionHandler` requires a C function pointer; can't capture Kotlin state.
4. **JS uses `dynamic` API, not raw `js()`** — raw `js()` blocks can't access Kotlin local variables; dynamic dispatch captures them via lambda closures.
5. **WasmJS CrashHandler writes to `console.error`** — WasmJS `js()` blocks can't call back into Kotlin code for IndexedDB writes during crashes.

---

## Dependencies

```
kermit (co.touchlab:kermit)       — LogWriter base class + platformLogWriter()
kotlinx-coroutines-core           — Mutex + CoroutineScope for async writes
kotlinx-datetime                  — Timestamp formatting
kotlinx-coroutines-android        — Android-only (androidMain)
```

No dependency on `ijs-core-lib`, `ijs-dispatcher-lib`, or any feature module.
