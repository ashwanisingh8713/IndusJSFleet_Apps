package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import com.indusjs.uicomponents.theme.FleetTokens

/**
 * The single, authoritative implementation of date range selection
 * for the entire application. Must not be reimplemented anywhere else.
 *
 * ### Trigger
 * Only opened by [FleetFilterBar] when a Custom-flagged chip is tapped,
 * or directly by a ViewModel managing dialog visibility.
 *
 * ### UI pattern
 * Material 3 [AlertDialog]. Not a bottom sheet. Not a popover.
 * Consistently a dialog across all targets.
 *
 * ### Validation
 * The Confirm button is disabled until both a start and end date are
 * selected and the start is not after the end.
 *
 * ### State ownership
 * Dialog open/close state lives in the caller. The component receives
 * [onDismiss] and [onApply] callbacks. It does not manage its own visibility.
 *
 * @param isVisible Whether the dialog is shown (caller owns this).
 * @param startDate Current start date in DD-MM-YYYY format.
 * @param endDate Current end date in DD-MM-YYYY format.
 * @param onStartDateChange Callback when start date changes.
 * @param onEndDateChange Callback when end date changes.
 * @param onApply Callback with (startDate, endDate) when confirmed.
 * @param onDismiss Callback when dialog is dismissed.
 */
@Composable
fun FleetDateRangePickerDialog(
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
        if (startDate.length == 10) validateDateRange(startDate) else null
    }
    val endDateError = remember(startDate, endDate) {
        when {
            endDate.length != 10 -> null
            validateDateRange(endDate) != null -> validateDateRange(endDate)
            else -> validateEndAfterStart(startDate, endDate)
        }
    }

    val canApply = startDate.length == 10 && endDate.length == 10 &&
        validateDateRange(startDate) == null && validateEndAfterStart(startDate, endDate) == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Date Range",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
            ) {
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApply(startDate, endDate) },
                enabled = canApply
            ) {
                Text(
                    text = "Apply",
                    fontWeight = FontWeight.SemiBold,
                    color = if (canApply) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private fun validateDateRange(date: String): String? {
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

private fun validateEndAfterStart(startDate: String, endDate: String): String? {
    val baseError = validateDateRange(endDate)
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
