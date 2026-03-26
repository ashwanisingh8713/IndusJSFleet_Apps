# `ijs-logger` Module — Kermit File Logger (Kotlin Multiplatform)

## Module Identity

- **Module name:** `ijs-logger`
- **Library:** `co.touchlab.kermit` (Kermit Logger)
- **Language:** Kotlin Multiplatform (KMP)
- **Targets:** Android, iOS, Web (JS/WASM)
- **Architecture:** Self-contained utility module. Consumed by the app layer via DI. No business logic module should depend on it directly.

---

## Requirements

### 1. Log File Naming

- Pattern: `ijs_fleet_ddMMYYYY_HHmm.txt`
- Example: `ijs_fleet_24032026_1430.txt`
- Timestamp captured **once at app launch**, not per log entry.
- Mid-session rollover suffix: `ijs_fleet_ddMMYYYY_HHmm_1.txt`, `_2.txt`, etc.

---

### 2. Log File Location (Per Platform)

| Platform    | Path                                          | Rationale                                                                 |
|-------------|-----------------------------------------------|---------------------------------------------------------------------------|
| **Android** | `sdcard/IndusJS/Fleet/Log/`                   | External storage, accessible for field retrieval                          |
| **iOS**     | `<AppDocumentsDirectory>/IndusJS/Fleet/Log/`  | `NSDocumentDirectory` — survives app updates, iTunes/Finder accessible   |
| **Web**     | `IndexedDB` → database: `IndusJS`, store: `FleetLogs` | No filesystem access in browser; IndexedDB is persistent across sessions |

- Create directory/store if it does not exist at first write.
- **Android:** Handle runtime permissions (`READ/WRITE_EXTERNAL_STORAGE` for API < 29; scoped storage strategy for API ≥ 29).
- **iOS:** No special permissions required for Documents directory.
- **Web:** Request persistent storage via `navigator.storage.persist()` to prevent browser eviction.

---

### 3. File / Entry Rotation Rules

- **New file (or IndexedDB record key) on every app launch** — never append to a prior session's log.
- **Max size: 2 MB per file/entry.**
- If 2 MB is exceeded mid-session, roll over with incremented suffix (`_1`, `_2`, …).
- Old logs are **retained**; no auto-deletion in scope.

---

### 4. Log Entry Format

All platforms write entries in this format:

```
[yyyy-MM-dd HH:mm:ss.SSS] [LEVEL] [ThreadID:thread-id | ThreadName:thread-name] [Tag] Message
```

**Example entries:**

```
[2026-03-24 14:30:05.123] [INFO]  [ThreadID:1 | ThreadName:main] [NetworkTag] Connected to broker
[2026-03-24 14:30:06.456] [DEBUG] [ThreadID:42 | ThreadName:DefaultDispatcher-worker-1] [MqttTag] Subscribing to topic
[2026-03-24 14:30:07.789] [ERROR] [ThreadID:42 | ThreadName:DefaultDispatcher-worker-1] [MqttTag] Publish failed: timeout
```

**Thread ID resolution per platform:**

| Platform    | Thread ID Source                                      | Thread Name Source                        |
|-------------|-------------------------------------------------------|-------------------------------------------|
| **Android** | `android.os.Process.myTid()`                          | `Thread.currentThread().name`             |
| **iOS**     | `pthread_mach_thread_np(pthread_self())`              | `Thread.current.name` or queue label      |
| **Web**     | Fixed `ThreadID:1` (JS is single-threaded)            | `"main"` or Web Worker name if applicable |

Levels written: `VERBOSE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `ASSERT`

---

### 5. Crash / Uncaught Exception Logging

| Platform    | Mechanism                                                                                      |
|-------------|------------------------------------------------------------------------------------------------|
| **Android** | `Thread.UncaughtExceptionHandler` — write full stack trace, then delegate to default handler  |
| **iOS**     | `NSSetUncaughtExceptionHandler` + `SIGABRT`/`SIGILL`/`SIGSEGV` signal handlers — write then re-raise |
| **Web**     | `window.onerror` + `window.onunhandledrejection` — write error + stack, then re-throw         |

Crash log entry format (all platforms):

```
[yyyy-MM-dd HH:mm:ss.SSS] [CRASH] [ThreadID:thread-id | ThreadName:thread-name] [UncaughtException]
<full stack trace or error message>
```

- Must **not** suppress existing crash reporters (Crashlytics, Sentry, etc.).
- Crash handler registered at module `init()` time.

---

### 6. Kermit Integration

- Implement platform-specific `LogWriter` subclasses via Kotlin `expect/actual`:
  - `expect class FileLogWriter` → `actual` per target
- Register `FileLogWriter` alongside `platformLogWriter()` (Logcat on Android, OSLog on iOS, console on Web).
- Logger singleton initialized once; accessible globally after `init()`.

---

### 7. Thread Safety & I/O

- **Android/iOS:** All file writes via `Dispatchers.IO` with a `Mutex` guard — no main-thread blocking.
- **Web:** All IndexedDB operations via `async/await` (JS coroutine dispatcher); writes are inherently single-threaded but must be non-blocking.

---

### 8. Module Public API

`ijs-logger` exposes **only**:

```kotlin
object IjsLogger {
    fun init(context: PlatformContext) // PlatformContext = expect/actual per target
    val logger: Logger
}
```

All internal classes (`FileLogWriter`, `CrashHandler`, `LogFileManager`) are `internal`.

---

## Explicit Exclusions

- No log compression.
- No log upload or remote sink.
- No auto-deletion or retention policy.
- No unit tests in this prompt scope.
- No performance benchmarking.
- No deprecated or backward-incompatible API surface.
- No obfuscation or encryption of log content.
