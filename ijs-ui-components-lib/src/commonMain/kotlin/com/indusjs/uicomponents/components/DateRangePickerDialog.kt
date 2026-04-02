package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode

/**
 * Date Range Picker Dialog for selecting custom date ranges.
 *
 * Start and End open [FleetDateTimePicker] (ijs-datetime-picker). No quick-select presets.
 *
 * @param isVisible Whether the dialog is visible
 * @param startDate Current start date in DD-MM-YYYY format
 * @param endDate Current end date in DD-MM-YYYY format
 * @param onStartDateChange Callback when start date changes
 * @param onEndDateChange Callback when end date changes
 * @param onApply Callback when Apply button is clicked with (startDate, endDate)
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun DateRangePickerDialog(
    isVisible: Boolean,
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onApply: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val startDateError = remember(startDate) {
        if (startDate.length == 10) validateDate(startDate) else null
    }
    val endDateError = remember(startDate, endDate) {
        when {
            endDate.length != 10 -> null
            validateDate(endDate) != null -> validateDate(endDate)
            else -> validateEndDate(startDate, endDate)
        }
    }

    val canApply = startDate.length == 10 && endDate.length == 10 &&
        validateDate(startDate) == null && validateEndDate(startDate, endDate) == null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Select date range",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FleetDateTimePicker(
                    date = startDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onStartDateChange(newDate) },
                    label = "Start Date",
                    mode = PickerMode.DATE_ONLY,
                    isError = startDateError != null,
                    errorMessage = startDateError,
                    modifier = Modifier.fillMaxWidth()
                )

                FleetDateTimePicker(
                    date = endDate,
                    time = "",
                    onDateTimeChange = { newDate, _ -> onEndDateChange(newDate) },
                    label = "End Date",
                    mode = PickerMode.DATE_ONLY,
                    minDate = startDate.ifBlank { null },
                    isError = endDateError != null,
                    errorMessage = endDateError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onApply(startDate, endDate) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canApply
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

/**
 * Validate date format (DD-MM-YYYY).
 */
private fun validateDate(date: String): String? {
    if (date.isBlank()) return "Date is required"
    if (date.length != 10) return null

    val regex = Regex("""\d{2}-\d{2}-\d{4}""")
    if (!regex.matches(date)) return "Invalid format (DD-MM-YYYY)"

    val parts = date.split("-")
    val day = parts[0].toIntOrNull() ?: return "Invalid day"
    val month = parts[1].toIntOrNull() ?: return "Invalid month"
    val year = parts[2].toIntOrNull() ?: return "Invalid year"

    if (month < 1 || month > 12) return "Month must be 1-12"
    if (day < 1 || day > 31) return "Day must be 1-31"
    if (year < 2020 || year > 2100) return "Invalid year"

    return null
}

/**
 * Validate end date is not before start date.
 */
private fun validateEndDate(startDate: String, endDate: String): String? {
    val baseError = validateDate(endDate)
    if (baseError != null) return baseError

    if (startDate.length != 10 || endDate.length != 10) return null

    val startParts = startDate.split("-")
    val endParts = endDate.split("-")

    if (startParts.size != 3 || endParts.size != 3) return null

    val startDay = startParts[0].toIntOrNull() ?: return null
    val startMonth = startParts[1].toIntOrNull() ?: return null
    val startYear = startParts[2].toIntOrNull() ?: return null

    val endDay = endParts[0].toIntOrNull() ?: return null
    val endMonth = endParts[1].toIntOrNull() ?: return null
    val endYear = endParts[2].toIntOrNull() ?: return null

    val startValue = startYear * 10000 + startMonth * 100 + startDay
    val endValue = endYear * 10000 + endMonth * 100 + endDay

    if (endValue < startValue) return "End date must be after start date"

    return null
}
