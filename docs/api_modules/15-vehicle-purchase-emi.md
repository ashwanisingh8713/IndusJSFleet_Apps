# Vehicle Purchase & EMI Tracking API

## Overview

Track vehicle purchases including loan/EMI information. Supports both cash and loan purchases with automatic EMI schedule generation and payment tracking.

**Access Control:** Only **Owner** and **General Manager** can access these endpoints.

---

## Tables

| Table | Description |
|-------|-------------|
| `vehicle_purchases` | Purchase + loan details (1:1 with vehicle) |
| `vehicle_loan_payments` | Individual EMI payment records |

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

## EMI Entry Types

| Value | Description |
|-------|-------------|
| `scheduled` | Auto-generated from loan tenure |
| `manual` | User recorded after paying via netbanking/bank |
| `app_payment` | Paid through app (future integration) |
| `auto_debit` | Bank auto-debited, user recording it |

---

## EMI Payment Status

| Value | Description |
|-------|-------------|
| `pending` | Scheduled EMI, not yet paid |
| `paid` | Payment completed |
| `overdue` | Past due date, not paid |
| `failed` | Payment attempt failed |
| `cancelled` | Payment cancelled |

---

## EMI Payment Modes

| Value | Description |
|-------|-------------|
| `cash` | Cash payment at bank |
| `netbanking` | Paid via net banking |
| `upi` | Paid via UPI |
| `auto_debit` | Bank auto-debit |
| `cheque` | Paid via cheque |
| `other` | Other payment mode |

---

## API Endpoints

### Vehicle Purchase APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/vehicles/:id/purchase` | Record purchase (cash/loan) |
| GET | `/vehicles/:id/purchase` | Get purchase details |
| PUT | `/vehicles/:id/purchase` | Update purchase |
| GET | `/vehicles/:id/loan-summary` | Get loan summary |
| GET | `/vehicles/:id/loan-payments` | Get vehicle's EMI history |

### Vehicle Loan Payment APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/vehicle-loan-payments` | Record EMI payment |
| GET | `/vehicle-loan-payments` | List all payments |
| GET | `/vehicle-loan-payments/:id` | Get payment details |
| PUT | `/vehicle-loan-payments/:id` | Update payment |
| DELETE | `/vehicle-loan-payments/:id` | Delete payment (Owner only) |
| GET | `/vehicle-loan-payments/upcoming` | Get upcoming EMIs |
| GET | `/vehicle-loan-payments/overdue` | Get overdue EMIs |
| POST | `/vehicle-loan-payments/:id/pay` | Mark scheduled EMI as paid |

---

## 1. Create Vehicle Purchase (Cash)

Record a cash purchase for a vehicle.

**Endpoint:** `POST /api/v2/vehicles/:id/purchase`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body

```json
{
    "purchase_date": "15-06-2025",
    "purchase_price": 1500000,
    "payment_type": "cash",
    "vendor_name": "Tata Motors Dealership",
    "invoice_number": "INV-2025-001",
    "down_payment": 1500000,
    "notes": "Full payment at purchase"
}
```

### Required Fields

| Field | Type | Description |
|-------|------|-------------|
| `purchase_date` | string | DD-MM-YYYY or ISO 8601 |
| `purchase_price` | number | Total purchase price |
| `payment_type` | string | `cash` |

### Response

```json
{
    "success": true,
    "message": "Vehicle purchase recorded successfully",
    "data": {
        "id": 1,
        "vehicle_id": 1,
        "purchase_date": "2025-06-15T00:00:00Z",
        "purchase_price": 1500000,
        "payment_type": "cash",
        "vendor_name": "Tata Motors Dealership",
        "invoice_number": "INV-2025-001",
        "down_payment": 1500000,
        "loan_status": "not_applicable",
        "total_paid": 1500000,
        "outstanding_balance": 0,
        "owner_id": 1,
        "created_by_user": {
            "id": 1,
            "first_name": "John",
            "last_name": "Owner",
            "role": "owner"
        }
    }
}
```

---

## 2. Create Vehicle Purchase (Loan)

Record a loan purchase for a vehicle with EMI details.

**Endpoint:** `POST /api/v2/vehicles/:id/purchase`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body

```json
{
    "purchase_date": "10-01-2025",
    "purchase_price": 2500000,
    "payment_type": "loan",
    "vendor_name": "Mahindra Showroom",
    "invoice_number": "INV-2025-002",
    "down_payment": 500000,
    "loan_amount": 2000000,
    "interest_rate": 9.5,
    "tenure_months": 60,
    "emi_amount": 42000,
    "loan_start_date": "10-02-2025",
    "financier_name": "HDFC Bank",
    "loan_account_number": "LOAN123456789",
    "bank_name": "HDFC Bank",
    "bank_account_number": "XXXX1234",
    "bank_ifsc": "HDFC0001234",
    "auto_debit_enabled": true,
    "notes": "60 month EMI plan"
}
```

### Required Fields for Loan

| Field | Type | Description |
|-------|------|-------------|
| `purchase_date` | string | DD-MM-YYYY or ISO 8601 |
| `purchase_price` | number | Total purchase price |
| `payment_type` | string | `loan` |
| `loan_amount` | number | Loan principal amount |
| `tenure_months` | number | Loan tenure in months |
| `emi_amount` | number | Monthly EMI amount |

### Optional Loan Fields

| Field | Type | Description |
|-------|------|-------------|
| `down_payment` | number | Down payment amount |
| `interest_rate` | number | Annual interest rate % |
| `loan_start_date` | string | First EMI due date (DD-MM-YYYY) |
| `financier_name` | string | Bank/NBFC name |
| `loan_account_number` | string | Loan account number |
| `bank_name` | string | Bank name for payments |
| `bank_account_number` | string | Bank account (masked) |
| `bank_ifsc` | string | Bank IFSC code |
| `auto_debit_enabled` | boolean | Auto-debit enabled |

### Auto-Generated EMI Schedule

When `loan_start_date` is provided, the system automatically generates pending EMI records with due dates for the entire loan tenure.

### Response

```json
{
    "success": true,
    "message": "Vehicle purchase recorded successfully",
    "data": {
        "id": 2,
        "vehicle_id": 2,
        "purchase_date": "2025-01-10T00:00:00Z",
        "purchase_price": 2500000,
        "payment_type": "loan",
        "down_payment": 500000,
        "loan_amount": 2000000,
        "interest_rate": 9.5,
        "tenure_months": 60,
        "emi_amount": 42000,
        "loan_start_date": "2025-02-10T00:00:00Z",
        "loan_end_date": "2030-02-10T00:00:00Z",
        "financier_name": "HDFC Bank",
        "loan_account_number": "LOAN123456789",
        "loan_status": "active",
        "total_paid": 500000,
        "outstanding_balance": 2000000,
        "emis_paid": 0,
        "emis_remaining": 60,
        "next_emi_due_date": "2025-02-10T00:00:00Z"
    }
}
```

---

## 3. Get Vehicle Purchase

Get purchase details for a vehicle.

**Endpoint:** `GET /api/v2/vehicles/:id/purchase`

**Authorization:** Bearer Token (Owner/GM only)

### Response

```json
{
    "success": true,
    "message": "Vehicle purchase retrieved",
    "data": {
        "id": 2,
        "vehicle_id": 2,
        "purchase_date": "2025-01-10T00:00:00Z",
        "purchase_price": 2500000,
        "payment_type": "loan",
        "vendor_name": "Mahindra Showroom",
        "down_payment": 500000,
        "loan_amount": 2000000,
        "interest_rate": 9.5,
        "tenure_months": 60,
        "emi_amount": 42000,
        "loan_start_date": "2025-02-10T00:00:00Z",
        "loan_end_date": "2030-02-10T00:00:00Z",
        "financier_name": "HDFC Bank",
        "loan_status": "active",
        "total_paid": 626000,
        "outstanding_balance": 1874000,
        "emis_paid": 3,
        "emis_remaining": 57,
        "next_emi_due_date": "2025-05-10T00:00:00Z",
        "vehicle": {
            "id": 2,
            "registration_number": "MH12AB5678",
            "make": "Mahindra",
            "model": "Blazo"
        }
    }
}
```

---

## 4. Update Vehicle Purchase

Update purchase details.

**Endpoint:** `PUT /api/v2/vehicles/:id/purchase`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body

```json
{
    "vendor_name": "Updated Vendor Name",
    "financier_name": "ICICI Bank",
    "notes": "Updated loan details"
}
```

---

## 5. Get Vehicle Loan Summary

Get comprehensive loan summary for a vehicle.

**Endpoint:** `GET /api/v2/vehicles/:id/loan-summary`

**Authorization:** Bearer Token (Owner/GM only)

### Response

```json
{
    "success": true,
    "message": "Loan summary retrieved",
    "data": {
        "vehicle_id": 2,
        "loan_amount": 2000000,
        "interest_rate": 9.5,
        "tenure_months": 60,
        "emi_amount": 42000,
        "loan_start_date": "2025-02-10T00:00:00Z",
        "loan_end_date": "2030-02-10T00:00:00Z",
        "financier": "HDFC Bank",
        "loan_account": "LOAN123456789",
        "loan_status": "active",
        "total_paid": 626000,
        "outstanding_balance": 1874000,
        "emis_paid": 3,
        "emis_remaining": 57,
        "emis_overdue": 0,
        "next_emi_due_date": "2025-05-10T00:00:00Z"
    }
}
```

---

## 6. Get Vehicle EMI History

Get EMI payment history for a specific vehicle.

**Endpoint:** `GET /api/v2/vehicles/:id/loan-payments`

**Authorization:** Bearer Token (Owner/GM only)

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | number | Page number (default: 1) |
| `per_page` | number | Items per page (default: 20) |
| `status` | string | Filter by status: `pending`, `paid`, `overdue` |

### Response

```json
{
    "success": true,
    "message": "Vehicle loan payments retrieved",
    "data": {
        "items": [
            {
                "id": 1,
                "vehicle_purchase_id": 2,
                "vehicle_id": 2,
                "emi_number": 1,
                "due_date": "2025-02-10T00:00:00Z",
                "amount": 42000,
                "payment_date": "2025-02-09T00:00:00Z",
                "principal_amount": 28000,
                "interest_amount": 14000,
                "entry_type": "manual",
                "payment_mode": "netbanking",
                "payment_status": "paid",
                "transaction_ref": "UTR123456789"
            }
        ],
        "count": 1,
        "total": 60,
        "page": 1,
        "per_page": 20,
        "total_pages": 3,
        "has_more": true
    }
}
```

---

## 7. Record EMI Payment (Minimal)

Record an EMI payment with minimal fields. Use this when user pays EMI via netbanking/bank and records in app.

**Endpoint:** `POST /api/v2/vehicle-loan-payments`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body (Minimal - Only 2 fields required!)

```json
{
    "vehicle_purchase_id": 1,
    "amount": 42000,
    "payment_date": "10-02-2026"
}
```

### Required Fields

| Field | Type | Description |
|-------|------|-------------|
| `vehicle_purchase_id` | number | ID of vehicle purchase |
| `amount` | number | Payment amount |
| `payment_date` | string | DD-MM-YYYY or ISO 8601 |

### Response

```json
{
    "success": true,
    "message": "EMI payment recorded successfully",
    "data": {
        "id": 4,
        "vehicle_purchase_id": 1,
        "vehicle_id": 2,
        "amount": 42000,
        "payment_date": "2026-02-10T00:00:00Z",
        "entry_type": "manual",
        "payment_status": "paid"
    }
}
```

---

## 8. Record EMI Payment (Full Details)

Record an EMI payment with all details.

**Endpoint:** `POST /api/v2/vehicle-loan-payments`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body

```json
{
    "vehicle_purchase_id": 1,
    "amount": 42500,
    "payment_date": "10-02-2026",
    "emi_number": 1,
    "principal_amount": 28000,
    "interest_amount": 14000,
    "late_fee": 500,
    "payment_mode": "netbanking",
    "payment_source": "HDFC NetBanking - Savings Account",
    "transaction_ref": "UTR123456789",
    "notes": "Paid with late fee"
}
```

### Optional Fields

| Field | Type | Description |
|-------|------|-------------|
| `emi_number` | number | EMI sequence number |
| `principal_amount` | number | Principal component |
| `interest_amount` | number | Interest component |
| `late_fee` | number | Late fee if any |
| `prepayment_amount` | number | Extra payment towards principal |
| `payment_mode` | string | cash, netbanking, upi, auto_debit, cheque |
| `payment_source` | string | Bank/UPI details |
| `transaction_ref` | string | UTR/Transaction ID |
| `notes` | string | User remarks |

---

## 9. Record Prepayment

Record an extra payment towards loan principal.

**Endpoint:** `POST /api/v2/vehicle-loan-payments`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body

```json
{
    "vehicle_purchase_id": 1,
    "amount": 100000,
    "payment_date": "15-03-2026",
    "prepayment_amount": 100000,
    "payment_mode": "netbanking",
    "notes": "Extra payment to reduce principal"
}
```

---

## 10. List All Loan Payments

List all loan payments across all vehicles.

**Endpoint:** `GET /api/v2/vehicle-loan-payments`

**Authorization:** Bearer Token (Owner/GM only)

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | number | Page number (default: 1) |
| `per_page` | number | Items per page (default: 20) |
| `vehicle_id` | number | Filter by vehicle |
| `vehicle_purchase_id` | number | Filter by purchase record |
| `status` | string | Filter by status: `pending`, `paid`, `overdue` |

### Response

```json
{
    "success": true,
    "message": "Loan payments retrieved",
    "data": {
        "items": [...],
        "count": 10,
        "total": 150,
        "page": 1,
        "per_page": 20,
        "total_pages": 8,
        "has_more": true
    }
}
```

---

## 11. Get Upcoming EMIs

Get EMIs due in the next N days (default 30).

**Endpoint:** `GET /api/v2/vehicle-loan-payments/upcoming`

**Authorization:** Bearer Token (Owner/GM only)

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `days` | number | Days to look ahead (default: 30) |

### Response

```json
{
    "success": true,
    "message": "Upcoming EMIs retrieved",
    "data": {
        "items": [
            {
                "id": 5,
                "vehicle_purchase_id": 2,
                "vehicle_id": 2,
                "emi_number": 4,
                "due_date": "2025-05-10T00:00:00Z",
                "amount": 42000,
                "payment_status": "pending",
                "vehicle": {
                    "registration_number": "MH12AB5678"
                }
            }
        ],
        "count": 3,
        "total_due": 126000,
        "date_range": {
            "from": "2026-01-31",
            "to": "2026-03-02"
        }
    }
}
```

---

## 12. Get Overdue EMIs

Get all overdue EMIs.

**Endpoint:** `GET /api/v2/vehicle-loan-payments/overdue`

**Authorization:** Bearer Token (Owner/GM only)

### Response

```json
{
    "success": true,
    "message": "Overdue EMIs retrieved",
    "data": {
        "items": [
            {
                "id": 6,
                "vehicle_purchase_id": 3,
                "vehicle_id": 3,
                "emi_number": 2,
                "due_date": "2025-12-10T00:00:00Z",
                "amount": 35000,
                "payment_status": "overdue",
                "vehicle": {
                    "registration_number": "MH14CD9012"
                }
            }
        ],
        "count": 1,
        "total_overdue": 35000
    }
}
```

---

## 13. Mark Scheduled EMI as Paid

Mark a scheduled (auto-generated) EMI as paid.

**Endpoint:** `POST /api/v2/vehicle-loan-payments/:id/pay`

**Authorization:** Bearer Token (Owner/GM only)

### Request Body

```json
{
    "payment_date": "10-02-2026",
    "payment_mode": "auto_debit",
    "transaction_ref": "AUTODRFT123456",
    "notes": "Bank auto-debited"
}
```

### Required Fields

| Field | Type | Description |
|-------|------|-------------|
| `payment_date` | string | DD-MM-YYYY or ISO 8601 |

### Optional Fields

| Field | Type | Description |
|-------|------|-------------|
| `amount` | number | Override EMI amount |
| `payment_mode` | string | Payment mode |
| `transaction_ref` | string | Transaction reference |
| `late_fee` | number | Late fee if any |
| `notes` | string | User remarks |

### Response

```json
{
    "success": true,
    "message": "EMI marked as paid",
    "data": {
        "id": 5,
        "vehicle_purchase_id": 2,
        "emi_number": 4,
        "amount": 42000,
        "payment_date": "2026-02-10T00:00:00Z",
        "entry_type": "manual",
        "payment_mode": "auto_debit",
        "payment_status": "paid",
        "transaction_ref": "AUTODRFT123456"
    }
}
```

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

### 3. Record Late Payment with Fee

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

### 4. Record Prepayment (Extra towards principal)

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

## Auto-Calculated Summary

When a payment is recorded, the `vehicle_purchases` table is automatically updated:

| Field | Calculation |
|-------|-------------|
| `total_paid` | Down payment + Sum of all paid EMIs |
| `outstanding_balance` | Loan amount - Sum of paid EMI amounts |
| `emis_paid` | Count of payments with status "paid" |
| `emis_remaining` | Tenure months - EMIs paid |
| `next_emi_due_date` | Next pending EMI's due date |
| `loan_status` | "closed" when outstanding balance <= 0 |

---

## Error Responses

### Purchase Already Exists

```json
{
    "success": false,
    "message": "Purchase record already exists",
    "error": "This vehicle already has a purchase record. Use PUT to update."
}
```

### Not a Loan Purchase

```json
{
    "success": false,
    "message": "No loan",
    "error": "This vehicle was purchased with cash, no loan details available"
}
```

### Missing Loan Details

```json
{
    "success": false,
    "message": "Missing loan details",
    "error": "loan_amount, tenure_months, and emi_amount are required for loan"
}
```

### Already Paid

```json
{
    "success": false,
    "message": "Already paid",
    "error": "This EMI has already been marked as paid"
}
```

---

## Notes

1. **Minimal Entry**: Only `amount` and `payment_date` required for manual EMI recording
2. **Auto Schedule**: When loan is created with start date, pending EMIs are auto-generated
3. **Summary Update**: When payment is recorded, vehicle_purchases summary is auto-updated
4. **Overdue Detection**: Pending EMIs past due date are automatically marked as overdue
5. **Future: App Payment**: Schema includes fields for future payment gateway integration
6. **Financial Access**: Only Owner and General Manager can manage purchase/EMI data

---

*Last Updated: January 2026*
