# 05a — Trip Payments (Revenue Side)

Trip payments track **money received from customers** for trips. They sit on the **revenue side** of the ledger — the counterpart to operational costs (TC-*, MC-*). Payments are NOT costs; they represent income collection.

---

## 1. Payment Lifecycle

```
Customer places order
  → Trip created with trip_price (selling_value)
    → Driver completes trip
      → Owner records payment(s) against the trip
        ├── Payment 1 — Advance  (before trip)
        ├── Payment 2 — Partial  (during/after)
        └── Payment 3 — Final    (closes collection)

Trip Price = ₹1,00,000 (what customer owes)
  - Advance:  ₹30,000 (received)
  - Partial:  ₹40,000 (received)
  - Final:    ₹30,000 (received)   ← Trip fully paid
  ─────────────────────────────────
  Total Collected: ₹1,00,000
  Pending: ₹0
```

---

## 2. Payment Enumerations

### Payment Type — WHEN in the trip lifecycle

| Enum | API Value | Display | Icon | Description | Default Due Date |
|------|-----------|---------|------|-------------|-----------------|
| `ADVANCE` | `advance` | Advance | ⬆️ | Before trip starts | N/A |
| `PARTIAL` | `partial` | Partial | 📊 | During or after trip | Optional |
| `FINAL` | `final` | Final | ✅ | Closes the trip payment | N/A (clears due) |
| `REFUND` | `refund` | Refund | ↩️ | Cancellation/overpayment | N/A (clears due) |

**Default:** `PARTIAL`
**Source:** `screen-payment/.../domain/entity/PaymentEnums.kt`

### Payment Mode — HOW the payment was made

| Enum | API Value | Display | Icon |
|------|-----------|---------|------|
| `CASH` | `cash` | Cash | 💵 |
| `UPI` | `upi` | UPI | 📱 |
| `BANK_TRANSFER` | `bank_transfer` | Bank Transfer | 🏦 |
| `CARD` | `card` | Card | 💳 |
| `CREDIT` | `credit` | Credit | 📝 |

**Default:** `CASH`

### Payment Status — Collection state

| Enum | API Value | Display | Icon | Meaning |
|------|-----------|---------|------|---------|
| `RECEIVED` | `received` | Received | ✅ | Money in hand |
| `PENDING` | `pending` | Pending | ⏳ | Awaiting payment |
| `CANCELLED` | `cancelled` | Cancelled | ❌ | Voided/reversed |

**Default:** `PENDING`

---

## 3. Domain Entities

### TripPayment (Primary Entity)

```kotlin
data class TripPayment(
    val id: String,
    val tripId: String,
    val vehicleId: String?,
    val driverId: String?,
    val customerId: String?,
    val customerName: String?,
    val customerContact: String?,
    val customerCompany: String?,
    val customerGst: String?,
    // Amounts
    val amount: Double,           // Gross payment amount
    val tdsAmount: Double,        // Tax Deducted at Source
    val discountAmount: Double,   // Discount given
    val netAmount: Double,        // amount - tdsAmount - discountAmount
    // Classification
    val paymentType: PaymentType,
    val paymentMode: PaymentMode,
    val paymentSource: String?,   // e.g., "Company Account", "Personal"
    val paymentDate: String?,     // DD-MM-YYYY
    val paymentStatus: PaymentStatus,
    // Transaction tracking
    val transactionId: String?,
    val bankName: String?,
    val receiptNumber: String?,
    val financialYear: String?,   // e.g., "2025-26"
    val financialMonth: String?,  // e.g., "2025-12"
    val notes: String?,
    val receivedBy: String?,
    val receivedAtLocation: String?,
    // Audit
    val ownerId: String?,
    val createdById: String?,
    val createdByName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    // Embedded trip info (for list display)
    val tripInfo: TripPaymentTripInfo?
)
```

**Net Amount Formula:** `netAmount = amount − tdsAmount − discountAmount`

### TripPaymentTripInfo (Embedded Trip Context)

Provides trip context when displaying payments in lists:

| Field | Type | Description |
|-------|------|-------------|
| `vehicleRegistration` | `String?` | Vehicle reg number |
| `vehicleMake` / `vehicleModel` | `String?` | Vehicle details |
| `driverName` | `String?` | Assigned driver |
| `customerName` | `String?` | Customer name |
| `startLocation` / `endLocation` | `String?` | Route endpoints |
| `tripPrice` | `Double?` | Total trip selling value |
| `tripState` | `String?` | Trip state (planned, on_route, completed) |
| `tripStartDate` / `tripEndDate` | `String?` | DD-MM-YYYY format |
| `tripStartTime` / `tripEndTime` | `String?` | HH:mm format |

### TripPaymentSummary (Aggregated Stats)

```kotlin
data class TripPaymentSummary(
    val totalReceived: Double,     // Sum of RECEIVED payments
    val totalPending: Double,      // Sum of PENDING payments
    val totalCancelled: Double,    // Sum of CANCELLED payments
    val totalTds: Double,          // Total TDS collected
    val totalDiscount: Double,     // Total discounts given
    val totalNetAmount: Double,    // Total net amount
    val paymentCount: Int,         // Total payment records
    val receivedCount: Int,        // Count of received payments
    val pendingCount: Int,         // Count of pending payments
    val thisMonthTotal: Double,    // Current month's total
    val byMode: Map<PaymentMode, Double>,  // Breakdown by payment mode
    val byType: Map<PaymentType, Double>   // Breakdown by payment type
)
```

### PendingPaymentsSummary (Dashboard)

```kotlin
data class PendingPaymentsSummary(
    val totalPending: Double,   // Total pending across ALL trips
    val totalCount: Int         // Number of pending payment entries
)
```

---

## 4. Filtering

### TripPaymentFilter

| Field | Type | Description | UI Component |
|-------|------|-------------|--------------|
| `tripId` | `String?` | Filter by specific trip | Trip selector dialog |
| `customerId` | `String?` | Filter by customer | Customer selector |
| `paymentType` | `PaymentType?` | Filter by type (advance/partial/final/refund) | Chip group |
| `paymentMode` | `PaymentMode?` | Filter by mode (cash/upi/bank/card/credit) | Chip group |
| `paymentStatus` | `PaymentStatus?` | Filter by status (received/pending/cancelled) | Chip group |
| `startDate` | `String?` | Start of date range (DD-MM-YYYY) | Date picker |
| `endDate` | `String?` | End of date range (DD-MM-YYYY) | Date picker |
| `page` | `Int` | Pagination page (default: 1) | Auto-incremented |
| `perPage` | `Int` | Items per page (default: 20) | Fixed |

**`hasFilters` computed property:** Returns `true` if any filter field is non-null.

### Filter Flow

```
User taps Filter icon
  → Bottom sheet opens with filter options
  → tempFilter state updated during interaction
  → On "Apply" → filter = tempFilter, reload payments
  → On "Reset" → filter = TripPaymentFilter(), reload
```

---

## 5. PaymentsScreen Summary Cards

The payments list screen shows three summary cards at the top:

| Card | Value Source | Computation |
|------|-------------|-------------|
| **Total Received** | `summary.totalReceived` or sum of RECEIVED payments | Server-provided or client-computed |
| **Total Pending** | `pendingSummary.totalPending` (from Dashboard API) | Cross-trip aggregate |
| **This Month** | `summary.thisMonthTotal` | Server-provided |

**Amount formatting:**
- ≥ ₹1Cr → `₹X.XCr`
- ≥ ₹1L → `₹X.XL`
- < ₹1L → `₹X,XXX.XX` (Indian comma format)

---

## 6. How Payments Relate to P&L

| Context | Uses Payments? | Details |
|---------|---------------|---------|
| **Trip P&L** | ❌ No | Uses `trip_price` (selling_value) as revenue, NOT sum of collected payments |
| **Vehicle P&L** | ❌ No | Revenue = sum of trip_price for vehicle's trips |
| **Consolidated P&L** | ❌ No | Revenue = total trip_price fleet-wide |
| **Dashboard Cost Overview** | ❌ No | Shows revenue from trip_price |
| **Dashboard Pending Payments** | ✅ Yes | Shows outstanding payments as alerts |
| **Customer Detail** | ✅ Yes | Shows payment history, pending amounts |
| **TripProfitLossDto** | ⚠️ Tracking only | `payment_status` and `pending_amount` fields for collection tracking |

**Key distinction:** P&L uses `selling_value` (what was agreed), not `amount_collected` (what was received).

---

## 7. API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/payments` | Yes | Create payment |
| `GET` | `/payments/{id}` | Yes | Get payment detail |
| `PUT` | `/payments/{id}` | Yes | Update payment |
| `DELETE` | `/payments/{id}` | Yes (Owner) | Delete payment |
| `GET` | `/payments` | Yes | List payments (with filter params) |
| `GET` | `/dashboard/pending-payments` | Yes | Pending payments summary |

### Query Parameters for `GET /payments`

| Parameter | Type | Description |
|-----------|------|-------------|
| `trip_id` | Int | Filter by trip |
| `customer_id` | Int | Filter by customer |
| `payment_type` | String | advance/partial/final/refund |
| `payment_mode` | String | cash/upi/bank_transfer/card/credit |
| `payment_status` | String | received/pending/cancelled |
| `start_date` | String | Start date (DD-MM-YYYY) |
| `end_date` | String | End date (DD-MM-YYYY) |
| `page` | Int | Page number |
| `per_page` | Int | Items per page |

---

## 8. Role-Based Access

| Role | Can View | Can Create | Can Edit | Can Delete |
|------|----------|------------|----------|------------|
| **Owner** | ✅ All | ✅ | ✅ | ✅ |
| **General Manager** | ✅ All | ✅ | ✅ | ❌ |
| **Manager** | ✅ Own | ✅ | ❌ | ❌ |
| **Supervisor** | ✅ Limited | ❌ | ❌ | ❌ |

---

## 9. Source Files

| File | Module | Purpose |
|------|--------|---------|
| `PaymentEnums.kt` | `screen-payment` | Payment type/mode/status enums |
| `TripPayment.kt` | `screen-payment` | Domain entities (TripPayment, Summary, Filter) |
| `TripPaymentDto.kt` | `screen-payment` | API DTOs |
| `TripPaymentRequest.kt` | `screen-payment` | Create/update request DTOs |
| `TripPaymentMapper.kt` | `screen-payment` | DTO ↔ Entity mapping |
| `TripPaymentRemoteDataSource.kt` | `screen-payment` | API calls |
| `TripPaymentRepositoryImpl.kt` | `screen-payment` | Repository implementation |
| `PaymentsContract.kt` | `screen-payment` | MVI contract (State, Intent, Effect) |
| `PaymentsViewModel.kt` | `screen-payment` | List screen ViewModel |
| `AddPaymentViewModel.kt` | `screen-payment` | Add/Edit screen ViewModel |
| `PaymentDetailViewModel.kt` | `screen-payment` | Detail screen ViewModel |
| `PaymentFeatureFacade.kt` | `screen-payment` | Compose entry points |

