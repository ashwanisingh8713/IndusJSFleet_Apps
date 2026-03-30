# Build Conventions — IndusJS Fleet

## Centralized Android Settings

All modules apply `gradle/fleet-android-conventions.gradle`:

```kotlin
// In each module's build.gradle.kts:
apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
```

**Auto-configured values (DO NOT set in module build files):**
- `compileSdk = 36`
- `minSdk = 23` (locationTracker overrides to 24)
- `targetSdk = 36`
- `jvmTarget = JVM_17`

## Compose Feature Convention

Feature modules with UI apply `gradle/fleet-compose-conventions.gradle`:

```kotlin
apply(from = rootProject.file("gradle/fleet-compose-conventions.gradle"))
```

**Auto-adds:** Compose Runtime, UI, Foundation, Material 3, Resources, Lifecycle, `ijs-ui-components-lib`

## Module Types

| Type | Plugins | Convention Files |
|------|---------|-----------------|
| Foundation lib | `kotlin.multiplatform`, `android.kmp.library` | `fleet-android-conventions` |
| Feature data lib | `kotlin.multiplatform`, `android.kmp.library`, `kotlinx-serialization` | `fleet-android-conventions` |
| Feature screen | All above + `compose.compiler`, `compose.multiplatform` | Both conventions |
| App module | `com.android.application`, `kotlin.android` | `fleet-android-conventions` |

## Version Catalog

All versions in `gradle/libs.versions.toml`. Key versions:
- Kotlin: 2.3.0, Compose: 1.10.0, Material 3: 1.10.0-alpha05
- Ktor: 3.3.3, Metro: 0.9.1, Navigation 3: 1.1.0-alpha01

## Build Commands

```bash
./gradlew :androidApp:assembleDebug              # Android debug
./gradlew :webApp:jsBrowserDevelopmentRun          # Web JS dev
./gradlew :webApp:wasmJsBrowserDevelopmentRun      # Web WASM dev
```

## Key Files

- `gradle/fleet-android-conventions.gradle` — Android settings (70 lines)
- `gradle/fleet-compose-conventions.gradle` — Compose + UI deps (47 lines)
- `gradle/libs.versions.toml` — Version catalog
- `settings.gradle.kts` — All 27 module includes

## Common Mistakes

- ❌ Setting `compileSdk`/`minSdk` in module build files — use convention
- ❌ Adding Compose deps manually — use `fleet-compose-conventions.gradle`
- ❌ Hardcoding versions — use `libs.versions.toml` catalog references

