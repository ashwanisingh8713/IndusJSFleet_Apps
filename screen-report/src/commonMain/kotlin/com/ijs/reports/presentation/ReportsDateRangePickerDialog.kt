package com.ijs.reports.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.indusjs.datetimepicker.DateTimeUtils
import com.indusjs.datetimepicker.FleetDateTimePicker
import com.indusjs.datetimepicker.PickerMode
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

/**
 * Date range dialog for Reports hub (Custom period). Uses [FleetDateTimePicker] for start and end.
 * Applies [onApply] with dates in YYYY-MM-DD for the P&L summary API.
 */
@Composable
internal fun ReportsDateRangePickerDialog(
    isVisible: Boolean,
    startDate: String,
    endDate: String,
    onApply: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var localStartDdMm by remember { mutableStateOf("") }
    var localEndDdMm by remember { mutableStateOf("") }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            localStartDdMm = apiDateToDdMm(startDate).ifBlank { DateTimeUtils.getCurrentDate() }
            localEndDdMm = apiDateToDdMm(endDate).ifBlank { DateTimeUtils.getCurrentDate() }
        }
    }

    val startError = remember(localStartDdMm) { validateDdMm(localStartDdMm) }
    val endError = remember(localStartDdMm, localEndDdMm) {
        validateDdMm(localEndDdMm) ?: validateEndOnOrAfterStart(localStartDdMm, localEndDdMm)
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Select Date Range",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetDateTimePicker(
                        date = localStartDdMm,
                        time = "",
                        onDateTimeChange = { d, _ -> localStartDdMm = d },
                        label = "Start Date",
                        mode = PickerMode.DATE_ONLY,
                        isError = startError != null,
                        errorMessage = startError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetDateTimePicker(
                        date = localEndDdMm,
                        time = "",
                        onDateTimeChange = { d, _ -> localEndDdMm = d },
                        label = "End Date",
                        mode = PickerMode.DATE_ONLY,
                        minDate = localStartDdMm.ifBlank { null },
                        isError = endError != null,
                        errorMessage = endError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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

                    val canApply = startError == null && endError == null &&
                        ddMmToIso(localStartDdMm) != null && ddMmToIso(localEndDdMm) != null

                    Button(
                        onClick = {
                            val s = ddMmToIso(localStartDdMm)
                            val e = ddMmToIso(localEndDdMm)
                            if (s != null && e != null) onApply(s, e)
                        },
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

private fun apiDateToDdMm(api: String): String {
    if (api.isBlank()) return ""
    if (api.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
        val ld = LocalDate.parse(api)
        return DateTimeUtils.formatDateParts(ld.day, ld.month.number, ld.year)
    }
    return api
}

private fun ddMmToIso(ddMm: String): String? {
    val ld = DateTimeUtils.parseDate(ddMm) ?: return null
    return ld.toString()
}

private fun validateDdMm(date: String): String? {
    if (date.isBlank()) return "Date is required"
    if (DateTimeUtils.parseDate(date) == null) return "Invalid date"
    return null
}

private fun validateEndOnOrAfterStart(startDdMm: String, endDdMm: String): String? {
    val s = DateTimeUtils.parseDate(startDdMm) ?: return null
    val e = DateTimeUtils.parseDate(endDdMm) ?: return null
    if (e < s) return "End date must be on or after start date"
    return null
}
