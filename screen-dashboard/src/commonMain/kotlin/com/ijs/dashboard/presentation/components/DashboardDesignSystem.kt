package com.ijs.dashboard.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetSectionHeader
import org.jetbrains.compose.resources.DrawableResource

/**
 * Dashboard aliases for the shared Fleet* section components (now promoted to
 * ijs-ui-components-lib). Kept as thin internal delegates so the dashboard's many call sites stay
 * unchanged while the implementation lives in one place app-wide.
 */

@Composable
internal fun DashboardSectionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = FleetSectionCard(modifier = modifier, onClick = onClick, content = content)

@Composable
internal fun DashboardSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    iconRes: DrawableResource? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) = FleetSectionHeader(title, modifier, emoji, iconRes, accent, actionLabel, onActionClick)

@Composable
internal fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    emoji: String? = null,
    iconRes: DrawableResource? = null,
    subLabel: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) = FleetMetricTile(value, label, modifier, accent, emoji, iconRes, subLabel, valueColor, onClick = onClick)

@Composable
internal fun AccentIconChip(
    accent: Color,
    chipSize: Dp,
    iconSize: Dp,
    emoji: String? = null,
    iconRes: DrawableResource? = null
) = FleetAccentIconChip(accent, chipSize, iconSize, emoji, iconRes)
