# ijs-datetime-utils — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.datetimeutils`

---

## 1. Purpose

Cross-platform date/time utility library. Single `FleetDateTime` object providing all date parsing, formatting, ISO 8601 conversion, and validation for the fleet app.

## 2. Key Formats

| Format | Pattern | Example |
|--------|---------|---------|
| UI Date | `DD-MM-YYYY` | `15-03-2026` |
| UI Time | `HH:mm` (24hr) | `15:30` |
| Display Date | `DD-MMM-YYYY` | `15-Mar-2026` |
| Display Time | `hh:mm AM/PM` | `03:30 PM` |
| ISO 8601 | `YYYY-MM-DDTHH:mm:ssZ` | `2026-03-15T15:30:00Z` |

## 3. Dependencies

- `kotlinx-datetime` only

## 4. Key Functions

```kotlin
FleetDateTime.today()                              // "16-03-2026"
FleetDateTime.toIso8601("16-03-2026", "15:30")     // "2026-03-16T15:30:00Z"
FleetDateTime.fromIso8601("2026-03-16T15:30:00Z")  // FleetDateTimeValue
FleetDateTime.formatAnyToDisplayDate("2026-03-16")  // "16-Mar-2026"
```

