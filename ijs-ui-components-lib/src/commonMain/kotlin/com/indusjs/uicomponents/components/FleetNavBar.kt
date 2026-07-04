package com.indusjs.uicomponents.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.theme.FleetTokens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * One destination in [FleetBottomNavBar] / [FleetNavRail]. Route-agnostic (the app layer maps its
 * `FleetRoute`s to these), permission-gated by the caller (only pass visible items).
 *
 * @property key Stable identifier (used for selection + the click callback).
 * @property label Display label (always shown; EN or HI).
 * @property iconRes Destination icon.
 * @property badgeCount Optional count badge (e.g. unread/alerts); renders when > 0.
 */
data class FleetNavItem(
    val key: String,
    val label: String,
    val iconRes: DrawableResource,
    val badgeCount: Int = 0,
)

/**
 * Calm Fintech bottom navigation bar (Compact breakpoint) — labels ALWAYS visible (EN+HI), selected
 * destination carries a `primaryContainer` pill behind the icon with `onPrimaryContainer` icon; rest
 * `onSurfaceVariant`. Surface container, no tonal tint. Fully data-driven via [items].
 *
 * §f9: height is CONTENT-DRIVEN (custom layout, not M3 NavigationBar's fixed 80dp). Single-line labels
 * (EN) → a compact bar (~64dp + safe-area); it grows ONLY when a label genuinely wraps to 2 lines (HI).
 * Icon→label gap is 4dp with trimmed vertical padding. §9.9 no-clip stays: labels [maxLines] = 2.
 */
@Composable
fun FleetBottomNavBar(
    items: List<FleetNavItem>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = cs.surfaceContainer,
        tonalElevation = FleetTokens.Elevation.None,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                // Trimmed vertical padding — the item content (pill + 4dp gap + label) drives height.
                .padding(vertical = FleetTokens.Spacing.XS),
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS),
            verticalAlignment = Alignment.Top,
        ) {
            items.forEach { item ->
                FleetBottomNavItem(
                    item = item,
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item.key) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FleetBottomNavItem(
    item: FleetNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val iconTint = if (selected) cs.onPrimaryContainer else cs.onSurfaceVariant
    val labelColor = if (selected) cs.onSurface else cs.onSurfaceVariant
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(FleetTokens.Radius.M))
            .clickable(onClick = onClick)
            // Keep the row a comfortable touch target without a tall fixed bar.
            .heightIn(min = FleetTokens.Height.MinTouchTarget)
            .padding(vertical = FleetTokens.Spacing.XXS),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS), // 4dp icon→label gap
    ) {
        // Selection pill behind the icon (M3-style ~64×32) — the only tinted surface in the bar.
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(FleetTokens.Radius.Pill))
                .background(if (selected) cs.primaryContainer else Color.Transparent)
                .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.XXS),
            contentAlignment = Alignment.Center,
        ) {
            NavIcon(item, tint = iconTint)
        }
        NavLabel(item.label, color = labelColor)
    }
}

/**
 * Calm Fintech navigation rail. At the **Medium** breakpoint it is the compact icon-above-label
 * rail (vertical twin of [FleetBottomNavBar]); at **Expanded** ([expanded] = true) it widens into a
 * full-label rail with the label BESIDE the icon (step-5 Finding-4 / c3).
 */
@Composable
fun FleetNavRail(
    items: List<FleetNavItem>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    if (items.isEmpty()) return
    if (expanded) {
        FleetNavRailExpanded(items, selectedKey, onSelect, modifier)
        return
    }
    val cs = MaterialTheme.colorScheme
    NavigationRail(
        modifier = modifier,
        containerColor = cs.surfaceContainer,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)) {
            items.forEach { item ->
                NavigationRailItem(
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item.key) },
                    icon = { NavIcon(item) },
                    label = { NavLabel(item.label) },
                    alwaysShowLabel = true,
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = cs.onPrimaryContainer,
                        selectedTextColor = cs.onSurface,
                        indicatorColor = cs.primaryContainer,
                        unselectedIconColor = cs.onSurfaceVariant,
                        unselectedTextColor = cs.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

/**
 * Expanded-breakpoint rail (>840dp): a wider surface with each destination as a full-width pill —
 * icon + label side-by-side, start-aligned. Selection = a `primaryContainer` pill spanning the item;
 * same Calm-Fintech semantics as [FleetBottomNavBar] (onPrimaryContainer icon, onSurface label).
 */
@Composable
private fun FleetNavRailExpanded(
    items: List<FleetNavItem>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxHeight().width(248.dp),
        color = cs.surfaceContainer,
        tonalElevation = FleetTokens.Elevation.None,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(FleetTokens.Spacing.S),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS),
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                val iconTint = if (selected) cs.onPrimaryContainer else cs.onSurfaceVariant
                val labelColor = if (selected) cs.onSurface else cs.onSurfaceVariant
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(FleetTokens.Radius.L))
                        .background(if (selected) cs.primaryContainer else Color.Transparent)
                        .clickable { onSelect(item.key) }
                        .heightIn(min = FleetTokens.Height.MinTouchTarget)
                        .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.M),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M),
                ) {
                    NavIcon(item, tint = iconTint)
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = labelColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Shared nav label — §9.9: wraps to TWO lines (Hindi / large font scale), centered; never
 * single-line-truncates. Ellipsis only guards a pathological 3-line overflow.
 */
@Composable
private fun NavLabel(label: String, color: Color = LocalContentColor.current) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun NavIcon(item: FleetNavItem, tint: Color = LocalContentColor.current) {
    val icon = @Composable {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(FleetTokens.IconSize.Default),
        )
    }
    if (item.badgeCount > 0) {
        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) { icon() }
    } else {
        icon()
    }
}
