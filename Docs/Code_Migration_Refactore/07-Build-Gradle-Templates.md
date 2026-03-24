# 07 — Build Gradle Templates

## Purpose

Exact `build.gradle.kts` changes required for each module during the presentation migration.

---

## Critical Fix: sharedUI Missing Dependency

`sharedUI/build.gradle.kts` does **NOT** currently depend on `ijs-ui-components-lib`.
This must be added before any import redirects.

```diff
# sharedUI/build.gradle.kts — commonMain.dependencies
+ implementation(project(":ijs-ui-components-lib"))
```

---

## Feature Module build.gradle.kts Template (Post-Migration)

Each feature module that gains a presentation layer must add Compose plugins and the
`fleet-compose-conventions.gradle` script (which auto-adds `ijs-ui-components-lib`,
Compose deps, Lifecycle deps).

### Plugins to Add

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.metro)
    // ── NEW: Required for Compose UI in feature module ──
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}
```

### Convention Script to Add

```kotlin
apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
// ── NEW: Adds ijs-ui-components-lib + Compose + Lifecycle deps ──
apply(from = rootProject.file("gradle/fleet-compose-conventions.gradle"))
```

### What `fleet-compose-conventions.gradle` Auto-Adds

```groovy
// No need to repeat these in feature build.gradle.kts:
implementation(project(":ijs-ui-components-lib"))
implementation(libs.compose.runtime)
implementation(libs.compose.ui)
implementation(libs.compose.foundation)
implementation(libs.compose.resources)
implementation(libs.compose.material3)
implementation(libs.androidx.lifecycle.viewmodel)
implementation(libs.androidx.lifecycle.runtime)
```

### What Must Stay in Feature build.gradle.kts

```kotlin
commonMain.dependencies {
    // Existing — keep as-is
    api(project(":ijs-network-lib"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kermit)
    implementation(libs.ktor.client.core)
}
```

---

## Per-Module Diffs

### feat-customer/build.gradle.kts

```diff
 plugins {
     alias(libs.plugins.kotlin.multiplatform)
     alias(libs.plugins.android.kmp.library)
     alias(libs.plugins.kotlinx.serialization)
     alias(libs.plugins.metro)
+    alias(libs.plugins.compose.compiler)
+    alias(libs.plugins.compose.multiplatform)
 }

 apply(from = rootProject.file("gradle/fleet-android-conventions.gradle"))
+apply(from = rootProject.file("gradle/fleet-compose-conventions.gradle"))

 kotlin {
     android {
-        namespace = "com.ijs.customer"
+        namespace = "com.ijs.customer"
+        androidResources.enable = true   // For composeResources if needed
     }
     // ... targets unchanged ...
 }
```

### feat-team/build.gradle.kts

Same diff pattern as feat-customer.

### feat-vehicle/build.gradle.kts

Same diff pattern. No additional dependencies needed — `ijs-network-lib` (via `api()`)
already provides `CostsRepository`, `CostTypesRepository`, `UserLocalDataSource`.

### feat-driver/build.gradle.kts

Same diff pattern as feat-vehicle.

### feat-trip/build.gradle.kts

Same diff pattern. `GooglePlacesService` access will come through `TripExternalDeps`
callback, not as a direct import.

### feat-payment/build.gradle.kts

Same diff pattern. `UserRepository` is available via `ijs-network-lib`.

### feat-finance/build.gradle.kts

Same diff pattern.

### feat-report/build.gradle.kts

Same diff pattern.

---

## sharedUI build.gradle.kts Changes (Cumulative)

```diff
 commonMain.dependencies {
     // IndusJS Libraries
     implementation(project(":ijs-core-lib"))
     implementation(project(":ijs-network-lib"))
+    implementation(project(":ijs-ui-components-lib"))
     implementation(project(":feat-driver"))
     implementation(project(":feat-vehicle"))
     implementation(project(":feat-trip"))
     implementation(project(":feat-customer"))
     implementation(project(":feat-payment"))
     implementation(project(":feat-team"))
     implementation(project(":feat-report"))
     implementation(project(":feat-finance"))
-    implementation(project(":ijs-error-lib"))        // Already transitive via ijs-network-lib
-    implementation(project(":ijs-dispatcher-lib"))    // Already transitive via ijs-network-lib
     implementation(project(":ijs-datetime-picker"))
     implementation(project(":ijs-datetime-utils"))
     api(project(":ijs-pdf-report"))
     // ... rest unchanged
 }
```

---

## Dependency Rule Enforcement

| Module | May depend on | Must NOT depend on |
|--------|--------------|-------------------|
| `feat-*` | `ijs-network-lib`, `ijs-ui-components-lib` | Any other `feat-*` module, `sharedUI` |
| `ijs-ui-components-lib` | `ijs-core-lib` | Any `feat-*` module, `ijs-network-lib` |
| `sharedUI` | All `feat-*`, `ijs-ui-components-lib`, `ijs-network-lib` | — |

