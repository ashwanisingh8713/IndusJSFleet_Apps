# Shared Libraries Module Guide

> Use this prompt when working with or modifying the shared KMP library modules.

## Module Overview

| Module | Package | Purpose | Targets |
|--------|---------|---------|---------|
| `ijs-error-lib` | `com.indusjs.error` | Result<T>, exception hierarchy, error handling | All KMP |
| `ijs-dispatcher-lib` | `com.indusjs.dispatcher` | Coroutine dispatchers, test helpers | All KMP |
| `ijs-datetime-utils` | `com.indusjs.datetimeutils` | FleetDateTime date/time operations | All KMP |
| `ijs-datetime-picker` | `com.indusjs.datetimepicker` | Compose date/time picker dialog | All KMP |
| `ijs-pdf-report` | `com.indusjs.pdfreport` | PDF report generation (HTML → PDF) | All KMP |

## Dependency Chain

```
sharedUI ──→ ijs-error-lib
sharedUI ──→ ijs-dispatcher-lib
sharedUI ──→ ijs-datetime-picker ──→ ijs-datetime-utils
sharedUI ──→ ijs-datetime-utils
sharedUI ──→ ijs-pdf-report ──→ ijs-datetime-utils
```

## ijs-error-lib

**Key exports:**
```kotlin
import com.indusjs.error.result.Result          // Result.Success, Result.Error, Result.Loading
import com.indusjs.error.exception.ApiException  // HTTP errors
import com.indusjs.error.exception.AuthException // 401/403
import com.indusjs.error.exception.NetworkException // Connectivity
import com.indusjs.error.exception.ValidationException // Field validation
import com.indusjs.error.handler.ErrorHandler    // getErrorInfo() for user-friendly messages
```

**Dependencies:** `kotlinx-coroutines-core` only

## ijs-dispatcher-lib

**Key exports:**
```kotlin
import com.indusjs.dispatcher.DispatcherProvider         // Interface
import com.indusjs.dispatcher.DefaultDispatcherProvider   // Production
import com.indusjs.dispatcher.TestDispatcherProvider      // Tests
```

**Dependencies:** `kotlinx-coroutines-core` only

## ijs-datetime-utils

**Key export:** `FleetDateTime` singleton object (1858 lines)

```kotlin
import com.indusjs.datetimeutils.FleetDateTime

FleetDateTime.today()
FleetDateTime.toIso8601(date, time)
FleetDateTime.formatAnyToDisplayDate(anyDateString)
FleetDateTime.isValidDate(date)
```

**Dependencies:** `kotlinx-datetime` only

## ijs-datetime-picker

**Key export:** `FleetDateTimePicker` composable

```kotlin
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode

FleetDateTimePicker(
    date = "15-03-2026", time = "15:30",
    onDateTimeChange = { d, t -> /* handle */ },
    mode = PickerMode.DATE_TIME
)
```

**Dependencies:** `ijs-datetime-utils` (API), Compose, `kotlinx-datetime`

## ijs-pdf-report

**Key export:** `PdfReportFacade` singleton

```kotlin
import com.indusjs.pdfreport.PdfReportFacade

val result = PdfReportFacade.generateTripCostsReport(data)
if (result.success) PdfReportFacade.shareReport(result)
```

Platform-specific: Android uses WebView, iOS uses UIPrintPageRenderer, Web uses Blob download.

**Dependencies:** `ijs-datetime-utils` (API), Compose, `kotlinx-coroutines-core`

## Adding a New Shared Library

1. Create module directory at project root (e.g., `ijs-my-lib/`)
2. Add `build.gradle.kts` with KMP targets:
   ```kotlin
   kotlin {
       androidTarget()
       iosX64(); iosArm64(); iosSimulatorArm64()
       js { browser() }
       wasmJs { browser() }
       sourceSets {
           commonMain.dependencies { /* common deps */ }
       }
   }
   ```
3. Add to `settings.gradle.kts`: `include(":ijs-my-lib")`
4. Add dependency in `sharedUI/build.gradle.kts`:
   ```kotlin
   commonMain.dependencies {
       api(project(":ijs-my-lib"))
   }
   ```
5. Create `AGENTS.md` in the module root

## Build Verification

```bash
./gradlew :ijs-error-lib:compileCommonMainKotlinMetadata
./gradlew :ijs-dispatcher-lib:compileCommonMainKotlinMetadata
./gradlew :ijs-datetime-utils:compileCommonMainKotlinMetadata
./gradlew :ijs-datetime-picker:compileCommonMainKotlinMetadata
./gradlew :ijs-pdf-report:compileCommonMainKotlinMetadata
```

