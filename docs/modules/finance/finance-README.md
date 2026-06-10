# Vehicle Finance Module

## Overview

The Vehicle Finance module provides comprehensive tracking of vehicle purchase information and loan/EMI payments. This module is accessible only to **Owner** and **General Manager** roles.

---

## Features

- **Purchase Information**: Track vehicle purchase details (date, price, vendor, payment type)
- **Loan Tracking**: Monitor loan details (financier, interest rate, tenure, EMI amount)
- **EMI Recording**: Record payments made to banks (netbanking, UPI, cash, auto-debit)
- **Loan Progress**: Visual progress bar showing EMIs paid vs remaining
- **Alerts**: Upcoming EMI and overdue EMI notifications
- **Financial Summary**: Fleet-wide finance overview

---

## User Access

| Role | Access |
|------|--------|
| Owner | ✅ Full Access |
| General Manager | ✅ Full Access |
| Manager | ❌ No Access |
| Supervisor | ❌ No Access |

---

## Screens

### 1. Vehicle Finance List
**Route:** `FleetRoute.VehicleFinance`

Main screen showing all vehicles with their finance status.

**Features:**
- Fleet finance summary (total vehicles, active loans, monthly EMI, outstanding)
- EMI alerts (upcoming and overdue)
- Filter by status (All, Loan, Cash, No Info)
- Search by vehicle registration
- Quick action to record EMI payment

### 2. Add Purchase Information
**Route:** `FleetRoute.AddPurchaseInfo`

Form to add purchase details for a vehicle.

**Fields:**
- Vehicle selection (only vehicles without purchase info)
- Purchase date (required)
- Purchase price (required)
- Vendor/Dealer name
- Invoice number
- Payment type (Cash or Loan)

**Loan-specific fields:**
- Down payment
- Interest rate (% p.a.)
- Tenure (months)
- EMI start date
- Financier/Bank name
- Loan account number
- Bank details for payment reference
- Auto-debit enabled flag

### 3. Vehicle Finance Detail
**Route:** `FleetRoute.VehicleFinanceDetail(vehicleId)`

Detailed view of vehicle's finance information.

**Sections:**
- Purchase Summary
- Loan Details (if financed)
- Loan Progress (visual + stats)
- Next EMI (with record button)
- Payment History (recent 3)
- Bank Details

### 4. EMI Payment History
**Route:** `FleetRoute.EmiPaymentHistory(vehicleId)`

Full payment history for a vehicle's loan.

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles/{id}/purchase` | POST | Add purchase info |
| `/vehicles/{id}/purchase` | GET | Get purchase details |
| `/vehicles/{id}/purchase` | PUT | Update purchase info |
| `/vehicles/{id}/loan-summary` | GET | Get loan summary |
| `/vehicles/{id}/loan-payments` | GET | Get EMI history |
| `/vehicle-loan-payments` | POST | Record EMI payment |
| `/vehicle-loan-payments/upcoming` | GET | Upcoming EMIs |
| `/vehicle-loan-payments/overdue` | GET | Overdue EMIs |
| `/vehicle-loan-payments/{id}/pay` | POST | Mark EMI as paid |

---

## Data Models

### VehiclePurchase
```kotlin
data class VehiclePurchase(
    val id: Int,
    val vehicleId: Int,
    val purchaseDate: String,
    val purchasePrice: Double,
    val vendorName: String?,
    val invoiceNumber: String?,
    val paymentType: PaymentType,  // CASH or LOAN
    val downPayment: Double,
    val loanAmount: Double,
    val interestRate: Double,
    val tenureMonths: Int,
    val emiAmount: Double,
    val loanStartDate: String?,
    val financierName: String?,
    val totalPaid: Double,
    val outstandingBalance: Double,
    val emisPaid: Int,
    val emisRemaining: Int,
    val nextEmiDueDate: String?,
    val loanStatus: LoanStatus  // NOT_APPLICABLE, ACTIVE, CLOSED, DEFAULTED
)
```

### LoanPayment
```kotlin
data class LoanPayment(
    val id: Int,
    val vehiclePurchaseId: Int,
    val emiNumber: Int?,
    val dueDate: String?,
    val amount: Double,
    val paymentDate: String?,
    val paymentMode: PaymentMode?,  // CASH, NETBANKING, UPI, AUTO_DEBIT, CHEQUE
    val paymentStatus: PaymentStatus,  // PENDING, PAID, OVERDUE
    val transactionRef: String?,
    val notes: String?
)
```

---

## File Structure

```
presentation/finance/
├── VehicleFinanceContract.kt      # MVI State, Intent, Effect
├── VehicleFinanceViewModel.kt     # Business logic
├── VehicleFinanceScreen.kt        # List screen
├── VehicleFinanceDetailScreen.kt  # Detail view
└── AddPurchaseInfoScreen.kt       # Add/Edit form

domain/
├── entity/finance/
│   ├── VehiclePurchase.kt
│   └── LoanPayment.kt
└── repository/finance/
    └── VehicleFinanceRepository.kt

data/
├── model/finance/
│   └── VehicleFinanceDto.kt
├── datasource/finance/
│   └── VehicleFinanceRemoteDataSource.kt
├── mapper/finance/
│   └── VehicleFinanceMapper.kt
└── repository/finance/
    └── VehicleFinanceRepositoryImpl.kt
```

---

## Navigation

**Hamburger Menu:**
```
☰ Menu
├── Vehicles
├── Drivers
├── Trips
├── Customers
├── Payments
├── Live Map
├── ──────────
├── Team Members
├── 🏦 Vehicle Finance  ← (Owner/GM only)
├── 📊 Reports & P/L    ← (Owner/GM only)
└── Profile & Settings
```

---

## Key Concepts

### Payment Types
| Type | Description |
|------|-------------|
| `cash` | Full payment at purchase |
| `loan` | Financed with EMI |

### Loan Status
| Status | Description |
|--------|-------------|
| `not_applicable` | Cash purchase |
| `active` | Loan in progress |
| `closed` | Loan fully paid |
| `defaulted` | Loan defaulted |

### Payment Modes
| Mode | Description |
|------|-------------|
| `netbanking` | Internet banking |
| `upi` | UPI transfer |
| `cash` | Cash deposit at bank |
| `auto_debit` | Bank auto-debit |
| `cheque` | Cheque payment |

---

## Best Practices

1. **EMI Recording**: This module only RECORDS payments made externally (via netbanking, UPI, cash at bank). It does NOT process actual payments.

2. **Required Fields**: When recording a payment, only `amount` and `payment_date` are required. Other fields are optional but help with tracking.

3. **Loan Progress**: Auto-calculated based on payments recorded. Updates `emis_paid`, `outstanding_balance`, and `total_paid` automatically.

4. **Alerts**: Upcoming EMIs (next 30 days) and overdue EMIs are shown in both the finance screen and can be integrated into the Dashboard alerts.

---

*Last Updated: January 2026*
