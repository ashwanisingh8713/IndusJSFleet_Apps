# Vehicle Loan Payments Table

## Table: `vehicle_loan_payments`

Stores individual EMI payment records for vehicle loans. Supports both manual entry (user paid via netbanking/bank) and scheduled EMIs (auto-generated from loan).

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `vehicle_purchase_id` | `bigint` | NOT NULL, INDEX, FK → vehicle_purchases.id | Reference to purchase |
| `vehicle_id` | `bigint` | NOT NULL, INDEX | Denormalized vehicle reference |
| `emi_number` | `int` | | EMI sequence number (1, 2, 3...) |
| `due_date` | `timestamp` | | Scheduled due date |
| `amount` | `decimal(12,2)` | **NOT NULL** | Payment amount (**REQUIRED**) |
| `payment_date` | `timestamp` | **NOT NULL** | When payment was made (**REQUIRED**) |
| `principal_amount` | `decimal(12,2)` | | Principal component |
| `interest_amount` | `decimal(12,2)` | | Interest component |
| `late_fee` | `decimal(10,2)` | | Late fee if any |
| `prepayment_amount` | `decimal(12,2)` | | Extra payment towards principal |
| `entry_type` | `varchar(20)` | NOT NULL, DEFAULT 'manual' | How entry was created |
| `payment_mode` | `varchar(20)` | | Mode of payment |
| `payment_source` | `varchar(255)` | | Bank/UPI details |
| `payment_status` | `varchar(20)` | NOT NULL, DEFAULT 'paid' | Payment status |
| `transaction_ref` | `varchar(100)` | | UTR/Transaction ID |
| `payment_gateway_ref` | `varchar(100)` | | Gateway reference (future) |
| `payment_gateway_status` | `varchar(50)` | | Gateway status (future) |
| `payment_initiated_at` | `timestamp` | | App payment initiation time |
| `payment_completed_at` | `timestamp` | | App payment completion time |
| `notes` | `text` | | User remarks |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by_id` | `bigint` | NOT NULL | User who created record |
| `updated_by_id` | `bigint` | | User who last updated |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Required Fields for Manual Entry

Only **2 fields** are required when user records a manual payment:

| Field | Description |
|-------|-------------|
| `amount` | Payment amount |
| `payment_date` | When payment was made |

All other fields are optional and can be filled for better tracking.

---

## Entry Types

| Value | Description |
|-------|-------------|
| `scheduled` | Auto-generated from loan tenure |
| `manual` | User recorded after paying via netbanking/bank |
| `app_payment` | Paid through app (future integration) |
| `auto_debit` | Bank auto-debited, user recording it |

---

## Payment Status

| Value | Description |
|-------|-------------|
| `pending` | Scheduled EMI, not yet paid |
| `paid` | Payment completed |
| `overdue` | Past due date, not paid |
| `failed` | Payment attempt failed |
| `cancelled` | Payment cancelled |

---

## Payment Modes

| Value | Description |
|-------|-------------|
| `cash` | Cash payment at bank |
| `netbanking` | Paid via net banking |
| `upi` | Paid via UPI |
| `auto_debit` | Bank auto-debit |
| `cheque` | Paid via cheque |
| `other` | Other payment mode |

---

## Indexes

```sql
CREATE INDEX idx_vehicle_loan_payments_purchase_id ON vehicle_loan_payments(vehicle_purchase_id);
CREATE INDEX idx_vehicle_loan_payments_vehicle_id ON vehicle_loan_payments(vehicle_id);
CREATE INDEX idx_vehicle_loan_payments_owner_id ON vehicle_loan_payments(owner_id);
CREATE INDEX idx_vehicle_loan_payments_status ON vehicle_loan_payments(payment_status);
CREATE INDEX idx_vehicle_loan_payments_due_date ON vehicle_loan_payments(due_date);
CREATE INDEX idx_vehicle_loan_payments_deleted_at ON vehicle_loan_payments(deleted_at);
```

---

## Relationships

| Relation | Type | Target Table | Foreign Key |
|----------|------|--------------|-------------|
| VehiclePurchase | belongs_to | vehicle_purchases | vehicle_purchase_id |
| Vehicle | belongs_to | vehicles | vehicle_id |
| Owner | belongs_to | users | owner_id |
| CreatedBy | belongs_to | users | created_by_id |
| UpdatedBy | belongs_to | users | updated_by_id |

---

## Sample Data

### Minimal Manual Entry (User paid via netbanking)
```json
{
  "vehicle_purchase_id": 2,
  "amount": 42000.00,
  "payment_date": "2025-02-10T00:00:00Z"
}
```

### Full Manual Entry
```json
{
  "id": 1,
  "vehicle_purchase_id": 2,
  "vehicle_id": 2,
  "emi_number": 1,
  "due_date": "2025-02-10T00:00:00Z",
  "amount": 42000.00,
  "payment_date": "2025-02-09T00:00:00Z",
  "principal_amount": 28000.00,
  "interest_amount": 14000.00,
  "late_fee": 0,
  "entry_type": "manual",
  "payment_mode": "netbanking",
  "payment_source": "HDFC NetBanking - Savings Account",
  "payment_status": "paid",
  "transaction_ref": "UTR123456789",
  "notes": "Paid one day early",
  "owner_id": 1
}
```

### Scheduled EMI (Auto-generated)
```json
{
  "id": 5,
  "vehicle_purchase_id": 2,
  "vehicle_id": 2,
  "emi_number": 5,
  "due_date": "2025-06-10T00:00:00Z",
  "amount": 42000.00,
  "principal_amount": 29500.00,
  "interest_amount": 12500.00,
  "entry_type": "scheduled",
  "payment_status": "pending",
  "owner_id": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/vehicle-loan-payments` | Record EMI payment | Owner, GM |
| GET | `/api/v2/vehicle-loan-payments` | List all payments | Owner, GM |
| GET | `/api/v2/vehicle-loan-payments/:id` | Get payment details | Owner, GM |
| PUT | `/api/v2/vehicle-loan-payments/:id` | Update payment | Owner, GM |
| DELETE | `/api/v2/vehicle-loan-payments/:id` | Delete payment | Owner only |
| GET | `/api/v2/vehicles/:id/loan-payments` | Get vehicle's EMI history | Owner, GM |
| GET | `/api/v2/vehicle-loan-payments/upcoming` | Upcoming EMIs (next 30 days) | Owner, GM |
| GET | `/api/v2/vehicle-loan-payments/overdue` | Overdue EMIs | Owner, GM |
| POST | `/api/v2/vehicle-loan-payments/:id/pay` | Mark scheduled EMI as paid | Owner, GM |

---

## Use Cases

### 1. User Pays EMI via Netbanking
User logs into bank website, pays EMI, then records in app:
```json
POST /api/v2/vehicle-loan-payments
{
  "vehicle_purchase_id": 2,
  "amount": 42000,
  "payment_date": "10-02-2026"
}
```

### 2. User Pays at Bank Branch (Cash)
User deposits cash at bank, records in app:
```json
POST /api/v2/vehicle-loan-payments
{
  "vehicle_purchase_id": 2,
  "amount": 42000,
  "payment_date": "10-02-2026",
  "payment_mode": "cash",
  "notes": "Paid at HDFC Koramangala branch"
}
```

### 3. Mark Scheduled EMI as Paid
When auto-generated EMI is paid:
```json
POST /api/v2/vehicle-loan-payments/5/pay
{
  "payment_date": "10-02-2026",
  "payment_mode": "auto_debit",
  "transaction_ref": "UTR987654321"
}
```

### 4. Record Late Payment with Fee
```json
POST /api/v2/vehicle-loan-payments
{
  "vehicle_purchase_id": 2,
  "amount": 42500,
  "payment_date": "15-02-2026",
  "late_fee": 500,
  "notes": "Late payment - 5 days after due date"
}
```

### 5. Record Prepayment (Extra towards principal)
```json
POST /api/v2/vehicle-loan-payments
{
  "vehicle_purchase_id": 2,
  "amount": 100000,
  "payment_date": "10-02-2026",
  "prepayment_amount": 100000,
  "notes": "Extra payment to reduce principal"
}
```

---

## Filters

| Parameter | Description |
|-----------|-------------|
| `vehicle_id` | Filter by vehicle |
| `vehicle_purchase_id` | Filter by purchase record |
| `status` | Filter by payment status (pending, paid, overdue) |
| `days` | For upcoming EMIs - number of days to look ahead (default: 30) |

---

## Notes

1. **Minimal Entry**: Only `amount` and `payment_date` required for manual entries
2. **Auto Schedule**: When loan is created with start date, pending EMIs are auto-generated
3. **Summary Update**: When payment is recorded, vehicle_purchases summary is auto-updated
4. **Overdue Detection**: Pending EMIs past due date are automatically marked as overdue
5. **Future: App Payment**: Schema includes fields for future payment gateway integration
6. **Financial Access**: Only Owner and General Manager can manage loan payments

---

*Last Updated: January 2026*
