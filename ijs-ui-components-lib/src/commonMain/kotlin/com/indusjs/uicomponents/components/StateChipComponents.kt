package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

// ==================== Compact State Chip with Action ====================

/**
 * Compact state chip with change button.
 *
 * Uses [getChipColorForScheme] so that NEUTRAL states (e.g. inactive)
 * render with a visible, non-faded color — the chip always looks tappable.
 */
@Composable
fun StateChipWithAction(
    stateLabel: String,
    stateIcon: String,
    colorScheme: StateColorScheme,
    onChangeClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val stateColor = getChipColorForScheme(colorScheme)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = stateColor.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stateIcon, style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = stateColor
                )
            }
        }

        if (enabled) {
            TextButton(
                onClick = onChangeClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.change),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * State action button for quick state changes.
 */
@Composable
fun StateActionButton(
    label: String,
    icon: String,
    colorScheme: StateColorScheme,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = getColorForScheme(colorScheme)

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color,
            disabledContentColor = color.copy(alpha = 0.5f)
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (enabled) color else color.copy(alpha = 0.5f)
            )
        )
    ) {
        Text(icon, style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

// ==================== Helper Functions ====================

/**
 * Standard state-to-color mapping used inside the dialog and badges.
 */
@Composable
fun getColorForScheme(colorScheme: StateColorScheme): Color {
    return when (colorScheme) {
        StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        StateColorScheme.WARNING -> com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber
        StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        StateColorScheme.INFO -> com.indusjs.uicomponents.theme.FleetStatusColors.InfoBlue
        StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
    }
}

/**
 * Color mapping for **clickable** chips and buttons.
 *
 * Unlike [getColorForScheme], NEUTRAL returns a darker, clearly visible shade
 * (`onSurfaceVariant`) so inactive-state chips never look disabled or un-tappable.
 */
@Composable
fun getChipColorForScheme(colorScheme: StateColorScheme): Color {
    return when (colorScheme) {
        StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
        StateColorScheme.WARNING -> com.indusjs.uicomponents.theme.FleetStatusColors.ExpenseAmber
        StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
        StateColorScheme.INFO -> com.indusjs.uicomponents.theme.FleetStatusColors.InfoBlue
        StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

