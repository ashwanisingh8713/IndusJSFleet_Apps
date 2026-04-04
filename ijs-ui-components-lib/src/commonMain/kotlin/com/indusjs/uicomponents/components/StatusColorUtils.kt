package com.indusjs.uicomponents.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.indusjs.fleet.core.constants.StatusConstants.StateColorScheme

/**
 * Centralized mapping from [StateColorScheme] to Material 3 theme colors.
 *
 * Use this instead of duplicating the when-expression in every screen.
 * Keeps status colors consistent across vehicles, drivers, trips, and payments.
 *
 * Usage:
 * ```
 * val color = stateColorSchemeToColor(VehicleStatus.getColorScheme(vehicle.status))
 * ```
 */
@Composable
fun stateColorSchemeToColor(scheme: StateColorScheme): Color = when (scheme) {
    StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
    StateColorScheme.WARNING -> MaterialTheme.colorScheme.secondary
    StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
    StateColorScheme.INFO -> MaterialTheme.colorScheme.tertiary
    StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.outline
}

/**
 * Color mapping for **clickable** status chips and badges.
 *
 * Unlike [stateColorSchemeToColor], NEUTRAL returns [MaterialTheme.colorScheme.onSurfaceVariant]
 * — a darker, clearly visible shade — so that chips with inactive/neutral status
 * never look disabled or un-tappable.
 *
 * Usage:
 * ```
 * val chipColor = stateColorSchemeToChipColor(VehicleStatus.getColorScheme(vehicle.status))
 * ```
 */
@Composable
fun stateColorSchemeToChipColor(scheme: StateColorScheme): Color = when (scheme) {
    StateColorScheme.SUCCESS -> MaterialTheme.colorScheme.primary
    StateColorScheme.WARNING -> MaterialTheme.colorScheme.secondary
    StateColorScheme.ERROR -> MaterialTheme.colorScheme.error
    StateColorScheme.INFO -> MaterialTheme.colorScheme.tertiary
    StateColorScheme.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
}

