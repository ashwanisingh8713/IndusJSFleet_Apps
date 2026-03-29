# 02 — Maintenance Cost Types (MC-*)

Maintenance costs are **per-vehicle repair and upkeep expenses**. Each cost entry is linked to a `vehicle_id`. They are NOT trip-specific — they cover workshop repairs, routine services, and part replacements regardless of trip activity.

---

## Groups & Items

### MC-G-001 — Engine & Mechanical (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `MC-001-001` | Engine Repair | ⚙️ | Light Green `#8BC34A` |
| `MC-001-002` | Transmission | ⚙️ | Light Green `#8BC34A` |
| `MC-001-003` | Clutch/Brake | ⚙️ | Light Green `#8BC34A` |
| `MC-001-004` | Suspension | ⚙️ | Light Green `#8BC34A` |

### MC-G-002 — Body & Exterior (3 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `MC-002-001` | Body Repair | 🚗 | Deep Orange `#FF5722` |
| `MC-002-002` | Paint Job | 🚗 | Deep Orange `#FF5722` |
| `MC-002-003` | Glass/Mirror | 🚗 | Deep Orange `#FF5722` |

### MC-G-003 — Tyres & Wheels (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `MC-003-001` | Tyre Replacement | 🛞 | Dark Grey `#424242` |
| `MC-003-002` | Tyre Repair | 🛞 | Dark Grey `#424242` |
| `MC-003-003` | Wheel Alignment | 🛞 | Dark Grey `#424242` |
| `MC-003-004` | Wheel Balancing | 🛞 | Dark Grey `#424242` |

### MC-G-004 — Electrical & Electronics (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `MC-004-001` | Battery | 🔋 | Yellow `#FFEB3B` |
| `MC-004-002` | Alternator/Starter | ⚡ | Amber `#FFC107` |
| `MC-004-003` | Wiring/Lights | ⚡ | Amber `#FFC107` |
| `MC-004-004` | AC Repair | ❄️ | Light Blue `#03A9F4` |

### MC-G-005 — Fuel & Fluids (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `MC-005-001` | Engine Oil | 🛢️ | Teal `#009688` |
| `MC-005-002` | Coolant | 🛢️ | Teal `#009688` |
| `MC-005-003` | Brake Fluid | 🛢️ | Teal `#009688` |
| `MC-005-004` | Gear Oil | 🛢️ | Teal `#009688` |

### MC-G-006 — Routine Service (4 items)

| ID | Label | Icon | Color |
|----|-------|------|-------|
| `MC-006-001` | Regular Service | 🔧 | Teal `#009688` |
| `MC-006-002` | Washing/Cleaning | 🔧 | Teal `#009688` |
| `MC-006-003` | Inspection | 🔧 | Teal `#009688` |
| `MC-006-004` | Other | 🔧 | Teal `#009688` |

**Total: 6 groups, 23 items.**

---

## Unique Fields (vs Trip Costs)

Maintenance costs have two fields that trip costs do **not**:

| Field | Type | Description |
|-------|------|-------------|
| `vendor_name` | `String?` | Workshop / service provider name |
| `invoice_no` | `String?` | Vendor invoice reference |

Maintenance costs also carry both `description` **and** `notes` (trip costs only have `notes`).

### Field Comparison: Trip Cost vs Maintenance Cost

| Field | Trip Cost | Maintenance Cost |
|-------|-----------|------------------|
| `notes` | ✅ | ✅ |
| `description` | ❌ | ✅ |
| `vendor_name` | ❌ | ✅ |
| `invoice_no` | ❌ | ✅ |
| `fuel_quantity` | ✅ (fuel only) | ❌ |
| `fuel_rate` | ✅ (fuel only) | ❌ |
| `fuel_type` | ✅ (fuel only) | ❌ |
| `km_per_liter` | ✅ (fuel only) | ❌ |
| `trip_id` | ✅ (required) | ❌ |

---

## Date & Time Format

| Context | Date Format | Time Format | Example |
|---------|-------------|-------------|---------|
| Individual cost | `DD-MM-YYYY` | `HH:MM` (24hr) | `"date": "20-12-2025", "time": "14:00"` |
| Bulk cost | ISO 8601 | ISO 8601 | `"date_time": "2025-12-20T14:00:00Z"` |
| UI display | `DD-MM-YYYY` | `HH:MM` | `20-12-2025`, `14:00` |

---

## Legacy Cost Type Mapping

| Legacy `cost_type` | Display Name | Closest Structured ID |
|--------------------|--------------|------------------------|
| `tyre` | Tyre | MC-G-003 group |
| `battery` | Battery | MC-004-001 |
| `servicing` | Servicing | MC-006-001 |
| `engine_repair` | Engine Repair | MC-001-001 |
| `body_repair` | Body Repair | MC-002-001 |
| `electrical` | Electrical | MC-G-004 group |
| `ac_repair` | AC Repair | MC-004-004 |
| `other` | Other | MC-006-004 |

---

## API Endpoints

### Cost Type Definitions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/cost-types/maintenance` | No | Fetch maintenance cost type hierarchy |

### Maintenance Cost CRUD

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/maintenance-costs` | Yes | Create single maintenance cost |
| `POST` | `/vehicles/{vehicleId}/maintenance-costs/bulk` | Yes | Bulk create maintenance costs |
| `GET` | `/vehicles/{vehicleId}/maintenance-costs` | Yes | List vehicle maintenance costs |
| `DELETE` | `/maintenance-costs/{costId}` | Yes | Delete maintenance cost |

### Vehicle Maintenance Costs (paginated)

| Method | Endpoint | Auth | Query Params |
|--------|----------|------|-------------|
| `GET` | `/vehicles/{vehicleId}/maintenance-costs` | Yes | `page`, `per_page`, `cost_type`, `start_date`, `end_date`, `sort_by`, `sort_order` |

---

## DTOs (defined in `ijs-core-lib`)

| DTO | File | Purpose |
|-----|------|---------|
| `MaintenanceCostDto` | `CostEntityModels.kt` | API response for a single maintenance cost |
| `CreateMaintenanceCostRequest` | `CostEntityModels.kt` | POST body for single cost |
| `BulkMaintenanceCostItem` | `CostEntityModels.kt` | Item in bulk create request |
| `BulkCreateMaintenanceCostsRequest` | `CostEntityModels.kt` | Bulk POST body |
| `MaintenanceCostsListDataDto` | `CostApiResponses.kt` | List wrapper with total |
| `VehicleMaintenanceCostsDataDto` | `CostEntityModels.kt` | Paginated vehicle maintenance costs |

---

## Source Files

| File | Module | Purpose |
|------|--------|---------|
| `CostApiResponses.kt` | `ijs-core-lib` | `MaintenanceCostTypes` fallback + API response wrappers |
| `CostEntityModels.kt` | `ijs-core-lib` | `MaintenanceCostDto`, request DTOs |
| `CostTypeUtils.kt` | `ijs-core-lib` | Legacy display name/icon/color (MC uses legacy strings in `getDisplayName` else branch) |

