# 05 — Payments & Finance (Revenue + Capital)

> **⚠️ This document has been split into more detailed, focused docs:**
> - **[05a-trip-payments.md](05a-trip-payments.md)** — Trip payment lifecycle, enums, entities, filtering, API, role access
> - **[05b-vehicle-finance.md](05b-vehicle-finance.md)** — Vehicle purchase, loan/EMI tracking, entities, EMI calculation, alerts
> - **[07-profit-loss-analysis.md](07-profit-loss-analysis.md)** — Comprehensive P&L across Trip, Vehicle, Driver, Customer
>
> This file is kept for backward compatibility. Refer to the above docs for the latest details.

Trip Payments and Vehicle Finance (EMI/Loan) are **not operational cost types** — they sit on the **revenue** and **capital expenditure** side of the ledger respectively. This doc covers their enums, statuses, and how they relate to the cost system.

---

## 1. Trip Payments (Revenue Side)

Trip payments track **money received from customers** for completed/ongoing trips. They are the revenue counterpart to trip costs.

```
Trip Price (selling_value)  ← what the customer owes
  ├── Payment 1 (advance)
  ├── Payment 2 (partial)
  └── Payment 3 (final)     ← closes the trip payment
```

### Payment Type

Defines **when** in the trip lifecycle the payment is made.

| Enum | API Value | Display | Icon | Description |
|------|-----------|---------|------|-------------|
| `ADVANCE` | `advance` | Advance | ⬆️ | Before trip starts |
| `PARTIAL` | `partial` | Partial | 📊 | During or after trip |
| `FINAL` | `final` | Final | ✅ | Closes the trip payment |
| `REFUND` | `refund` | Refund | ↩️ | Cancellation / overpayment |

Default: `PARTIAL`

### Payment Mode

Defines **how** the payment was made.

| Enum | API Value | Display | Icon |
|------|-----------|---------|------|
| `CASH` | `cash` | Cash | 💵 |
| `UPI` | `upi` | UPI | 📱 |
| `BANK_TRANSFER` | `bank_transfer` | Bank Transfer | 🏦 |
| `CARD` | `card` | Card | 💳 |
| `CREDIT` | `credit` | Credit | 📝 |

Default: `CASH`

### Payment Status

| Enum | API Value | Display | Icon |
|------|-----------|---------|------|
| `RECEIVED` | `received` | Received | ✅ |
| `PENDING` | `pending` | Pending | ⏳ |
| `CANCELLED` | `cancelled` | Cancelled | ❌ |

Default: `PENDING`

### How Payments Relate to P&L

- **Trip P&L** uses `trip_price` (selling_value) as revenue — NOT the sum of payments received.
- `payment_status` and `pending_amount` are tracked in `TripProfitLossDto` for **collection tracking**, not P&L calculation.
- Dashboard shows `pending_payments` as an alert/summary, separate from cost overview.

### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/payments` | Yes | Create payment |
| `GET` | `/payments/{id}` | Yes | Payment detail |
| `PUT` | `/payments/{id}` | Yes | Update payment |
| `GET` | `/payments` | Yes | List payments (filterable) |

### Source File

`screen-payment/.../domain/entity/PaymentEnums.kt`

---

## 2. Vehicle Finance — EMI / Loan Payments (Capital Side)

Vehicle finance tracks **vehicle acquisition costs** — purchase price, down payment, and loan EMI payments. This is capital expenditure, NOT operational cost.

### Purchase Payment Type

| Enum | API Value | Display |
|------|-----------|---------|
| `CASH` | `cash` | Cash / Full Payment |
| `LOAN` | `loan` | Loan (EMI) |

Detection: `isFinanced = paymentType == PaymentType.LOAN`

### Loan Status

| Enum | API Value | Display |
|------|-----------|---------|
| `NOT_APPLICABLE` | `not_applicable` | Not Applicable |
| `ACTIVE` | `active` | Active |
| `CLOSED` | `closed` | Closed |
| `DEFAULTED` | `defaulted` | Defaulted |

### EMI Payment Status

| Enum | API Value | Display |
|------|-----------|---------|
| `PENDING` | `pending` | Pending |
| `PAID` | `paid` | Paid |
| `OVERDUE` | `overdue` | Overdue |
| `FAILED` | `failed` | Failed |
| `CANCELLED` | `cancelled` | Cancelled |

### EMI Payment Mode

| Enum | API Value | Display |
|------|-----------|---------|
| `CASH` | `cash` | Cash |
| `NETBANKING` | `netbanking` | Netbanking |
| `UPI` | `upi` | UPI |
| `AUTO_DEBIT` | `auto_debit` | Auto Debit |
| `CHEQUE` | `cheque` | Cheque |
| `OTHER` | `other` | Other |

### EMI Entry Type

How the payment record was created:

| Enum | API Value | Display |
|------|-----------|---------|
| `SCHEDULED` | `scheduled` | Scheduled |
| `MANUAL` | `manual` | Manual |
| `APP_PAYMENT` | `app_payment` | App Payment |
| `AUTO_DEBIT` | `auto_debit` | Auto Debit |

### Key Computed Fields (VehiclePurchase)

| Field | Formula |
|-------|---------|
| `totalInterest` | `(emiAmount × tenureMonths) − loanAmount` |
| `totalPayable` | `emiAmount × tenureMonths` |
| `loanProgressPercent` | `(emisPaid / tenureMonths) × 100` |
| `downPaymentPercent` | `(downPayment / purchasePrice) × 100` |

### Loan Summary (LoanSummary entity)

| Field | Description |
|-------|-------------|
| `loanAmount` | Total loan principal |
| `emiAmount` | Monthly EMI |
| `tenureMonths` | Loan tenure |
| `interestRate` | Annual interest rate |
| `totalPaid` | Sum of all paid EMIs |
| `outstandingBalance` | Remaining loan balance |
| `emisPaid` / `emisRemaining` | Tracking |
| `nextEmiDueDate` | Next payment due |

### EMI Alerts (EmiAlert entity)

| Field | Description |
|-------|-------------|
| `daysUntilDue` | Days until next EMI |
| `daysOverdue` | Days past due date |
| `severity` | `CRITICAL` (overdue), `WARNING` (≤3 days), `INFO` (>3 days) |

### How Finance Relates to P&L

- EMI / loan payments are **NOT included** in Vehicle P&L or Fleet P&L.
- They are capital expenditure — tracked separately in `screen-finance`.
- The `vehicle_trip_profit_loss_guide.md` in `Docs/cost_types/` discusses theoretical cost allocation (EMI per km, depreciation per trip) but the current system does not implement this allocation.

### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/vehicles/{vehicleId}/purchase` | Yes | Record purchase info |
| `GET` | `/vehicles/{vehicleId}/purchase` | Yes | Get purchase info |
| `PUT` | `/vehicles/{vehicleId}/purchase` | Yes | Update purchase info |
| `GET` | `/vehicles/{vehicleId}/loan-summary` | Yes | Loan summary |
| `GET` | `/vehicles/{vehicleId}/loan-payments` | Yes | EMI payment history |

### Source Files

| File | Module | Purpose |
|------|--------|---------|
| `PaymentEnums.kt` | `screen-payment` | Trip payment type/mode/status enums |
| `TripPayment.kt` | `screen-payment` | Trip payment domain entity |
| `TripPaymentDto.kt` | `screen-payment` | Trip payment DTO |
| `VehiclePurchase.kt` | `screen-finance` | Purchase + loan domain entities |
| `LoanPayment.kt` | `screen-finance` | EMI payment + loan summary + alert entities |

