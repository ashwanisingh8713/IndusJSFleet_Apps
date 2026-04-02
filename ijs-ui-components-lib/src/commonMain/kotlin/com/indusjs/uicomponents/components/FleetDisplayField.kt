package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.indusjs.uicomponents.theme.FleetTokens

/**
 * A read-only display component for detail / profile / summary screens.
 *
 * This is **not** an input. It cannot receive focus, cannot be typed into,
 * and has no keyboard interaction. It renders a label and a value as styled
 * text. It is **not** built on `OutlinedTextField`.
 *
 * ### Distinction from [FleetInputField]
 * [FleetInputField] is for editable text entry. [FleetDisplayField] is for
 * presenting labelled values that the user cannot change on this screen
 * (e.g. "Plate Number: KA-01-AB-1234", "Licence Type: HMV").
 *
 * @param label The field label (rendered above the value in secondary style).
 * @param value The display value (rendered in primary body style).
 * @param modifier Modifier for the container.
 * @param valueColor Override colour for the value text. Defaults to onSurface.
 */
@Composable
fun FleetDisplayField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = FleetTokens.Spacing.S)
            .semantics { contentDescription = "$label: $value" }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}
