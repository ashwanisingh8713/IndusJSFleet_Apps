# Vehicle Purchases Table

## Table: `vehicle_purchases`

Stores vehicle purchase details including loan/EMI information for financed vehicles.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| `vehicle_id` | `bigint` | UNIQUE, NOT NULL, FK → vehicles.id | Reference to vehicle |
| `purchase_date` | `timestamp` | NOT NULL | Date of purchase |
| `purchase_price` | `decimal(14,2)` | NOT NULL | Total purchase price |
| `vendor_name` | `varchar(255)` | | Dealer/Vendor name |
| `invoice_number` | `varchar(100)` | | Purchase invoice number |
| `payment_type` | `varchar(20)` | NOT NULL, DEFAULT 'cash' | `cash` or `loan` |
| `down_payment` | `decimal(14,2)` | DEFAULT 0 | Down payment amount |
| `loan_amount` | `decimal(14,2)` | | Total loan principal |
| `interest_rate` | `decimal(5,2)` | | Annual interest rate % |
| `tenure_months` | `int` | | Loan tenure in months |
| `emi_amount` | `decimal(12,2)` | | Monthly EMI amount |
| `loan_start_date` | `timestamp` | | First EMI due date |
| `loan_end_date` | `timestamp` | | Last EMI due date |
| `financier_name` | `varchar(255)` | | Bank/NBFC name |
| `loan_account_number` | `varchar(100)` | | Loan account number |
| `bank_name` | `varchar(255)` | | Bank name for payments |
| `bank_account_number` | `varchar(100)` | | Bank account (masked) |
| `bank_ifsc` | `varchar(20)` | | Bank IFSC code |
| `auto_debit_enabled` | `boolean` | DEFAULT false | Auto-debit EMI enabled |
| `total_paid` | `decimal(14,2)` | DEFAULT 0 | Total amount paid (auto-calculated) |
| `outstanding_balance` | `decimal(14,2)` | DEFAULT 0 | Remaining loan amount (auto-calculated) |
| `emis_paid` | `int` | DEFAULT 0 | Number of EMIs paid (auto-calculated) |
| `emis_remaining` | `int` | DEFAULT 0 | Pending EMIs (auto-calculated) |
| `next_emi_due_date` | `timestamp` | | Next EMI due date (auto-calculated) |
| `loan_status` | `varchar(20)` | DEFAULT 'not_applicable' | Loan status |
| `notes` | `text` | | Additional notes |
| `owner_id` | `bigint` | NOT NULL, INDEX | Fleet owner |
| `created_by_id` | `bigint` | NOT NULL | User who created record |
| `updated_by_id` | `bigint` | | User who last updated |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Payment Types

| Value | Description |
|-------|-------------|
| `cash` | Full payment at purchase |
| `loan` | Financed with EMI |

---

## Loan Status

| Value | Description |
|-------|-------------|
| `not_applicable` | Cash purchase, no loan |
| `active` | Loan in progress, EMIs being paid |
| `closed` | Loan fully paid off |
| `defaulted` | Loan defaulted |

---

## Indexes

```sql
CREATE UNIQUE INDEX idx_vehicle_purchases_vehicle_id ON vehicle_purchases(vehicle_id);
CREATE INDEX idx_vehicle_purchases_owner_id ON vehicle_purchases(owner_id);
CREATE INDEX idx_vehicle_purchases_loan_status ON vehicle_purchases(loan_status);
CREATE INDEX idx_vehicle_purchases_deleted_at ON vehicle_purchases(deleted_at);
```

---

## Relationships

| Relation | Type | Target Table | Foreign Key |
|----------|------|--------------|-------------|
| Vehicle | belongs_to | vehicles | vehicle_id |
| Owner | belongs_to | users | owner_id |
| CreatedBy | belongs_to | users | created_by_id |
| UpdatedBy | belongs_to | users | updated_by_id |
| Payments | has_many | vehicle_loan_payments | vehicle_purchase_id |

---

## Sample Data

### Cash Purchase
```json
{
  "id": 1,
  "vehicle_id": 1,
  "purchase_date": "2025-06-15T00:00:00Z",
  "purchase_price": 1500000.00,
  "vendor_name": "Tata Motors Dealership",
  "invoice_number": "INV-2025-001",
  "payment_type": "cash",
  "down_payment": 1500000.00,
  "loan_status": "not_applicable",
  "total_paid": 1500000.00,
  "outstanding_balance": 0,
  "owner_id": 1
}
```

### Loan Purchase
```json
{
  "id": 2,
  "vehicle_id": 2,
  "purchase_date": "2025-01-10T00:00:00Z",
  "purchase_price": 2500000.00,
  "vendor_name": "Mahindra Showroom",
  "invoice_number": "INV-2025-002",
  "payment_type": "loan",
  "down_payment": 500000.00,
  "loan_amount": 2000000.00,
  "interest_rate": 9.5,
  "tenure_months": 60,
  "emi_amount": 42000.00,
  "loan_start_date": "2025-02-10T00:00:00Z",
  "loan_end_date": "2030-02-10T00:00:00Z",
  "financier_name": "HDFC Bank",
  "loan_account_number": "LOAN123456789",
  "loan_status": "active",
  "total_paid": 626000.00,
  "outstanding_balance": 1874000.00,
  "emis_paid": 3,
  "emis_remaining": 57,
  "next_emi_due_date": "2025-05-10T00:00:00Z",
  "owner_id": 1
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/vehicles/:id/purchase` | Record purchase | Owner, GM |
| GET | `/api/v2/vehicles/:id/purchase` | Get purchase details | Owner, GM |
| PUT | `/api/v2/vehicles/:id/purchase` | Update purchase | Owner, GM |
| GET | `/api/v2/vehicles/:id/loan-summary` | Get loan summary | Owner, GM |
| GET | `/api/v2/vehicles/:id/loan-payments` | Get vehicle's EMI history | Owner, GM |

---

## Auto-Calculated Fields

When a payment is recorded:
1. `total_paid` = Down payment + Sum of all paid EMIs
2. `outstanding_balance` = Loan amount - Sum of paid EMI amounts
3. `emis_paid` = Count of payments with status "paid"
4. `emis_remaining` = Tenure months - EMIs paid
5. `next_emi_due_date` = Next pending EMI's due date
6. `loan_status` = "closed" when outstanding balance <= 0

---

## Notes

1. **One-to-One with Vehicle**: Each vehicle can have only one purchase record
2. **Auto EMI Generation**: When loan with start date is created, pending EMI records are auto-generated
3. **Summary Auto-Update**: Purchase summary fields are automatically updated when payments are recorded
4. **Financial Access**: Only Owner and General Manager can view/manage purchase records
5. **Soft Delete**: Records are soft-deleted, preserving history

---

*Last Updated: January 2026*
