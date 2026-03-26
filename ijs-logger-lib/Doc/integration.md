# ijs-logger — Integration Guide

## Adding ijs-logger to a Module

### build.gradle.kts
```kotlin
commonMain.dependencies {
    implementation(project(":ijs-logger"))
}
```

### Initialization

Logger **must** be initialized once at app startup before any logging calls.

#### Android — `FleetApplication.kt`
```kotlin
import com.indusjs.logger.IjsLogger
import com.indusjs.logger.PlatformContext

class FleetApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        IjsLogger.init(PlatformContext(this))  // before Crashlytics
        // ...
    }
}
```

#### iOS — `sharedUI/src/iosMain/kotlin/main.kt`
```kotlin
import com.indusjs.logger.IjsLogger
import com.indusjs.logger.PlatformContext

fun MainViewController(): UIViewController {
    IjsLogger.init(PlatformContext())
    return ComposeUIViewController { App(...) }
}
```

#### JS / WasmJS — via `initPlatformLogger()` in App.kt
```kotlin
// Automatically called from App() composable
// sharedUI/src/jsMain/.../LoggerInit.js.kt calls IjsLogger.init(PlatformContext())
```

---

## Usage — Logging

```kotlin
import com.indusjs.logger.IjsLogger

// Simple log
IjsLogger.logger.i(tag = "NetworkTag") { "Connected to server" }

// With throwable
IjsLogger.logger.e(tag = "AuthTag", throwable = exception) { "Token expired" }

// Debug
IjsLogger.logger.d(tag = "TripVM") { "Loading trip $tripId" }
```

### Severity Levels
`VERBOSE` → `DEBUG` → `INFO` → `WARN` → `ERROR` → `ASSERT`

All levels are written to both the platform log (Logcat/OSLog/console) and the file log.

---

## Log File Locations for Debugging

### Android
```bash
# API < 29
adb shell ls /sdcard/IndusJS/Fleet/Log/

# API >= 29
adb shell ls /storage/emulated/0/Android/data/com.indusjs.fleet.androidApp/files/IndusJS/Fleet/Log/

# Pull log file
adb pull /storage/emulated/0/Android/data/com.indusjs.fleet.androidApp/files/IndusJS/Fleet/Log/ijs_fleet_25032026_1430.txt
```

### iOS
```
# Via Xcode → Devices & Simulators → Download Container
# Path inside container: Documents/IndusJS/Fleet/Log/
```

### Web (JS/WasmJS)
```
# Browser DevTools → Application → IndexedDB → IndusJS → FleetLogs
# Each entry key = filename, value = full log text
```

---

## Current Wiring in the Project

| Module | Dependency | Init Location |
|--------|-----------|---------------|
| `androidApp` | `implementation(project(":ijs-logger"))` | `FleetApplication.onCreate()` |
| `sharedUI` | `implementation(project(":ijs-logger"))` | `App()` → `initPlatformLogger()` |
| `settings.gradle.kts` | `include(":ijs-logger")` | — |

---

## Limitations

- No log compression
- No log upload or remote sink
- No auto-deletion or retention policy
- No encryption of log content
- WasmJS crash handler only writes to `console.error` (not IndexedDB)
- iOS thread ID uses `NSThread.hash` proxy (not Mach thread port)

