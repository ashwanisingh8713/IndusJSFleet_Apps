package com.indusjs.uicomponents.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Definition of a single filter in a [FleetFilterBar].
 *
 * @param T Identifier type.
 * @property id Unique identifier returned in the selection callback.
 * @property label Display label for the chip.
 * @property count Optional count badge (e.g. "Active (18)").
 * @property isDateRangeTrigger When `true`, tapping this chip opens
 *   [FleetDateRangePickerDialog] instead of emitting a regular selection.
 *   This is the **only** path to open a date range dialog anywhere in the app.
 */
data class FilterDefinition<T>(
    val id: T,
    val label: String,
    val count: Int? = null,
    val isDateRangeTrigger: Boolean = false
)

/**
 * The canonical filter bar for the entire application.
 *
 * A horizontally scrollable row of chips. Fully data-driven via
 * [FilterDefinition] list.
 *
 * ### "All" chip
 * Always the first chip. When selected, deselects all others.
 * When any other chip is selected, "All" is automatically deselected.
 *
 * ### "Custom" date trigger
 * A [FilterDefinition] with [FilterDefinition.isDateRangeTrigger] = `true`
 * opens [FleetDateRangePickerDialog] when tapped. This is the only way the
 * date range dialog opens anywhere in the application.
 *
 * ### Callbacks
 * - [onFilterSelected] — regular filter selection (returns filter id).
 * - [onDateRangeConfirmed] — date range confirmation (returns start, end
 *   strings in DD-MM-YYYY format).
 *
 * @param T Identifier type for filters.
 * @param filters List of filter definitions.
 * @param selectedFilterId Currently selected filter. `null` means "All".
 * @param onFilterSelected Callback when a regular chip is tapped.
 *   Passes `null` when "All" is tapped.
 * @param onDateRangeConfirmed Callback when date range is confirmed.
 * @param allLabel Label for the "All" chip.
 * @param allCount Optional count for the "All" chip.
 * @param modifier Modifier for the row.
 * @param initialStartDate Pre-populated start date for the date picker.
 * @param initialEndDate Pre-populated end date for the date picker.
 */
@Composable
fun <T> FleetFilterBar(
    filters: List<FilterDefinition<T>>,
    selectedFilterId: T?,
    onFilterSelected: (T?) -> Unit,
    onDateRangeConfirmed: (startDate: String, endDate: String) -> Unit = { _, _ -> },
    allLabel: String = "",
    allCount: Int? = null,
    modifier: Modifier = Modifier,
    initialStartDate: String = "",
    initialEndDate: String = ""
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember(initialStartDate) { mutableStateOf(initialStartDate) }
    var endDate by remember(initialEndDate) { mutableStateOf(initialEndDate) }

    val resolvedAllLabel = allLabel.ifBlank { stringResource(Res.string.filter_all) }
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = FleetTokens.Spacing.L),
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FleetFilterChip(
            selected = selectedFilterId == null,
            label = resolvedAllLabel,
            count = allCount,
            onClick = { onFilterSelected(null) }
        )

        filters.forEach { filter ->
            FleetFilterChip(
                selected = filter.id == selectedFilterId,
                label = filter.label,
                count = filter.count,
                onClick = {
                    if (filter.isDateRangeTrigger) {
                        showDatePicker = true
                    } else {
                        onFilterSelected(filter.id)
                    }
                }
            )
        }
    }

    if (showDatePicker) {
        FleetDateRangePickerDialog(
            isVisible = true,
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { startDate = it },
            onEndDateChange = { endDate = it },
            onApply = { s, e ->
                showDatePicker = false
                onDateRangeConfirmed(s, e)
            },
            onDismiss = { showDatePicker = false }
        )
    }
}
