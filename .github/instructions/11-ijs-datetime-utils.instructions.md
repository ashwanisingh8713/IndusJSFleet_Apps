# ijs-datetime-utils — IndusJS Fleet

## Purpose

Date/time conversion utilities. Wraps `kotlinx-datetime` with fleet-specific formatting.

## Package: `com.indusjs.datetimeutils`

## Key Object: `FleetDateTime`

| Function | Purpose |
|----------|---------|
| `toIso8601(date, time)` | DD-MM-YYYY + HH:MM → `2026-01-04T14:30:00Z` |
| `fromIso8601(iso)` | ISO 8601 → DD-MM-YYYY |
| `formatDate(date)` | Ensure DD-MM-YYYY format |
| `formatTime(time)` | Ensure HH:MM format |
| `getMinDateForTripCost(isoDate)` | Extract date for cost validation |
| `getCurrentDate()` | Today in DD-MM-YYYY |
| `getCurrentTime()` | Now in HH:MM |

## Date Format Rules (CRITICAL)

| API Context | Send Format | Use Function |
|-------------|-------------|-------------|
| Trip scheduling | ISO 8601 | `FleetDateTime.toIso8601(date, time)` |
| Cost entries | DD-MM-YYYY | Send as-is |
| Document expiry | DD-MM-YYYY | Send as-is |
| UI display | DD-MM-YYYY | Default format |

## Module Path

`ijs-datetime-utils/src/commonMain/kotlin/com/indusjs/datetimeutils/FleetDateTime.kt`

## Depends On: `kotlinx-datetime`
## Depended On By: `ijs-core-lib`, `ijs-datetime-picker`, `ijs-pdf-report`

## Common Mistakes

- ❌ Sending DD-MM-YYYY to trip scheduling API — convert with `toIso8601()`
- ❌ Manual date parsing — use `FleetDateTime` functions
- ❌ Using `java.time` — use `kotlinx-datetime` (multiplatform)

