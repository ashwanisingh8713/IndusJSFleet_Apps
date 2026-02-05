# PDF Report Functionality Documentation

## Overview

This document provides a comprehensive reference for all PDF Report functionality in the IndusJS Fleet application.

---

## 1. Core PDF Infrastructure

### Common Module (expect declarations)

**Location:** `sharedUI/src/commonMain/kotlin/com/indusjs/fleet/core/pdf/`

| File | Purpose |
|------|---------|
| `PdfExportHandler.kt` | Trip costs PDF export handler |
| `TripCostsPdfGenerator.kt` | Trip costs PDF generator class |
| `CustomerTripsPdfExportHandler.kt` | Customer trips PDF export |
| `CustomerPaymentsPdfExportHandler.kt` | Customer payments PDF export |
| `PaymentsPdfExportHandler.kt` | Payments list & receipt PDF export |
| `PaymentsPdfHtmlGenerator.kt` | HTML template generator for payments |

### Platform-specific Implementations

| Platform | Location |
|----------|----------|
| Android | `androidMain/kotlin/.../core/pdf/*.android.kt` |
| iOS | `iosMain/kotlin/.../core/pdf/*.ios.kt` |
| JS | `jsMain/kotlin/.../core/pdf/*.js.kt` |
| WasmJS | `wasmJsMain/kotlin/.../core/pdf/*.wasmJs.kt` |

---

## 2. PDF Data Classes

| Data Class | Location | Purpose |
|------------|----------|---------|
| `TripCostsPdfData` | `TripDetailContract.kt` | Trip costs report data |
| `TripCostPdfItem` | `TripDetailContract.kt` | Individual trip cost item |
| `CustomerTripsPdfData` | `CustomerDetailContract.kt` | Customer trips report data |
| `CustomerTripPdfItem` | `CustomerDetailContract.kt` | Individual trip item for PDF |
| `CustomerPaymentsPdfData` | `CustomerDetailContract.kt` | Customer payments report data |
| `CustomerPaymentPdfItem` | `CustomerDetailContract.kt` | Individual payment item for PDF |
| `PaymentsListPdfData` | `PaymentsContract.kt` | Payments list report data |
| `PaymentReceiptPdfData` | `PaymentDetailScreen.kt` | Individual payment receipt data |

---

## 3. Screens with PDF Export

| Screen | File | PDF Handler | PDF Type |
|--------|------|-------------|----------|
| **TripDetailScreen** | `trips/detail/TripDetailScreen.kt` | `PdfExportHandler` | Trip Costs PDF |
| **CustomerDetailScreen** | `customers/detail/CustomerDetailScreen.kt` | `CustomerTripsPdfExportHandler`, `CustomerPaymentsPdfExportHandler` | Customer Trips, Payments, Financials |
| **PaymentsScreen** | `payments/PaymentsScreen.kt` | `PaymentsListPdfExportHandler` | Payments Summary PDF |
| **PaymentDetailScreen** | `payments/PaymentDetailScreen.kt` | `PaymentReceiptPdfExportHandler` | Payment Receipt PDF |

---

## 4. PDF Export Intents/Effects

| Contract | Intent/Effect | Description |
|----------|---------------|-------------|
| `TripDetailContract` | `Effect.ExportPdf(TripCostsPdfData)` | Export trip costs to PDF |
| `CustomerDetailContract` | `Intent.ExportPdf(ReportType)` | Export customer report |
| `CustomerDetailContract` | `Effect.ExportPaymentsPdf(CustomerPaymentsPdfData)` | Export payments PDF |
| `CustomerDetailContract` | `Effect.ExportTripsPdf(CustomerTripsPdfData)` | Export trips PDF |
| `PaymentsContract` | `Intent.ExportPaymentsToPdf` | Export payments list |

---

## 5. PDF Export Flow

```
User Clicks Export Button
    ↓
ViewModel.handleIntent(ExportPdf)
    ↓
ViewModel prepares PDF Data (TripCostsPdfData, CustomerPaymentsPdfData, etc.)
    ↓
ViewModel.sendEffect(Effect.ExportPdf(pdfData))
    ↓
Screen receives Effect and sets pdfExportData state
    ↓
PdfExportHandler Composable receives pdfData
    ↓
Platform-specific PDF generation (Android uses PdfDocument API)
    ↓
Dialog shows with Open/Share options
```

---

## 6. Android PDF Implementation Details

### Native PDF Generation

Android uses the native `PdfDocument` API for high-quality PDF generation:

```kotlin
// Example from PdfExportHandler.android.kt
val pdfDocument = PdfDocument()
val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
val page = pdfDocument.startPage(pageInfo)
val canvas = page.canvas

// Draw content using Canvas API
canvas.drawText(...)
canvas.drawRect(...)

pdfDocument.finishPage(page)
pdfDocument.writeTo(outputStream)
pdfDocument.close()
```

### Permission Handling

- Android 11+ (API 30+): Uses `MANAGE_EXTERNAL_STORAGE` or app-specific directory
- Android 10 and below: Uses `WRITE_EXTERNAL_STORAGE` permission

### File Storage

PDFs are saved to: `Documents/IndusJSFleet/` directory

---

## 7. PDF Export UI Components

### Export Result Dialog

Shows after successful PDF generation with options:
- **Open** - Opens PDF in default viewer
- **Share** - Shares PDF via system share sheet
- **Close** - Dismisses dialog

### Export Progress

Shows loading indicator during PDF generation.

---

## 8. Summary Statistics

| Category | Count |
|----------|-------|
| **PDF Handler Files (Common)** | 6 |
| **PDF Handler Files (Platform-specific)** | 20 (5 per platform × 4 platforms) |
| **Screens with PDF Export** | 4 main screens |
| **PDF Data Classes** | 8 |

---

## 9. Best Practices

1. **Always use platform-specific PDF handlers** via expect/actual pattern
2. **Use native `PdfDocument` API** on Android for better quality
3. **Provide Open/Share options** in result dialog
4. **Handle permissions properly** for storage access
5. **Generate meaningful filenames** with dates (e.g., `TripCosts_AB12CD34_2026-02-05.pdf`)
6. **Use 12-hour format with AM/PM** for datetime display in PDFs using `FleetDateTime.formatIsoToDisplayDateTime12Hour()`
7. **Use DD-MMM-YYYY format** for dates using `FleetDateTime.formatIsoToDisplayDate()`
6. **Use consistent styling** across all PDF reports
7. **Include header with company/app branding**
8. **Include footer with page numbers** for multi-page reports
9. **Format currency and dates** consistently
10. **Handle empty data gracefully** - show appropriate message

---

## 10. Adding New PDF Export

### Step 1: Create Data Class

```kotlin
// In your Contract file
data class MyReportPdfData(
    val title: String,
    val generatedDate: String,
    val items: List<MyReportPdfItem>
)

data class MyReportPdfItem(
    val id: String,
    val name: String,
    val value: String
)
```

### Step 2: Create Common Handler

```kotlin
// core/pdf/MyReportPdfExportHandler.kt
@Composable
expect fun MyReportPdfExportHandler(
    pdfData: MyReportPdfData?,
    onExportComplete: () -> Unit,
    onExportError: (String) -> Unit
)
```

### Step 3: Create Platform Implementations

Create actual implementations for:
- `MyReportPdfExportHandler.android.kt`
- `MyReportPdfExportHandler.ios.kt`
- `MyReportPdfExportHandler.js.kt`
- `MyReportPdfExportHandler.wasmJs.kt`

### Step 4: Add to Screen

```kotlin
// In your Screen composable
var pdfExportData by remember { mutableStateOf<MyReportPdfData?>(null) }

// Handle effect
is Effect.ExportPdf -> {
    pdfExportData = effect.pdfData
}

// Add handler
MyReportPdfExportHandler(
    pdfData = pdfExportData,
    onExportComplete = { pdfExportData = null },
    onExportError = { error -> 
        pdfExportData = null
        // Show error snackbar
    }
)
```

### Step 5: Add Intent/Effect

```kotlin
// In Contract
sealed interface Intent {
    data object ExportPdf : Intent
}

sealed interface Effect {
    data class ExportPdf(val pdfData: MyReportPdfData) : Effect
}
```

### Step 6: Handle in ViewModel

```kotlin
private fun exportPdf() {
    val pdfData = MyReportPdfData(
        title = "My Report",
        generatedDate = FleetDateTime.today(),
        items = state.value.items.map { ... }
    )
    sendEffect(Effect.ExportPdf(pdfData))
}
```
