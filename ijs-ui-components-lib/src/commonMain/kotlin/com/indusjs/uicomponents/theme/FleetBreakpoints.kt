package com.indusjs.uicomponents.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size breakpoints matching Material 3 window size classes.
 *
 * Every component that needs adaptive behaviour reads the active breakpoint
 * from [rememberFleetBreakpoint] rather than querying screen dimensions.
 *
 * | Class     | Width        | Examples                              |
 * |-----------|-------------|---------------------------------------|
 * | Compact   | < 600dp     | Phone portrait                        |
 * | Medium    | 600–840dp   | Phone landscape, small tablet          |
 * | Expanded  | > 840dp     | Tablet landscape, desktop, JS browser |
 */
sealed class FleetBreakpoint {
    data object Compact : FleetBreakpoint()
    data object Medium : FleetBreakpoint()
    data object Expanded : FleetBreakpoint()
}

private val COMPACT_MAX: Dp = 600.dp
private val MEDIUM_MAX: Dp = 840.dp

/**
 * Derives the active [FleetBreakpoint] from the container's [maxWidth].
 *
 * Call inside a [BoxWithConstraints] scope. This reads the **container**
 * width, not the global window width, so components placed in multi-column
 * layouts on larger screens behave correctly.
 *
 * ```kotlin
 * BoxWithConstraints {
 *     val bp = rememberFleetBreakpoint()
 *     when (bp) {
 *         FleetBreakpoint.Compact  -> CompactLayout()
 *         FleetBreakpoint.Medium   -> MediumLayout()
 *         FleetBreakpoint.Expanded -> ExpandedLayout()
 *     }
 * }
 * ```
 */
@Composable
fun BoxWithConstraintsScope.rememberFleetBreakpoint(): FleetBreakpoint {
    val width = maxWidth
    return remember(width) {
        when {
            width < COMPACT_MAX -> FleetBreakpoint.Compact
            width < MEDIUM_MAX -> FleetBreakpoint.Medium
            else -> FleetBreakpoint.Expanded
        }
    }
}

/**
 * Utility to compute a breakpoint from an arbitrary [Dp] width.
 * Useful outside of [BoxWithConstraints] when the width is already known.
 */
fun fleetBreakpointFor(width: Dp): FleetBreakpoint = when {
    width < COMPACT_MAX -> FleetBreakpoint.Compact
    width < MEDIUM_MAX -> FleetBreakpoint.Medium
    else -> FleetBreakpoint.Expanded
}

/**
 * Returns `true` when the breakpoint is [FleetBreakpoint.Compact].
 * Convenience for the most common adaptive check.
 */
val FleetBreakpoint.isCompact: Boolean
    get() = this is FleetBreakpoint.Compact

/**
 * Returns `true` when the breakpoint is at least [FleetBreakpoint.Medium].
 */
val FleetBreakpoint.isAtLeastMedium: Boolean
    get() = this is FleetBreakpoint.Medium || this is FleetBreakpoint.Expanded

/**
 * Returns `true` when the breakpoint is [FleetBreakpoint.Expanded].
 */
val FleetBreakpoint.isExpanded: Boolean
    get() = this is FleetBreakpoint.Expanded
