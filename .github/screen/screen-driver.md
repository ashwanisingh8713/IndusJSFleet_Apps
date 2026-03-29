# screen-driver

## Overview

**Package:** `com.ijs.driver`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Complete driver lifecycle management — CRUD operations for drivers, license tracking, status transitions, driver cost management (salary, advance, bonus, penalty, deductions), caretaker assignment, trip history, cost history with PDF export.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `DriverFeatureFacade`, List (Contract/VM/Screen + StateOptions), Detail (Contract/VM/Screen), Create (Contract/VM/Screen), Cost Entry (Contract/VM/Screen) |
| **Domain** | `Driver`, `DriverStatus`, `LicenseType` entities; `DriverRepository` interface; `DriverUseCases` |
| **Data** | `DriverRemoteDataSource`, `DriverRepositoryImpl`, `DriverMapper`, `DriverDto`, `DriverCostModels` |

---

## Dependencies

```
screen-driver → ijs-network-lib → ijs-core-lib
screen-driver → screen-team (caretaker assignment dropdown)
screen-driver → ijs-pdf-report (cost reports)
screen-driver → ijs-datetime-picker (date fields)
```

**Cross-feature note:** Depends on `screen-team` for `TeamMemberDto` used in caretaker selection dropdowns when creating/editing drivers.

---

## Screens

### 1. DriversScreen (List)

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Drivers` |
| ViewModel | `DriversViewModel` |
| Contract | `DriversContract` |

**Features:**
- Search bar for driver name/mobile filtering
- Status filter chips (All, Active, Inactive, On Route, On Leave, Suspended, Terminated)
- Driver cards with status badge, license info, contact details
- Toggle active/inactive, delete driver actions
- Status transition with validation

**Key Intents:** `LoadDrivers`, `RefreshDrivers`, `SearchDrivers`, `FilterByStatus`, `SelectDriver`, `DeleteDriver`, `UpdateDriverStatus`, `ToggleDriverActive`

---

### 2. DriverDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.DriverDetail(driverId)` |
| ViewModel | `DriverDetailViewModel` |
| Contract | `DriverDetailContract` |

**Features:**
- **Info section:** Name, mobile, email, license number/type/expiry, DOB, address, emergency contact, blood group, joining date
- **Edit mode:** Inline editing of all fields with validation
- **Status management:** Status badge with transition dropdown showing valid next states
- **Caretaker assignment:** Dropdown of team members (from `screen-team`)
- **Costs tab:** Cost history with breakdown by group (Payments: salary, advance, bonus; Deductions: penalty, fine; Other), pagination, date range filter
- **Trips tab:** Trip history linked to driver
- **PDF export:** Driver costs report

**Key Intents:** `LoadDriver`, `ToggleEditMode`, `SaveChanges`, `UpdateStatus`, `LoadCosts`, `LoadMoreCosts`, `AssignCaretaker`, `ExportPdf`

---

### 3. CreateDriverScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.CreateDriver` |
| ViewModel | `CreateDriverViewModel` |
| Contract | `CreateDriverContract` |

**Features:**
- Required fields: First Name, Last Name, Mobile, License Number, License Expiry
- Optional fields: Email, License Type (LMV/HMV/HPMV/HGMV/TRANS), DOB, Address, Emergency Contact, Blood Group, Joining Date
- Caretaker assignment dropdown
- Validation with field-level errors

---

### 4. DriverCostEntryScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.DriverCostEntry(driverId?)` |
| ViewModel | `DriverCostEntryViewModel` |
| Contract | `DriverCostEntryContract` |

**Features:**
- Driver selection dropdown (pre-selected if `driverId` provided)
- **Multi-row cost entry:** Add multiple cost rows in one submission
- Cost type selection with groups: Payment (salary, advance, bonus), Deduction (penalty, fine, damage), Other
- Dynamic cost types from API (`DriverCostTypes`)
- Date/time fields per row
- Amount field with automatic deduction sign for deduction types
- Notes per entry
- Cost history dialog
- Row expand/collapse, add/remove rows

---

## Facade

```kotlin
object DriverFeatureFacade {
    fun DriversListEntry(viewModel, onNavigateBack, onNavigateToDetail, onNavigateToAdd)
    fun CreateDriverEntry(viewModel, onNavigateBack, onDriverCreated)
    fun DriverDetailEntry(viewModel, driverId, onNavigateBack, onNavigateToAddDriverCost)
    fun DriverCostEntryEntry(viewModel, initialDriverId?, onNavigateBack)
}
```

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `Driver` | Full driver data (name, mobile, license, status, address, etc.) |
| `DriverStatus` | Enum: INACTIVE, ACTIVE, ON_ROUTE, ON_LEAVE, SUSPENDED, TERMINATED |
| `LicenseType` | Enum: LMV, HMV, HPMV, HGMV, TRANS |

---

## State Machine

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/drivers` | GET | List drivers (paginated) |
| `/drivers` | POST | Create driver |
| `/drivers/{id}` | GET | Get driver details |
| `/drivers/{id}` | PUT | Update driver |
| `/drivers/{id}` | DELETE | Delete driver |
| `/drivers/{id}/toggle-active` | PATCH | Toggle active/inactive |
| `/drivers/{id}/status` | PATCH | Update status |
| `/drivers/{id}/costs` | GET | Driver cost history |
| `/drivers/{id}/costs` | POST | Add driver cost(s) |
| `/drivers/cost-types` | GET | Dynamic cost type definitions |

