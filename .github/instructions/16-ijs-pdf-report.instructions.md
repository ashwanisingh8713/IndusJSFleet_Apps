# ijs-pdf-report — IndusJS Fleet

## Purpose

PDF report generation via HTML→PDF conversion with platform-specific renderers.

## Package: `com.indusjs.pdfreport`

## Key Components

| Component | Purpose |
|-----------|---------|
| `PdfReportFacade` | Main entry — generates PDF for report type |
| `PdfExportDialog` | UI dialog for export options |
| `PdfGenerator` | Platform interface (expect/actual) |
| `AndroidPdfGenerator` | Android: WebView → PDF |
| `IosPdfGenerator` | iOS: WKWebView → PDF |
| `JsPdfGenerator` | JS: Browser print → PDF |
| `WasmPdfGenerator` | WASM: Browser print → PDF |

## Report Types

- Vehicle costs, Trip costs, Driver costs
- Profit/Loss statements (vehicle, trip, consolidated)
- Payment summaries, Customer financials

## Module Path

`ijs-pdf-report/src/commonMain/kotlin/com/indusjs/pdfreport/`

## Depends On: `ijs-datetime-utils`
## Depended On By: `sharedUI`

