package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint

/**
 * A data-driven option for [FleetDropdown].
 *
 * @param T Identifier type.
 * @property id Unique option identifier returned in selection callback.
 * @property label Display label.
 */
data class DropdownOption<T>(
    val id: T,
    val label: String
)

/**
 * The canonical selection dropdown for the entire application.
 *
 * Built on Material 3 [ExposedDropdownMenuBox]. Not a custom popover.
 *
 * ### Single-line trigger
 * The trigger field is always single-line. Selected option text truncates
 * with ellipsis if it exceeds the field width.
 *
 * ### Options list
 * Passed as a list of [DropdownOption] — fully data-driven.
 *
 * ### Max height
 * The dropdown menu is capped at 240dp. When options exceed this,
 * the menu scrolls internally.
 *
 * ### Adaptive width
 * Full width on Compact, content-driven on Medium / Expanded.
 *
 * ### States
 * Supports default, disabled, and error (with message below) states.
 *
 * @param T Identifier type for options.
 * @param label Field label.
 * @param options List of selectable options.
 * @param selectedOptionId Currently selected option id (null = nothing selected).
 * @param onOptionSelected Callback with the selected option id.
 * @param modifier Modifier for the component.
 * @param placeholder Placeholder when nothing is selected.
 * @param isError Whether the field shows error state.
 * @param errorMessage Error message below the field.
 * @param enabled Whether the dropdown is interactive.
 * @param leadingIcon Optional leading icon slot.
 * @param accessibilityLabel Content description for screen readers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FleetDropdown(
    label: String,
    options: List<DropdownOption<T>>,
    selectedOptionId: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    accessibilityLabel: String? = label
) {
    val resolvedPlaceholder = placeholder.ifBlank { stringResource(Res.string.placeholder_select_option) }
    var isExpanded by remember { mutableStateOf(false) }
    val selectedLabel = options.find { it.id == selectedOptionId }?.label ?: ""

    val semanticsModifier = if (accessibilityLabel != null) {
        Modifier.semantics { contentDescription = accessibilityLabel }
    } else {
        Modifier
    }

    BoxWithConstraints(modifier = modifier) {
        val bp = rememberFleetBreakpoint()
        val widthModifier = when (bp) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier
        }

        ExposedDropdownMenuBox(
            expanded = isExpanded && enabled,
            onExpandedChange = { if (enabled) isExpanded = !isExpanded },
            modifier = Modifier
                .then(widthModifier)
                .then(semanticsModifier)
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                // Keep the floating label to ONE line so a long label in a narrow / 2-column slot
                // can't wrap and double the field height — every dropdown stays the standard ~56dp.
                label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                placeholder = { Text(resolvedPlaceholder, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                modifier = Modifier
                    .then(widthModifier)
                    .menuAnchor(),
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                isError = isError,
                supportingText = if (isError && errorMessage != null) {
                    { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded && enabled)
                },
                shape = RoundedCornerShape(FleetTokens.Radius.L),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = isExpanded && enabled,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier.heightIn(max = FleetTokens.Width.DropdownMaxHeight),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(FleetTokens.Radius.L)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onOptionSelected(option.id)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}
