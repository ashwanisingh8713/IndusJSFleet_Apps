# screen-vehicle

## Overview

**Package:** `com.ijs.vehicle`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Complete vehicle lifecycle management — register vehicles with documents (RC, Insurance, Fitness, Permit, Pollution), track vehicle status transitions, manage maintenance costs (tyre, battery, servicing, engine repair, etc.), view trip history, manage documents with upload/preview/download, assign caretakers, and export cost reports as PDF.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `VehicleFeatureFacade`, `VehicleStateOptions`, List (Contract/VM/Screen), Detail (Contract/VM/Screen), Add Vehicle (Contract/VM/Screen), Maintenance Cost Entry (Contract/VM/Screen) |
| **Domain** | `Vehicle`, `VehicleDetail`, `VehicleStatus`, `VehicleDocument`, `DocumentType` entities; `VehicleRepository` interface; `VehicleUseCases` |
| **Data** | `VehicleRemoteDataSource`, `VehicleRepositoryImpl`, `VehicleMapper`, `VehicleDto` |

---

## Dependencies

```
screen-vehicle → ijs-network-lib → ijs-core-lib
screen-vehicle → screen-team (TeamMemberDto for caretaker assignment)
screen-vehicle → screen-driver (Driver entity for assigned driver display)
screen-vehicle → ijs-pdf-report, ijs-datetime-picker
```

**Cross-feature note:** Depends on `screen-team` for caretaker selection and `screen-driver` for displaying the assigned driver on vehicle details.

---

## Screens

### 1. VehiclesScreen (List)

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Vehicles` |
| ViewModel | `VehiclesViewModel` |
| Contract | `VehiclesContract` |

**Features:**
- Status filter chips: All, Active, Inactive, On Route, Maintenance, Damaged, Decommissioned
- Vehicle cards with registration number, make/model, status badge, assigned driver
- Search by registration number
- Pull-to-refresh
- FAB to add new vehicle

**Key Intents:** `LoadVehicles`, `RefreshVehicles`, `FilterByStatus`, `SearchVehicles`, `SelectVehicle`, `AddVehicle`

---

### 2. VehicleDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.VehicleDetail(vehicleId)` |
| ViewModel | `VehicleDetailViewModel` |
| Contract | `VehicleDetailContract` |

**Features:**
- **Tabs:** Overview, Trips, Documents, Costs
- **Overview tab:**
  - Vehicle info: Registration, Make, Model, Year, Type, Chassis, Engine number
  - Status badge with valid transition buttons
  - Assigned driver display
  - Caretaker assignment dropdown (team members)
  - Edit mode for all fields
- **Trips tab:** Trip history for this vehicle
- **Documents tab:**
  - Document cards: RC, Insurance, Fitness Certificate, Permit, Pollution Certificate
  - Upload document with file picker
  - Document preview/download
  - Expiry date tracking with color-coded warnings
  - Delete document
- **Costs tab:**
  - Maintenance cost history with breakdown
  - Date range filter
  - Cost type filter
  - Pagination
  - "Add Maintenance Cost" button
  - PDF export

**File operations:** Vehicle detail supports file upload via `onRequestFilePicker` callback, document preview via `onOpenDocumentPreview`, download via `onDownloadDocument`, and save via `onSaveDocument` — all platform-specific callbacks wired in `sharedUI`.

---

### 3. AddVehicleScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.AddVehicle` |
| ViewModel | `AddVehicleViewModel` |
| Contract | `AddVehicleContract` |

**Features:**
- **Vehicle info section:** Registration Number*, Make, Model, Year, Vehicle Type (Truck, Van, etc.), Chassis Number, Engine Number
- **Document section:** Upload RC, Insurance, Fitness, Permit, Pollution documents with expiry dates
- **Assignment section:** Assign caretaker (team member)
- File picker integration for document uploads
- Validation: registration number format, required fields
- On success → navigates to VehicleDetail

---

### 4. MaintenanceCostEntryScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.MaintenanceCostEntry(vehicleId?)` |
| ViewModel | `MaintenanceCostEntryViewModel` |
| Contract | `MaintenanceCostEntryContract` |

**Features:**
- Vehicle selection dropdown (pre-selected if `vehicleId` provided)
- Multi-row cost entry
- Maintenance cost types: tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other
- Dynamic cost types from API (`MaintenanceCostTypes`)
- Date/time fields per row
- Amount and notes per row
- Cost history dialog
- Add/remove rows

---

## Facade

```kotlin
object VehicleFeatureFacade {
    fun VehiclesListEntry(viewModel, onNavigateBack, onNavigateToDetail, onNavigateToAdd)
    fun AddVehicleEntry(viewModel, onNavigateBack, onVehicleRegistered, onNavigateToCreateTeamMember, onRequestFilePicker)
    fun VehicleDetailEntry(viewModel, vehicleId, onNavigateBack, onNavigateToMaintenanceCost, onRequestFilePicker?, onOpenDocumentPreview?, onDownloadDocument?, onSaveDocument?)
    fun MaintenanceCostEntryEntry(viewModel, initialVehicleId?, onNavigateBack)
}
```

---

## State Machine

```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned
```

Valid transitions enforced by `VehicleStatus.getValidTransitions()` → `StatusConstants.VehicleTransitions`.

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `Vehicle` | List-level data (id, registration, make, model, status, driver) |
| `VehicleDetail` | Full detail with documents, costs, trips |
| `VehicleStatus` | Enum: INACTIVE, ACTIVE, ON_ROUTE, MAINTENANCE, DAMAGED, DECOMMISSIONED |
| `VehicleDocument` | Document with type, file URL, expiry date |
| `DocumentType` | Enum: RC, INSURANCE, FITNESS, PERMIT, POLLUTION, OTHER |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles` | GET | List vehicles (paginated, filterable) |
| `/vehicles` | POST | Register new vehicle |
| `/vehicles/{id}` | GET | Get vehicle details |
| `/vehicles/{id}` | PUT | Update vehicle |
| `/vehicles/{id}/status` | PATCH | Update vehicle status |
| `/vehicles/{id}/documents` | GET | List vehicle documents |
| `/vehicles/{id}/documents` | POST | Upload document |
| `/vehicles/{id}/documents/{docId}` | DELETE | Delete document |
| `/vehicles/{id}/maintenance-costs` | GET | Maintenance cost history |
| `/vehicles/{id}/maintenance-costs` | POST | Add maintenance cost(s) |
| `/vehicles/maintenance-cost-types` | GET | Dynamic cost type definitions |

---

## Document Types & Expiry Tracking

| Document | Required | Expiry Tracked | Alert Threshold |
|----------|----------|----------------|-----------------|
| RC (Registration Certificate) | Yes | Yes | 30 days |
| Insurance | Yes | Yes | 30 days |
| Fitness Certificate | Yes | Yes | 30 days |
| Permit | Conditional | Yes | 30 days |
| Pollution Certificate | Yes | Yes | 30 days |

Documents expiring within 7 days appear as **critical** alerts, 7–30 days as **warning** alerts.

