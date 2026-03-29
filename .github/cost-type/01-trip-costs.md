# 01 — Trip Cost Types (TC-*)

Trip costs are **per-trip operational expenses** incurred during a trip's lifecycle. Each cost entry is linked to a `trip_id` and optionally to a `vehicle_id`.

---

## Groups & Items

### TC-G-001 — Fuel & Energy (5 items)

| ID | Label | Icon | Color | Notes |
|----|-------|------|-------|-------|
| `TC-001-001` | Petrol | ⛽ | Green `#4CAF50` | |
| `TC-001-002` | Diesel | ⛽ | Green `#4CAF50` | Default fuel type |
| `TC-001-003` | CNG/LPG | ⛽ | Green `#4CAF50` | |
| `TC-001-004` | EV Charging | 🔌 | Green `#4CAF50` | |
| `TC-001-005` | AdBlue/DEF | ⛽ | Green `#4CAF50` | Diesel exhaust fluid |

**Fuel-specific fields** (only for TC-G-001 items):

| Field | Type | Description |
|-------|------|-------------|
| `fuel_type` | `String?` | One of TC-001-001..005. Default: `TC-001-002` (Diesel) |
| `fuel_quantity` | `Double?` | Litres/units |
| `fuel_rate` | `Double?` | Price per litre/unit |
| `km_per_liter` | `Double?` | Mileage at this fill |

Fuel detection: `isFuelCost = groupId == "TC-G-001" || costType == "fuel"`

### TC-G-002 — Toll & Parking (3 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `TC-002-001` | Toll Charges | 🛣️ | Blue `#2196F3` |
| `TC-002-002` | Parking Fees | 🅿️ | Purple `#9C27B0` |
| `TC-002-003` | Entry Charges | 🚧 | Deep Purple `#673AB7` |

### TC-G-003 — Loading & Unloading (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `TC-003-001` | Loading Charges | 📦 | Brown `#795548` |
| `TC-003-002` | Unloading Charges | 📤 | Brown `#795548` |
| `TC-003-003` | Crane/Forklift | 🏗️ | Brown `#795548` |
| `TC-003-004` | Labor Charges | 👷 | Brown `#795548` |

### TC-G-004 — Driver Expenses (3 items)

| ID | Label | Icon | Color | Auto-sync to Driver? |
|----|-------|------|-------|----------------------|
| `TC-004-001` | Driver Allowance | 👤 | Orange `#FF9800` | ✅ → `DC-004-001` |
| `TC-004-002` | Food & Meals | 🍽️ | Orange `#FF9800` | ✅ → `DC-004-002` |
| `TC-004-003` | Accommodation | 🏨 | Orange `#FF9800` | ✅ → `DC-004-002` |

> **Cross-category sync:** All TC-G-004 items are automatically synced to the driver's cost history (DC-G-004) via `TripCostToDriverCostMapper`. See [04-cost-relations-and-aggregation.md](04-cost-relations-and-aggregation.md).

### TC-G-005 — Permits & Compliance (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `TC-005-001` | State Permit | 🎫 | Cyan `#00BCD4` |
| `TC-005-002` | National Permit | 🎫 | Cyan `#00BCD4` |
| `TC-005-003` | Chalan/Fine | 📄 | Cyan `#00BCD4` |
| `TC-005-004` | Weighbridge | ⚖️ | Cyan `#00BCD4` |

### TC-G-006 — Miscellaneous (3 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `TC-006-001` | Police/RTO | 👮 | Blue Grey `#607D8B` |
| `TC-006-002` | Commission | 💼 | Blue Grey `#607D8B` |
| `TC-006-003` | Other | 💵 | Blue Grey `#607D8B` |

**Total: 6 groups, 22 items.**

---

## Date & Time Format

| Context | Date Format | Time Format | Example |
|---------|-------------|-------------|---------|
| Individual trip cost | `DD-MM-YYYY` | `HH:MM` (24hr) | `"date": "20-12-2025", "time": "10:30"` |
| Bulk trip cost | ISO 8601 | ISO 8601 | `"date_time": "2025-12-20T10:30:00Z"` |
| UI display | `DD-MM-YYYY` | `HH:MM` | `20-12-2025`, `10:30` |

---

## Legacy Cost Type Mapping

Old API responses may use flat string cost types. These are mapped via `CostTypeUtils.getDisplayName`:

| Legacy `cost_type` | Display Name | Closest Structured ID |
|--------------------|--------------|------------------------|
| `fuel` | Fuel | TC-G-001 group |
| `toll` | Toll | TC-002-001 |
| `driver_allowance` | Driver Allowance | TC-004-001 |
| `parking` | Parking | TC-002-002 |
| `loading_charges` | Loading Charges | TC-003-001 |
| `unloading_charges` | Unloading Charges | TC-003-002 |
| `chalan` | Chalan | TC-005-003 |
| `permit` | Permit | TC-005-001 |
| `insurance` | Insurance | *(no TC equivalent — legacy only)* |
| `other` | Other | TC-006-003 |

---

## API Endpoints

### Cost Type Definitions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/cost-types/trip` | No | Fetch trip cost type hierarchy |

### Trip Cost CRUD

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/trip-costs` | Yes | Create single trip cost |
| `POST` | `/trips/{tripId}/costs/bulk` | Yes | Bulk create trip costs |
| `GET` | `/trips/{tripId}/costs` | Yes | List trip costs |
| `GET` | `/trips/{tripId}/costs/summary` | Yes | Cost summary by type |
| `DELETE` | `/trip-costs/{costId}` | Yes | Delete trip cost |

### Vehicle Trip Costs

| Method | Endpoint | Auth | Query Params |
|--------|----------|------|-------------|
| `GET` | `/vehicles/{vehicleId}/trip-costs` | Yes | `page`, `per_page`, `cost_type`, `start_date`, `end_date`, `sort_by`, `sort_order` |

---

## DTOs (defined in `ijs-core-lib`)

| DTO | File | Purpose |
|-----|------|---------|
| `TripCostDto` | `CostEntityModels.kt` | API response for a single trip cost |
| `CreateTripCostRequest` | `CostEntityModels.kt` | POST body for single cost |
| `BulkTripCostItem` | `CostEntityModels.kt` | Item in bulk create request |
| `BulkCreateTripCostsRequest` | `CostEntityModels.kt` | Bulk POST body |
| `TripCostSummaryDto` | `CostEntityModels.kt` | Aggregated summary by type |
| `TripCostsListDataDto` | `CostApiResponses.kt` | List wrapper with total |
| `VehicleTripCostsDataDto` | `CostEntityModels.kt` | Paginated vehicle trip costs |

---

## Source Files

| File | Module | Purpose |
|------|--------|---------|
| `CostApiResponses.kt` | `ijs-core-lib` | `TripCostTypes` fallback + API response wrappers |
| `CostEntityModels.kt` | `ijs-core-lib` | `TripCostDto`, request DTOs |
| `CostTypeUtils.kt` | `ijs-core-lib` | Display name, icon, color for TC-* IDs |
| `TripCostComponents.kt` | `ijs-ui-components-lib` | Reusable Compose cost display components |
| `CostEntryRow` | `screen-trip` | UI state for cost entry form row |
| `TripCostEntryContract.kt` | `screen-trip` | MVI contract for trip cost entry |

