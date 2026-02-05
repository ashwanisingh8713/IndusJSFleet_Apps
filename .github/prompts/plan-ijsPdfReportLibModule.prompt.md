# Plan: Create `ijs-pdf-report` Library Module with Facade Pattern

## Overview

Create a dedicated Kotlin Multiplatform PDF Report library module (`ijs-pdf-report`) that consolidates all PDF generation functionality using the Facade design pattern. This module provides a unified API for generating, saving, and sharing PDFs across Android, iOS, JS, and WasmJS platforms with consistent naming using `DD-MMM-YYYY hh:mm am/pm` timestamp format from `ijs-datetime-utils`.

---

## Key Decisions

| Decision | Choice |
|----------|--------|
| iOS PDF Implementation | HTML WebView rendering to PDF |
| JS/WasmJS PDF Library | jsPDF |
| Execution Model | Async with coroutines |
| Storage Location | Platform-specific defaults |
| Existing sharedUI PDF code | Delete after migration |

---

## Module Structure

```
ijs-pdf-report/
├── build.gradle.kts
├── README.md
└── src/
    ├── commonMain/kotlin/com/indusjs/pdfreport/
    │   ├── PdfReportFacade.kt              # Main Facade entry point
    │   ├── model/
    │   │   ├── PdfReportData.kt            # Base sealed interface
    │   │   ├── TripCostsPdfData.kt
    │   │   ├── CustomerTripsPdfData.kt
    │   │   ├── CustomerPaymentsPdfData.kt
    │   │   ├── PaymentsListPdfData.kt
    │   │   ├── PaymentReceiptPdfData.kt
    │   │   └── PdfExportResult.kt
    │   ├── generator/
    │   │   ├── PdfGenerator.kt             # expect class
    │   │   └── HtmlTemplateGenerator.kt    # HTML template generator
    │   ├── util/
    │   │   └── PdfFileNameGenerator.kt     # Filename generation utility
    │   └── ui/
    │       └── PdfExportDialog.kt          # expect composable
    ├── androidMain/kotlin/com/indusjs/pdfreport/
    │   ├── generator/
    │   │   └── PdfGenerator.android.kt     # HTML WebView to PDF
    │   └── ui/
    │       └── PdfExportDialog.android.kt
    ├── iosMain/kotlin/com/indusjs/pdfreport/
    │   ├── generator/
    │   │   └── PdfGenerator.ios.kt         # HTML WebView to PDF
    │   └── ui/
    │       └── PdfExportDialog.ios.kt
    ├── jsMain/kotlin/com/indusjs/pdfreport/
    │   ├── generator/
    │   │   └── PdfGenerator.js.kt          # jsPDF library
    │   └── ui/
    │       └── PdfExportDialog.js.kt
    └── wasmJsMain/kotlin/com/indusjs/pdfreport/
        ├── generator/
        │   └── PdfGenerator.wasmJs.kt      # jsPDF library
        └── ui/
            └── PdfExportDialog.wasmJs.kt
```

---

## PDF Filename Format

| Report Type | Filename Format | Example |
|-------------|-----------------|---------|
| Trip Costs | `TripCosts_{VehicleNumber}_Trip{Id}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `TripCosts_AB12CD34_Trip1_05-Feb-2026_02-30-PM.pdf` |
| Customer Trips | `CustomerTrips_{CustomerName}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `CustomerTrips_ABC-Corporation_05-Feb-2026_02-30-PM.pdf` |
| Customer Payments | `CustomerPayments_{CustomerName}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `CustomerPayments_ABC-Corporation_05-Feb-2026_02-30-PM.pdf` |
| Payments List | `PaymentsList_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `PaymentsList_05-Feb-2026_02-30-PM.pdf` |
| Payment Receipt | `PaymentReceipt_{ReceiptNumber}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `PaymentReceipt_RP-001_05-Feb-2026_02-30-PM.pdf` |

**Note:** Customer name will be sanitized (replace spaces with `-`, remove special characters) for valid filenames.

---

## Storage Locations

| Platform | Location |
|----------|----------|
| Android | `/sdcard/IndusJSFleet/exportedPdf/` |
| iOS | App Documents directory |
| JS/WasmJS | Browser download folder |

---

## Android Permission Handling

```kotlin
// Android 11+ (API 30+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    if (!Environment.isExternalStorageManager()) {
        // Request MANAGE_EXTERNAL_STORAGE via Settings intent
    }
}
// Android 6-10 (API 23-29)
else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
}
```

---

## Usage Example

```kotlin
// In ViewModel
private fun exportTripCostsPdf() {
    viewModelScope.launch {
        updateState { copy(isExporting = true) }
        
        val pdfData = TripCostsPdfData(
            tripId = trip.id,
            vehicleNumber = trip.vehicleNumber,
            costs = trip.costs.map { ... },
            totalCost = calculateTotal(),
            generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
        )
        
        val result = PdfReportFacade.generateTripCostsReport(pdfData)
        
        updateState { copy(isExporting = false) }
        
        if (result.success) {
            sendEffect(Effect.ShowPdfResult(result))
        } else {
            sendEffect(Effect.ShowError(result.errorMessage ?: "Failed to generate PDF"))
        }
    }
}

// In Screen
PdfExportDialog(
    result = state.pdfResult,
    isLoading = state.isExporting,
    onOpen = { 
        scope.launch { PdfReportFacade.openReport(state.pdfResult!!) }
    },
    onShare = { 
        scope.launch { PdfReportFacade.shareReport(state.pdfResult!!) }
    },
    onDismiss = { viewModel.sendIntent(Intent.ClearPdfResult) }
)
```

---

## Files to Delete from sharedUI (After Migration)

```
sharedUI/src/commonMain/kotlin/com/indusjs/fleet/core/pdf/
├── CustomerPaymentsPdfExportHandler.kt       # DELETE
├── CustomerTripsPdfExportHandler.kt          # DELETE
├── PaymentsPdfExportHandler.kt               # DELETE
├── PaymentsPdfHtmlGenerator.kt               # DELETE
├── PdfExportHandler.kt                       # DELETE
└── TripCostsPdfGenerator.kt                  # DELETE

sharedUI/src/androidMain/kotlin/com/indusjs/fleet/core/pdf/
├── CustomerPaymentsPdfExportHandler.android.kt   # DELETE
├── CustomerTripsPdfExportHandler.android.kt      # DELETE
├── PaymentsPdfExportHandler.android.kt           # DELETE
├── PdfExportHandler.android.kt                   # DELETE
└── TripCostsPdfGenerator.android.kt              # DELETE

sharedUI/src/iosMain/kotlin/com/indusjs/fleet/core/pdf/
├── (all files)                                   # DELETE

sharedUI/src/jsMain/kotlin/com/indusjs/fleet/core/pdf/
├── (all files)                                   # DELETE

sharedUI/src/wasmJsMain/kotlin/com/indusjs/fleet/core/pdf/
├── (all files)                                   # DELETE
```

---

## Implementation Completed

### Created Files:

1. **Module Configuration**
   - `ijs-pdf-report/build.gradle.kts` - With androidx.core:core-ktx dependency for FileProvider
   - `ijs-pdf-report/README.md`
   - Updated `settings.gradle.kts` with `include(":ijs-pdf-report")`
   - Updated `sharedUI/build.gradle.kts` with `api(project(":ijs-pdf-report"))`

2. **Data Models (commonMain)**
   - `model/PdfReportData.kt` - Base sealed interface + PdfReportType enum
   - `model/PdfExportResult.kt` - Result class with success/error factory methods
   - `model/TripCostsPdfData.kt`
   - `model/CustomerTripsPdfData.kt`
   - `model/CustomerPaymentsPdfData.kt`
   - `model/PaymentsListPdfData.kt`
   - `model/PaymentReceiptPdfData.kt`

3. **Utilities (commonMain)**
   - `util/PdfFileNameGenerator.kt` - Filename generation with sanitization using ijs-datetime-utils

4. **Generator (commonMain)**
   - `generator/PdfGenerator.kt` - expect class with generatePdf, sharePdf, openPdf methods
   - `generator/HtmlTemplateGenerator.kt` - Shared HTML templates with formatAmount fix for multiplatform

5. **Facade (commonMain)**
   - `PdfReportFacade.kt` - Main entry point with type-specific methods

6. **UI (commonMain)**
   - `ui/PdfExportDialog.kt` - expect composable for showing export results

7. **Platform Implementations**
   - `androidMain/generator/PdfGenerator.android.kt` - Uses WebView + PrintDocumentAdapter + FileProvider
   - `androidMain/ui/PdfExportDialog.android.kt` - Material3 dialog with Save/Share/Cancel options
   - `iosMain/generator/PdfGenerator.ios.kt` - Uses UIMarkupTextPrintFormatter + UIPrintPageRenderer with @OptIn(ExperimentalForeignApi)
   - `iosMain/ui/PdfExportDialog.ios.kt` - Material3 dialog
   - `jsMain/generator/PdfGenerator.js.kt` - Browser download using Blob + anchor element
   - `jsMain/ui/PdfExportDialog.js.kt` - Material3 dialog
   - `wasmJsMain/generator/PdfGenerator.wasmJs.kt` - Simplified with JS external functions
   - `wasmJsMain/ui/PdfExportDialog.wasmJs.kt` - Material3 dialog

### Key Fixes Applied:
1. **formatAmount()** - Replaced String.format() with multiplatform-compatible number formatting using kotlin.math.round
2. **iOS Implementation** - Added @file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class) and CGRectMake import from CoreGraphics
3. **Android Implementation** - Added androidx.core:core-ktx:1.15.0 dependency for FileProvider
4. **JS Implementation** - Fixed lambda syntax issues with proper undefined check
5. **WasmJS Implementation** - Simplified to use external JS declarations for alert function

### Build Verification:
- ✅ `ijs-pdf-report:assemble` - BUILD SUCCESSFUL
- ✅ `sharedUI:compileAndroidMain` - BUILD SUCCESSFUL (with warnings only)
- ✅ `androidApp:assembleDebug` - BUILD SUCCESSFUL

### Composable Handlers (commonMain)
Ready-to-use composable handlers added in `handler/` package:
- `TripCostsPdfHandler.kt`
- `CustomerTripsPdfHandler.kt`
- `CustomerPaymentsPdfHandler.kt`
- `PaymentsListPdfHandler.kt`
- `PaymentReceiptPdfHandler.kt`

---

## Next Steps (Migration from sharedUI)

Once the new `ijs-pdf-report` module has been tested:

1. **Update existing screens** to use `PdfReportFacade` instead of old PDF handlers
2. **Delete old PDF files** from sharedUI as listed in the "Files to Delete" section
3. **Test PDF generation** on Android, iOS, and Web platforms

