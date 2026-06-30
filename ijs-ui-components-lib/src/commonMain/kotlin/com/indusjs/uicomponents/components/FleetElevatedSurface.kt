package com.indusjs.uicomponents.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme

/**
 * Calm Fintech depth — the dark-mode "crushed black" fallback (direction §4 / DDD C2).
 *
 * On near-black dark surfaces a drop shadow is invisible, so elevation is conveyed by a **1px top-edge
 * highlight** (light catching the top of a raised surface) instead. This modifier draws that highlight as a
 * hairline border whose colour fades from `onSurface @ alpha` at the top to transparent by mid-height —
 * reading as a lit top edge. In **light mode it is a no-op** (the surface's own soft shadow conveys depth),
 * so callers keep their existing light appearance unchanged.
 *
 * Apply on a clipped, shaped surface (after `.clip(shape)`), passing the same [shape]. Pick [highlightAlpha]
 * by elevation tier via [FleetElevation] (C2 per-step %).
 */
@Composable
fun Modifier.fleetElevatedSurface(
    shape: Shape,
    highlightAlpha: Float = FleetElevation.Card,
): Modifier {
    if (!isAppInDarkTheme()) return this
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = highlightAlpha)
    val brush = Brush.verticalGradient(
        0f to highlight,
        0.5f to Color.Transparent,
    )
    return this.border(BorderStroke(FleetTokens.Border.Hairline, brush), shape)
}

/** Dark top-highlight alphas per elevation tier (DDD C2: 10/10/7/6/5/4%). */
object FleetElevation {
    const val Card: Float = 0.10f
    const val Raised: Float = 0.10f
    const val Dropdown: Float = 0.07f
    const val Dialog: Float = 0.06f
    const val Modal: Float = 0.05f
    const val Pill: Float = 0.04f
}
