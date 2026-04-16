# AGENTS.md - webApp

## Purpose

Web platform entry point for the IndusJS Fleet app. **Thin wrapper** that renders the shared Compose UI from `sharedUI` in a browser using `ComposeViewport`. Supports both JavaScript and WebAssembly targets.

**Razorpay:** `index.html` loads Standard Checkout (`checkout.js`) and a small `__IndusFleetRazorpay` bridge before `webApp.js`. `main.kt` passes `platformRazorpayLauncher()` into `App` so subscription checkout uses the real JS / Wasm launchers from `screen-payment` (not the default no-op cancel).

**Package:** (root — no package declaration in `main.kt`); platform helpers in `com.indusjs.fleet.web`  
**Targets:** JS (browser), WasmJS (browser)

---

## Source Tree

```
src/
├── commonMain/
│   ├── kotlin/
│   │   ├── main.kt                    # ComposeViewport { App(razorpayLauncher = ...) }
│   │   └── com/indusjs/fleet/web/
│   │       └── PlatformRazorpay.kt    # expect fun platformRazorpayLauncher()
│   └── resources/
│       ├── index.html                 # checkout.js + __IndusFleetRazorpay + webApp.js
│       └── manifest.json
├── jsMain/kotlin/.../web/
│   └── PlatformRazorpay.js.kt         # actual → createWebRazorpayLauncher()
└── wasmJsMain/kotlin/.../web/
    └── PlatformRazorpay.wasmJs.kt     # actual → createWasmJsRazorpayLauncher()
```

---

## Entry Point

```kotlin
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.indusjs.fleet.App
import com.indusjs.fleet.web.platformRazorpayLauncher

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App(razorpayLauncher = platformRazorpayLauncher()) }
```

All other UI, navigation, and payment **logic** live in `sharedUI` / `screen-payment`. Web-specific pieces: HTML shell, Razorpay script order, and `expect`/`actual` launcher wiring.

---

## Build & Run

```bash
./gradlew :webApp:jsBrowserDevelopmentRun        # JS dev server (hot reload)
./gradlew :webApp:wasmJsBrowserDevelopmentRun    # WASM dev server (hot reload)
./gradlew :webApp:jsBrowserProductionWebpack       # JS production bundle
./gradlew :webApp:wasmJsBrowserProductionWebpack   # WASM production bundle
```

Processed HTML for both targets: `webApp/build/processedResources/js/main/index.html` and `.../wasmJs/main/index.html` (must include Razorpay scripts before the app bundle).

## Dependencies

- `project(":sharedUI")` — app shell, navigation, `LocalRazorpayLauncher` provider
- `project(":screen-payment")` — `RazorpayLauncher` factories for JS / Wasm
- `compose.ui` — Compose UI for web (`ComposeViewport`)

**Razorpay design doc:** [../Docs/Razorpay/webApp_RAZORPAY_INTEGRATION.md](../Docs/Razorpay/webApp_RAZORPAY_INTEGRATION.md) — [../Docs/Razorpay/README.md](../Docs/Razorpay/README.md).

## Limitations vs Android

- No file picker support (document upload not available)
- No Firebase Crashlytics
- PDF generation uses browser Blob download (no native share sheet)
- MQTT for Maps may require WebSocket bridge
- Razorpay requires network access to `checkout.razorpay.com` (script + checkout UI); backend must expose order-create / verify APIs with correct CORS for your web origin
