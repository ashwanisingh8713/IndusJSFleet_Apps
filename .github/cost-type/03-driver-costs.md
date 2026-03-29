# 03 — Driver Cost Types (DC-*)

Driver costs are **per-driver financial entries** covering salary, bonuses, deductions, and miscellaneous expenses. Each cost is linked to a `driver_id` and optionally to a `trip_id` (when the cost originated from a trip expense sync).

**Key distinction:** Driver costs have a **sign** — entries are either **earnings** (positive for the driver) or **deductions** (negative). The API tracks a net summary: `Net = Earnings − Deductions`.

---

## Groups & Items

### DC-G-001 — Salary & Wages (4 items) ➕ Earnings

| ID | Label | Sign |
|----|-------|------|
| `DC-001-001` | Monthly Salary | ➕ |
| `DC-001-002` | Daily Wages | ➕ |
| `DC-001-003` | Overtime | ➕ |
| `DC-001-004` | Holiday Pay | ➕ |

### DC-G-002 — Incentives & Bonuses (5 items) ➕ Earnings

| ID | Label | Sign |
|----|-------|------|
| `DC-002-001` | Trip Bonus | ➕ |
| `DC-002-002` | Performance Bonus | ➕ |
| `DC-002-003` | Fuel Savings | ➕ |
| `DC-002-004` | On-Time Delivery | ➕ |
| `DC-002-005` | Safety Bonus | ➕ |

### DC-G-003 — Deductions (5 items) ➖ Deductions

| ID | Label | Sign |
|----|-------|------|
| `DC-003-001` | Advance Recovery | ➖ |
| `DC-003-002` | Damage Deduction | ➖ |
| `DC-003-003` | Fine | ➖ |
| `DC-003-004` | Loan EMI | ➖ |
| `DC-003-005` | Insurance | ➖ |

> **Deduction detection:** `isDeductionCost = groupId == "DC-G-003" || isDeduction`
> The constant `DriverCostTypes.DEDUCTION_GROUP_ID = "DC-G-003"` is used by the UI to auto-set the `is_deduction` flag.

### DC-G-004 — Other (5 items) ➕ Earnings

| ID | Label | Sign | Notes |
|----|-------|------|-------|
| `DC-004-001` | Training | ➕ | Also receives trip driver allowance sync |
| `DC-004-002` | Uniform | ➕ | Also receives trip food/accommodation sync |
| `DC-004-003` | Medical | ➕ | |
| `DC-004-004` | License Renewal | ➕ | |
| `DC-004-005` | Other | ➕ | |

**Total: 4 groups, 19 items.**

---

## Deduction Logic & Summary

The API returns a summary with every driver cost listing:

```kotlin
data class DriverCostsSummaryDto(
    val totalEarnings: Double = 0.0,    // Sum of all non-deduction costs
    val totalDeductions: Double = 0.0,  // Sum of all deduction costs (DC-G-003)
    val netAmount: Double = 0.0,        // totalEarnings − totalDeductions
    val costCount: Int = 0              // Total cost entries
)
```

**Formula:** `Net Pay = Total Earnings − Total Deductions`

Detection in code (`DriverCostDto`):
```kotlin
val isDeductionCost: Boolean
    get() = groupId == DriverCostTypes.DEDUCTION_GROUP_ID || isDeduction
```

---

## Trip Linkage

Driver costs optionally link to a trip via `trip_id`. This happens when:
1. A trip cost in TC-G-004 (Driver Expenses) is saved → `TripCostToDriverCostMapper` auto-creates a corresponding driver cost with `trip_id` set.
2. A manual driver cost entry can also optionally reference a trip.

See [04-cost-relations-and-aggregation.md](04-cost-relations-and-aggregation.md) for full sync details.

---

## Month Field

Driver costs include an optional `month` field in `YYYY-MM` format (e.g., `"2025-12"`).

- Used for **monthly salary/deduction filtering** — "show all costs for December 2025."
- Extracted from the date field: `DD-MM-YYYY → YYYY-MM` (e.g., `"20-12-2025" → "2025-12"`).
- The API supports filtering by `month` query parameter.

---

## Structural Note: Flat vs Grouped

`DriverCostTypes` (in `ijs-core-lib`) exposes **both** accessors:

```kotlin
object DriverCostTypes {
    val types: List<Pair<String, String>> = listOf(...)   // Pre-built flat list
    val groups: List<CostTypeGroupDto> = listOf(...)      // Hierarchical groups
    const val DEDUCTION_GROUP_ID = "DC-G-003"
}
```

This differs from `TripCostTypes` and `MaintenanceCostTypes` which only expose `groups` and derive `types` via a computed getter:

```kotlin
object TripCostTypes {
    val types: List<Pair<String, String>>
        get() = groups.flatMap { group -> group.items.map { it.id to it.label } }  // Computed
    val groups: List<CostTypeGroupDto> = listOf(...)
}
```

Both approaches produce the same result; the difference is that `DriverCostTypes.types` is eagerly initialized.

---

## Date Format

| Context | Date Format | Example |
|---------|-------------|---------|
| Individual cost | `DD-MM-YYYY` | `"date": "20-12-2025"` |
| Month filter | `YYYY-MM` | `"month": "2025-12"` |
| UI display | `DD-MM-YYYY` | `20-12-2025` |

Driver costs do **not** have a separate `time` field.

---

## API Endpoints

### Cost Type Definitions

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/cost-types/driver` | No | Fetch driver cost type hierarchy |

### Driver Cost CRUD

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/drivers/{driverId}/costs` | Yes | Create single driver cost |
| `POST` | `/drivers/{driverId}/costs/bulk` | Yes | Bulk create driver costs |
| `GET` | `/drivers/{driverId}/costs` | Yes | List driver costs (`page`, `per_page`, `group_id`, `month`, `start_date`, `end_date`) |

---

## DTOs (defined in `ijs-core-lib`)

| DTO | File | Purpose |
|-----|------|---------|
| `DriverCostDto` | `DriverCostModels.kt` | API response for a single driver cost |
| `CreateDriverCostRequest` | `DriverCostModels.kt` | POST body for single cost |
| `BulkDriverCostItem` | `DriverCostModels.kt` | Item in bulk create request |
| `BulkCreateDriverCostsRequest` | `DriverCostModels.kt` | Bulk POST body |
| `DriverCostsListDto` | `DriverCostModels.kt` | List wrapper with summary + pagination |
| `DriverCostsSummaryDto` | `DriverCostModels.kt` | Earnings/deductions/net summary |

---

## Source Files

| File | Module | Purpose |
|------|--------|---------|
| `DriverCostModels.kt` | `ijs-core-lib` | `DriverCostTypes` fallback + all DTOs |
| `screen-driver/.../DriverCostModels.kt` | `screen-driver` | **Redirect file** — points to `ijs-core-lib` (moved to avoid circular deps) |
| `CostsRemoteDataSource.kt` | `ijs-network-lib` | `getDriverCosts`, `createDriverCost`, `bulkCreateDriverCosts` |
| `TripCostToDriverCostMapper.kt` | `screen-trip` | TC-G-004 → DC-G-004 auto-sync utility |

