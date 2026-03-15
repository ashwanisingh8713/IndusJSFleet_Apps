# Customers Feature

> Use this prompt when working on customer management.

## Screens & Routes

| Screen | Route | Description |
|--------|-------|-------------|
| `CustomersListScreen` | `Customers` | Search customers, offline cached |
| `CustomerDetailScreen` | `CustomerDetail(customerId)` | Tabs: Info, Trips, Financials |
| `CreateCustomerScreen` | `CreateCustomer` | Company + contact + GST + address |

## API Endpoints

```
GET    /customers              → List all customers
GET    /customers/{id}         → Customer details (includes trips, financials)
POST   /customers              → Create customer
PUT    /customers/{id}         → Update customer
PATCH  /customers/{id}/toggle-active → Toggle active/inactive
GET    /customers/{id}/statistics   → Customer financial statistics
```

## Domain Entity

```kotlin
data class Customer(
    val id: String,
    val companyName: String,
    val contactPerson: String?,
    val mobile: String,
    val email: String?,
    val gstNumber: String?,
    val address: String?,
    val city: String?,
    val state: String?,
    val isActive: Boolean,
    val totalTrips: Int?,
    val totalRevenue: Double?,
    val outstandingAmount: Double?
)
```

## Offline Caching

Customers are cached in `FleetDatabase` via `CustomerLocalDataSourceImpl` using Room DAO. On first load, data is fetched from API and persisted. Subsequent loads use cache with pull-to-refresh for remote sync.

## Key Files

| Layer | File |
|-------|------|
| Entity | `domain/entity/customer/` |
| Repository | `domain/repository/customer/` |
| Use Cases | `domain/usecase/customer/` (8 use cases) |
| DataSource | `data/datasource/customer/` (Remote + Local) |
| Repository Impl | `data/repository/customer/CustomerRepositoryImpl.kt` |
| Presentation | `presentation/customers/list/`, `detail/`, `create/` |

