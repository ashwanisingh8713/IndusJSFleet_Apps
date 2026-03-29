# screen-customer

## Overview

**Package:** `com.ijs.customer`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Complete customer lifecycle management — CRUD operations for customers (company info, contact, GST, address), customer trip history, financial summaries, pending payments tracking, and PDF export of customer reports.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `CustomerFeatureFacade`, List (Contract/VM/Screen), Detail (Contract/VM/Screen + handlers), Create (Contract/VM/Screen), `CustomerTypeConverters` |
| **Domain** | `Customer`, `CustomerSummary`, `CustomerStatistics`, `CustomerTrip`, `CustomerPendingPayment`, `CustomerFinancials` entities; `CustomerRepository` interface; Use cases |
| **Data** | `CustomerRemoteDataSource`, `CustomerLocalDataSource`, `CustomerRepositoryImpl`, `CustomerMapper`, `CustomerDto` |

---

## Dependencies

```
screen-customer → ijs-network-lib → ijs-core-lib
screen-customer → ijs-pdf-report (PDF export)
```

- `api(project(":ijs-network-lib"))` — HTTP client, auth, base data sources
- `implementation(project(":ijs-pdf-report"))` — customer financial PDF export

---

## Screens

### 1. CustomersListScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Customers` |
| ViewModel | `CustomersListViewModel` |
| Contract | `CustomersListContract` |

**Features:**
- Search bar with real-time filtering
- Customer cards showing company name, person name, contact
- Pagination with load-more
- Pull-to-refresh
- FAB for quick add

**Key Intents:** `LoadCustomers`, `RefreshCustomers`, `LoadMore`, `UpdateSearchQuery`, `OnCustomerClick`, `OnAddCustomerClick`
**Key Effects:** `NavigateToCustomerDetail(id)`, `NavigateToCreateCustomer`, `ShowSnackbar`

---

### 2. CustomerDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.CustomerDetail(customerId)` |
| ViewModel | `CustomerDetailViewModel` |
| Contract | `CustomerDetailContract` |

**Features:**
- Tabbed layout: **Overview**, **Trips**, **Payments**, **Financials**
- **Overview tab:** Customer info display + inline edit mode (company name, person name, contacts, email, GST, address, notes)
- **Trips tab:** Trip history with state filter (All, Planned, On Route, Completed, Cancelled), pagination
- **Payments tab:** Pending payments list with summary (total pending, overdue count), pagination
- **Financials tab:** Revenue, expenses, pending, received payments summary (Owner/GM only)
- PDF export for trips, payments, and financials

**Refactored handlers (to stay under 500 lines):**
- `CustomerDetailTripsHandler` — loads and paginates trips
- `CustomerDetailPaymentsHandler` — loads pending payments
- `CustomerDetailFinancialsHandler` — loads financial summary
- `CustomerDetailPdfExporter` — generates PDF reports

**Key Intents:** `LoadCustomer`, `ChangeTab`, `ToggleEditMode`, `SaveChanges`, `LoadTrips`, `LoadPendingPayments`, `LoadFinancials`, `ExportPdf`

---

### 3. CreateCustomerScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.CreateCustomer` |
| ViewModel | `CreateCustomerViewModel` |
| Contract | `CreateCustomerContract` |

**Features:**
- Form fields: Company Name*, Person Name*, Primary Contact*, Secondary Contact, Address, Email, GST Number, Notes
- Field validation (mobile format, email format, GST format)
- On success → navigates to CustomerDetail

**Key Intents:** `UpdateField(field, value)`, `CreateCustomer`, `ClearError`
**Key Effects:** `CustomerCreated(id)`, `ShowSnackbar`

---

## Facade

```kotlin
object CustomerFeatureFacade {
    fun CustomersListEntry(viewModel, onNavigateBack, onNavigateToCustomerDetail, onNavigateToCreateCustomer)
    fun CustomerDetailEntry(viewModel, customerId, onNavigateBack)
    fun CreateCustomerEntry(viewModel, onNavigateBack, onNavigateToCustomerDetail)
}
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `Customer` | Full customer data (id, companyName, personName, contacts, GST, address, notes) |
| `CustomerSummary` | Lightweight for dropdowns (id, companyName, personName, contact) |
| `CustomerStatistics` | Financial overview (total trips, revenue, pending/received payments) |
| `CustomerTrip` | Trip record linked to customer (route, status, pricing) |
| `CustomerPendingPayment` | Unpaid trip with amount owed |
| `CustomerFinancials` | Revenue/expense/payment breakdown |

---

## Use Cases

| Use Case | Description |
|----------|-------------|
| `GetCustomersUseCase` | Paginated customer list with search/filter |
| `GetCustomerUseCase` | Single customer by ID |
| `CreateCustomerUseCase` | Create new customer |
| `UpdateCustomerUseCase` | Update existing customer |
| `GetCustomerStatisticsUseCase` | Financial stats for a customer |
| `GetCustomerTripsUseCase` | Trip history for customer |
| `GetCustomerPendingPaymentsUseCase` | Pending payments for customer |
| `GetCustomerFinancialsUseCase` | Detailed financials for customer |
| `GetCustomerSummariesUseCase` | Lightweight list for selection dropdowns |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/customers` | GET | List customers (paginated, searchable) |
| `/customers` | POST | Create customer |
| `/customers/{id}` | GET | Get customer details |
| `/customers/{id}` | PUT | Update customer |
| `/customers/{id}/statistics` | GET | Customer financial statistics |
| `/customers/{id}/trips` | GET | Customer trip history |
| `/customers/{id}/pending-payments` | GET | Customer pending payments |
| `/customers/{id}/financials` | GET | Customer financial breakdown |
| `/customers/summaries` | GET | Lightweight list for dropdowns |

---

## Inter-Module Communication

- **No cross-feature module dependencies.** Customer data is self-contained.
- `CustomerSummary` is used by `screen-trip` and `screen-payment` — those modules access customer data through `ijs-core-lib` shared contracts or via their own API calls.
- PDF export uses `ijs-pdf-report` for HTML→PDF conversion.

