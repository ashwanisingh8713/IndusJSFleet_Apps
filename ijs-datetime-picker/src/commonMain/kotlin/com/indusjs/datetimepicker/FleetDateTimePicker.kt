package com.indusjs.datetimepicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Picker mode enum for different selection types.
 */
enum class PickerMode {
    DATE_TIME,  // Both date and time
    DATE_ONLY,  // Only date
    TIME_ONLY   // Only time
}

/**
 * A cross-platform DateTime picker component that supports:
 * - DATE_TIME: Both date and time selection
 * - DATE_ONLY: Only date selection
 * - TIME_ONLY: Only time selection (Quick Select hidden)
 */
@Composable
fun FleetDateTimePicker(
    date: String,
    time: String,
    onDateTimeChange: (date: String, time: String) -> Unit,
    modifier: Modifier = Modifier,
    mode: PickerMode = PickerMode.DATE_TIME,
    label: String = when (mode) {
        PickerMode.DATE_TIME -> "Date & Time"
        PickerMode.DATE_ONLY -> "Date"
        PickerMode.TIME_ONLY -> "Time"
    },
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    minDate: String? = null,
    maxDate: String? = null,
    initialDisplayDate: String? = null,  // Date to show in calendar when picker opens with empty date
    /** When false, hides the "Quick Select" shortcuts row inside the picker dialog (e.g. date range dialog). */
    showQuickDateShortcuts: Boolean = true
) {
    var showDialog by remember { mutableStateOf(false) }

    // Build display value based on mode - use formatted display (e.g., "18 April 2024, 10:30 AM")
    val displayValue = when (mode) {
        PickerMode.DATE_TIME -> DateTimeUtils.formatDateTimeForDisplay(date, time)
        PickerMode.DATE_ONLY -> DateTimeUtils.formatDateForDisplay(date)
        PickerMode.TIME_ONLY -> DateTimeUtils.formatTimeForDisplay(time)
    }

    // Placeholder based on mode
    val placeholder = when (mode) {
        PickerMode.DATE_TIME -> "Select date & time"
        PickerMode.DATE_ONLY -> "Select date"
        PickerMode.TIME_ONLY -> "Select time"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = enabled,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showDialog = true },
            enabled = false,
            label = if (label.isNotEmpty()) { { Text(label) } } else null,
            placeholder = { Text(placeholder) },
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            singleLine = true,
            // Match the app's standard field rounding (FleetTokens.Radius.L = 12dp) so date/time
            // fields aren't visually flatter than the FleetInputField/FleetDropdown fields.
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDialog) {
        PickerDialog(
            mode = mode,
            initialDate = date,
            initialTime = time,
            initialDisplayDate = initialDisplayDate,
            onConfirm = { newDate, newTime ->
                onDateTimeChange(newDate, newTime)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            minDate = minDate,
            maxDate = maxDate,
            showQuickDateShortcuts = showQuickDateShortcuts
        )
    }
}

/**
 * Convenience composable for Date only picker.
 *
 * @param initialDisplayDate The date to show in calendar when picker opens with empty date.
 *                           Useful for DOB fields to show 18 years ago, etc.
 */
@Composable
fun FleetDatePicker(
    date: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    minDate: String? = null,
    maxDate: String? = null,
    initialDisplayDate: String? = null,
    showQuickDateShortcuts: Boolean = true
) {
    FleetDateTimePicker(
        date = date,
        time = "",
        onDateTimeChange = { newDate, _ -> onDateChange(newDate) },
        modifier = modifier,
        mode = PickerMode.DATE_ONLY,
        label = label,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage,
        minDate = minDate,
        maxDate = maxDate,
        initialDisplayDate = initialDisplayDate,
        showQuickDateShortcuts = showQuickDateShortcuts
    )
}

/**
 * Convenience composable for Time only picker.
 */
@Composable
fun FleetTimePicker(
    time: String,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Time",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    FleetDateTimePicker(
        date = "",
        time = time,
        onDateTimeChange = { _, newTime -> onTimeChange(newTime) },
        modifier = modifier,
        mode = PickerMode.TIME_ONLY,
        label = label,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage
    )
}

@Composable
private fun PickerDialog(
    mode: PickerMode,
    initialDate: String,
    initialTime: String,
    initialDisplayDate: String? = null,
    onConfirm: (date: String, time: String) -> Unit,
    onDismiss: () -> Unit,
    minDate: String?,
    maxDate: String?,
    showQuickDateShortcuts: Boolean = true
) {
    // Use initialDisplayDate when date is blank (e.g., for DOB show 18 years ago)
    val effectiveDisplayDate = initialDate.ifBlank {
        initialDisplayDate ?: DateTimeUtils.getCurrentDate()
    }
    var selectedDate by remember { mutableStateOf(effectiveDisplayDate) }
    var selectedTime by remember { mutableStateOf(initialTime.ifBlank { DateTimeUtils.getCurrentTime() }) }

    var calendarMonth by remember {
        val parts = try { effectiveDisplayDate.split("-").map { it.toInt() } } catch (e: Exception) {
            val now = DateTimeUtils.getCurrentDateParts(); listOf(now.first, now.second, now.third)
        }
        mutableStateOf(parts[1])
    }
    var calendarYear by remember {
        val parts = try { effectiveDisplayDate.split("-").map { it.toInt() } } catch (e: Exception) {
            val now = DateTimeUtils.getCurrentDateParts(); listOf(now.first, now.second, now.third)
        }
        mutableStateOf(parts[2])
    }

    val dialogTitle = when (mode) {
        PickerMode.DATE_TIME -> "Select Date & Time"
        PickerMode.DATE_ONLY -> "Select Date"
        PickerMode.TIME_ONLY -> "Select Time"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Title
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Preview based on mode
                PreviewSection(mode = mode, selectedDate = selectedDate, selectedTime = selectedTime)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Section - only for DATE_TIME and DATE_ONLY
                if (mode == PickerMode.DATE_TIME || mode == PickerMode.DATE_ONLY) {
                    DatePickerSection(
                        modifier = Modifier.fillMaxWidth(),
                        selectedDate = selectedDate,
                        calendarMonth = calendarMonth,
                        calendarYear = calendarYear,
                        onCalendarMonthYearChange = { month, year ->
                            calendarMonth = month
                            calendarYear = year
                        },
                        onDateSelected = { newDate ->
                            selectedDate = newDate
                            try {
                                val parts = newDate.split("-").map { it.toInt() }
                                calendarMonth = parts[1]
                                calendarYear = parts[2]
                            } catch (e: Exception) { }
                        },
                        minDate = minDate,
                        maxDate = maxDate
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Time Picker Section - only for DATE_TIME and TIME_ONLY
                if (mode == PickerMode.DATE_TIME || mode == PickerMode.TIME_ONLY) {
                    TimePickerSection(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTime = selectedTime,
                        onTimeSelected = { selectedTime = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Quick Shortcuts - only for DATE_TIME and DATE_ONLY (NOT for TIME_ONLY)
                if (showQuickDateShortcuts && (mode == PickerMode.DATE_TIME || mode == PickerMode.DATE_ONLY)) {
                    QuickDateShortcutsRow(
                        onDateSelected = { newDate ->
                            selectedDate = newDate
                            try {
                                val parts = newDate.split("-").map { it.toInt() }
                                calendarMonth = parts[1]
                                calendarYear = parts[2]
                            } catch (e: Exception) { }
                        },
                        minDate = minDate,
                        maxDate = maxDate
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            when (mode) {
                                PickerMode.DATE_TIME -> onConfirm(selectedDate, selectedTime)
                                PickerMode.DATE_ONLY -> onConfirm(selectedDate, "")
                                PickerMode.TIME_ONLY -> onConfirm("", selectedTime)
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewSection(
    mode: PickerMode,
    selectedDate: String,
    selectedTime: String
) {
    // Format for display: "18 April 2024, 10:30 AM"
    val displayText = when (mode) {
        PickerMode.DATE_TIME -> DateTimeUtils.formatDateTimeForDisplay(selectedDate, selectedTime)
        PickerMode.DATE_ONLY -> DateTimeUtils.formatDateForDisplay(selectedDate)
        PickerMode.TIME_ONLY -> DateTimeUtils.formatTimeForDisplay(selectedTime)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText.ifBlank { "Select date & time" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun QuickDateShortcutsRow(
    onDateSelected: (String) -> Unit,
    minDate: String?,
    maxDate: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Quick Select — Jump to date from today",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val shortcuts = listOf(
                "Today" to 0,
                "Tomorrow" to 1,
                "+7 Days" to 7,
                "+30 Days" to 30
            )

            shortcuts.forEach { (label, daysAhead) ->
                val targetDate = DateTimeUtils.getDateFromToday(daysAhead)
                val isEnabled = DateTimeUtils.isDateInRange(targetDate, minDate, maxDate)

                OutlinedButton(
                    onClick = { if (isEnabled) onDateSelected(targetDate) },
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

