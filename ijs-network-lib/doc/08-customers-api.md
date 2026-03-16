# 08. Customers API

> **Package:** `com.indusjs.fleet.data.datasource.customer`  
> **Auth Required:** Yes (Bearer token)  
> **Last Updated:** 16-Mar-2026

---

## Endpoints

### CRUD

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/customers` | List customers (paginated) |
| `GET` | `/customers/{id}` | Get customer by ID |
| `POST` | `/customers` | Create a new customer |
| `PUT` | `/customers/{id}` | Update customer |
| `PATCH` | `/customers/{id}/toggle-status` | Toggle customer active status |

### Analytics (Owner / GM only)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/customers/{id}/statistics` | Customer statistics |
| `GET` | `/customers/{id}/trips` | Customer trips (paginated) |
| `GET` | `/customers/{id}/pending-payments` | Customer pending payments |
| `GET` | `/customers/{id}/payments` | Customer payment history |
| `GET` | `/customers/{id}/payment-summary` | Payment breakdown by mode/month |
| `GET` | `/customers/{id}/financial-report` | Comprehensive customer P&L |

---

## Data Source

### `CustomerRemoteDataSource`

> **Note:** This is a concrete class (not interface + impl pattern).

```kotlin
@Inject
class CustomerRemoteDataSource(
    private val httpClient: HttpClient,
    private val json: Json
) : RemoteDataSource {
    suspend fun getCustomers(token: String, page: Int, perPage: Int): CustomerListResponse
    suspend fun getCustomer(token: String, customerId: Int): CustomerResponse
    suspend fun createCustomer(token: String, request: CreateCustomerRequest): CustomerResponse
    suspend fun updateCustomer(token: String, customerId: Int, request: UpdateCustomerRequest): CustomerResponse
    suspend fun toggleCustomerStatus(token: String, customerId: Int): CustomerResponse
    suspend fun getCustomerStatistics(token: String, customerId: Int, startDate: String?, endDate: String?): CustomerStatisticsResponse
    suspend fun getCustomerTrips(token: String, customerId: Int, page: Int, perPage: Int, state: String?): CustomerTripsResponse
    suspend fun getCustomerPendingPayments(token: String, customerId: Int, page: Int, perPage: Int): CustomerPendingPaymentsResponse
    suspend fun getCustomerPayments(token: String, customerId: Int, page: Int, perPage: Int, status: String?, mode: String?): CustomerPaymentsResponse
    suspend fun getCustomerPaymentSummary(token: String, customerId: Int, startDate: String?, endDate: String?): CustomerPaymentSummaryResponse
    suspend fun getCustomerFinancialReport(token: String, customerId: Int, period: String, startDate: String?, endDate: String?): CustomerFinancialReportResponse
}
```

### `CustomerLocalDataSource`

Caches customer list locally.

---

## Key Request DTOs

### `CreateCustomerRequest`

| Field | JSON Key | Type | Required |
|-------|----------|------|----------|
| `companyName` | `company_name` | `String` | ✅ |
| `contactPerson` | `contact_person` | `String` | ✅ |
| `mobile` | `mobile` | `String` | ✅ |
| `email` | `email` | `String?` | ❌ |
| `gstNumber` | `gst_number` | `String?` | ❌ |
| `address` | `address` | `String?` | ❌ |
| `city` | `city` | `String?` | ❌ |
| `state` | `state` | `String?` | ❌ |
| `pincode` | `pincode` | `String?` | ❌ |

---

## Query Parameters

### List Customers

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | `Int` | Page number (default: 1) |
| `per_page` | `Int` | Items per page (default: 20) |

### Customer Trips

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | `Int` | Page number |
| `per_page` | `Int` | Items per page |
| `state` | `String?` | Filter by trip state |

### Customer Payments

| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | `Int` | Page number |
| `per_page` | `Int` | Items per page |
| `status` | `String?` | Payment status filter |
| `mode` | `String?` | Payment mode filter |

### Financial Report

| Parameter | Type | Description |
|-----------|------|-------------|
| `period` | `String` | Period: `monthly`, `quarterly`, `yearly` |
| `start_date` | `String?` | Custom start date |
| `end_date` | `String?` | Custom end date |

---

## Source Files

| File | Path |
|------|------|
| CustomerRemoteDataSource | `data/datasource/customer/CustomerRemoteDataSource.kt` |
| CustomerLocalDataSource | `data/datasource/customer/CustomerLocalDataSource.kt` |
| DTOs | `data/model/customer/CustomerDto.kt` |

