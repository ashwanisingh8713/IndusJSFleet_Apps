package com.ijs.trip.presentation.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.trip.domain.entity.TripStatus
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Reusable section card with icon header used across trip detail sections.
 */
@Composable
internal fun EnhancedSectionCard(
    title: String,
    iconRes: DrawableResource,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetTitledSectionCard(title = title, iconRes = iconRes, content = content)
}

/**
 * Reusable info row with a monochrome vector leading icon, label, and value.
 * [iconRes] null renders the row with no leading icon (e.g. the priority row).
 */
@Composable
internal fun EnhancedInfoRow(
    iconRes: DrawableResource?,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FleetTokens.Spacing.S),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.4f)
        ) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.S)
                )
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = FleetTokens.Height.Divider
        )
    }
}

/**
 * Enhanced status badge for trip status display.
 */
@Composable
internal fun EnhancedStatusBadge(
    status: TripStatus,
    onClick: () -> Unit,
    stateLabels: Map<String, String> = emptyMap()
) {
    val colorScheme = TripStatus.getColorScheme(status)
    val baseColor = com.indusjs.uicomponents.components.stateColorSchemeToChipColor(colorScheme)
    val containerColor = baseColor.copy(alpha = 0.15f)
    val iconText = TripStatus.getIcon(status)
    val apiValue = TripStatus.toApiString(status)
    val text = stateLabels[apiValue] ?: TripStatus.getDisplayLabel(status)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(FleetTokens.Radius.XXL),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.S),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = iconText, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = baseColor
            )
            if (status == TripStatus.PLANNED) {
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = baseColor
                )
            }
        }
    }
}


/**
 * Dropdown field for edit mode selections.
 */
@Composable
internal fun EditDropdownField(
    label: String,
    selectedText: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    error: String?,
    icon: String
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onToggle() },
            shape = RoundedCornerShape(FleetTokens.Radius.L),
            color = if (error != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(FleetTokens.Spacing.L),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(if (isExpanded) "▲" else "▼", style = MaterialTheme.typography.labelMedium)
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = FleetTokens.Spacing.L, top = FleetTokens.Spacing.XS)
            )
        }
    }
}

/**
 * Returns emoji icon for trip priority level.
 */
internal fun getPriorityIcon(priority: String?): String {
    return when (priority?.lowercase()) {
        "high" -> "🔴"
        "medium" -> "🟡"
        "low" -> "🟢"
        else -> "⚪"
    }
}

