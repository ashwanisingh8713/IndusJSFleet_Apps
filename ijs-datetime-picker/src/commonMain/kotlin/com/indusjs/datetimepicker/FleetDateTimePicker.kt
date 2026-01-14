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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A cross-platform DateTime picker component that displays date and time selection
 * in a single unified popup.
 */
@Composable
fun FleetDateTimePicker(
    date: String,
    time: String,
    onDateTimeChange: (date: String, time: String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date & Time",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    minDate: String? = null,
    maxDate: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    // Build display value - no icons, just text with | separator
    val displayValue = when {
        date.isNotBlank() && time.isNotBlank() -> "$date  |  $time"
        date.isNotBlank() -> date
        time.isNotBlank() -> time
        else -> ""
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Use OutlinedTextField style - looks like standard input
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
            placeholder = { Text("Select date & time") },
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDialog) {
        DateTimePickerDialog(
            initialDate = date,
            initialTime = time,
            onDateTimeSelected = { newDate, newTime ->
                onDateTimeChange(newDate, newTime)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            minDate = minDate,
            maxDate = maxDate
        )
    }
}

@Composable
private fun DateTimePickerDialog(
    initialDate: String,
    initialTime: String,
    onDateTimeSelected: (date: String, time: String) -> Unit,
    onDismiss: () -> Unit,
    minDate: String?,
    maxDate: String?
) {
    var selectedDate by remember { mutableStateOf(initialDate.ifBlank { DateTimeUtils.getCurrentDate() }) }
    var selectedTime by remember { mutableStateOf(initialTime.ifBlank { DateTimeUtils.getCurrentTime() }) }

    var calendarMonth by remember {
        val parts = try { selectedDate.split("-").map { it.toInt() } } catch (e: Exception) {
            val now = DateTimeUtils.getCurrentDateParts(); listOf(now.first, now.second, now.third)
        }
        mutableStateOf(parts[1])
    }
    var calendarYear by remember {
        val parts = try { selectedDate.split("-").map { it.toInt() } } catch (e: Exception) {
            val now = DateTimeUtils.getCurrentDateParts(); listOf(now.first, now.second, now.third)
        }
        mutableStateOf(parts[2])
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
                    text = "Select Date & Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Single line Date & Time Preview - no icons
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
                            text = selectedDate,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "  |  ",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                        Text(
                            text = selectedTime,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker Section
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

                // Time Picker Section
                TimePickerSection(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTime = selectedTime,
                    onTimeSelected = { selectedTime = it }
                )

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Shortcuts
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
                        onClick = { onDateTimeSelected(selectedDate, selectedTime) }
                    ) {
                        Text("Confirm")
                    }
                }
            }
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
        // Single line label with description
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
