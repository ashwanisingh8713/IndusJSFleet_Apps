package com.indusjs.datetimepicker

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Quick date shortcut chips for fast date selection.
 *
 * Provides shortcuts for:
 * - Today (current date)
 * - Tomorrow (+1 day)
 * - Next Week (+7 days)
 * - Next Month (+30 days)
 *
 * Shortcuts are automatically disabled if they fall outside
 * the specified min/max date range.
 */
@Composable
internal fun QuickDateShortcuts(
    onDateSelected: (String) -> Unit,
    minDate: String?,
    maxDate: String?
) {
    val shortcuts = remember {
        listOf(
            QuickDateShortcut("Today", 0),
            QuickDateShortcut("Tomorrow", 1),
            QuickDateShortcut("+7 Days", 7),
            QuickDateShortcut("+30 Days", 30)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick Select",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shortcuts.forEach { shortcut ->
                val calculatedDate = DateTimeUtils.getDateFromToday(shortcut.daysAhead)
                val isEnabled = DateTimeUtils.isDateInRange(calculatedDate, minDate, maxDate)

                ShortcutChip(
                    label = shortcut.label,
                    enabled = isEnabled,
                    onClick = {
                        if (isEnabled) {
                            onDateSelected(calculatedDate)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShortcutChip(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        },
        enabled = enabled,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            labelColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = false,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    )
}

private data class QuickDateShortcut(
    val label: String,
    val daysAhead: Int
)

