# screen-customer — IndusJS Fleet

## Purpose

Customer feature: CRUD, company/contact/GST info, trip history, financial summaries.

## Package: `com.ijs.customer`

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| CustomersListScreen | `Customers` | Search customers |
| CustomerDetailScreen | `CustomerDetail(id)` | Info, trip history, financials |
| CreateCustomerScreen | `CreateCustomer` | Company + contact + GST + address |

## Key Files

| File | Purpose |
|------|---------|
| `presentation/CustomerFeatureFacade.kt` | Facade — 3 entry points |
| `presentation/list/CustomersList*.kt` | List Contract/VM/Screen |
| `presentation/detail/CustomerDetail*.kt` | Detail Contract/VM/Screen |
| `presentation/create/CreateCustomer*.kt` | Create Contract/VM/Screen |
| `data/datasource/CustomerRemoteDataSource.kt` | API calls |
| `data/datasource/CustomerLocalDataSource.kt` | Local caching |
| `data/repository/CustomerRepositoryImpl.kt` | Repository impl |
| `domain/entity/Customer.kt` | Domain entity |
| `domain/repository/CustomerRepository.kt` | Repository interface |

## Special: Local Caching

Customer module uses both `CustomerRemoteDataSource` and `CustomerLocalDataSource`
for caching customer lists. `CustomerLocalDataSource` is created in `sharedUI`'s DI
and passed to `FeatureRepositoryFactory`.

## APIs

- `GET /customers` — List all
- `GET /customers/{id}` — Detail
- `POST /customers` — Create
- `PUT /customers/{id}` — Update

## Module Path

`screen-customer/src/commonMain/kotlin/com/ijs/customer/`

## Depends On: `ijs-network-lib`

