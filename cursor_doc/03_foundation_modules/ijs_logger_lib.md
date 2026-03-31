# ijs-logger-lib — Logging & Crash Handling

**Namespace:** `com.indusjs.logger`
**Depends on:** `ijs-core-lib` (api), Kermit, kotlinx-datetime

## File Tree

```
ijs-logger-lib/src/
├── commonMain/kotlin/com/indusjs/logger/
│   ├── CrashHandler.kt           # expect class
│   ├── FileLogWriter.kt          # Kermit LogWriter → file
│   ├── IjsLogger.kt              # Main entry point (object)
│   ├── KermitFleetLogger.kt      # FleetLogger impl using Kermit
│   ├── LogFileManager.kt         # expect class (file I/O)
│   ├── PlatformContext.kt        # expect class (Android Context wrapper)
│   └── ThreadInfo.kt             # expect fun (thread ID/name)
├── androidMain/
│   ├── CrashHandler.android.kt   # UncaughtExceptionHandler
│   ├── LogFileManager.android.kt # External files dir + BufferedWriter
│   ├── PlatformContext.android.kt # Wraps android.content.Context
│   └── ThreadInfo.android.kt     # Process.myTid + Thread.name
├── iosMain/
│   ├── CrashHandler.ios.kt       # NSSetUncaughtExceptionHandler
│   ├── LogFileManager.ios.kt     # Documents/IndusJS/Fleet/Log
│   ├── PlatformContext.ios.kt    # Marker class
│   └── ThreadInfo.ios.kt         # NSThread
├── jsMain/                        # window.onerror / IndexedDB
└── wasmJsMain/                    # Similar to JS
```

## Public API

### IjsLogger (object)

```kotlin
object IjsLogger {
    fun init(context: PlatformContext)     // Idempotent; sets up Kermit + file + crash handler
    val logger: Logger                      // Kermit Logger instance (throws if not initialized)
    fun createFleetLogger(): FleetLogger   // Returns KermitFleetLogger
}
```

### PlatformContext

```kotlin
expect class PlatformContext
// Android actual: PlatformContext(context: android.content.Context)
// iOS/JS/WasmJS actual: PlatformContext() (marker)
```

### FleetLogger Interface (in ijs-core-lib)

```kotlin
interface FleetLogger {
    fun d(tag: String, message: String)    // Debug
    fun i(tag: String, message: String)    // Info
    fun w(tag: String, message: String)    // Warning
    fun e(tag: String, message: String, throwable: Throwable? = null)  // Error
}
```

## Platform Behavior

| Behavior | Android | iOS | JS | WasmJS |
|----------|---------|-----|----|--------|
| Log files | `getExternalFilesDir/Logs` + 2MB rollover | `Documents/IndusJS/Fleet/Log` | IndexedDB `IndusJS/FleetLogs` | Same as JS |
| Crash handler | `Thread.setDefaultUncaughtExceptionHandler` | `NSSetUncaughtExceptionHandler` | `window.onerror` + `onunhandledrejection` | `console.error` |
| Thread info | `Process.myTid`, `Thread.currentThread().name` | `NSThread` hash + name | id=1, name="main" | Same as JS |

## Initialization

- **Android:** `FleetApplication.onCreate()` → `IjsLogger.init(PlatformContext(this))`
- **iOS:** `sharedUI/iosMain/main.kt` → `IjsLogger.init(PlatformContext())`
- **JS/WasmJS:** `LoggerInit.*.kt` in sharedUI platform source sets

## Usage

```kotlin
// In DefaultViewModelProvider
val fleetLogger = IjsLogger.createFleetLogger()

// In feature code (injected)
logger.d("VehicleVM", "Loading vehicles...")
logger.e("TripRepo", "API call failed", exception)
```
