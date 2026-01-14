package com.indusjs.datetimepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact Date picker section with calendar grid.
 */
@Composable
internal fun DatePickerSection(
    modifier: Modifier = Modifier,
    selectedDate: String,
    calendarMonth: Int,
    calendarYear: Int,
    onCalendarMonthYearChange: (month: Int, year: Int) -> Unit,
    onDateSelected: (String) -> Unit,
    minDate: String?,
    maxDate: String?
) {
    val selectedDay = if (selectedDate.isNotBlank()) {
        try {
            val parts = selectedDate.split("-").map { it.toInt() }
            val dateMonth = parts[1]
            val dateYear = parts[2]
            if (calendarMonth == dateMonth && calendarYear == dateYear) parts[0] else null
        } catch (e: Exception) { null }
    } else { null }

    Column(modifier = modifier) {
        // Month/Year Navigation - 32dp buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = {
                    if (calendarMonth == 1) {
                        onCalendarMonthYearChange(12, calendarYear - 1)
                    } else {
                        onCalendarMonthYearChange(calendarMonth - 1, calendarYear)
                    }
                },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(text = "◀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "${DateTimeUtils.getMonthName(calendarMonth)} $calendarYear",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            FilledTonalIconButton(
                onClick = {
                    if (calendarMonth == 12) {
                        onCalendarMonthYearChange(1, calendarYear + 1)
                    } else {
                        onCalendarMonthYearChange(calendarMonth + 1, calendarYear)
                    }
                },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(text = "▶", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Compact Calendar Grid
        CompactCalendarGrid(
            year = calendarYear,
            month = calendarMonth,
            selectedDay = selectedDay,
            onDaySelected = { day ->
                val newDate = DateTimeUtils.formatDateParts(day, calendarMonth, calendarYear)
                onDateSelected(newDate)
            },
            minDate = minDate,
            maxDate = maxDate
        )
    }
}

@Composable
private fun CompactCalendarGrid(
    year: Int,
    month: Int,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    minDate: String?,
    maxDate: String?
) {
    val daysInMonth = DateTimeUtils.getDaysInMonth(year, month)
    val firstDayOfWeek = DateTimeUtils.getFirstDayOfWeek(year, month)
    val todayParts = DateTimeUtils.getCurrentDateParts()
    val isCurrentMonth = month == todayParts.second && year == todayParts.third

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        // Weekday Headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days Grid - Compact with smaller cells
        var dayCounter = 1
        repeat(6) { weekIndex ->
            if (dayCounter > daysInMonth) return@repeat

            Row(
                modifier = Modifier.fillMaxWidth().height(28.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { dayOfWeek ->
                    val shouldShowDay = if (weekIndex == 0) {
                        dayOfWeek >= firstDayOfWeek
                    } else {
                        dayCounter <= daysInMonth
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (shouldShowDay && dayCounter <= daysInMonth) {
                            val currentDay = dayCounter
                            val isSelected = currentDay == selectedDay
                            val isToday = isCurrentMonth && currentDay == todayParts.first
                            val isEnabled = DateTimeUtils.isDayEnabled(currentDay, month, year, minDate, maxDate)

                            CompactDayCell(
                                day = currentDay,
                                isSelected = isSelected,
                                isToday = isToday,
                                isEnabled = isEnabled,
                                onClick = { onDaySelected(currentDay) }
                            )
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Selected date is slightly bigger (26dp) than today/normal (24dp)
    val cellSize = if (isSelected) 26.dp else 24.dp
    val fontSize = if (isSelected) 12.sp else 11.sp

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = fontSize,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}
