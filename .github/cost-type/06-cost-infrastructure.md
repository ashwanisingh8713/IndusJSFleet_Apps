# 06 — Cost Type Infrastructure

This doc covers the **technical plumbing** behind cost types: how they are fetched, cached, served to UI, and what fallback mechanisms exist.

---

## 1. Dynamic Fetch → Cache → Fallback Chain

Cost types are **server-driven** — the API returns the full hierarchy. The app caches them locally and falls back to hardcoded values if the cache is empty and the API is unreachable.

### Initialization Flow (App Startup)

```
App starts → AppInitializer
  ↓
InitializeCostTypesUseCase.invoke()
  ↓
CostTypesRepositoryImpl.initializeCostTypesIfNeeded()
  ↓
┌─ Check local cache (CostsLocalDataSource.hasCostTypesCached())
│
├─ CACHED & VALID?
│  └─ Return Success (skip API call)
│
├─ CACHED but EMPTY/CORRUPT?
│  └─ Clear cache → proceed to API fetch
│
├─ NOT CACHED?
│  ├─ Check auth token exists
│  │  ├─ NO TOKEN → Return Success (skip, will retry later)
│  │  └─ TOKEN EXISTS → Fetch from API:
│  │       ├─ GET /cost-types/trip        → saveTripCostTypes()
│  │       ├─ GET /cost-types/maintenance → saveMaintenanceCostTypes()
│  │       └─ GET /cost-types/driver      → saveDriverCostTypes()
│  │
│  └─ If ALL fail → Return Success anyway (hardcoded fallback will be used)
└─
```

### Runtime Access Flow (When UI Needs Cost Types)

```
ViewModel calls GetTripCostTypesUseCase()
  ↓
CostTypesRepositoryImpl.getTripCostTypesFlat()
  ↓
┌─ localDataSource.getTripCostTypes()
│
├─ CACHED? → CostTypeCategoryDto.toFlatList()
│
├─ NOT CACHED? → TripCostTypes.groups (hardcoded fallback)
│                 → groupsToFlatList(groups)
└─
Result: List<Pair<String, String>>  // (id, label) pairs
```

### Manual Refresh Flow

```
User taps "Refresh" in cost entry screen
  ↓
ViewModel calls refreshTripCostTypes()
  ↓
CostTypesRepositoryImpl.refreshTripCostTypes()
  ↓
clearTripCostTypes() → fetch from API → saveTripCostTypes()
  ↓
Return Result<Unit>
```

---

## 2. Local Cache Implementation

**Interface:** `CostsLocalDataSource` (in `ijs-network-lib`)
**Implementation:** `CostsLocalDataSourceImpl` (in `sharedUI`, uses `multiplatform-settings`)

Storage: JSON-serialized `CostTypeCategoryDto` stored as strings in `Settings`:

| Key | Value |
|-----|-------|
| `trip_cost_types_cache` | JSON of `CostTypeCategoryDto` for trip costs |
| `maintenance_cost_types_cache` | JSON of `CostTypeCategoryDto` for maintenance costs |
| `driver_cost_types_cache` | JSON of `CostTypeCategoryDto` for driver costs |

Cache operations are implemented in `CostTypesDao` (in `sharedUI`):

```kotlin
// Save
settings.putString(KEY_TRIP_COST_TYPES, json.encodeToString(entity))

// Read
settings.getStringOrNull(KEY_TRIP_COST_TYPES)?.let { json.decodeFromString(it) }

// Check
settings.getStringOrNull(KEY_TRIP_COST_TYPES) != null

// Clear
settings.remove(KEY_TRIP_COST_TYPES)
```

---

## 3. Hardcoded Fallback Objects

When the cache is empty and the API is unreachable, the app uses these fallback objects:

| Object | Module | Groups | Items | File |
|--------|--------|--------|-------|------|
| `TripCostTypes` | `ijs-core-lib` | 6 | 22 | `CostApiResponses.kt` |
| `MaintenanceCostTypes` | `ijs-core-lib` | 6 | 23 | `CostApiResponses.kt` |
| `DriverCostTypes` | `ijs-core-lib` | 4 | 19 | `DriverCostModels.kt` |
| `FuelTypes` | `ijs-core-lib` | — | 5 | `CostApiResponses.kt` |

> **These are the source of truth for IDs and labels.** If `CostTypeUtils.kt` ever diverges from these, the fallback objects are correct.

---

## 4. API Response Format

All three cost type endpoints return the same structure:

```json
{
  "success": true,
  "message": "Trip cost types fetched",
  "data": {
    "category_id": "trip-costs",
    "category_name": "Trip Costs",
    "groups": [
      {
        "group_id": "TC-G-001",
        "group_name": "Fuel & Energy",
        "items": [
          { "id": "TC-001-001", "value": "TC-001-001", "label": "Petrol" },
          { "id": "TC-001-002", "value": "TC-001-002", "label": "Diesel" }
        ]
      }
    ]
  }
}
```

Mapped to Kotlin:

```
CostTypesApiResponse
  └── data: CostTypeCategoryDto
        ├── categoryId: String
        ├── categoryName: String
        └── groups: List<CostTypeGroupDto>
              ├── groupId: String
              ├── groupName: String
              └── items: List<CostTypeItemDto>
                    ├── id: String
                    ├── value: String
                    └── label: String
```

---

## 5. UI Utilities

### CostTypeUtils (in `ijs-core-lib`)

Static utility for formatting cost types in UI:

| Method | Input | Output | Notes |
|--------|-------|--------|-------|
| `getDisplayName(costTypeOrId)` | `"TC-001-002"` or `"fuel"` | `"Diesel"` or `"Fuel"` | Handles structured IDs + legacy strings |
| `getIcon(costTypeOrId)` | `"TC-001-002"` | `"⛽"` | Emoji icon |
| `getColor(costTypeOrId)` | `"TC-001-002"` | `Color(0xFF4CAF50)` | Predefined color. Uses `startsWith` for group-level matching |
| `getIconByGroup(groupId)` | `"TC-G-001"` | `"⛽"` | Group-level icon |
| `getColorByGroup(groupId)` | `"TC-G-001"` | `Color(0xFF4CAF50)` | Group-level color |

### Composable Extensions

| Function | Purpose |
|----------|---------|
| `getCostTypeColorComposable(costTypeOrId)` | Returns `CostTypeUtils.getColor()` or falls back to `MaterialTheme.colorScheme.primary` |
| `getCostColorByGroupComposable(groupId)` | Returns `CostTypeUtils.getColorByGroup()` or falls back to `MaterialTheme.colorScheme.primary` |

### `getColor` Defensive Behavior

`getColor` uses `startsWith("TC-006")` prefix matching rather than exact ID matching. This means:
- Any unknown future ID within a group prefix will get the group's default color.
- Phantom/legacy IDs that no longer exist in the fallback will still render with a valid color instead of crashing.
- The fallback default is Grey `#9E9E9E`, which the Composable extension replaces with `MaterialTheme.colorScheme.primary`.

### CostBreakdownItemDto (in `ijs-core-lib`)

Unified DTO for cost breakdowns used in P&L reports and dashboard:

```kotlin
data class CostBreakdownItemDto(
    val costId: String? = null,       // New structured ID
    val costLabel: String? = null,    // Human-readable label
    val groupId: String? = null,      // Group for categorization
    val costType: String = "",        // Legacy cost type string
    val amount: Double = 0.0,
    val count: Int = 0,
    val percentage: Double = 0.0
) {
    val displayLabel: String
        get() = costLabel ?: costType.replace("_", " ").replaceFirstChar { it.uppercase() }
}
```

### TripCostComponents (in `ijs-ui-components-lib`)

Reusable Compose components for displaying costs:

| Component | Purpose |
|-----------|---------|
| `TotalCostHeader` | Prominent card showing total expenses, transaction count, category count |
| `ExportPdfButton` | Button to export costs as PDF |
| Cost item cards | Individual cost entry display with icon, amount, date |

---

## 6. Use Cases

| Use Case | Module | Purpose |
|----------|--------|---------|
| `InitializeCostTypesUseCase` | `ijs-network-lib` | App startup — fetch + cache if not already cached |
| `GetTripCostTypesUseCase` | `ijs-network-lib` | Get trip cost types (flat or grouped) |
| `GetMaintenanceCostTypesUseCase` | `ijs-network-lib` | Get maintenance cost types (flat or grouped) |
| `GetDriverCostTypesUseCase` | `ijs-network-lib` | Get driver cost types (flat or grouped) |

Each `Get*UseCase` provides two methods:
- `invoke(): List<Pair<String, String>>` — flat list for dropdowns
- `getGrouped(): CostTypeCategoryDto?` — full hierarchy for grouped selectors

---

## 7. Complete Source File Reference

| File | Module | Purpose |
|------|--------|---------|
| **DTOs & Fallback** | | |
| `CostModels.kt` | `ijs-core-lib` | `CostTypeCategoryDto`, `CostTypeGroupDto`, `CostTypeItemDto` |
| `CostApiResponses.kt` | `ijs-core-lib` | `TripCostTypes`, `MaintenanceCostTypes`, `FuelTypes` fallback + API response wrappers |
| `CostEntityModels.kt` | `ijs-core-lib` | `TripCostDto`, `MaintenanceCostDto`, request DTOs, bulk DTOs |
| `CostBreakdownItemDto.kt` | `ijs-core-lib` | Unified cost breakdown item |
| `DriverCostModels.kt` | `ijs-core-lib` | `DriverCostTypes` fallback + driver cost DTOs |
| **Infrastructure** | | |
| `CostTypesRepository.kt` | `ijs-network-lib` | Repository interface |
| `CostTypesRepositoryImpl.kt` | `ijs-network-lib` | Repository implementation with fetch/cache/fallback logic |
| `CostsRemoteDataSource.kt` | `ijs-network-lib` | HTTP client for all cost APIs |
| `CostsLocalDataSource.kt` | `ijs-network-lib` | Local cache interface |
| `CostsLocalDataSourceImpl.kt` | `sharedUI` | Settings-based cache implementation |
| `CostTypesDao.kt` | `sharedUI` | Low-level Settings read/write for cost types |
| `ApiConfig.kt` | `ijs-network-lib` | API endpoint constants (`/cost-types/trip`, etc.) |
| **Use Cases** | | |
| `InitializeCostTypesUseCase.kt` | `ijs-network-lib` | App startup initialization |
| `GetCostTypesUseCase.kt` | `ijs-network-lib` | `GetTripCostTypesUseCase`, `GetMaintenanceCostTypesUseCase`, `GetDriverCostTypesUseCase` |
| **UI Utilities** | | |
| `CostTypeUtils.kt` | `ijs-core-lib` | Display name, icon, color mapping |
| `TripCostComponents.kt` | `ijs-ui-components-lib` | Reusable Compose cost display components |

