package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
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
 * destination carries a `primaryContainer` pill indicator with `onPrimaryContainer` icon; rest
 * `onSurfaceVariant`. Surface container, no tonal tint. Fully data-driven via [items].
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
    NavigationBar(
        modifier = modifier,
        containerColor = cs.surface,
        tonalElevation = FleetTokens.Elevation.None,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.key == selectedKey,
                onClick = { onSelect(item.key) },
                icon = { NavIcon(item) },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
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

/**
 * Calm Fintech navigation rail (Medium / Expanded breakpoint) — vertical twin of [FleetBottomNavBar].
 */
@Composable
fun FleetNavRail(
    items: List<FleetNavItem>,
    selectedKey: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val cs = MaterialTheme.colorScheme
    NavigationRail(
        modifier = modifier,
        containerColor = cs.surface,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)) {
            items.forEach { item ->
                NavigationRailItem(
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item.key) },
                    icon = { NavIcon(item) },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
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

@Composable
private fun NavIcon(item: FleetNavItem) {
    val icon = @Composable {
        Icon(
            painter = painterResource(item.iconRes),
            contentDescription = item.label,
            modifier = Modifier.size(FleetTokens.IconSize.Default),
        )
    }
    if (item.badgeCount > 0) {
        BadgedBox(badge = { Badge { Text(item.badgeCount.toString()) } }) { icon() }
    } else {
        icon()
    }
}
