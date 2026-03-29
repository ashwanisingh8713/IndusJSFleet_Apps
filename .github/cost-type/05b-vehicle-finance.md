# 05b — Vehicle Finance: EMI & Loan Payments (Capital Side)

Vehicle finance tracks **vehicle acquisition costs** — purchase price, down payment, and loan EMI payments. This is **capital expenditure (CapEx)**, completely separate from operational costs (TC-*, MC-*, DC-*) and revenue (payments).

---

## 1. Purchase & Finance Lifecycle

```
Owner purchases a vehicle
  ├── CASH purchase → Single payment, no tracking needed
  │     └── purchasePrice recorded, loanStatus = NOT_APPLICABLE
  │
  └── LOAN purchase → Down payment + EMI schedule
        ├── downPayment recorded
        ├── Loan created (amount, rate, tenure, financier)
        │     loanAmount = purchasePrice − downPayment
        │     emiAmount  = calculated EMI
        │     loanStatus = ACTIVE
        │
        └── Monthly EMI payments tracked
              ├── EMI #1  (paid ✅)
              ├── EMI #2  (paid ✅)
              ├── EMI #3  (overdue ⚠️)  ← alert generated
              ├── ...
              └── EMI #N  (pending ⏳)
              
              When all EMIs paid → loanStatus = CLOSED
```

---

## 2. Enumerations

### Purchase Payment Type

| Enum | API Value | Display | Implications |
|------|-----------|---------|-------------|
| `CASH` | `cash` | Cash / Full Payment | No loan tracking, loanStatus = NOT_APPLICABLE |
| `LOAN` | `loan` | Loan (EMI) | Full loan tracking, EMI schedule, alerts |

**Detection:** `isFinanced = paymentType == PaymentType.LOAN`

### Loan Status

| Enum | API Value | Display | When |
|------|-----------|---------|------|
| `NOT_APPLICABLE` | `not_applicable` | Not Applicable | Cash purchase, no loan |
| `ACTIVE` | `active` | Active | Loan in progress, EMIs being paid |
| `CLOSED` | `closed` | Closed | All EMIs paid, loan completed |
| `DEFAULTED` | `defaulted` | Defaulted | Borrower failed to pay |

### EMI Payment Status

| Enum | API Value | Display | Color |
|------|-----------|---------|-------|
| `PENDING` | `pending` | Pending | Yellow/Amber |
| `PAID` | `paid` | Paid | Green |
| `OVERDUE` | `overdue` | Overdue | Red |
| `FAILED` | `failed` | Failed | Dark Red |
| `CANCELLED` | `cancelled` | Cancelled | Grey |

### EMI Payment Mode

| Enum | API Value | Display |
|------|-----------|---------|
| `CASH` | `cash` | Cash |
| `NETBANKING` | `netbanking` | Netbanking |
| `UPI` | `upi` | UPI |
| `AUTO_DEBIT` | `auto_debit` | Auto Debit |
| `CHEQUE` | `cheque` | Cheque |
| `OTHER` | `other` | Other |

### EMI Entry Type (How the record was created)

| Enum | API Value | Display |
|------|-----------|---------|
| `SCHEDULED` | `scheduled` | Scheduled |
| `MANUAL` | `manual` | Manual |
| `APP_PAYMENT` | `app_payment` | App Payment |
| `AUTO_DEBIT` | `auto_debit` | Auto Debit |

---

## 3. Domain Entities

### VehiclePurchase (Primary Entity)

```kotlin
data class VehiclePurchase(
    val id: Int,
    val vehicleId: Int,
    val vehicle: VehicleBasicInfo?,
    // Purchase details
    val purchaseDate: String,        // DD-MM-YYYY
    val purchasePrice: Double,       // Total vehicle price
    val vendorName: String?,
    val invoiceNumber: String?,
    // Loan details (only if LOAN)
    val paymentType: PaymentType,    // CASH or LOAN
    val downPayment: Double,
    val loanAmount: Double,          // purchasePrice − downPayment
    val interestRate: Double,        // Annual interest rate %
    val tenureMonths: Int,           // Loan duration in months
    val emiAmount: Double,           // Monthly EMI
    val loanStartDate: String?,
    val loanEndDate: String?,
    // Financier details
    val financierName: String?,
    val loanAccountNumber: String?,
    val bankName: String?,
    val bankAccountNumber: String?,
    val bankIfsc: String?,
    val autoDebitEnabled: Boolean,
    // Progress tracking
    val totalPaid: Double,
    val outstandingBalance: Double,
    val emisPaid: Int,
    val emisRemaining: Int,
    val nextEmiDueDate: String?,
    val loanStatus: LoanStatus,
    val notes: String?,
    // Audit
    val ownerId: Int,
    val createdAt: String?,
    val updatedAt: String?
)
```

### Computed Fields on VehiclePurchase

| Field | Formula | Description |
|-------|---------|-------------|
| `isFinanced` | `paymentType == LOAN` | Is this a loan purchase? |
| `loanProgressPercent` | `(emisPaid / tenureMonths) × 100` | % of loan repaid |
| `totalInterest` | `(emiAmount × tenureMonths) − loanAmount` | Total interest over loan life |
| `totalPayable` | `emiAmount × tenureMonths` | Total amount to be paid (for loan) |
| `downPaymentPercent` | `(downPayment / purchasePrice) × 100` | Down payment as % of price |

### EMI Calculation (Live Preview in Form)

The Add Purchase form shows a live EMI preview using the standard amortization formula:

```
EMI = P × r × (1 + r)^n / ((1 + r)^n − 1)

Where:
  P = loanAmount (purchasePrice − downPayment)
  r = monthlyRate (annualRate / 12 / 100)
  n = tenureMonths

If interestRate = 0 → EMI = loanAmount / tenureMonths (simple division)
```

**Additional computed fields in form:**
- `calculatedLoanAmount = purchasePrice − downPayment`
- `calculatedTotalInterest = (EMI × tenureMonths) − loanAmount`
- `calculatedTotalPayable = EMI × tenureMonths`

### LoanPayment (EMI Entry)

```kotlin
data class LoanPayment(
    val id: Int,
    val vehiclePurchaseId: Int,
    val vehicleId: Int,
    val emiNumber: Int?,            // Sequential EMI number
    val dueDate: String?,           // When EMI was due
    val amount: Double,             // Payment amount
    val paymentDate: String?,       // When actually paid
    val principalAmount: Double,    // Principal component
    val interestAmount: Double,     // Interest component
    val lateFee: Double,            // Late fee if any
    val prepaymentAmount: Double,   // Extra prepayment
    val entryType: EntryType,
    val paymentMode: PaymentMode?,
    val paymentSource: String?,
    val paymentStatus: PaymentStatus,
    val transactionRef: String?,
    val notes: String?,
    // Audit
    val ownerId: Int,
    val createdAt: String?,
    val updatedAt: String?
)
```

### LoanSummary (Dashboard View)

```kotlin
data class LoanSummary(
    val vehicleId: Int,
    val vehiclePurchaseId: Int,
    val vehicle: VehicleBasicInfo?,
    val loanAmount: Double,
    val emiAmount: Double,
    val tenureMonths: Int,
    val interestRate: Double,
    val totalPaid: Double,
    val outstandingBalance: Double,
    val emisPaid: Int,
    val emisRemaining: Int,
    val nextEmiDueDate: String?,
    val nextEmiAmount: Double,
    val loanStatus: LoanStatus,
    val financierName: String?
)
```

### EmiAlert (Notification Entity)

| Field | Description |
|-------|-------------|
| `daysUntilDue` | Days until next EMI payment |
| `daysOverdue` | Days past the due date |
| `isOverdue` | `true` if past due |
| `severity` | Computed alert severity |

**Severity Levels:**

| Severity | Condition | Visual |
|----------|-----------|--------|
| `CRITICAL` | `isOverdue = true` | Red badge, immediate attention |
| `WARNING` | `daysUntilDue ≤ 3` | Yellow badge, pay soon |
| `INFO` | `daysUntilDue > 3` | Blue badge, upcoming |

---

## 4. Filtering (Finance List Screen)

### FinanceFilter

| Filter | Values | Description |
|--------|--------|-------------|
| `ALL` | All vehicles | Default — show everything |
| `LOAN` | Financed only | Vehicles with active loans |
| `CASH` | Cash purchases | Vehicles bought outright |
| `PENDING` | Not recorded | Vehicles without purchase info |

### Search

Text search across: `registrationNumber`, `make`, `model`

### Finance List Item Mapping

```
For each vehicle in fleet:
  purchase = vehiclePurchases[vehicle.id]
  status = when {
    purchase == null → PENDING (no purchase recorded)
    purchase.isFinanced → LOAN
    else → CASH
  }
```

---

## 5. Finance Summary Stats

The finance list screen shows aggregate stats:

| Stat | Description |
|------|-------------|
| `totalVehicles` | Total vehicles in fleet |
| `financedVehicles` | Count of vehicles with LOAN type |
| `cashVehicles` | Count of vehicles with CASH type |
| `pendingVehicles` | Count without purchase info |
| `totalLoanAmount` | Sum of all active loan amounts |
| `monthlyEmiTotal` | Sum of all active monthly EMIs |
| `totalPaid` | Sum of all EMIs paid so far |
| `totalOutstanding` | Sum of remaining loan balances |

---

## 6. How Finance Relates to P&L

| Context | Includes EMI/Loan? | Reason |
|---------|-------------------|--------|
| **Trip P&L** | ❌ No | Trip P&L = selling_value − trip_costs only |
| **Vehicle P&L** | ❌ No | Vehicle P&L = revenue − trip_costs − maintenance_costs |
| **Consolidated P&L** | ❌ No | Fleet P&L = total_revenue − total_expenses |
| **Dashboard Cost Overview** | ❌ No | Shows operational costs only |
| **Cost Analysis** | ❌ No | Analyzes TC-* and MC-* types only |

**Why excluded:** EMI/loan payments are **capital expenditure** — they represent the cost of acquiring an asset, not the cost of operating it. The industry standard is to track them separately from operational P&L.

> **Theoretical allocation (not implemented):** Some fleet operators allocate vehicle cost per-trip using depreciation: `cost_per_trip = purchasePrice / expectedTotalTrips`. This is documented in `Docs/cost_types/vehicle_trip_profit_loss_guide.md` but not implemented in the current system.

---

## 7. API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/vehicles/{vehicleId}/purchase` | Yes | Record purchase info |
| `GET` | `/vehicles/{vehicleId}/purchase` | Yes | Get purchase info |
| `PUT` | `/vehicles/{vehicleId}/purchase` | Yes | Update purchase info |
| `GET` | `/vehicles/{vehicleId}/loan-summary` | Yes | Get loan summary |
| `GET` | `/vehicles/{vehicleId}/loan-payments` | Yes | List EMI payments |
| `POST` | `/vehicles/{vehicleId}/loan-payments` | Yes | Record EMI payment |

---

## 8. Form Validation Rules

### Add Purchase Form

| Field | Required | Validation |
|-------|----------|------------|
| `vehicleId` | ✅ Yes | Must select a vehicle |
| `purchaseDate` | ✅ Yes | DD-MM-YYYY format |
| `purchasePrice` | ✅ Yes | Must be > 0 |
| `vendorName` | ❌ No | Free text |
| `invoiceNumber` | ❌ No | Free text |

### Loan-specific fields (required when `paymentType = LOAN`)

| Field | Required | Validation |
|-------|----------|------------|
| `downPayment` | ✅ Yes | Must be numeric |
| `interestRate` | ✅ Yes | Must be numeric |
| `tenureMonths` | ✅ Yes | Must be > 0 |
| `financierName` | ✅ Yes | Non-blank |
| `loanStartDate` | ✅ Yes | DD-MM-YYYY format |

### Record Payment Form

| Field | Required | Validation |
|-------|----------|------------|
| `paymentAmount` | ✅ Yes | Must be > 0 |
| `paymentDate` | ✅ Yes | Non-blank |
| `paymentMode` | ❌ No | Enum selection |
| `transactionRef` | ❌ No | Free text |

---

## 9. Source Files

| File | Module | Purpose |
|------|--------|---------|
| `VehiclePurchase.kt` | `screen-finance` | Purchase + loan domain entities + enums |
| `LoanPayment.kt` | `screen-finance` | EMI payment + loan summary + alert entities |
| `VehicleFinanceDto.kt` | `screen-finance` | API DTOs |
| `VehicleFinanceMapper.kt` | `screen-finance` | DTO ↔ Entity mapping |
| `VehicleFinanceRemoteDataSource.kt` | `screen-finance` | API calls |
| `VehicleFinanceRepositoryImpl.kt` | `screen-finance` | Repository implementation |
| `VehicleFinanceContract.kt` | `screen-finance` | MVI contract (State, Intent, Effect) |
| `VehicleFinanceViewModel.kt` | `screen-finance` | Shared ViewModel for all finance screens |
| `VehicleFinanceScreen.kt` | `screen-finance` | Finance list screen |
| `VehicleFinanceDetailScreen.kt` | `screen-finance` | Vehicle finance detail |
| `AddPurchaseInfoScreen.kt` | `screen-finance` | Add/edit purchase form |
| `EmiPaymentHistoryScreen.kt` | `screen-finance` | EMI payment history |
| `FinanceFeatureFacade.kt` | `screen-finance` | Compose entry points |

