# Customers Table

## Table: `customers`

Stores customer/client information for fleet owners.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `company_name` | `varchar(255)` | | Company/business name |
| `person_name` | `varchar(255)` | | Contact person name |
| `primary_contact` | `varchar(50)` | NOT NULL | Primary phone number |
| `secondary_contact` | `varchar(50)` | | Secondary phone number |
| `company_address` | `text` | | Business address |
| `email` | `varchar(255)` | | Email address |
| `gst_number` | `varchar(50)` | | GST registration number |
| `notes` | `text` | | Additional notes |
| **Ownership & Status** |
| `owner_id` | `bigint` | INDEX, NOT NULL | Fleet owner |
| `is_active` | `boolean` | DEFAULT true | Active status |
| `created_by_id` | `bigint` | INDEX | User who created |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Computed Fields (Not Stored)

These fields are calculated at query time:

| Field | Description |
|-------|-------------|
| `total_trips` | Count of trips with this customer |
| `total_revenue` | Sum of expected_trip_price |
| `pending_payment` | Sum of unpaid amounts |

---

## Indexes

```sql
CREATE INDEX idx_customers_owner_id ON customers(owner_id);
CREATE INDEX idx_customers_created_by_id ON customers(created_by_id);
CREATE INDEX idx_customers_is_active ON customers(is_active);
CREATE INDEX idx_customers_deleted_at ON customers(deleted_at);
```

---

## Relationships

| Relation | Type | Target Table | Foreign Key |
|----------|------|--------------|-------------|
| Owner | belongs_to | users | owner_id |
| CreatedBy | belongs_to | users | created_by_id |
| Trips | has_many | trips | customer_id |

---

## Sample Data

```json
{
  "id": 1,
  "company_name": "ABC Construction Pvt Ltd",
  "person_name": "Suresh Patel",
  "primary_contact": "9876543210",
  "secondary_contact": "9876543211",
  "company_address": "123 Industrial Area, Pune, Maharashtra 411001",
  "email": "suresh@abcconstruction.com",
  "gst_number": "27AABCU9603R1ZM",
  "notes": "Regular customer since 2023",
  "owner_id": 1,
  "is_active": true,
  "created_by_id": 1,
  "created_at": "2026-01-01T00:00:00Z"
}
```

---

## Customer with Statistics

```json
{
  "id": 1,
  "company_name": "ABC Construction Pvt Ltd",
  "person_name": "Suresh Patel",
  "primary_contact": "9876543210",
  "email": "suresh@abcconstruction.com",
  "gst_number": "27AABCU9603R1ZM",
  "is_active": true,
  "total_trips": 45,
  "total_revenue": 4500000.00,
  "pending_payment": 125000.00
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v2/customers` | Create customer | Owner, GM, Manager |
| GET | `/api/v2/customers` | List customers | All |
| GET | `/api/v2/customers/:id` | Get customer details | All |
| PUT | `/api/v2/customers/:id` | Update customer | Owner, GM, Manager |
| PATCH | `/api/v2/customers/:id/toggle-active` | Enable/disable customer | Owner, GM |
| **Trip Data** |
| GET | `/api/v2/customers/:id/trips` | Get customer's trips | All |
| **Financial APIs (Owner/GM Only)** |
| GET | `/api/v2/customers/:id/statistics` | Comprehensive customer stats | Owner, GM |
| GET | `/api/v2/customers/:id/pending-payments` | Trips with pending payments | Owner, GM |
| GET | `/api/v2/customers/:id/payments` | All payments received | Owner, GM |
| GET | `/api/v2/customers/:id/payment-summary` | Payment breakdown by mode/type | Owner, GM |
| GET | `/api/v2/customers/:id/financial-report` | P&L report for customer | Owner, GM |

### Query Parameters (Financial APIs)

| Parameter | Format | Description | Applies To |
|-----------|--------|-------------|------------|
| `start_date` | YYYY-MM-DD or DD-MM-YYYY | Filter from date | All financial APIs |
| `end_date` | YYYY-MM-DD or DD-MM-YYYY | Filter to date | All financial APIs |
| `period` | monthly/quarterly/yearly | Period grouping | financial-report |
| `status` | received/pending | Payment status filter | payments |
| `mode` | cash/upi/bank_transfer/card | Payment mode filter | payments |

---

## Permission Matrix

| Action | Owner | GM | Manager | Supervisor |
|--------|-------|-----|---------|------------|
| Create | ✅ | ✅ | ✅ | ❌ |
| View | ✅ | ✅ | ✅ | ✅ |
| Update | ✅ | ✅ | ✅ | ❌ |
| Delete | ✅ | ✅ | ❌ | ❌ |
| Statistics | ✅ | ✅ | ❌ | ❌ |

---

## Customer in Trip Association

Customers are associated with trips via `customer_id`:

```go
// In Trip model
type Trip struct {
    // ...
    CustomerID *uint     `gorm:"index" json:"customer_id,omitempty"`
    Customer   *Customer `gorm:"-" json:"customer,omitempty"`
    
    // Legacy fields (deprecated)
    CustomerName    string `json:"customer_name,omitempty"`
    CustomerContact string `json:"customer_contact,omitempty"`
}
```

### Trip Response with Customer

```json
{
  "id": 1,
  "customer_id": 1,
  "customer": {
    "id": 1,
    "company_name": "ABC Construction",
    "person_name": "Suresh Patel",
    "primary_contact": "9876543210"
  },
  "start_location": "Mumbai",
  "end_location": "Pune"
}
```

---

## Customer Statistics Query

```sql
SELECT 
    c.id,
    c.company_name,
    c.person_name,
    COUNT(t.id) as total_trips,
    COALESCE(SUM(t.expected_trip_price), 0) as total_revenue,
    COALESCE(SUM(
        CASE WHEN t.payment_status != 'full' 
        THEN t.expected_trip_price - t.paid_trip_price 
        ELSE 0 END
    ), 0) as pending_payment
FROM customers c
LEFT JOIN trips t ON t.customer_id = c.id AND t.deleted_at IS NULL
WHERE c.id = ? AND c.deleted_at IS NULL
GROUP BY c.id;
```

---

## Use Cases

### 1. Creating a Trip with Customer
```json
POST /api/v2/trips
{
  "vehicle_id": 1,
  "driver_id": 1,
  "customer_id": 1,
  "start_location": "Mumbai",
  "end_location": "Pune",
  "expected_trip_price": 50000.00
}
```

### 2. Customer Statistics Response
```json
GET /api/v2/customers/1/statistics?start_date=2026-01-01&end_date=2026-12-31
{
  "customer": {
    "id": 1,
    "company_name": "ABC Construction",
    "person_name": "Suresh Patel",
    "primary_contact": "9876543210"
  },
  "summary": {
    "total_trips": 45,
    "completed_trips": 40,
    "cancelled_trips": 2,
    "active_trips": 3,
    "planned_trips": 0,
    "total_revenue": 4500000.00,
    "total_paid": 3800000.00,
    "total_pending": 700000.00,
    "collection_rate": 84.44,
    "total_trip_costs": 2500000.00,
    "net_profit": 1300000.00
  },
  "trips_by_state": {
    "completed": 40,
    "cancelled": 2,
    "on_route": 2,
    "planned": 1
  },
  "payment_by_mode": {
    "cash": 1500000.00,
    "upi": 1800000.00,
    "bank_transfer": 500000.00
  },
  "monthly_revenue": [
    {"month": "2026-01", "trips": 5, "revenue": 500000.00, "collected": 450000.00}
  ],
  "date_range": {
    "start_date": "2026-01-01T00:00:00Z",
    "end_date": "2026-12-31T23:59:59Z"
  }
}
```

### 3. Pending Payments Response
```json
GET /api/v2/customers/1/pending-payments
{
  "customer": {
    "id": 1,
    "company_name": "ABC Construction"
  },
  "items": [
    {
      "trip_id": 123,
      "scheduled_date": "2026-01-10T00:00:00Z",
      "start_location": "Mumbai",
      "end_location": "Pune",
      "vehicle_number": "MH12AB1234",
      "expected_price": 50000.00,
      "paid_amount": 30000.00,
      "pending_amount": 20000.00,
      "days_overdue": 7,
      "payment_status": "partial"
    }
  ],
  "summary": {
    "total_pending": 700000.00,
    "pending_count": 15
  },
  "count": 15,
  "page": 1,
  "per_page": 20,
  "total_pages": 1,
  "has_more": false
}
```

### 4. Financial Report Response
```json
GET /api/v2/customers/1/financial-report?period=monthly
{
  "customer": {
    "id": 1,
    "company_name": "ABC Construction",
    "person_name": "Suresh Patel"
  },
  "date_range": {
    "start_date": null,
    "end_date": null,
    "period": "monthly"
  },
  "revenue_summary": {
    "total_expected": 4500000.00,
    "total_received": 3800000.00,
    "total_pending": 700000.00
  },
  "cost_summary": {
    "total_trip_costs": 2500000.00,
    "fuel_costs": 1500000.00,
    "toll_costs": 300000.00,
    "other_costs": 700000.00
  },
  "profit_loss": {
    "gross_profit": 1300000.00,
    "profit_margin": 34.21,
    "collection_rate": 84.44,
    "is_profitable": true
  },
  "trip_summary": {
    "total_trips": 45,
    "completed_trips": 40,
    "cancelled_trips": 2,
    "active_trips": 3
  },
  "period_breakdown": [
    {"period": "2026-01", "revenue": 500000.00, "collected": 450000.00, "trips": 5}
  ],
  "top_vehicles": [
    {"vehicle_id": 1, "registration_number": "MH12AB1234", "trip_count": 15, "total_revenue": 1500000.00}
  ]
}
```

### 5. Finding Customer's Pending Payments (SQL)
```sql
SELECT 
    t.id as trip_id,
    t.scheduled_date,
    t.expected_trip_price,
    t.paid_trip_price,
    (t.expected_trip_price - t.paid_trip_price) as pending
FROM trips t
WHERE t.customer_id = ?
  AND t.payment_status != 'full'
  AND t.deleted_at IS NULL
ORDER BY t.scheduled_date DESC;
```

### 3. Customer Revenue Report
```sql
SELECT 
    DATE_TRUNC('month', t.scheduled_date) as month,
    COUNT(*) as trip_count,
    SUM(t.expected_trip_price) as revenue,
    SUM(t.paid_trip_price) as collected
FROM trips t
WHERE t.customer_id = ?
  AND t.deleted_at IS NULL
GROUP BY DATE_TRUNC('month', t.scheduled_date)
ORDER BY month DESC;
```

---

## Notes

1. **Owner Isolation**: Each owner has their own customer list
2. **Legacy Support**: Old trips may have inline `customer_name`/`customer_contact` instead of `customer_id`
3. **GST Number**: Format: 2 digits (state code) + 10 chars (PAN) + 1 char (entity) + 1 char (Z default) + 1 char (checksum)
4. **Soft Delete**: Customers are disabled, not deleted (preserves trip history)
5. **Financial Stats**: Only visible to Owner and General Manager

---

*Last Updated: January 2026*

