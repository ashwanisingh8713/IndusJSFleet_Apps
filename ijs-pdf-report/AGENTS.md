# AGENTS.md - ijs-pdf-report

## Purpose

Cross-platform PDF report generation library for the IndusJS Fleet app. Uses the **Facade pattern** — single entry point `PdfReportFacade` generates HTML templates and converts them to PDF via platform-specific renderers. Used for trip cost reports, driver cost reports, customer financials, vehicle P&L, payment receipts, and more.

**Package:** `com.indusjs.pdfreport`  
**Targets:** Android, iOS (x64/arm64/simulatorArm64), JS, WasmJS  
**Dependencies:** `ijs-datetime-utils` (API), Compose Multiplatform, `kotlinx-coroutines-core`

---

## Source Tree

```
src/commonMain/kotlin/com/indusjs/pdfreport/
├── PdfReportFacade.kt           # ★ Single entry point for all PDF operations
├── PdfContextInitializer.kt     # Platform context initialization (Android needs Context)
├── generator/
│   ├── PdfGenerator.kt          # expect class — platform-specific HTML→PDF conversion
│   └── HtmlTemplateGenerator.kt # Shared HTML template builder for each report type
├── handler/
│   └── PdfShareHandler.kt       # Share/open/save PDF files
├── model/                       # Data models for each report type
│   ├── PdfReportData.kt         # Base sealed interface + PdfReportType enum
│   ├── PdfExportResult.kt       # Result: success/failure, file path, bytes
│   ├── TripCostsPdfData.kt
│   ├── DriverCostsPdfData.kt
│   ├── CustomerTripsPdfData.kt
│   ├── CustomerPaymentsPdfData.kt
│   ├── CustomerFinancialsPdfData.kt
│   ├── VehicleProfitLossPdfData.kt
│   ├── VehicleMaintenanceCostsPdfData.kt
│   ├── VehicleFinancePdfData.kt
│   ├── FleetProfitLossPdfData.kt
│   ├── CostAnalysisPdfData.kt
│   ├── PaymentReceiptPdfData.kt
│   └── PaymentsListPdfData.kt
├── ui/
│   └── PdfExportDialog.kt      # expect composable — export progress/share dialog
└── util/
    └── PdfFileNameGenerator.kt  # Sanitized filename generation with timestamp
```

### Platform Implementations

| Platform | HTML→PDF Method |
|----------|----------------|
| Android | `WebView` + `PrintDocumentAdapter` + `FileProvider` |
| iOS | `UIMarkupTextPrintFormatter` + `UIPrintPageRenderer` |
| JS | Browser Blob + anchor download element |
| WasmJS | External JS declarations for download |

---

## Entry Point: `PdfReportFacade`

```kotlin
// Generate a report
val result = PdfReportFacade.generateTripCostsReport(tripCostsPdfData)
if (result.success) {
    PdfReportFacade.shareReport(result)    // Platform share sheet
    PdfReportFacade.openReport(result)     // Platform PDF viewer
}
```

**Available generators:**
- `generateTripCostsReport(data)` — Trip cost breakdown
- `generateDriverCostsReport(data)` — Driver cost summary
- `generateCustomerTripsReport(data)` — Customer's trip history
- `generateCustomerPaymentsReport(data)` — Customer payment history
- `generateVehicleProfitLossReport(data)` — Vehicle P&L
- `generatePaymentReceiptReport(data)` — Single payment receipt
- `generatePaymentsListReport(data)` — Payments list export

### Filename Format

`{ReportType}_{Identifier}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf`

Example: `TripCosts_MH12AB34_Trip1_15-Mar-2026_02-30-PM.pdf`

---

## Usage in ViewModel

```kotlin
private fun exportPdf() {
    viewModelScope.launch {
        updateState { copy(isExporting = true) }
        val data = TripCostsPdfData(tripId = trip.id, vehicleNumber = trip.vehicleNumber, costs = ...)
        val result = PdfReportFacade.generateTripCostsReport(data)
        updateState { copy(isExporting = false) }
        if (result.success) sendEffect(Effect.ShowPdfResult(result))
        else sendEffect(Effect.ShowError(result.errorMessage ?: "Failed"))
    }
}
```

### Storage Locations

| Platform | Path |
|----------|------|
| Android | `/sdcard/IndusJSFleet/exportedPdf/` (requires MANAGE_EXTERNAL_STORAGE on API 30+) |
| iOS | App Documents directory |
| JS/WasmJS | Browser download folder |
