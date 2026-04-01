package com.indusjs.uicomponents.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Represents a single filter option in the chip row.
 *
 * @param T The type of the filter value.
 * @property value The actual filter value used for selection.
 * @property label Display label for the chip.
 * @property count Optional count displayed as a badge (e.g., "Planned (5)").
 * @property icon Optional leading icon content.
 */
data class FilterChipOption<T>(
    val value: T,
    val label: String,
    val count: Int? = null,
    val icon: (@Composable () -> Unit)? = null
)

/**
 * A horizontally scrolling row of Material 3 Filter Chips.
 *
 * Replaces duplicate filter chip implementations across 6+ screens.
 * Supports optional count badges and leading icons per chip.
 *
 * UX Finding #85: "Horizontal scrolling filter chips duplicated across 6+
 * screens with slight variations."
 * UX Finding #42: "Filter chips don't show counts per status."
 *
 * Usage:
 * ```
 * FleetFilterChipRow(
 *     options = listOf(
 *         FilterChipOption("all", "All", count = 25),
 *         FilterChipOption("active", "Active", count = 18),
 *         FilterChipOption("inactive", "Inactive", count = 7)
 *     ),
 *     selectedValue = state.selectedFilter,
 *     onSelected = { viewModel.sendIntent(Intent.FilterBy(it)) }
 * )
 * ```
 *
 * @param T The type of the filter value.
 * @param options List of filter options to display.
 * @param selectedValue The currently selected filter value.
 * @param onSelected Callback when a chip is selected.
 * @param modifier Modifier for the row container.
 * @param showCounts Whether to display count badges. Defaults to true.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FleetFilterChipRow(
    options: List<FilterChipOption<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    showCounts: Boolean = true
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { option ->
            val isSelected = option.value == selectedValue

            FilterChip(
                selected = isSelected,
                onClick = { onSelected(option.value) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = option.label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (showCounts && option.count != null) {
                            Text(
                                text = "(${option.count})",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                },
                leadingIcon = option.icon?.let { iconContent ->
                    { iconContent() }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

/**
 * A convenience overload using simple String labels.
 * Automatically adds an "All" option as the first chip.
 *
 * @param labels Map of filter value to display label (e.g., "active" to "Active").
 * @param selectedValue The currently selected filter value. Null means "All".
 * @param onSelected Callback when a chip is selected. Null means "All" was tapped.
 * @param counts Optional map of filter value to count.
 * @param allLabel Label for the "All" chip.
 * @param totalCount Optional count for the "All" chip.
 * @param modifier Modifier for the row container.
 */
@Composable
fun FleetFilterChipRow(
    labels: Map<String, String>,
    selectedValue: String?,
    onSelected: (String?) -> Unit,
    counts: Map<String, Int> = emptyMap(),
    allLabel: String = "All",
    totalCount: Int? = null,
    modifier: Modifier = Modifier
) {
    val options = buildList {
        add(
            FilterChipOption(
                value = null as String?,
                label = allLabel,
                count = totalCount
            )
        )
        labels.forEach { (value, label) ->
            add(
                FilterChipOption(
                    value = value as String?,
                    label = label,
                    count = counts[value]
                )
            )
        }
    }

    FleetFilterChipRow(
        options = options,
        selectedValue = selectedValue,
        onSelected = onSelected,
        modifier = modifier
    )
}
