# 12. Vehicle Finance API

> **Package:** `com.indusjs.fleet.data.datasource.finance`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### Purchase Info

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles/{id}/purchase` | Get purchase info |
| `POST` | `/vehicles/{id}/purchase` | Create purchase record |
| `PUT` | `/vehicles/{id}/purchase` | Update purchase record |

### Loan Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles/{id}/loan-summary` | Get loan summary |

### Loan Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicles/{id}/loan-payments` | List vehicle's loan payments |
| `GET` | `/vehicle-loan-payments` | List all loan payments |
| `GET` | `/vehicle-loan-payments/{id}` | Get payment by ID |
| `POST` | `/vehicle-loan-payments` | Record a loan payment |
| `PUT` | `/vehicle-loan-payments/{id}` | Update a payment |
| `PATCH` | `/vehicle-loan-payments/{id}/pay` | Mark EMI as paid |
| `DELETE` | `/vehicle-loan-payments/{id}` | Delete a payment |

### EMI Alerts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/vehicle-loan-payments/upcoming` | Get upcoming EMIs |
| `GET` | `/vehicle-loan-payments/overdue` | Get overdue EMIs |

---

## Data Source Interface

### `VehicleFinanceRemoteDataSource`

```kotlin
interface VehicleFinanceRemoteDataSource {
    // Purchase
    suspend fun getPurchase(token: String, vehicleId: Int): VehiclePurchaseResponse
    suspend fun createPurchase(token: String, vehicleId: Int, request: CreatePurchaseRequest): VehiclePurchaseResponse
    suspend fun updatePurchase(token: String, vehicleId: Int, request: UpdatePurchaseRequest): VehiclePurchaseResponse

    // Loan Summary
    suspend fun getLoanSummary(token: String, vehicleId: Int): LoanSummaryResponse

    // Loan Payments
    suspend fun getLoanPayments(token: String, vehicleId: Int, page: Int, perPage: Int, status: String?): LoanPaymentsListResponse
    suspend fun getAllLoanPayments(token: String, page: Int, perPage: Int, vehicleId: Int?, status: String?): LoanPaymentsListResponse
    suspend fun getPaymentById(token: String, paymentId: Int): LoanPaymentResponse
    suspend fun recordPayment(token: String, request: RecordPaymentRequest): LoanPaymentResponse
    suspend fun updatePayment(token: String, paymentId: Int, request: UpdatePaymentRequest): LoanPaymentResponse
    suspend fun markEmiPaid(token: String, paymentId: Int, request: MarkEmiPaidRequest): LoanPaymentResponse
    suspend fun deletePayment(token: String, paymentId: Int): DeleteResponse

    // Alerts
    suspend fun getUpcomingEmis(token: String, days: Int): EmiAlertsResponse
    suspend fun getOverdueEmis(token: String): EmiAlertsResponse
}
```

**Implementation:** `VehicleFinanceRemoteDataSourceImpl` — `@Inject`, depends on `HttpClient`, `Json`

---

## Key Request DTOs

### `CreatePurchaseRequest`

| Field | JSON Key | Type | Required | Notes |
|-------|----------|------|----------|-------|
| `purchaseDate` | `purchase_date` | `String` | ✅ | DD-MM-YYYY |
| `purchasePrice` | `purchase_price` | `Double` | ✅ | |
| `purchaseType` | `purchase_type` | `String` | ✅ | `cash` or `loan` |
| `dealerName` | `dealer_name` | `String?` | ❌ | |
| `downPayment` | `down_payment` | `Double?` | ❌ | For loan |
| `loanAmount` | `loan_amount` | `Double?` | ❌ | For loan |
| `loanTenure` | `loan_tenure` | `Int?` | ❌ | Months |
| `interestRate` | `interest_rate` | `Double?` | ❌ | Annual % |
| `emiAmount` | `emi_amount` | `Double?` | ❌ | Monthly EMI |
| `financierName` | `financier_name` | `String?` | ❌ | Bank/NBFC name |
| `loanAccountNumber` | `loan_account_number` | `String?` | ❌ | |

### `RecordPaymentRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `vehicleId` | `vehicle_id` | `Int` | ✅ |
| `amount` | `amount` | `Double` | ✅ |
| `paymentDate` | `payment_date` | `String` | ✅ |
| `paymentMode` | `payment_mode` | `String` | ✅ |
| `referenceNumber` | `reference_number` | `String?` | ❌ |
| `notes` | `notes` | `String?` | ❌ |

### `MarkEmiPaidRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `paymentDate` | `payment_date` | `String` | ✅ |
| `paymentMode` | `payment_mode` | `String` | ✅ |
| `referenceNumber` | `reference_number` | `String?` | ❌ |
| `amountPaid` | `amount_paid` | `Double?` | ❌ |

---

## Query Parameters

### List Loan Payments

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | `Int` | Page number |
| `per_page` | `Int` | Items per page |
| `vehicle_id` | `Int?` | Filter by vehicle |
| `status` | `String?` | `pending`, `paid`, `overdue` |

### Upcoming EMIs

| Parameter | Type | Description |
|-----------|------|-------------|
| `days` | `Int` | Look-ahead days |

---

## Source Files

| File | Path |
|------|------|
| VehicleFinanceRemoteDataSource | `data/datasource/finance/VehicleFinanceRemoteDataSource.kt` |
| DTOs | `data/model/finance/VehicleFinanceDto.kt` |

