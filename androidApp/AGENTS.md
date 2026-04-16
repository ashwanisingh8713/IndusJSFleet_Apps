# AGENTS.md - androidApp

## Purpose

Android platform entry point for the IndusJS Fleet app. **Thin shell** that hosts the shared Compose UI from `sharedUI` module. Handles Android-specific concerns: file picking for document upload, document preview/download, Firebase Crashlytics initialization, and edge-to-edge display.

**Package:** `com.indusjs.fleet.androidApp`  
**minSdk:** 23 | **targetSdk:** 36 | **compileSdk:** 36 | **JVM:** 17

---

## Source Tree

```
src/main/
├── AndroidManifest.xml
├── kotlin/com/indusjs/fleet/androidApp/
│   ├── FleetApplication.kt   # Application class — Firebase/Crashlytics init
│   └── AppActivity.kt        # Single activity — hosts shared Compose UI (193 lines)
├── res/                       # Android resources (launcher icons, themes)
└── google-services.json       # Firebase project config
```

---

## Key Files

### AppActivity.kt (193 lines)
- Single `ComponentActivity` with `enableEdgeToEdge()`
- Calls `App()` composable from `sharedUI` module
- Provides **platform callbacks** to the shared `App()`:
  - **`onPickFile`**: Uses `ActivityResultContract` for document selection (PDF, images, Word docs)
  - **`onOpenDocument`**: Opens documents via `Intent.ACTION_VIEW` with appropriate MIME type
  - **`onDownloadDocument`**: Saves documents to device storage
  - **`onThemeChanged`**: Syncs status bar icon color with dark/light theme via `WindowInsetsControllerCompat`
- Contains `PickDocumentContract` — custom `ActivityResultContract<Array<String>, Uri?>` that opens Android's document picker with multiple MIME types

### FleetApplication.kt
- Initializes Firebase + Crashlytics at app startup
- Sets `isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG` (no crash reports in debug)
- Provides `setUserId()` for crash report context
- Calls `PdfContextInitializer.init(this)` for `ijs-pdf-report` module

---

## Build

```bash
./gradlew :androidApp:assembleDebug    # Debug APK → build/outputs/apk/debug/
./gradlew :androidApp:assembleRelease  # Release APK (needs signing config)
```

## Dependencies

- `project(":sharedUI")` — all UI and business logic
- `androidx.activity:activity-compose` — Compose Activity integration
- Firebase BOM — Crashlytics + Analytics
- `androidx.core:core-ktx` — FileProvider for PDF sharing

**All business logic, screens, navigation, and data flows live in `sharedUI`. This module only provides the Android shell.**

**Razorpay:** [../Docs/Razorpay/androidApp_RAZORPAY_INTEGRATION.md](../Docs/Razorpay/androidApp_RAZORPAY_INTEGRATION.md) — [../Docs/Razorpay/README.md](../Docs/Razorpay/README.md).
