package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint

/**
 * Data-driven tab definition.
 *
 * @param T Identifier type for the tab.
 * @property id Unique identifier returned in [FleetTabBar]'s callback.
 * @property label Display label.
 * @property badgeCount Optional badge count. Renders when > 0.
 */
data class FleetTab<T>(
    val id: T,
    val label: String,
    val badgeCount: Int = 0
)

/**
 * The canonical tab bar for the entire application.
 *
 * ### Adaptive scroll
 * On [FleetBreakpoint.Compact] the tab row is scrollable.
 * On Medium / Expanded it is fixed and distributes tabs evenly.
 *
 * ### Active indicator
 * A bottom border in the brand colour indicates the active tab.
 * No background fill on tabs.
 *
 * ### Badge support
 * Each [FleetTab] carries an optional [FleetTab.badgeCount].
 * When > 0 a badge renders on the tab label.
 *
 * ### Fully data-driven
 * Tabs are a list of [FleetTab] objects. No labels are hardcoded.
 *
 * @param T Identifier type for tabs.
 * @param tabs List of tab definitions.
 * @param selectedTabId Currently selected tab identifier (caller owns state).
 * @param onTabSelected Callback with the id of the tapped tab.
 * @param modifier Modifier for the tab row.
 */
@Composable
fun <T> FleetTabBar(
    tabs: List<FleetTab<T>>,
    selectedTabId: T,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tabs.isEmpty()) return

    val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)

    BoxWithConstraints {
        val bp = rememberFleetBreakpoint()

        val indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (bp is FleetBreakpoint.Compact) {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                modifier = modifier,
                edgePadding = FleetTokens.Spacing.L,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = indicator,
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    FleetTabItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(tab.id) }
                    )
                }
            }
        } else {
            TabRow(
                selectedTabIndex = selectedIndex,
                modifier = modifier,
                containerColor = MaterialTheme.colorScheme.surface,
                indicator = indicator,
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    FleetTabItem(
                        tab = tab,
                        selected = index == selectedIndex,
                        onClick = { onTabSelected(tab.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> FleetTabItem(
    tab: FleetTab<T>,
    selected: Boolean,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = FleetTokens.Spacing.S)
            .semantics { contentDescription = tab.label },
        text = {
            if (tab.badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(tab.badgeCount.toString())
                        }
                    }
                ) {
                    TabLabel(tab.label, selected)
                }
            } else {
                TabLabel(tab.label, selected)
            }
        },
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TabLabel(label: String, selected: Boolean) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
    )
}
