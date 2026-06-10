# 08. Trip Payments
Trip payment management endpoints for recording and tracking payments with full financial reporting support.
## Date & Time Formats
- **DateTime**: ISO 8601 format `2026-01-15T14:30:00Z`
- All dates in responses are ISO 8601
## Role Permissions
| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| View Payments | ✅ | ✅ | ❌ | ❌ |
| Record Payment | ✅ | ✅ | ❌ | ❌ |
| Update Payment | ✅ | ✅ | ❌ | ❌ |
| Delete Payment | ✅ | ❌ | ❌ | ❌ |
| View Reports | ✅ | ✅ | ❌ | ❌ |
## Payment Types
| Type | Description |
|------|-------------|
| `advance` | Advance/booking payment before trip |
| `partial` | Partial payment during/after trip |
| `final` | Final settlement payment |
| `refund` | Refund to customer |
## Payment Modes
| Mode | Required Fields | `payment_source` Example |
|------|-----------------|--------------------------|
| `cash` | - | `"Received by: Driver Ramesh"` |
| `upi` | `transaction_id` | `"user@paytm"` |
| `bank_transfer` | `transaction_id`, `bank_name` | `"XXXX1234 - HDFC Bank"` |
| `card` | `transaction_id` | `"XXXX-4567 (Visa)"` |
| `credit` | `due_date` | `"Net 30 days"` |
| `other` | `notes` | `"Wallet transfer"` |

**Note:** `payment_source` stores mode-specific identifiers. Card/account numbers should be masked (last 4 digits only).

## Payment Status
| Status | Description |
|--------|-------------|
| `received` | Payment confirmed and added to trip total |
| `pending` | Awaiting confirmation |
| `cancelled` | Payment cancelled - reversed from total |
---
## Endpoints
### Record Payment
```http
POST {{base_url}}/trip-payments
Authorization: Bearer {{token}}
Content-Type: application/json
```
**Request Body:**
```json
{
    "trip_id": 1,
    "vehicle_id": 1,
    "driver_id": 1,
    "customer_id": 1,
    "customer_name": "ABC Construction Pvt Ltd",
    "customer_contact": "9876543210",
    "customer_company": "ABC Construction",
    "customer_gst": "27AABCU9603R1ZM",
    "amount": 30000,
    "tds_amount": 300,
    "discount_amount": 0,
    "payment_type": "advance",
    "payment_mode": "upi",
    "payment_source": "customer@paytm",
    "payment_status": "received",
    "payment_date": "2026-01-15T10:00:00Z",
    "transaction_id": "UPI123456789",
    "received_by": "Ramesh Singh (Driver)",
    "received_at_location": "Customer Office, Pune",
    "notes": "Advance payment for trip booking"
}
```
**Response:**
```json
{
    "success": true,
    "message": "Payment recorded successfully",
    "data": {
        "payment": {
            "id": 1,
            "trip_id": 1,
            "receipt_number": "RP-202601-001-00001",
            "amount": 30000,
            "tds_amount": 300,
            "discount_amount": 0,
            "net_amount": 29700,
            "payment_type": "advance",
            "payment_mode": "upi",
            "payment_source": "customer@paytm",
            "payment_status": "received",
            "payment_date": "2026-01-15T10:00:00Z",
            "transaction_id": "UPI123456789",
            "financial_year": "2025-26",
            "financial_month": "2026-01",
            "financial_quarter": "Q4-2025-26"
        },
        "trip_summary": {
            "trip_id": 1,
            "expected_amount": 100000,
            "paid_trip_price": 29700,
            "payment_count": 1,
            "pending_amount": 70300,
            "payment_status": "partial"
        }
    }
}
```
---
### List All Payments
```http
GET {{base_url}}/trip-payments
Authorization: Bearer {{token}}
```
**Query Parameters:**
| Parameter | Description | Example |
|-----------|-------------|---------|
| `trip_id` | Filter by trip | `?trip_id=1` |
| `vehicle_id` | Filter by vehicle | `?vehicle_id=1` |
| `customer_id` | Filter by customer | `?customer_id=1` |
| `payment_type` | Filter by type | `?payment_type=advance` |
| `payment_mode` | Filter by mode | `?payment_mode=card` |
| `payment_status` | Filter by status | `?payment_status=pending` |
| `financial_year` | Filter by FY | `?financial_year=2025-26` |
| `financial_month` | Filter by month | `?financial_month=2026-01` |
| `start_date` | From date | `?start_date=2026-01-01` |
| `end_date` | To date | `?end_date=2026-01-31` |
| `page` | Page number | `?page=1` |
| `per_page` | Items per page | `?per_page=20` |
---
### Get Payment Details
```http
GET {{base_url}}/trip-payments/:id
Authorization: Bearer {{token}}
```
---
### Update Payment
```http
PUT {{base_url}}/trip-payments/:id
Authorization: Bearer {{token}}
Content-Type: application/json
```
**Request Body:**
```json
{
    "amount": 30000,
    "tds_amount": 300,
    "notes": "Updated notes"
}
```
---
### Delete/Cancel Payment
```http
DELETE {{base_url}}/trip-payments/:id
Authorization: Bearer {{token}}
```
**Note:** Only Owner can delete payments. Cancelled payments are soft-deleted and trip totals are recalculated.
---
## Financial Reports
### Payment Summary
```http
GET {{base_url}}/trip-payments/summary
Authorization: Bearer {{token}}
```
**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| `financial_year` | Filter by FY |
| `financial_month` | Filter by month |
| `start_date` | From date |
| `end_date` | To date |
**Response:**
```json
{
    "success": true,
    "data": {
        "period": "2026-01",
        "total_collected": 3500000,
        "total_tds": 35000,
        "total_discount": 15000,
        "net_collected": 3450000,
        "refunds": 25000,
        "collection_by_mode": {
            "cash": 450000,
            "upi": 850000,
            "bank_transfer": 1500000,
            "card": 700000
        },
        "payment_count": 150
    }
}
```
### Payments by Customer
```http
GET {{base_url}}/trip-payments/by-customer/:customer_id
Authorization: Bearer {{token}}
```
**Response:**
```json
{
    "success": true,
    "data": {
        "customer_id": 1,
        "customer_name": "ABC Construction",
        "customer_company": "ABC Construction Pvt Ltd",
        "customer_gst": "27AABCU9603R1ZM",
        "total_billed": 500000,
        "total_received": 450000,
        "total_tds": 4500,
        "pending_amount": 45500,
        "trip_count": 10,
        "payment_count": 25,
        "payments": [...]
    }
}
```
### Payments by Period
```http
GET {{base_url}}/trip-payments/by-period
Authorization: Bearer {{token}}
```
**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| `financial_year` | Required - FY |
| `group_by` | `month` or `quarter` |
**Response:**
```json
{
    "success": true,
    "data": {
        "financial_year": "2025-26",
        "periods": [
            {
                "period": "2025-04",
                "total_collected": 2500000,
                "total_tds": 25000,
                "net_collected": 2475000,
                "payment_count": 100
            }
        ]
    }
}
```
### Cash Flow Report
```http
GET {{base_url}}/trip-payments/cash-flow
Authorization: Bearer {{token}}
```
**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| `start_date` | From date |
| `end_date` | To date |
### TDS Report
```http
GET {{base_url}}/trip-payments/tds-report
Authorization: Bearer {{token}}
```
**Query Parameters:**
| Parameter | Description |
|-----------|-------------|
| `financial_year` | Required - FY |
**Response:**
```json
{
    "success": true,
    "data": {
        "financial_year": "2025-26",
        "total_tds": 125000,
        "customer_wise_tds": [
            {
                "customer_id": 1,
                "customer_name": "ABC Construction",
                "customer_gst": "27AABCU9603R1ZM",
                "total_paid": 500000,
                "tds_deducted": 5000,
                "tds_percent": 1.0
            }
        ]
    }
}
```
---
## Notes
1. **Receipt Numbers**: Auto-generated unique receipt numbers (format: RP-YYYYMM-OWNER-SEQUENCE)
2. **Financial Periods**: Auto-calculated based on Indian FY (April-March)
3. **Net Amount**: Automatically calculated as `amount - tds_amount - discount_amount`
4. **Trip Auto-Update**: Trip's `paid_trip_price` and `payment_status` auto-updated after each payment operation
5. **TDS Tracking**: Track TDS deductions for Form 26AS reconciliation
6. **Customer Snapshot**: Customer details captured at payment time for historical accuracy
---
*Last Updated: January 2026*
