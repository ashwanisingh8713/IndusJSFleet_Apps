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
import com.indusjs.fleet.core.util.currentTimeMillis
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Instant

/**
 * Date Range Picker Dialog for selecting custom date ranges.
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

    // Store raw digit values (without delimiters)
    var localStartRaw by remember(startDate) { mutableStateOf(startDate.replace("-", "")) }
    var localEndRaw by remember(endDate) { mutableStateOf(endDate.replace("-", "")) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }

    // Convert raw to formatted for validation
    fun formatDate(raw: String): String {
        if (raw.length != 8) return raw
        return "${raw.substring(0, 2)}-${raw.substring(2, 4)}-${raw.substring(4, 8)}"
    }

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
                // Header
                Text(
                    text = "Select Date Range",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Start Date
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetDateField(
                        rawValue = localStartRaw,
                        onRawValueChange = { raw ->
                            localStartRaw = raw
                            val formatted = formatDate(raw)
                            onStartDateChange(formatted)
                            startDateError = validateDate(formatted)
                        },
                        label = "Start Date",
                        placeholder = "DD-MM-YYYY",
                        isError = startDateError != null,
                        errorMessage = startDateError
                    )
                }

                // End Date
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetDateField(
                        rawValue = localEndRaw,
                        onRawValueChange = { raw ->
                            localEndRaw = raw
                            val formatted = formatDate(raw)
                            onEndDateChange(formatted)
                            val startFormatted = formatDate(localStartRaw)
                            endDateError = validateEndDate(startFormatted, formatted)
                        },
                        label = "End Date",
                        placeholder = "DD-MM-YYYY",
                        isError = endDateError != null,
                        errorMessage = endDateError
                    )
                }

                // Quick Selections
                Text(
                    text = "Quick Select",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSelectChip(
                        label = "Last 7 days",
                        onClick = {
                            val (start, end) = getLastNDays(7)
                            localStartRaw = start.replace("-", "")
                            localEndRaw = end.replace("-", "")
                            onStartDateChange(start)
                            onEndDateChange(end)
                            startDateError = null
                            endDateError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSelectChip(
                        label = "Last 30 days",
                        onClick = {
                            val (start, end) = getLastNDays(30)
                            localStartRaw = start.replace("-", "")
                            localEndRaw = end.replace("-", "")
                            onStartDateChange(start)
                            onEndDateChange(end)
                            startDateError = null
                            endDateError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSelectChip(
                        label = "This Month",
                        onClick = {
                            val (start, end) = getThisMonth()
                            localStartRaw = start.replace("-", "")
                            localEndRaw = end.replace("-", "")
                            onStartDateChange(start)
                            onEndDateChange(end)
                            startDateError = null
                            endDateError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSelectChip(
                        label = "Last Month",
                        onClick = {
                            val (start, end) = getLastMonth()
                            localStartRaw = start.replace("-", "")
                            localEndRaw = end.replace("-", "")
                            onStartDateChange(start)
                            onEndDateChange(end)
                            startDateError = null
                            endDateError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
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

                    val localStartFormatted = formatDate(localStartRaw)
                    val localEndFormatted = formatDate(localEndRaw)
                    Button(
                        onClick = {
                            if (startDateError == null && endDateError == null &&
                                localStartRaw.length == 8 && localEndRaw.length == 8) {
                                onApply(localStartFormatted, localEndFormatted)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = startDateError == null && endDateError == null &&
                                  localStartRaw.length == 8 && localEndRaw.length == 8
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSelectChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Validate date format (DD-MM-YYYY).
 */
private fun validateDate(date: String): String? {
    if (date.isBlank()) return "Date is required"
    if (date.length != 10) return null // Still typing

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

    // Parse dates
    val startParts = startDate.split("-")
    val endParts = endDate.split("-")

    if (startParts.size != 3 || endParts.size != 3) return null

    val startDay = startParts[0].toIntOrNull() ?: return null
    val startMonth = startParts[1].toIntOrNull() ?: return null
    val startYear = startParts[2].toIntOrNull() ?: return null

    val endDay = endParts[0].toIntOrNull() ?: return null
    val endMonth = endParts[1].toIntOrNull() ?: return null
    val endYear = endParts[2].toIntOrNull() ?: return null

    // Compare dates
    val startValue = startYear * 10000 + startMonth * 100 + startDay
    val endValue = endYear * 10000 + endMonth * 100 + endDay

    if (endValue < startValue) return "End date must be after start date"

    return null
}

/**
 * Get last N days date range in DD-MM-YYYY format.
 */
private fun getLastNDays(days: Int): Pair<String, String> {
    val nowMs = currentTimeMillis()
    val today = Instant.fromEpochMilliseconds(nowMs)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    val start = today.minus(DatePeriod(days = days))

    val endDate = formatLocalDate(today)
    val startDate = formatLocalDate(start)

    return startDate to endDate
}

/**
 * Get this month's date range.
 */
private fun getThisMonth(): Pair<String, String> {
    val nowMs = currentTimeMillis()
    val today = Instant.fromEpochMilliseconds(nowMs)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    val startDate = padTwo(1) + "-" + padTwo(today.monthNumber) + "-" + today.year.toString()
    val endDate = formatLocalDate(today)

    return startDate to endDate
}

/**
 * Get last month's date range.
 */
private fun getLastMonth(): Pair<String, String> {
    val nowMs = currentTimeMillis()
    val today = Instant.fromEpochMilliseconds(nowMs)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date

    val lastMonth = today.minus(DatePeriod(months = 1))
    val firstDayOfLastMonth = LocalDate(lastMonth.year, lastMonth.monthNumber, 1)
    val lastDayOfLastMonth = LocalDate(lastMonth.year, lastMonth.monthNumber, 1)
        .plus(DatePeriod(months = 1))
        .minus(DatePeriod(days = 1))

    val startDate = formatLocalDate(firstDayOfLastMonth)
    val endDate = formatLocalDate(lastDayOfLastMonth)

    return startDate to endDate
}

/**
 * Format LocalDate to DD-MM-YYYY string.
 */
private fun formatLocalDate(date: LocalDate): String {
    return padTwo(date.dayOfMonth) + "-" + padTwo(date.monthNumber) + "-" + date.year.toString()
}

/**
 * Pad a number to two digits.
 */
private fun padTwo(value: Int): String {
    return if (value < 10) "0$value" else value.toString()
}
