# ijs-pdf-report — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.pdfreport`

---

## 1. Purpose

Cross-platform PDF report generation using the Facade pattern. `PdfReportFacade` generates HTML templates and converts them to PDF via platform-specific renderers. Supports trip costs, driver costs, customer financials, vehicle P&L, payment receipts, and more.

## 2. Dependencies

- `ijs-datetime-utils` (api)
- Compose Multiplatform
- `kotlinx-coroutines-core`

## 3. Platform Renderers

| Platform | HTML→PDF Method |
|----------|----------------|
| Android | `WebView` + `PrintDocumentAdapter` + `FileProvider` |
| iOS | `UIMarkupTextPrintFormatter` + `UIPrintPageRenderer` |
| JS/WasmJS | Browser print API |

## 4. Key API

```kotlin
// Generate and share a PDF report
PdfReportFacade.generateReport(reportType, data) // → PdfExportResult
```

### Report Types
`TripCosts`, `DriverCosts`, `CustomerTrips`, `CustomerPayments`, `VehicleProfitLoss`, `FleetProfitLoss`, `CostAnalysis`, `PaymentReceipt`, `PaymentsList`

