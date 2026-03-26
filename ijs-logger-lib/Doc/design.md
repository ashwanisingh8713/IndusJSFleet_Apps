# ijs-logger — Design Document

## Overview

`ijs-logger` is a **self-contained KMP utility module** that provides file-based logging via Kermit's `LogWriter` extension. It writes structured log entries to platform-specific persistent storage and installs crash handlers for uncaught exceptions.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        commonMain                            │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐ │
│  │  IjsLogger   │→ │FileLogWriter │→ │ LogFileManager     │ │
│  │  (public)     │  │ (internal)   │  │ (expect/actual)    │ │
│  └──────┬───────┘  └──────────────┘  └────────────────────┘ │
│         │           ┌──────────────┐  ┌────────────────────┐ │
│         └─────────→ │ CrashHandler │  │ ThreadInfo         │ │
│                     │ (expect/actual)│ │ (expect/actual)    │ │
│                     └──────────────┘  └────────────────────┘ │
│                     ┌──────────────┐                         │
│                     │PlatformContext│                         │
│                     │(expect/actual)│                         │
│                     └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────────┐
│ androidMain│ │  iosMain  │ │  jsMain   │ │  wasmJsMain   │
│            │ │           │ │           │ │               │
│BufferedWriter│NSFileHandle│ IndexedDB  │ │  IndexedDB    │
│sdcard/app-ext│Documents/ │ (dynamic)  │ │  (js() interop)│
│            │ │           │ │           │ │               │
│Thread.set..│ │staticCFn +│ │window.on..│ │ window.on..   │
│ExcHandler  │ │NSSetUnc.. │ │(dynamic)  │ │ (console.err) │
└───────────┘ └───────────┘ └───────────┘ └───────────────┘
```

---

## Dependent Module Flow

```
androidApp ──→ ijs-logger     (IjsLogger.init in FleetApplication)
sharedUI ────→ ijs-logger     (initPlatformLogger() expect/actual)
    ├── iosMain                (IjsLogger.init in MainViewController)
    ├── jsMain                 (IjsLogger.init via initPlatformLogger)
    └── wasmJsMain             (IjsLogger.init via initPlatformLogger)
```

No other module depends on `ijs-logger`. It is a leaf dependency.

---

## Data Flow

```
IjsLogger.init(PlatformContext)
  → Creates LogFileManager (opens/creates log file)
  → Creates FileLogWriter (Kermit LogWriter wrapping LogFileManager)
  → Registers FileLogWriter + platformLogWriter() with Kermit Logger
  → Creates & installs CrashHandler

User code: IjsLogger.logger.i(tag = "Tag") { "message" }
  → FileLogWriter.log(severity, message, tag, throwable)
    → Formats: [timestamp] [LEVEL] [ThreadID:id | ThreadName:name] [Tag] Message
    → scope.launch { mutex.withLock { fileManager.write(entry) } }
      → Platform write (file/IndexedDB)

Uncaught exception:
  → CrashHandler callback fires
    → Formats crash entry
    → fileManager.write(entry) + close()
    → Delegates to previous handler (Crashlytics etc.)
```

---

## Platform-Specific Notes

### Android
- **Storage:** `getExternalFilesDir()` for API 29+ (scoped storage), legacy `getExternalStorageDirectory()` for older APIs
- **PlatformContext:** Wrapper class `PlatformContext(val context: Context)` — not a typealias because `Context` is abstract
- **Crash handler:** `Thread.setDefaultUncaughtExceptionHandler` — preserves previous handler (Crashlytics chain)

### iOS
- **Storage:** `NSDocumentDirectory` via `NSSearchPathForDirectoriesInDomains` → `NSFileHandle` append-writes
- **Crash handler:** `NSSetUncaughtExceptionHandler` with `staticCFunction` — uses a global `LogFileManager` reference because static C functions can't capture Kotlin state
- **Thread ID:** `NSThread.currentThread.hash` — `pthread_mach_thread_np` is not exposed in Kotlin/Native iOS interop headers

### JS (Browser)
- **Storage:** IndexedDB database `IndusJS`, object store `FleetLogs`
- **CRITICAL:** Uses Kotlin `dynamic` dispatch (not raw `js()` blocks) so Kotlin variables are captured in lambda closures
- **Crash handler:** `window.onerror` + `window.onunhandledrejection` via `dynamic` API — calls `fileManager.write()` directly from Kotlin

### WasmJS (Browser)
- **Storage:** IndexedDB via `js()` function interop (can access function parameters)
- **Crash handler:** Writes to `console.error` only — WasmJS `js()` blocks can't call back into Kotlin code
- **Persistence:** `navigator.storage.persist()` requested on init

---

## File Rotation Rules

| Rule | Value |
|------|-------|
| New file per session | `ijs_fleet_ddMMYYYY_HHmm.txt` |
| Max size per file | 2 MB |
| Rollover naming | `_1`, `_2`, `_3`… suffix |
| Old file retention | Permanent (no auto-delete) |
| Append to prior session | Never |

