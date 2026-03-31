# androidApp — Android Entry Point

## Overview

Thin shell that bootstraps the Compose Multiplatform app on Android with platform-specific integrations (Firebase, file handling, logging).

## File Tree

```
androidApp/
├── build.gradle.kts
├── google-services.json
├── AGENTS.md
└── src/main/
    ├── AndroidManifest.xml
    ├── kotlin/com/indusjs/fleet/androidApp/
    │   ├── FleetApplication.kt        # Application class
    │   └── AppActivity.kt             # Single activity
    └── res/
        ├── mipmap-anydpi-v26/ic_launcher.xml
        └── xml/
            ├── file_paths.xml          # FileProvider paths
            └── network_security_config.xml
```

## Bootstrap Flow

### FleetApplication (Application)
1. `IjsLogger.init(PlatformContext(this))` — initializes Kermit + file logging + crash handler
2. Firebase Crashlytics: collection **off** in debug, **on** in release

### AppActivity (Single Activity)
1. `enableEdgeToEdge()` — full-screen layout
2. `setContent { AndroidApp() }` — enters Compose

### AndroidApp Composable
Calls `com.indusjs.fleet.App(...)` from `sharedUI` with Android-specific callbacks:
- `onThemeChanged` — adjusts status bar icons for light/dark
- `onPickFile` — file picker via `ActivityResultContracts`
- `onOpenDocument` — open document via `FileProvider` + `ACTION_VIEW`
- `onDownloadDocument` — download to device storage
- `onSaveDocument` — save via content resolver

## Configuration

| Setting | Value |
|---------|-------|
| `applicationId` | `com.indusjs.fleet.androidApp` |
| `compileSdk` | 36 (from conventions) |
| `minSdk` | 23 (from conventions) |
| `targetSdk` | 36 (from conventions) |
| `jvmTarget` | JVM_17 |
| `versionCode` | 1 |
| `versionName` | "1.0.0" |

## Manifest Permissions

- `INTERNET`
- `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, `MANAGE_EXTERNAL_STORAGE`
- Network security config: cleartext only for `192.168.1.8` (local dev)

## Dependencies

- `project(":sharedUI")` — entire fleet app
- `project(":ijs-logger-lib")` — platform logging
- `androidx.activity:activity-compose`
- Firebase BOM → Crashlytics + Analytics
