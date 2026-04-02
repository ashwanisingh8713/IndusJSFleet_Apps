package com.ijs.reports.presentation

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
 * Date Range Picker Dialog for Reports that uses FleetDateTimePicker (calendar-based)
 * instead of raw text input fields. No Quick Select section.
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
fun ReportDateRangeDialog(
    isVisible: Boolean,
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onApply: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var localStart by remember(startDate) { mutableStateOf(startDate) }
    var localEnd by remember(endDate) { mutableStateOf(endDate) }

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
                    text = "Select Date Range",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FleetDateTimePicker(
                    date = localStart,
                    time = "",
                    onDateTimeChange = { newDate, _ ->
                        localStart = newDate
                        onStartDateChange(newDate)
                    },
                    label = "Start Date",
                    mode = PickerMode.DATE_ONLY,
                    modifier = Modifier.fillMaxWidth()
                )

                FleetDateTimePicker(
                    date = localEnd,
                    time = "",
                    onDateTimeChange = { newDate, _ ->
                        localEnd = newDate
                        onEndDateChange(newDate)
                    },
                    label = "End Date",
                    mode = PickerMode.DATE_ONLY,
                    minDate = localStart.ifBlank { null },
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
                        onClick = {
                            if (localStart.isNotBlank() && localEnd.isNotBlank()) {
                                onApply(localStart, localEnd)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = localStart.isNotBlank() && localEnd.isNotBlank()
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
