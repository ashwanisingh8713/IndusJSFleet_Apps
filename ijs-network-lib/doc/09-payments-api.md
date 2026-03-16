# 09. Trip Payments API

> **Package:** `com.indusjs.fleet.data.datasource.payment`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/trip-payments` | Create a trip payment |
| `GET` | `/trip-payments` | List payments (with filters) |
| `GET` | `/trip-payments/{id}` | Get payment by ID |
| `PUT` | `/trip-payments/{id}` | Update a payment |
| `DELETE` | `/trip-payments/{id}` | Delete a payment |

### Trip-specific

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/trips/{id}/payments` | Get payments for a trip |

### Reports

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/trip-payments/summary` | Payment summary report |
| `GET` | `/trip-payments/tds-report` | TDS (tax) report |

---

## Data Source

### `TripPaymentRemoteDataSource`

> **Note:** Concrete class, not interface + impl.

```kotlin
@Inject
class TripPaymentRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json
) {
    // Create
    suspend fun createPayment(token: String, request: CreateTripPaymentRequest): TripPaymentResponse
    suspend fun addPaymentToTrip(token: String, tripId: Int, request: AddPaymentToTripRequest): TripPaymentResponse

    // Read
    suspend fun listPayments(token: String, page: Int, perPage: Int, tripId: Int?, customerId: Int?, paymentType: String?, paymentMode: String?, paymentStatus: String?, startDate: String?, endDate: String?): TripPaymentListResponse
    suspend fun getTripPayments(token: String, tripId: Int, page: Int, perPage: Int): TripPaymentsHistoryResponse
    suspend fun getPayment(token: String, paymentId: Int): TripPaymentResponse

    // Update
    suspend fun updatePayment(token: String, paymentId: Int, request: UpdateTripPaymentRequest): TripPaymentResponse

    // Delete
    suspend fun deletePayment(token: String, paymentId: Int): TripPaymentResponse

    // Reports
    suspend fun getPaymentSummary(token: String, startDate: String?, endDate: String?): PaymentSummaryReportResponse
    suspend fun getTdsReport(token: String, financialYear: String): TdsReportResponse
}
```

---

## Query Parameters (List Payments)

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | `Int` | Page number (default: 1) |
| `per_page` | `Int` | Items per page (default: 20) |
| `trip_id` | `Int?` | Filter by trip |
| `customer_id` | `Int?` | Filter by customer |
| `payment_type` | `String?` | `advance`, `final`, `partial` |
| `payment_mode` | `String?` | `cash`, `upi`, `bank_transfer`, `cheque`, `card` |
| `payment_status` | `String?` | `received`, `pending`, `cancelled` |
| `start_date` | `String?` | Filter start date |
| `end_date` | `String?` | Filter end date |

---

## Key Request DTOs

### `CreateTripPaymentRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `tripId` | `trip_id` | `Int` | ✅ |
| `amount` | `amount` | `Double` | ✅ |
| `paymentMode` | `payment_mode` | `String` | ✅ |
| `paymentType` | `payment_type` | `String` | ✅ |
| `paymentDate` | `payment_date` | `String` | ✅ |
| `referenceNumber` | `reference_number` | `String?` | ❌ |
| `notes` | `notes` | `String?` | ❌ |
| `tdsAmount` | `tds_amount` | `Double?` | ❌ |
| `tdsPercentage` | `tds_percentage` | `Double?` | ❌ |

### `UpdateTripPaymentRequest`

Same fields as create, but all optional.

### Payment Modes

```
cash, upi, bank_transfer, cheque, card
```

### Payment Status

```
received, pending, cancelled
```

### Payment Types

```
advance, final, partial
```

---

## Source Files

| File | Path |
|------|------|
| TripPaymentRemoteDataSource | `data/datasource/payment/TripPaymentRemoteDataSource.kt` |
| DTOs | `data/model/payment/TripPaymentDto.kt` |
| Request DTOs | `data/model/payment/TripPaymentRequest.kt` |

