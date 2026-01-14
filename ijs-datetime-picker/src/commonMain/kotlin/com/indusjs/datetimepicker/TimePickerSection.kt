package com.indusjs.datetimepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Compact Time picker section with scrollable hour and minute columns.
 * Auto-selects when scrolling stops at center position.
 * Shows current time as default.
 */
@Composable
internal fun TimePickerSection(
    modifier: Modifier = Modifier,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    // Parse time or use current time as default
    val (hour, minute) = if (selectedTime.isNotBlank() && selectedTime != "00:00") {
        try {
            selectedTime.split(":").map { it.toInt() }
        } catch (e: Exception) {
            val now = DateTimeUtils.getCurrentTimeParts()
            listOf(now.first, now.second)
        }
    } else {
        val now = DateTimeUtils.getCurrentTimeParts()
        listOf(now.first, now.second)
    }

    var selectedHour by remember { mutableStateOf(hour) }
    var selectedMinute by remember { mutableStateOf(minute) }

    // Update parent with current time on first load
    LaunchedEffect(Unit) {
        if (selectedTime.isBlank() || selectedTime == "00:00") {
            onTimeSelected(DateTimeUtils.formatTimeParts(selectedHour, selectedMinute))
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title with light background
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = "Select Time",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Wheels Container
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hour Column
            TimeWheelColumn(
                items = (0..23).toList(),
                selectedItem = selectedHour,
                onItemSelected = {
                    selectedHour = it
                    onTimeSelected(DateTimeUtils.formatTimeParts(selectedHour, selectedMinute))
                },
                label = "Hour"
            )

            // Colon Separator
            Text(
                text = ":",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Minute Column
            TimeWheelColumn(
                items = (0..59).toList(),
                selectedItem = selectedMinute,
                onItemSelected = {
                    selectedMinute = it
                    onTimeSelected(DateTimeUtils.formatTimeParts(selectedHour, selectedMinute))
                },
                label = "Minute"
            )
        }
    }
}

@Composable
private fun TimeWheelColumn(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    label: String
) {
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 34

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Label with background - larger and visible
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val listState = rememberLazyListState()

        // Scroll to correct position on first composition
        LaunchedEffect(Unit) {
            listState.scrollToItem(selectedItem)
        }

        // FIXED: Correct center calculation when scrolling stops
        LaunchedEffect(listState.isScrollInProgress) {
            if (!listState.isScrollInProgress) {
                val firstVisible = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset

                // FIXED: centerIndex calculation
                val centerIndex = if (offset > itemHeight / 2) {
                    firstVisible + 1
                } else {
                    firstVisible
                }

                if (centerIndex in items.indices) {
                    if (items[centerIndex] != selectedItem) {
                        onItemSelected(items[centerIndex])
                    }
                    coroutineScope.launch {
                        listState.animateScrollToItem(centerIndex)
                    }
                }
            }
        }

        Box(
            modifier = Modifier.width(60.dp).height(106.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight.dp)
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top spacer - pushes first item down so it can be centered
                item { Spacer(modifier = Modifier.height(itemHeight.dp)) }

                itemsIndexed(items) { index, item ->
                    val isSelected = item == selectedItem
                    val displayText = if (item < 10) "0$item" else "$item"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight.dp)
                            .clickable {
                                onItemSelected(item)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayText,
                            fontSize = if (isSelected) 18.sp else 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                }

                // Bottom spacer
                item { Spacer(modifier = Modifier.height(itemHeight.dp)) }
            }
        }
    }
}
