# webApp — Web Entry Point

## Overview

Minimal web shell for JS + WasmJS browser targets. Single composable entry point.

## File Tree

```
webApp/
├── build.gradle.kts
├── AGENTS.md
└── src/commonMain/
    ├── kotlin/main.kt
    └── resources/
        ├── index.html
        └── manifest.json
```

## Bootstrap

### main.kt
```kotlin
fun main() {
    ComposeViewport(document.body!!) {
        App()  // com.indusjs.fleet.App from sharedUI
    }
}
```

No platform-specific callbacks passed (no file handling, no theme change hooks).

### index.html
- Full-viewport layout with loading SVG animation
- Script: `webApp.js` (webpack output)
- Meta: responsive viewport

### manifest.json
- PWA-compatible: name, icons, theme color
- `"IndusJSFleet"` branding

## Build Targets

```bash
./gradlew :webApp:jsBrowserDevelopmentRun      # JS dev server
./gradlew :webApp:wasmJsBrowserDevelopmentRun   # WASM dev server
```

## Dependencies

- `project(":sharedUI")` — entire fleet app
- `compose-ui` — Compose for web
