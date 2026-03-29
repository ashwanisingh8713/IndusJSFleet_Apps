# screen-finance

## Overview

**Package:** `com.ijs.finance`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Vehicle purchase and loan tracking — record vehicle purchases (cash or loan), track EMI payments, view loan summaries with progress, manage outstanding balances, and monitor upcoming/overdue EMI alerts.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `FinanceFeatureFacade`, `VehicleFinanceContract`, `VehicleFinanceViewModel`, `VehicleFinanceScreen`, `VehicleFinanceDetailScreen`, `AddPurchaseInfoScreen`, `EmiPaymentHistoryScreen` |
| **Domain** | `VehiclePurchase`, `LoanPayment`, `LoanSummary`, `EmiAlert`, `VehicleBasicInfo`, enums; `VehicleFinanceRepository` interface |
| **Data** | `VehicleFinanceRemoteDataSource`, `VehicleFinanceRepositoryImpl`, `VehicleFinanceMapper`, `VehicleFinanceDto` |

**Shared ViewModel pattern:** All 4 finance screens share a single `VehicleFinanceViewModel` instance, created via `rememberSharedViewModel("finance_$vehicleId")` in `sharedUI`.

---

## Dependencies

```
screen-finance → ijs-network-lib → ijs-core-lib
screen-finance → screen-vehicle (Vehicle entity for list display)
screen-finance → ijs-pdf-report, ijs-datetime-picker
```

**Cross-feature note:** Depends on `screen-vehicle` for `Vehicle` entity to display vehicle list with finance status overlay.

---

## Screens

### 1. VehicleFinanceScreen (List)

| Property | Value |
|----------|-------|
| Route | `FleetRoute.VehicleFinance` |
| ViewModel | `VehicleFinanceViewModel` (shared) |
| Contract | `VehicleFinanceContract` |

**Features:**
- All vehicles with finance status overlay (Financed, Cash, Not Recorded)
- Summary: Total vehicles, financed count, cash count, total loan, monthly EMI total, outstanding
- Filter chips: All, Financed, Cash, Pending
- Search by vehicle registration
- Upcoming EMI alerts, overdue alerts
- FAB to add new purchase info

---

### 2. VehicleFinanceDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.VehicleFinanceDetail(vehicleId)` |
| ViewModel | `VehicleFinanceViewModel` (shared) |

**Features:**
- Vehicle info header
- Purchase details: date, price, vendor, invoice, payment type
- Loan details (if financed): loan amount, interest rate, tenure, EMI, financier, bank info
- Loan progress bar with % completion
- Loan summary: total paid, outstanding, EMIs paid/remaining, next due date
- Actions: Edit purchase, View EMI history

---

### 3. AddPurchaseInfoScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.AddPurchaseInfo` / `FleetRoute.EditPurchaseInfo(id)` |
| ViewModel | `VehicleFinanceViewModel` (shared) |

**Features:**
- Vehicle selection dropdown
- Purchase info: date, price, vendor name, invoice number
- Payment type toggle: Cash / Loan
- Loan fields (conditional): down payment, interest rate, tenure months, loan start date, financier name, loan account, bank details, auto-debit toggle
- Auto-calculated: loan amount (price - down payment), EMI amount
- Notes field

---

### 4. EmiPaymentHistoryScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.EmiPaymentHistory(vehicleId)` |
| ViewModel | `VehicleFinanceViewModel` (shared) |

**Features:**
- EMI payment list with status (paid, pending, overdue)
- Payment details: amount, date, mode, transaction reference
- Record new EMI payment button
- Payment mode options: Cash, UPI, Bank Transfer, Cheque, Card

---

## Facade

```kotlin
object FinanceFeatureFacade {
    fun VehicleFinanceListEntry(viewModel, onNavigateBack, onNavigateToDetail, onNavigateToAddPurchase)
    fun VehicleFinanceDetailEntry(viewModel, vehicleId, onNavigateBack, onNavigateToEdit, onNavigateToHistory)
    fun AddPurchaseInfoEntry(viewModel, onNavigateBack)
    fun EmiPaymentHistoryEntry(viewModel, vehicleId, onNavigateBack)
}
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `VehiclePurchase` | Purchase record with loan details, progress, computed fields |
| `LoanPayment` | Individual EMI payment record |
| `LoanSummary` | Aggregated loan status (total paid, outstanding, EMIs) |
| `EmiAlert` | Upcoming or overdue EMI notifications |
| `VehicleBasicInfo` | Minimal vehicle info for display |
| `PaymentType` | Enum: CASH, LOAN |
| `PaymentMode` | Enum: CASH, UPI, BANK_TRANSFER, CHEQUE, CARD |
| `LoanStatus` | Enum: ACTIVE, COMPLETED, DEFAULTED, NOT_APPLICABLE |
| `FinanceFilter` | Enum: ALL, FINANCED, CASH, PENDING |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicle-finance` | GET | List all vehicle purchases |
| `/vehicle-finance` | POST | Record new purchase |
| `/vehicle-finance/{vehicleId}` | GET | Get purchase for vehicle |
| `/vehicle-finance/{vehicleId}` | PUT | Update purchase info |
| `/vehicle-finance/{vehicleId}/payments` | GET | EMI payment history |
| `/vehicle-finance/{vehicleId}/payments` | POST | Record EMI payment |
| `/vehicle-finance/{vehicleId}/summary` | GET | Loan summary |

