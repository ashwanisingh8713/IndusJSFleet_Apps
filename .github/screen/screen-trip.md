# screen-trip

## Overview

**Package:** `com.ijs.trip`
**Module type:** Full-stack feature module (Data + Domain + Presentation)
**Purpose:** Complete trip lifecycle management — create trips with route planning (Google Places API), assign vehicles/drivers/customers, manage cargo, schedule departures/arrivals, track trip state transitions, record trip costs (fuel, tolls, etc.), manage trip payments, and support inline editing of all trip details.

This is the **most complex feature module** in the application, with the most screens, the most cross-feature dependencies, and the richest state management.

---

## Architecture

| Layer | Contents |
|-------|----------|
| **Presentation** | `TripFeatureFacade`, `TripStateOptions`, List (Contract/VM/Screen), Create (Contract/VM/Screen), Detail (Contract/VM/Screen + 10 handler/component files), Cost Entry (Contract/VM/Screen + mapper) |
| **Domain** | `Trip`, `TripStatus`, `TripDetail`, `CargoType` entities; `TripRepository` interface; `TripUseCases` |
| **Data** | `TripRemoteDataSource`, `TripRepositoryImpl`, `TripMapper`, `TripDto` |

**Detail screen refactoring (500-line limit compliance):**
- `TripDetailViewModel.kt` — intent routing, primary state management
- `TripDetailDataLoader.kt` — loads trip, costs, payments data
- `TripDetailActionHandler.kt` — save, cancel, status transitions
- `TripDetailStateManager.kt` — state update helpers
- `TripDetailLocationHandler.kt` — Google Places autocomplete integration
- `TripDetailScreen.kt` — main screen scaffold with tabs
- `TripDetailViewContent.kt` — view mode content
- `TripDetailEditContent.kt` — edit mode forms
- `TripDetailEditForms.kt` — individual form sections
- `TripDetailInfoSections.kt` — info display sections
- `TripDetailComponents.kt` — reusable detail components
- `TripCostsContent.kt` — costs tab content
- `TripPaymentsContent.kt` — payments tab content

---

## Dependencies

```
screen-trip → ijs-network-lib → ijs-core-lib
screen-trip → screen-vehicle (Vehicle entity for vehicle selection)
screen-trip → screen-driver (Driver entity for driver selection)
screen-trip → screen-customer (Customer entity for customer selection)
screen-trip → screen-payment (TripPayment entity for payment display)
screen-trip → ijs-pdf-report, ijs-datetime-picker
```

**Most cross-feature dependencies** of any module. Trip creation requires vehicle, driver, and customer selection. Trip detail shows costs and payments.

---

## Screens

### 1. TripsScreen (List)

| Property | Value |
|----------|-------|
| Route | `FleetRoute.Trips` |
| ViewModel | `TripsViewModel` |
| Contract | `TripsContract` |

**Features:**
- Status filter tabs: All, Planned, On Route, Completed, Cancelled, Failed, Delayed
- Trip cards with route (start→end), vehicle, driver, status badge, scheduled date
- Price display (Owner/GM only — Managers see "N/A")
- Search by route locations
- FAB to create new trip

---

### 2. CreateTripScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.CreateTrip` |
| ViewModel | `CreateTripViewModel` |
| Contract | `CreateTripContract` |

**Features:**
- **Route section:** Start location, End location (Google Places autocomplete with distance calculation)
- **Assignment section:** Vehicle dropdown (active vehicles), Driver dropdown (active drivers)
- **Customer section:** Customer selection with "Add New Customer" option
- **Schedule section:** Departure date/time, Expected arrival date/time (ISO 8601 conversion)
- **Cargo section:** Cargo type (Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others), cargo weight, description
- **Pricing section:** Purchase price, Selling value (Owner/GM only)
- **Notes field**

**Google Places integration:** Uses `GooglePlacesService` from `ijs-network-lib` for location autocomplete and `Distance Matrix API` for route distance calculation.

---

### 3. TripDetailScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.TripDetail(tripId)` |
| ViewModel | `TripDetailViewModel` |
| Contract | `TripDetailContract` |

**Features (View mode):**
- Trip header with status badge and state transition buttons
- **Route info:** Start → End locations, distance
- **Assignment info:** Vehicle (reg number), Driver (name), Customer (company)
- **Schedule info:** Departure/arrival dates, actual vs scheduled
- **Cargo info:** Type, weight, description
- **Pricing info:** Purchase price, Selling value (Owner/GM only)
- **Costs tab:** Trip cost list with breakdown, "Add Cost" button
- **Payments tab:** Payment list with summary, "Add Payment" button

**Features (Edit mode):**
- Inline editing of all fields (route, assignment, schedule, cargo, pricing)
- Google Places autocomplete for route editing
- Vehicle/driver/customer re-assignment
- Save/Cancel with validation
- Status transitions: Planned→On Route, On Route→Completed, Planned→Cancelled, etc.

---

### 4. TripCostEntryScreen

| Property | Value |
|----------|-------|
| Route | `FleetRoute.TripCostEntry(tripId?, vehicleId?)` |
| ViewModel | `TripCostEntryViewModel` |
| Contract | `TripCostEntryContract` |

**Features:**
- Trip selection dropdown (pre-selected if `tripId` provided)
- Multi-row cost entry (add multiple costs at once)
- Cost type selection: fuel, toll, driver_allowance, parking, loading_charges, unloading_charges, chalan, permit, insurance, other
- Dynamic cost types from API (`TripCostTypes`)
- Date/time fields per row
- Amount and notes per row
- Driver cost linking (optional: some trip costs can also create driver costs via `TripCostToDriverCostMapper`)

---

## Facade

```kotlin
object TripFeatureFacade {
    fun TripsListEntry(viewModel, onNavigateBack, onNavigateToDetail, onNavigateToCreate)
    fun CreateTripEntry(viewModel, onNavigateBack, onTripCreated, onNavigateToAddCustomer)
    fun TripDetailEntry(viewModel, tripId, onNavigateBack, onNavigateToAddTripCost, onNavigateToAddPayment, onNavigateToAddCustomer)
    fun TripCostEntryEntry(viewModel, initialTripId?, onNavigateBack)
}
```

---

## State Machine

```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
delayed → on_route (resume)
```

Valid transitions are enforced by `TripStatus.getValidTransitions()` which delegates to `StatusConstants.TripTransitions`.

---

## Domain Entities

| Entity | Description |
|--------|-------------|
| `Trip` | Full trip data (route, assignment, schedule, cargo, pricing, status) |
| `TripStatus` | Enum: PLANNED, ON_ROUTE, COMPLETED, CANCELLED, FAILED, DELAYED |
| `CargoType` | Enum: GITTI, BALU, BHAKSHI, ENTA, HAZARDOUS, VALUABLE, OTHERS |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/trips` | GET | List trips (paginated, filterable by status) |
| `/trips` | POST | Create trip |
| `/trips/{id}` | GET | Get trip details |
| `/trips/{id}` | PUT | Update trip |
| `/trips/{id}/cancel` | PATCH | Cancel trip |
| `/trips/{id}/status` | PATCH | Update trip status |
| `/trips/{id}/costs` | GET | Trip cost history |
| `/trips/{id}/costs` | POST | Add trip cost(s) |
| `/trips/{id}/payments` | GET | Trip payment history |
| `/trips/cost-types` | GET | Dynamic cost type definitions |

---

## Date Format Handling

| Context | Format | Conversion |
|---------|--------|------------|
| Trip scheduling (departure/arrival) | ISO 8601 (`2026-01-04T14:30:00Z`) | `FleetDateTime.toIso8601(date, time)` |
| Trip costs | `DD-MM-YYYY` + `HH:MM` | Send as-is |
| UI display | `DD-MM-YYYY` + `HH:MM` (24hr) | Default |

