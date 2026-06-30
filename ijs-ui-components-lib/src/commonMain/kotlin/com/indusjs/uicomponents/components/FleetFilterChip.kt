package com.indusjs.uicomponents.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.theme.FleetTokens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * The canonical "Calm Fintech" filter chip (direction §4 / step-4 selected-chip token) — one token for
 * every list/filter row, replacing stock Material `FilterChip`.
 *
 * - **Unselected:** transparent fill + 1px `outlineVariant` hairline + `onSurfaceVariant` label.
 * - **Selected:** neutral `secondaryContainer` fill (no border) + `onSecondaryContainer` label — calm,
 *   not the heavy indigo of the old pills.
 * - **Press:** 12% `onSurface` state-layer. Pill shape, constant `Medium` weight.
 *
 * Works for single-choice filters (`role = RadioButton`) and multi-select filters alike — the caller owns
 * `selected`. An optional [count] renders as a muted trailing "(n)"; an optional [leadingIcon] renders at 16dp.
 */
@Composable
fun FleetFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    count: Int? = null,
    leadingIcon: DrawableResource? = null,
) {
    val cs = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shape = RoundedCornerShape(FleetTokens.Radius.Pill)
    val container = if (selected) cs.secondaryContainer else Color.Transparent
    val baseContent = if (selected) cs.onSecondaryContainer else cs.onSurfaceVariant
    val content = if (enabled) baseContent else baseContent.copy(alpha = FleetTokens.StateLayer.DisabledContent)

    Row(
        modifier = modifier
            .heightIn(min = FleetTokens.Height.ButtonSmall)
            .clip(shape)
            .background(container)
            .then(
                if (!selected) Modifier.border(BorderStroke(FleetTokens.Border.Hairline, cs.outlineVariant), shape)
                else Modifier
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            // press state-layer on top of the fill/border
            .background(if (pressed) cs.onSurface.copy(alpha = FleetTokens.StateLayer.Pressed) else Color.Transparent)
            .padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS),
    ) {
        if (leadingIcon != null) {
            Icon(
                painter = painterResource(leadingIcon),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(FleetTokens.IconSize.S),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count != null) {
            Text(
                text = "($count)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = content.copy(alpha = 0.7f),
                maxLines = 1,
            )
        }
    }
}
