# ijs-pdf-report — PDF Report Generation

**Namespace:** `com.indusjs.pdfreport`
**Depends on:** `ijs-datetime-utils` (api), Compose, coroutines

## File Tree (49 files)

```
ijs-pdf-report/src/
├── commonMain/kotlin/com/indusjs/pdfreport/
│   ├── PdfContextInitializer.kt          # Platform context setup
│   ├── PdfReportFacade.kt                # Main entry point
│   ├── generator/
│   │   ├── HtmlTemplateGenerator.kt      # Builds HTML from report data
│   │   └── PdfGenerator.kt              # expect (platform-specific)
│   ├── handler/
│   │   ├── CostAnalysisPdfHandler.kt
│   │   ├── CustomerFinancialsPdfHandler.kt
│   │   ├── CustomerPaymentsPdfHandler.kt
│   │   ├── CustomerTripsPdfHandler.kt
│   │   ├── DriverCostsPdfHandler.kt
│   │   ├── FleetProfitLossPdfHandler.kt
│   │   ├── InitializePdfContext.kt       # expect
│   │   ├── PaymentReceiptPdfHandler.kt
│   │   ├── PaymentsListPdfHandler.kt
│   │   ├── TripCostsPdfHandler.kt
│   │   ├── VehicleFinancePdfHandler.kt
│   │   ├── VehicleMaintenanceCostsPdfHandler.kt
│   │   └── VehicleProfitLossPdfHandler.kt
│   ├── model/                            # Report data models
│   └── ui/
│       └── PdfExportDialog.kt            # expect (platform-specific dialog)
├── androidMain/ (PdfGenerator, PdfExportDialog, PdfContextInitializer, InitializePdfContext)
├── iosMain/     (same set)
├── jsMain/      (same set)
└── wasmJsMain/  (same set)
```

## How It Works

```
Report Request
  → PdfReportFacade.generateReport(reportType, data)
    → HtmlTemplateGenerator.generateHtml(data)    # Builds styled HTML
    → PdfFileNameGenerator.generateFileName(type)  # e.g., "VehiclePL_2026-03-31.pdf"
    → PdfGenerator.generatePdf(html, fileName)     # Platform-specific rendering
      → PdfExportResult (path, size, or error)
```

## Platform-Specific Rendering

### Android
1. Requires `PdfContextInitializer.initialize(context)` (typically via `InitializePdfContext()` composable)
2. Creates off-screen `WebView`, attaches to activity `decorView`
3. Loads HTML via `loadDataWithBaseURL`
4. After page load + 3s delay: measures/layouts WebView
5. Draws WebView to `Bitmap`, splits into A4 pages
6. Writes via `PdfDocument` to `getExternalFilesDir(DOCUMENTS)/exportedPdf/`
7. Share/Open: `FileProvider` URI + `ACTION_SEND` / `ACTION_VIEW`

### iOS
1. `UIMarkupTextPrintFormatter` + `UIPrintPageRenderer`
2. `UIGraphicsBeginPDFContextToData` with page loop
3. Writes to `Documents/IndusJSFleet/{fileName}`
4. Share: `UIActivityViewController`; Open: `UIDocumentInteractionController`

### JS (Browser)
1. Hidden iframe writes HTML
2. Creates `Blob` with `text/html`
3. Triggers download (actually downloads `.html`, not binary PDF)
4. Comment notes future jsPDF/html2pdf integration

### WasmJS
Same approach as JS.

## Report Types

| Handler | Report |
|---------|--------|
| `TripCostsPdfHandler` | Trip costs breakdown |
| `VehicleMaintenanceCostsPdfHandler` | Vehicle maintenance costs |
| `DriverCostsPdfHandler` | Driver costs (salary, advance, deductions) |
| `VehicleProfitLossPdfHandler` | Vehicle P&L statement |
| `FleetProfitLossPdfHandler` | Fleet-wide P&L |
| `CostAnalysisPdfHandler` | Cost analysis by type |
| `PaymentReceiptPdfHandler` | Single payment receipt |
| `PaymentsListPdfHandler` | Payment list export |
| `CustomerFinancialsPdfHandler` | Customer financial summary |
| `CustomerPaymentsPdfHandler` | Customer payment history |
| `CustomerTripsPdfHandler` | Customer trip history |
| `VehicleFinancePdfHandler` | Vehicle finance/loan details |

## PdfExportDialog

Platform-specific Compose dialog shown during PDF generation:
- Progress indicator
- Cancel option
- Result display (success with share/open, or error with retry)
