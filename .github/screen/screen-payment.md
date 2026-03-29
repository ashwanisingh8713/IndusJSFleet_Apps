# screen-payment

## Overview

**Package:** `com.ijs.payment`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Trip payment management — record payments from customers for trips, track payment status (received, pending, cancelled), support multiple payment modes (cash, UPI, bank transfer, cheque, card), edit/delete payments, filter by date/status/mode, and export payment reports as PDF.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `PaymentFeatureFacade`, `PaymentsContract`/`PaymentsViewModel`/`PaymentsScreen` (list), `AddPaymentViewModel`/`AddPaymentScreen`, `PaymentDetailViewModel`/`PaymentDetailScreen` |
| **Domain** | `TripPayment`, `PaymentStatus`, `PaymentMode`, `PaymentType`, `TripPaymentSummary`, `TripPaymentFilter`, `PendingPaymentsSummary`; `TripPaymentRepository`, `TripProviderForPayment` |
| **Data** | `TripPaymentRemoteDataSource`, `TripPaymentRepositoryImpl`, `TripPaymentMapper`, `TripPaymentDto`, `TripPaymentRequest` |

---

## Dependencies

```
screen-payment → ijs-network-lib → ijs-core-lib
screen-payment → screen-customer (CustomerSummary for payment context)
screen-payment → ijs-pdf-report, ijs-datetime-picker
```

**Cross-feature note:** Depends on `screen-customer` for `CustomerSummary` entity used in payment forms to show/select customer info.

---

## Screens

### 1. PaymentsScreen (List)

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Payments` |
| ViewModel | `PaymentsViewModel` |
| Contract | `PaymentsContract` |

**Features:**
- Summary header: Total Received, Total Pending, This Month amounts
- Payment cards with trip info, customer, amount, mode icon, status badge
- Filter bottom sheet: date range, payment status, payment mode
- Pagination with load-more
- Delete payment with confirmation dialog
- PDF export of filtered payments
- FAB for quick add payment

**State highlights:**
- `payments: List<TripPayment>` — paginated list
- `summary: TripPaymentSummary?` — aggregate amounts
- `pendingSummary: PendingPaymentsSummary?` — from dashboard API
- `filter: TripPaymentFilter` — active filters (date, status, mode)
- Computed: `totalReceived`, `totalPending`, `thisMonth` with Indian currency formatting (₹, L, Cr)

**Key Intents:** `LoadPayments`, `RefreshPayments`, `LoadMore`, `ApplyFilter`, `ClearFilters`, `DeletePayment`, `ExportPdf`

---

### 2. AddPaymentScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.AddPayment(tripId?, vehicleId?)` / `FleetRoute.EditPayment(paymentId)` |
| ViewModel | `AddPaymentViewModel` |

**Features:**
- Trip selection (auto-populated if `tripId` provided)
- Customer info display (from trip data)
- Payment fields: amount, TDS amount, discount, net amount (auto-calculated)
- Payment type: Full / Partial
- Payment mode: Cash, UPI, Bank Transfer, Cheque, Card
- Payment source, transaction ID, bank name, receipt number
- Payment date, financial year/month
- Notes, received by, received at location
- **Edit mode:** Pre-populates form when `paymentId` is provided

---

### 3. PaymentDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.PaymentDetail(paymentId)` |
| ViewModel | `PaymentDetailViewModel` |

**Features:**
- Payment amount with status badge
- Trip info section (route, vehicle, driver)
- Customer info section (company, contact, GST)
- Payment details: mode, type, source, transaction ID, bank, receipt
- Financial period info
- Edit / Delete actions
- PDF export single payment receipt

---

## Facade

```kotlin
object PaymentFeatureFacade {
    fun PaymentsListEntry(viewModel, onNavigateBack, onNavigateToDetail, onNavigateToAddPayment)
    fun AddPaymentEntry(viewModel, tripId?, paymentId?, onNavigateBack)
    fun PaymentDetailEntry(viewModel, paymentId, onNavigateBack, onNavigateToEdit)
}
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `TripPayment` | Full payment record with trip/customer info, computed display fields |
| `TripPaymentTripInfo` | Embedded trip data (route, vehicle, driver) |
| `TripPaymentSummary` | Aggregate: total received, pending, this month |
| `TripPaymentFilter` | Filter state: date range, status, mode |
| `PendingPaymentsSummary` | Outstanding payments overview |
| `PaymentStatus` | Enum: RECEIVED, PENDING, CANCELLED |
| `PaymentMode` | Enum: CASH, UPI, BANK_TRANSFER, CHEQUE, CARD (each with icon + display name) |
| `PaymentType` | Enum: FULL, PARTIAL |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/payments` | GET | List payments (paginated, filterable) |
| `/payments` | POST | Create payment |
| `/payments/{id}` | GET | Get payment details |
| `/payments/{id}` | PUT | Update payment |
| `/payments/{id}` | DELETE | Delete payment |
| `/payments/summary` | GET | Payment summary statistics |

---

## Inter-Module Communication

- Uses `TripProviderForPayment` interface for cross-feature trip data access without direct trip module dependency at the domain level.
- `CustomerSummary` from `screen-customer` for customer display in payment forms.

