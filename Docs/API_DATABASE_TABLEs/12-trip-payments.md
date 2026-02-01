# Trip Payments Table

## Table: `trip_payments` (Renamed from `payments`)

Comprehensive payment tracking for trips with detailed financial reporting support. Tracks multiple partial payments per trip with full customer and transaction details.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| **Identity** |
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| **Trip & Vehicle Reference** |
| `trip_id` | `bigint` | NOT NULL, INDEX, FK | Associated trip |
| `vehicle_id` | `bigint` | NOT NULL, INDEX, FK | Associated vehicle |
| `driver_id` | `bigint` | INDEX, FK | Driver at time of payment |
| **Customer Reference** |
| `customer_id` | `bigint` | INDEX, FK | Reference to Customer entity |
| `customer_name` | `varchar(255)` | | Customer name (snapshot) |
| `customer_contact` | `varchar(20)` | | Customer phone (snapshot) |
| `customer_company` | `varchar(255)` | | Company name (snapshot) |
| `customer_gst` | `varchar(20)` | | GST number (for invoicing) |
| **Payment Amount Details** |
| `amount` | `decimal(12,2)` | NOT NULL | Payment amount (₹) |
| `tds_amount` | `decimal(12,2)` | DEFAULT 0 | TDS deducted (₹) |
| `discount_amount` | `decimal(12,2)` | DEFAULT 0 | Discount given (₹) |
| `net_amount` | `decimal(12,2)` | NOT NULL | Net received = amount - tds - discount |
| **Payment Type & Mode** |
| `payment_type` | `varchar(20)` | NOT NULL | advance/partial/final/refund |
| `payment_mode` | `varchar(20)` | NOT NULL | cash/upi/bank_transfer/card |
| `payment_source` | `varchar(100)` | | Mode-specific source identifier |
| `payment_status` | `varchar(20)` | NOT NULL, DEFAULT 'received' | received/pending/cancelled |
| **Date & Time** |
| `payment_date` | `timestamp` | NOT NULL | Date of payment |
| `due_date` | `timestamp` | | Expected payment date (for tracking) |
| **Transaction Details** |
| `transaction_id` | `varchar(100)` | INDEX | UPI/NEFT/RTGS transaction ID |
| `reference_number` | `varchar(100)` | | Internal reference number |
| `receipt_number` | `varchar(50)` | UNIQUE | Payment receipt number |
| **Bank Details** |
| `bank_name` | `varchar(100)` | | Bank name |
| `bank_branch` | `varchar(100)` | | Bank branch |
| `account_number` | `varchar(50)` | | Account number (masked) |
| `ifsc_code` | `varchar(20)` | | IFSC code |
| **Invoice Reference** |
| `invoice_number` | `varchar(50)` | INDEX | Related invoice number |
| `invoice_date` | `timestamp` | | Invoice date |
| `invoice_amount` | `decimal(12,2)` | | Original invoice amount |
| **Receipt Details** |
| `received_by` | `varchar(100)` | | Person who received |
| `received_at_location` | `varchar(255)` | | Location of payment receipt |
| **Financial Period (Auto-calculated)** |
| `financial_year` | `varchar(10)` | INDEX | FY (e.g., "2025-26") |
| `financial_month` | `varchar(7)` | INDEX | Month (e.g., "2026-01") |
| `financial_quarter` | `varchar(10)` | INDEX | Quarter (e.g., "Q4-2025-26") |
| **Notes & Remarks** |
| `notes` | `text` | | Payment notes |
| `remarks` | `text` | | Internal remarks |
| **Ownership & Audit** |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by` | `bigint` | INDEX | User who recorded |
| `updated_by` | `bigint` | | User who last updated |
| `verified_by` | `bigint` | | User who verified |
| `verified_at` | `timestamp` | | Verification timestamp |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Payment Types

| Type | Description | Use Case |
|------|-------------|----------|
| `advance` | Advance/booking payment | Before trip starts |
| `partial` | Partial payment | During or after trip |
| `final` | Final settlement | Closes the trip payment |
| `refund` | Refund to customer | Cancellation/overpayment |

---

## Payment Modes

| Mode | Description | Required Fields | `payment_source` Example |
|------|-------------|-----------------|--------------------------|
| `cash` | Cash payment | - | `"Received by: Driver Ramesh"` |
| `upi` | UPI transaction | `transaction_id` | `"user@paytm"` or `"9876543210@upi"` |
| `bank_transfer` | NEFT/RTGS/IMPS | `transaction_id`, `bank_name` | `"XXXX1234 - HDFC Bank"` |
| `card` | Credit/Debit card | `transaction_id` | `"XXXX-4567 (Visa Credit)"` |
| `credit` | On credit/account | `due_date` | `"Net 30 days"` |
| `other` | Other methods | `notes` | `"Wallet transfer"` |

**Note:** `payment_source` stores mode-specific identifiers for tracking and reconciliation. Card numbers and account numbers should be masked (only last 4 digits).

---

## Payment Status

| Status | Description | Trip Impact |
|--------|-------------|-------------|
| `received` | Payment confirmed | Updates `paid_trip_price` |
| `pending` | Awaiting confirmation | No update until confirmed |
| `cancelled` | Payment cancelled | Reverses from `paid_trip_price` |

---

## Indexes

```sql
-- Primary indexes
CREATE INDEX idx_trip_payments_trip_id ON trip_payments(trip_id);
CREATE INDEX idx_trip_payments_vehicle_id ON trip_payments(vehicle_id);
CREATE INDEX idx_trip_payments_customer_id ON trip_payments(customer_id);
CREATE INDEX idx_trip_payments_owner_id ON trip_payments(owner_id);

-- Financial reporting indexes
CREATE INDEX idx_trip_payments_payment_date ON trip_payments(payment_date);
CREATE INDEX idx_trip_payments_financial_year ON trip_payments(financial_year);
CREATE INDEX idx_trip_payments_financial_month ON trip_payments(financial_month);
CREATE INDEX idx_trip_payments_financial_quarter ON trip_payments(financial_quarter);

-- Transaction tracking indexes
CREATE INDEX idx_trip_payments_transaction_id ON trip_payments(transaction_id);
CREATE INDEX idx_trip_payments_invoice_number ON trip_payments(invoice_number);
CREATE UNIQUE INDEX idx_trip_payments_receipt_number ON trip_payments(receipt_number);

-- Status and type indexes
CREATE INDEX idx_trip_payments_payment_status ON trip_payments(payment_status);
CREATE INDEX idx_trip_payments_payment_type ON trip_payments(payment_type);
CREATE INDEX idx_trip_payments_payment_mode ON trip_payments(payment_mode);

-- Soft delete
CREATE INDEX idx_trip_payments_deleted_at ON trip_payments(deleted_at);
```

---

## Financial Period Auto-Calculation

```go
// Auto-set financial period fields on payment creation
func (p *TripPayment) SetFinancialPeriod() {
    paymentDate := p.PaymentDate
    
    // Financial Year (April to March in India)
    year := paymentDate.Year()
    month := int(paymentDate.Month())
    if month < 4 {
        p.FinancialYear = fmt.Sprintf("%d-%02d", year-1, year%100)
    } else {
        p.FinancialYear = fmt.Sprintf("%d-%02d", year, (year+1)%100)
    }
    
    // Financial Month
    p.FinancialMonth = paymentDate.Format("2006-01")
    
    // Financial Quarter
    var quarter string
    switch {
    case month >= 4 && month <= 6:
        quarter = "Q1"
    case month >= 7 && month <= 9:
        quarter = "Q2"
    case month >= 10 && month <= 12:
        quarter = "Q3"
    default:
        quarter = "Q4"
    }
    p.FinancialQuarter = fmt.Sprintf("%s-%s", quarter, p.FinancialYear)
}
```

---

## Receipt Number Generation

```go
// Format: RP-{YYYYMM}-{OwnerID}-{Sequence}
// Example: RP-202601-001-00042
func GenerateReceiptNumber(ownerID uint, paymentDate time.Time) string {
    sequence := GetNextSequence(ownerID, paymentDate.Format("200601"))
    return fmt.Sprintf("RP-%s-%03d-%05d", 
        paymentDate.Format("200601"), 
        ownerID, 
        sequence)
}
```

---

## Sample Data

### Advance Payment (Cash)
```json
{
  "id": 1,
  "trip_id": 1,
  "vehicle_id": 1,
  "driver_id": 1,
  "customer_id": 1,
  "customer_name": "ABC Construction Pvt Ltd",
  "customer_contact": "9876543210",
  "customer_company": "ABC Construction",
  "customer_gst": "27AABCU9603R1ZM",
  "amount": 30000.00,
  "tds_amount": 0,
  "discount_amount": 0,
  "net_amount": 30000.00,
  "payment_type": "advance",
  "payment_mode": "cash",
  "payment_source": "Received by: Driver Ramesh",
  "payment_status": "received",
  "payment_date": "2026-01-15T10:00:00Z",
  "receipt_number": "RP-202601-001-00001",
  "received_by": "Ramesh Singh (Driver)",
  "received_at_location": "Customer Office, Pune",
  "financial_year": "2025-26",
  "financial_month": "2026-01",
  "financial_quarter": "Q4-2025-26",
  "notes": "Advance payment for trip booking",
  "owner_id": 1,
  "created_by": 2
}
```

### Partial Payment (Bank Transfer with TDS)
```json
{
  "id": 2,
  "trip_id": 1,
  "vehicle_id": 1,
  "customer_id": 1,
  "customer_name": "ABC Construction Pvt Ltd",
  "customer_company": "ABC Construction",
  "customer_gst": "27AABCU9603R1ZM",
  "amount": 40000.00,
  "tds_amount": 400.00,
  "discount_amount": 0,
  "net_amount": 39600.00,
  "payment_type": "partial",
  "payment_mode": "bank_transfer",
  "payment_source": "XXXX1234 - Union Bank",
  "payment_status": "received",
  "payment_date": "2026-01-18T14:30:00Z",
  "transaction_id": "UBIN202601180012345",
  "reference_number": "REF-001-002",
  "receipt_number": "RP-202601-001-00002",
  "bank_name": "Union Bank of India",
  "bank_branch": "Pune Main",
  "account_number": "XXXX1234",
  "invoice_number": "INV-2026-00001",
  "invoice_date": "2026-01-15T00:00:00Z",
  "invoice_amount": 100000.00,
  "financial_year": "2025-26",
  "financial_month": "2026-01",
  "financial_quarter": "Q4-2025-26",
  "notes": "Second payment - 1% TDS deducted",
  "owner_id": 1,
  "created_by": 1
}
```

### Final Payment (Card)
```json
{
  "id": 3,
  "trip_id": 1,
  "vehicle_id": 1,
  "customer_id": 1,
  "customer_name": "ABC Construction Pvt Ltd",
  "customer_company": "ABC Construction",
  "amount": 30000.00,
  "tds_amount": 300.00,
  "discount_amount": 0,
  "net_amount": 29700.00,
  "payment_type": "final",
  "payment_mode": "card",
  "payment_source": "XXXX-4567 (Visa Credit)",
  "payment_status": "received",
  "payment_date": "2026-01-20T11:00:00Z",
  "transaction_id": "CARD202601200001",
  "bank_name": "HDFC Bank",
  "receipt_number": "RP-202601-001-00003",
  "financial_year": "2025-26",
  "financial_month": "2026-01",
  "financial_quarter": "Q4-2025-26",
  "notes": "Final payment via card",
  "owner_id": 1,
  "created_by": 1
}
```

### Refund Payment
```json
{
  "id": 4,
  "trip_id": 2,
  "vehicle_id": 1,
  "customer_id": 2,
  "customer_name": "XYZ Builders",
  "amount": 5000.00,
  "tds_amount": 0,
  "discount_amount": 0,
  "net_amount": 5000.00,
  "payment_type": "refund",
  "payment_mode": "bank_transfer",
  "payment_status": "received",
  "payment_date": "2026-01-19T10:00:00Z",
  "transaction_id": "REF202601190001",
  "receipt_number": "RP-202601-001-00004",
  "bank_name": "ICICI Bank",
  "notes": "Refund for trip cancellation",
  "owner_id": 1,
  "created_by": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| **CRUD Operations** |
| POST | `/api/v2/trip-payments` | Record payment | Owner, GM |
| GET | `/api/v2/trip-payments` | List all payments | Owner, GM |
| GET | `/api/v2/trip-payments/:id` | Get payment details | Owner, GM |
| PUT | `/api/v2/trip-payments/:id` | Update payment | Owner, GM |
| DELETE | `/api/v2/trip-payments/:id` | Cancel payment | Owner |
| **Trip-specific** |
| GET | `/api/v2/trips/:id/payments` | Get trip's payments | Owner, GM |
| POST | `/api/v2/trips/:id/payments` | Add payment to trip | Owner, GM |
| **Financial Reports** |
| GET | `/api/v2/trip-payments/summary` | Payment summary | Owner, GM |
| GET | `/api/v2/trip-payments/by-customer/:id` | Customer payments | Owner, GM |
| GET | `/api/v2/trip-payments/by-period` | Payments by period | Owner, GM |
| GET | `/api/v2/trip-payments/cash-flow` | Cash flow report | Owner, GM |
| GET | `/api/v2/trip-payments/tds-report` | TDS summary | Owner, GM |

---

## Trip Payment Summary Response

```go
type TripPaymentSummary struct {
    TripID          uint    `json:"trip_id"`
    ExpectedAmount  float64 `json:"expected_amount"`
    TotalReceived   float64 `json:"total_received"`
    TotalTDS        float64 `json:"total_tds"`
    TotalDiscount   float64 `json:"total_discount"`
    NetReceived     float64 `json:"net_received"`
    PendingAmount   float64 `json:"pending_amount"`
    PaymentCount    int     `json:"payment_count"`
    LastPaymentDate string  `json:"last_payment_date"`
    PaymentStatus   string  `json:"payment_status"`
}
```

### Response Example
```json
{
  "trip_id": 1,
  "expected_amount": 100000.00,
  "total_received": 100000.00,
  "total_tds": 700.00,
  "total_discount": 0,
  "net_received": 99300.00,
  "pending_amount": 700.00,
  "payment_count": 3,
  "last_payment_date": "2026-01-20T11:00:00Z",
  "payment_status": "full"
}
```

---

## Customer-wise Receivables

```go
type CustomerReceivables struct {
    CustomerID      uint    `json:"customer_id"`
    CustomerName    string  `json:"customer_name"`
    CustomerCompany string  `json:"customer_company"`
    TripCount       int     `json:"trip_count"`
    TotalBilled     float64 `json:"total_billed"`
    TotalReceived   float64 `json:"total_received"`
    TotalTDS        float64 `json:"total_tds"`
    PendingAmount   float64 `json:"pending_amount"`
    OldestPending   string  `json:"oldest_pending_date"`
    DaysOverdue     int     `json:"max_days_overdue"`
}
```

### Customer Receivables Query
```sql
SELECT 
    tp.customer_id,
    tp.customer_name,
    tp.customer_company,
    COUNT(DISTINCT tp.trip_id) as trip_count,
    SUM(tp.amount) as total_billed,
    SUM(CASE WHEN tp.payment_status = 'received' THEN tp.net_amount ELSE 0 END) as total_received,
    SUM(tp.tds_amount) as total_tds,
    SUM(CASE WHEN tp.payment_status = 'pending' THEN tp.amount ELSE 0 END) as pending_amount,
    MIN(CASE WHEN tp.payment_status = 'pending' THEN tp.payment_date END) as oldest_pending
FROM trip_payments tp
WHERE tp.owner_id = ?
  AND tp.deleted_at IS NULL
GROUP BY tp.customer_id, tp.customer_name, tp.customer_company
HAVING SUM(CASE WHEN tp.payment_status = 'pending' THEN tp.amount ELSE 0 END) > 0
ORDER BY pending_amount DESC;
```

---


## Cash Flow Report

```go
type CashFlowReport struct {
    Period      string             `json:"period"`
    Inflows     map[string]float64 `json:"inflows"`
    TotalInflow float64            `json:"total_inflow"`
    Outflows    float64            `json:"outflows"`
    TDSDeducted float64            `json:"tds_deducted"`
    NetCashFlow float64            `json:"net_cash_flow"`
}
```

### Cash Flow Query
```sql
SELECT 
    financial_month as period,
    payment_mode,
    SUM(CASE WHEN payment_type != 'refund' AND payment_status = 'received' 
        THEN net_amount ELSE 0 END) as inflow,
    SUM(CASE WHEN payment_type = 'refund' AND payment_status = 'received' 
        THEN net_amount ELSE 0 END) as outflow,
    SUM(tds_amount) as tds_deducted
FROM trip_payments
WHERE owner_id = ?
  AND payment_date BETWEEN ? AND ?
  AND deleted_at IS NULL
GROUP BY financial_month, payment_mode
ORDER BY financial_month, payment_mode;
```

---

## TDS Report

```go
type TDSReport struct {
    FinancialYear   string       `json:"financial_year"`
    TotalTDS        float64      `json:"total_tds"`
    CustomerWiseTDS []CustomerTDS `json:"customer_wise_tds"`
}

type CustomerTDS struct {
    CustomerID   uint    `json:"customer_id"`
    CustomerName string  `json:"customer_name"`
    CustomerGST  string  `json:"customer_gst"`
    TotalPaid    float64 `json:"total_paid"`
    TDSDeducted  float64 `json:"tds_deducted"`
    TDSPercent   float64 `json:"tds_percent"`
}
```

### TDS Query
```sql
SELECT 
    tp.customer_id,
    tp.customer_name,
    tp.customer_gst,
    SUM(tp.amount) as total_paid,
    SUM(tp.tds_amount) as tds_deducted,
    ROUND(SUM(tp.tds_amount) * 100.0 / NULLIF(SUM(tp.amount), 0), 2) as tds_percent
FROM trip_payments tp
WHERE tp.owner_id = ?
  AND tp.financial_year = ?
  AND tp.tds_amount > 0
  AND tp.payment_status = 'received'
  AND tp.deleted_at IS NULL
GROUP BY tp.customer_id, tp.customer_name, tp.customer_gst
ORDER BY tds_deducted DESC;
```

---

## Trip Integration

When a payment is recorded, the trip is automatically updated:

```go
// UpdateTripPaymentSummary recalculates trip payment totals
func UpdateTripPaymentSummary(db *gorm.DB, tripID uint) error {
    // Get all successful payments
    var result struct {
        TotalReceived float64
        TotalTDS      float64
        TotalDiscount float64
        NetReceived   float64
        PaymentCount  int64
    }
    
    db.Model(&TripPayment{}).
        Select(`
            COALESCE(SUM(amount), 0) as total_received,
            COALESCE(SUM(tds_amount), 0) as total_tds,
            COALESCE(SUM(discount_amount), 0) as total_discount,
            COALESCE(SUM(CASE WHEN payment_status = 'received' THEN net_amount ELSE 0 END), 0) as net_received,
            COUNT(CASE WHEN payment_status = 'received' THEN 1 END) as payment_count
        `).
        Where("trip_id = ? AND deleted_at IS NULL", tripID).
        Scan(&result)
    
    // Update trip
    var trip Trip
    db.First(&trip, tripID)
    
    updates := map[string]interface{}{
        "paid_trip_price": result.NetReceived,
        "payment_count":   result.PaymentCount,
    }
    
    // Determine payment status
    if result.NetReceived >= trip.ExpectedTripPrice && trip.ExpectedTripPrice > 0 {
        updates["payment_status"] = "full"
        updates["is_full_payment_done"] = true
        updates["is_payment_pending"] = false
    } else if result.NetReceived > 0 {
        updates["payment_status"] = "partial"
        updates["is_full_payment_done"] = false
        updates["is_payment_pending"] = true
    } else {
        updates["payment_status"] = "pending"
        updates["is_full_payment_done"] = false
        updates["is_payment_pending"] = true
    }
    
    return db.Model(&trip).Updates(updates).Error
}
```

---

## Payment Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    TRIP PAYMENT LIFECYCLE                       │
└─────────────────────────────────────────────────────────────────┘

TRIP CREATED (expected_trip_price = ₹1,00,000)
    │  paid_trip_price = 0
    │  payment_count = 0
    │  payment_status = "pending"
    ▼
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

ADVANCE PAYMENT (₹30,000 Cash)
    │  POST /api/v2/trip-payments
    │  { trip_id: 1, amount: 30000, payment_type: "advance",
    │    payment_mode: "cash", payment_status: "received" }
    │
    │  → Creates trip_payments record
    │  → Auto-updates trip:
    │      paid_trip_price = 30,000
    │      payment_count = 1
    │      payment_status = "partial"
    ▼
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

PARTIAL PAYMENT (₹40,000 Bank Transfer with 1% TDS)
    │  POST /api/v2/trip-payments
    │  { trip_id: 1, amount: 40000, tds_amount: 400,
    │    net_amount: 39600, payment_type: "partial",
    │    payment_mode: "bank_transfer", transaction_id: "NEFT123" }
    │
    │  → paid_trip_price = 69,600
    │  → payment_count = 2
    │  → payment_status = "partial"
    ▼
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

FINAL PAYMENT (₹30,000 Card with 1% TDS)
    │  POST /api/v2/trip-payments
    │  { trip_id: 1, amount: 30000, tds_amount: 300,
    │    net_amount: 29700, payment_type: "final",
    │    payment_mode: "card", transaction_id: "CARD123456" }
    │
    │  → paid_trip_price = 99,300
    │  → payment_count = 3
    │  → payment_status = "full" (99,300 ≥ 1,00,000 threshold)
    │  → is_full_payment_done = true
    ▼
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

TRIP FULLY PAID ✓
    Final Summary:
    - Total Received: ₹1,00,000
    - TDS Deducted: ₹700
    - Net Received: ₹99,300
    - Payment Count: 3
```
---

## Dashboard Integration

Payment data appears in financial dashboard:

```json
{
  "financial_summary": {
    "period": "2026-01",
    "total_receivables": 1250000.00,
    "collected_this_month": 3500000.00,
    "tds_deducted": 35000.00,
    "net_collected": 3465000.00,
    "pending_payments_count": 15,
    "refunds_this_month": 25000.00
  },
  "collection_by_mode": {
    "cash": 450000,
    "upi": 850000,
    "bank_transfer": 1500000,
    "card": 700000
  },
  "pending_payments": [
    {
      "trip_id": 25,
      "customer_name": "ABC Construction",
      "customer_company": "ABC Construction Pvt Ltd",
      "pending_amount": 75000.00,
      "days_overdue": 15
    }
  ]
}
```

---

## Financial Reports Integration

### Monthly Collection Report
```json
{
  "report_type": "monthly_collection",
  "period": "2026-01",
  "data": {
    "opening_receivables": 500000,
    "new_billings": 3000000,
    "collections": {
      "cash": 450000,
      "upi": 850000,
      "bank_transfer": 1500000,
      "card": 665000
    },
    "tds_deducted": 34650,
    "discounts_given": 15000,
    "refunds": 25000,
    "closing_receivables": 425000
  }
}
```

### Customer Aging Report
```json
{
  "report_type": "customer_aging",
  "as_of_date": "2026-01-15",
  "aging_buckets": {
    "current": 250000,
    "1_30_days": 150000,
    "31_60_days": 75000,
    "61_90_days": 25000,
    "over_90_days": 10000
  },
  "total_outstanding": 510000
}
```

---

## Notes

1. **Customer Snapshot**: Store customer details at payment time for historical accuracy
2. **TDS Tracking**: Track TDS deductions for Form 26AS reconciliation and certificate issuance
3. **Receipt Numbers**: Unique, sequential receipt numbers for accounting compliance
4. **Financial Periods**: Auto-calculated based on Indian Financial Year (April-March)
5. **Audit Trail**: Full tracking of who created/updated/verified each payment
6. **GST Integration**: Store customer GST for future invoice generation
7. **Refund Handling**: Negative cash flow tracked separately for reconciliation
8. **Multi-currency**: Future scope - add currency field for international customers
9. **Bank Reconciliation**: Match payments with bank statements using transaction_id

---

## Further Considerations

### 1. Invoice Generation
- Auto-generate invoice when trip is completed
- Link invoice to payments via `invoice_number`
- Support GST-compliant invoice format

### 2. Payment Reminders
- Automated reminders for pending payments
- Configurable reminder schedule (7, 15, 30 days)
- SMS/Email/Push notification channels

### 3. Payment Gateway Integration
- Support online payment collection
- Auto-reconcile with payment gateway
- Store gateway reference in `transaction_id`

### 4. Credit Limit Management
- Track customer credit limits
- Alert on credit limit breach
- Block new trips for overdue customers

### 5. Accounting Integration
- Export to Tally/QuickBooks format
- Chart of accounts mapping
- Journal entry generation

### 6. Revenue Calculation Standards
- **Current State**: `selling_value` is used in some places for revenue calculations (legacy)
- **Recommended Standard**: Use `expected_trip_price` as the primary revenue field
- **Field Clarification**:
  - `expected_trip_price`: The quoted/agreed price with customer (use for revenue)
  - `selling_value`: Legacy field for cargo selling value (keep for backward compatibility)
  - `purchase_price`: Cost of cargo purchase (use for margin calculations)
- **Migration Path**:
  - Update all revenue queries to use `expected_trip_price`
  - Keep `selling_value` for cargo-specific calculations only
  - Document clear usage in API responses

### 7. Database Column Cleanup
- **Deprecated Columns** (to be removed in future migration):
  - `partial_payment_amount` → Replaced by `paid_trip_price` (calculated from trip_payments)
  - `pending_amount` → Calculated as `expected_trip_price - paid_trip_price`
- **Affected Files** (Updated):
  - `controllers/report_controller.go` - ✅ Fixed to use `paid_trip_price`
  - `controllers/dashboard_controller.go` - ✅ Fixed pending payments count
  - `controllers/customer_controller.go` - Customer pending payments
- **Migration Steps**:
  1. ✅ Update all queries to use new column names
  2. Add migration to calculate `paid_trip_price` from `trip_payments` table
  3. Remove deprecated columns after verification
  4. Update documentation and API schemas

### 8. Testing Checklist
After fixing deprecated column references, verify:
- [x] `GET /api/v2/dashboard/pending-payments` - Returns pending payments list
- [ ] `GET /api/v2/customers/:id/pending-payments` - Returns customer's pending payments
- [x] `GET /api/v2/dashboard/cost-overview` - Revenue calculations correct
- [ ] `GET /api/v2/reports/profit-loss` - Financial summaries accurate
- [x] `GET /api/v2/dashboard/financial-summary` - All amounts calculated correctly
- [ ] Trip payment creation auto-updates `paid_trip_price` on trip
- [ ] Payment cancellation correctly reverses `paid_trip_price`

---

*Last Updated: January 2026*
