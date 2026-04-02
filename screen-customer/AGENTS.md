# AGENTS.md — screen-customer

## Purpose

Full-stack **Customer Management** feature module. Handles customer CRUD, list with search, and a rich detail screen with tabs for trips, payments, and financials. Supports PDF export of customer financial reports.

**Package:** `com.ijs.customer`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/customer/
├── LogTags.kt
├── data/
│   ├── datasource/
│   │   ├── CustomerLocalDataSource.kt     # Local cache interface
│   │   └── CustomerRemoteDataSource.kt    # API: list, detail, create, update, trips, payments, financials
│   ├── mapper/
│   │   └── CustomerMapper.kt             # CustomerDto ↔ Customer entity
│   ├── model/
│   │   └── CustomerDto.kt                # @Serializable DTOs
│   └── repository/
│       └── CustomerRepositoryImpl.kt     # Repository impl with auth token pattern
├── domain/
│   ├── entity/
│   │   └── Customer.kt                   # Customer domain entity (company, contact, GST, address)
│   ├── repository/
│   │   └── CustomerRepository.kt         # Repository interface
│   └── usecase/
│       └── CustomerUseCases.kt           # GetCustomers, GetCustomerDetail, CreateCustomer, etc.
└── presentation/
    ├── CustomerFeatureFacade.kt           # DI entry point
    ├── CustomerTypeConverters.kt          # Type helpers for Navigation 3 route arguments
    ├── create/
    │   ├── CreateCustomerContract.kt
    │   ├── CreateCustomerScreen.kt        # Customer registration form (company + contact + GST + address)
    │   └── CreateCustomerViewModel.kt
    ├── detail/
    │   ├── CustomerDetailContract.kt
    │   ├── CustomerDetailScreen.kt        # Tabbed detail: Info, Trips, Payments, Financials
    │   ├── CustomerDetailViewModel.kt
    │   ├── CustomerDetailTripsHandler.kt      # Trips tab data loading
    │   ├── CustomerDetailPaymentsHandler.kt   # Payments tab data loading
    │   ├── CustomerDetailFinancialsHandler.kt # Financials tab data loading
    │   ├── CustomerDetailPdfExporter.kt       # PDF export for customer reports
    │   └── components/
    │       ├── FinancialsTabContent.kt    # Revenue, expenses, profit/loss summary
    │       ├── PaymentsTabContent.kt      # Payment list with status grouping
    │       ├── PaymentItemCards.kt        # Individual payment cards
    │       ├── PaymentSummarySection.kt   # Payment summary (total, pending, received)
    │       └── TripsTabContent.kt         # Trip list for this customer
    └── list/
        ├── CustomersListContract.kt
        ├── CustomersListScreen.kt         # Customer list with search
        └── CustomersListViewModel.kt
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-network-lib` |
| implementation | `:ijs-pdf-report` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, ktor-client |

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| CustomersListScreen | `Customers` | Search customers by name/company |
| CustomerDetailScreen | `CustomerDetail(id)` | Tabbed: Info, Trips, Payments, Financials |
| CreateCustomerScreen | `CreateCustomer` | Registration form |

---

## Key Patterns

- **Tabbed detail with handlers** — Each tab's data loading is extracted into a dedicated handler class (`CustomerDetailTripsHandler`, etc.) to keep the ViewModel under 500 lines.
- **PDF export** — `CustomerDetailPdfExporter` generates HTML-based PDF reports for customer financials, trips, and payments via `ijs-pdf-report`.
- **Type converters** — `CustomerTypeConverters` provides serialization helpers for passing customer data through Navigation 3 routes.
- **Local data source** — `CustomerLocalDataSource` interface is defined here for caching; implementation lives in `sharedUI` with Room.
