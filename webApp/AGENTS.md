# AGENTS.md - webApp

## Purpose

Web platform entry point for the IndusJS Fleet app. **Minimal wrapper** that renders the shared Compose UI from `sharedUI` module in a browser using `ComposeViewport`. Supports both JavaScript and WebAssembly targets.

**Package:** (root — no package declaration)  
**Targets:** JS (browser), WasmJS (browser)

---

## Source Tree

```
src/
├── commonMain/
│   ├── kotlin/
│   │   └── main.kt           # ★ Single entry point (7 lines)
│   └── resources/
│       └── index.html         # HTML shell
```

---

## Entry Point

```kotlin
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.indusjs.fleet.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() = ComposeViewport { App() }
```

**That's the entire source code.** All UI, business logic, and navigation comes from `sharedUI`.

---

## Build & Run

```bash
./gradlew :webApp:jsBrowserDevelopmentRun        # JS dev server (hot reload)
./gradlew :webApp:wasmJsBrowserDevelopmentRun    # WASM dev server (hot reload)
./gradlew :webApp:jsBrowserProductionWebpack     # JS production bundle
./gradlew :webApp:wasmJsBrowserProductionWebpack # WASM production bundle
```

## Dependencies

- `project(":sharedUI")` — all UI and business logic
- `compose.ui` — Compose UI for web (`ComposeViewport`)

## Limitations vs Android

- No file picker support (document upload not available)
- No Firebase Crashlytics
- PDF generation uses browser Blob download (no native share sheet)
- MQTT for Maps may require WebSocket bridge
