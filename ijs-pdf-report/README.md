# ijs-pdf-report

Cross-platform PDF Report generation library for IndusJS Fleet application.

## Overview

This module provides a unified API for generating, saving, and sharing PDF reports across Android, iOS, JS, and WasmJS platforms using the Facade design pattern.

## Features

- **Facade Pattern**: Single entry point via `PdfReportFacade`
- **HTML-based PDF Generation**: Consistent rendering across platforms
- **Platform-specific implementations**:
  - Android: WebView with PrintDocumentAdapter
  - iOS: WKWebView with UIPrintPageRenderer
  - JS/WasmJS: jsPDF library
- **Async with Coroutines**: Non-blocking PDF generation
- **Save/Share/Open**: Built-in file operations

## Supported Report Types

| Report Type | Data Class | Handler | Description |
|-------------|------------|---------|-------------|
| Trip Costs | `TripCostsPdfData` | `TripCostsPdfHandler` | Trip costs breakdown report |
| Customer Trips | `CustomerTripsPdfData` | `CustomerTripsPdfHandler` | Customer's trips summary |
| Customer Payments | `CustomerPaymentsPdfData` | `CustomerPaymentsPdfHandler` | Customer's payments list |
| Customer Financials | `CustomerFinancialsPdfData` | `CustomerFinancialsPdfHandler` | Customer financial report |
| Payments List | `PaymentsListPdfData` | `PaymentsListPdfHandler` | All payments list report |
| Payment Receipt | `PaymentReceiptPdfData` | `PaymentReceiptPdfHandler` | Individual payment receipt |
| Vehicle Finance | `VehicleFinancePdfData` | `VehicleFinancePdfHandler` | Vehicle purchase & EMI report |
| Fleet P&L | `FleetProfitLossPdfData` | `FleetProfitLossPdfHandler` | Fleet-wide profit/loss |
| Vehicle P&L | `VehicleProfitLossPdfData` | `VehicleProfitLossPdfHandler` | Per-vehicle profit/loss |
| Cost Analysis | `CostAnalysisPdfData` | `CostAnalysisPdfHandler` | Cost breakdown by type |

## Usage

### Option 1: Using Composable Handlers (Recommended for UI)

The library provides ready-to-use composable handlers that manage the entire export flow:

```kotlin
// In your composable screen
@Composable
fun TripDetailScreen() {
    var pdfData by remember { mutableStateOf<TripCostsPdfData?>(null) }

    // Handler automatically generates PDF and shows dialog
    TripCostsPdfHandler(
        pdfData = pdfData,
        onExportComplete = { pdfData = null },
        onExportError = { error -> showSnackbar(error) }
    )

    // Trigger export
    Button(onClick = { pdfData = createTripCostsPdfData() }) {
        Text("Export PDF")
    }
}
```

Available handlers:
- `TripCostsPdfHandler` - Trip costs breakdown
- `CustomerTripsPdfHandler` - Customer's trips
- `CustomerPaymentsPdfHandler` - Customer's payments
- `CustomerFinancialsPdfHandler` - Customer financials
- `PaymentsListPdfHandler` - All payments list
- `PaymentReceiptPdfHandler` - Payment receipt
- `VehicleFinancePdfHandler` - Vehicle purchase & EMI
- `FleetProfitLossPdfHandler` - Fleet-wide P&L
- `VehicleProfitLossPdfHandler` - Per-vehicle P&L
- `CostAnalysisPdfHandler` - Cost breakdown analysis

### Option 2: Using Facade Directly (For custom flow)

```kotlin
// Generate Trip Costs PDF
val pdfData = TripCostsPdfData(
    tripId = 1,
    vehicleNumber = "AB12CD34",
    costs = listOf(...),
    generatedAt = FleetDateTime.formatDisplayDateTime12Hour(FleetDateTime.now())
)

val result = PdfReportFacade.generateTripCostsReport(pdfData)

if (result.success) {
    // Show dialog with Open/Share options
    PdfReportFacade.shareReport(result)
}
```

## PDF Filename Format

| Screen | Report Type | Filename Format | Example |
|--------|-------------|-----------------|---------|
| Trip Details | Trip Costs | `TripCosts_{VehicleNumber}_Trip{Id}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `TripCosts_AB12CD34_Trip123_05-Feb-2026_10-30-AM.pdf` |
| Customer Details - Trips Tab | Customer Trips | `{CustomerName}_Trips_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `ABCCorp_Trips_05-Feb-2026_10-30-AM.pdf` |
| Customer Details - Payments Tab | Customer Payments | `{CustomerName}_Payments_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `ABCCorp_Payments_05-Feb-2026_10-30-AM.pdf` |
| Customer Details - Financials Tab | Customer Financials | `{CustomerName}_Financials_{Period}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `ABCCorp_Financials_Jan2026_05-Feb-2026_10-30-AM.pdf` |
| Payments Screen | Payments List | `PaymentsList_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `PaymentsList_05-Feb-2026_10-30-AM.pdf` |
| Payment Details | Payment Receipt | `Receipt_{ReceiptNumber}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `Receipt_RP-202602-001_05-Feb-2026_10-30-AM.pdf` |
| Finance Details | Vehicle Finance | `{VehicleNumber}_Finance_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `AB12CD34_Finance_05-Feb-2026_10-30-AM.pdf` |
| Fleet P&L Overview | Fleet P&L | `FleetPL_{Period}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `FleetPL_Jan2026_05-Feb-2026_10-30-AM.pdf` |
| Vehicle P&L | Vehicle P&L | `{VehicleNumber}_PL_{Period}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `AB12CD34_PL_Jan2026_05-Feb-2026_10-30-AM.pdf` |
| Cost Analysis | Cost Analysis | `CostAnalysis_{CostType}_{DD-MMM-YYYY}_{hh-mm-AM-PM}.pdf` | `CostAnalysis_Fuel_05-Feb-2026_10-30-AM.pdf` |

### Filename Conventions

- **Customer Names**: Spaces removed, special characters replaced with underscore
- **Vehicle Numbers**: Used as-is from registration
- **Date Format**: DD-MMM-YYYY (e.g., 05-Feb-2026)
- **Time Format**: hh-mm-AM/PM (e.g., 10-30-AM)
- **Period Format**: MonthYear (e.g., Jan2026) or Custom date range

## Storage Locations

| Platform | Location |
|----------|----------|
| Android | App's external files directory: `/Android/data/{packageName}/files/Documents/exportedPdf/` |
| iOS | App Documents directory |
| JS/WasmJS | Browser download folder |

## Dependencies

- `ijs-datetime-utils` - Date/Time formatting
- Compose Multiplatform - UI components
- kotlinx-coroutines - Async operations
