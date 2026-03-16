# ijs-datetime-picker — Module Documentation

> **Version:** 1.0  
> **Last Updated:** 16-Mar-2026  
> **Namespace:** `com.indusjs.datetimepicker`

---

## 1. Purpose

Cross-platform Compose Multiplatform date/time picker dialog. Provides Material 3 calendar grid, time wheel selectors, and quick-select shortcuts ("Today", "Tomorrow", "Next Week").

## 2. Dependencies

- `ijs-datetime-utils` (api)
- Compose Multiplatform
- `kotlinx-datetime`

## 3. Key Composable

```kotlin
@Composable
fun FleetDateTimePicker(
    date: String,            // DD-MM-YYYY
    time: String,            // HH:MM (24hr)
    onDateTimeChange: (date: String, time: String) -> Unit,
    mode: PickerMode = PickerMode.DATE_TIME,
    label: String = "Date & Time",
    minDate: String? = null,
    maxDate: String? = null
)
```

### PickerMode
- `DATE_TIME` — Both date and time
- `DATE_ONLY` — Only date selection
- `TIME_ONLY` — Only time selection

